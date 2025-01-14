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
import java.util.List;

import org.microbean.assign.Matcher;

import org.microbean.qualifier.NamedAttributeMap;

import static org.microbean.bean.InterceptorBindings.anyInterceptorBinding;
import static org.microbean.bean.InterceptorBindings.interceptorBindings;

/**
 * A {@link Matcher} encapsulating <a
 * href="https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#interceptors">CDI-compatible interceptor binding
 * matching rules</a>.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #test(Collection, Collection)
 */
public final class InterceptorBindingsMatcher implements Matcher<Collection<? extends NamedAttributeMap<?>>, Collection<? extends NamedAttributeMap<?>>> {

  /**
   * Creates a new {@link InterceptorBindingsMatcher}.
   */
  public InterceptorBindingsMatcher() {
    super();
  }

  /**
   * Returns {@code true} if and only if either (a) both the collection of {@linkplain
   * InterceptorBindings#interceptorBindings(Collection) interceptor bindings present} in {@code receiverAttributes} and
   * the collection of {@linkplain InterceptorBindings#interceptorBindings(Collection) interceptor bindings present} in
   * {@code payloadAttributes} are {@linkplain Collection#isEmpty() empty}, or (b) if the collection of {@linkplain
   * InterceptorBindings#interceptorBindings(Collection) interceptor bindings present} in {@code payloadAttributes} has
   * only one element and that element {@linkplain InterceptorBindings#anyInterceptorBinding(NamedAttributeMap) is the
   * <dfn>any</dfn> interceptor binding}, or (c) the sizes of the collection of {@linkplain
   * InterceptorBindings#interceptorBindings(Collection) interceptor bindings present} in {@code receiverAttributes} and
   * the collection of {@linkplain InterceptorBindings#interceptorBindings(Collection) interceptor bindings present} in
   * {@code payloadAttributes} are the same and the collection of {@linkplain
   * InterceptorBindings#interceptorBindings(Collection) interceptor bindings present} in {@code receiverAttributes}
   * {@linkplain Collection#containsAll(Collection) contains all} the collection of {@linkplain
   * InterceptorBindings#interceptorBindings(Collection) interceptor bindings present} in {@code payloadAttributes} and
   * the collection of {@linkplain InterceptorBindings#interceptorBindings(Collection) interceptor bindings present} in
   * {@code payloadAttributes} {@linkplain Collection#containsAll(Collection) contains all} the collection of
   * {@linkplain InterceptorBindings#interceptorBindings(Collection) interceptor bindings present} in {@code
   * receiverAttributes}.
   *
   * @param receiverAttributes a {@link Collection} of {@link NamedAttributeMap}s; must not be {@code null}
   *
   * @param payloadAttributes a {@link Collection} of {@link NamedAttributeMap}s; must not be {@code null}
   *
   * @return {@code true} if and only if either (a) both the collection of {@linkplain
   * InterceptorBindings#interceptorBindings(Collection) interceptor bindings present} in {@code receiverAttributes} and
   * the collection of {@linkplain InterceptorBindings#interceptorBindings(Collection) interceptor bindings present} in
   * {@code payloadAttributes} are {@linkplain Collection#isEmpty() empty}, or (b) if the collection of {@linkplain
   * InterceptorBindings#interceptorBindings(Collection) interceptor bindings present} in {@code payloadAttributes} has
   * only one element and that element {@linkplain InterceptorBindings#anyInterceptorBinding(NamedAttributeMap) is the
   * <dfn>any</dfn> interceptor binding}, or (c) the sizes of the collection of {@linkplain
   * InterceptorBindings#interceptorBindings(Collection) interceptor bindings present} in {@code receiverAttributes} and
   * the collection of {@linkplain InterceptorBindings#interceptorBindings(Collection) interceptor bindings present} in
   * {@code payloadAttributes} are the same and the collection of {@linkplain
   * InterceptorBindings#interceptorBindings(Collection) interceptor bindings present} in {@code receiverAttributes}
   * {@linkplain Collection#containsAll(Collection) contains all} the collection of {@linkplain
   * InterceptorBindings#interceptorBindings(Collection) interceptor bindings present} in {@code payloadAttributes} and
   * the collection of {@linkplain InterceptorBindings#interceptorBindings(Collection) interceptor bindings present} in
   * {@code payloadAttributes} {@linkplain Collection#containsAll(Collection) contains all} the collection of
   * {@linkplain InterceptorBindings#interceptorBindings(Collection) interceptor bindings present} in {@code
   * receiverAttributes}
   *
   * @exception NullPointerException if either {@code receiverAttributes} or {@code payloadAttributes} is {@code null}
   */
  @Override // Matcher<Collection<? extends NamedAttributeMap<?>>, Collection<? extends NamedAttributeMap<?>>>
  public final boolean test(final Collection<? extends NamedAttributeMap<?>> receiverAttributes,
                            final Collection<? extends NamedAttributeMap<?>> payloadAttributes) {
    final List<? extends NamedAttributeMap<?>> payloadBindings = interceptorBindings(payloadAttributes);
    if (payloadBindings.isEmpty()) {
      return interceptorBindings(receiverAttributes).isEmpty();
    } else if (payloadBindings.size() == 1 && anyInterceptorBinding(payloadBindings.get(0))) {
      return true;
    }
    final List<? extends NamedAttributeMap<?>> receiverBindings = interceptorBindings(receiverAttributes);
    return
      receiverBindings.size() == payloadBindings.size() &&
      receiverBindings.containsAll(payloadBindings) &&
      payloadBindings.containsAll(receiverBindings);
  }

}
