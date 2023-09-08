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
 * An {@link AutoCloseable} {@link WeakReference} that formally disposes of referents after they have been {@linkplain
 * #clear() cleared} by the Java Virtual Machine during garbage collection.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #DisposableReference(Object, Consumer)
 */
public final class DisposableReference<R> extends WeakReference<R> implements AutoCloseable {


  /*
   * Static fields.
   */


  /**
   * A {@link Logger} for instances of this class.
   *
   * <p>The {@link Logger}'s name is equal to this class' {@linkplain Class#getName() name}.</p>
   */
  private static final Logger LOGGER = System.getLogger(DisposableReference.class.getName());

  /**
   * A {@link VarHandle} providing access to the {@link #disposed} field.
   *
   * @nullability This field is never {@code null}.
   *
   * @see #disposed
   *
   * @see #dispose()
   *
   * @see #disposed()
   */
  private static final VarHandle DISPOSED;

  static {
    try {
      DISPOSED = MethodHandles.lookup().findVarHandle(DisposableReference.class, "disposed", boolean.class);
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
   * @see #DisposableReference(Object, Consumer)
   */
  private final R referent;

  /**
   * A {@link Consumer} used to dispose of the {@linkplain #referent referent} at the appropriate time.
   *
   * @nullability This field may be {@code null}.
   */
  private final Consumer<? super R> disposer;

  /**
   * Whether or not the {@link #dispose()} method has been called successfully.
   *
   * @see #dispose()
   *
   * @see #disposed()
   *
   * @see #DISPOSED
   */
  private volatile boolean disposed;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link DisposableReference}.
   *
   * @param referent the referent; may be {@code null}
   *
   * @param disposer a thread-safe {@link Consumer} whose {@link Consumer#accept(Object) accept(Object)} method, which
   * must be idempotent, will be invoked from a separate thread to dispose of the referent after it has been {@linkplain
   * #clear() cleared} by the Java Virtual Machine during garbage collection; may be {@code null} in which case no
   * destruction will take place
   */
  public DisposableReference(final R referent, final Consumer<? super R> disposer) {
    super(Objects.requireNonNull(referent, "referent"), ReferenceQueue.INSTANCE);
    this.referent = referent;
    this.disposer = disposer == null ? DisposableReference::noopDispose : disposer;
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
   * accept(Object)} method on the {@link Consumer} representing the disposer {@linkplain #DisposableReference(Object,
   * Consumer) supplied at construction time}, thus notionally disposeing the {@linkplain #DisposableReference(Object,
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
   * <p>This method is often called from a thread dedicated to disposeing {@linkplain #enqueue() enqueued} {@link
   * DisposableReference}s, so the {@link Consumer} {@linkplain #DisposableReference(Object, Consumer) supplied at
   * construction time} must be thread-safe.</p>
   *
   * @return {@code true} if this invocation of this method caused destruction to happen successfully; {@code false} in
   * all other cases
   *
   * @exception RuntimeException if an invocation of the {@link Consumer#accept(Object) accept(Object)} method on the
   * {@link Consumer} {@linkplain #DisposableReference(Object, Consumer) supplied at construction time} fails
   *
   * @idempotency This method is idempotent.
   *
   * @threadsafety This method is safe for concurrent use by multiple threads.
   *
   * @see #DisposableReference(Object, Consumer)
   */
  public final boolean dispose() {
    if (DISPOSED.compareAndSet(this, false, true)) { // volatile write; assume disposer success
      try {
        this.disposer.accept(this.referent);
      } catch (RuntimeException | Error e) {
        DISPOSED.setVolatile(false); // volatile write; oops; our optimism was misplaced
        throw e;
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns {@code true} if and only if there has been a prior successful invocation of {@link #dispose()} that
   * returned {@code true}.
   *
   * @return {@code true} if and only if there has been a prior successful invocation of {@link #dispose()} that
   * returned {@code true}
   *
   * @see #dispose()
   */
  public final boolean disposed() {
    return this.disposed; // volatile read
  }


  /*
   * Static methods.
   */


  private static final <R> void noopDispose(final R r) {
    if (LOGGER.isLoggable(DEBUG)) {
      LOGGER.log(DEBUG, "DisposableReference referent " + r + " has been cleared");
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
      final Thread t = new Thread(ReferenceQueue.INSTANCE, "DisposableReference disposer");
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
          ((DisposableReference<?>)this.remove()).dispose();
        } catch (final InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }

  }

}
