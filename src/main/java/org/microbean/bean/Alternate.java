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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import java.util.function.BiFunction;

import org.microbean.bean.AmbiguousResolutionException;
import org.microbean.bean.Selector;

import static java.util.Collections.unmodifiableCollection;

public interface Alternate extends Ranked {

  public default boolean alternate() {
    return false;
  }

  public static interface Resolver<T> {

    public default <T extends Alternate> T resolve(final Set<? extends T> alternates) {
      return this.resolve(null, alternates);
    }

    public default <T extends Alternate> T resolve(final Selector selector, final Set<? extends T> alternates) {
      return this.resolve(selector, alternates, Resolver::fail);
    }

    public default <T extends Alternate> T resolve(final Set<? extends T> alternates,
                                                   final BiFunction<? super Selector, ? super Collection<? extends T>, ? extends T> failureHandler) {
      return this.resolve(null, alternates, failureHandler);
    }

    public default <T extends Alternate> T resolve(final Selector selector,
                                                   final Set<? extends T> alternates,
                                                   final BiFunction<? super Selector, ? super Collection<? extends T>, ? extends T> failureHandler) {
      if (alternates == null || alternates.isEmpty()) {
        return null;
      } else if (alternates.size() == 1) {
        return alternates.iterator().next();
      }

      T candidate = null;
      Collection<T> unresolved = null;
      // Highest rank wins
      int maxRank = DEFAULT_RANK;

      for (final T alternate : alternates) {
        if (alternate.alternate()) {
          final int alternateRank = alternate.rank();
          if (alternateRank == maxRank) {
            if (candidate == null || !candidate.alternate()) {
              // Prefer alternates regardless of ranks.
              candidate = alternate;
            } else {
              assert candidate.rank() == maxRank : "Unexpected rank: " + candidate.rank() + "; was expecting: " + maxRank;
              // The existing candidate is an alternate and by definition has the highest rank we've seen so far; the
              // incoming alternate is also an alternate; both have equal ranks: we can't resolve this.
              if (unresolved == null) {
                unresolved = new ArrayList<>(6);
              }
              unresolved.add(candidate);
              unresolved.add(alternate);
              candidate = null;
            }
          } else if (alternateRank > maxRank) {
            if (candidate == null || !candidate.alternate() || alternateRank > candidate.rank()) {
              // The existing candidate is either null, not an alternate (and alternates are always preferred), or an
              // alternate with losing rank, so junk it in favor of the incoming alternate.
              candidate = alternate;
              // We have a new maxRank.
              maxRank = alternateRank;
            } else if (alternateRank == candidate.rank()) {
              // The existing candidate is also an alternate and has the same rank.
              if (unresolved == null) {
                unresolved = new ArrayList<>(6);
              }
              unresolved.add(candidate);
              unresolved.add(alternate);
              candidate = null;
            } else {
              assert alternateRank < candidate.rank() : "alternateRank >= candidate.rank(): " + alternateRank + " >= " + candidate.rank();
              // The existing candidate is also an alternate but has a higher rank than the alternate, so keep it
              // (do nothing).
            }
          } else {
            // drop alternate by doing nothing
          }
        } else if (candidate == null) {
          // The incoming "alternate" is not an alternate, but that doesn't matter; the candidate is null, so accept
          // the alternate no matter what.
          candidate = alternate;
        } else if (!candidate.alternate()) {
          // The existing candidate is not an alternate.  The incoming "alternate" is not an alternate.  Ranks in this
          // case are irrelevant.  We cannot resolve this.
          if (unresolved == null) {
            unresolved = new ArrayList<>(6);
          }
          unresolved.add(candidate);
          unresolved.add(alternate);
          candidate = null;
        } else {
          // do nothing
        }
      }

      if (unresolved != null && !unresolved.isEmpty()) {
        if (candidate != null) {
          unresolved.add(candidate);
        }
        candidate =
          failureHandler == null ? fail(selector, unmodifiableCollection(unresolved)) : failureHandler.apply(selector, unmodifiableCollection(unresolved));
      }

      return candidate;
    }

    // Preconditions:
    //
    // When used as a failureHandler, either it will be the case that:
    // * every Alternate in the Collection returns true from isAlternate() AND every Alternate in the Collection returns
    //   the same value from rank() OR
    // * every Alternate in the Collection returns false from isAlternate()
    // No other state of affairs will hold.
    public static <T extends Alternate> T fail(final Selector selector, final Collection<? extends T> alternates) {
      assert assertPreconditions(alternates);
      throw new AmbiguousResolutionException(selector, alternates, "TODO: this message needs to be better; can't resolve these alternates: " + alternates);
    }

    private static boolean assertPreconditions(final Collection<? extends Alternate> alternates) {
      assert alternates != null;
      assert alternates.size() >= 2 : "alternates: " + alternates;
      Integer rank = null;
      Boolean alternateStatus = null;
      for (final Alternate alternate : alternates) {
        if (rank == null) {
          rank = Integer.valueOf(alternate.rank());
          alternateStatus = Boolean.valueOf(alternate.alternate());
        } else {
          assert rank.intValue() == alternate.rank();
          assert alternateStatus.booleanValue() == alternate.alternate();
        }
      }
      return true;
    }

  }

}
