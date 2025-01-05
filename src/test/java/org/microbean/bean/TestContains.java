/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2024 microBean™.
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
import javax.lang.model.element.TypeParameterElement;

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

final class TestContains {

  private static final Domain domain = new DefaultDomain();

  private TestContains() {
    super();
  }

  @Test
  final <T extends String, S extends T> void testWildcardContains() {

    final ExecutableElement ee = domain.executableElement(domain.typeElement(this.getClass().getName()),
                                                          domain.noType(TypeKind.VOID),
                                                          "testWildcardContains");
    final TypeMirror t = domain.typeParameterElement(ee, "T").asType();
    final TypeMirror s = domain.typeParameterElement(ee, "S").asType();
    final DeclaredType string = domain.declaredType("java.lang.String");
    final DeclaredType charSequence = domain.declaredType("java.lang.CharSequence");
    final DeclaredType object = domain.declaredType("java.lang.Object");
    final WildcardType qExtendsCharSequence = domain.wildcardType(charSequence, null);
    final WildcardType qExtendsString = domain.wildcardType(string, null);
    final WildcardType qSuperCharSequence = domain.wildcardType(null, charSequence);
    final WildcardType qSuperString = domain.wildcardType(null, string);

    assertTrue(domain.contains(string, string)); // reflexive
    assertTrue(domain.contains(qExtendsString, qExtendsString)); // reflexive
    assertTrue(domain.contains(qExtendsString, string));
    assertTrue(domain.contains(qExtendsCharSequence, charSequence));
    assertTrue(domain.contains(qExtendsCharSequence, string));
    assertTrue(domain.contains(qExtendsCharSequence, qExtendsString));

    assertTrue(domain.contains(qExtendsCharSequence, t));
    assertTrue(domain.contains(qExtendsCharSequence, s));
    assertTrue(domain.contains(qExtendsString, t));
    assertTrue(domain.contains(qExtendsString, s));
    
    assertTrue(domain.contains(qSuperString, qSuperString)); // reflexive
    assertTrue(domain.contains(qSuperString, string));
    assertTrue(domain.contains(qSuperString, charSequence));
    assertTrue(domain.contains(qSuperString, qSuperCharSequence));
    assertTrue(domain.contains(qSuperCharSequence, charSequence));
    assertTrue(domain.contains(qSuperCharSequence, object));

    assertFalse(domain.contains(qSuperCharSequence, string));
    assertFalse(domain.contains(string, charSequence));
    assertFalse(domain.contains(charSequence, string));
    assertFalse(domain.contains(charSequence, qExtendsCharSequence));
    assertFalse(domain.contains(string, qExtendsString));
    assertFalse(domain.contains(string, qSuperString));
    assertFalse(domain.contains(qExtendsString, qSuperString));
    assertFalse(domain.contains(qSuperString, qExtendsString));
  }

  @Test
  final <T extends Integer, S extends Integer & Serializable, R extends S> void testTypeVariableContainsNothingExceptItself() {
    final ExecutableElement ee = domain.executableElement(domain.typeElement(this.getClass().getName()),
                                                          domain.noType(TypeKind.VOID),
                                                          "testTypeVariableContainsNothingExceptItself");
    final TypeMirror t = domain.typeParameterElement(ee, "T").asType();
    final TypeVariable s = (TypeVariable)domain.typeParameterElement(ee, "S").asType();
    final TypeMirror r = domain.typeParameterElement(ee, "R").asType();
    final DeclaredType integer = domain.declaredType("java.lang.Integer");

    assertTrue(domain.contains(t, t));
    
    assertFalse(domain.contains(t, integer));
    assertFalse(domain.contains(integer, t));
    assertFalse(domain.contains(s, s.getUpperBound()));
    assertFalse(domain.contains(s.getUpperBound(), s));
    assertFalse(domain.contains(t, s));
    assertFalse(domain.contains(s, t));
    assertFalse(domain.contains(s, r));
    assertFalse(domain.contains(r, s));
    assertFalse(domain.contains(t, r));
    assertFalse(domain.contains(r, t));
  }

}
