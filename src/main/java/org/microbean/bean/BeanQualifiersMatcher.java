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

import org.microbean.qualifier.NamedAttributeMap;

import static org.microbean.bean.Qualifiers.anyAndDefaultQualifiers;
import static org.microbean.bean.Qualifiers.defaultQualifier;
import static org.microbean.bean.Qualifiers.qualifiers;

public final class BeanQualifiersMatcher implements Matcher<Collection<? extends NamedAttributeMap<?>>, Collection<? extends NamedAttributeMap<?>>> {

  public BeanQualifiersMatcher() {
    super();
  }

  @Override // Matcher<Collection<? extends NamedAttributeMap<?>>, Collection<? extends NamedAttributeMap<?>>>
  public final boolean test(final Collection<? extends NamedAttributeMap<?>> receiverAttributes,
                            final Collection<? extends NamedAttributeMap<?>> payloadAttributes) {
    final Collection<? extends NamedAttributeMap<?>> receiverQualifiers = qualifiers(receiverAttributes);
    final Collection<? extends NamedAttributeMap<?>> payloadQualifiers = qualifiers(payloadAttributes);
    // https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#performing_typesafe_resolution
    // "A bean is assignable to a given injection point if...the bean has all the required qualifiers. If no required
    // qualifiers were explicitly specified, the container assumes the required qualifier @Default."
    //
    // https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#builtin_qualifiers
    // "Every bean has the built-in qualifier @Any, even if it does not explicitly declare this qualifier. If a bean
    // does not explicitly declare a qualifier other than @Named or @Any, the bean has exactly one additional qualifier,
    // of type @Default. This is called the default qualifier."
    //
    // https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#injection_point_default_qualifier
    // "If an injection point declares no qualifier, the injection point has exactly one qualifier, the default
    // qualifier @Default."
    return
      receiverQualifiers.isEmpty() ? payloadQualifiers.isEmpty() || payloadQualifiers.contains(defaultQualifier()) :
      payloadQualifiers.isEmpty() ? anyAndDefaultQualifiers().containsAll(receiverQualifiers) :
      payloadQualifiers.containsAll(receiverQualifiers);
  }

}
