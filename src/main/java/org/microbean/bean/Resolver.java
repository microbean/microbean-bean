/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2022 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.bean;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;

import java.util.Collection;

import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import java.util.stream.Stream;

public class Resolver implements AutoCloseable {


  /*
   * Static fields.
   */


  private static final VarHandle UNRESOLVED;

  static {
    final Lookup lookup = MethodHandles.lookup();
    try {
      UNRESOLVED = lookup.findVarHandle(Resolver.class, "unresolved", ConcurrentLinkedQueue.class);
    } catch (final NoSuchFieldException | IllegalAccessException reflectiveOperationException) {
      throw (Error)new ExceptionInInitializerError(reflectiveOperationException.getMessage()).initCause(reflectiveOperationException);
    }
  }


  /*
   * Instance fields.
   */


  private volatile ConcurrentLinkedQueue<Alternate> unresolved;

  private final BiFunction<? super Selector, ? super Collection<? extends Alternate>, ? extends Alternate> failureHandler;


  /*
   * Constructors.
   */


  public Resolver() {
    this(Resolver::fail);
  }

  public Resolver(final BiFunction<? super Selector, ? super Collection<? extends Alternate>, ? extends Alternate> failureHandler) {
    super();
    this.failureHandler = failureHandler == null ? Resolver::fail : failureHandler;
  }


  /*
   * Instance methods.
   */


  public final <T extends Alternate> T resolve(final Stream<T> alternates) {
    return this.resolve(null, alternates);
  }
  
  public final <T extends Alternate> T resolve(final Selector selector, final Stream<T> alternates) {
    try (final Stream<T> s = alternates.onClose(this::clear)) {
      return s.reduce(this::reduce).orElse(this.fail(selector));
    }
  }

  protected <T extends Alternate> T reduce(final T t0, final T t1) {
    final T returnValue;
    if (t0 == null || t0 == t1) {
      returnValue = t1;
    } else if (t1 == null) {
      returnValue = t0;
    } else if (t0.alternate()) {
      if (t1.alternate()) {
        final int t0Priority = t0.priority();
        final int t1Priority = t1.priority();
        if (t0Priority == t1Priority) {
          returnValue = null;
        } else if (t0Priority < t1Priority) {
          returnValue = t1;
        } else {
          returnValue = t0;
        }
      } else {
        returnValue = t1;
      }
    } else {
      returnValue = t1.alternate() ? t0 : null;
    }
    if (returnValue == null) {
      this.addUnresolved(t0, t1);
    }
    return returnValue;
  }

  protected <T extends Alternate> void addUnresolved(final T unresolved1, final T unresolved2) {
    final Collection<Alternate> q = this.unresolved();
    q.add(unresolved1);
    q.add(unresolved2);
  }

  @SuppressWarnings("unchecked")
  protected <T extends Alternate> T fail(final Selector selector) {
    final Collection<? extends Alternate> unresolved = this.unresolved; // volatile read
    try {
      return (T)this.failureHandler.apply(selector, unresolved);
    } finally {
      this.clear();
    }
  }

  @Override // AutoCloseable
  public final void close() {
    this.clear();
  }

  protected void clear() {
    final Collection<?> c = this.unresolved; // volatile read
    if (c != null) {
      c.clear();
    }
  }

  private final ConcurrentLinkedQueue<Alternate> unresolved() {
    ConcurrentLinkedQueue<Alternate> q = this.unresolved; // volatile read
    if (q == null) {
      q = new ConcurrentLinkedQueue<>();
      if (!UNRESOLVED.compareAndSet(this, null, q)) { // volatile write
        return this.unresolved; // volatile read
      }
    }
    return q;
  }


  /*
   * Static methods.
   */


  private static final Alternate fail(final Selector s, final Collection<? extends Alternate> u) {
    if (u == null || u.isEmpty()) {
      throw new UnsatisfiedResolutionException(s, "TODO: unsatisfied");
    } else {
      throw new AmbiguousResolutionException(s, u, "TODO: this message needs to be better; can't resolve these alternates: " + u);
    }
  }

  /**
   * If the supplied {@link Collection} of unresolved {@link
   * Alternate}s is {@code null} or {@linkplain Collection#isEmpty()
   * empty}, returns {@code null}.
   *
   * <p>If the supplied {@link Collection} of unresolved {@link
   * Alternate}s is non-{@code null} and is not {@linkplain
   * Collection#isEmpty() empty}, this method throws an {@link
   * AmbiguousResolutionException}.</p>
   *
   * <p>This method exists only so it can be referenced in {@linkplain
   * #Resolver(BiFunction) constructor as a
   * <code>failureHandler</code>}.  It is not used by this class.</p>
   *
   * @param s a {@link Selector}; ignored
   *
   * @param u a {@link Collection} of unresolved {@link Alternate}s;
   * may be {@code null} or {@linkplain Collection#isEmpty() empty} in
   * which case {@code null} will be returned
   *
   * @return {@code null}
   *
   * @exception AmbiguousResolutionException if {@code u} is
   * non-{@code null} and {@linkplain Collection#isEmpty() non-empty}
   *
   * @nullability This method returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static final Alternate returnNull(final Selector s, final Collection<? extends Alternate> u) {
    return u == null || u.isEmpty() ? null : fail(s, u);
  }

}
