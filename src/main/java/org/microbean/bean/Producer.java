/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2025 microBean™.
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

import java.util.SequencedSet;

/**
 * An interface whose implementations {@linkplain #produce(Request) produce} possibly uninitialized contextual
 * instances.
 *
 * <p>{@link Producer}s are used to implement {@link Factory} instances' {@link Factory#create(Request) create(Request)}
 * and {@link Factory#destroy(Object, Request) destroy(Object, Request)} methods. Values returned from the {@link
 * #produce(Request)} method are often supplied to {@link Initializer}s.</p>
 *
 * @param <I> the type of contextual instance
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #produce(Request)
 *
 * @see Factory#create(Request)
 */
// Subordinate to Factory<I> (really to Initializer<I>)
// Akin to CDI's Producer.
// Handles instance production and disposal, *including intercepted production*.
//
// Does NOT handle initialization; see for example
// https://github.com/search?q=repo%3Aweld%2Fcore+%22.produce%28%29%22+language%3AJava&type=code. Obviously it may
// acquire dependencies and supply them during production, but the point is it doesn't do field injection or initializer
// method invocation.
//
// Does NOT handle post-initialization.
// Does NOT handle business method interception.
// Does NOT handle pre-disposal.
// See also: InterceptingProducer
@FunctionalInterface
public interface Producer<I> extends Aggregate {

  /**
   * Disposes of the supplied contextual instance.
   *
   * <p>The default implementation of this method checks to see if {@code i} is an instance of {@link AutoCloseable},
   * and, if so, calls {@link AutoCloseable#close() close()} on it, throwing any resulting exception as a {@link
   * DestructionException}.</p>
   *
   * @param i a contextual instance {@linkplain #produce(Request) produced} by this {@link Producer}; may be {@code
   * null}
   *
   * @param r the {@link Request} that was {@linkplain #produce(Request) present at production time}; must not be {@code
   * null}
   *
   * @exception NullPointerException if {@code r} is {@code null}
   *
   * @exception DestructionException if {@code i} is an {@link AutoCloseable} instance, and if its {@link
   * AutoCloseable#close() close()} method throws a checked exception
   */
  public default void dispose(final I i, final Request<I> r) {
    if (i instanceof AutoCloseable ac) {
      try {
        ac.close();
      } catch (final RuntimeException | Error e) {
        throw e;
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new DestructionException(e.getMessage(), e);
      } catch (final Exception e) {
        throw new DestructionException(e.getMessage(), e);
      }
    }
  }

  /**
   * Produces a new contextual instance and returns it by calling the {@link #produce(SequencedSet)} method with the
   * return value of an invocation of the {@link #assign(Request)} method with the supplied {@link Request}.
   *
   * @param r a {@link Request}; must not be {@code null}
   *
   * @return a new contextual instance, or {@code null}
   *
   * @exception NullPointerException if {@code r} is {@code null}
   *
   * @see #produce(SequencedSet)
   *
   * @see #assign(Request)
   */
  public default I produce(final Request<?> r) {
    return this.produce(this.assign(r));
  }

  /**
   * Produces a new contextual instance and returns it, possibly (often) making use of the supplied, dependent,
   * contextual references.
   *
   * <p>Implementations of this method must not call {@link #produce(Request)} or an infinite loop may result.</p>
   *
   * @param assignments a {@link SequencedSet} of {@link Assignment}s this {@link Producer} needs to create the
   * contextual instance; must not be {@code null}
   *
   * @return a new contextual instance, or {@code null}
   *
   * @exception NullPointerException if {@code dependentContextualReferences} is {@code null}
   */
  public I produce(final SequencedSet<? extends Assignment<?>> assignments);

}
