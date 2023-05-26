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

import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;

import java.util.Objects;
import java.util.Optional;

import org.microbean.constant.Constables;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_Object;

import static org.microbean.bean.ConstantDescs.CD_SingletonFactory;

public class SingletonFactory<I> implements Factory<I> {

  private final I singleton;

  public SingletonFactory(final I singleton) {
    super();
    this.singleton = singleton;
  }

  @Override // Factory<I>
  public final I singleton() {
    return this.singleton;
  }

  @Override // Constable
  public Optional<DynamicConstantDesc<SingletonFactory<I>>> describeConstable() {
    return Constables.describeConstable(this.singleton())
      .map(singletonDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                   MethodHandleDesc.ofConstructor(CD_SingletonFactory,
                                                                                  CD_Object),
                                                   singletonDesc));
  }

  @Override // Factory<I>
  public final I produce(final Creation<I> c) {
    return this.singleton();
  }

  @Override // Object
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other != null && other.getClass() == this.getClass()) {
      return Objects.equals(this.singleton(), ((SingletonFactory<?>)other).singleton());
    } else {
      return false;
    }
  }

  @Override // Object
  public final int hashCode() {
    final Object s = singleton();
    if (s == null) {
      return 527; // 31 * 17 + 0
    }
    return 527 + s.hashCode();
  }

  @Override
  public String toString() {
    return "c -> {" + this.singleton() + "}";
  }
  
}
