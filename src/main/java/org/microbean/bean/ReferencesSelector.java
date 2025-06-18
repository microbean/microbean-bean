/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2025 microBean™.
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

import org.microbean.assign.AttributedType;

/**
 * A supplier of {@link References} objects.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #references(AttributedType)
 *
 * @see #reference(AttributedType)
 *
 * @see References
 */
// TODO: not crazy about the circular dependencies, but they're no worse than, say, a sealed class
public interface ReferencesSelector {

  /**
   * Destroys the supplied contextual reference if and only if it meets the conditions for destruction.
   *
   * @param r a contextual reference; may be {@code null} in which case {@code false} will be returned
   *
   * @return {@code true} if and only if destruction occurred
   *
   * @exception DestructionException if an error occurs
   */
  public boolean destroy(final Object r); // e.g. CDI's Instance#destroy(Object); works only on normal- and @Dependent-scoped objects

  /**
   * Returns a {@link References} capable of locating contextual references of the relevant type.
   *
   * @param <R> the contextual reference type
   *
   * @param t an {@link AttributedType} describing the contextual reference type; must not be {@code null}
   *
   * @return a non-{@code null} {@link References}
   *
   * @exception NullPointerException if {@code t} is {@code null}
   *
   * @see References
   */
  public <R> References<R> references(final AttributedType t);

  /**
   * A convenience method that acquires and returns what is presumed, possibly incorrectly, to be the sole contextual
   * reference of the relevant type.
   *
   * @param <R> the contextual reference type
   *
   * @param t an {@link AttributedType} describing the contextual reference type; must not be {@code null}
   *
   * @return a non-{@code null} contextual reference
   *
   * @exception NullPointerException if {@code t} is {@code null}
   *
   * @exception UnsatisfiedReductionException if there is no contextual reference for the relevant type
   *
   * @exception AmbiguousReductionException if there is more than one contextual reference for the relevant type
   *
   * @see #references(AttributedType)
   *
   * @see References#get()
   */
  public default <R> R reference(final AttributedType t) {
    return this.<R>references(t).get();
  }

}
