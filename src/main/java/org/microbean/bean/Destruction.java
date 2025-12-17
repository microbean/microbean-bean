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
 * An object describing the imminent destruction of a contextual instance by the {@link Factory#destroy(Object,
 * Destruction)} method.
 *
 * <p>Any {@link Destruction} implementation <strong>must</strong> also be a {@link Creation} implementation, or
 * undefined behavior and errors may occur.</p>
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #close()
 *
 * @see Creation
 *
 * @see ReferencesSelector
 */
public interface Destruction extends AutoCloseable, ReferencesSelector {

  /**
   * Closes this {@link Destruction} idempotently, normally thereby releasing a contextual instance's dependent objects
   * that have been stored opaquely in this {@link Destruction} by some other mechanism at {@linkplain
   * Factory#create(Creation) creation time}.
   */
  @Override // AutoCloseable
  public void close();

}
