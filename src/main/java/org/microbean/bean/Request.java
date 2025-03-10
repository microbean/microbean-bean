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

/**
 * A request for a contextual reference of a particular attributed type, along with functionality to help fulfil the
 * request.
 *
 * @param <I> the type of the contextual reference
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #beanReduction()
 *
 * @see ReferenceSelector
 *
 * @see Creation
 */
public interface Request<I> extends Creation<I>, ReferenceSelector {

  /**
   * Returns the {@link BeanReduction} describing this {@link Request} in progress.
   *
   * @return the {@link BeanReduction} describing this {@link Request} in progress, or {@code null} if this is the
   * <dfn>primordial request</dfn>
   *
   * @microbean.idempotency Implementations of this method must be idempotent and deterministic.
   *
   * @microbean.nullability Implementations of this method may return {@code null}, but only to represent the
   * <dfn>primordial request</dfn>.
   *
   * @microbean.threadsafety Implementations of this method must be safe for concurrent use by multiple threads.
   *
   * @see BeanReduction
   */
  public BeanReduction<I> beanReduction();

  /**
   * Returns a {@link Request} that is conceptually a <dfn>child</dfn> of this {@link Request} and that represents a
   * request for a contextual reference satisfying the supplied {@link BeanReduction}.
   *
   * <p>If the supplied {@link BeanReduction} {@linkplain BeanReduction#equals(Object) is equal to} the return value of
   * an invocation of this {@link Request}'s {@link #beanReduction()} method, it is permissible for this {@link Request}
   * to be returned instead of a new child.</p>
   *
   * @param <J> the type of contextual reference returned by the child {@link Request}
   *
   * @param beanReduction a {@link BeanReduction}; must not be {@code null}
   *
   * @return a {@link Request} that is conceptually a <dfn>child</dfn> of this {@link Request} and that represents a
   * request for a contextual reference satisfying the supplied {@link BeanReduction}; never {@code null}
   *
   * @exception NullPointerException if {@code beanReduction} is {@code null}
   */
  // Returns a "child" Request, where "childness" means "in the service of the parent". So a request for a
  // CoffeeMaker is the logical parent of a request for its Heater, which is the new child.
  //
  // If the supplied BeanReduction is equal to this Request's current beanReduction(), it is permissible for this
  // Request to be returned instead.
  //
  // It is very common for a Request implementation to *also* be (or house) an AutoCloseableRegistry implementation. So
  // if the Heater object is in "none"/dependent scope, it should get added to the CoffeeMaker parent's set of
  // AutoCloseables to be closed when the parent goes out of scope, and if a child request for a Fuse object gets
  // created, then the Fuse should be added to the Heater child's set of AutoCloseables, and so on.
  //
  // See AutoCloseableRegistry#newChild() for how that part works.
  //
  // Note that the semantics of this child(BeanReduction) method don't have the same "tree node"-like quality as
  // AutoCloseableRegistry#newChild(). That is, there's no requirement that a child Request retain a reference to its
  // parent Request.
  public <J> Request<J> child(final BeanReduction<J> beanReduction);

  /**
   * Returns {@code true} if and only if this {@link Request} implementation is the <dfn>primordial request</dfn>.
   *
   * <p>The default implementation of this method returns {@code true} if and only if the return value of an invocation
   * of the {@link #beanReduction()} method is {@code null}.</p>
   *
   * @return {@code true} if and only if this {@link Request} is the <dfn>primordial request</dfn>
   */
  public default boolean primordial() {
    return this.beanReduction() == null;
  }

  /**
   * Acquires a contextual reference that matches the supplied {@link AttributedType} and returns it.
   *
   * @param <R> the type of contextual reference
   *
   * @param attributedType an {@link AttributedType}; must not be {@code null}
   *
   * @return a contextual reference, which may be {@code null}
   *
   * @exception NullPointerException if {@code attributedtype} is {@code null}
   */
  @SuppressWarnings("unchecked")
  public default <R> R reference(final AttributedType attributedType) {
    return this.reference(attributedType, (Creation<R>)this);
  }

}
