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
package org.microbean.bean2;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

final class TestMethodHandlesLookup {

  private TestMethodHandlesLookup() {
    super();
  }

  @Disabled
  @Test
  final void test() {
    final Crap crap = new Crap() {};
    crap.crap(MethodHandles.publicLookup());
    System.out.println("***");
    final Lookup lookup = MethodHandles.lookup();
    crap.crap(lookup);
    System.out.println("***");
    crap.crap(lookup.in(Crap.class));
    System.out.println("***");
  }

  private static interface Crap {

    public default void crap(final Lookup lookup) {
      System.out.println("*** lookup class: " + lookup.lookupClass());
      System.out.println("*** previous lookup class: " + lookup.previousLookupClass());
      System.out.println("*** lookup modes: " + lookup.lookupModes());
      System.out.println("*** original access? " + ((lookup.lookupModes() & Lookup.ORIGINAL) == Lookup.ORIGINAL));
      System.out.println("*** toString(): " + lookup);
    }
    
  }

}
