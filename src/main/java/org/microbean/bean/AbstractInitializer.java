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

import java.util.Collection;
import java.util.Set;

public class AbstractInitializer<I> implements Initializer<I> {


  /*
   * Instance fields.
   */


  private final Set<Dependency> dependencies;


  /*
   * Constructors.
   */


  public AbstractInitializer() {
    super();
    this.dependencies = Set.of();
  }

  public AbstractInitializer(final Collection<? extends Dependency> dependencies) {
    super();
    this.dependencies = Set.copyOf(dependencies);
  }


  /*
   * Instance methods.
   */


  // TODO: c and r go together, always, so anytime you need an r you need a c.
  @Override // Initializer<I>
  public I initialize(final I i, final Creation<I> c, final ReferenceSelector r) {
    return i;
  }

  @Override // Initializer<I> (Aggregate)
  public final Set<Dependency> dependencies() {
    return this.dependencies;
  }

}
