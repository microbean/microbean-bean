/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2024–2026 microBean™.
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
package org.microbean.bean.model;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

import java.util.function.Function;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

import static java.util.function.Predicate.not;

final class Trees {

  private Trees() {
    super();
  }

  /**
   * Returns a {@link Stream} of nodes in breadth-first order from a root node of a notional (and definitionally
   * acyclic) tree and a means for acquiring its immediate notional children.
   *
   * <p>No checks are performed for cycles in the resulting graph which means that if the supplied {@link Function} is
   * not well-behaved undefined behavior can result, including infinite loops.</p>
   *
   * @param node the root node of a notional tree; must not be {@code null}
   *
   * @param f a {@link Function} that accepts a node in the notional tree and returns a non-{@code null} {@link
   * Iterable} that can {@linkplain Iterable#forEach(Object) iterate over} its immediate children; must not return
   * {@code null} and must not result in infinite loops
   *
   * @return a {@link Stream} of nodes in breadth-first order; never {@code null}
   *
   * @exception NullPointerException if {@code node} or {@code f} is {@code null} or if the return value of an
   * invocation of the supplied {@link Function} is {@code null} or notionally contains a {@code null} value
   */
  // f's output is not checked for cycles or containment of node
  static final <N> Stream<N> streamBreadthFirst(final N node, final Function<? super N, ? extends Iterable<? extends N>> f) {
    final Queue<N> q = new ArrayDeque<>();
    return
      Stream.iterate(requireNonNull(node, "node"),
                     not(Objects::isNull),
                     n -> {
                       f.apply(n).forEach(q::add);
                       return q.poll();
                     });
  }

  /**
   * Returns a {@link Stream} of nodes in depth-first order from a root node of a notional (and definitionally acyclic)
   * tree and a means for acquiring its immediate notional children.
   *
   * <p>No checks are performed for cycles in the resulting graph which means that if the supplied {@link Function} is
   * not well-behaved undefined behavior can result, including infinite loops.</p>
   *
   * @param node the root node of a notional tree; must not be {@code null}
   *
   * @param f a {@link Function} that accepts a node in the notional tree and returns a non-{@code null} {@link
   * Iterable} that can {@linkplain Iterable#forEach(Object) iterate over} its immediate children; must not return
   * {@code null} and must not result in infinite loops
   *
   * @return a {@link Stream} of nodes in depth-first order; never {@code null}
   *
   * @exception NullPointerException if {@code node} or {@code f} is {@code null} or if the return value of an
   * invocation of the supplied {@link Function} is {@code null} or notionally contains a {@code null} value
   */
  static final <N> Stream<N> streamDepthFirst(final N node, final Function<? super N, ? extends Iterable<? extends N>> f) {
    // See https://www.techempower.com/blog/2016/10/19/efficient-multiple-stream-concatenation-in-java/
    // return Stream.concat(Stream.of(node), f.apply(node).stream().flatMap(e -> streamDepthFirst(e, f)));
    return
      Stream.of(Stream.of(node),
                StreamSupport.stream(f.apply(node).spliterator(), false)
                .flatMap(e -> streamDepthFirst(e, f))) // note recursion
      .flatMap(Function.identity()); // flatMap here also has the nice side effect of imposing order
  }

}
