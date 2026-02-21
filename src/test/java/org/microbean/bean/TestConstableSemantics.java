/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2026 microBean™.
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

import java.lang.constant.Constable;

import java.lang.invoke.MethodHandles;

import java.util.Iterator;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;

import javax.lang.model.type.TypeMirror;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.microbean.constant.Constables;

import org.microbean.construct.DefaultDomain;
import org.microbean.construct.Domain;

import org.microbean.construct.type.UniversalType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static java.lang.invoke.MethodHandles.lookup;

import static org.microbean.construct.element.AnnotationMirrors.sameAnnotations;

final class TestConstableSemantics {

  private static Domain domain;

  private static BeanTypes beanTypes;

  private static Qualifiers qualifiers;

  private TestConstableSemantics() {
    super();
  }

  @BeforeAll
  static final void initializeDomain() {
    domain = new DefaultDomain();
    beanTypes = new BeanTypes(domain);
    qualifiers = new Qualifiers(domain, new org.microbean.assign.Qualifiers(domain));
  }

  @Disabled // UniversalAnnotation#describeConstable() is hosed; see https://github.com/microbean/microbean-construct/issues/41
  @Test
  final void testAnyAndDefaultQualifiersList() throws ReflectiveOperationException {
    final List<? extends AnnotationMirror> list = qualifiers.anyAndDefaultQualifiers();
    @SuppressWarnings("unchecked")
    final List<? extends AnnotationMirror> thawedList = (List<? extends AnnotationMirror>)Constables.describeConstable(list)
      .orElseThrow(AssertionError::new)
      .resolveConstantDesc(MethodHandles.lookup());
    assertTrue(sameAnnotations(list, thawedList, null));
  }

  @Test
  final void testType() throws ReflectiveOperationException {
    final UniversalType t = (UniversalType)domain.typeElement("java.lang.String").asType();
    assertFalse(t.delegate() instanceof Constable);
    final UniversalType t2 = (UniversalType)t.describeConstable()
      .orElseThrow(AssertionError::new)
      .resolveConstantDesc(lookup());

    // This may surprise the reader. By Java Language Model contract, a TypeMirror may represent the "same type" as
    // another TypeMirror without being equal to it. See UniversalType#equals(Object) for more. That is the case here.
    assertNotEquals(t, t2);
    assertTrue(domain.sameType(t, t2));
  }

  @SuppressWarnings("unchecked")
  @Test
  final void testListOfTypes() throws ReflectiveOperationException {
    final List<TypeMirror> l = List.of(domain.typeElement("java.lang.String").asType(),
                                       domain.javaLangObject().asType());
    final List<TypeMirror> l2 = (List<TypeMirror>)Constables.describeConstable(l)
      .orElseThrow(AssertionError::new)
      .resolveConstantDesc(lookup());

    // This may surprise the reader. By Java Language Model contract, a TypeMirror may represent the "same type" as
    // another TypeMirror without being equal to it. See UniversalType#equals(Object) for more. That is the case here.
    assertNotEquals(l, l2);
    assertSameTypes(l, l2);
  }

  @Test
  final void testBeanTypeList() throws ReflectiveOperationException {
    final BeanTypeList btl = beanTypes.beanTypes(List.of(domain.typeElement("java.lang.String").asType(),
                                                         domain.javaLangObject().asType()));
    final BeanTypeList btl2 = (BeanTypeList)btl.describeConstable()
      .orElseThrow(AssertionError::new)
      .resolveConstantDesc(lookup());

    // This may surprise the reader. By Java Language Model contract, a TypeMirror may represent the "same type" as
    // another TypeMirror without being equal to it. See UniversalType#equals(Object) for more. That is the case here.
    assertNotEquals(btl, btl2);
    assertSameTypes(btl, btl2);
  }

  @Disabled // UniversalAnnotation#describeConstable() is hosed; see https://github.com/microbean/microbean-construct/issues/41
  @Test
  final void testId() throws ReflectiveOperationException {
    final Id id =
      new Id(beanTypes.beanTypes(List.of(domain.typeElement("java.lang.String").asType(),
                                         domain.javaLangObject().asType())),
             qualifiers.anyAndDefaultQualifiers());
    final Id id2 = (Id)Constables.describeConstable(id)
      .orElseThrow(AssertionError::new)
      .resolveConstantDesc(lookup());
    // This may surprise the reader. By Java Language Model contract, a TypeMirror may represent the "same type" as
    // another TypeMirror without being equal to it. See UniversalType#equals(Object) for more. That is the case
    // (indirectly) here.
    assertNotEquals(id, id2);
    assertSameTypes(id.types(), id2.types());
    assertTrue(sameAnnotations(id.annotations(), id2.annotations()));
  }

  @Test
  final void testConstant() throws ReflectiveOperationException {
    final Constant<String> c = new Constant<>("Hello");
    assertNotNull(c.singleton());
    @SuppressWarnings("unchecked")
    final Constant<String> c2 = (Constant<String>)Constables.describeConstable(c).orElseThrow(AssertionError::new).resolveConstantDesc(lookup());
    assertNotSame(c, c2);
    assertSame(c.singleton(), c2.singleton());
  }

  private static final <T extends Iterable<TypeMirror>> void assertSameTypes(final T a, final T b) {
    final Iterator<? extends TypeMirror> ai = a.iterator();
    final Iterator<? extends TypeMirror> bi = b.iterator();
    while (ai.hasNext()) {
      assertTrue(bi.hasNext());
      assertTrue(domain.sameType(ai.next(), bi.next()));
    }
    assertFalse(bi.hasNext());
  }

}
