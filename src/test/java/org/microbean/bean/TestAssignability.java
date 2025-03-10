/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2024–2025 microBean™.
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

import java.io.Serializable;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import org.junit.jupiter.api.Test;

import org.microbean.construct.DefaultDomain;
import org.microbean.construct.Domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestAssignability {

  private static final Domain domain = new DefaultDomain();

  private TestAssignability() {
    super();
  }

  @Test
  final <T extends Integer & Serializable, S extends Number & Serializable, R extends S> void testAssignabilityOfTypeVariable() throws NoSuchMethodException {
    final ExecutableElement ee = domain.executableElement(domain.typeElement(this.getClass().getName()),
                                                          domain.noType(TypeKind.VOID),
                                                          "testAssignabilityOfTypeVariable");
    final TypeVariable t = (TypeVariable)domain.typeParameterElement(ee, "T").asType();
    final TypeMirror s = domain.typeParameterElement(ee, "S").asType();
    final TypeMirror r = domain.typeParameterElement(ee, "S").asType();
    final DeclaredType integer = domain.declaredType("java.lang.Integer");
    final DeclaredType serializable = domain.declaredType("java.io.Serializable");
    final DeclaredType number = domain.declaredType("java.lang.Number");

    //
    // IMPORTANT:
    //
    // Remember that Domain#assignable(), like javax.lang.model.util.Types#isAssignable() on which it is based, is
    // "backwards"!
    //

    // integer is the *payload*; number is the *receiver*; Integer is assignable to Number
    assertTrue(domain.assignable(integer, number));
    assertTrue(domain.assignable(integer, serializable));
    assertTrue(domain.assignable(number, serializable));

    // t is the *payload*; integer is the *receiver*; T is assignable to Integer
    assertTrue(domain.assignable(t, integer));
    assertTrue(domain.assignable(t, serializable));

    // t has multiple bounds so its upper bound is an intersection type
    final IntersectionType tUpper = (IntersectionType)t.getUpperBound();
    assertSame(TypeKind.INTERSECTION, tUpper.getKind());

    // t's upper bound is an intersection type, one of whose components is Integer; it is therefore assignable to Integer
    assertTrue(domain.assignable(tUpper, integer));
    assertTrue(domain.assignable(tUpper, serializable));

    // t is the *payload*; number is the *receiver*; T is assignable to Number (by transitivity)
    assertTrue(domain.assignable(t, number));

    // t and s are not assignable in either direction
    assertFalse(domain.assignable(t, s));
    assertFalse(domain.assignable(s, t));

    // r is the *payload*; s is the *receiver*; R is assignable to S (it extends it)
    assertTrue(domain.assignable(r, s));
  }

  @Test
  final void testAssignabilityOfWildcard() {
    final DeclaredType string = domain.declaredType("java.lang.String");
    final WildcardType qExtendsString = domain.wildcardType(string, null);
    final DeclaredType extendsBound = (DeclaredType)qExtendsString.getExtendsBound();
    assertSame(TypeKind.DECLARED, extendsBound.getKind());
    assertNotSame(string, extendsBound); // or could be; can't rely on it
    assertEquals("java.lang.String", ((TypeElement)extendsBound.asElement()).getQualifiedName().toString());
    assertTrue(domain.sameType(string, extendsBound));
    assertEquals(string, extendsBound);

    // Perhaps these assertions are surprising but they are correct.
    assertFalse(domain.assignable(string, qExtendsString));
    assertFalse(domain.assignable(qExtendsString, string));
  }

}
