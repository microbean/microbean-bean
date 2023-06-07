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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ConcurrentMap;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import org.junit.jupiter.api.Test;

import org.microbean.lang.Lang;

import org.microbean.lang.type.DelegatingTypeMirror;

import org.microbean.lang.visitor.Visitors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.microbean.lang.Lang.arrayType;
import static org.microbean.lang.Lang.declaredType;
import static org.microbean.lang.Lang.sameType;
import static org.microbean.lang.Lang.typeElement;
import static org.microbean.lang.Lang.unwrap;

final class TestReferenceTypeList {

  private TestReferenceTypeList() {
    super();
  }

  @Test
  final void testInterfacesOnly() {
    final TypeMirror s = Lang.declaredType("java.io.Serializable");
    final ReferenceTypeList rtl = ReferenceTypeList.closure(s);
    final List<? extends TypeMirror> types = rtl.types();
    System.out.println("*** types: " + types);

  }

  @Test
  final <T> void testSorting() {
    final List<TypeMirror> l = new ArrayList<>();
    final TypeMirror object = declaredType(Object.class);
    l.add(object);
    final TypeMirror mapStringString = declaredType(null, typeElement(Map.class), declaredType(String.class), declaredType(String.class));
    l.add(mapStringString);
    final TypeMirror concurrentMapStringString = declaredType(null, typeElement(ConcurrentMap.class), declaredType(String.class), declaredType(String.class));
    l.add(concurrentMapStringString);
    final TypeMirror objectArray = arrayType(Object[].class);
    l.add(objectArray);
    final ReferenceTypeList rtl = new ReferenceTypeList(l);
    List<? extends TypeMirror> types = rtl.types();
    assertEquals(4, types.size());
    assertSame(object, types.get(0));
    assertSame(objectArray, types.get(1));
    assertSame(concurrentMapStringString, types.get(2));
    assertSame(mapStringString, types.get(3));
    types = rtl.classes();
    assertEquals(1, types.size());
    assertSame(object, types.get(0));
    types = rtl.arrays();
    assertEquals(1, types.size());
    assertSame(objectArray, types.get(0));
    types = rtl.typeVariables();
    assertTrue(types.isEmpty());
    types = rtl.interfaces();
    assertEquals(2, types.size());
    assertSame(concurrentMapStringString, types.get(0));
    assertSame(mapStringString, types.get(1));
  }

  @Test
  final void testClosure() {
    final TypeMirror string = declaredType(String.class);
    final ReferenceTypeList rtl = ReferenceTypeList.closure(string);
    final List<? extends TypeMirror> types = rtl.types();
    assertEquals(7, types.size());
    assertSame(string, types.get(0));
    assertSame(unwrap(declaredType(Object.class)), unwrap(types.get(1)));
    assertSame(unwrap(declaredType(java.io.Serializable.class)), unwrap(types.get(2)));
    assertSame(unwrap(declaredType(CharSequence.class)), unwrap(types.get(3)));
    assertTrue(sameType(unwrap(declaredType(null, typeElement(Comparable.class), declaredType(String.class))), unwrap(types.get(4))));
    assertSame(unwrap(declaredType(java.lang.constant.Constable.class)), unwrap(types.get(5)));
    assertSame(unwrap(declaredType(java.lang.constant.ConstantDesc.class)), unwrap(types.get(6)));
  }

  @Test
  final <T extends ClassDesc, S extends String> void testTypeVariables() throws IllegalAccessException, NoSuchMethodException {
    final Visitors visitors = new Visitors(Lang.elementSource());
    final TypeVariable t = Lang.typeVariable(this.getClass().getDeclaredMethod("testTypeVariables"), "T");
    final TypeVariable s = Lang.typeVariable(this.getClass().getDeclaredMethod("testTypeVariables"), "S");

    System.out.println("*** supertype of " + t + ": " + visitors.supertypeVisitor().visit(t));
    System.out.println("*** supertype of " + s + ": " + visitors.supertypeVisitor().visit(s));
    ReferenceTypeList rtl = ReferenceTypeList.closure(t, visitors);
    System.out.println("*** rtl types: " + rtl.types());
    rtl = ReferenceTypeList.closure(s, visitors);
    System.out.println("*** rtl types: " + rtl.types());
  }

}
