/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2024 microBean™.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public interface ReferenceSelector extends AutoCloseable, CreationSupplier {


  /*
   * Abstract methods.
   */


  public BeanSet beanSet();

  public <R> R reference(final BeanSelectionCriteria beanSelectionCriteria, final Bean<R> bean, final Creation<R> creation);


  /*
   * Default methods.
   */


  @Override // AutoCloseable
  public default void close() {}

  public default <R> R reference(final BeanSelectionCriteria beanSelectionCriteria, final Creation<R> creation) {
    final Bean<?> b = this.beanSet().bean(beanSelectionCriteria);
    if (b == null) {
      throw new UnsatisfiedResolutionException(beanSelectionCriteria);
    }
    return this.reference(beanSelectionCriteria, b.cast(), creation);
  }

  // Convenience
  @Deprecated
  public default Object[] referencesArray(final Collection<? extends BeanSelectionCriteria> bscs, final Creation<?> c) {
    if (bscs.isEmpty()) {
      return new Object[0];
    }
    final Object[] rv = new Object[bscs.size()];
    int i = 0;
    final Iterator<? extends BeanSelectionCriteria> iterator = bscs.iterator();
    while (iterator.hasNext()) {
      rv[i++] = this.reference(iterator.next(), c);
    }
    return rv;
  }

  // Convenience
  @Deprecated
  public default List<?> referencesList(final Collection<? extends BeanSelectionCriteria> bscs, final Creation<?> c) {
    if (bscs.isEmpty()) {
      return List.of();
    }
    // Can't use List.of() variants because references might be null.
    final List<Object> rv = new ArrayList<>(bscs.size());
    for (final BeanSelectionCriteria bsc : bscs) {
      rv.add(this.reference(bsc, c));
    }
    return Collections.unmodifiableList(rv);
  }

}
