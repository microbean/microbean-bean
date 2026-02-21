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

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.QualifiedNameable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.microbean.assign.Annotated;

import org.microbean.construct.DefaultDomain;
import org.microbean.construct.Domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestQualifiers {

  private org.microbean.assign.Qualifiers aq;
  
  private Qualifiers bq;
  
  private TestQualifiers() {
    super();
  }

  @BeforeEach
  final void setupQualifiers() {
    final Domain domain = new DefaultDomain();
    this.aq = new org.microbean.assign.Qualifiers(domain);
    this.bq = new Qualifiers(domain, aq);
  }
  
  @Test
  final void testDefaultQualifier() {
    final AnnotationMirror dq = this.bq.defaultQualifier();
    assertTrue(((QualifiedNameable)dq.getAnnotationType().asElement()).getQualifiedName().contentEquals("Default"));
    final List<? extends AnnotationMirror> mas = dq.getAnnotationType().asElement().getAnnotationMirrors();
    assertEquals(4, mas.size());
    assertTrue(mas.get(0).getAnnotationType().asElement().getSimpleName().contentEquals("Qualifier"));
    assertTrue(mas.get(1).getAnnotationType().asElement().getSimpleName().contentEquals("Retention"));
    assertTrue(mas.get(2).getAnnotationType().asElement().getSimpleName().contentEquals("Target"));
    assertTrue(mas.get(3).getAnnotationType().asElement().getSimpleName().contentEquals("Documented"));
    assertTrue(this.aq.qualifier(dq));
    assertFalse(this.aq.metaQualifier(dq));
  }
  
}
