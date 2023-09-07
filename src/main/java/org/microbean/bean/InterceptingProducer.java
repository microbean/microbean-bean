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

import java.util.Objects;

// A Producer that applies constructor interceptions, so useful only when constructor interception is supposed to be
// happening. Used, like all Producers, as part of implementing a Factory.
//
// Intended mainly for generated subclasses.
public abstract class InterceptingProducer<I> implements Producer<I> {

  protected final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
  
  protected InterceptingProducer() {
    super();
  }

  @Override // Producer<I>
  public final I produce(final Creation<I> c, final References<?> r) {
    return this.produce(this.parameters(c, r));
  }

  protected Object[] parameters(final Creation<I> c, final References<?> r) {
    return EMPTY_OBJECT_ARRAY;
  }
  
  protected abstract I produce(final Object[] parameters);
  
}
