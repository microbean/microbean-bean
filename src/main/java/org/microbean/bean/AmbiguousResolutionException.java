/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023 microBean™.
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
import java.util.List;

public class AmbiguousResolutionException extends ResolutionException {

  private static final long serialVersionUID = 1L;

  private final transient Collection<Alternate> alternates;

  public AmbiguousResolutionException() {
    this(null, null, null, null);
  }

  public AmbiguousResolutionException(final Collection<? extends Alternate> alternates,
                                      final String message) {
    this(null, alternates, message, null);
  }

  public AmbiguousResolutionException(final BeanSelectionCriteria beanSelectionCriteria,
                                      final Collection<? extends Alternate> alternates,
                                      final String message) {
    this(beanSelectionCriteria, alternates, message, null);
  }

  public AmbiguousResolutionException(final Collection<? extends Alternate> alternates,
                                      final Throwable cause) {
    this(null, alternates, null, cause);
  }

  public AmbiguousResolutionException(final BeanSelectionCriteria beanSelectionCriteria,
                                      final Collection<? extends Alternate> alternates,
                                      final Throwable cause) {
    this(beanSelectionCriteria, alternates, null, cause);
  }

  public AmbiguousResolutionException(final Collection<? extends Alternate> alternates,
                                      final String message,
                                      final Throwable cause) {
    this(null, alternates, message, cause);
  }

  public AmbiguousResolutionException(final BeanSelectionCriteria beanSelectionCriteria,
                                      final Collection<? extends Alternate> alternates,
                                      final String message,
                                      final Throwable cause) {
    super(beanSelectionCriteria, message, cause);
    if (alternates == null || alternates.isEmpty()) {
      this.alternates = List.of();
    } else {
      this.alternates = List.copyOf(alternates);
    }
  }

  public final Collection<Alternate> alternates() {
    return this.alternates;
  }

}
