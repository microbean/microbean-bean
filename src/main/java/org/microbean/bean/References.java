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

  public <I> Creation<I> creation();

  public <R> R reference(final Selector selector, final Bean<R> bean, final Creation<R> creation);

  // Destroys r if and only if it is (a) dependent and (b) supplied by get()
  public boolean destroy(final R r);  
  
  public default <R> R reference(final Selector selector, final Creation<R> creation) {
    final Bean<?> b = this.beanSet().bean(selector);
    if (b == null) {
      throw new UnsatisfiedResolutionException(selector);
    }
    return this.reference(selector, b.cast(), creation);
  }

  public default Cardinality cardinality() {
    final Iterator<?> i = this.iterator();
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
