/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.microbean.bean;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import java.lang.System.Logger;

import java.lang.ref.WeakReference;

import java.util.Objects;

import java.util.function.Consumer;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * An {@link AutoCloseable} {@link WeakReference} that destroys referents after they have been {@linkplain #clear()
 * cleared} by the Java Virtual Machine during garbage collection.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #DependentReference(Object, Consumer)
 */
public final class DependentReference<R> extends WeakReference<R> implements AutoCloseable {


  /*
   * Static fields.
   */


  /**
   * A {@link Logger} for instances of this class.
   *
   * <p>The {@link Logger}'s name is equal to this class' {@linkplain Class#getName() name}.</p>
   */
  private static final Logger LOGGER = System.getLogger(DependentReference.class.getName());

  /**
   * A {@link VarHandle} providing access to the {@link #destroyd} field.
   *
   * @nullability This field is never {@code null}.
   *
   * @see #destroyed
   *
   * @see #destroy()
   *
   * @see #destroyed()
   */
  private static final VarHandle DESTROYED;

  static {
    try {
      DESTROYED = MethodHandles.lookup().findVarHandle(DependentReference.class, "destroyed", boolean.class);
    } catch (final NoSuchFieldException | IllegalAccessException e) {
      throw (Error)new ExceptionInInitializerError(e.getMessage()).initCause(e);
    }
  }


  /*
   * Instance fields.
   */


  /**
   * The referent initially retrievable via the {@link #get()} method, but in a way that does not prevent weak
   * reachability.
   *
   * @nullability This field may be {@code null}.
   *
   * @see #DependentReference(Object, Consumer)
   */
  private final R referent;

  /**
   * A {@link Consumer} used to destroy the {@linkplain #referent referent} at the appropriate time.
   *
   * @nullability This field may be {@code null}.
   */
  private final Consumer<? super R> destructor;

  /**
   * Whether or not the {@link #destroy()} method has been called successfully.
   *
   * @see #destroy()
   *
   * @see #destroyed()
   *
   * @see #DESTROYED
   */
  private volatile boolean destroyed;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link DependentReference}.
   *
   * @param referent the referent; may be {@code null}
   *
   * @param destructor a thread-safe {@link Consumer} whose {@link Consumer#accept(Object) accept(Object)} method, which
   * must be idempotent, will be invoked from a separate thread to destroy the referent after it has been {@linkplain
   * #clear() cleared} by the Java Virtual Machine during garbage collection; may be {@code null} in which case no
   * destruction will take place
   */
  public DependentReference(final R referent, final Consumer<? super R> destructor) {
    super(Objects.requireNonNull(referent, "referent"), ReferenceQueue.INSTANCE);
    this.referent = referent;
    this.destructor = destructor == null ? DependentReference::noopDestroy : destructor;
  }


  /*
   * Instance methods.
   */


  /**
   * Calls {@link #enqueue()}.
   *
   * @see #enqueue()
   */
  @Override // AutoCloseable
  public final void close() {
    this.enqueue(); // idempotent
  }

  /**
   * Calls {@link #refersTo(Object) refersTo(null)} and returns the result.
   *
   * @return the result of an invocation of {@link #refersTo(Object) refersTo(null)}
   *
   * @see #refersTo(Object)
   */
  public final boolean closed() {
    return this.refersTo(null);
  }

  /**
   * If there has been no prior successful invocation of this method, calls the {@link Consumer#accept(Object)
   * accept(Object)} method on the {@link Consumer} representing the destructor {@linkplain #DependentReference(Object,
   * Consumer) supplied at construction time}, thus notionally destroying the {@linkplain #DependentReference(Object,
   * Consumer) referent supplied at construction time}, and returns {@code true}.
   *
   * <p>Destruction does not imply {@linkplain #close() closing}, and closing does not imply destruction (though it
   * normally will eventually lead to it).</p>
   *
   * <p>If destruction does not result in any {@link RuntimeException} or {@link Error} being thrown, then calling this
   * method again, from any thread, will have no effect and it will return {@code false}.</p>
   *
   * <p>If the first invocation of this method from any thread succeeds, then it will return {@code true}, and all other
   * invocations of this method from any thread will return {@code false}, and will have no effect.</p>
   *
   * <p>This method is often called from a thread dedicated to destroying {@linkplain #enqueue() enqueued} {@link
   * DependentReference}s, so the {@link Consumer} {@linkplain #DependentReference(Object, Consumer) supplied at
   * construction time} must be thread-safe.</p>
   *
   * @return {@code true} if this invocation of this method caused destruction to happen successfully; {@code false} in
   * all other cases
   *
   * @exception RuntimeException if an invocation of the {@link Consumer#accept(Object) accept(Object)} method on the
   * {@link Consumer} {@linkplain #DependentReference(Object, Consumer) supplied at construction time} fails
   *
   * @idempotency This method is idempotent.
   *
   * @threadsafety This method is safe for concurrent use by multiple threads.
   *
   * @see #DependentReference(Object, Consumer)
   */
  public final boolean destroy() {
    if (DESTROYED.compareAndSet(this, false, true)) { // volatile write; assume destructor success
      try {
        this.destructor.accept(this.referent);
      } catch (RuntimeException | Error e) {
        DESTROYED.setVolatile(false); // volatile write; oops; our optimism was misplaced
        throw e;
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns {@code true} if and only if there has been a prior successful invocation of {@link #destroy()} that
   * returned {@code true}.
   *
   * @return {@code true} if and only if there has been a prior successful invocation of {@link #destroy()} that
   * returned {@code true}
   *
   * @see #destroy()
   */
  public final boolean destroyed() {
    return this.destroyed; // volatile read
  }


  /*
   * Static methods.
   */


  private static final <R> void noopDestroy(final R r) {
    if (LOGGER.isLoggable(DEBUG)) {
      LOGGER.log(DEBUG, "DependentReference referent " + r + " has been cleared");
    }
  }


  /*
   * Inner and nested classes.
   */


  private static final class ReferenceQueue extends java.lang.ref.ReferenceQueue<Object> implements Runnable {


    /*
     * Static fields.
     */


    private static final ReferenceQueue INSTANCE = new ReferenceQueue();

    static {
      final Thread t = new Thread(ReferenceQueue.INSTANCE, "DependentReference destroyer");
      t.setDaemon(true);
      t.setPriority(3); // a little less important than the default
      t.start();
    }


    /*
     * Constructors.
     */


    private ReferenceQueue() {
      super();
    }


    /*
     * Instance methods.
     */


    @Override // Runnable
    public final void run() {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          ((DependentReference<?>)this.remove()).destroy();
        } catch (final InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }

  }

}
