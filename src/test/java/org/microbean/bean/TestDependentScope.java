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

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

final class TestDependentScope {

  private SeContainer c;
  
  private TestDependentScope() {
    super();
  }

  @BeforeEach
  final void startContainer() {
    this.c = SeContainerInitializer.newInstance()
      .disableDiscovery()
      .addBeanClasses(SimpleBean.class,
                      ComplexBean.class)
      .initialize();
  }

  @AfterEach
  final void stopContainer() {
    this.c.close();
  }

  @Test
  final void testDependentScope() {
    this.c.select(ComplexBean.class).get();
  }

  @Dependent
  private static class SimpleBean {

    @Inject
    SimpleBean() {
      super();
    }
    
  }

  @Dependent
  private static class ComplexBean {

    @Inject
    ComplexBean(final SimpleBean bean1, final SimpleBean bean2) {
      super();
      assertNotNull(bean1);
      assertNotNull(bean2);
      assertNotSame(bean1, bean2);
    }
    
  }

}
