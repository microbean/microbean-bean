/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2024–2026 microBean™.
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

import java.util.function.Predicate;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

import org.microbean.assign.Matcher;

import static java.util.Objects.requireNonNull;

import static org.microbean.construct.element.AnnotationMirrors.sameAnnotation;
import static org.microbean.construct.element.AnnotationMirrors.contains;
import static org.microbean.construct.element.AnnotationMirrors.containsAll;

/**
 * A {@link Matcher} encapsulating <a
 * href="https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.0#observertypesafe_resolution">CDI-compatible bean
 * qualifier matching rules</a>.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #test(Collection, Collection)
 */
public class BeanQualifiersMatcher
  implements Matcher<Collection<? extends AnnotationMirror>, Collection<? extends AnnotationMirror>> {


  /*
   * Instance fields.
   */


  private final org.microbean.assign.Qualifiers aq;

  private final Qualifiers bq;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link BeanQualifiersMatcher}.
   *
   * @param aq a (@link org.microbean.assign.Qualifiers}; must not be {@code null}
   *
   * @param bq a {@link Qualifiers}; must not be {@code null}
   *
   * @exception NullPointerException if any argument is {@code null}
   */
  public BeanQualifiersMatcher(final org.microbean.assign.Qualifiers aq,
                               final Qualifiers bq) {
    super();
    this.aq = requireNonNull(aq, "aq");
    this.bq = requireNonNull(bq, "bq");
  }


  /*
   * Instance methods.
   */


  /*
   * Returns the non-{@code null}, determinate {@link Predicate} {@linkplain
   * org.microbean.assign.Qualifiers#Qualifiers(Domain, AnnotationMirror, Predicate) supplied (indirectly) at
   * construction time} that returns {@code true} if a given {@link ExecutableElement}, representing an annotation
   * element, is to be included in any comparison operation.
   *
   * @return the non-{@code null}, determinate {@link Predicate} {@linkplain
   * org.microbean.assign.Qualifiers#Qualifiers(Domain, AnnotationMirror, Predicate) supplied (indirectly) at
   * construction time} that returns {@code true} if a given {@link ExecutableElement}, representing an annotation
   * element, is to be included in any comparison operation
   *
   * @see #BeanQualifiersMatcher(Qualifiers)
   *
   * @see org.microbean.assign.Qualifiers#annotationElementInclusionPredicate()
   */
  // public final Predicate<? super ExecutableElement> annotationElementInclusionPredicate() {
  //   return this.qualifiers.annotationElementInclusionPredicate();
  // }

  @Override // Matcher<Collection<? extends AnnotationMirror>, Collection<? extends AnnotationMirror>>
  public final boolean test(final Collection<? extends AnnotationMirror> receiverAnnotations,
                            final Collection<? extends AnnotationMirror> payloadAnnotations) {
    final Collection<? extends AnnotationMirror> receiverQualifiers = this.aq.qualifiers(receiverAnnotations);
    final Collection<? extends AnnotationMirror> payloadQualifiers = this.aq.qualifiers(payloadAnnotations);
    // https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#performing_typesafe_resolution "A bean is
    // assignable to a given injection point if...the bean [payload] has all the required [receiver] qualifiers. If no
    // required qualifiers were explicitly specified, the container assumes the required qualifier @Default."
    //
    // https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#builtin_qualifiers "Every bean has the built-in
    // qualifier @Any, even if it does not explicitly declare this qualifier. If a bean does not explicitly declare a
    // qualifier other than @Named or @Any, the bean has exactly one additional qualifier, of type @Default. This is
    // called the default qualifier."
    //
    // https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#injection_point_default_qualifier
    // "If an injection point declares no qualifier, the injection point has exactly one qualifier, the default
    // qualifier @Default."
    return
      receiverQualifiers.isEmpty() ?
      payloadQualifiers.isEmpty() || this.aq.contains(payloadQualifiers, this.bq.defaultQualifier()) :
      this.aq.containsAll(payloadQualifiers.isEmpty() ? this.bq.anyAndDefaultQualifiers() : payloadQualifiers,
                          receiverQualifiers);
  }

}
