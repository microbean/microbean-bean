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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;

@ExtendWith(ExperimentalPreConstructCallback.class)
@ExtendWith(ExperimentalBeforeAllCallback.class)
@ExtendWith(ExperimentalTestInstancePostProcessor.class)
@ExtendWith(ExperimentalBeforeEachCallback.class)
@TestInstance(PER_METHOD)
final class TestJUnitExtensionsPerMethod {

  private TestJUnitExtensionsPerMethod() {
    super();
    System.out.println("*** per method constructor");
  }

  @Test
  final void frob() {
    System.out.println("*** frob");
  }

  @Test
  final void blat() {
    System.out.println("*** blat");
  }
  
}
