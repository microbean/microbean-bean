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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.microbean.lang.JavaLanguageModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.microbean.bean.Qualifiers.anyAndDefaultQualifiers;
import static org.microbean.bean.Qualifiers.defaultQualifier;

import static org.microbean.scope.Scope.SINGLETON_ID;

final class TestBeans {

  private JavaLanguageModel jlm;
  
  private TestBeans() {
    super();
  }

  @BeforeEach
  final void setup() {
    this.jlm = new JavaLanguageModel();
  }

  @Test
  final void testBeans() {
    Bean<String> hello =
      new Bean<>(new Id(List.of(jlm.type(String.class), jlm.type(Object.class)),
                        anyAndDefaultQualifiers(),
                        SINGLETON_ID),
                 c -> "Hello");
    final Beans beans = new Beans(Set.of(hello));
    final Set<Bean<?>> set = beans.beans();
    assertEquals(2, set.size());
    hello = beans.bean(new Selector<>(jlm.type(String.class), List.of(defaultQualifier()))).cast();
    assertSame("Hello", hello.factory().create(null));
    hello = beans.bean(new Selector<>(jlm.type(Object.class), List.of(defaultQualifier()))).cast();
    assertSame("Hello", hello.factory().create(null));
  }

}
