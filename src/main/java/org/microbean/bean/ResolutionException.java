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

public class ResolutionException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private transient final Selector selector;

  public ResolutionException() {
    super();
    this.selector = null;
  }

  public ResolutionException(final Selector selector) {
    super();
    this.selector = selector;
  }

  public ResolutionException(final String message) {
    super(message);
    this.selector = null;
  }

  public ResolutionException(final Selector selector,
                             final String message) {
    super(message);
    this.selector = selector;
  }

  public ResolutionException(final Throwable cause) {
    super(cause);
    this.selector = null;
  }

  public ResolutionException(final Selector selector,
                             final Throwable cause) {
    super(cause);
    this.selector = selector;
  }

  public ResolutionException(final String message,
                             final Throwable cause) {
    super(message, cause);
    this.selector = null;
  }

  public ResolutionException(final Selector selector,
                             final String message,
                             final Throwable cause) {
    super(message, cause);
    this.selector = selector;
  }

  public final Selector selector() {
    return this.selector;
  }

  @Override
  public String toString() {
    final Selector selector = this.selector();
    if (selector == null) {
      return super.toString();
    } else if (this.getLocalizedMessage() == null) {
      return super.toString() + ": selector: " + selector;
    } else {
      return super.toString() + "; selector " + selector;
    }
  }

}
