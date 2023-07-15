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
package org.microbean.bean2;

public class BeanException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public BeanException() {
    super();
  }

  public BeanException(final String message) {
    super(message);
  }

  public BeanException(final Throwable cause) {
    super(cause);
  }

  public BeanException(final String message,
                       final Throwable cause) {
    super(message, cause);
  }

}
