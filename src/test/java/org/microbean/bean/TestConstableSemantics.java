/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2022 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.bean;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import org.microbean.qualifier.Qualifier;

import org.microbean.scope.Scope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestConstableSemantics {

  private TestConstableSemantics() {
    super();
  }

  @Test
  final void testConstableSemantics() throws ReflectiveOperationException {
    final Qualifier<?, ?> scopeId = Qualifier.of("a.b.c");
    final Qualifier<?, ?> governingScopeId = Qualifier.of("d.e.f");
    Scope scope = Scope.of(scopeId, true, governingScopeId);
    assertEquals(scope, scope.describeConstable().orElseThrow().resolveConstantDesc(MethodHandles.publicLookup().in(Scope.class)));
  }

}
