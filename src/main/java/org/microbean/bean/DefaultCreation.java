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

public class DefaultCreation<I> implements Creation<I> {

  private final Selector selector;

  private final References<Object> references;

  private final Destruction destruction;

  public DefaultCreation(final References<Object> references, final Selector selector, final Destruction destruction) {
    super();
    this.selector = selector;
    this.references = Objects.requireNonNull(references, "references");
    this.destruction = Objects.requireNonNull(destruction, "destruction");
  }

  @Override // Creation<I>
  @SuppressWarnings("unchecked")
  public DefaultCreation<I> clone() {
    try {
      return (DefaultCreation<I>)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  @Override // ReferencesProvider
  public final References<Object> references() {
    return this.references;
  }

  @Override // Creation<I>
  public final Selector selector() {
    return this.selector;
  }

  @Override // Creation<I>
  public final Destruction destruction() {
    return this.destruction;
  }

}
