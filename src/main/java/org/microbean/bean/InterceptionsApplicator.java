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

import java.util.function.BiFunction;

/**
 * An interface whose implementations install <dfn>around-invoke</dfn> interceptions.
 *
 * @param <I> the type of contextual instance
 *
 * <p>{@link InterceptionsApplicator} implementations are typically used to implement the {@link
 * Factory#create(Request)} method, together with {@link PostInitializer}s, {@link Initializer}s and {@link Producer}s.</p>
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #apply(Object, Request)
 *
 * @see PostInitializer
 *
 * @see Initializer
 *
 * @see Producer
 */
// Subordinate to Factory<I>. Normally applied to PostInitializer<I> output.
// An applicator of business method interceptions. This is used during assembly of a Factory implementation and should
// be used probably only when "around-invoke" interceptions are in effect.
@FunctionalInterface
public interface InterceptionsApplicator<I> extends BiFunction<I, Request<I>, I> {

  /**
   * Installs <dfn>around-invoke</dfn> method interceptions on the supplied contextual instance, which is presumed to
   * have been {@linkplain PostInitializer fully initialized}, and returns the result.
   *
   * @param uninterceptedInstance a fully initialized contextual instance that needs to have certain of its methods
   * intercepted; must not be {@code null}
   *
   * @param r a {@link Request} which may be used to acquire supporting contextual references; must not be {@code null}
   *
   * @return the supplied contextual instance, a copy of it, or a proxy wrapping it; never {@code null}
   *
   * @exception NullPointerException if any argument is {@code null}
   */
  @Override // BiFunction<I, Request<I>, I>
  public I apply(final I uninterceptedInstance, final Request<I> r);

}
