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

import java.util.Objects;

import java.util.function.Supplier;

public final record Value<I>(I value) implements SingletonFactory<I>, Supplier<I> {

  @Deprecated
  public Value {
    Objects.requireNonNull(value, "value");
  }
  
  @Override // Singleton<I>
  public final I singleton() {
    return this.value();
  }

  @Override // Supplier<I>
  public final I get() {
    return this.value();
  }

  @Override // SingletonFactory<I>
  public final I produce(final Creation<I> ignored) {
    return this.value();
  }

  public static final <I> Value<I> of(final I instance) {
    return new Value<>(instance);
  }
  
}
