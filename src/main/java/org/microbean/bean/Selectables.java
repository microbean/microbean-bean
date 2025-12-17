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

import java.util.concurrent.ConcurrentHashMap;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import org.microbean.assign.AttributedType;
import org.microbean.assign.Matcher;
import org.microbean.assign.Selectable;

import static java.util.Objects.requireNonNull;

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
    requireNonNull(s, "s");
    requireNonNull(p, "p");
    requireNonNull(ranker, "ranker");

    // Relevant bits:
    //
    // https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#unsatisfied_and_ambig_dependencies
    // https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#dynamic_lookup (Search for "The iterator() method
    // must")
    //
    // In CDI 5 @Reserve will also enter the chat.
    return c -> {
      final List<E> elements = s.select(c);
      final int size = elements.size();
      switch (size) {
      case 0:
        return List.of();
      case 1:
        return List.of(elements.get(0));
      default:
        break;
      }

      int maxRank = Integer.MIN_VALUE;
      final List<E> reductionList = new ArrayList<>(size); // will never be larger, only smaller
      boolean reductionListContainsOnlyRankedAlternates = false;

      for (final E element : elements) {
        if (!p.test(element)) {
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

      return switch (reductionList.size()) {
        // No reduction at all took place. "If typesafe resolution results in an ambiguous dependency and the set of
        // candidate beans contains no alternative, the set of resulting beans contains all candidate beans."
      case 0 -> elements;
      case 1 -> List.of(reductionList.get(0)); // Optimization for the common case
      default -> List.copyOf(reductionList);
      };
    };
  }

  /**
   * {@linkplain Beans#normalize(Collection) Normalizes} the supplied {@link Collection} of {@link Bean}s and returns a
   * {@link Selectable} suitable for it and the supplied {@link Matcher}.
   *
   * <p>The returned {@link Selectable} does not cache its results.</p>
   *
   * @param beans a {@link Collection} of {@link Bean}s; must not be {@code null}
   *
   * @param m a {@link Matcher}; must not be {@code null}
   *
   * @return a non-{@code null} {@link Selectable}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @see org.microbean.assign.Selectables#filtering(Collection, java.util.function.BiPredicate)
   *
   * @see #ambiguityReducing(Selectable, Predicate, ToIntFunction)
   *
   * @see Beans#normalize(Collection)
   */
  public static final Selectable<AttributedType, Bean<?>> typesafeFiltering(final Collection<? extends Bean<?>> beans,
                                                                            final Matcher<? super AttributedType, ? super Id> m) {
    requireNonNull(m, "m");
    if (beans.isEmpty()) {
      return org.microbean.assign.Selectables.empty();
    }
    final List<Bean<?>> normalizedBeans = normalize(beans);
    return
      org.microbean.assign.Selectables.filtering(normalizedBeans, (b, c) -> m.test(c, b.id()));
  }

}
