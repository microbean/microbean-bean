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
import java.util.List;

import javax.lang.model.AnnotatedConstruct;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import org.microbean.assign.Annotated;
import org.microbean.assign.Matcher;

import static java.util.Objects.requireNonNull;

/**
 * A {@link Matcher} that tests an {@link Id} to see if it <dfn>matches</dfn> an {@link AnnotatedConstruct}.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #test(Annotated, Id)
 *
 * @see BeanQualifiersMatcher
 *
 * @see BeanTypeMatcher
 *
 * @see Matcher
 *
 * @see Id
 *
 * @see Beans#typesafeFilteringSelectable(Collection, Matcher)
 */
// Convenience. Nothing actually refers to this class. See Selectables#typesafeFiltering(Collection, Matcher)
public final class IdMatcher implements Matcher<Annotated<? extends AnnotatedConstruct>, Id> {

  private final BeanTypeMatcher tm;

  private final BeanQualifiersMatcher qm;

  private final Matcher<? super Annotated<? extends AnnotatedConstruct>, ? super Id> other;

  /**
   * Creates a new {@link IdMatcher}.
   *
   * @param tm a {@link BeanTypeMatcher}; must not be {@code null}
   *
   * @param qm a {@link BeanQualifiersMatcher}; must not be {@code null}
   *
   * @exception NullPointerException} if {@code tm} or {@code qm} is {@code null}
   *
   * @see #IdMatcher(BeanTypeMatcher, BeanQualifiersMatcher, Matcher)
   */
  public IdMatcher(final BeanTypeMatcher tm, final BeanQualifiersMatcher qm) {
    this(tm, qm, null);
  }

  /**
   * Creates a new {@link IdMatcher}.
   *
   * @param tm a {@link BeanTypeMatcher}; must not be {@code null}
   *
   * @param qm a {@link BeanQualifiersMatcher}; must not be {@code null}
   *
   * @param other a supplementary {@link Matcher}; may be {@code null}
   *
   * @exception NullPointerException} if {@code tm} or {@code qm} is {@code null}
   */
  public IdMatcher(final BeanTypeMatcher tm,
                   final BeanQualifiersMatcher qm,
                   final Matcher<? super Annotated<? extends AnnotatedConstruct>, ? super Id> other) {
    super();
    this.tm = requireNonNull(tm, "tm");
    this.qm = requireNonNull(qm, "qm");
    this.other = other == null ? (ac, id) -> true : other;
  }

  /*
   * Tests the supplied {@link Id} to see if it <dfn>matches</dfn> the supplied {@link AnnotatedConstruct} and returns the
   * result.
   *
   * @param ac an {@link AnnotatedConstruct}; must not be {@code null}
   *
   * @param id an {@link Id}; must not be {@code null}
   *
   * @return {@code true} if {@code id} matches {@code ac}; {@code false} otherwise
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @see #test(Annotated, Id)
   *
   * @see Annotated#of(AnnotatedConstruct, Predicate)
   *
   * @see BeanQualifiersMatcher#test(Collection, Collection)
   *
   * @see #test(TypeMirror, Iterable)
   *
   * @see BeanTypeMatcher#test(TypeMirror, TypeMirror)
   */
  // @Deprecated(forRemoval = true) // Annotated.of(AnnotatedConstruct) exists
  // public final boolean test(final AnnotatedConstruct ac, final Id id) {
  //   return this.test(Annotated.of(ac, this.qm.annotationElementInclusionPredicate()), id);
  // }

  /**
   * Tests the supplied {@link Id} to see if it <dfn>matches</dfn> the supplied {@link Annotated Annotated&lt;? extends
   * AnnotatedConstruct&gt;} and returns the result.
   *
   * @param aac an {@link Annotated Annotated&lt;? extends AnnotatedConstruct&gt;}; must not be {@code null}
   *
   * @param id an {@link Id}; must not be {@code null}
   *
   * @return {@code true} if {@code id} matches {@code aac}; {@code false} otherwise
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @see Annotated#of(AnnotatedConstruct, Predicate)
   *
   * @see BeanQualifiersMatcher#test(Collection, Collection)
   *
   * @see #test(TypeMirror, Iterable)
   *
   * @see BeanTypeMatcher#test(TypeMirror, TypeMirror)
   */
  @Override // Matcher<Annotated<? extends AnnotatedConstruct>, Id>
  public final boolean test(final Annotated<? extends AnnotatedConstruct> aac, final Id id) {
    final AnnotatedConstruct ac = aac.annotated();
    return
      this.test(ac instanceof Element e ? e.asType() : (TypeMirror)ac, id.types()) &&
      this.qm.test(aac.annotations(), id.annotations()) &&
      this.other.test(aac, id);
  }

  /**
   * Tests the supplied {@link Iterable} of {@link TypeMirror}s to see if at least one {@link TypeMirror} it yields
   * <dfn>matches</dfn> the supplied {@link TypeMirror} and returns the result.
   *
   * <p>A {@link TypeMirror} <em>t</em> from the supplied {@link Iterable} <dfn>matches</dfn> the supplied {@code type}
   * argument if an invocation of the {@link BeanTypeMatcher#test(TypeMirror, TypeMirror)} method invoked on the
   * {@linkplain #IdMatcher(BeanTypeMatcher, BeanQualifiersMatcher, InterceptorBindingsMatcher)
   * <code>BeanTypeManager</code> supplied at construction time} supplied with {@code type} and <em>t</em> returns
   * {@code true}.</p>
   *
   * @param type a {@link TypeMirror} to test against; must not be {@code null}
   *
   * @param candidatess an {@link Iterable} of {@link TypeMirror}s; must not be {@code null}
   *
   * @return {@code true} if at least one {@link TypeMirror} from {@code candidates} matches {@code type}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @see BeanTypeMatcher#test(TypeMirror, TypeMirror)
   */
  private final boolean test(final TypeMirror type, final Iterable<? extends TypeMirror> candidates) {
    requireNonNull(type, "type");
    for (final TypeMirror candidate : candidates) {
      if (this.tm.test(type, candidate)) {
        return true;
      }
    }
    return false;
  }

}
