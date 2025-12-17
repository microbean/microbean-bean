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

import javax.lang.model.type.TypeMirror;

import org.microbean.assign.AttributedType;
import org.microbean.assign.Matcher;

import org.microbean.attributes.Attributes;

import static java.util.Objects.requireNonNull;

/**
 * A {@link Matcher} that tests an {@link Id} to see if it matches an {@link AttributedType}.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #test(AttributedType, Id)
 *
 * @see BeanQualifiersMatcher
 *
 * @see BeanTypeMatcher
 *
 * @see Matcher
 *
 * @see Id
 *
 * @see Selectables#typesafeFiltering(Collection, Matcher)
 */
// Convenience. Nothing actually refers to this class. See Selectables#typesafeFiltering(Collection, Matcher)
public final class IdMatcher implements Matcher<AttributedType, Id> {

  private final BeanTypeMatcher tm;

  private final BeanQualifiersMatcher qm;

  private final Matcher<AttributedType, Id> other;

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
                   final Matcher<AttributedType, Id> other) {
    super();
    this.tm = requireNonNull(tm, "tm");
    this.qm = requireNonNull(qm, "qm");
    this.other = other == null ? (t, i) -> true : other;
  }

  /**
   * Tests the supplied {@link Id} to see if it <dfn>matches</dfn> the supplied {@link AttributedType} and returns the
   * result.
   *
   * <p>An {@link Id} <dfn>matches</dfn> an {@link AttributedType} if and only if all of the following are {@code
   * true}:</p>
   *
   * <ol>
   *
   * <li>An invocation of the {@link BeanQualifiersMatcher#test(Collection, Collection)} method on the {@link
   * BeanQualifiersMatcher} supplied at {@linkplain #IdMatcher(BeanTypeMatcher, BeanQualifiersMatcher,
   * Matcher) construction time} supplied with the supplied {@linkplain AttributedType#attributes()
   * <code>AttributedType</code>'s attributes} and the supplied {@linkplain Id#attributes() <code>Id</code>'s
   * attributes} returns {@code true}</li>
   *
   * <li>An invocation of the {@link Matcher#test(Object, Object)} method on the supplementary {@link Matcher} supplied
   * at {@linkplain #IdMatcher(BeanTypeMatcher, BeanQualifiersMatcher, Matcher) construction time} supplied with the
   * supplied {@linkplain AttributedType} and the supplied {@link Id} returns {@code true}</li>
   *
   * <li>An invocation of this {@link IdMatcher}'s {@link #test(TypeMirror, Iterable)} method supplied with the supplied
   * {@linkplain AttributedType#type() <code>AttributedType</code>'s type} and the supplied {@linkplain Id#attributes()
   * <code>Id</code>'s attributes} returns {@code true}</li>
   *
   * </ol>
   *
   * @param t an {@link AttributedType}; must not be {@code null}
   *
   * @param id an {@link Id}; must not be {@code null}
   *
   * @return {@code true} if {@code id} matches {@code t}; {@code false} otherwise
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @see BeanQualifiersMatcher#test(Collection, Collection)
   *
   * @see #test(TypeMirror, Iterable)
   *
   * @see BeanTypeMatcher#test(TypeMirror, TypeMirror)
   */
  @Override // Matcher<AttributedType, Id> (BiPredicate<AttributedType, Id>)
  public final boolean test(final AttributedType t, final Id id) {
    return
      this.test(t.type(), id.types()) &&
      this.qm.test(t.attributes(), id.attributes()) &&
      this.other.test(t, id);
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
   * @param ts an {@link Iterable} of {@link TypeMirror}s; must not be {@code null}
   *
   * @return {@code true} if at least one {@link TypeMirror} from {@code ts} matches {@code t}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @see BeanTypeMatcher#test(TypeMirror, TypeMirror)
   */
  private final boolean test(final TypeMirror type, final Iterable<? extends TypeMirror> ts) {
    requireNonNull(type, "type");
    for (final TypeMirror t : ts) {
      if (this.tm.test(type, t)) {
        return true;
      }
    }
    return false;
  }

}
