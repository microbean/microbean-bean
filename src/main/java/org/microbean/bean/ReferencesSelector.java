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

/**
 * A supplier of {@link References} objects.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #references(AttributedType)
 *
 * @see References
 */
public interface ReferencesSelector {

  /**
   * Returns a {@link References} capable of locating contextual references of the relevant type.
   *
   * @param <S> the contextual reference type
   *
   * @param t an {@link AttributedType} describing the contextual reference type; must not be {@code null}
   *
   * @return a non-{@code null} {@link References}
   *
   * @exception NullPointerException if {@code t} is {@code null}
   *
   * @see References
   */
  public <S> References<S> references(final AttributedType t);

}
