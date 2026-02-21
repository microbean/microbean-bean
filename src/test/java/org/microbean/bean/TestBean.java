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

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.microbean.constant.Constables;

import org.microbean.construct.DefaultDomain;
import org.microbean.construct.Domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestBean {

  private Domain domain;

  private BeanTypes beanTypes;

  private Qualifiers qualifiers;

  private TestBean() {
    super();
  }

  @BeforeEach
  final void setup() {
    this.domain = new DefaultDomain();
    this.beanTypes = new BeanTypes(domain);
    this.qualifiers = new Qualifiers(domain, new org.microbean.assign.Qualifiers(domain));
  }
  
  @Disabled // see https://github.com/microbean/microbean-construct/issues/39
  @Test
  final void testConstableStuff() {
    final Factory<String> f = new Constant<>("Hello");
    final Id id =
      new Id(beanTypes.beanTypes(List.of(domain.declaredType("java.lang.String"),
                                         domain.javaLangObject().asType())),
             qualifiers.anyAndDefaultQualifiers());
    assertTrue(id instanceof Constable);
    assertFalse(id.types().describeConstable().isEmpty());
    assertFalse(id.describeConstable().isEmpty());
  }

  @Disabled // see https://github.com/microbean/microbean-construct/issues/39
  @Test
  final void testAnyAndDefaultQualifiersCanBeConstable() {
    assertFalse(Constables.describeConstable(qualifiers.anyAndDefaultQualifiers()).isEmpty());
  }

}
