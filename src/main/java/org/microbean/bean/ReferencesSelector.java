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

import javax.lang.model.AnnotatedConstruct;

import org.microbean.assign.Annotated;

import org.microbean.construct.Domain;

/**
 * A supplier of {@link References} objects.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #references(Annotated)
 *
 * @see #reference(Annotated)
 *
 * @see References
 */
// TODO: not crazy about the circular dependencies, but they're no worse than, say, a sealed class
public interface ReferencesSelector {

  /**
   * Destroys the supplied contextual reference if and only if it meets the conditions for destruction.
   *
   * @param r a contextual reference; may be {@code null} in which case {@code false} will be returned
   *
   * @return {@code true} if and only if destruction occurred
   *
   * @exception DestructionException if an error occurs
   */
  public boolean destroy(final Object r); // e.g. CDI's Instance#destroy(Object); works only on normal- and @Dependent-scoped objects

  // I don't like this at all. But you need something that can make, e.g., AnnotatedConstructs, so that you can call the other
  // methods in this interface. Every other type can be acquired at runtime via a ReferencesSelector, but you need a
  // Domain to kick start the process.
  /**
   * Returns a non-{@code null} {@link Domain} that can be used to furnish {@link javax.lang.model.AnnotatedConstruct}s.
   *
   * <p>Implementations of this method must be safe for concurrent use by multiple threads.</p>
   *
   * <h4>Design Note</h4>
   *
   * <p>Without this method, componenents using a {@link ReferencesSelector} will have to arrange to have their own
   * {@link Domain} somehow. Inconveniently, this is the only type in the system that cannot be acquired by an
   * invocation of the {@link #references(Annotated)} method, since you need a {@link Domain} to make an {@link
   * Annotated Annotated&lt;? extends AnnotatedConstruct&gt;}.</p>
   *
   * @return a non-{@code null} {@link Domain}
   *
   * @see Domain
   */
  public Domain domain();

  /**
   * Returns a non-{@code null}, determinate, contextual reference appropriate for the supplied {@link Bean}.
   *
   * <p>Implementations of this method must be safe for concurrent use by multiple threads.</p>
   *
   * <p>Implementations of this method must, directly or indirectly, must ensure that ultimately the supplied {@link
   * Bean}'s {@linkplain Bean#factory() affiliated <code>Factory</code>} is the mechanism used, and no other is used, to
   * {@linkplain Factory#create(Creation) create} any contextual instance underlying the contextual reference to be
   * returned.</p>
   *
   * @param <R> the contextual reference type
   *
   * @param bean a non-{@code null} {@link Bean}
   *
   * @return a non-{@code null}, determinate, contextual reference appropriate for the supplied {@link Bean}
   *
   * @exception NullPointerException if {@code bean} is {@code null}
   *
   * @exception IllegalArgumentException if {@code bean} is unsuitable in any way
   */
  // Experimental. Needed because you might grab a pile of Beans, then filter them yourself according to external
  // criteria, and then want references for them. I'm slightly sour on this because up to this point you don't need
  // Beans to implement ReferencesSelector at all. Maybe this needs to be a different interface, but then you're still
  // faced with the issue that fundamentally this method returns a contextual reference, and that *is* the domain of
  // ReferencesSelector.
  //
  // You could do reference(Predicate) but that's TOO generic.
  public <R> R reference(final Bean<R> bean);

  /**
   * Returns a non-{@code null}, determinate {@link References} capable of locating contextual references compatible
   * with the supplied {@link Annotated Annotated&lt;? extends AnnotatedConstruct&gt;}.
   *
   * @param <R> the contextual reference type
   *
   * @param aac a non-{@code null} {@link Annotated Annotated&lt;? extends AnnotatedConstruct&gt;} describing the
   * putative contextual reference; must represent {@code <R>}
   *
   * @return a non-{@code null}, determinate {@link References}
   *
   * @exception NullPointerException if {@code aac} is {@code null}
   *
   * @exception IllegalArgumentException if {@code aac} is unsuitable in any way
   *
   * @see References
   *
   * @see Annotated#of(AnnotatedConstruct)
   */
  public <R> References<R> references(final Annotated<? extends AnnotatedConstruct> aac);

  /**
   * Returns a non-{@code null}, determinate {@link References} capable of locating contextual references compatible
   * with the supplied {@link AnnotatedConstruct}.
   *
   * @param <R> the contextual reference type
   *
   * @param ac a non-{@code null} {@link AnnotatedConstruct} describing the putative contextual reference; must represent
   * {@code <R>}
   *
   * @return a non-{@code null}, determinate {@link References}
   *
   * @exception NullPointerException if {@code ac} is {@code null}
   *
   * @exception IllegalArgumentException if {@code ac} is unsuitable in any way
   *
   * @see References
   *
   * @see Annotated#of(AnnotatedConstruct)
   *
   * @deprecated Please use the {@link #references(Annotated)} method instead.
   */
  @Deprecated
  public default <R> References<R> references(final AnnotatedConstruct ac) {
    return references(Annotated.of(ac));
  }

  /**
   * A convenience method that acquires and returns what is presumed, possibly incorrectly, to be the sole contextual
   * reference compatible with the supplied {@link AnnotatedConstruct}.
   *
   * @param <R> the contextual reference type
   *
   * @param ac a non-{@code null} {@link AnnotatedConstruct} describing the putative contextual reference; must
   * represent {@code <R>}
   *
   * @return a non-{@code null}, determinate contextual reference
   *
   * @exception NullPointerException if {@code ac} is {@code null}
   *
   * @exception UnsatisfiedResolutionException if there is no compatible contextual reference
   *
   * @exception AmbiguousResolutionException if there is more than one compatible contextual reference
   *
   * @see #references(Annotated)
   *
   * @see Annotated#of(AnnotatedConstruct)
   *
   * @see References#get()
   *
   * @deprecated Please use the {@link #reference(Annotated)} method instead.
   */
  @Deprecated
  public default <R> R reference(final AnnotatedConstruct ac) {
    return this.<R>reference(Annotated.of(ac));
  }

  /**
   * A convenience method that acquires and returns what is presumed, possibly incorrectly, to be the sole contextual
   * reference compatible with the supplied {@link Annotated Annotated&lt;? extends AnnotatedConstruct&gt;}.
   *
   * @param <R> the contextual reference type
   *
   * @param aac a non-{@code null} {@link Annotated Annotated&lt;? extends AnnotatedConstruct&gt;} describing the
   * putative contextual reference; must represent {@code <R>}
   *
   * @return a non-{@code null}, determinate contextual reference
   *
   * @exception NullPointerException if {@code aac} is {@code null}
   *
   * @exception UnsatisfiedResolutionException if there is no compatible contextual reference
   *
   * @exception AmbiguousResolutionException if there is more than one compatible contextual reference
   *
   * @see #references(Annotated)
   *
   * @see References#get()
   */
  public default <R> R reference(final Annotated<? extends AnnotatedConstruct> aac) {
    return this.<R>references(aac).get();
  }

}
