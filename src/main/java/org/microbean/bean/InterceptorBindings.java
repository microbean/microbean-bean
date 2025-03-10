/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2025 microBean™.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.microbean.attributes.Attributes;
import org.microbean.attributes.StringValue;

/**
 * A utility class providing methods that work with interceptor bindings.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 */
public final class InterceptorBindings {

  private static final Attributes INTERCEPTOR_BINDING = Attributes.of("InterceptorBinding");

  private static final List<Attributes> INTERCEPTOR_BINDING_LIST = List.of(INTERCEPTOR_BINDING);

  private static final Attributes ANY_INTERCEPTOR_BINDING = Attributes.of("Any", INTERCEPTOR_BINDING_LIST);

  private InterceptorBindings() {
    super();
  }

  /**
   * Returns a {@link Attributes} representing the <dfn>any</dfn> interceptor binding.
   *
   * @return a {@link Attributes} representing the <dfn>any</dfn> interceptor binding; never {@code null}
   */
  public static final Attributes anyInterceptorBinding() {
    return ANY_INTERCEPTOR_BINDING;
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Attributes} represents the <dfn>any</dfn>
   * interceptor binding.
   *
   * @param a a {@link Attributes}; may be {@code null} in which case {@code false} will be returned
   *
   * @return {@code true} if and only if the supplied {@link Attributes} represents the <dfn>any</dfn>
   * interceptor binding
   *
   * @see #anyInterceptorBinding()
   */
  public static final boolean anyInterceptorBinding(final Attributes a) {
    return ANY_INTERCEPTOR_BINDING.equals(a) && interceptorBinding(a);
  }

  /**
   * Returns a {@link Attributes} representing the <dfn>interceptor binding</dfn> (meta-) interceptor binding.
   *
   * @return a {@link Attributes} representing the <dfn>interceptor binding</dfn> (meta-) interceptor binding;
   * never {@code null}
   */
  public static final Attributes interceptorBinding() {
    return INTERCEPTOR_BINDING;
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Attributes} is itself a {@link Attributes} that can be used
   * to designate other {@link Attributes} instances as interceptor bindings, or a {@link Attributes} so designated.
   *
   * @param a a {@link Attributes}; may be {@code null} in which case {@code false} will be returned
   *
   * @return {@code true} if and only if the supplied {@link Attributes} is itself a {@link Attributes}
   * that can be used to designate other {@link Attributes} instances as interceptor bindings, or a {@link
   * Attributes} so designated
   *
   * @see #interceptorBinding()
   */
  public static final boolean interceptorBinding(final Attributes a) {
    return a != null && interceptorBinding(a.attributes(a.name()));
  }

  private static final boolean interceptorBinding(final Iterable<? extends Attributes> mds) {
    for (final Attributes md : mds) {
      if (md.equals(INTERCEPTOR_BINDING) && md.attributes().isEmpty() || interceptorBinding(md)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Given a {@link Collection} of {@link Attributes}s, returns an immutable {@link List} consisting of those
   * {@link Attributes} instances that are {@linkplain #interceptorBinding() deemed to be interceptor bindings}.
   *
   * @param c a {@link Collection}; must not be {@code null}
   *
   * @return a {@link List} of interceptor bindings
   *
   * @exception NullPointerException if {@code c} is {@code null}
   */
  public static final List<Attributes> interceptorBindings(final Collection<? extends Attributes> c) {
    if (c.isEmpty()) {
      return List.of();
    }
    final ArrayList<Attributes> list = new ArrayList<>(c.size());
    for (final Attributes a : c) {
      if (interceptorBinding(a)) {
        list.add(a);
      }
    }
    list.trimToSize();
    return Collections.unmodifiableList(list);
  }

  /**
   * Returns a {@link Attributes} representing a <dfn>target class</dfn> interceptor binding.
   *
   * @param type the target class name; must not be {@code null}
   *
   * @return a {@link Attributes} representing a <dfn>target class</dfn> interceptor binding; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}
   */
  public static final Attributes targetClassInterceptorBinding(final String type) {
    return Attributes.of("TargetClass", Map.of("class", StringValue.of(type)), Map.of(), Map.of("TargetClass", INTERCEPTOR_BINDING_LIST));
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Attributes} is a <dfn>target class</dfn> interceptor
   * binding.
   *
   * @param a a {@link Attributes}; must not be {@code null}
   *
   * @return {@code true} if and only if the supplied {@link Attributes} is a <dfn>target class</dfn> interceptor
   * binding
   *
   * @exception NullPointerException if {@code a} is {@code null}
   */
  // Is a a TargetClass interceptor binding?
  public static final boolean targetClassInterceptorBinding(final Attributes a) {
    return a.name().equals("TargetClass") && interceptorBinding(a);
  }

}
