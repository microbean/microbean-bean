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

import java.util.List;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Deprecated
final class TestRankedReducer {

  private TestRankedReducer() {
    super();
  }

  @Test
  final void test0() {
    final Ranked a0 = new Ranked() {};
    final Ranked a1 = new Ranked() {
        @Override
        public final boolean alternate() { return true; }
      };
    assertSame(a1, RankedReducer.of().reduce(List.of(a0, a1), null));
  }

  @Test
  final void test1() {
    final Ranked a0 = new Ranked() {
        @Override
        public final boolean alternate() { return true; }
      };
    final Ranked a1 = new Ranked() {};
    assertSame(a0, RankedReducer.of().reduce(List.of(a0, a1), null));
  }

  @Test
  final void test2() {
    final Ranked a0 = new Ranked() {};
    final Ranked a1 = new Ranked() {};
    assertNotSame(a0, a1);
    assertThrows(AmbiguousReductionException.class, () -> RankedReducer.of().reduce(List.of(a0, a1), null));
  }

  @Test
  final void test3() {
    final Ranked a0 = new Ranked() {
        @Override
        public final boolean alternate() { return true; }
      };
    final Ranked a1 = new Ranked() {
        @Override
        public final boolean alternate() { return true; }
      };
    assertNotSame(a0, a1);
    assertThrows(AmbiguousReductionException.class, () -> RankedReducer.of().reduce(List.of(a0, a1), null));
  }

  @Test
  final void test4() {
    final Ranked r0 = new Ranked() {};
    final Ranked r1 = new Ranked() {
        @Override
        public final int rank() { return 1; }
      };
    // Surprising, perhaps, but 100% correct: ranks only matter when alternate() returns true.
    assertThrows(AmbiguousReductionException.class, () -> RankedReducer.of().reduce(List.of(r0, r1), null));
  }

  @Test
  final void test5() {
    final Ranked r0 = new Ranked() {};
    final Ranked r1 = new Ranked() {};
    assertNotSame(r0, r1);
    // Surprising, perhaps, but 100% correct: ranks only matter when alternate() returns true.
    assertThrows(AmbiguousReductionException.class, () -> RankedReducer.of().reduce(List.of(r0, r1), null));
  }

  @Test
  final void test6() {
    final Ranked r0 = new Ranked() {
        @Override
        public final int rank() { return 1; }
      };
    final Ranked r1 = new Ranked() {};
    // Surprising, perhaps, but 100% correct: ranks only matter when alternate() returns true.
    assertThrows(AmbiguousReductionException.class, () -> RankedReducer.of().reduce(List.of(r0, r1), null));
  }

  
}
