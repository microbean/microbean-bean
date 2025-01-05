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

import java.util.Collection;
import java.util.Objects;

import javax.lang.model.type.TypeMirror;

import org.microbean.qualifier.NamedAttributeMap;

public final class IdMatcher implements Matcher<AttributedType, Id> {

  private final BeanQualifiersMatcher qm;

  private final InterceptorBindingsMatcher ibm;

  private final BeanTypeMatcher tm;

  public IdMatcher(final BeanQualifiersMatcher qm,
                   final InterceptorBindingsMatcher ibm,
                   final BeanTypeMatcher tm) {
    super();
    this.qm = Objects.requireNonNull(qm, "qm");
    this.ibm = Objects.requireNonNull(ibm, "ibm");
    this.tm = Objects.requireNonNull(tm, "tm");
  }

  @Override // Matcher<AttributedType, Id> (BiPredicate<AttributedType, Id>)
  public final boolean test(final AttributedType t, final Id id) {
    final Collection<? extends NamedAttributeMap<?>> attributes = t.attributes();
    final Collection<? extends NamedAttributeMap<?>> idAttributes = id.attributes();
    return
      this.qm.test(attributes, idAttributes) &&
      this.ibm.test(attributes, idAttributes) &&
      this.test(t.type(), id.types());
  }

  public final boolean test(final TypeMirror type, final Iterable<? extends TypeMirror> ts) {
    for (final TypeMirror t : ts) {
      if (this.tm.test(type, t)) {
        return true;
      }
    }
    return false;
  }

}
