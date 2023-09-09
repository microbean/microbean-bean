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

/**
 * A representation of a {@link Factory}'s {@linkplain Factory#create(Creation, ReferenceSelector) creation activity}.
 *
 * <p>Most {@link Creation} implementations will, and should, also be {@link AutoCloseableRegistry} implementations.
 * See {@link DefaultCreation} as one arbitrary example.</p>
 *
 * @author <a href="https://about.me/lairdnelson" target="_parent">Laird Nelson</a>
 */
public interface Creation<I> extends AutoCloseable, Cloneable {

  /**
   * Clones this {@link Creation} and, critically, if this {@link Creation} implementation implements {@link
   * AutoCloseableRegistry}, arranges to have the resulting clone {@linkplain
   * AutoCloseableRegistry#register(AutoCloseable) registered with} its immediate ancestor (this {@link Creation}) such
   * that {@linkplain #close() closing} the ancestor (this {@link Creation}) will also {@linkplain #close() close} the
   * clone.
   *
   * <p>If an implementation of this method does not adhere to these requirements, undefined behavior, and possibly
   * memory leaks, will occur.</p>
   *
   * @return a clone of this {@link Creation}; never {@code null}
   *
   * @see #close()
   *
   * @see AutoCloseableRegistry
   *
   * @see AutoCloseableRegistry#register(AutoCloseable)
   *
   * @threadsafety Implementations of this method must be safe for concurrent use by multiple threads.
   */
  public Creation<I> clone();

  /**
   * Closes this {@link Creation}, and, if this {@link Creation} implementation implements {@link
   * AutoCloseableRegistry}, any clones that were {@linkplain AutoCloseableRegistry#register(AutoCloseable) registered
   * with it}.
   *
   * <p>{@link Factory} implementations (and other user-authored code) normally should not invoke this method, and
   * indeed doing so may result in undefined behavior and/or an {@link IllegalStateException} being thrown.</p>
   *
   * <p>Invoking this method during actual creation may cause undefined behavior and/or an {@link IllegalStateException}
   * to be thrown.</p>
   *
   * @exception IllegalStateException if it is not legal yet to close this {@link Creation}
   *
   * @see #clone()
   *
   * @see AutoCloseableRegistry
   *
   * @idempotency Implementations of this method must be idempotent.
   *
   * @threadsafety Implementations of this method must be safe for concurrent use by multiple threads.
   */
  // MUST be idempotent
  // During creation (as opposed to destruction) this method should throw an IllegalStateException.
  @Override // AutoCloseable
  public void close();

  /**
   * Casts this {@link Creation} to the inferred return type and returns it.
   *
   * <p>Overrides of this default method must return this {@link Creation} cast to the apppropriate type. Any other
   * return value will result in undefined behavior.</p>
   *
   * @return this {@link Creation}, cast to an appropriate type; never {@code null}
   *
   * @exception ClassCastException if the cast could not be performed for any reason
   */
  public default <J> Creation<J> cast() {
    return cast(this);
  }

  /**
   * Signals that the supplied {@code instance} has been created and is about to be made available for use.
   *
   * <p>This method is typically invoked from within a {@link Factory#create(Creation, ReferenceSelector)} implementation
   * immediately prior to its returning a value.</p>
   *
   * <p>The default implementation of this method does nothing.</p>
   *
   * @param instance the instance that was created; must not be {@code null}
   *
   * @exception NullPointerException if {@code instance} was {@code null}
   *
   * @exception IllegalArgumentException if {@code instance} was found to be unsuitable for any reason
   *
   * @idempotency Overrides of this method must be idempotent.
   *
   * @threadsafety Overrides of this method must be safe for concurrent use by multiple threads.
   */
  // MUST be idempotent
  // For incomplete instances
  public default void created(final I instance) {

  }

  /**
   * Casts the supplied {@link Creation} to the inferred return type and returns it.
   *
   * @param c the {@link Creation} to cast and return; may be {@code null}
   *
   * @return the {@link Creation}, cast to an appropriate type, or {@code null}
   */
  @SuppressWarnings("unchecked")
  public static <I> Creation<I> cast(final Creation<?> c) {
    return (Creation<I>)c;
  }

}
