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

import java.util.List;
import java.util.Objects;

import java.util.concurrent.ConcurrentHashMap;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A {@linkplain FunctionalInterface functional interface} whose implementations can <dfn>reduce</dfn> an unspecified
 * notional collection of elements to a single element according to some <em>criteria</em>.
 *
 * <p>This interface is related to, but should not be confused with, the {@link Reducer} interface, implementations of
 * which can be used to build {@link Reducible} instances.</p>
 *
 * @param <C> the type of criteria
 *
 * @param <T> the element type
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #reduce(Object)
 *
 * @see Reducer
 */
@FunctionalInterface
public interface Reducible<C, T> {


  /*
   * Instance methods.
   */


  /**
   * Given a criteria object, which may be {@code null}, returns an object that represents the <em>reduction</em> of a
   * notional collection of objects.
   *
   * <p>Most {@link Reducible} implementations will return determine values from invocations of this method, but there
   * is no requirement to do so.</p>
   *
   * @param criteria the criteria; may be {@code null} to indicate no criteria
   *
   * @return a single object, or {@code null}
   *
   * @exception ReductionException if reduction could not occur or if an error occurs
   */
  public T reduce(final C criteria);


  /*
   * Static methods.
   */


  /**
   * Calls the {@link #of(Selectable, Reducer, BiFunction)} method with the supplied {@code selectable}, the supplied {@code r}, and a reference to the {@link Reducer#fail(List, Object)} method,
   * and returns the result.
   *
   * @param <C> the criteria type
   *
   * @param <E> the element type
   *
   * @param selectable a {@link Selectable}; must not be {@code null}
   *
   * @param r a {@link Reducer}; must not be {@code null}
   *
   * @return a {@link Reducible}; never {@code null}
   *
   * @exception NullPointerException if {@code selectable} or {@code r} is {@code null}
   *
   * @see #of(Selectable, Reducer, BiFunction)
   *
   * @see Reducer#fail(List, Object)
   */
  public static <C, E> Reducible<C, E> of(final Selectable<C, E> selectable,
                                          final Reducer<C, E> r) {
    return of(selectable, r, Reducer::fail);
  }

  /**
   * Returns a {@link Reducible} implementation that uses the supplied {@link Reducer} for its reduction operations.
   *
   * @param <C> the criteria type
   *
   * @param <E> the element type
   *
   * @param selectable a {@link Selectable}; must not be {@code null}
   *
   * @param r a {@link Reducer}; must not be {@code null}
   *
   * @param failureHandler a {@link BiFunction} serving as the supplied {@link Reducer}'s <dfn>failure handler</dfn>;
   * must not be {@code null}
   *
   * @return a {@link Reducible}; never {@code null}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @see Reducer
   */
  public static <C, E> Reducible<C, E> of(final Selectable<C, E> selectable,
                                          final Reducer<C, E> r,
                                          final BiFunction<? super List<? extends E>, ? super C, ? extends E> failureHandler) {
    Objects.requireNonNull(selectable, "selectable");
    Objects.requireNonNull(r, "r");
    final BiFunction<? super List<? extends E>, ? super C, ? extends E> fh = Objects.requireNonNull(failureHandler, "failureHandler");
    return c -> r.reduce(selectable.select(c), c, fh);
  }

  /**
   * Calls the {@link #ofCaching(Selectable, Reducer, BiFunction)} method with the supplied {@code selectable}, the
   * supplied {@code r}, the supplied {@code failureHandler}, and a reference to the {@link Reducer#fail(List, Object)}
   * method, and returns its result.
   *
   * @param <C> the criteria type
   *
   * @param <E> the element type
   *
   * @param selectable a {@link Selectable}; must not be {@code null}
   *
   * @param r a {@link Reducer}; must not be {@code null}
   *
   * @return a {@link Reducible}; never {@code null}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @see #ofCaching(Selectable, Reducer, BiFunction)
   */
  public static <C, E> Reducible<C, E> ofCaching(final Selectable<C, E> selectable, final Reducer<C, E> r) {
    return ofCaching(selectable, r, Reducer::fail);
  }

  /**
   * Calls the {@link #ofCaching(Selectable, Reducer, BiFunction, BiFunction)} method with the supplied {@code
   * selectable}, the supplied {@code r}, the supplied {@code fh}, and a reference to the {@link
   * ConcurrentHashMap#computeIfAbsent(Object, java.util.function.Function) computeIfAbsent(Object, Function)} method of
   * a new {@link ConcurrentHashMap}, and returns its result.
   *
   * @param <C> the criteria type
   *
   * @param <E> the element type
   *
   * @param selectable a {@link Selectable}; must not be {@code null}
   *
   * @param r a {@link Reducer}; must not be {@code null}
   *
   * @param fh a {@link BiFunction} serving as the supplied {@link Reducer}'s <dfn>failure handler</dfn>;
   * must not be {@code null}
   *
   * @return a {@link Reducible}; never {@code null}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @see #ofCaching(Selectable, Reducer, BiFunction, BiFunction)
   */
  public static <C, E> Reducible<C, E> ofCaching(final Selectable<C, E> selectable,
                                                 final Reducer<C, E> r,
                                                 final BiFunction<? super List<? extends E>, ? super C, ? extends E> fh) {
    return ofCaching(selectable, r, fh, new ConcurrentHashMap<C, E>()::computeIfAbsent);
  }

  /**
   * Returns a {@link Reducible} implementation that uses the supplied {@link Reducer} for its reduction operations and
   * the supplied {@code computeIfAbsent} {@link BiFunction} for its caching implementation.
   *
   * @param <C> the criteria type
   *
   * @param <E> the element type
   *
   * @param selectable a {@link Selectable}; must not be {@code null}
   *
   * @param r a {@link Reducer}; must not be {@code null}
   *
   * @param fh a {@link BiFunction} serving as the supplied {@link Reducer}'s <dfn>failure handler</dfn>; must not be
   * {@code null}
   *
   * @param cache a {@link BiFunction} with the same semantics as the {@link
   * java.util.Map#computeIfAbsent(Object, java.util.function.Function)} method; must not be {@code null}
   *
   * @return a {@link Reducible}; never {@code null}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @see #of(Selectable, Reducer, BiFunction)
   *
   * @see java.util.Map#computeIfAbsent(Object, java.util.function.Function)
   */
  public static <C, E> Reducible<C, E> ofCaching(final Selectable<C, E> selectable,
                                                 final Reducer<C, E> r,
                                                 final BiFunction<? super List<? extends E>, ? super C, ? extends E> fh,
                                                 final BiFunction<? super C, Function<C, E>, ? extends E> cache) {
    Objects.requireNonNull(cache, "cache");
    final Reducible<C, E> reducible = of(selectable, r, fh);
    return c -> cache.apply(c, reducible::reduce);
  }

}
