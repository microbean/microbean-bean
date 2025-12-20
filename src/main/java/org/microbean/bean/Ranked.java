/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2025 microBean™.
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

/**
 * An interface whose implementations can be ranked numerically in descending order (the highest or greatest rank
 * <dfn>outranks</dfn>, or wins, or trumps, or comes first).
 *
 * <p>In addition, an implementation may be designated as an {@linkplain #alternate() alternate}, which may affect the
 * interpretation of the implementation's {@linkplain #rank() rank}, usually by conferring a rank that cannot be
 * outranked by any other.</p>
 *
 * <p>Given a series of {@link Ranked} implementations sorted by {@linkplain #rank() rank}, the first element of the
 * series will bear the highest, or greatest, {@linkplain #rank() rank}.</p>
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #alternate()
 *
 * @see #rank()
 *
 * @see #outranks(Ranked)
 *
 * @see #outranks(int)
 *
 * @see #outranks(int, int)
 *
 * @deprecated This class is deprecated for future removal.
 */
@Deprecated(forRemoval = true)
public interface Ranked {


  /*
   * Static fields.
   */


  /**
   * The default rank ({@value}) when returned by an implementation of the {@link #rank()} method.
   *
   * @see #rank()
   */
  public static final int DEFAULT_RANK = 0;


  /*
   * Instance methods.
   */


  /**
   * Returns the rank of this {@link Ranked} implementation.
   *
   * <p>Implementations of this method may return any integer: positive, zero, or negative.</p>
   *
   * <p>The default implementation of this method returns the value of the {@link #DEFAULT_RANK} field ({@value
   * #DEFAULT_RANK}).</p>
   *
   * <p>Overrides of this method must return a determinate value.</p>
   *
   * @return the rank of this {@link Ranked} implementation
   *
   * @see #outranks(int, int)
   */
  // Highest rank wins (comes first), i.e. descending order
  public default int rank() {
    return DEFAULT_RANK;
  }

  /**
   * Returns {@code true} if this {@link Ranked} is to be considered an <em>alternate</em>, which may have an effect on
   * how the return value of the {@link #rank()} method is interpreted in some situations.
   *
   * <p>The default implementation of this method returns {@code false}.</p>
   *
   * <p>Overrides of this method must be idempotent and return a determinate value.</p>
   *
   * @return {@code true} if this {@link Ranked} is to be considered an <em>alternate</em>
   */
  public default boolean alternate() {
    return false;
  }

  /**
   * Returns {@code true} if this {@link Ranked} outranks the supplied {@link Ranked} according to the rules described
   * in the {@linkplain #outranks(int, int) specification for the <code>outranks(int, int)</code> method}.
   *
   * <p>Overriding this method, while possible and permitted, is discouraged.</p>
   *
   * @param other a {@link Ranked}; may be {@code null} (in which case this method will return {@code true})
   *
   * @return {@code true} if this {@link Ranked} outranks the supplied {@link Ranked}
   *
   * @see #outranks(int, int)
   */
  public default boolean outranks(final Ranked other) {
    return other == null || outranks(this.rank(), other.rank());
  }

  /**
   * Returns {@code true} if this {@link Ranked} bears a {@linkplain #rank() rank} that outranks the rank represented by
   * {@code j} according to the rules described in the {@linkplain #outranks(int, int) specification for the
   * <code>outranks(int, int)</code> method}.
   *
   * <p>Overriding this method, while possible and permitted, is discouraged.</p>
   *
   * @param j a rank
   *
   * @return {@code true} if this {@link Ranked} bears a {@linkplain #rank() rank} that {@linkplain #outranks(int, int)
   * outranks} the supplied rank
   *
   * @see #outranks(int, int)
   */
  public default boolean outranks(final int j) {
    return outranks(this.rank(), j);
  }


  /*
   * Static methods.
   */


  /**
   * Returns {@code true} if and only if {@code r0} is non-{@code null} and {@linkplain #outranks(Ranked) outranks}
   * {@code r1}.
   *
   * @param r0 a {@link Ranked}; may be {@code null} in which case {@code false} will be returned
   *
   * @param r1 a {@link Ranked}; may be {@code null}
   *
   * @return {@code true} if and only if {@code r0} is non-{@code null} and {@linkplain #outranks(Ranked) outranks}
   * {@code r1}
   *
   * @see #outranks(Ranked)
   */
  public static boolean outranks(final Ranked r0, final Ranked r1) {
    return r0 != null && r0.outranks(r1);
  }

  /**
   * Returns {@code true} if and only if a rank represented by {@code i} outranks a rank represented by {@code j}.
   *
   * <p>Given two ranks, <em>i</em> and <em>j</em>, <em>i</em> <em>outranks</em> <em>j</em> if and only if <em>i</em> is
   * greater than ({@code >}) <em>j</em>.</p>
   *
   * @param i an {@code int} representing a rank
   *
   * @param j an {@code int} representing a rank
   *
   * @return {@code true} if and only if {@code i} outranks {@code j}
   *
   * @microbean.idempotency This method is idempotent and deterministic.
   *
   * @microbean.threadsafety This method is safe for concurrent use by multiple threads.
   */
  public static boolean outranks(final int i, final int j) {
    return i > j;
  }

}
