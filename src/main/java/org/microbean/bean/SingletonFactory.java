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

public interface SingletonFactory<I> extends Factory<I> {

  @Override // Factory<I>
  public I singleton();  
  
  @Override // Factory<I>
  public default boolean destroys() {
    return false;
  }

  @Override // Factory<I>
  public default void destroy(final I ignoredSingleton) {

  }

  @Override // Factory<I>
  public default void destroy(final I ignoredSingleton, final Destruction ignoredDestruction) {

  }
  
}
