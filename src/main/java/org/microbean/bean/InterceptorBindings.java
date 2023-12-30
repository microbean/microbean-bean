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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.microbean.qualifier.NamedAttributeMap;

/**
 * A utility class providing methods that work with interceptor bindings.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 */
public final class InterceptorBindings {

  private static final NamedAttributeMap<String> INTERCEPTOR_BINDING = new NamedAttributeMap<>("InterceptorBinding");

  private static final List<NamedAttributeMap<String>> INTERCEPTOR_BINDING_LIST = List.of(INTERCEPTOR_BINDING);

  private static final NamedAttributeMap<String> ANY_INTERCEPTOR_BINDING =
    new NamedAttributeMap<>("Any", Map.of(), Map.of(), INTERCEPTOR_BINDING_LIST);

  private InterceptorBindings() {
    super();
  }

  public static final NamedAttributeMap<String> anyInterceptorBinding() {
    return ANY_INTERCEPTOR_BINDING;
  }

  public static final boolean anyInterceptorBinding(final NamedAttributeMap<?> nam) {
    return ANY_INTERCEPTOR_BINDING.equals(nam) && interceptorBinding(nam);
  }

  public static final NamedAttributeMap<String> interceptorBinding() {
    return INTERCEPTOR_BINDING;
  }

  public static final boolean interceptorBinding(final NamedAttributeMap<?> a) {
    return a != null && interceptorBinding(a.metadata());
  }

  private static final boolean interceptorBinding(final Iterable<? extends NamedAttributeMap<?>> mds) {
    for (final NamedAttributeMap<?> md : mds) {
      if (md.equals(INTERCEPTOR_BINDING) && md.metadata().isEmpty() || interceptorBinding(md)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Given a {@link Collection} of {@link NamedAttributeMap}s, returns a {@link List} consisting of those {@link
   * NamedAttributeMap} instances that are {@linkplain #interceptorBinding() deemed to be interceptor bindings}.
   *
   * @param c a {@link Collection}; must not be {@code null}
   *
   * @return a {@link List} of interceptor bindings
   */
  public static final List<NamedAttributeMap<?>> interceptorBindings(final Collection<? extends NamedAttributeMap<?>> c) {
    if (c.isEmpty()) {
      return List.of();
    }
    final ArrayList<NamedAttributeMap<?>> list = new ArrayList<>(c.size());
    for (final NamedAttributeMap<?> nam : c) {
      if (interceptorBinding(nam)) {
        list.add(nam);
      }
    }
    list.trimToSize();
    return Collections.unmodifiableList(list);
  }

  public static final NamedAttributeMap<?> targetClassInterceptorBinding(final String type) {
    return new NamedAttributeMap<String>("TargetClass", Map.of("class", Objects.requireNonNull(type, "type")), Map.of(), INTERCEPTOR_BINDING_LIST);
  }

  // Is nam a TargetClass interceptor binding?
  public static final boolean targetClassInterceptorBinding(final NamedAttributeMap<?> nam) {
    return nam.name().equals("TargetClass") && interceptorBinding(nam);
  }

}
