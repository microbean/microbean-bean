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

import java.util.Collection;

import org.junit.jupiter.api.Test;

import org.microbean.qualifier.NamedAttributeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.microbean.bean.Qualifiers.defaultQualifier;

final class TestQualifiers {

  private TestQualifiers() {
    super();
  }

  @Test
  final void testDefaultQualifierStaticMethod() {
    final NamedAttributeMap<?> dq = defaultQualifier();
    assertEquals("Default", dq.name());
    final Collection<? extends NamedAttributeMap<?>> md = dq.metadata();
    assertEquals(1, md.size());
    final NamedAttributeMap<?> q = md.iterator().next();
    assertEquals("Qualifier", q.name());
    assertTrue(q.isEmpty());
    assertTrue(Qualifiers.Kind.QUALIFIER.describes(dq));
    assertFalse(Qualifiers.Kind.QUALIFIER.describes(q));
  }
  
}
