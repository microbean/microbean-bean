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
 * An interface whose implementation perform pre-destruction logic for contextual instances.
 *
 * <p>{@link PreDestructor}s are typically used during the execution of a {@link Factory}'s {@link
 * Factory#destroy(Object, Request)} method.</p>
 *
 * @param <I> the type of contextual instance
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see Factory#destroy(Object, Request)
 */
// Subordinate to Factory<I>.
// Calls preDestroy() methods.
@FunctionalInterface
public interface PreDestructor<I> {

  /**
   * Performs pre-destruction logic for the supplied contextual instance.
   *
   * @param i a contextual instance; must not be {@code null}
   *
   * @param r a {@link Request}; msut not be {@code null}
   *
   * @return {@code i} as supplied
   *
   * @exception NullPointerException if either argument is {@code null}
   */
  public I preDestroy(final I i, final Request<I> r);

}
