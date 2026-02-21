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

import java.lang.System.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.AnnotatedConstruct;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.microbean.assign.Annotated;
import org.microbean.assign.Matcher;
import org.microbean.assign.Selectable;

import org.microbean.construct.Domain;

import org.microbean.construct.element.SyntheticAnnotationMirror;
import org.microbean.construct.element.SyntheticAnnotationTypeElement;
import org.microbean.construct.element.SyntheticAnnotationTypeElement.SyntheticAnnotationElement;
import org.microbean.construct.element.SyntheticAnnotationValue;
import org.microbean.construct.element.SyntheticName;

import static java.lang.System.getLogger;

import static java.util.Collections.sort;
import static java.util.Objects.requireNonNull;

import static javax.lang.model.type.TypeKind.BOOLEAN;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.lang.model.type.TypeKind.INT;

import static org.microbean.construct.element.AnnotationMirrors.sameAnnotation;
import static org.microbean.construct.element.AnnotationMirrors.get;

/**
 * Utility methods for working with {@link Bean}s.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #normalize(Collection)
 *
 * @see Bean
 */
public final class Beans {


  /*
   * Static fields.
   */


  private static final Logger LOGGER = getLogger(Beans.class.getName());


  /*
   * Instance fields.
   */


  private final AnnotationMirror alternate;

  private final AnnotationMirror rank;

  private final Comparator<Bean<?>> byAlternateThenByRankComparator;


  /*
   * Constructors.
   */

  /**
   * Creates a new {@link Beans}.
   *
   * @param d a non-{@code null} {@link Domain}
   *
   * @see #Beans(Domain, AnnotationMirror, AnnotationMirror)
   */
  public Beans(final Domain d) {
    this(d, null, null);
  }
  
  /**
   * Creates a new {@link Beans}.
   *
   * @param alternate a non-{@code null} {@link AnnotationMirror} that will designate a {@link Bean} as being an
   * alternate
   *
   * @param rank a non-{@code null} {@link AnnotationMirror} that can be examined to determine the <dfn>rank</dfn> of a {@link Bean}
   * that has been deemed to be an alternate
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @see #Beans(Domain, AnnotationMirror, AnnotationMirror)
   */
  public Beans(final AnnotationMirror alternate, final AnnotationMirror rank) {
    this(null, alternate, rank);
  }
  
  /**
   * Creates a new {@link Beans}.
   *
   * @param d a non-{@code null} {@link Domain}
   *
   * @param alternate an {@link AnnotationMirror} that will designate a {@link Bean} as being an alternate; may be
   * {@code null}; if non-{@code null} must have a {@code boolean}-typed {@code value} element
   *
   * @param rank an {@link AnnotationMirror} that can be examined to determine the <dfn>rank</dfn> of a {@link Bean}
   * that has been deemed to be an alternate; may be {@code null}; if non-{@code null} must have an {@code int}-typed
   * {@code value} element
   */
  public Beans(final Domain d,
               final AnnotationMirror alternate,
               final AnnotationMirror rank) {
    super();
    if (alternate == null) {
      if (d == null) {
        this.alternate = null;
      } else {
        this.alternate =
          new SyntheticAnnotationMirror(new SyntheticAnnotationTypeElement(List.of(),
                                                                           new SyntheticName("Alternate"),
                                                                           List.of(new SyntheticAnnotationElement(d.primitiveType(BOOLEAN),
                                                                                                                  new SyntheticName("value"),
                                                                                                                  new SyntheticAnnotationValue(false)))));
      }
    } else {
      this.alternate = alternate;
    }
    if (rank == null) {
      if (d == null || this.alternate == null) {
        this.rank = null;
      } else {
        this.rank =
          new SyntheticAnnotationMirror(new SyntheticAnnotationTypeElement(List.of(),
                                                                           new SyntheticName("Rank"),
                                                                           List.of(new SyntheticAnnotationElement(d.primitiveType(INT),
                                                                                                                  new SyntheticName("value"),
                                                                                                                  new SyntheticAnnotationValue(0)))));
      }
    } else {
      this.rank = rank;
    }
    this.byAlternateThenByRankComparator =
      Comparator.<Bean<?>, Boolean>comparing(this::alternate).reversed()
      .thenComparing(Comparator.<Bean<?>, Integer>comparing(this::rank).reversed());
  }

  /**
   * Returns a non-{@code null}, determinate, immutable {@link List} containing a subset of {@linkplain
   * Bean#equals(Object) distinct elements} contained in the supplied {@link Collection}, sorted in a deliberately
   * unspecified fashion.
   *
   * @param beans a {@link Collection} of {@link Bean}s; must not be {@code null}
   *
   * @return a non-{@code null}, determinate, immutable {@link List}
   *
   * @exception NullPointerException if {@code beans} is {@code null}
   *
   * @see Bean#equals(Object)
   */
  public final List<Bean<?>> normalize(final Collection<? extends Bean<?>> beans) {
    return beans.isEmpty() ? List.of() : switch (beans.size()) {
    case 1 -> List.copyOf(beans);
    default -> {
      final List<Bean<?>> newBeans = new ArrayList<>(beans instanceof Set<? extends Bean<?>> s ? s : new HashSet<>(beans));
      yield newBeans.isEmpty() ? List.of() : switch (newBeans.size()) {
      case 1 -> List.of(newBeans.get(0));
      default -> {
        sort(newBeans, this.byAlternateThenByRankComparator);
        yield List.copyOf(newBeans);
      }
      };
    }
    };
  }
  
  /**
   * {@linkplain #normalize(Collection) Normalizes} the supplied {@link Collection} of {@link Bean}s and returns a
   * {@link Selectable} suitable for it and the supplied {@link Matcher}, using the {@link
   * org.microbean.assign.Selectables#filtering(Collection, BiPredicate)} method internally.
   *
   * <p>The returned {@link Selectable} does not cache its results or resolve ambiguities.</p>
   *
   * @param beanCollection a {@link Collection} of {@link Bean}s; must not be {@code null}
   *
   * @param m a {@link Matcher}; must not be {@code null}
   *
   * @return a non-{@code null}, determinate {@link Selectable}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @see Matcher#test(Object, Object)
   *
   * @see org.microbean.assign.Selectables#filtering(Collection, java.util.function.BiPredicate)
   *
   * @see Selectables#ambiguityReducing(Selectable, Predicate, ToIntFunction)
   *
   * @see #normalize(Collection)
   */
  public final Selectable<Annotated<? extends AnnotatedConstruct>, Bean<?>> typesafeFilteringSelectable(final Collection<? extends Bean<?>> beanCollection,
                                                                                                        final Matcher<? super Annotated<? extends AnnotatedConstruct>, ? super Id> m) {
    requireNonNull(m, "m"); // often an IdMatcher; need to overhaul this
    return
      beanCollection.isEmpty() ?
      org.microbean.assign.Selectables.empty() :
      org.microbean.assign.Selectables.filtering(this.normalize(beanCollection), (b, aac) -> m.test(aac, b.id()));
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Bean} {@linkplain Bean#id() has an <code>Id</code>} for which
   * {@link #alternate(Id)} returns {@code true}.
   *
   * @param b a non-{@code null} {@link Bean}
   *
   * @return {@code true} if and only if the supplied {@link Bean} {@linkplain Bean#id() has an <code>Id</code>} for which
   * {@link #alternate(Id)} returns {@code true}
   *
   * @exception NullPointerException if {@code b} is {@code null}
   *
   * @see #alternate(Id)
   */
  public final boolean alternate(final Bean<?> b) {
    return this.alternate(b.id());
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Id} {@linkplain Id#annotations() has annotations} for which
   * {@link #alternate(Collection)} returns {@code true}.
   *
   * @param id a non-{@code null} {@link Id}
   *
   * @return {@code true} if and only if the supplied {@link Id} {@linkplain Id#annotations() has annotations} for which
   * {@link #alternate(Collection)} returns {@code true}
   *
   * @exception NullPointerException if {@code id} is {@code null}
   *
   * @see #alternate(Collection)
   */
  public final boolean alternate(final Id id) {
    return this.alternate(id.annotations());
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Collection} of {@link AnnotationMirror}s contains at least
   * one {@link AnnotationMirror} for which {@link #alternate(AnnotationMirror)} returns {@code true}.
   *
   * @param as a non-{@code null} {@link Collection} of {@link AnnotationMirror}s
   *
   * @return {@code true} if and only if the supplied {@link Collection} of {@link AnnotationMirror}s contains at least
   * one {@link AnnotationMirror} for which {@link #alternate(AnnotationMirror)} returns {@code true}
   *
   * @exception NullPointerException if {@code as} is {@code null}
   *
   * @see #alternate(AnnotationMirror)
   */
  public final boolean alternate(final Collection<? extends AnnotationMirror> as) {
    for (final AnnotationMirror a : as) {
      if (this.alternate(a)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns {@code true} if and only if the supplied {@link AnnotationMirror} designates whatever it annotates as being
   * an <dfn>alternate</dfn>.
   *
   * @param a a non-{@code null} {@link AnnotationMirror}
   *
   * @return {@code true} if and only if the supplied {@link AnnotationMirror} designates whatever it annotates as being
   * an <dfn>alternate</dfn>
   *
   * @exception NullPointerException if {@code a} is {@code null}
   *
   * @see #Beans(Domain, AnnotationMirror, AnnotationMirror)
   */
  public final boolean alternate(final AnnotationMirror a) {
    return
      this.alternate != null &&
      ((QualifiedNameable)this.alternate.getAnnotationType().asElement()).getQualifiedName().contentEquals(((QualifiedNameable)a.getAnnotationType().asElement()).getQualifiedName()) &&
      get(a, "value") instanceof Boolean b &&
      b.booleanValue();
  }

  /**
   * Returns the result of invoking the {@link #rank(Id)} method with the supplied {@link Bean}'s {@link Bean#id() Id}.
   *
   * @param b a non-{@code null} {@link Bean}
   *
   * @return a rank, which may be {@code 0}
   *
   * @exception NullPointerException if {@code b} is {@code null}
   *
   * @see #rank(Id)
   */
  public final int rank(final Bean<?> b) {
    return this.rank(b.id());
  }

  /**
   * Returns the result of invoking the {@link #rank(Collection)} method with the supplied {@link Id}'s {@linkplain
   * Id#annotations() annotations}.
   *
   * @param id a non-{@code null} {@link Id}
   *
   * @return a rank, which may be {@code 0}
   *
   * @exception NullPointerException if {@code id} is {@code null}
   *
   * @see #rank(Collection)
   */
  public final int rank(final Id id) {
    return this.alternate(id) ? this.rank(id.annotations()) : 0;
  }

  /**
   * Iterates over the supplied {@link Collection} of {@link AnnotationMirror}s and invokes the {@link
   * #rank(AnnotationMirror)} method on each one until a non-zero result is found and returns that result, or {@code 0}
   * in all other cases.
   *
   * @param as a non-{@code null} {@link Collection} of {@link AnnotationMirror}s
   *
   * @return a rank, which may be {@code 0}
   *
   * @exception NullPointerException if {@code as} is {@code null}
   *
   * @see #rank(AnnotationMirror)
   */
  public final int rank(final Collection<? extends AnnotationMirror> as) {
    for (final AnnotationMirror a : as) {
      final int rank = this.rank(a);
      if (rank != 0) {
        return rank;
      }
    }
    return 0;
  }

  /**
   * Returns an {@code int} representing the <dfn>rank</dfn> derivable from the supplied {@link AnnotationMirror}, which
   * may be {@code 0}, or {@code 0} if the supplied {@link AnnotationMirror} does not or cannot supply a rank.
   *
   * @param a a non-{@code null} {@link AnnotationMirror}
   *
   * @return an {@code int} representing the <dfn>rank</dfn> derivable from the supplied {@link AnnotationMirror}, which
   * may be {@code 0}, or {@code 0} if the supplied {@link AnnotationMirror} does not or cannot supply a rank
   *
   * @exception NullPointerException if {@code a} is {@code null}
   *
   * @see #Beans(Domain, AnnotationMirror, AnnotationMirror)
   */
  public final int rank(final AnnotationMirror a) {
    return
      this.rank != null &&
      ((QualifiedNameable)this.rank.getAnnotationType().asElement()).getQualifiedName().contentEquals(((QualifiedNameable)a.getAnnotationType().asElement()).getQualifiedName()) &&
      get(a, "value") instanceof Integer i ?
      i.intValue() :
      0;
  }

}
