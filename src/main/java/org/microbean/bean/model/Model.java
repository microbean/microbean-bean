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
package org.microbean.bean.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;

import java.util.function.BiFunction;
import java.util.function.Function;

import javax.lang.model.AnnotatedConstruct;

import javax.lang.model.element.Element;

import org.microbean.assign.Aggregate;
import org.microbean.assign.Annotated;
import org.microbean.assign.Selectable;

import org.microbean.bean.Bean;

import static java.util.HashMap.newHashMap;

import static java.util.HashSet.newHashSet;

import static java.util.Objects.requireNonNull;

import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 * An immutable model of a system's {@linkplain DependencyResolution dependency resolutions}.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #toSelectionCache()
 *
 * @see #valid()
 */
public final class Model {

  private final Set<DependencyResolution> dependencyResolutions;

  private volatile boolean valid;

  private Model() {
    this(Set.of());
  }

  private Model(Set<? extends DependencyResolution> dependencyResolutions) {
    super();
    if (dependencyResolutions == null || dependencyResolutions.isEmpty()) {
      this.dependencyResolutions = Set.of();
      this.valid = true;
    } else {
      this.dependencyResolutions = Set.copyOf(dependencyResolutions);
    }
  }

  @Override // Object
  public final boolean equals(final Object other) {
    return switch (other) {
    case null -> false;
    case Model m when this.getClass() == m.getClass() -> this.dependencyResolutions.equals(m.dependencyResolutions);
    default -> false;
    };
  }

  @Override // Object
  public final int hashCode() {
    return this.dependencyResolutions.hashCode();
  }

  /**
   * Returns {@code true} if and only if this {@link Model} is <dfn>valid</dfn>.
   *
   * <p>A {@link Model} is valid if and only if, for each of its {@link DependencyResolution}s, there {@linkplain
   * DependencyResolution#beans() exists} exactly one {@link Bean}.</p>
   *
   * @return {@code true} if and only if this {@link Model} is <dfn>valid</dfn>
   *
   * @see #toSelectionCache()
   */
  public final boolean valid() {
    if (this.valid) { // volatile read
      return true;
    }
    for (final DependencyResolution dependencyResolution : this.dependencyResolutions) {
      if (dependencyResolution.beans().size() != 1) {
        return false;
      }
    }
    this.valid = true; // volatile write
    return true;
  }

  /**
   * Returns a non-{@code null}, immutable, determinate {@link Set} of all {@link DependencyResolution}s managed by this
   * {@link Model} that are {@linkplain DependencyResolution#ambiguous() ambiguous}.
   *
   * @return a non-{@code null}, immutable, determinate {@link Set} of all {@link DependencyResolution}s managed by this
   * {@link Model} that are {@linkplain DependencyResolution#ambiguous() ambiguous}
   *
   * @see DependencyResolution#ambiguous()
   */
  public final Set<DependencyResolution> ambiguousDependencyResolutions() {
    return this.dependencyResolutions.stream().filter(DependencyResolution::ambiguous).collect(toUnmodifiableSet());
  }

  /**
   * Returns a non-{@code null}, immutable, determinate {@link Set} of all {@link DependencyResolution}s managed by this
   * {@link Model} that are {@linkplain DependencyResolution#unsatisfied() unsatisfied}.
   *
   * @return a non-{@code null}, immutable, determinate {@link Set} of all {@link DependencyResolution}s managed by this
   * {@link Model} that are {@linkplain DependencyResolution#unsatisfied() unsatisfied}
   *
   * @see DependencyResolution#unsatisfied()
   */
  public final Set<DependencyResolution> unsatisfiedDependencyResolutions() {
    return this.dependencyResolutions.stream().filter(DependencyResolution::unsatisfied).collect(toUnmodifiableSet());
  }

  /**
   * Returns a non-{@code null} {@link BiFunction} that represents a compute-if-absent operation on an internal,
   * unbounded, thread-safe cache.
   *
   * <p>The return value is suitable for passing to the {@link org.microbean.assign.Selectables#caching(Selectable,
   * BiFunction)} method.</p>
   *
   * @return a non-{@code null} {@link BiFunction} that represents a compute-if-absent operation on an internal,
   * unbounded, thread-safe cache
   *
   * @exception IllegalStateException if this {@link Model} is not {@linkplain #valid() valid}
   *
   * @see org.microbean.assign.Selectables#caching(Selectable, BiFunction)
   *
   * @see #ambiguousDependencyResolutions()
   *
   * @see #unsatisfiedDependencyResolutions()
   */
  // So you can do:
  // org.microbean.assign.Selectables.caching(selectable, model.toSelectionCache());
  public final BiFunction<? super Annotated<? extends AnnotatedConstruct>, Function<? super Annotated<? extends AnnotatedConstruct>, ? extends List<Bean<?>>>, ? extends List<Bean<?>>> toSelectionCache() {
    if (!this.valid()) {
      throw new IllegalStateException("not valid");
    }
    // TODO: look at this carefully. We have Annotated now, which makes an AnnotatedConstruct and its annotations
    // cacheable. We may not need AnnotatedConstruct in here, just TypeMirror.
    final Map<Annotated<? extends AnnotatedConstruct>, List<Bean<?>>> m = new ConcurrentHashMap<>(this.dependencyResolutions.size()); // too big but whatever
    for (final DependencyResolution dr : this.dependencyResolutions) {
      m.putIfAbsent(dr.annotated(), dr.beans());
    }
    return m::computeIfAbsent; // m is mutable on purpose; we can revisit if we decide that a Model is the absolute source of truth
  }

  /**
   * Returns a non-{@code null} {@link Model} built from the supplied {@link Selectable}.
   *
   * @param s a non-{@code null} {@link Selectable}
   *
   * @return a non-{@code null} {@link Model} built from the supplied {@link Selectable}
   *
   * @exception NullPointerException if {@code s} is {@code null}
   *
   * @see Selectable
   */
  // s would normally be typesafeFiltering and ambiguityReducing but not necessarily cached
  @SuppressWarnings("unchecked")
  public static Model of(final Selectable<? super Annotated<? extends AnnotatedConstruct>, ? extends Bean<?>> s) {
    final Collection<? extends Aggregate> allBeans = s.select(null);
    if (allBeans.isEmpty()) {
      return new Model(Set.of());
    }
    final Map<Annotated<? extends AnnotatedConstruct>, List<Bean<?>>> m = newHashMap(allBeans.size() * 5); // estimate;
    final Set<DependencyResolution> dependencyResolutions = newHashSet(allBeans.size() * 5);
    for (final Aggregate bean : allBeans) {
      for (final Annotated<? extends Element> dependency : bean.dependencies()) {
        // TODO: This is irritating. A Bean's dependencies are basically Elements (that's good) but all this Set
        // business is designed to reduce demand down to qualified types (e.g. TypeMirror, but with Element
        // annotations).
        //
        // Time has passed and now we have the Annotated interface from microbean-assign. This is still a slight
        // mess. We still probably want to "reduce" an Annotated<Element> to an Annotated<TypeMirror> and cache that
        // sucker.
        //
        // Resuming commentary: That is, you may have 37 (Annotated) Elements that all "have" (asType()) the same
        // TypeMirror and notional set of annotations, and you really want your model to reflect just the one.
        //
        // Now you get into type equality etc. (See
        // https://github.com/microbean/microbean-construct/issues/31#issuecomment-3565216353 and
        // https://docs.oracle.com/en/java/javase/25/docs/api/java.compiler/javax/lang/model/type/TypeMirror.html#equals(java.lang.Object)
        // and
        // https://docs.oracle.com/en/java/javase/25/docs/api/java.compiler/javax/lang/model/util/Types.html#isSameType(javax.lang.model.type.TypeMirror,javax.lang.model.type.TypeMirror).)
        //
        // Probably what we want to do is a laborious "bring your own Set" implementation where we do not use hashcodes
        // (there's no way to "do" a hashcode of a TypeMirror that is "compatible" with sametypeness). So keep a running
        // list of TypeMirrors that we have seen (as determined by sameType).
        //
        // Not sure this is right, actually. The solution may be to do work inside the Selectable and the caching
        // mechanics. Specifically, if a Selectable works on an AnnotatedConstruct, then a caching version of that
        // should reduce the AnnotatedConstruct to a TypeMirror (with annotations perhaps propagated from the Element to
        // the TypeMirror)
        //
        // Then we want to make synthetic types (ugh) for ...
        //
        // See also https://github.com/jakartaee/cdi/issues/877 and https://github.com/jakartaee/inject/issues/40
        //
        // Other things that are tangentially related: https://github.com/openjdk/jdk/pull/24775#issuecomment-3133482962
        dependencyResolutions.add(new DependencyResolution(dependency,
                                                           m.computeIfAbsent(dependency,
                                                                             aac -> (List<Bean<?>>)s.select(aac))));
      }
    }
    return new Model(dependencyResolutions);
  }

  /**
   * A representation of the attempted <dfn>resolution</dfn> of a <dfn>dependency</dfn>, represented by an {@link
   * Annotated Annotated&lt;? extends AnnotatedConstruct&gt;}, to a {@link List} of {@link Bean}s that match it.
   *
   * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
   *
   * @see Annotated
   *
   * @see Annotated#of(AnnotatedConstruct)
   *
   * @see Bean
   */
  public static final class DependencyResolution {

    private final Annotated<? extends AnnotatedConstruct> annotated;

    private final List<Bean<?>> beans;

    private DependencyResolution(final Annotated<? extends AnnotatedConstruct> annotated, final List<Bean<?>> beans) {
      super();
      this.annotated = requireNonNull(annotated, "annotated");
      this.beans = List.copyOf(beans);
    }

    /**
     * Returns {@code true} if and only if the return value of an invocation of the {@link #beans()} method {@linkplain
     * List#size() has a size} greater than {@code 1}.
     *
     * @return {@code true} if and only if the return value of an invocation of the {@link #beans()} method {@linkplain
     * List#size() has a size} greater than {@code 1}
     *
     * @see #beans()
     */
    public final boolean ambiguous() {
      return this.beans().size() > 1;
    }

    /**
     * Returns the non-{@code null}, determinate {@link Annotated Annotated&lt;? extends AnnotatedConstruct&gt;}
     * representing this {@link DependencyResolution}'s dependency.
     *
     * @return the non-{@code null}, determinate {@link Annotated Annotated&lt;? extends AnnotatedConstruct&gt;}
     * representing this {@link DependencyResolution}'s dependency
     */
    public final Annotated<? extends AnnotatedConstruct> annotated() {
      return this.annotated;
    }

    /**
     * Returns the non-{@code null}, immutable, determinate {@link List} of {@link Bean}s representing this {@link
     * DependencyResolution}'s dependency resolution.
     *
     * @return the non-{@code null}, immutable, determinate {@link List} of {@link Bean}s representing this {@link
     * DependencyResolution}'s dependency resolution
     */
    public final List<Bean<?>> beans() {
      return this.beans;
    }

    @Override // Object
    public final boolean equals(final Object other) {
      return this == other || switch (other) {
      case null -> false;
      case DependencyResolution dr when dr.getClass() == this.getClass() -> this.annotated.equals(dr.annotated) && this.beans.equals(dr.beans);
      default -> false;
      };
    }

    @Override // Object
    public final int hashCode() {
      return this.annotated.hashCode() ^ this.beans.hashCode();
    }

    /**
     * Returns {@code true} if and only if the return value of an invocation of the {@link #beans()} method {@linkplain
     * List#isEmpty() is empty}.
     *
     * @return {@code true} if and only if the return value of an invocation of the {@link #beans()} method {@linkplain
     * List#isEmpty() is empty}
     *
     * @see #beans()
     */
    public final boolean unsatisfied() {
      return this.beans().isEmpty();
    }

    @Override // Object
    public final String toString() {
      return this.annotated() + " = " + this.beans();
    }

  }

}
