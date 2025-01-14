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

/**
 * A {@link BeanException} concerning problematic <dfn>reductions</dfn>.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see Reducible
 *
 * @see Reducer
 */
public class ReductionException extends BeanException {

  private static final long serialVersionUID = 1L;

  private final transient Object criteria;

  /**
   * Creates a new {@link ReductionException}.
   *
   * @param criteria an {@link Object} representing reduction criteria; may be {@code null}
   *
   * @param message a detail message; may be {@code null}
   *
   * @param cause a {@link Throwable} that caused this {@link ReductionException} to be created; may be {@code null}
   */
  public ReductionException(final Object criteria, final String message, final Throwable cause) {
    super(message, cause);
    this.criteria = criteria;
  }

  /**
   * Returns this {@link ReductionException}'s criteria object, which may be {@code null}.
   *
   * @return this {@link ReductionException}'s criteria object, which may be {@code null}
   */
  public final Object criteria() {
    return this.criteria;
  }

  /**
   * Returns a {@link String} reprsentation of this {@link ReductionException}.
   *
   * @return a {@link String} reprsentation of this {@link ReductionException}; never {@code null}
   */
  @Override
  public String toString() {
    final Object criteria = this.criteria();
    return criteria == null ? super.toString() : super.toString() + ": criteria: " + criteria;
  }

}
