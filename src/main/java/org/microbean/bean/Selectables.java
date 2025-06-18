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
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import org.microbean.assign.AttributedType;
import org.microbean.assign.Matcher;
import org.microbean.assign.Selectable;

import static org.microbean.bean.Beans.normalize;

/**
 * Utility methods for working with {@link Selectable}s.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see Selectable
 *
 * @see org.microbean.assign.Selectables
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
   *
   * @see #ambiguityReducing(Selectable, Predicate, ToIntFunction)
   *
   * @deprecated Please use the {@link #ambiguityReducing(Selectable, Predicate, ToIntFunction)} method instead.
   */
  @Deprecated(forRemoval = true, since = "0.0.19")
  public static final <C, E extends Ranked> Selectable<C, E> ambiguityReducing(final Selectable<C, E> s) {
    return ambiguityReducing(s, Ranked::alternate, Ranked::rank);
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
   * @param p a {@link Predicate} that tests whether an element is an <dfn>alternate</dfn>; must not be {@code null}
   *
   * @param ranker a {@link ToIntFunction} that returns a <dfn>rank</dfn> for an alternate; a rank of {@code 0}
   * indicates no particular rank; must not be {@code null}
   *
   * @return a non-{@code null} {@link Selectable}
   *
   * @exception NullPointerException if any argument is {@code null}
   */
  public static final <C, E> Selectable<C, E> ambiguityReducing(final Selectable<C, E> s,
                                                                final Predicate<? super E> p,
                                                                final ToIntFunction<? super E> ranker) {
    Objects.requireNonNull(s, "s");
    Objects.requireNonNull(p, "p");
    Objects.requireNonNull(ranker, "ranker");

    // Relevant bits:
    //
    // https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#unsatisfied_and_ambig_dependencies
    // https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#dynamic_lookup (Search for "The iterator() method
    // must")
    return c -> {
      final List<E> elements = s.select(c);
      final int size = elements.size();
      switch (size) {
      case 0 -> List.of();
      case 1 -> List.of(elements.get(0));
      default -> {}
      };

      int maxRank = Integer.MIN_VALUE;
      final List<E> reductionList = new ArrayList<>(size); // will never be larger, only smaller
      boolean reductionListContainsOnlyRankedAlternates = false;

      for (final E element : elements) {
        if (!p.test(element)) { // TODO: eventually this method will go away
          // The element is not an alternate. We skip it.
          continue;
        }

        final int rank = ranker.applyAsInt(element);
        if (rank == 0) {
          // The element is an alternate. It has the default rank, so no explicit rank. Headed toward ambiguity. No need
          // to look at maxRank etc.
          if (reductionListContainsOnlyRankedAlternates) {
            reductionListContainsOnlyRankedAlternates = false;
          }
          reductionList.add(element);
          continue;
        }

        if (reductionList.isEmpty()) {
          // The element is an alternate with an explicit rank. The reduction list is empty. The element's rank is
          // therefore by definition the highest one encountered so far. Add the element to the reduction list.
          assert !reductionListContainsOnlyRankedAlternates : "Unexpected reductionListContainsOnlyRankedAlternates: " + reductionListContainsOnlyRankedAlternates;
          assert rank > maxRank : "rank <= maxRank: " + rank + " <= " + maxRank; // TODO: I think this is correct
          maxRank = rank;
          reductionList.add(element);
          reductionListContainsOnlyRankedAlternates = true;
          continue;
        }

        if (reductionListContainsOnlyRankedAlternates) {
          // The element is an alternate. It has an explicit rank. The (non-empty) reduction list is known to contain
          // only ranked alternates (in fact it should contain exactly one).
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
   * @see org.microbean.assign.Selectables#filtering(Collection, BiPredicate)
   *
   * @see #ambiguityReducing(Selectable)
   *
   * @see org.microbean.assign.Selectables#caching(Selectable)
   *
   * @see Beans#normalize(Collection)
   */
  public static final Selectable<AttributedType, Bean<?>> typesafeReducing(final Collection<? extends Bean<?>> beans,
                                                                           final Matcher<? super AttributedType, ? super Id> m) {
    Objects.requireNonNull(m, "m");
    final List<Bean<?>> normalizedBeans = normalize(beans);
    return
      normalizedBeans.isEmpty() ?
      org.microbean.assign.Selectables.empty() :
      org.microbean.assign.Selectables.filtering(normalizedBeans, (b, c) -> m.test(c, b.id()));
  }

}
