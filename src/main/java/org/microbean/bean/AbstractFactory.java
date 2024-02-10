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

// TODO: this is mildly fouled up. The spirit is right but the implementation is not so hot.
@Deprecated
public abstract class AbstractFactory<I> implements Factory<I> {

  private static final Initializer<?> PASSTHROUGH_INITIALIZER = new AbstractInitializer<Object>();

  private static final PostInitializer<?> PASSTHROUGH_POSTINITIALIZER = new AbstractPostInitializer<Object>();

  private static final InterceptionsApplicator<?> PASSTHROUGH_INTERCEPTIONSAPPLICATOR = new AbstractInterceptionsApplicator<Object>();

  private static final PreDestructor<?> PASSTHROUGH_PREDESTRUCTOR = new AbstractPreDestructor<Object>();

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
  public I create(final Creation<I> c, final ReferenceSelector r) {
    // Produce the product, initialize the product, apply business method interceptions to the product, return the
    // product
    return this.interceptionsApplicator.apply(this.postInitializer.postInitialize(this.initializer.initialize(this.producer.produce(c, r), c, r), c, r), c, r);
  }

  @Override // Factory<I>
  public boolean destroys() {
    return !this.destroyed;
  }

  // MUST be idempotent
  @Override // Factory<I>
  @SuppressWarnings("try")
  public void destroy(final I i, final AutoCloseable destructionRegistry, final Creation<I> c, final ReferenceSelector rs) {
    if (this.destroyed) {
      return;
    }
    if (destructionRegistry == null) {
      this.producer.dispose(this.preDestructor.preDestroy(i, c, rs), c, rs);
    } else {
      try (destructionRegistry) {
        this.producer.dispose(this.preDestructor.preDestroy(i, c, rs), c, rs);
      } catch (final RuntimeException | Error e) {
        throw e;
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new DestructionException(e.getMessage(), e);
      } catch (final Exception e) {
        throw new DestructionException(e.getMessage(), e);
      }
    }
    this.destroyed = true;
  }

}
