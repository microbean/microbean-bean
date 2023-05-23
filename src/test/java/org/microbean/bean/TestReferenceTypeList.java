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

import java.util.List;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import org.junit.jupiter.api.Test;

import org.microbean.lang.Lang;

import org.microbean.lang.visitor.Visitors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.microbean.lang.Lang.unwrap;

final class TestReferenceTypeList {

  private TestReferenceTypeList() {
    super();
  }

  @Test
  final void testInterfacesOnly() {
    final TypeMirror s = Lang.declaredType("java.io.Serializable");
    final ReferenceTypeList rtl = ReferenceTypeList.closure(s);
    final List<TypeMirror> types = rtl.types();
    System.out.println("*** types: " + types);

  }

  @Test
  final void testClosure() {
    final TypeMirror s = Lang.declaredType("java.lang.String");
    final ReferenceTypeList rtl = ReferenceTypeList.closure(s);
    System.out.println("*** classTypes: " + rtl.classTypes());
    System.out.println("*** interfaceTypes: " + rtl.interfaceTypes());
    final List<TypeMirror> types = rtl.types();
    assertSame(unwrap(s), unwrap(types.get(0)));
    assertSame(unwrap(Lang.declaredType("java.lang.Object")), unwrap(types.get(types.size() - 1)));
    System.out.println("*** types: " + types);
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
