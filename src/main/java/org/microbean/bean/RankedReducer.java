/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2024–2025 microBean™.
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
import java.util.List;

import java.util.function.BiFunction;

import static java.util.Collections.unmodifiableList;

import static org.microbean.bean.Ranked.DEFAULT_RANK;

/**
 * A {@link Reducer} implementation that works with {@link Ranked} objects.
 *
 * @param <C> the type of criteria used in the {@link #reduce(List, Object, BiFunction)} method
 *
 * @param <T> a {@link Ranked} type
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #reduce(List, Object, BiFunction)
 *
 * @deprecated This class is not really needed and is tentatively deprecated.
 */
@Deprecated(since = "0.0.18")
public final class RankedReducer<C, T extends Ranked> implements Reducer<C, T> {


  /*
   * Static fields.
   */


  private static final RankedReducer<?, ?> INSTANCE = new RankedReducer<>();


  /*
   * Constructors.
   */


  private RankedReducer() {
    super();
  }


  /*
   * Instance methods.
   */


  @Override
  public final T reduce(final List<? extends T> elements,
                        final C c,
                        final BiFunction<? super List<? extends T>, ? super C, ? extends T> failureHandler) {
    if (elements == null || elements.isEmpty()) {
      // https://github.com/microbean/microbean-bean/issues/21
      return failureHandler == null ? Reducer.fail(List.of(), c) : failureHandler.apply(List.of(), c);
    } else if (elements.size() == 1) {
      return elements.get(0);
    }

    T candidate = null;
    List<T> unresolved = null;
    // Highest rank wins
    int maxRank = DEFAULT_RANK;

    for (final T element : elements) {
      if (alternate(element)) {
        final int elementRank = rank(element);
        if (elementRank == maxRank) {
          if (candidate == null || !alternate(candidate)) {
            // Prefer elements regardless of ranks.
            candidate = element;
          } else {
            assert rank(candidate) == maxRank : "Unexpected rank: " + rank(candidate) + "; was expecting: " + maxRank;
            // The existing candidate is an alternate and by definition has the highest rank we've seen so far; the
            // incoming element is also an alternate; both have equal ranks: we can't resolve this.
            if (unresolved == null) {
              unresolved = new ArrayList<>(6);
            }
            unresolved.add(candidate);
            unresolved.add(element);
            candidate = null;
          }
        } else if (elementRank > maxRank) {
          if (candidate == null || !alternate(candidate) || elementRank > rank(candidate)) {
            // The existing candidate is either null, not an alternate (and alternates are always preferred), or an
            // alternate with losing rank, so junk it in favor of the incoming element.
            candidate = element;
            // We have a new maxRank.
            maxRank = elementRank;
          } else if (elementRank == rank(candidate)) {
            // The existing candidate is also an alternate and has the same rank.
            if (unresolved == null) {
              unresolved = new ArrayList<>(6);
            }
            unresolved.add(candidate);
            unresolved.add(element);
            candidate = null;
          } else {
            assert elementRank < rank(candidate) : "elementRank >= rank(candidate): " + elementRank + " >= " + rank(candidate);
            // The existing candidate is also an alternate but has a higher rank than the alternate, so keep it (do
            // nothing).
          }
        }
        // ...else drop element by doing nothing
      } else if (candidate == null) {
        // The incoming element is not an alternate, but that doesn't matter; the candidate is null, so accept the
        // element no matter what.
        candidate = element;
      } else if (!alternate(candidate)) {
        // The existing candidate is not an alternate. The incoming element is not an alternate. Ranks in this case are
        // irrelevant, perhaps surprisingly. We cannot resolve this.
        if (unresolved == null) {
          unresolved = new ArrayList<>(6);
        }
        unresolved.add(candidate);
        unresolved.add(element);
        candidate = null;
      }
      // ...else do nothing
    }

    if (unresolved != null && !unresolved.isEmpty()) {
      if (candidate != null) {
        unresolved.add(candidate);
      }
      candidate =
        failureHandler == null ? Reducer.fail(unmodifiableList(unresolved), c) : failureHandler.apply(unmodifiableList(unresolved), c);
    }

    return candidate;
  }

  // Preparing for a future where alternate status is not a first-class citizen.
  private final boolean alternate(final T t) {
    return t.alternate();
  }

  // Preparing for a future where rank is not a first-class citizen.
  private final int rank(final T t) {
    return t.rank();
  }


  /*
   * Static methods.
   */


  /**
   * Returns a {@link RankedReducer} implementation.
   *
   * @param <C> the type of criteria
   *
   * @param <T> the type of the {@link Ranked} reduction
   *
   * @return a {@link RankedReducer} implementation; never {@code null}
   *
   * @microbean.nullability This method never returns {@code null}.
   *
   * @microbean.idempotency This method is idempotent and deterministic.
   *
   * @microbean.threadsafety This method is safe for concurrent use by multiple threads.
   */
  @SuppressWarnings("unchecked")
  public static final <C, T extends Ranked> Reducer<C, T> of() {
    return (Reducer<C, T>)INSTANCE;
  }

}
