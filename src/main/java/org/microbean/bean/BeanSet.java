/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2024 microBean™.
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

import java.util.Collection;
import java.util.SequencedSet;

import java.util.function.BiFunction;

/**
 * A notional, immutable, and threadsafe set of {@link Bean}s that permits certain kinds of querying and resolution.
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 */
public interface BeanSet {

  /**
   * Returns an entirely immutable {@link SequencedSet} of {@link Bean}s containing all {@link Bean}s known to this
   * {@link BeanSet} implementation.
   *
   * @return an entirely immutable {@link SequencedSet} of {@link Bean}s containing all {@link Bean}s known to this
   * {@link BeanSet} implementation; never {@code null}
   */
  // Give me all the Beans
  public SequencedSet<Bean<?>> beans();

  /**
   * Returns an entirely immutable {@link SequencedSet} of {@link Bean}s {@linkplain BeanSelectionCriteria#selects(Bean)
   * selected by the supplied <code>beanSelectionCriteria</code>}.
   *
   * @param beanSelectionCriteria a {@link BeanSelectionCriteria}; must not be {@code null}
   *
   * @return an entirely immutable {@link SequencedSet} of {@link Bean}s {@linkplain BeanSelectionCriteria#selects(Bean)
   * selected by the supplied <code>beanSelectionCriteria</code>}; never {@code null}
   *
   * @exception NullPointerException if {@code beanSelectionCriteria} is {@code null}
   */
  // Give me Beans that match
  public SequencedSet<Bean<?>> beans(final BeanSelectionCriteria beanSelectionCriteria);

  /**
   * Returns the sole {@link Bean} {@linkplain BeanSelectionCriteria#selects(Bean) selected by the supplied
   * <code>beanSelectionCriteria</code>}, or {@code null} if there is no such {@link Bean}.
   *
   * <p>If there is more than one {@link Bean} {@linkplain BeanSelectionCriteria#selects(Bean) selected by the supplied
   * <code>beanSelectionCriteria</code>}, then the supplied {@link BiFunction} is invoked with the supplied {@code
   * beanSelectionCriteria} and the selected {@link Bean}s and its result is returned.</p>
   *
   * @param beanSelectionCriteria a {@link BeanSelectionCriteria}; must not be {@code null}
   *
   * @param op a disambiguating {@link BiFunction} that can further resolve a {@link Collection} of {@link Bean}s to a
   * single {@link Bean}; must not be {@code null}
   *
   * @return the resolved {@link Bean}, or {@code null}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @exception RuntimeException if the supplied {@code op} throws a {@link RuntimeException}
   */
  // Give me the single Bean that matches, null if none match, or run op on the conflicting bits
  public Bean<?> bean(final BeanSelectionCriteria beanSelectionCriteria,
                      final BiFunction<? super BeanSelectionCriteria, ? super Collection<? extends Bean<?>>, ? extends Bean<?>> op);

  /**
   * Returns the sole {@link Bean} {@linkplain BeanSelectionCriteria#selects(Bean) selected by the supplied
   * <code>beanSelectionCriteria</code>}, or {@code null} if there is no such {@link Bean}.
   *
   * <p>If there is more than one {@link Bean} {@linkplain BeanSelectionCriteria#selects(Bean) selected by the supplied
   * <code>beanSelectionCriteria</code>}, then an {@link AmbiguousResolutionException} is thrown.</p>
   *
   * @param beanSelectionCriteria a {@link BeanSelectionCriteria}; must not be {@code null}
   *
   * @return the resolved {@link Bean}, or {@code null}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @exception AmbiguousResolutionException if the supplied {@code beanSelectionCriteria} selects more than one {@link
   * Bean}
   */
  // Give me the single Bean that matches, null if none match, or throw an exception
  public default Bean<?> bean(final BeanSelectionCriteria beanSelectionCriteria) {
    return this.bean(beanSelectionCriteria, Alternate.Resolver::fail);
  }

}
