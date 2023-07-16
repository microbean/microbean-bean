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

public interface Creation<I> extends AutoCloseable, Cloneable {

  // MUST be idempotent
  // For incomplete instances
  public default void created(final I instance) {}

  public Creation<I> clone();

  // MUST be idempotent
  // During creation (as opposed to destruction) this method should throw an IllegalStateException.
  @Override // AutoCloseable
  public void close();

}
