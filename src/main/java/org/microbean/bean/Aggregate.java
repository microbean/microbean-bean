/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2024–2025 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.microbean.bean;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;

/**
 * An object with {@linkplain AttributedElement dependencies}.
 *
 * <p>By default, {@link Aggregate}s have no dependencies.</p>
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
  public static final SequencedSet<Assignment<?>> EMPTY_ASSIGNMENTS = Collections.unmodifiableSequencedSet(new LinkedHashSet<>(0));

  /**
   * An immutable, empty {@link SequencedSet} of {@link AttributedElement}s.
   */
  public static final SequencedSet<AttributedElement> EMPTY_DEPENDENCIES = Collections.unmodifiableSequencedSet(new LinkedHashSet<>(0));


  /*
   * Default instance methods.
   */


  /**
   * Returns an unmodifiable {@link SequencedSet} of {@link AttributedElement} instances.
   *
   * @return an unmodifiable {@link SequencedSet} of {@link AttributedElement} instances; never {@code null}
   *
   * @see AttributedElement
   */
  public default SequencedSet<AttributedElement> dependencies() {
    return EMPTY_DEPENDENCIES;
  }

  /**
   * Assigns a contextual reference to each of this {@link Aggregate}'s {@link AttributedElement} instances and returns the
   * resulting {@link List} of {@link Assignment}s.
   *
   * @param r a {@link Request}; must not be {@code null}
   *
   * @return a {@link List} of {@link Assignment} instances; never {@code null}
   *
   * @exception NullPointerException if {@code r} is {@code null}
   */
  public default SequencedSet<? extends Assignment<?>> assign(final Request<?> r) {
    final Collection<? extends AttributedElement> ds = this.dependencies();
    if (ds == null || ds.isEmpty()) {
      return EMPTY_ASSIGNMENTS;
    }
    final SequencedSet<Assignment<?>> assignments = new LinkedHashSet<>();
    for (final AttributedElement d : ds) {
      assignments.add(new Assignment<>(d, r.reference(d.attributedType())));
    }
    return Collections.unmodifiableSequencedSet(assignments);
  }

}
