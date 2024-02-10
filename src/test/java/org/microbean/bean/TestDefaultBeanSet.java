/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2024 microBean™.
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
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;

import java.lang.invoke.MethodHandles;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.microbean.constant.Constables;

import org.microbean.lang.Lang;
import org.microbean.lang.TypeAndElementSource;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.privateLookupIn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.microbean.bean.Qualifiers.anyAndDefaultQualifiers;
import static org.microbean.bean.Qualifiers.defaultQualifier;

import static org.microbean.scope.Scope.SINGLETON_ID;

final class TestDefaultBeanSet {

  private static final TypeAndElementSource tes = Lang.typeAndElementSource();

  private static final Assignability assignability = new Assignability(tes);
  
  private static Bean<String> hello;
  
  private static Set<Bean<?>> beanSet;

  private DefaultBeanSet beans;
  
  private TestDefaultBeanSet() {
    super();
  }

  @BeforeAll
  static final void staticSetup() {
    hello =
      new Bean<>(new Id(List.of(Lang.declaredType(String.class), Lang.declaredType(Object.class)),
                        anyAndDefaultQualifiers(),
                        SINGLETON_ID),
                 new Singleton<>("Hello"));
    assertTrue(hello.factory() instanceof java.lang.constant.Constable);
    beanSet = Set.of(hello);
  }

  @BeforeEach
  final void setup() {
    this.beans = new DefaultBeanSet(assignability, beanSet, Map.of(), null);
  }

  @Test
  final void testTypeElementResolution() {
    System.out.println(this.getClass().getModule());
    final ModuleElement m = Lang.moduleElement("org.microbean.bean");
    assertNotNull(m);
    final TypeElement e = Lang.typeElement(m, "org.microbean.bean.Alternate.Resolver");
    assertNotNull(e);
  }
  
  @Test
  final void testBeans() {
    final Set<Bean<?>> set = this.beans.beans();

    // 5 == Bean<String>, Bean<DefaultBeanSet>, Bean<Alternate.Resolver>, Bean<Assignability>, Bean<TypeAndElementSource>
    assertEquals(5, set.size());
    hello = beans.bean(new BeanSelectionCriteria(assignability, tes.declaredType(String.class), List.of(defaultQualifier()), true)).cast();
    assertSame("Hello", hello.factory().create(null, null));
    hello = beans.bean(new BeanSelectionCriteria(assignability, tes.declaredType(Object.class), List.of(defaultQualifier()), true)).cast();
    assertSame("Hello", hello.factory().create(null, null));
  }

  @SuppressWarnings("unchecked")
  @Test
  final void testConstableStuff() throws ReflectiveOperationException {
    assertNotNull(beanSet);
    final DynamicConstantDesc<Set<Bean<?>>> cd = (DynamicConstantDesc<Set<Bean<?>>>)Constables.describeConstable(beanSet).orElseThrow(AssertionError::new);
    Set<Bean<?>> set = cd.resolveConstantDesc(privateLookupIn(ReferenceTypeList.class, lookup()));
    final class ConstantDefaultBeanSet extends DefaultBeanSet {
      public ConstantDefaultBeanSet() throws ReflectiveOperationException {
        super(assignability, (Collection<Bean<?>>)cd.resolveConstantDesc(privateLookupIn(ReferenceTypeList.class, lookup())), Map.of(), null);
      }
    }
    final DefaultBeanSet beans = new ConstantDefaultBeanSet();
    final Set<Bean<?>> resolvedSet = beans.beans();

    // 3 == Bean<String>, Bean<DefaultBeanSet>, Bean<Alternate.Resolver>
    assertEquals(1, set.size());
    hello = beans.bean(new BeanSelectionCriteria(assignability, tes.declaredType(String.class), List.of(defaultQualifier()), true)).cast();
    assertSame("Hello", hello.factory().create(null, null));
    hello = beans.bean(new BeanSelectionCriteria(assignability, tes.declaredType(Object.class), List.of(defaultQualifier()), true)).cast();
    assertSame("Hello", hello.factory().create(null, null));
  }

}
