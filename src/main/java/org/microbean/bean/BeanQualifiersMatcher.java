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

import org.microbean.assign.Matcher;

import org.microbean.qualifier.NamedAttributeMap;

import static org.microbean.assign.Qualifiers.anyAndDefaultQualifiers;
import static org.microbean.assign.Qualifiers.defaultQualifier;
import static org.microbean.assign.Qualifiers.qualifiers;

/**
 * A {@link Matcher} encapsulating <a
 * href="https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#observertypesafe_resolution">CDI-compatible bean
 * qualifier matching rules</a>.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #test(Collection, Collection)
 */
public final class BeanQualifiersMatcher implements Matcher<Collection<? extends NamedAttributeMap<?>>, Collection<? extends NamedAttributeMap<?>>> {


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link BeanQualifiersMatcher}.
   */
  public BeanQualifiersMatcher() {
    super();
  }


  /*
   * Instance methods.
   */


  /**
   * Returns {@code true} if and only if either (a) the collection of {@linkplain
   * org.microbean.assign.Qualifiers#qualifiers(Collection) qualifiers present} in {@code receiverAttributes} is
   * {@linkplain Collection#isEmpty() empty} and either the collection of {@linkplain
   * org.microbean.assign.Qualifiers#qualifiers(Collection) qualifiers present} in {@code payloadAttributes} is also
   * empty or contains the {@linkplain org.microbean.assign.Qualifiers#defaultQualifier() default qualifier}, or (b) if
   * the collection of {@linkplain org.microbean.assign.Qualifiers#qualifiers(Collection) qualifiers present} in {@code
   * payloadAttributes} {@linkplain Collection#isEmpty() is empty} or the return value of {@link
   * org.microbean.assign.Qualifiers#anyAndDefaultQualifiers()} {@linkplain Collection#containsAll(Collection) contains
   * all} of the {@linkplain org.microbean.assign.Qualifiers#qualifiers(Collection) qualifiers present} in {@code
   * receiverAttributes}, or (c) if the collection of {@linkplain org.microbean.assign.Qualifiers#qualifiers(Collection)
   * qualifiers present} in {@code payloadAttributes} {@linkplain Collection#containsAll(Collection) contains all} of
   * the {@linkplain org.microbean.assign.Qualifiers#qualifiers(Collection) qualifiers present} in {@code
   * receiverAttributes}.
   *
   * @param receiverAttributes a {@link Collection} of {@link NamedAttributeMap}s; must not be {@code null}
   *
   * @param payloadAttributes a {@link Collection} of {@link NamedAttributeMap}s; must not be {@code null}
   *
   * @return {@code true} if and only if either (a) the collection of {@linkplain
   * org.microbean.assign.Qualifiers#qualifiers(Collection) qualifiers present} in {@code receiverAttributes} is
   * {@linkplain Collection#isEmpty() empty} and either the collection of {@linkplain
   * org.microbean.assign.Qualifiers#qualifiers(Collection) qualifiers present} in {@code payloadAttributes} is also
   * empty or contains the {@linkplain org.microbean.assign.Qualifiers#defaultQualifier() default qualifier}, or (b) if
   * the collection of {@linkplain org.microbean.assign.Qualifiers#qualifiers(Collection) qualifiers present} in {@code
   * payloadAttributes} {@linkplain Collection#isEmpty() is empty} or the return value of {@link
   * org.microbean.assign.Qualifiers#anyAndDefaultQualifiers()} {@linkplain Collection#containsAll(Collection) contains
   * all} of the {@linkplain org.microbean.assign.Qualifiers#qualifiers(Collection) qualifiers present} in {@code
   * receiverAttributes}, or (c) if the collection of {@linkplain org.microbean.assign.Qualifiers#qualifiers(Collection)
   * qualifiers present} in {@code payloadAttributes} {@linkplain Collection#containsAll(Collection) contains all} of
   * the {@linkplain org.microbean.assign.Qualifiers#qualifiers(Collection) qualifiers present} in {@code
   * receiverAttributes}
   *
   * @exception NullPointerException if either {@code receiverAttributes} or {@code payloadAttributes} is {@code null}
   */
  @Override // Matcher<Collection<? extends NamedAttributeMap<?>>, Collection<? extends NamedAttributeMap<?>>>
  public final boolean test(final Collection<? extends NamedAttributeMap<?>> receiverAttributes,
                            final Collection<? extends NamedAttributeMap<?>> payloadAttributes) {
    final Collection<? extends NamedAttributeMap<?>> receiverQualifiers = qualifiers(receiverAttributes);
    Collection<? extends NamedAttributeMap<?>> payloadQualifiers = qualifiers(payloadAttributes);
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
      (payloadQualifiers.isEmpty() ? anyAndDefaultQualifiers() : payloadQualifiers).containsAll(receiverQualifiers);
  }

}
