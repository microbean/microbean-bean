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

import java.util.function.BiFunction;

/**
 * A {@linkplain FunctionalInterface functional interface} whose implementations can either <em>reduce</em> a supplied
 * {@link List} of elements representing a successful <em>selection</em> to a single element normally drawn or
 * calculated from the selection according to some <em>criteria</em>, or fail gracefully in the face of ambiguity by
 * invoking a supplied <em>failure handler</em>.
 *
 * <p>The reduction may be a simple filtering operation, or may be a summing or aggregating operation, or anything
 * else.</p>
 *
 * <p>This interface is conceptually subordinate to, but should not be confused with, the {@link Reducible}
 * interface.</p>
 *
 * <p>{@link Reducer} implementations are often used to help build {@link Reducible} implementations. See, for example,
 * {@link Reducible#ofCaching(Selectable, Reducer, BiFunction)}.</p>
 *
 * @param <C> the type of criteria
 *
 * @param <T> the element type
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #reduce(List, Object, BiFunction)
 *
 * @see Reducible
 */
@FunctionalInterface
public interface Reducer<C, T> {

  /**
   * Performs some kind of reductive or filtering operation on the supplied {@link List}, according to the supplied
   * criteria, and returns the single result, or, if reduction fails, invokes the supplied {@link BiFunction} with a
   * sublist representing a partial reduction (or an empty list representing a reduction that simply could not be
   * performed), along with the supplied criteria, and returns its result.
   *
   * <p>Implementations of this method must return determinate values.</p>
   *
   * @param elements an immutable {@link List} to reduce; must not be {@code null}; represents a successful selection
   * from a larger collection of elements
   *
   * @param c the criteria effectively describing the initial selection and the desired reduction; may be {@code null}
   * to indicate no criteria; may be ignored if not needed by an implementation
   *
   * @param failureHandler a {@link BiFunction} receiving a failed reduction (usually a portion of the supplied {@code
   * elements}), and the selection and reduction criteria, that returns a substitute reduction (or, more commonly,
   * throws an exception); must not be {@code null}; must be invoked if reduction fails or undefined behavior may result
   *
   * @return a single, possibly {@code null}, element normally drawn or computed from the supplied {@code elements}, or
   * a synthetic value returned by an invocation of the supplied {@code failureHandler}'s {@link
   * BiFunction#apply(Object, Object) apply(Object, Object)} method
   *
   * @exception NullPointerException if {@code elements} or {@code failureHandler} is {@code null}
   *
   * @exception ReductionException if the {@code failureHandler} function throws a {@link ReductionException}
   *
   * @see #fail(List, Object)
   */
  // List, not Stream, for equality semantics and caching purposes.
  // List, not Set, because it's much faster and reduction can take care of duplicates if needed
  // List, not Collection, because you want easy access to the (possibly) only element without creating iterators
  // C, not Predicate, because it may not be necessary to actually filter the List to perform the reduction
  // failureHandler will receive only those elements that could not be eliminated
  // c is a pass-through used only during failure
  public T reduce(final List<? extends T> elements,
                  final C c,
                  final BiFunction<? super List<? extends T>, ? super C, ? extends T> failureHandler);


  /*
   * Default methods.
   */


  /**
   * Invokes the {@link #reduce(List, Object, BiFunction)} method with the return value of an invocation of the {@link
   * Selectable#select(Object) select(Object)} method on the supplied {@link Selectable} supplied with {@code c}, and
   * the supplied {@code c} and {@code failureHandler} arguments, and returns the result.
   *
   * @param f a {@link Selectable}; must not be {@code null}
   *
   * @param c the criteria effectively describing the initial selection and the desired reduction; may be {@code null}
   * to indicate no criteria; may be ignored if not needed by an implementation
   *
   * @param failureHandler a {@link BiFunction} receiving a failed reduction (usually a portion of the supplied {@code
   * elements}), and the selection and reduction criteria, that returns a substitute reduction (or, more commonly,
   * throws an exception); must not be {@code null}; must be invoked if reduction fails or undefined behavior may result
   *
   * @return a single, possibly {@code null}, element normally drawn or computed from the {@link List} returned from the
   * supplied {@link Selectable}'s {@link Selectable#select(Object) select(Object)} method, or a synthetic value
   * returned by an invocation of the supplied {@code failureHandler}'s {@link BiFunction#apply(Object, Object)
   * apply(Object, Object)} method
   *
   * @exception NullPointerException if {@code f} or {@code failureHandler} is {@code null}
   *
   * @see #reduce(List, Object, BiFunction)
   */
  public default T reduce(final Selectable<? super C, ? extends T> f,
                          final C c,
                          final BiFunction<? super List<? extends T>, ? super C, ? extends T> failureHandler) {
    return this.reduce(f.select(c), c, failureHandler);
  }

  /**
   * Invokes the {@link #reduce(List, Object, BiFunction)} method with the return value of an invocation of the {@link
   * Selectable#select(Object) select(Object)} method on the supplied {@link Selectable} supplied with {@code c}, and
   * the supplied {@code c} and a reference to the {@link #fail(List, Object) fail(List, Object)} method, and returns
   * the result.
   *
   * @param f a {@link Selectable}; must not be {@code null}
   *
   * @param c the criteria effectively describing the initial selection and the desired reduction; may be {@code null}
   * to indicate no criteria; may be ignored if not needed by an implementation
   *
   * @return a single, possibly {@code null}, element normally drawn or computed from the {@link List} returned from the
   * supplied {@link Selectable}'s {@link Selectable#select(Object) select(Object)} method, or the sole element
   * returned by an invocation of the {@link #fail(List, Object) fail(List, Object)} method
   *
   * @exception NullPointerException if {@code f} or {@code failureHandler} is {@code null}
   *
   * @exception UnsatisfiedReductionException if an invocation of the {@link #fail(List, Object) fail(List, Object)} method throws an {@link UnsatisfiedReductionException}
   *
   * @exception AmbiguousReductionException if an invocation of the {@link #fail(List, Object) fail(List, Object)} method throws an {@link AmbiguousReductionException}
   *
   * @see #reduce(Selectable, Object, BiFunction)
   *
   * @see #fail(List, Object)
   */
  public default T reduce(final Selectable<? super C, ? extends T> f, final C c) {
    return this.reduce(f, c, Reducer::fail);
  }

  /**
   * Invokes the {@link #reduce(List, Object, BiFunction)} method with the supplied arguments and a reference to the
   * {@link #fail(List, Object)} method, and returns the result.
   *
   * @param elements an immutable {@link List} to reduce; must not be {@code null}; represents a successful selection
   * from a larger collection of elements
   *
   * @param c the criteria effectively describing the initial selection and the desired reduction; may be {@code null}
   * to indicate no criteria; may be ignored if not needed by an implementation
   *
   * @return a single, possibly {@code null}, element normally drawn or computed from the supplied {@code elements}, or
   * a synthetic value returned by an invocation of the supplied {@code failureHandler}'s {@link
   * BiFunction#apply(Object, Object) apply(Object, Object)} method
   *
   * @exception NullPointerException if {@code elements} is {@code null}
   *
   * @see #fail(List, Object)
   *
   * @see #reduce(List, Object, BiFunction)
   */
  public default T reduce(final List<? extends T> elements, final C c) {
    return this.reduce(elements, c, Reducer::fail);
  }


  /*
   * Static methods.
   */


  /**
   * Returns a {@link Reducer} whose {@link #reduce(List, Object, BiFunction)} method, for a given {@link List} of
   * elements, returns the {@link List}'s sole element if the {@link List} has exactly one element, and returns the
   * result of invoking its supplied failure handler otherwise.
   *
   * @param <C> the type of the criteria
   *
   * @param <T> the type of the elements
   *
   * @return a {@link Reducer}; never {@code null}
   *
   * @see #reduce(List, Object, BiFunction)
   */
  // A Reducer that works only when the selection is of size 1.
  public static <C, T> Reducer<C, T> ofSimple() {
    return Reducer::reduceObviously;
  }

  /**
   * Returns a {@link Reducer} whose {@link #reduce(List, Object, BiFunction)} method, for a given {@link List} of
   * elements, returns the result of invoking its supplied failure handler.
   *
   * @param <C> the type of the criteria
   *
   * @param <T> the type of the elements
   *
   * @return a {@link Reducer}; never {@code null}
   *
   * @see #reduce(List, Object, BiFunction)
   */
  // A Reducer that simply calls its supplied failure handler no matter what.
  public static <C, T> Reducer<C, T> ofFailing() {
    return Reducer::failUnconditionally;
  }

  /**
   * Throws an {@link UnsatisfiedReductionException} if {@code elements} is empty, throws an {@link
   * AmbiguousReductionException} if the {@linkplain List#size() size} of {@code elements} is greater than {@code 1},
   * and returns the sole element of {@code elements} otherwise.
   *
   * <p>A reference to this method is often used as a <dfn>failure handler</dfn> in an invocation of the {@link
   * #reduce(List, Object, BiFunction)} method.</p>
   *
   * @param <C> the type of the criteria
   *
   * @param <T> the type of the elements
   *
   * @param elements a {@link List} of elements under reduction; must not be {@code null}
   *
   * @param c a criteria object; may be {@code null}
   *
   * @return the sole element present in {@code elements}, which may be {@code null}
   *
   * @exception NullPointerException if {@code elements} is {@code null}
   *
   * @exception UnsatisfiedReductionException if {@code elements} is empty
   *
   * @exception AmbiguousReductionException if the {@linkplain List#size() size} of {@code elements} is greater than
   * {@code 1}
   *
   * @see #reduce(List, Object, BiFunction)
   *
   * @see #reduce(List, Object)
   */
  // Default failure handler; call by method reference. Fails if the selection does not consist of one element.
  public static <C, T> T fail(final List<? extends T> elements, final C c) {
    return switch (elements.size()) {
    case 0 -> throw new UnsatisfiedReductionException((Object)c, null, null);
    case 1 -> elements.get(0);
    default -> throw new AmbiguousReductionException(c, elements, "Cannot reduce: " + elements);
    };
  }

  /**
   * Returns {@code null} when invoked, regardless of arguments.
   *
   * <p>A reference to this method can be used as a <dfn>failure handler</dfn> in an invocation of the {@link
   * #reduce(List, Object, BiFunction)} method.</p>
   *
   * @param <A> the type of the first parameter; ignored
   *
   * @param <B> the type of the second parameter; ignored
   *
   * @param <C> the type of the return type; ignored
   *
   * @param a an object; may be {@code null}; ignored
   *
   * @param b an object; may be {@code null}; ignored
   *
   * @return {@code null} when invoked
   */
  // Convenience failure handler; call by method reference. Returns null when invoked.
  public static <A, B, C> C returnNull(final A a, final B b) {
    return null;
  }

  private static <C, T> T reduceObviously(final List<? extends T> l,
                                          final C c,
                                          final BiFunction<? super List<? extends T>, ? super C, ? extends T> fh) {
    return switch (l.size()) {
    case 0 -> fh == null ? fail(List.of(), c) : fh.apply(List.of(), c);
    case 1 -> l.get(0);
    default -> fh == null ? fail(l, c) : fh.apply(l, c);
    };
  }

  private static <C, T> T failUnconditionally(final List<? extends T> l,
                                              final C c,
                                              final BiFunction<? super List<? extends T>, ? super C, ? extends T> fh) {
    return fh.apply(l, c);
  }

}
