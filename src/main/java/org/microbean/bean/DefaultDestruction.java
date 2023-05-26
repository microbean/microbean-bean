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

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import java.util.concurrent.CopyOnWriteArraySet;

import java.util.concurrent.atomic.AtomicReference;

public class DefaultDestruction implements Dependents, Destruction {

  private final References<Object> references;

  private final AtomicReference<Collection<AutoCloseable>> dependents;
  
  public DefaultDestruction(final References<Object> references) {
    super();
    this.references = Objects.requireNonNull(references, "references");
    this.dependents = new AtomicReference<>();
  }

  @Override // Dependents
  public void add(final AutoCloseable dependent) {
    if (dependent != null) {
      // We might receive an immutable Collection here (see close()) which indicates that this Destruction has been
      // released/freed/discarded/closed. Such a Collection will in such a case throw an UnsupportedOperationException,
      // which is conveniently exactly what we want.
      this.dependents.updateAndGet(DefaultDestruction::newIfNeeded).add(dependent);
    }
  }

  @Override // ReferencesProvider
  public final References<Object> references() {
    return this.references;
  }

  @Override // Destruction
  public void close() {
    // We replace the Collection<AutoCloseable> held by the AtomicReference with an immutable empty set.  This will
    // cause reentrances into this close() method to turn into no-ops (which we want), and will cause add() to fail
    // (which we also want).
    final Collection<? extends AutoCloseable> dependents = this.dependents.getAndUpdate(DefaultDestruction::returnEmptySet);
    if (dependents != null && !dependents.isEmpty()) {
      RuntimeException re = null;
      for (final AutoCloseable dependent : dependents) {
        try {
          dependent.close();
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

  }

  private static final Collection<AutoCloseable> newIfNeeded(final Collection<AutoCloseable> old) {
    return old == null ? new CopyOnWriteArraySet<>() : old;
  }

  private static final Collection<AutoCloseable> returnEmptySet(final Collection<AutoCloseable> ignored) {
    return Set.of();
  }


}
