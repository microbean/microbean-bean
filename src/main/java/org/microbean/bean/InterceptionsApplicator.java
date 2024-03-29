/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2024 microBean™.
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

// Subordinate to Factory<T>.
// An applicator of business method interceptions. This is used during assembly of a Factory implementation and should
// be used probably only when "around-invoke" interceptions are in effect.
@FunctionalInterface
public interface InterceptionsApplicator<I> {

  // i is the contextual instance
  // TODO: c and rs go together, always, so anytime you need an rs you need a c.
  public I apply(final I i, final Creation<I> c, final ReferenceSelector rs);

}
