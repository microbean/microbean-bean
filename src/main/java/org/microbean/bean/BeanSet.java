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

import java.util.Collection;
import java.util.Set;

import java.util.function.BiFunction;

import org.microbean.bean.Alternate.Resolver;

public interface BeanSet {

  // Optional operation
  public default Resolver resolver(final Resolver r) {
    throw new UnsupportedOperationException();
  }

  public Set<Bean<?>> beans();

  public Set<Bean<?>> beans(final Selector selector);

  public default Bean<?> bean(final Selector selector) {
    return this.bean(selector, Alternate.Resolver::fail);
  }

  public Bean<?> bean(final Selector selector,
                      final BiFunction<? super Selector, ? super Collection<? extends Bean<?>>, ? extends Bean<?>> op);

}
