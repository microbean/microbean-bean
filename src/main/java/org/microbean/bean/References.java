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

import java.util.Iterator;

import java.util.function.Supplier;

import javax.lang.model.type.TypeMirror;

public interface References<R> extends AutoCloseable, Iterable<R>, Supplier<R> {

  public BeanSet beanSet();

  public default <R> R supplyReference(final Selector selector) {
    return this.supplyReference(selector, null);
  }

  public <R> R supplyReference(final Selector selector, final Bean<R> bean);

  public default Cardinality cardinality() {
    final Iterator<R> i = this.iterator();
    if (i.hasNext()) {
      i.next();
      return i.hasNext() ? Cardinality.MANY : Cardinality.ONE;
    }
    return Cardinality.ZERO;
  }

  @Override // Supplier<R>
  public default R get() {
    final Iterator<R> iterator = this.iterator();
    if (!iterator.hasNext()) {
      throw new UnsatisfiedResolutionException();
    }
    final R returnValue = iterator.next();
    if (iterator.hasNext()) {
      throw new AmbiguousResolutionException();
    }
    return returnValue;
  }

  public <R2 extends R> References<R2> withSelector(final Selector selector);

  public boolean destroy(final R r);

  // Overriding encouraged to be more efficient/threadsafe/etc.
  @Override // AutoCloseable
  public default void close() {
    for (final R r : this) {
      this.destroy(r);
    }
  }
  
  public enum Cardinality {

    ZERO,
    ONE,
    MANY;

  }

}
