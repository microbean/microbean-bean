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

public interface Request {

  /**
   * Returns the {@link Selector} on behalf of which a request is currently happening, or {@code null} if that
   * information is not available.
   *
   * <p>The default implementation of this method returns {@code null}.  Overrides are encouraged.</p>
   *
   * @return the {@link Selector} on behalf of which a request is currently happening, or {@code null} if that
   * information is not available
   *
   * @nullability Implementations of this method may (and often will) return {@code null}.
   *
   * @threadsafety Implementations of this method must be safe for concurrent use by multiple threads.
   *
   * @idempotency Implementations of this method must be idempotent and deterministic.
   */
  public default Selector<?> selector() {
    return null;
  }

  public Request withSelector(final Selector<?> selector);
  
}
