/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2025 microBean™.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.util.concurrent.ConcurrentHashMap;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.microbean.assign.Matcher;

import static org.microbean.bean.Beans.normalize;

import static org.microbean.bean.Ranked.DEFAULT_RANK;

/**
 * Utility methods for working with {@link Selectable}s.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see Selectable
 */
public final class Selectables {

  private Selectables() {
    super();
  }

  /**
   * Returns a {@link Selectable} that reduces any ambiguity in the results returned by another {@link Selectable},
   * considering alternate status and rank.
   *
   * @param <C> the criteria type
   *
   * @param <E> the element type
   *
   * @param s a {@link Selectable}; must not be {@code null}
   *
   * @return a non-{@code null} {@link Selectable}
   *
   * @exception NullPointerException if {@code s} is {@code null}
   *
   * @see Ranked#rank()
   *
   * @see Ranked#alternate()
   */
  public static final <C, E extends Ranked> Selectable<C, E> ambiguityReducing(final Selectable<C, E> s) {
    Objects.requireNonNull(s, "s");
    // Relevant bits:
    //
    // https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#unsatisfied_and_ambig_dependencies
    // https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#dynamic_lookup (Search for "The iterator() method
    // must")
    return c -> {
      final List<E> elements = s.select(c);
      if (elements.isEmpty()) {
        return List.of();
      } else if (elements.size() == 1) {
        return List.of(elements.get(0));
      }

      int maxRank = Integer.MIN_VALUE;
      final List<E> reductionList = new ArrayList<>(elements.size()); // will never be larger, only smaller
      boolean reductionListContainsOnlyRankedAlternates = false;

      for (final E element : elements) {
        if (!element.alternate()) { // TODO: eventually this method will go away
          // The element is not an alternate. We skip it.
          continue;
        }

        final int rank = element.rank(); // TODO: eventually this method will go away
        if (rank == DEFAULT_RANK) {
          // The element is an alternate. It has the default rank, so no explicit rank. Headed toward ambiguity. No need
          // to look at maxRank etc.
          if (reductionListContainsOnlyRankedAlternates) {
            reductionListContainsOnlyRankedAlternates = false;
          }
          reductionList.add(element);
          continue;
        }

        if (reductionList.isEmpty()) {
          // The element is an alternate. It has an explicit rank. The reduction list is empty. The element's rank is
          // therefore the highest one encountered so far. Add the element to the reduction list.
          assert !reductionListContainsOnlyRankedAlternates : "Unexpected reductionListContainsOnlyRankedAlternates: " + reductionListContainsOnlyRankedAlternates;
          if (rank > maxRank) {
            maxRank = rank;
          }
          reductionList.add(element);
          reductionListContainsOnlyRankedAlternates = true;
          continue;
        }

        if (reductionListContainsOnlyRankedAlternates) {
          // The element is an alternate. It has an explicit rank. The reduction list is known to contain only ranked
          // alternates (in fact it should contain exactly one).
          assert reductionList.size() == 1 : "Unexpected reductionList size: " + reductionList;
          if (rank > maxRank) {
            // The element's rank is higher than the rank of the (sole) element in the list. Record the new highest rank
            // and replace the sole element in the list with this element.
            maxRank = rank;
            reductionList.set(0, element);
          }
          continue;
        }

        // The element is an alternate. It has an explicit rank (but this does not matter as we'll see). The list we're
        // using to store alternates does not have a possibility of reducing to size 1, because it already contains
        // unranked alternates, so we have to add this element to it, regardless of what its rank is. This operation
        // will not affect the highest rank.
        reductionList.add(element);
      }

      assert reductionListContainsOnlyRankedAlternates ? reductionList.size() == 1 : true : "Unexpected reductionList size: " + reductionList;
      if (reductionList.isEmpty()) {
        // No reduction at all took place. "If typesafe resolution results in an ambiguous dependency and the set of
        // candidate beans contains no alternative, the set of resulting beans contains all candidate beans."
        return elements;
      } else if (reductionList.size() == 1) {
        return List.of(reductionList.get(0));
      }
      return List.copyOf(reductionList);
    };
  }

  /**
   * Returns a {@link Selectable} that caches its results.
   *
   * <p>The cache is unbounded.</p>
   *
   * @param <C> the criteria type
   *
   * @param <E> the element type
   *
   * @param selectable a {@link Selectable}; must not be {@code null}
   *
   * @return a non-{@code null} {@link Selectable}
   *
   * @exception NullPointerException if {@code selectable} is {@code null}
   *
   * @see #caching(Selectable, BiFunction)
   */
  public static <C, E> Selectable<C, E> caching(final Selectable<C, E> selectable) {
    final Map<C, List<E>> selectionCache = new ConcurrentHashMap<>();
    return Selectables.<C, E>caching(selectable, selectionCache::computeIfAbsent);
  }

  /**
   * Returns a {@link Selectable} that caches its results.
   *
   * @param <C> the criteria type
   *
   * @param <E> the element type
   *
   * @param selectable a {@link Selectable}; must not be {@code null}
   *
   * @param f a {@link BiFunction} that returns a cached result, computing it on demand via its supplied mapping {@link
   * Function} if necessary; must not be {@code null}; normally safe for concurrent use by multiple threads; often a
   * reference to the {@link ConcurrentHashMap#computeIfAbsent(Object, Function)} method
   *
   * @return a non-{@code null} {@link Selectable}
   *
   * @exception NullPointerException if {@code selectable} or {@code f} is {@code null}
   *
   * @see ConcurrentHashMap#computeIfAbsent(Object, Function)
   */
  public static <C, E> Selectable<C, E> caching(final Selectable<C, E> selectable,
                                                final BiFunction<? super C, Function<? super C, ? extends List<E>>, ? extends List<E>> f) {
    Objects.requireNonNull(selectable, "selectable");
    return c -> f.apply(c, selectable::select);
  }

  /**
   * Returns a {@link Selectable} whose {@link Selectable#select(Object)} method always returns an {@linkplain List#of()
   * empty, immutable <code>List</code>}.
   *
   * @param <C> the criteria type
   *
   * @param <E> the element type
   *
   * @return a non-{@code null} {@link Selectable}
   */
  public static final <C, E> Selectable<C, E> empty() {
    return Selectables::empty;
  }

  private static final <C, E> List<E> empty(final C ignored) {
    return List.of();
  }

  /**
   * Returns a {@link Selectable} using the supplied {@link Collection} as its elements, and the supplied {@link
   * BiFunction} as its <em>selector function</em>.
   *
   * <p>There is no guarantee that this method will return new {@link Selectable} instances.</p>
   *
   * <p>The {@link Selectable} instances returned by this method may or may not cache their selections.</p>
   *
   * <p>The selector function must select a sublist from the supplied {@link Collection} as mediated by the supplied
   * criteria. The selector function must additionally be idempotent and must produce a determinate value when given the
   * same arguments.</p>
   *
   * <p>No validation of these semantics of the selector function is performed.</p>
   *
   * @param <C> the criteria type
   *
   * @param <E> the element type
   *
   * @param collection a {@link Collection} of elements from which sublists may be selected; must not be {@code null}
   *
   * @param f the selector function; must not be {@code null}
   *
   * @return a {@link Selectable}; never {@code null}
   *
   * @exception NullPointerException if either {@code collection} or {@code f} is {@code null}
   */
  @SuppressWarnings("unchecked")
  public static <C, E> Selectable<C, E> filtering(final Collection<? extends E> collection,
                                                  final BiFunction<? super E, ? super C, ? extends Boolean> f) {
    Objects.requireNonNull(f, "f");
    return collection.isEmpty() ? empty() : c -> (List<E>)collection.stream().filter(e -> f.apply(e, c)).toList();
  }

  /**
   * {@linkplain Beans#normalize(Collection) Normalizes} the supplied {@link Collection} of {@link Bean}s and returns a
   * {@link Selectable} for it and the supplied {@link Matcher}.
   *
   * <p>The {@link Selectable} does not cache its results.</p>
   *
   * @param beans a {@link Collection} of {@link Bean}s; must not be {@code null}
   *
   * @param m an {@link IdMatcher}; must not be {@code null}
   *
   * @return a non-{@code null} {@link Selectable}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @see #filtering(Collection, BiFunction)
   *
   * @see #ambiguityReducing(Selectable)
   *
   * @see #caching(Selectable)
   *
   * @see Beans#normalize(Collection)
   */
  public static final Selectable<AttributedType, Bean<?>> typesafeReducing(final Collection<? extends Bean<?>> beans,
                                                                           final Matcher<? super AttributedType, ? super Id> m) {
    Objects.requireNonNull(m, "m");
    final List<Bean<?>> normalizedBeans = normalize(beans);
    return normalizedBeans.isEmpty() ? empty() : filtering(normalizedBeans, (b, c) -> m.test(c, b.id()));
  }

}
