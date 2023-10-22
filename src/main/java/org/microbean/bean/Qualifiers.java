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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.microbean.qualifier.NamedAttributeMap;

public final class Qualifiers {

  private static final NamedAttributeMap<String> QUALIFIER = new NamedAttributeMap<>("Qualifier");

  private static final List<NamedAttributeMap<String>> QUALIFIER_LIST = List.of(QUALIFIER);

  private static final NamedAttributeMap<?> ANY_QUALIFIER = new NamedAttributeMap<>("Any", Map.of(), Map.of(), QUALIFIER_LIST);

  private static final List<NamedAttributeMap<?>> ANY_QUALIFIERS = List.of(ANY_QUALIFIER);

  private static final NamedAttributeMap<?> DEFAULT_QUALIFIER = new NamedAttributeMap<>("Default", Map.of(), Map.of(), QUALIFIER_LIST);

  private static final List<NamedAttributeMap<?>> DEFAULT_QUALIFIERS = List.of(DEFAULT_QUALIFIER);

  private static final List<NamedAttributeMap<?>> ANY_AND_DEFAULT_QUALIFIERS = List.of(ANY_QUALIFIER, DEFAULT_QUALIFIER);
  
  private Qualifiers() {
    super();
  }

  public static final NamedAttributeMap<?> anyQualifier() {
    return ANY_QUALIFIER;
  }

  public static final boolean anyQualifier(final NamedAttributeMap<?> nam) {
    return ANY_QUALIFIER.equals(nam) && qualifier(nam);
  }

  public static final List<NamedAttributeMap<?>> anyQualifiers() {
    return ANY_QUALIFIERS;
  }

  public static final List<NamedAttributeMap<?>> anyAndDefaultQualifiers() {
    return ANY_AND_DEFAULT_QUALIFIERS;
  }

  public static final NamedAttributeMap<?> defaultQualifier() {
    return DEFAULT_QUALIFIER;
  }

  public static final boolean defaultQualifier(final NamedAttributeMap<?> nam) {
    return DEFAULT_QUALIFIER.equals(nam) && qualifier(nam);
  }

  public static final List<NamedAttributeMap<?>> defaultQualifiers() {
    return DEFAULT_QUALIFIERS;
  }

  public static final NamedAttributeMap<?> qualifier() {
    return QUALIFIER;
  }

  public static final boolean qualifier(final NamedAttributeMap<?> q) {
    return q != null && qualifier(q.metadata());
  }

  private static final boolean qualifier(final Iterable<? extends NamedAttributeMap<?>> mds) {
    for (final NamedAttributeMap<?> md : mds) {
      if (QUALIFIER.equals(md) && md.metadata().isEmpty() || qualifier(md)) {
        return true;
      }
    }
    return false;
  }

  public static final List<NamedAttributeMap<?>> qualifiers(final Collection<? extends NamedAttributeMap<?>> c) {
    if (c == null || c.isEmpty()) {
      return List.of();
    }
    final ArrayList<NamedAttributeMap<?>> list = new ArrayList<>(c.size());
    for (final NamedAttributeMap<?> a : c) {
      if (qualifier(a)) {
        list.add(a);
      }
    }
    list.trimToSize();
    return Collections.unmodifiableList(list);
  }

  public static final NamedAttributeMap<?> of(final NamedAttributeMap<?> nam) {
    return switch (nam) {
    case null -> throw new NullPointerException("nam");
    case NamedAttributeMap<?> q when anyQualifier(q) -> anyQualifier();
    case NamedAttributeMap<?> q when defaultQualifier(q) -> defaultQualifier();
    case NamedAttributeMap<?> q when QUALIFIER.equals(q) && q.metadata().isEmpty() -> qualifier();
    default -> nam;
    };
  }

  public static final List<NamedAttributeMap<?>> of(final List<NamedAttributeMap<?>> list) {
    return switch (list) {
    case null -> throw new NullPointerException("list");
    case List<NamedAttributeMap<?>> l when l.size() == 1 && anyQualifier(l.get(0)) -> anyQualifiers();
    case List<NamedAttributeMap<?>> l when l.size() == 1 && defaultQualifier(l.get(0)) -> defaultQualifiers();
    case List<NamedAttributeMap<?>> l when l.size() == 2 && anyQualifier(l.get(0)) && defaultQualifier(l.get(1)) -> anyAndDefaultQualifiers();
    default -> list;
    };
  }

}
