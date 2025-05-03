/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2024–2025 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.microbean.bean;

import java.lang.System.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static java.lang.System.getLogger;
import static java.util.Collections.sort;
import static java.util.HashSet.newHashSet;

/**
 * Utility methods for working with {@link Bean}s.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #normalize(Collection)
 *
 * @see Bean
 */
public final class Beans {


  /*
   * Static fields.
   */


  private static final Logger LOGGER = getLogger(Beans.class.getName());

  private static final Comparator<Bean<?>> byRankComparator = Comparator
    .<Bean<?>>comparingInt(Ranked::rank)
    .reversed();

  private static final Comparator<Bean<?>> byAlternateThenByRankComparator = Comparator
    .<Bean<?>, Boolean>comparing(Ranked::alternate)
    .reversed()
    .thenComparing(byRankComparator);


  /*
   * Constructors.
   */


  private Beans() {
    super();
  }


  /*
   * Static methods.
   */


  /**
   * Returns an immutable {@link List} containing a subset of {@linkplain Bean#equals(Object) distinct elements}
   * contained in the supplied {@link Collection}, sorted in a deliberately unspecified fashion.
   *
   * @param beans a {@link Collection} of {@link Bean}s; must not be {@code null}
   *
   * @return a non-{@code null} immutable {@link List}
   *
   * @exception NullPointerException if {@code beans} is {@code null}
   *
   * @see Bean#equals(Object)
   */
  public static List<Bean<?>> normalize(final Collection<? extends Bean<?>> beans) {
    if (beans.isEmpty()) {
      return List.of();
    } else if (beans.size() == 1) {
      // no duplicate weeding or sorting needed
      return List.copyOf(beans);
    }
    final ArrayList<Bean<?>> newBeans = new ArrayList<>(beans.size());
    if (beans instanceof Set) {
      newBeans.addAll(beans);
    } else {
      final Set<Bean<?>> newBeansSet = newHashSet(beans.size());
      for (final Bean<?> bean : beans) {
        if (newBeansSet.add(bean)) {
          newBeans.add(bean);
        }
      }
      if (newBeans.isEmpty()) {
        return List.of();
      }
    }
    assert !newBeans.isEmpty();
    if (newBeans.size() > 1) {
      // Sort
      sort(newBeans, byAlternateThenByRankComparator);
    }
    return List.copyOf(newBeans);
  }

}
