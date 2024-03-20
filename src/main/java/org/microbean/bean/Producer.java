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

import java.util.Set;

// Subordinate to Factory<I>.
// Akin to CDI's Producer.
// Handles instance production and disposal, including intercepted production.
// Does NOT handle initialization; see for example https://github.com/search?q=repo%3Aweld%2Fcore+%22.produce%28%29%22+language%3AJava&type=code
// Does NOT handle post-initialization.
// Does NOT handle business method interception.
// Does NOT handle pre-disposal.
// See also: InterceptingProducer
@FunctionalInterface
public interface Producer<I> extends Aggregate {

  @Override // Aggregate
  public default Set<Dependency> dependencies() {
    return Set.of();
  }

  // TODO: c and rs go together, always, so anytime you need an rs you need a c.
  public default void dispose(final I i, final Creation<I> c, final ReferenceSelector rs) {
    if (i instanceof AutoCloseable ac) {
      try {
        ac.close();
      } catch (final RuntimeException | Error e) {
        throw e;
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new DestructionException(e.getMessage(), e);
      } catch (final Exception e) {
        throw new DestructionException(e.getMessage(), e);
      }
    }
  }

  // TODO: c and rs go together, always, so anytime you need an rs you need a c.
  public I produce(final Creation<I> c, final ReferenceSelector rs);

}
