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
package org.microbean.bean2;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.microbean.bean2.BeanException;

public class DefaultDestructionRegistry implements AutoCloseable, AutoCloseableRegistry {

  private Set<AutoCloseable> closeables;

  public DefaultDestructionRegistry() {
    super();
  }
  
  @Override // AutoCloseable
  public final void close() {
    RuntimeException re = null;
    synchronized (this) {
      if (this.closeables == null) {
        this.closeables = Set.of();
        return;
      } else if (this.closeables == Set.<AutoCloseable>of()) {
        return;
      }
      final Set<? extends AutoCloseable> closeables = this.closeables;
      this.closeables = Set.of(); // prohibit recursion
      for (final AutoCloseable c : closeables) {
        try {
          c.close();
        } catch (final Exception e) {
          if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
          }
          if (re == null) {
            re = e instanceof RuntimeException r ? r : new BeanException(e.getMessage(), e);
          } else {
            re.addSuppressed(e);
          }
        }
      }
    }
    if (re != null) {
      throw re;
    }
  }

  public synchronized final boolean closed() {
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
      } else if (this.closeables == Set.<AutoCloseable>of()) {
        throw new IllegalStateException();
      }
      return this.closeables.add(closeable);
    }
  }

  public synchronized final int size() {
    return this.closeables == null ? 0 : this.closeables.size();
  }

  @Override
  public final int hashCode() {
    return super.hashCode();
  }

  @Override
  public final boolean equals(final Object other) {
    return super.equals(other);
  }

}
