/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2025 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.microbean.bean;

/**
 * A representation of a {@link Factory}'s {@linkplain Factory#create(Request) creation activity}.
 *
 * @param <I> the type of instance being created
 *
 * @author <a href="https://about.me/lairdnelson" target="_parent">Laird Nelson</a>
 */
@FunctionalInterface
public interface Creation<I> {

  /**
   * Signals that the supplied {@code instance} has been created, typically by an invocation of a {@link Factory}'s
   * {@link Factory#create(Request) create(Request)} method, and is about to be made available for use.
   *
   * <p>This method is typically invoked from within a {@link Factory#create(Request)} implementation immediately prior
   * to its returning a value.</p>
   *
   * <p>It is permissible for an implementation of this method to do nothing.</p>
   *
   * @param instance the instance that was created; must not be {@code null}
   *
   * @exception NullPointerException if {@code instance} was {@code null} and the implementation does not support {@code
   * null} arguments
   *
   * @exception IllegalArgumentException if {@code instance} was found to be unsuitable for any reason
   *
   * @microbean.idempotency Implementations of this method must be idempotent.
   *
   * @microbean.threadsafety Implementations of this method must be safe for concurrent use by multiple threads.
   */
  // MUST be idempotent
  // For incomplete instances; see also https://stackoverflow.com/questions/50202523/creationalcontext-should-a-custom-bean-always-call-push-from-its-create-met
  public void created(final I instance);

}
