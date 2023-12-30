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

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class DefaultAutoCloseableRegistry implements AutoCloseableRegistry {

  // @GuardedBy("this")
  private Set<AutoCloseable> closeables;

  public DefaultAutoCloseableRegistry() {
    super();
  }

  @Override // Cloneable
  public DefaultAutoCloseableRegistry clone() {
    final DefaultAutoCloseableRegistry dacr;
    try {
      dacr = (DefaultAutoCloseableRegistry)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new AssertionError(e.getMessage(), e);
    }
    dacr.closeables = null;
    this.register(dacr); // critical
    return dacr;
  }

  @Override // AutoCloseableRegistry
  public final void close() {
    final Set<? extends AutoCloseable> closeables;
    synchronized (this) {
      closeables = this.closeables;
      if (closeables == null) {
        this.closeables = Set.of(); // prevent registrations
        return;
      } else if (this.closed()) {
        return;
      }
      this.closeables = Set.of(); // prevent recursion; causes closed() to return true
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
      } catch (final Exception e) {
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
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

  @Override // AutoCloseable
  public final synchronized boolean closed() {
    return this.closeables == Set.<AutoCloseable>of();
  }

  @Override // AutoCloseableRegistry
  public final boolean register(final AutoCloseable closeable) {
    if (Objects.requireNonNull(closeable, "closeable") == this) {
      throw new IllegalArgumentException("closeable == this");
    }
    synchronized (this) {
      if (this.closeables == null) {
        this.closeables = new LinkedHashSet<>();
      }
      return !this.closed() && this.closeables.add(closeable);
    }
  }

  public final synchronized int size() {
    return this.closeables == null ? 0 : this.closeables.size();
  }

}
