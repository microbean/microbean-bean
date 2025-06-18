/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2025 microBean™.
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

/**
 * A {@link ReductionException} indicating that a reduction did not occur because there were no elements to reduce.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 */
public final class UnsatisfiedReductionException extends ReductionException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates a new {@link UnsatisfiedReductionException}.
   *
   * @param criteria an {@link Object} representing reduction criteria; may be {@code null}
   *
   * @param message a detail message; may be {@code null}
   *
   * @param cause a {@link Throwable} that caused this {@link UnsatisfiedReductionException} to be created; may be
   * {@code null}
   */
  public UnsatisfiedReductionException(final Object criteria, final String message, final Throwable cause) {
    super(criteria, message, cause);
  }

}
