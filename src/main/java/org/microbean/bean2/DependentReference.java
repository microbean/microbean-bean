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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import java.util.Objects;

import java.util.function.Consumer;

public class DependentReference<R> extends WeakReference<R> implements AutoCloseable {

  private final R referent;

  private final Consumer<? super R> destructor;

  private volatile boolean destroyed;

  public DependentReference(final R referent, final ReferenceQueue<? super R> rq, final Consumer<? super R> destructor) {
    super(Objects.requireNonNull(referent, "referent"), rq);
    this.referent = referent;
    this.destructor = destructor == null ? DependentReference::noopDestroy : destructor;
  }

  @Override // AutoCloseable
  public void close() {
    this.enqueue(); // idempotent
  }

  public boolean closed() {
    return this.refersTo(null);
  }

  public final void destroy() {
    if (!this.destroyed()) {
      this.destructor.accept(this.referent);
      this.destroyed = true;
    }
  }

  public final boolean destroyed() {
    return this.destroyed;
  }

  private static final <R> void noopDestroy(final R r) {}

}
