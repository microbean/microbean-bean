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

import java.util.Objects;

public abstract class AbstractFactory<I> implements Factory<I> {

  private final InterceptionsApplicator<I> interceptionsApplicator;

  private final Initializer<I> initializer;

  private final Producer<I> producer;

  private final boolean destroys;

  private final Destructor<I> destructor;

  protected AbstractFactory(final Producer<I> producer) {
    this(null, null, producer, null);
  }

  protected AbstractFactory(final Producer<I> producer, final Destructor<I> destructor) {
    this(null, null, producer, destructor);
  }

  @SuppressWarnings("unchecked")
  protected AbstractFactory(final InterceptionsApplicator<I> interceptionsApplicator, // applies business method interceptions
                            final Initializer<I> initializer, // calls initializer methods and post-initialize methods
                            final Producer<I> producer, // handles production, including possibly intercepted production
                            final Destructor<I> destructor) { // calls pre-destroy methods and handles destruction
    super();
    this.interceptionsApplicator = interceptionsApplicator == null ? AbstractFactory::noopIntercept : interceptionsApplicator;
    this.initializer = initializer == null ? (NoOpInitializer<I>)NoOpInitializer.INSTANCE : initializer;
    this.producer = Objects.requireNonNull(producer);
    if (destructor == null) {
      this.destroys = false;
      this.destructor = new Destructor<I>(); // this destructor is basically a no-op
    } else {
      this.destroys = true;
      this.destructor = destructor;
    }
  }

  @Override // Factory<I>
  public I create(final Creation<I> c, final References<?> r) {
    // Produce the product, initialize the product, apply business method interceptions to the product, return the
    // product
    return this.interceptionsApplicator.apply(this.initializer.initialize(this.producer.produce(c, r), c, r), c, r);
  }

  @Override // Factory<I>
  public boolean destroys() {
    return this.destroys;
  }

  // MUST be idempotent
  @Override // Factory<I>
  @SuppressWarnings("try")
  public void destroy(final I i, final AutoCloseable destructionRegistry, final References<?> references) {
    this.destructor.destroy(i, destructionRegistry, references);
  }

  private static final <I> I noopIntercept(final I i, final Creation<I> c, final References<?> r) {
    return i;
  }

  private static final class NoOpInitializer<I> extends Initializer<I> {

    private static final NoOpInitializer<?> INSTANCE = new NoOpInitializer<>();

    private NoOpInitializer() {
      super();
    }

    @Override
    protected final I performInitialization(final I i, final Creation<I> c, final References<?> r) {
      return i;
    }

  }

}
