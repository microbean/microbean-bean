/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2025 microBean™.
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
 * A reigstry of {@link AutoCloseable} instances that itself is {@link AutoCloseable}.
 *
 * <p>{@linkplain #close() Closing} an {@link AutoCloseableRegistry} {@linkplain AutoCloseable#close() closes} its
 * registrants.</p>
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 *
 * @see #register(AutoCloseable)
 */
public interface AutoCloseableRegistry extends AutoCloseable {

  /**
   * Returns a new {@link AutoCloseableRegistry} instance that is not {@linkplain #closed() closed}, has no {@linkplain
   * #register(AutoCloseable) registrations} yet, and is {@linkplain #register(AutoCloseable) registered} with this
   * {@link AutoCloseableRegistry}.
   *
   * <p>The new {@link AutoCloseableRegistry} child therefore functions as an intermediate or leaf node in a tree of
   * {@link AutoCloseable} implementations, such that if any node of the tree is {@linkplain AutoCloseable#close()
   * closed}, all of its descendants will be {@linkplain AutoCloseable#close() closed} as well.</p>
   *
   * @return a new, {@linkplain #closed() unclosed} {@link AutoCloseableRegistry} {@linkplain #register(AutoCloseable)
   * registered} with this {@link AutoCloseableRegistry} that functions as an intermediate or leaf node in a tree of
   * such nodes
   *
   * @exception IllegalStateException if this {@link AutoCloseableRegistry} is {@linkplain #closed() closed}
   *
   * @microbean.nullability Implementations of this method must not return {@code null}.
   *
   * @microbean.idempotency All successful invocations of implementations of this method must return new, distinct {@link
   * AutoCloseableRegistry} instances.
   *
   * @microbean.threadsafety Implementations of this method must be safe for concurrent use by multiple threads.
   *
   * @see #register(AutoCloseable)
   */
  public AutoCloseableRegistry newChild();

  /**
   * Closes this {@link AutoCloseableRegistry} and {@linkplain AutoCloseable#close() closes} its {@linkplain
   * #register(AutoCloseable) registrants}.
   *
   * <p>After any successful invocation of this method, an invocation of the {@link #closed()} method must forever after
   * return {@code true}.</p>
   *
   * @microbean.idempotency Implementations of this method must be idempotent.
   *
   * @microbean.threadsafety Implementations of this method must be safe for concurrent use by multiple threads.
   *
   * @see #closed()
   */
  @Override // AutoCloseable
  public void close();

  /**
   * Returns {@code true} if and only if this {@link AutoCloseableRegistry} is {@linkplain #close() closed}.
   *
   * <p>Once an invocation of this method has returned {@code true}, on any thread, subsequent invocations must also
   * return {@code true}, on any thread.</p>
   *
   * <p>An implementation of this method must return {@code false} until an invocation of the {@link #close()} method
   * has successfully completed, and must return {@code true} thereafter.</p>
   *
   * @return {@code true} if and only if this {@link AutoCloseableRegistry} is {@linkplain #close() closed}
   *
   * @microbean.idempotency Implementations of this method must be idempotent.
   *
   * @microbean.threadsafety Implementations of this method must be safe for concurrent use by multiple threads.
   *
   * @see #close()
   */
  public boolean closed();

  /**
   * If this {@link AutoCloseableRegistry} is not {@linkplain #closed() closed}, and if the supplied {@link
   * AutoCloseable} has not yet been registered, registers it such that it will be {@linkplain AutoCloseable#close()
   * closed} when this {@link AutoCloseableRegistry} is {@linkplain #close() closed}, and returns {@code true}.
   *
   * <p>This method takes no action and returns {@code false} in all other cases.</p>
   *
   * @param closeable the {@link AutoCloseable} to register; must not be {@code null}
   *
   * @return {@code true} if and only if this {@link AutoCloseableRegistry} is not {@linkplain #closed() closed} and the
   * supplied {@link AutoCloseable} is not already registered and registration completed successfully; {@code false} in
   * all other cases
   *
   * @exception NullPointerException if {@code closeable} is {@code null}
   *
   * @microbean.idempotency Implementations of this method must be idempotent.
   *
   * @microbean.threadsafety Implementations of this method must be safe for concurrent use by multiple threads.
   */
  public boolean register(final AutoCloseable closeable);

}
