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

import java.util.Iterator;

/**
 * An {@link Iterable} {@linkplain #iterator() providing access to contextual references of a given type}, and a {@link
 * ReferencesSelector} providing access to a {@link References} of a different type.
 *
 * @param <R> the contextual reference type
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see ReferencesSelector
 */
public interface References<R> extends Iterable<R>, ReferencesSelector {

  /**
   * A convenience method that acquires and returns what is presumed, possibly incorrectly, to be the sole contextual
   * reference available from this {@link References}.
   *
   * @return a contextual reference; never {@code null}
   *
   * @exception UnsatisfiedResolutionException if there are no contextual references to return
   *
   * @exception AmbiguousResolutionException if there is more than one contextual reference to return
   */
  public default R get() {
    final Iterator<R> i = this.iterator();
    if (!i.hasNext()) {
      throw new UnsatisfiedResolutionException(null, null, null);
    }
    final R r = i.next();
    if (i.hasNext()) {
      throw new AmbiguousResolutionException(null, null, null);
    }
    return r;
  }

  /**
   * Returns the size of this {@link References}, if known, or a negative {@code int} if the size is not known.
   *
   * @return the size of this {@link References} or a negative {@code int} if it is not known
   */
  // @Deprecated // Thinking about deprecating/removing this
  public int size();

}
