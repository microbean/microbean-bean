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

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.microbean.constant.Constables;

import org.microbean.lang.Lang;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.microbean.bean.Qualifiers.anyAndDefaultQualifiers;

import static org.microbean.scope.Scope.SINGLETON_ID;

final class TestBean {

  private TestBean() {
    super();
  }

  @Test
  final void testConstableStuff() {
    final Factory<String> f = new Singleton<>("Hello");
    final Id id =
      new Id(List.of(Lang.declaredType(String.class), Lang.declaredType(Object.class)),
             anyAndDefaultQualifiers(),
             SINGLETON_ID);
    assertTrue(id instanceof Constable);
    assertFalse(Constables.describeConstable(id.types()).isEmpty());
    assertFalse(id.describeConstable().isEmpty());
  }

  @Test
  final void testAnyAndDefaultQualifiersCanBeConstable() {
    assertFalse(Constables.describeConstable(anyAndDefaultQualifiers()).isEmpty());
  }
  
}
