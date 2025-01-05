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

import java.util.LinkedHashSet;
import java.util.Set;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A straightforward {@link AutoCloseableRegistry} implementation.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see AutoCloseableRegistry
 */
public class DefaultAutoCloseableRegistry implements AutoCloseableRegistry {


  /*
   * Instance fields.
   */


  private final Lock lock;

  // @GuardedBy("lock")
  private Set<AutoCloseable> closeables;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link DefaultAutoCloseableRegistry}.
   */
  public DefaultAutoCloseableRegistry() {
    super();
    this.lock = new ReentrantLock();
  }


  /*
   * Instance methods.
   */


  /**
   * Returns a new {@link DefaultAutoCloseableRegistry} instance that is not {@linkplain #closed() closed}, has no
   * {@linkplain #register(AutoCloseable) registrations} yet, and is {@linkplain #register(AutoCloseable) registered}
   * with this {@link DefaultAutoCloseableRegistry}.
   *
   * @return a new, {@linkplain #closed() unclosed} {@link DefaultAutoCloseableRegistry} {@linkplain
   * #register(AutoCloseable) registered} with this {@link DefaultAutoCloseableRegistry}
   *
   * @exception IllegalStateException if this {@link DefaultAutoCloseableRegistry} is {@linkplain #closed() closed}
   *
   * @microbean.nullability This method does not, and its overrides must not, return {@code null}.
   *
   * @microbean.idempotency Overrides of this method must return new, distinct {@link DefaultAutoCloseableRegistry} instances.
   *
   * @microbean.threadsafety This method is, and its overrides must be, safe for concurrent use by multiple threads.
   *
   * @see #register(AutoCloseable)
   */
  @Override // AutoCloseableRegistry
  public DefaultAutoCloseableRegistry newChild() {
    final DefaultAutoCloseableRegistry child = new DefaultAutoCloseableRegistry();
    if (!this.register(child)) { // CRITICAL
      throw new IllegalStateException();
    }
    return child;
  }

  /**
   * Closes this {@link DefaultAutoCloseableRegistry} and {@linkplain AutoCloseable#close() closes} its {@linkplain
   * #register(AutoCloseable) registrants}.
   *
   * <p>{@link AutoCloseable#close()} is called on all {@linkplain #register(AutoCloseable) registrants}, even in the
   * presence of exceptions. {@link RuntimeException}s consequently thrown may therefore {@linkplain
   * Throwable#getSuppressed() contain suppressed exceptions}.</p>
   *
   * <p>Overrides of this method wishing to add semantics to this behavior should perform that work before calling
   * {@link #close() super.close()}.</p>
   *
   * <p>Overrides of this method must call {@link #close() super.close()} or undefined behavior may result.</p>
   *
   * <p>After any successful invocation of this method, an invocation of the {@link #closed()} method will forever after
   * return {@code true}.</p>
   *
   * @microbean.idempotency This method is, and its overrides must be, idempotent.
   *
   * @microbean.threadsafety This method is, and its overrides must be, safe for concurrent use by multiple threads.
   *
   * @see #closed()
   */
  @Override // AutoCloseableRegistry (AutoCloseable)
  public void close() {
    final Set<? extends AutoCloseable> closeables;
    lock.lock();
    try {
      closeables = this.closeables;
      if (closeables == Set.<AutoCloseable>of()) {
        // Already closed
        return;
      }
      this.closeables = Set.of();
    } finally {
      lock.unlock();
    }
    if (closeables == null) {
      // nothing to close
      return;
    }
    RuntimeException re = null;
    for (final AutoCloseable c : closeables) {
      try {
        c.close();
      } catch (final RuntimeException e) {
        if (re == null) {
          re = e;
        } else {
          re.addSuppressed(e);
        }
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        if (re == null) {
          re = new BeanException(e.getMessage(), e);
        } else {
          re.addSuppressed(e);
        }
      } catch (final Exception e) {
        if (re == null) {
          re = new BeanException(e.getMessage(), e);
        } else {
          re.addSuppressed(e);
        }
      }
    }
    if (re != null) {
      throw re;
    }
  }

  /**
   * Returns {@code true} if and only if this {@link DefaultAutoCloseableRegistry} is {@linkplain #close() closed}.
   *
   * <p>Once an invocation of this method has returned {@code true}, on any thread, subsequent invocations must also
   * return {@code true}, on any thread.</p>
   *
   * <p>This method will return {@code false} until an invocation of the {@link #close()} method has successfully
   * completed, and will return {@code true} thereafter.</p>
   *
   * @return {@code true} if and only if this {@link DefaultAutoCloseableRegistry} is {@linkplain #close() closed}
   *
   * @microbean.idempotency This method is idempotent.
   *
   * @microbean.threadsafety This method is safe for concurrent use by multiple threads.
   *
   * @see #close()
   */
  @Override // AutoCloseableRegistry
  public final boolean closed() {
    lock.lock();
    try {
      return this.closeables == Set.<AutoCloseable>of();
    } finally {
      lock.unlock();
    }
  }

  /**
   * If this {@link DefaultAutoCloseableRegistry} is not {@linkplain #closed() closed}, and if the supplied {@link
   * AutoCloseable} has not yet been registered, registers it such that it will be {@linkplain AutoCloseable#close()
   * closed} when this {@link DefaultAutoCloseableRegistry} is {@linkplain #close() closed}, and returns {@code true}.
   *
   * <p>This method takes no action and returns {@code false} in all other cases.</p>
   *
   * @param closeable the {@link AutoCloseable} to register; must not be {@code null}
   *
   * @return {@code true} if and only if this {@link DefaultAutoCloseableRegistry} is not {@linkplain #closed() closed}
   * and the supplied {@link AutoCloseable} is not already registered and registration completed successfully; {@code
   * false} in all other cases
   *
   * @exception NullPointerException if {@code closeable} is {@code null}
   *
   * @microbean.idempotency This method is idempotent.
   *
   * @microbean.threadsafety This method is safe for concurrent use by multiple threads.
   */
  @Override // AutoCloseableRegistry
  public final boolean register(final AutoCloseable closeable) {
    if (closeable == null || closeable == this) {
      return false;
    }
    lock.lock();
    try {
      if (this.closeables == Set.<AutoCloseable>of()) {
        // Already closed
        return false;
      } else if (this.closeables == null) {
        this.closeables = new LinkedHashSet<>();
      }
      return this.closeables.add(closeable);
    } finally {
      lock.unlock();
    }
  }

}
