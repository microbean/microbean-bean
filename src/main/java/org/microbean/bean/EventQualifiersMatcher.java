/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2024–2025 microBean™.
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

import static org.microbean.bean.Qualifiers.anyQualifier;
import static org.microbean.bean.Qualifiers.defaultQualifier;
import static org.microbean.bean.Qualifiers.defaultQualifiers;
import static org.microbean.bean.Qualifiers.qualifiers;

public final class EventQualifiersMatcher implements Matcher<Collection<? extends NamedAttributeMap<?>>, Collection<? extends NamedAttributeMap<?>>> {

  public EventQualifiersMatcher() {
    super();
  }

  @Override // Matcher<Collection<? extends NamedAttributeMap<?>>, Collection<? extends NamedAttributeMap<?>>>
  public final boolean test(final Collection<? extends NamedAttributeMap<?>> receiverAttributes,
                            final Collection<? extends NamedAttributeMap<?>> payloadAttributes) {
    // "An event is delivered to an observer method if...the observer method has no event qualifiers or has a subset of
    // the event qualifiers."
    final Collection<? extends NamedAttributeMap<?>> receiverQualifiers = qualifiers(receiverAttributes);
    return receiverQualifiers.isEmpty() || qualifiers(payloadAttributes).containsAll(receiverQualifiers);
  }

}
