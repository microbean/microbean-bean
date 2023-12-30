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
package org.microbean.bean;

import java.util.Objects;

public class DefaultCreation<I> implements AutoCloseableRegistry, Creation<I> {

  private final AutoCloseableRegistry registry;

  private final BeanSelectionCriteria beanSelectionCriteria;

  public DefaultCreation(final AutoCloseableRegistry registry) {
    this(registry, null);
  }

  public DefaultCreation(final AutoCloseableRegistry registry,
                         final BeanSelectionCriteria beanSelectionCriteria) {
    super();
    this.registry = Objects.requireNonNull(registry, "registry");
    this.beanSelectionCriteria = beanSelectionCriteria;
  }

  @Override // Creation<I>
  public BeanSelectionCriteria beanSelectionCriteria() {
    return beanSelectionCriteria;
  }

  @Override // Creation<I>
  public void created(final I instance) {
    // TODO:
  }

  @Override // AutoCloseableRegistry
  public DefaultCreation<I> clone() {
    return this.clone(this.beanSelectionCriteria());
  }

  @Override // Creation<I>
  @SuppressWarnings("unchecked")
  public DefaultCreation<I> clone(final BeanSelectionCriteria beanSelectionCriteria) {
    return new DefaultCreation<>(this.registry.clone(), beanSelectionCriteria);
  }

  @Override
  public final void close() {
    this.registry.close();
  }

  @Override
  public final boolean closed() {
    return this.registry.closed();
  }

  @Override
  public final boolean register(final AutoCloseable ac) {
    return this.registry.register(ac);
  }

}
