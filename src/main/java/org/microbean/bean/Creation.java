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
 * A representation of a {@link Factory}'s {@linkplain Factory#create(Creation) creation activity}.
 *
 * <p>Any {@link Creation} implementation must also be a {@link Destruction} implementation, or undefined behavior and
 * errors may occur.</p>
 *
 * <p>Many {@link Creation} implementations are also {@link AutoCloseableRegistry} implementations. This is not a
 * requirement of the {@link Creation} or {@link Destruction} contracts.</p>
 *
 * @param <I> the type of instance being created
 *
 * @author <a href="https://about.me/lairdnelson" target="_parent">Laird Nelson</a>
 *
 * @see #creating(Object)
 *
 * @see AutoCloseableRegistry
 *
 * @see Destruction
 *
 * @see ReferencesSelector
 */
public interface Creation<I> extends ReferencesSelector {

  /**
   * Returns the {@link Id} of the {@link Bean} {@linkplain Bean#factory() whose} {@link Factory}'s {@link
   * Factory#create(Creation)} invocation is responsible for the existence of this {@link Creation}.
   *
   * <p>Implementations of this method may return {@code null}.</p>
   *
   * <p>Implementations of this method must return a determinate value.</p>
   *
   * @return an {@link Id}, or {@code null}
   *
   * @see Bean
   *
   * @see Factory#create(Creation)
   */
  public Id id();

  /**
   * Signals that the supplied {@code instance} is in the process of being created, typically by an invocation of a
   * {@link Factory}'s {@link Factory#create(Creation) create(Creation)} method, and is about to be made available for
   * use.
   *
   * <p>This method is typically invoked from within a {@link Factory#create(Creation)} implementation immediately
   * prior to its returning a value.</p>
   *
   * <p>It is permissible and very common for an implementation of this method to do nothing. The default implementation
   * of this method does nothing.</p>
   *
   * <p>Implementations of this method <strong>must</strong> ensure that if two or more invocations of this method are
   * supplied with the same object reference, only one invocation will have any effect, whether by throwing an {@link
   * IllegalArgumentException} or by simply ignoring redundant invocations.</p>
   *
   * @param instance the instance that is in the process of being returned from an invocation of a {@link
   * Factory#create(Creation)} method; may be {@code null}
   *
   * @exception IllegalArgumentException if {@code instance} was found to be unsuitable for any reason
   */
  // MUST be idempotent
  // For incomplete instances; see also https://stackoverflow.com/questions/50202523/creationalcontext-should-a-custom-bean-always-call-push-from-its-create-met
  public default void creating(final I instance) {

  }

}
