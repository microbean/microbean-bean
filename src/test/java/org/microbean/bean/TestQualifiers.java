/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2025 microBean™.
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

import java.util.Collection;

import org.junit.jupiter.api.Test;

import org.microbean.attributes.Attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestQualifiers {

  private static final Qualifiers qualifiers = new Qualifiers();
  
  private TestQualifiers() {
    super();
  }

  @Test
  final void testDefaultQualifierStaticMethod() {
    final Attributes dq = qualifiers.defaultQualifier();
    assertEquals("Default", dq.name());
    final Collection<? extends Attributes> md = dq.attributes(dq.name());
    assertEquals(1, md.size());
    final Attributes q = md.iterator().next();
    assertEquals("Qualifier", q.name());
    assertTrue(q.values().isEmpty());
    assertTrue(qualifiers.qualifier(dq));
    assertFalse(qualifiers.qualifier(q));
  }
  
}
