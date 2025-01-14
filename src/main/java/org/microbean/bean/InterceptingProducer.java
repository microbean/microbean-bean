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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.SequencedSet;

import org.microbean.interceptor.InterceptionFunction;
import org.microbean.interceptor.InterceptorMethod;

import static org.microbean.interceptor.Interceptions.ofConstruction;

/**
 * A {@link Producer} that applies constructor interception to produce contextual instances.
 *
 * @param <I> the type of contextual instance
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see InterceptorMethod
 */
// Applies around-construct logic to contextual instance production.
public final class InterceptingProducer<I> implements Producer<I> {

  private final InterceptionFunction f;

  private final Producer<I> producer;

  /**
   * Creates a new {@link InterceptingProducer}.
   *
   * @param interceptorMethods a {@link Collection} of {@link InterceptorMethod}s; must not be {@code null}
   *
   * @param producer a subordinate {@link Producer} to which are delegated the {@link #assign(Request)}, {@link
   * #dependencies()}, {@link #dispose(Object, Request)} and {@link #produce(SequencedSet)} operations; must not be
   * {@code null}
   *
   * @exception NullPointerException if either argument is {@code null}
   */
  @SuppressWarnings("unchecked")
  public InterceptingProducer(final Collection<? extends InterceptorMethod> interceptorMethods,
                              final Producer<I> producer) {
    super();
    this.producer = producer;
    final SequencedSet<AttributedElement> dependencies = producer.dependencies();
    this.f = ofConstruction(interceptorMethods, (ignored, argumentsArray) -> {
        final SequencedSet<Assignment<?>> assignments = new LinkedHashSet<>();
        int i = 0;
        for (final AttributedElement dependency : dependencies) {
          assignments.add(new Assignment<>(dependency, argumentsArray[i++]));
        }
        return this.produce(Collections.unmodifiableSequencedSet(assignments));
      });
  }

  /**
   * Calls the {@link #assign(Request) assign(Request)} method on the {@linkplain #InterceptingProducer(Collection,
   * Producer) <code>Producer</code> supplied at construction time} with the supplied {@link Request} and returns the
   * result.
   *
   * @param r a {@link Request}; must not be {@code null}
   *
   * @return the result of calling the {@link #assign(Request) assign(Request)} method on the {@linkplain
   * #InterceptingProducer(Collection, Producer) <code>Producer</code> supplied at construction time}; never {@code
   * null}
   *
   * @exception NullPointerException if {@code r} is {@code null}
   *
   * @see #InterceptingProducer(Collection, Producer)
   *
   * @see Producer#assign(Request)
   */
  @Override // Producer<I> (Aggregate)
  public final SequencedSet<? extends Assignment<?>> assign(final Request<?> r) {
    return this.producer.assign(r);
  }

  /**
   * Calls the {@link #dependencies() dependencies()} method on the {@linkplain #InterceptingProducer(Collection,
   * Producer) <code>Producer</code> supplied at construction time} and returns the result.
   *
   * @return the result of calling the {@link #dependencies() dependencies()} method on the {@linkplain
   * #InterceptingProducer(Collection, Producer) <code>Producer</code> supplied at construction time}; never {@code
   * null}
   *
   * @see #InterceptingProducer(Collection, Producer)
   *
   * @see Aggregate#dependencies()
   */
  @Override // Producer<I> (Aggregate)
  public final SequencedSet<AttributedElement> dependencies() {
    return this.producer.dependencies();
  }

  /**
   * Calls the {@link #dispose(Object, Request) dispose(Object, Request)} method on the {@linkplain
   * #InterceptingProducer(Collection, Producer) <code>Producer</code> supplied at construction time} with the supplied
   * {@code i} and the supplied {@link Request}.
   *
   * @param i a contextual instance produced by this {@link InterceptingProducer}; may be {@code null}
   *
   * @param r a {@link Request}; must not be {@code null}
   *
   * @exception NullPointerException if {@code r} is {@code null}
   *
   * @see #InterceptingProducer(Collection, Producer)
   *
   * @see Producer#assign(Request)
   */
  @Override // Producer<I>
  public final void dispose(final I i, final Request<I> r) {
    this.producer.dispose(i, r);
  }

  /**
   * Produces a potentially uninitialized contextual instance and returns it, using the {@linkplain
   * #InterceptingProducer(Collection, Producer) <code>Collection</code> of <code>InterceptorMethod</code>s supplied at
   * construction time} to intercept the production.
   *
   * @param r a {@link Request}; must not be {@code null}
   *
   * @return a contextual instance, which may be {@code null}
   *
   * @exception NullPointerException if {@code r} is {@code null}
   *
   * @see #InterceptingProducer(Collection, Producer)
   *
   * @see Producer#produce(Request)
   */
  @Override // Producer<I>
  @SuppressWarnings("unchecked")
  public final I produce(final Request<?> r) {
    final Collection<? extends AttributedElement> dependencies = this.dependencies();
    final Object[] array = new Object[dependencies.size()];
    int i = 0;
    for (final AttributedElement d : dependencies) {
      array[i++] = r.reference(d.attributedType());
    }
    return (I)this.f.apply(array);
  }

  /**
   * Calls the {@link #produce(SequencedSet) produce(SequencedSet)} method on the {@linkplain
   * #InterceptingProducer(Collection, Producer) <code>Producer</code> supplied at construction time} with the supplied
   * {@code assignments} and returns the result.
   *
   * @param assignments a {@link SequencedSet} of {@link Assignment}s; must not be {@code null}
   *
   * @exception NullPointerException if {@code assignments} is {@code null}
   *
   * @see #InterceptingProducer(Collection, Producer)
   *
   * @see Producer#produce(SequencedSet)
   */
  @Override // Producer<I>
  public final I produce(final SequencedSet<? extends Assignment<?>> assignments) {
    return this.producer.produce(assignments);
  }

}
