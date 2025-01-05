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

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.TestInstance.Lifecycle;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.BeforeEachCallback;

public class ExperimentalBeforeEachCallback implements BeforeEachCallback {

  private final AtomicBoolean initialized;
  
  public ExperimentalBeforeEachCallback() {
    super();
    this.initialized = new AtomicBoolean();
  }

  @Override
  public final void beforeEach(final ExtensionContext extensionContext) {
    final Lifecycle l = extensionContext.getTestInstanceLifecycle().orElseThrow();
    switch (l) {
    case PER_METHOD:
      apply(extensionContext);
      break;
    case PER_CLASS:
      if (initialized.compareAndSet(false, true)) {
        apply(extensionContext);
      }
      break;
    default:
      throw new AssertionError();
    }
  }

  protected void apply(final ExtensionContext extensionContext) {
    System.out.println("*** apply called");
  }
  
}
