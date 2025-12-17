/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2025 microBean™.
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

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.microbean.assign.AttributedType;

import org.microbean.construct.Domain;

final class TestReferences {

  private TestReferences() {
    super();
  }

  @Test
  public void test() {

  }

  private static final class DummyReferences<R> implements References<R> {

    private DummyReferences() {
      super();
    }

    @Override
    public Domain domain() {
      return null;
    }

    @Override
    public Iterator<R> iterator() {
      return List.<R>of().iterator();
    }

    @Override
    public <R> R reference(final Bean<R> bean) {
      throw new IllegalArgumentException();
    }
    
    @Override
    public <S> DummyReferences<S> references(final AttributedType t) {
      return new DummyReferences<>();
    }

    @Override
    public boolean destroy(final Object r) {
      return false;
    }

    @Override
    public final int size() {
      return 0;
    }
    
  }
  
}
