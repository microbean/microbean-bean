/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2024–2025 microBean™.
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

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.DynamicConstantDesc;

import java.util.Optional;

import org.microbean.constant.Constables;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_Object;

import static java.lang.constant.MethodHandleDesc.ofConstructor;

import static java.util.Objects.requireNonNull;

/**
 * A {@link Factory} that returns its singleton from its {@link #create(Request)} method.
 *
 * @param <I> the singleton type
 *
 * @param singleton the singleton
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #singleton()
 *
 * @see #create(Request)
 */
public final record Constant<I>(I singleton) implements Constable, Factory<I> {

  /**
   * Creates a new {@link Constant}.
   *
   * @param singleton an object; must not be {@code null}
   *
   * @exception NullPointerException if {@code singleton} is {@code null}
   */
  public Constant {
    requireNonNull(singleton, "singleton");
  }

  /**
   * Invokes the {@link #singleton()} method and returns its result.
   *
   * @param creation a {@link Creation}; may be {@code null}; ignored
   *
   * @return the result of invoking the {@link #singleton()} method
   *
   * @see #singleton()
   */
  @Override // Factory<I>
  public final I create(final Creation<I> creation) {
    return this.singleton;
  }

  /**
   * Returns {@code true} if and only if the return value of an invocation of the {@link #singleton()} method is {@link
   * AutoCloseable}.
   *
   * @return {@code true} if and only if the return value of an invocation of the {@link #singleton()} method is {@link
   * AutoCloseable}
   */
  @Override // Factory<I>
  public final boolean destroys() {
    return this.singleton instanceof AutoCloseable;
  }

  @Override // Factory<I>
  public final void destroy(final I i, final Destruction creation) {
    Factory.super.destroy(i, creation);
  }

  @Override // Constable
  public final Optional<DynamicConstantDesc<I>> describeConstable() {
    return Constables.describeConstable(this.singleton())
      .map(iDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                           ofConstructor(this.getClass().describeConstable().orElseThrow(),
                                                         CD_Object),
                                           iDesc));
  }


}
