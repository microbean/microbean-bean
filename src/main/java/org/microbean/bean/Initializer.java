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
 * An interface whose implementations {@linkplain #initialize(Object, Request) initialize} contextual instances.
 *
 * <p>{@link Initializer}s are subordinate to {@link Producer}s, typically operating on the contextual instances they
 * produce.</p>
 *
 * @param <I> the type of contextual instance
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #initialize(Object, Request)
 */
// Subordinate to Factory<I> (really to PostInitializer<I>). Normally applied to Producer<I> output.
// Calls initializer methods and injects fields
// Note that this deliberately extends Aggregate, providing access to dependencies.
public interface Initializer<I> extends Aggregate {

  /**
   * Initializes the supplied contextual instance, possibly using the supplied {@link Request} to obtain supporting
   * contextual references.
   *
   * <p>Typically {@link Initializer} implementations will call <dfn>initializer methods</dfn> and perform <dfn>field
   * injection</dfn> on the supplied contextual instance.</p>
   *
   * @param i the contextual instance to initialize; may be {@code null}
   *
   * @param r a {@link Request} that can be used to acquire supporting contextual references; may be {@code null}
   *
   * @return the initialized instance, or a copy of it, or a stand-in for it
   *
   * @see Producer#produce(Request)
   */
  public I initialize(final I i, final Request<I> r);

}
