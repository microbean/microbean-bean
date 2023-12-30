/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023 microBean™.
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

import java.lang.constant.ClassDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;

import java.util.Objects;
import java.util.Optional;

import org.microbean.constant.Constables;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.FALSE;
import static java.lang.constant.ConstantDescs.TRUE;

public final class Singleton<I> implements Factory<I> {

  private static final ClassDesc CD_Singleton = ClassDesc.of(Singleton.class.getName());

  private volatile I singleton;

  private final I product;

  public Singleton(final I product) {
    this(product, false);
  }

  public Singleton(final I product, final boolean preCreate) {
    super();
    this.product = Objects.requireNonNull(product, "product");
    this.singleton = preCreate ? product : null;
  }

  @Override
  public final boolean destroys() {
    return false;
  }

  @Override
  public final I singleton() {
    return this.singleton;
  }

  @Override
  public final I create(final Creation<I> c, final ReferenceSelector references) {
    if (this.singleton == null) {
      this.singleton = this.product;
    }
    return this.product;
  }

  @Override // Constable
  public final Optional<DynamicConstantDesc<Singleton<I>>> describeConstable() {
    return Constables.describeConstable(this.product)
      .map(productDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                 MethodHandleDesc.ofConstructor(CD_Singleton,
                                                                                CD_Object,
                                                                                CD_boolean),
                                                 productDesc,
                                                 this.singleton == null ? FALSE : TRUE));
  }

}
