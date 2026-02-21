/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2024–2026 microBean™.
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

import java.util.List;

import javax.lang.model.type.TypeMirror;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.microbean.construct.DefaultDomain;
import org.microbean.construct.Domain;

import static javax.lang.model.type.TypeKind.INT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.microbean.assign.Types.erasedName;

final class TestBeanTypes {

  private Domain domain;

  private BeanTypes beanTypes;

  private TestBeanTypes() {
    super();
  }

  @BeforeEach
  final void setup() {
    this.domain = new DefaultDomain();
    this.beanTypes = new BeanTypes(this.domain);
  }


  @Test
  final void testDirectSupertypesOfintAreEmpty() {
    final List<? extends TypeMirror> ts = domain.directSupertypes(domain.primitiveType(INT));
    assertTrue(ts.isEmpty()); // although note JLS 4.10.1; oh well
  }

  @Test
  final void testSupertypesOfint() {
    final List<? extends TypeMirror> ts = beanTypes.supertypes(domain.primitiveType(INT));
    assertEquals(1, ts.size());
    assertEquals("int", erasedName(ts.get(0)));
  }

  @Test
  final void testBeanTypesOfint() {
    final List<? extends TypeMirror> ts = beanTypes.beanTypes(domain.primitiveType(INT));
    assertEquals(2, ts.size());
    assertEquals("int", erasedName(ts.get(0)));
    assertEquals("java.lang.Object", erasedName(ts.get(1))); // per CDI specification
  }


  @Test
  final void testDirectSupertypesOfObjectArrayAreASingleIntersectionType() {
    final List<? extends TypeMirror> ts = domain.directSupertypes(domain.arrayTypeOf(domain.declaredType("java.lang.Object")));
    assertEquals(1, ts.size());
    assertEquals("java.io.Serializable&java.lang.Cloneable", erasedName(ts.get(0))); // wut
  }

  @Test
  final void testSupertypesOfObjectArray() {
    final List<? extends TypeMirror> ts = beanTypes.supertypes(domain.arrayTypeOf(domain.declaredType("java.lang.Object")));
    assertEquals(5, ts.size());
    assertEquals("java.lang.Object[]", erasedName(ts.get(0)));
    assertEquals("java.io.Serializable&java.lang.Cloneable", erasedName(ts.get(1))); // wut
    assertEquals("java.lang.Object", erasedName(ts.get(2)));
    assertEquals("java.io.Serializable", erasedName(ts.get(3)));
    assertEquals("java.lang.Cloneable", erasedName(ts.get(4)));
  }

  @Test
  final void testBeanTypesOfObjectArray() {
    final List<? extends TypeMirror> ts = beanTypes.beanTypes(domain.arrayTypeOf(domain.declaredType("java.lang.Object")));
    assertEquals(2, ts.size());
    assertEquals("java.lang.Object[]", erasedName(ts.get(0)));
    assertEquals("java.lang.Object", erasedName(ts.get(1)));
  }


  @Test
  final void testSupertypesOfString() {
    final List<? extends TypeMirror> ts = beanTypes.supertypes(domain.declaredType("java.lang.String"));
    assertEquals(7, ts.size(), ts.toString());
    assertEquals("java.lang.String", erasedName(ts.get(0)));
    assertEquals("java.lang.Object", erasedName(ts.get(1)));
    assertEquals("java.io.Serializable", erasedName(ts.get(2)));
    assertEquals("java.lang.CharSequence", erasedName(ts.get(3)));
    assertEquals("java.lang.Comparable", erasedName(ts.get(4)));
    assertEquals("java.lang.constant.Constable", erasedName(ts.get(5)));
    assertEquals("java.lang.constant.ConstantDesc", erasedName(ts.get(6)));
  }

  @Test
  final void testBeanTypesOfString() {
    final List<? extends TypeMirror> ts = beanTypes.beanTypes(domain.declaredType("java.lang.String"));
    assertEquals(7, ts.size(), ts.toString());
    assertEquals("java.lang.String", erasedName(ts.get(0)));
    assertEquals("java.lang.Object", erasedName(ts.get(1)));
    assertEquals("java.io.Serializable", erasedName(ts.get(2)));
    assertEquals("java.lang.CharSequence", erasedName(ts.get(3)));
    assertEquals("java.lang.Comparable", erasedName(ts.get(4)));
    assertEquals("java.lang.constant.Constable", erasedName(ts.get(5)));
    assertEquals("java.lang.constant.ConstantDesc", erasedName(ts.get(6)));

  }


  @Test
  final void testDirectSupertypesOfInterfaceA() {
    final List<? extends TypeMirror> ts = domain.directSupertypes(domain.declaredType("org.microbean.bean.TestBeanTypes.A"));
    assertEquals(1, ts.size());
    assertEquals("java.lang.Object", ts.get(0).toString());
  }

  @Test
  final void testDirectSupertypesOfInterfaceBExtendsA() {
    final List<? extends TypeMirror> ts = domain.directSupertypes(domain.declaredType("org.microbean.bean.TestBeanTypes.B"));
    assertEquals(2, ts.size());
    assertEquals("java.lang.Object", erasedName(ts.get(0)));
    assertEquals("org.microbean.bean.TestBeanTypes.A", erasedName(ts.get(1)));
  }

  @Test
  final void testDirectSupertypesOfInterfaceCExtendsBAreInDeclarationOrder() {
    final List<? extends TypeMirror> ts = domain.directSupertypes(domain.declaredType("org.microbean.bean.TestBeanTypes.C"));
    assertEquals(3, ts.size());
    assertEquals("java.lang.Object", erasedName(ts.get(0)));
    assertEquals("org.microbean.bean.TestBeanTypes.A", erasedName(ts.get(1)));
    assertEquals("org.microbean.bean.TestBeanTypes.B", erasedName(ts.get(2)));
  }


  @Test
  final void testSupertypesOfListExtendsString() {
    final List<? extends TypeMirror> ts =
      beanTypes.supertypes(domain.declaredType(domain.typeElement("java.util.List"),
                                            domain.wildcardType(domain.declaredType("java.lang.String"),
                                                             null)));
    assertEquals(5, ts.size());
    assertEquals("java.lang.Object", erasedName(ts.get(0)));
    assertEquals("java.util.List", erasedName(ts.get(1)));
    assertEquals("java.util.SequencedCollection", erasedName(ts.get(2)));
    assertEquals("java.util.Collection", erasedName(ts.get(3)));
    assertEquals("java.lang.Iterable", erasedName(ts.get(4)));
  }

  @Test
  final void testBeanTypesOfListExtendsString() {
    final List<? extends TypeMirror> ts = beanTypes.beanTypes(domain.declaredType(domain.typeElement("java.util.List"),
                                                                     domain.wildcardType(domain.declaredType("java.lang.String"),
                                                                                      null)));
    assertEquals(1, ts.size());
    assertEquals("java.lang.Object", erasedName(ts.get(0)));
  }


  @Test
  final void testSupertypesOfWildcard() {
    final TypeMirror t = domain.wildcardType(domain.declaredType("java.lang.String"), null);
    final List<? extends TypeMirror> ts = beanTypes.supertypes(t);
    assertEquals(1, ts.size()); // no direct supertypes
    assertSame(t, ts.get(0));
  }

  @Test
  final void testBeanTypesOfWildcard() {
    assertTrue(beanTypes.beanTypes(domain.wildcardType(domain.declaredType("java.lang.String"), null)).isEmpty());
  }


  /*
  @Test
  final <T> void testSupertypesOfTypeVariable() throws ReflectiveOperationException {
    final List<TypeMirror> ts =
      beanTypes.supertypes(domain.typeVariable(this.getClass().getDeclaredMethod("testSupertypesOfTypeVariable").getTypeParameters()[0]));
    assertEquals(2, ts.size());
    assertEquals("T", erasedName(ts.get(0)));
    assertEquals("java.lang.Object", erasedName(ts.get(1)));
  }

  @Test
  final <T> void testBeanTypesOfTypeVariable() throws ReflectiveOperationException {
    final List<TypeMirror> ts = beanTypes.beanTypes(domain.typeVariable(this.getClass().getDeclaredMethod("testBeanTypesOfTypeVariable").getTypeParameters()[0]));
    assertEquals(1, ts.size());
    assertEquals("java.lang.Object", erasedName(ts.get(0)));
  }
  */


  private static interface A {};

  private static interface B extends A {};

  private static interface C extends A, B {};

}
