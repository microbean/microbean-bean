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

import java.util.Objects;

/**
 * A pairing of an {@link AttributedType} and a {@link Bean} resulting from a reduction.
 *
 * @param <I> the type of contextual instances the supplied {@link Bean} is capable of creating
 *
 * @param attributedType an {@link AttributedType}
 *
 * @param bean a {@link Bean} deemed suitable for the {@code attributedType}
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @deprecated This class seems not to be needed so is tentatively deprecated.
 */
@Deprecated(since = "0.0.18")
public final record BeanReduction<I>(AttributedType attributedType, Bean<I> bean) {

  /**
   * Creates a new {@link BeanReduction}.
   *
   * @param attributedType an {@link AttributedType}; must not be {@code null}
   *
   * @param bean a {@link Bean} deemed suitable for the {@code attributedType}; must not be {@code null}
   */
  public BeanReduction {
    Objects.requireNonNull(attributedType, "attributedType");
    Objects.requireNonNull(bean, "bean");
  }

}
