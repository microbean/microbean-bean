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

public class UnsatisfiedResolutionException extends ResolutionException {

  private static final long serialVersionUID = 1L;

  public UnsatisfiedResolutionException() {
    super();
  }

  public UnsatisfiedResolutionException(final BeanSelectionCriteria beanSelectionCriteria) {
    super(beanSelectionCriteria);
  }

  public UnsatisfiedResolutionException(final String message) {
    super(message);
  }

  public UnsatisfiedResolutionException(final BeanSelectionCriteria beanSelectionCriteria,
                                        final String message) {
    super(beanSelectionCriteria, message);
  }

  public UnsatisfiedResolutionException(final Throwable cause) {
    super(cause);
  }

  public UnsatisfiedResolutionException(final BeanSelectionCriteria beanSelectionCriteria,
                                        final Throwable cause) {
    super(beanSelectionCriteria, cause);
  }

  public UnsatisfiedResolutionException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public UnsatisfiedResolutionException(final BeanSelectionCriteria beanSelectionCriteria,
                                        final String message,
                                        final Throwable cause) {
    super(beanSelectionCriteria, message, cause);
  }

}
