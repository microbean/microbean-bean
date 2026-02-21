/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2025–2026 microBean™.
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

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.function.Predicate;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.microbean.construct.Domain;

import org.microbean.construct.element.SyntheticAnnotationMirror;
import org.microbean.construct.element.SyntheticAnnotationTypeElement;
import org.microbean.construct.element.SyntheticAnnotationValue;
import org.microbean.construct.element.SyntheticName;
import org.microbean.construct.element.UniversalElement;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.NULL;

import static java.lang.constant.MethodHandleDesc.ofConstructor;

import static java.util.Collections.unmodifiableList;

import static java.util.Objects.requireNonNull;

import static javax.lang.model.element.ElementKind.ANNOTATION_TYPE;
import static javax.lang.model.element.ElementKind.ENUM_CONSTANT;

/**
 * A utility class for working with <dfn>qualifiers</dfn>.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see org.microbean.assign.Qualifiers
 */
// Experimental. First foray into replacing Attributes with (synthetic) AnnotationMirrors.
public class Qualifiers implements Constable {


  /*
   * Instance fields.
   */


  private final org.microbean.assign.Qualifiers aq;

  private final AnnotationMirror anyQualifier;

  private final List<AnnotationMirror> anyQualifiers;

  private final AnnotationMirror defaultQualifier;

  private final List<AnnotationMirror> defaultQualifiers;

  private final List<AnnotationMirror> anyAndDefaultQualifiers;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link Qualifiers}.
   *
   * @param d a non-{@code null} {@link Domain}
   *
   * @param aq a non-{@code null} {@link org.microbean.assign.Qualifiers}
   *
   * @exception NullPointerException if {@code aq} is {@code null}
   *
   * @see #Qualifiers(Domain, org.microbean.assign.Qualifiers, AnnotationMirror, AnnotationMirror)
   */
  public Qualifiers(final Domain d, final org.microbean.assign.Qualifiers aq) {
    this(d, aq, null, null);
  }

  /**
   * Creates a new {@link Qualifiers}.
   *
   * @param aq a non-{@code null} {@link org.microbean.assign.Qualifiers}
   *
   * @param anyQualifier a non-{@code null} {@link AnnotationMirror} representing the <dfn>any qualifier</dfn>
   *
   * @param defaultQualifier a non-{@code null} {@link AnnotationMirror} representing the <dfn>default qualifier</dfn>
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @see org.microbean.assign.Qualifiers
   */
  public Qualifiers(final org.microbean.assign.Qualifiers aq,
                    final AnnotationMirror anyQualifier,
                    final AnnotationMirror defaultQualifier) {
    this(null, aq, requireNonNull(anyQualifier, "anyQualifier"), requireNonNull(defaultQualifier, "defaultQualifier"));
  }

  /**
   * Creates a new {@link Qualifiers}.
   *
   * @param d a {@link Domain}; may be {@code null} if both {@code anyQualifier} and {@code defaultQualifier} are
   * non-{@code null}
   *
   * @param aq a non-{@code null} {@link org.microbean.assign.Qualifiers}
   *
   * @param anyQualifier a (possibly {@code null}) {@link AnnotationMirror} representing the <dfn>any qualifier</dfn>;
   * if {@code null}, then {@code d} must be non-{@code null}
   *
   * @param defaultQualifier a (possibly {@code null}) {@link AnnotationMirror} representing the <dfn>default
   * qualifier</dfn>; if {@code null}, then {@code d} must be non-{@code null}
   *
   * @exception NullPointerException if {@code aq} is {@code null} or if {@code d} is {@code null} in certain situations
   *
   * @see org.microbean.assign.Qualifiers
   */
  public Qualifiers(final Domain d,
                    final org.microbean.assign.Qualifiers aq,
                    final AnnotationMirror anyQualifier,
                    final AnnotationMirror defaultQualifier) {
    super();
    this.aq = requireNonNull(aq, "aq");
    if (anyQualifier == null || defaultQualifier == null) {
      final List<? extends AnnotationMirror> as = d.typeElement("java.lang.annotation.Documented").getAnnotationMirrors();
      assert as.size() == 3; // @Documented, @Retention, @Target, in that order, all annotated in turn with each other
      final List<SyntheticAnnotationValue> savs = new ArrayList<>(4);
      for (final Element e : d.typeElement("java.lang.annotation.ElementType").getEnclosedElements()) {
        if (e.getKind() == ENUM_CONSTANT && e instanceof VariableElement ve) {
          final Name n = e.getSimpleName();
          if (n.contentEquals("TYPE") || n.contentEquals("METHOD") || n.contentEquals("FIELD") || n.contentEquals("PARAMETER")) {
            savs.add(new SyntheticAnnotationValue(ve));
          }
        }
      }
      final AnnotationMirror targetAnnotation =
        new SyntheticAnnotationMirror(d.typeElement("java.lang.annotation.Target"), Map.of("value", savs));
      final List<AnnotationMirror> metaAnnotations =
        List.of(aq.metaQualifier(),
                as.get(1), // @Retention
                targetAnnotation, // @Target
                as.get(0)); // @Documented
      this.anyQualifier =
        anyQualifier == null ?
        new SyntheticAnnotationMirror(new SyntheticAnnotationTypeElement(metaAnnotations, "Any")) :
        anyQualifier;
      this.defaultQualifier =
        defaultQualifier == null ?
        new SyntheticAnnotationMirror(new SyntheticAnnotationTypeElement(metaAnnotations, "Default")) :
        defaultQualifier;
    } else {
      this.anyQualifier = anyQualifier;
      this.defaultQualifier = defaultQualifier;
    }
    this.anyQualifiers = List.of(this.anyQualifier);
    this.defaultQualifiers = List.of(this.defaultQualifier);
    this.anyAndDefaultQualifiers = List.of(this.anyQualifier, this.defaultQualifier);
  }


  /*
   * Instance methods.
   */


  /**
   * Returns a non-{@code null}, determinate, immutable {@link List} housing only the {@linkplain #anyQualifier()
   * <dfn>any qualifier</dfn>} and {@link #defaultQualifier() <dfn>default qualifier</dfn>}.
   *
   * @return a non-{@code null}, determinate, immutable {@link List} housing only the {@linkplain #anyQualifier()
   * <dfn>any qualifier</dfn>} and {@linkplain #defaultQualifier() <dfn>default qualifier</dfn>}
   */
  public final List<AnnotationMirror> anyAndDefaultQualifiers() {
    return this.anyAndDefaultQualifiers;
  }

  /**
   * Returns the non-{@code null}, determinate {@link AnnotationMirror} representing the <dfn>any qualifier</dfn>.
   *
   * @return the non-{@code null}, determinate {@link AnnotationMirror} representing the <dfn>any qualifier</dfn>
   */
  public final AnnotationMirror anyQualifier() {
    return this.anyQualifier;
  }

  /**
   * Returns a non-{@code null}, determinate, immutable {@link List} housing only the {@linkplain #anyQualifier()
   * <dfn>any qualifier</dfn>}.
   *
   * @return a non-{@code null}, determinate, immutable {@link List} housing only the {@linkplain #anyQualifier()
   * <dfn>any qualifier</dfn>}
   */
  public final List<AnnotationMirror> anyQualifiers() {
    return this.anyQualifiers;
  }

  /**
   * Returns {@code true} if and only if the supplied {@link AnnotationMirror} is {@linkplain
   * org.microbean.assign.Qualifiers#sameAnnotation(AnnotationMirror, AnnotationMirror) the same} as the {@linkplain
   * #anyQualifier() <dfn>any qualifier</dfn>}.
   *
   * @param a a non-{@code null} {@link AnnotationMirror}
   *
   * @return {@code true} if and only if the supplied {@link AnnotationMirror} is {@linkplain
   * org.microbean.assign.Qualifiers#sameAnnotation(AnnotationMirror, AnnotationMirror) the same} as the {@linkplain
   * #anyQualifier() <dfn>any qualifier</dfn>}
   *
   * @exception NullPointerException if {@code a} is {@code null}
   *
   * @see #anyQualifier()
   *
   * @see org.microbean.assign.Qualifiers#sameAnnotation(AnnotationMirror, AnnotationMirror)
   */
  public final boolean anyQualifier(final AnnotationMirror a) {
    return this.aq.sameAnnotation(this.anyQualifier, a);
  }

  /**
   * Returns the non-{@code null}, determinate {@link AnnotationMirror} representing the <dfn>default qualifier</dfn>.
   *
   * @return the non-{@code null}, determinate {@link AnnotationMirror} representing the <dfn>default qualifier</dfn>
   */
  public final AnnotationMirror defaultQualifier() {
    return this.defaultQualifier;
  }

  /**
   * Returns a non-{@code null}, determinate, immutable {@link List} housing only the {@linkplain #defaultQualifier()
   * <dfn>default qualifier</dfn>}.
   *
   * @return a non-{@code null}, determinate, immutable {@link List} housing only the {@linkplain #defaultQualifier()
   * <dfn>default qualifier</dfn>}
   */
  public final List<AnnotationMirror> defaultQualifiers() {
    return this.defaultQualifiers;
  }

  /**
   * Returns {@code true} if and only if the supplied {@link AnnotationMirror} is {@linkplain
   * org.microbean.assign.Qualifiers#sameAnnotation(AnnotationMirror, AnnotationMirror) the same} as the {@linkplain
   * #defaultQualifier() <dfn>default qualifier</dfn>}.
   *
   * @param a a non-{@code null} {@link AnnotationMirror}
   *
   * @return {@code true} if and only if the supplied {@link AnnotationMirror} is {@linkplain
   * org.microbean.assign.Qualifiers#sameAnnotation(AnnotationMirror, AnnotationMirror) the same} as the {@linkplain
   * #defaultQualifier() <dfn>default qualifier</dfn>}
   *
   * @exception NullPointerException if {@code a} is {@code null}
   *
   * @see #defaultQualifier()
   *
   * @see org.microbean.assign.Qualifiers#sameAnnotation(AnnotationMirror, AnnotationMirror)
   */
  public final boolean defaultQualifier(final AnnotationMirror a) {
    return this.aq.sameAnnotation(this.defaultQualifier, a);
  }

  @Override // Constable
  public Optional<? extends ConstantDesc> describeConstable() {
    final ClassDesc cdAnnotationMirror = AnnotationMirror.class.describeConstable().orElseThrow();
    return this.aq.describeConstable()
      .flatMap(aqDesc -> (this.anyQualifier instanceof Constable c ? c.describeConstable() : Optional.<ConstantDesc>empty())
               .flatMap(anyQualifierDesc -> (this.defaultQualifier instanceof Constable c ? c.describeConstable() : Optional.<ConstantDesc>empty())
                        .map(defaultQualifierDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                                            ofConstructor(this.getClass().describeConstable().orElseThrow(),
                                                                                          Domain.class.describeConstable().orElseThrow(),
                                                                                          org.microbean.assign.Qualifiers.class.describeConstable().orElseThrow(),
                                                                                          cdAnnotationMirror,
                                                                                          cdAnnotationMirror),
                                                                            NULL,
                                                                            aqDesc,
                                                                            anyQualifierDesc,
                                                                            defaultQualifierDesc))));
  }

}
