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

import java.util.List;

import javax.lang.model.type.TypeKind;

import org.junit.jupiter.api.Test;

import org.microbean.lang.Lang;
import org.microbean.lang.TypeAndElementSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestSelector {

private static final TypeAndElementSource tes = Lang.typeAndElementSource();

  private static final Assignability assignability = new Assignability(tes);
  
  private TestSelector() {
    super();
  }

  @Test
  final void testSelectorStringSelectsString() {
    final BeanSelectionCriteria s = new BeanSelectionCriteria(assignability, tes.declaredType(String.class), List.of(), true);
    assertTrue(s.selects(tes.declaredType(String.class)));
  }

  @Test
  final void testSelectorStringDoesNotSelectObject() {
    final BeanSelectionCriteria s = new BeanSelectionCriteria(assignability, tes.declaredType(String.class), List.of(), true);
    assertFalse(s.selects(tes.declaredType(Object.class)));
  }

  @Test
  final void testSelectorIntSelectsInteger() {
    final BeanSelectionCriteria s = new BeanSelectionCriteria(assignability, tes.primitiveType(TypeKind.INT), List.of(), true);
    assertTrue(s.selects(tes.declaredType(Integer.class)));
  }

  @Test
  final void testSelectorObjectDoesNotSelectString() {
    final BeanSelectionCriteria s = new BeanSelectionCriteria(assignability, tes.declaredType(Object.class), List.of(), true);
    assertFalse(s.selects(tes.declaredType(String.class)));
  }

  @Test
  final void testSelectorListUnknownExtendsStringSelectsListString() {
    final BeanSelectionCriteria s =
      new BeanSelectionCriteria(assignability,
                                tes.declaredType(null,
                                                 tes.typeElement(List.class),
                                                 tes.wildcardType(tes.declaredType(String.class), null)),
                                List.of(),
                                true);
    assertTrue(s.selects(tes.declaredType(null, tes.typeElement(List.class), tes.declaredType(String.class))));
  }

}
