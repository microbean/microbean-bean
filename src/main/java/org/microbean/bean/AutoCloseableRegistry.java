/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2024 microBean™.
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
 * A {@link Cloneable} reigstry of {@link AutoCloseable} instances that itself is {@link AutoCloseable}.
 *
 * <p>{@linkplain #close() Closing} an {@link AutoCloseableRegistry} {@linkplain AutoCloseable#close() closes} its
 * registrants.</p>
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 *
 * @see #register(AutoCloseable)
 */
public interface AutoCloseableRegistry extends AutoCloseable, Cloneable {

  /**
   * Returns a new {@link AutoCloseableRegistry} instance that is not {@linkplain #closed() closed} and has no
   * {@linkplain #register(AutoCloseable)} registrations.
   *
   * <p>The new instance will be {@linkplain #register(AutoCloseable) registered} with this {@link
   * AutoCloseableRegistry} if this {@link AutoCloseableRegistry} is not {@linkplain #closed() closed}.</p>
   *
   * @return a new {@link AutoCloseableRegistry}
   *
   * @nullability Implementations of this method must not return {@code null}.
   *
   * @idempotency All invocations of implementations of this method must return new, distinct {@link AutoCloseable}
   * instances.
   *
   * @threadsafety Implementations of this method must be safe for concurrent use by multiple threads.
   *
   * @see Cloneable
   *
   * @see #closed()
   *
   * @see #register(AutoCloseable)
   */
  public AutoCloseableRegistry clone();

  /**
   * Closes this {@link AutoCloseableRegistry} and {@linkplain AutoCloseable#close() closes} its {@linkplain
   * #register(AutoCloseable) registrants}.
   *
   * @idempotency Implementations of this method must be idempotent.
   *
   * @threadsafety Implementations of this method must be safe for concurrent use by multiple threads.
   */
  @Override // AutoCloseable
  public void close();

  /**
   * Returns {@code true} if and only if this {@link AutoCloseableRegistry} has been {@linkplain #close() closed}.
   *
   * @return {@code true} if and only if this {@link AutoCloseableRegistry} has been {@linkplain #close() closed}
   *
   * @idempotency Implementations of this method must be idempotent.
   *
   * @threadsafety Implementations of this method must be safe for concurrent use by multiple threads.
   */
  public boolean closed();

  /**
   * If this {@link AutoCloseableRegistry} is not {@linkplain #closed() closed}, and if the supplied {@link
   * AutoCloseable} has not yet been registered, registers it such that it will be {@linkplain AutoCloseable#close()
   * closed} when this {@link AutoCloseableRegistry} is {@linkplain #close() closed}, and returns {@code true}.
   *
   * <p>This method takes no action and returns {@code false} in all other cases.</p>
   *
   * @param c the {@link AutoCloseable} to register; must not be {@code null}
   *
   * @return {@code true} if and only if this {@link AutoCloseableRegistry} is not {@linkplain #closed() closed} and the
   * supplied {@link AutoCloseable} is not already registered and registration completed successfully; {@code false} in
   * all other cases
   *
   * @exception NullPointerException if {@code c} is {@code null}
   */
  public boolean register(final AutoCloseable c);

}
