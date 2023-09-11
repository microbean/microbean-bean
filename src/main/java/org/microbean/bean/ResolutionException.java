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

public class ResolutionException extends BeanException {

  private static final long serialVersionUID = 1L;

  private transient final BeanSelectionCriteria beanSelectionCriteria;

  public ResolutionException() {
    super();
    this.beanSelectionCriteria = null;
  }

  public ResolutionException(final BeanSelectionCriteria beanSelectionCriteria) {
    super();
    this.beanSelectionCriteria = beanSelectionCriteria;
  }

  public ResolutionException(final String message) {
    super(message);
    this.beanSelectionCriteria = null;
  }

  public ResolutionException(final BeanSelectionCriteria beanSelectionCriteria,
                             final String message) {
    super(message);
    this.beanSelectionCriteria = beanSelectionCriteria;
  }

  public ResolutionException(final Throwable cause) {
    super(cause);
    this.beanSelectionCriteria = null;
  }

  public ResolutionException(final BeanSelectionCriteria beanSelectionCriteria,
                             final Throwable cause) {
    super(cause);
    this.beanSelectionCriteria = beanSelectionCriteria;
  }

  public ResolutionException(final String message,
                             final Throwable cause) {
    super(message, cause);
    this.beanSelectionCriteria = null;
  }

  public ResolutionException(final BeanSelectionCriteria beanSelectionCriteria,
                             final String message,
                             final Throwable cause) {
    super(message, cause);
    this.beanSelectionCriteria = beanSelectionCriteria;
  }

  public final BeanSelectionCriteria beanSelectionCriteria() {
    return this.beanSelectionCriteria;
  }

  @Override
  public String toString() {
    final BeanSelectionCriteria beanSelectionCriteria = this.beanSelectionCriteria();
    if (beanSelectionCriteria == null) {
      return super.toString();
    } else if (this.getLocalizedMessage() == null) {
      return super.toString() + ": beanSelectionCriteria: " + beanSelectionCriteria;
    } else {
      return super.toString() + "; beanSelectionCriteria " + beanSelectionCriteria;
    }
  }

}
