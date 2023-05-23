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

import javax.lang.model.type.TypeKind;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.microbean.lang.Lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.microbean.bean.Qualifiers.anyAndDefaultQualifiers;
import static org.microbean.bean.Qualifiers.defaultQualifier;

import static org.microbean.scope.Scope.SINGLETON_ID;

final class TestSelector {

  private TestSelector() {
    super();
  }

  @Test
  final void testSelectorStringSelectsString() {
    final Selector<?> s = new Selector<>(Lang.declaredType(String.class), List.of());
    assertTrue(s.selects(Lang.declaredType(String.class)));
  }
  
  @Test
  final void testSelectorStringDoesNotSelectObject() {
    final Selector<?> s = new Selector<>(Lang.declaredType(String.class), List.of());
    assertFalse(s.selects(Lang.declaredType(Object.class)));
  }

  @Test
  final void testSelectorIntSelectsInteger() {
    final Selector<?> s = new Selector<>(Lang.primitiveType(TypeKind.INT), List.of()); // boxing is true by default
    /*
      java.lang.NullPointerException: Cannot invoke "com.sun.tools.javac.code.Symbol$ClassSymbol.erasure(com.sun.tools.javac.code.Types)" because "sym" is null
      at jdk.compiler/com.sun.tools.javac.model.JavacTypes.getDeclaredType(JavacTypes.java:237)
      at jdk.compiler/com.sun.tools.javac.model.JavacTypes.getDeclaredType(JavacTypes.java:249)
      at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.Lang.declaredType(Lang.java:664)
      at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.Lang.declaredType(Lang.java:611)
      at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.TestSelector.testSelectorIntSelectsInteger(TestSelector.java:63)
    */
    assertTrue(s.selects(Lang.declaredType(Integer.class)));
  }
  
  @Test
  final void testSelectorObjectDoesNotSelectString() {
    final Selector<?> s = new Selector<>(Lang.declaredType(Object.class), List.of());
    assertFalse(s.selects(Lang.declaredType(String.class)));
  }

  @Test
  final void testSelectorListUnknownExtendsStringSelectsListString() {
    final Selector<?> s =
      new Selector<>(Lang.declaredType(Lang.typeElement(List.class),
                                       Lang.wildcardType(Lang.declaredType(String.class), null)),
                     List.of());
    assertTrue(s.selects(Lang.declaredType(Lang.typeElement(List.class), Lang.declaredType(String.class))));
  }

}
