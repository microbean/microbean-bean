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
import java.util.Collections;
import java.util.SequencedSet;

import java.util.function.Function;

import static java.util.Collections.unmodifiableSequencedSet;

import static java.util.LinkedHashSet.newLinkedHashSet;

/**
 * An object with {@linkplain AttributedElement dependencies}.
 *
 * <p>By default, {@link Aggregate}s have {@linkplain #EMPTY_DEPENDENCIES no dependencies}.</p>
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 *
 * @see #dependencies()
 */
public interface Aggregate {


  /*
   * Static fields.
   */


  /**
   * An immutable, empty {@link SequencedSet} of {@link Assignment}s.
   */
  public static final SequencedSet<Assignment<?>> EMPTY_ASSIGNMENTS = unmodifiableSequencedSet(newLinkedHashSet(0));

  /**
   * An immutable, empty {@link SequencedSet} of {@link AttributedElement}s.
   */
  public static final SequencedSet<AttributedElement> EMPTY_DEPENDENCIES = unmodifiableSequencedSet(newLinkedHashSet(0));


  /*
   * Default instance methods.
   */


  /**
   * Returns an immutable {@link SequencedSet} of {@link AttributedElement} instances.
   *
   * @return an immutable {@link SequencedSet} of {@link AttributedElement} instances; never {@code null}
   *
   * @see AttributedElement
   */
  public default SequencedSet<AttributedElement> dependencies() {
    return EMPTY_DEPENDENCIES;
  }

  /**
   * A convenience method that assigns a contextual reference to each of this {@link Aggregate}'s {@link
   * AttributedElement} instances and returns the resulting {@link SequencedSet} of {@link Assignment}s.
   *
   * <p>Typically there is no need to override this method.</p>
   *
   * @param r a {@link Function} that retrieves a contextual reference suitable for an {@link AttributedType}; if {@link
   * #dependencies()} returns a non-empty {@link SequencedSet} then this argument must not be {@code null}
   *
   * @return an immutable {@link SequencedSet} of {@link Assignment} instances; never {@code null}
   *
   * @exception NullPointerException if {@code r} is {@code null}
   *
   * @see References
   */
  public default SequencedSet<? extends Assignment<?>> assign(final Function<? super AttributedType, ?> r) {
    final Collection<? extends AttributedElement> ds = this.dependencies();
    if (ds == null || ds.isEmpty()) {
      return EMPTY_ASSIGNMENTS;
    }
    final SequencedSet<Assignment<?>> assignments = newLinkedHashSet(ds.size());
    ds.forEach(d -> assignments.add(new Assignment<>(d, r.apply(d.attributedType()))));
    return Collections.unmodifiableSequencedSet(assignments);
  }

}
