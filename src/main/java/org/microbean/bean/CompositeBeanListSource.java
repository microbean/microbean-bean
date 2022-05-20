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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CompositeBeanListSource implements BeanListSource {

  private final List<Bean<?>> beans;

  private CompositeBeanListSource(final Iterable<? extends Collection<Bean<?>>> beanCollections) {
    this(beanCollections == null ? Collections.emptyIterator() : beanCollections.iterator());
  }
  
  private CompositeBeanListSource(final Iterator<? extends Collection<Bean<?>>> iterator) {
    super();
    if (iterator != null && iterator.hasNext()) {
      final ArrayList<Bean<?>> beans = new ArrayList<>(64);
      while (iterator.hasNext()) {
        beans.addAll(iterator.next());
      }
      beans.trimToSize();
      this.beans = Collections.unmodifiableList(beans);
    } else {
      this.beans = List.of();
    }
  }

  @Override // BeanListSource
  public final List<Bean<?>> beanList() {
    return this.beans;
  }

  public static final CompositeBeanListSource of(final Iterable<? extends Collection<Bean<?>>> beanCollections) {
    return new CompositeBeanListSource(beanCollections);
  }

  public static final CompositeBeanListSource of(final Iterator<? extends Collection<Bean<?>>> beanCollections) {
    return new CompositeBeanListSource(beanCollections);
  }

  public static final CompositeBeanListSource of() {
    return of(java.util.ServiceLoader.load(BeanList.class));
  }
  
}
