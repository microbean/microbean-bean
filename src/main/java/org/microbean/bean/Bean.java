/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2022 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.bean;

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;

import java.util.Objects;
import java.util.Optional;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;

import static java.lang.constant.DirectMethodHandleDesc.Kind.STATIC;

import static org.microbean.bean.ConstantDescs.CD_Bean;
import static org.microbean.bean.ConstantDescs.CD_Factory;
import static org.microbean.bean.ConstantDescs.CD_Id;

import static org.microbean.scope.Scope.SINGLETON;

public final record Bean<I>(Factory<I> factory, Id id) implements Alternate, Constable {


  /*
   * Constructors.
   */


  @Deprecated
  public Bean {
    Objects.requireNonNull(factory, "factory");
    Objects.requireNonNull(id, "id");
  }


  /*
   * Instance methods.
   */


  @Override // Alternate
  public final boolean alternate() {
    return this.id().alternate();
  }

  @Override // Prioritized
  public final int priority() {
    return this.id().priority();
  }

  @Override // Constable
  public final Optional<? extends ConstantDesc> describeConstable() {
    final Object factory = this.factory();
    if (factory instanceof Constable constableFactory) {
      final ConstantDesc factoryCd = constableFactory.describeConstable().orElse(null);
      if (factoryCd != null) {
        final ConstantDesc idCd = this.id().describeConstable().orElse(null);
        if (idCd != null) {
          return
            Optional.of(DynamicConstantDesc.of(BSM_INVOKE,
                                               MethodHandleDesc.ofMethod(STATIC,
                                                                         CD_Bean,
                                                                         "of",
                                                                         MethodTypeDesc.of(CD_Bean,
                                                                                           CD_Factory,
                                                                                           CD_Id)),
                                               factoryCd,
                                               idCd));
        }
      }
    }
    return Optional.empty();
  }


  /*
   * Static methods.
   */


  public static final <I> Bean<I> of(final I singleton) {
    return of(Value.of(singleton), Id.of(Selector.ofAnyAndDefault(singleton.getClass()), SINGLETON.id()));
  }

  public static final <I> Bean<I> of(final Factory<I> factory, final Id id) {
    return new Bean<>(factory, id);
  }

}
