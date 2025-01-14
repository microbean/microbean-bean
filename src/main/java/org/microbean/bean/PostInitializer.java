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
 * An interface whose implementations complete initialization of contextual instances.
 *
 * <p>{@link PostInitializer}s are typically applied to {@linkplain Initializer#initialize(Object, Request) initialized
 * instances}, i.e. contextual instances that have been fully injected.</p>
 *
 * <p>{@link PostInitializer}s are typically used in implementations of the {@link Factory#create(Request)} method,
 * together with {@link InterceptionsApplicator}s, {@link Initializer}s and {@link Producer}s.</p>
 *
 * @param <I> the type of contextual instance
 * 
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #postInitialize(Object, Request)
 *
 * @see Initializer
 *
 * @see InterceptionsApplicator
 *
 * @see Producer
 */
// Subordinate to Factory<I> (really to InterceptionsApplicator<I>). Normally applied to Initializer<I> output.
// Calls postConstruct() methods.
// Note that this deliberately does NOT extend Aggregate, since initialization must have already occurred.
public interface PostInitializer<I> {

  /**
   * Completes the initialization of a contextual instance and returns the result.
   *
   * @param i an {@linkplain Initializer#initialize(Object, Request) initialized} contextual instance; must not be
   * {@code null}
   *
   * @param r a {@link Request}; must not be {@code null}
   *
   * @return a contextual instance that has been completely initialized; never {@code null}
   *
   * @exception NullPointerException if either argument is {@code null}
   */
  public I postInitialize(final I i, final Request<I> r);

}
