/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2025 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.  ou may obtain a copy of the License at
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

/**
 * A {@link ReductionException} indicating that many contextual instances could not be reduced to one.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 */
public class AmbiguousReductionException extends ReductionException {

  private static final long serialVersionUID = 1L;

  private final transient Collection<?> alternates;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link AmbiguousReductionException}.
   *
   * @param criteria the criteria by which a reduction was supposed to be effected; may be {@code null}
   *
   * @param alternates the contextual instances that could not be reduced; may be {@code null}
   *
   * @param message a detail message describing the exception; may be {@code null}
   */
  public AmbiguousReductionException(final Object criteria,
                                     final Collection<?> alternates,
                                     final String message) {
    super(criteria, message, null);
    if (alternates == null || alternates.isEmpty()) {
      this.alternates = List.of();
    } else {
      this.alternates = List.copyOf(alternates);
    }
  }

  /**
   * Returns the contextual instances that could not be reduced.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return a non-{@code null}, immutable {@link Collection} of contextual instances
   */
  public final Collection<?> alternates() {
    return this.alternates;
  }

}
