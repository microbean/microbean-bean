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

import java.util.Objects;

// TODO: this is mildly fouled up. The spirit is right but the implementation is not so hot. Retaining for now so I can
// see how the subsidiary parts fit together, which is worth knowing.
@Deprecated(forRemoval = true)
abstract class AbstractFactory<I> implements Factory<I> {

  private static final Initializer<?> PASSTHROUGH_INITIALIZER = (i, r) -> i;

  private static final PostInitializer<?> PASSTHROUGH_POSTINITIALIZER = (i, r) -> i;

  private static final InterceptionsApplicator<?> PASSTHROUGH_INTERCEPTIONSAPPLICATOR = (i, r) -> i;

  private static final PreDestructor<?> PASSTHROUGH_PREDESTRUCTOR = (i, r) -> i;

  private final Producer<I> producer;

  private final Initializer<I> initializer;

  private final PostInitializer<I> postInitializer;

  private final InterceptionsApplicator<I> interceptionsApplicator;

  private final PreDestructor<I> preDestructor;

  private volatile boolean destroyed;

  @SuppressWarnings("unchecked")
  protected AbstractFactory(final Producer<I> producer, // production and destruction (including intercepted production)
                            final Initializer<I> initializer, // initialization (fields and initializer methods)
                            final PostInitializer<I> postInitializer, // post-initialization methods
                            final InterceptionsApplicator<I> interceptionsApplicator, // applies business method interceptions
                            final PreDestructor<I> preDestructor) { // pre-destroy methods
    super();
    this.producer = Objects.requireNonNull(producer, "producer");
    this.initializer = initializer == null ? (Initializer<I>)PASSTHROUGH_INITIALIZER : initializer;
    this.postInitializer = postInitializer == null ? (PostInitializer<I>)PASSTHROUGH_POSTINITIALIZER : postInitializer;
    this.interceptionsApplicator = interceptionsApplicator == null ? (InterceptionsApplicator<I>)PASSTHROUGH_INTERCEPTIONSAPPLICATOR : interceptionsApplicator;
    this.preDestructor = preDestructor == null ? (PreDestructor<I>)PASSTHROUGH_PREDESTRUCTOR : preDestructor;
  }

  @Override // Factory<I>
  public I create(final Request<I> r) {
    // Produce the product (with interceptions or not; producer is in charge of that), initialize the product,
    // post-initialize the product, apply business method interceptions to the product, return the (possibly intercepted) product
    return this.interceptionsApplicator.apply(this.postInitializer.postInitialize(this.initializer.initialize(this.producer.produce(r), r), r), r);
  }

  @Override // Factory<I>
  public boolean destroys() {
    return !this.destroyed;
  }

  // MUST be idempotent
  @Override // Factory<I>
  @SuppressWarnings("try")
  public void destroy(final I i, final Request<I> r) {
    if (this.destroyed) {
      return;
    }
    this.producer.dispose(this.preDestructor.preDestroy(i, r), r);
    this.destroyed = true;
  }

}
