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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;

import org.microbean.assign.Matcher;

import org.microbean.construct.Domain;

import static java.util.HashSet.newHashSet;

/**
 * A {@link Selectable} and {@link Reducible} implementation that works with {@link Bean} and {@link
 * AttributedType} instances.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #Beans(Selectable, Reducible)
 *
 * @see Selectable
 *
 * @see Reducible
 *
 * @see Reducer
 *
 * @see RankedReducer
 *
 * @see Bean
 *
 * @see AttributedType
 */
public final class Beans implements Selectable<AttributedType, Bean<?>>, Reducible<AttributedType, Bean<?>> {


  /*
   * Static fields.
   */


  private static final Logger LOGGER = System.getLogger(Beans.class.getName());

  private static final Comparator<Bean<?>> byRankComparator = Comparator
    .<Bean<?>>comparingInt(Ranked::rank)
    .reversed();

  private static final Comparator<Bean<?>> byAlternateThenByRankComparator = Comparator
    .<Bean<?>, Boolean>comparing(Ranked::alternate)
    .reversed()
    .thenComparing(byRankComparator);


  /*
   * Instance fields.
   */


  private final Selectable<AttributedType, Bean<?>> s;

  private final Reducible<AttributedType, Bean<?>> r;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link Beans}.
   *
   * <p>This constructor is best suited for testing.</p>
   *
   * @param domain a {@link Domain}; must not be {@code null}
   *
   * @param beans an array of zero or more {@link Bean}s; may be {@code null}
   *
   * @exception NullPointerException if {@code domain} is {@code null}
   *
   * @see #Beans(Domain, Collection)
   */
  public Beans(final Domain domain, final Bean<?>... beans) {
    this(domain, beans == null || beans.length == 0 ? List.of() : List.of(beans));
  }

  /**
   * Creates a new {@link Beans}.
   *
   * @param domain a {@link Domain}; must not be {@code null}
   *
   * @param beans a {@link Collection} of {@link Bean}s; must not be {@code null}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @see #cachingSelectableOf(Collection, Matcher, Map)
   *
   * @see #Beans(Selectable)
   */
  public Beans(final Domain domain, final Collection<? extends Bean<?>> beans) {
    this(cachingSelectableOf(beans,
                             new IdMatcher(new BeanQualifiersMatcher(),
                                           new InterceptorBindingsMatcher(),
                                           new BeanTypeMatcher(domain)),
                             Map.of()));
  }

  /**
   * Creates a new {@link Beans}.
   *
   * <p>{@link Bean} instances selected by the supplied {@link Selectable} will be reduced using a {@link
   * RankedReducer}.</p>
   *
   * @param s a {@link Selectable}; must not be {@code null}
   *
   * @exception NullPointerException if {@code s} is {@code null}
   *
   * @see RankedReducer
   */
  public Beans(final Selectable<AttributedType, Bean<?>> s) {
    this(s, Reducible.of(s, RankedReducer.of()));
  }

  /**
   * Creates a new {@link Beans}.
   *
   * @param s a {@link Selectable}; must not be {@code null}
   *
   * @param r a {@link Reducible} to apply to elements selected by the supplied {@link Selectable}; must not be {@code
   * null}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @see #cachingSelectableOf(Collection, Matcher, Map)
   *
   * @see Reducible#of(Selectable, Reducer)
   *
   * @see RankedReducer
   */
  public Beans(final Selectable<AttributedType, Bean<?>> s,
               final Reducible<AttributedType, Bean<?>> r) {
    this.s = Objects.requireNonNull(s, "s");
    this.r = Objects.requireNonNull(r, "r");
  }


  /*
   * Instance methods.
   */


  @Override // Selectable<AttributedType, Bean<?>>
  public final List<Bean<?>> select(final AttributedType c) {
    return this.s.select(c);
  }

  @Override // Reducible<AttributedType, Bean<?>>
  public final Bean<?> reduce(final AttributedType c) {
    return this.r.reduce(c);
  }


  /*
   * Static methods.
   */


  /**
   * Returns a new {@link Selectable} that caches its results.
   *
   * <p>The cache is unbounded.</p>
   *
   * @param beans a {@link Collection} of {@link Bean}s; must not be {@code null}
   *
   * @param idMatcher an {@link IdMatcher}; must not be {@code null}
   *
   * @param selections a (normally empty) {@link Map} of precomputed selections; must not be {@code null}
   *
   * @return a new {@link Selectable}; never {@code null}
   *
   * @exception NullPointerException if any argument is {@code null}
   */
  public static final Selectable<AttributedType, Bean<?>> cachingSelectableOf(final Collection<? extends Bean<?>> beans,
                                                                              final Matcher<? super AttributedType, ? super Id> idMatcher,
                                                                              final Map<? extends AttributedType, ? extends List<Bean<?>>> selections) {

    Objects.requireNonNull(idMatcher, "idMatcher");
    final Map<AttributedType, List<Bean<?>>> selectionCache = new ConcurrentHashMap<>();
    final ArrayList<Bean<?>> newBeans = new ArrayList<>(31); // 31 == arbitrary
    final Set<Bean<?>> newBeansSet = newHashSet(31); // 31 == arbitrary
    for (final Entry<? extends AttributedType, ? extends List<Bean<?>>> e : selections.entrySet()) {
      final List<Bean<?>> selection = e.getValue();
      if (!selection.isEmpty()) {
        final Set<Bean<?>> newSelectionSet = newHashSet(7); // 7 == arbitrary
        final ArrayList<Bean<?>> newSelection = new ArrayList<>(selection.size());
        for (final Bean<?> b : selection) {
          if (newSelectionSet.add(b)) {
            newSelection.add(b);
          }
          if (newBeansSet.add(b)) {
            newBeans.add(b);
          }
        }
        newSelectionSet.clear();
        newSelection.trimToSize();
        Collections.sort(newSelection, byAlternateThenByRankComparator);
        selectionCache.put(e.getKey(), Collections.unmodifiableList(newSelection));
      }
    }
    for (final Bean<?> bean : beans) {
      if (newBeansSet.add(bean)) {
        newBeans.add(bean);
      }
    }
    newBeansSet.clear();
    if (newBeans.isEmpty()) {
      return Beans::empty;
    }
    Collections.sort(newBeans, byAlternateThenByRankComparator);
    newBeans.trimToSize();
    return attributedType -> selectionCache.computeIfAbsent(attributedType, at -> newBeans.stream().filter(b -> idMatcher.test(at, b.id())).toList());
  }

  private static final <C, T> List<T> empty(final C ignored) {
    return List.of();
  }

}
