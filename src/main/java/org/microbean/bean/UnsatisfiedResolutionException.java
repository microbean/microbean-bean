/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2022 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.bean;

public class UnsatisfiedResolutionException extends ResolutionException {

  private static final long serialVersionUID = 1L;

  public UnsatisfiedResolutionException() {
    this(null, null, null);
  }

  public UnsatisfiedResolutionException(final String message) {
    this(null, message, null);
  }

  public UnsatisfiedResolutionException(final Selector selector,
                                        final String message) {
    this(selector, message, null);
  }

  public UnsatisfiedResolutionException(final Throwable cause) {
    this(null, null, cause);
  }

  public UnsatisfiedResolutionException(final Selector selector,
                                        final Throwable cause) {
    this(selector, null, cause);
  }

  public UnsatisfiedResolutionException(final String message,
                                        final Throwable cause) {
    this(null, message, cause);
  }

  public UnsatisfiedResolutionException(final Selector selector,
                                        final String message,
                                        final Throwable cause) {
    super(selector, message, cause);
  }

}