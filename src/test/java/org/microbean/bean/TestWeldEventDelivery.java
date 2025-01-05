/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2024 microBean™.
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

import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.ProcessBeanAttributes;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestWeldEventDelivery {

  private SeContainer container;

  @Inject
  private Event<Integer> event;

  private boolean primitiveReceived;

  private boolean declaredReceived;

  private TestWeldEventDelivery() {
    super();
  }

  @BeforeEach
  final void resetState() {
    primitiveReceived = false;
    declaredReceived = false;
  }

  @BeforeEach
  final void startContainer() {
    final Class<?> testClass = this.getClass();
    final TestWeldEventDelivery testInstance = this;
    this.container = SeContainerInitializer.newInstance()
      .disableDiscovery()
      .addBeanClasses(testClass)
      .addExtensions(new Extension() {
          // This ceremony is a nice way, finally, to get a generic JUnit test class beanified in the proper way. I may
          // move this to a JUnit helper library someday. Left here for posterity.
          final void forceSingletonScope(@Observes final ProcessBeanAttributes<?> pba) {
            if (pba.getAnnotated().getBaseType() == testClass) {
              pba.configureBeanAttributes().scope(Singleton.class);
            }
          }
          final <T> void beanifyTestInstance(@Observes
                                             final ProcessInjectionTarget<T> pit,
                                             final BeanManager bm) {
            if (pit.getAnnotatedType().getJavaClass() == testClass) {
              final InjectionTarget<T> it = pit.getInjectionTarget();
              pit.setInjectionTarget(new InjectionTarget<>() {
                  @Override public final void dispose(final T i) { it.dispose(i); }
                  @Override public final Set<InjectionPoint> getInjectionPoints() { return it.getInjectionPoints(); }
                  @Override public final void inject(final T i, final CreationalContext<T> cc) { it.inject(i, cc); }
                  @Override public final void postConstruct(final T i) { it.postConstruct(i); }
                  @Override public final void preDestroy(final T i) { it.preDestroy(i); }
                  @Override @SuppressWarnings("unchecked") public final T produce(final CreationalContext<T> cc) { return (T)testInstance; }
                });
            }
          }
        })
      .initialize();
    // Explicit selection is required to trigger injection.
    assertSame(testInstance, this.container.select(testClass).get());
  }

  @AfterEach
  final void stopContainer() {
    this.container.close();
  }

  @Test
  final void testWeldEventDelivery() {
    this.event.fire(Integer.valueOf(42));
    assertTrue(this.primitiveReceived);
    assertTrue(this.declaredReceived);
  }

  private void primitiveEventReceived(@Observes final int event) {
    this.primitiveReceived = true;
  }

  private void wrapperEventReceived(@Observes final Integer event) {
    assertNotNull(event);
    this.declaredReceived = true;
  }

}
