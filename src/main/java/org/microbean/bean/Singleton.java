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

import org.microbean.development.annotation.OverridingEncouraged;

/**
 * An interface whose implementations return a single (typically
 * cached) object.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #singleton()
 */
public interface Singleton<I> {

  /**
   * If this {@link Singleton} will notionally produce exactly one
   * canonical instance of a product ever from invocation time
   * forward, returns that product instance; if any other state of
   * affairs of any kind holds, or might hold, ever, returns {@code
   * null}.
   *
   * <p>If an implementation of this method returns a non-{@code null}
   * instance, the caller may reasonably expect that the returned
   * instance will be cached somewhere for the lifetime of the Java
   * virtual machine.  Callers may reasonably therefore never call
   * this method again, and implementations must not assume that they
   * will.</p>
   *
   * <p>Once an implementation of this method returns a non-{@code
   * null} value, it must return that same value for subsequent
   * invocations regardless of calling thread.  Any other behavior is
   * undefined.</p>
   *
   * <p>If an implementation of this method returns {@code null}, it
   * is not obliged to return {@code null} forever afterward.</p>
   *
   * <p>The product returned by an implementation of this method is
   * considered to be a contextual instance, but not necessarily a
   * contextual reference, that is fully initialized and intercepted
   * and otherwise ready for use.</p>
   *
   * <p>The default implementation of this method returns {@code
   * null}.</p>
   *
   * @return the one instance this {@link Singleton} implementation
   * will produce during its lifetime, or {@code null} in all other
   * conceivable cases
   *
   * @nullability Implementations of this method and their overrides
   * may and often will return {@code null}.
   *
   * @threadsafety Implementations of this method must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency No guarantees of idempotency or determinism are made
   * of implementations of this method, save for the fact that once an
   * implementation of this method returns a non-{@code null} value it
   * must forever after return the same value regardless of caller
   * thread.
   */
  @OverridingEncouraged
  public default I singleton() {
    return null;
  }

}
