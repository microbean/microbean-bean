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

import org.microbean.assign.AttributedType;

import org.microbean.construct.Domain;

/**
 * A supplier of {@link References} objects.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #references(AttributedType)
 *
 * @see #reference(AttributedType)
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

  // I don't like this at all. But you need something that can make, e.g., TypeMirrors, so that you can create
  // AttributedTypes, which you need to call the other methods in this interface. Every other type can be acquired at
  // runtime via a ReferencesSelector, but you need a Domain to kick start the process.
  /**
   * Returns a {@link Domain} that can be used to produce {@link javax.lang.model.type.TypeMirror}s so that {@link
   * AttributedType} instances used by the {@link #references(AttributedType)} and {@link #reference(AttributedType)}
   * methods may be created.
   *
   * <p>Implementations of this method must not return {@code null}.</p>
   *
   * <p>Implementations of this method must be safe for concurrent use by multiple threads.</p>
   *
   * <p>Implementations of this method must return a determinate value.</p>
   *
   * <h4>Design Note</h4>
   *
   * <p>Without this method, componenents using a {@link ReferencesSelector} will have to arrange to have their own
   * {@link Domain} somehow. Inconveniently, this is the only type in the system that cannot be acquired by an
   * invocation of the {@link #references(AttributedType)} method, since you need a {@link Domain} to make an {@link
   * AttributedType}.</p>
   *
   * @return a non-{@code null} {@link Domain}
   */
  public Domain domain();

  /**
   * Returns a contextual reference of the relevant type appropriate for the supplied {@link Bean}.
   *
   * <p>Implementations of this method must be safe for concurrent use by multiple threads.</p>
   *
   * <p>Implementations of this method must, directly or indirectly, use the supplied {@link Bean}'s {@linkplain
   * Bean#factory() affiliated <code>Factory</code>} to {@linkplain Factory#create(Creation) create} any contextual
   * instance underlying the contextual reference to be returned.</p>
   *
   * @param <R> the contextual reference type
   *
   * @param bean a {@link Bean}; must not be {@code null}
   *
   * @return a non-{@code null} contextual reference
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
   * Returns a {@link References} capable of locating contextual references of the relevant type.
   *
   * @param <R> the contextual reference type
   *
   * @param t an {@link AttributedType} describing the contextual reference type; must not be {@code null}
   *
   * @return a non-{@code null} {@link References}
   *
   * @exception NullPointerException if {@code t} is {@code null}
   *
   * @exception IllegalArgumentException if {@code t} is unsuitable in any way
   *
   * @see References
   */
  public <R> References<R> references(final AttributedType t);

  /**
   * A convenience method that acquires and returns what is presumed, possibly incorrectly, to be the sole contextual
   * reference of the relevant type.
   *
   * @param <R> the contextual reference type
   *
   * @param t an {@link AttributedType} describing the contextual reference type; must not be {@code null}
   *
   * @return a non-{@code null} contextual reference
   *
   * @exception NullPointerException if {@code t} is {@code null}
   *
   * @exception UnsatisfiedResolutionException if there is no contextual reference for the relevant type
   *
   * @exception AmbiguousResolutionException if there is more than one contextual reference for the relevant type
   *
   * @see #references(AttributedType)
   *
   * @see References#get()
   */
  public default <R> R reference(final AttributedType t) {
    return this.<R>references(t).get();
  }

}
