/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2025 microBean™.
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

import org.microbean.attributes.Attributes;

/**
 * A {@link org.microbean.assign.Qualifiers} extended to work with commonly-used {@linkplain #anyQualifier() any},
 * {@linkplain #defaultQualifier() default}, and {@linkplain #primordialQualifier() primordial} qualifiers.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see org.microbean.assign.Qualifiers
 * 
 * @see Attributes
 */
public class Qualifiers extends org.microbean.assign.Qualifiers {

  private final Attributes ANY_QUALIFIER;

  private final List<Attributes> ANY_QUALIFIERS;

  private final Attributes DEFAULT_QUALIFIER;

  private final List<Attributes> DEFAULT_QUALIFIERS;

  private final List<Attributes> ANY_AND_DEFAULT_QUALIFIERS;

  private final Attributes PRIMORDIAL_QUALIFIER;

  private final List<Attributes> PRIMORDIAL_QUALIFIERS;

  /**
   * Creates a new {@link Qualifiers}.
   */
  public Qualifiers() {
    super();
    this.ANY_QUALIFIER = Attributes.of("Any", this.qualifiers());
    this.ANY_QUALIFIERS = List.of(this.ANY_QUALIFIER);

    this.DEFAULT_QUALIFIER = Attributes.of("Default", this.qualifiers());
    this.DEFAULT_QUALIFIERS = List.of(this.DEFAULT_QUALIFIER);

    this.ANY_AND_DEFAULT_QUALIFIERS = List.of(ANY_QUALIFIER, DEFAULT_QUALIFIER);

    this.PRIMORDIAL_QUALIFIER = Attributes.of("Primordial", this.qualifiers());
    this.PRIMORDIAL_QUALIFIERS = List.of(PRIMORDIAL_QUALIFIER);
  }

  /**
   * Returns an unmodifiable {@link List} consisting solely of the unattributed <dfn>any qualifier</dfn> and the
   * <dfn>default qualifier</dfn>.
   *
   * @return an unmodifiable {@link List} consisting solely of the unattributed any qualifier and the default qualifier;
   * never {@code null}
   *
   * @see #anyQualifier()
   *
   * @see #defaultQualifier()
   */
  public List<Attributes> anyAndDefaultQualifiers() {
    return ANY_AND_DEFAULT_QUALIFIERS;
  }

  /**
   * Returns the unattributed <dfn>any qualifier</dfn>.
   *
   * @return the <dfn>any qualifier</dfn>; never {@code null}
   *
   * @see #anyQualifiers()
   */
  public Attributes anyQualifier() {
    return ANY_QUALIFIER;
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Attributes} {@linkplain Attributes#equals(Object) is equal
   * to} the unattributed {@linkplain #anyQualifier() any qualifier}.
   *
   * @param a an {@link Attributes}; must not be {@code null}
   *
   * @return {@code true} if and only if the supplied {@link Attributes} {@linkplain Attributes#equals(Object) is equal
   * to} the unattributed {@linkplain #anyQualifier() any qualifier}
   *
   * @exception NullPointerException if {@code a} is {@code null}
   */
  public boolean anyQualifier(final Attributes a) {
    return this.anyQualifier() == a || this.anyQualifier().equals(a) && this.qualifier(a);
  }

  /**
   * Returns an immutable {@link List} consisting solely of the unattributed <dfn>any qualifier</dfn>.
   *
   * @return an immutable {@link List}; never {@code null}
   *
   * @see #anyQualifier()
   */
  public List<Attributes> anyQualifiers() {
    return ANY_QUALIFIERS;
  }

  /**
   * Returns the <dfn>default qualifier</dfn>.
   *
   * @return the <dfn>default qualifier</dfn>; never {@code null}
   *
   * @see #defaultQualifiers()
   */
  public Attributes defaultQualifier() {
    return DEFAULT_QUALIFIER;
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Attributes} {@linkplain
   * Attributes#equals(Object) is equal to} the {@linkplain #defaultQualifier() default qualifier}.
   *
   * @param a an {@link Attributes}; must not be {@code null}
   *
   * @return {@code true} if and only if the supplied {@link Attributes} {@linkplain
   * Attributes#equals(Object) is equal to} the {@linkplain #defaultQualifier() default qualifier}
   *
   * @exception NullPointerException if {@code a} is {@code null}
   */
  public boolean defaultQualifier(final Attributes a) {
    return this.defaultQualifier() == a || this.defaultQualifier().equals(a) && qualifier(a);
  }

  /**
   * Returns an immutable {@link List} consisting solely of the <dfn>default qualifier</dfn>.
   *
   * @return an immutable {@link List}; never {@code null}
   *
   * @see #defaultQualifier()
   */
  public List<Attributes> defaultQualifiers() {
    return DEFAULT_QUALIFIERS;
  }

  /**
   * Returns an {@link Attributes} that is {@linkplain Attributes#equals(Object) equal to} the supplied {@link
   * Attributes}.
   *
   * <p>The returned {@link Attributes} may be the supplied {@link Attributes} or a different instance.</p>
   *
   * @param a an {@link Attributes}; must not be {@code null}
   *
   * @return an {@link Attributes} that is {@linkplain Attributes#equals(Object) equal to} the supplied {@link
   * Attributes}; never {@code null}
   *
   * @exception NullPointerException if {@code a} is {@code null}
   */
  public Attributes normalize(final Attributes a) {
    return switch (a) {
    case null -> throw new NullPointerException("a");
    case Attributes q when this.defaultQualifier(q) -> this.defaultQualifier();
    default -> super.normalize(a);
    };
  }

  /**
   * Returns an immutable {@link List} of {@link Attributes}s that is {@linkplain List#equals(Object) equal to} the
   * supplied {@link List}.
   *
   * <p>The returned {@link List} may be the supplied {@link List} or a different instance.</p>
   *
   * @param list a {@link List} of {@link Attributes}s; must not be {@code null}
   *
   * @return an immutable {@link List} of {@link Attributes}s that is {@linkplain List#equals(Object) equal to} the
   * supplied {@link List}; never {@code null}
   *
   * @exception NullPointerException if {@code list} is {@code null}
   */
  public List<Attributes> normalize(final List<Attributes> list) {
    return switch (list.size()) {
    case 0 -> List.of();
    case 1 -> list.equals(this.defaultQualifiers()) ? this.defaultQualifiers() : List.copyOf(list);
    default -> super.normalize(list);
    };
  }

  /**
   * Returns the <dfn>primordial qualifier</dfn>.
   *
   * @return the <dfn>primordial qualifier</dfn>; never {@code null}
   *
   * @see #primordialQualifiers()
   */
  public Attributes primordialQualifier() {
    return PRIMORDIAL_QUALIFIER;
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Attributes} {@linkplain
   * Attributes#equals(Object) is equal to} the {@linkplain #primordialQualifier() primordial qualifier}.
   *
   * @param a an {@link Attributes}; must not be {@code null}
   *
   * @return {@code true} if and only if the supplied {@link Attributes} {@linkplain
   * Attributes#equals(Object) is equal to} the {@linkplain #primordialQualifier() primordial qualifier}
   *
   * @exception NullPointerException if {@code a} is {@code null}
   */
  public boolean primordialQualifier(final Attributes a) {
    return this.primordialQualifier() == a || this.primordialQualifier().equals(a) && this.qualifier(a);
  }

  /**
   * Returns an immutable {@link List} consisting solely of the <dfn>primordial qualifier</dfn>.
   *
   * @return an immutable {@link List}; never {@code null}
   *
   * @see #primordialQualifier()
   */
  public List<Attributes> primordialQualifiers() {
    return PRIMORDIAL_QUALIFIERS;
  }

  /**
   * Returns an unmodifiable {@link List} consisting only of those {@link Attributes} in the supplied {@link
   * Collection} that {@linkplain #qualifier(Attributes) are qualifiers}.
   *
   * @param c a {@link Collection} of {@link Attributes}s; must not be {@code null}
   *
   * @return an unmodifiable {@link List} consisting only of those {@link Attributes}s in the supplied {@link
   * Collection} that {@linkplain #qualifier(Attributes) are qualifiers}; never {@code null}
   *
   * @exception NullPointerException if {@code c} is {@code null}
   */
  public List<Attributes> qualifiers(final Collection<? extends Attributes> c) {
    return switch (c) {
    case Collection<?> c0 when c0.isEmpty() -> List.of();
    case Collection<?> c0 when c0.equals(defaultQualifiers()) -> defaultQualifiers();
    case Collection<?> c0 when c0.equals(anyAndDefaultQualifiers()) -> anyAndDefaultQualifiers();
    default -> super.qualifiers(c);
    };
  }

}
