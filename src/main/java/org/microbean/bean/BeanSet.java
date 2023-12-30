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
import java.util.SequencedSet;

import java.util.function.BiFunction;

public interface BeanSet {

  // Give me all the Beans
  public SequencedSet<Bean<?>> beans();

  // Give me Beans that match
  public SequencedSet<Bean<?>> beans(final BeanSelectionCriteria beanSelectionCriteria);

  // Give me the single Bean that matches, or run op on the conflicting bits
  public Bean<?> bean(final BeanSelectionCriteria beanSelectionCriteria,
                      final BiFunction<? super BeanSelectionCriteria, ? super Collection<? extends Bean<?>>, ? extends Bean<?>> op);

  // Give me the single Bean that matches, or throw an exception
  public default Bean<?> bean(final BeanSelectionCriteria beanSelectionCriteria) {
    return this.bean(beanSelectionCriteria, Alternate.Resolver::fail);
  }

}
