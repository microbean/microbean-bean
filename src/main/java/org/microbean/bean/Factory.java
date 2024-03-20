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

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;

import java.util.Optional;
import java.util.Set;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;

/**
 * A creator and destroyer of an instance of something.
 *
 * @param <I> the type of the instances this {@link Factory} creates and destroys
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 */
@FunctionalInterface
public interface Factory<I> extends Aggregate, Constable {

  public I create(final Creation<I> c, final ReferenceSelector referenceSelector);

  @Override // Aggregate
  public default Set<Dependency> dependencies() {
    return Set.of();
  }

  public default I singleton() {
    return null;
  }

  public default boolean destroys() {
    return true;
  }

  // MUST be idempotent
  // If i is an AutoCloseable, MUST be idempotent
  // autoCloseableRegistry's close() MUST be idempotent
  public default void destroy(final I i, final AutoCloseable autoCloseableRegistry, final Creation<I> c, final ReferenceSelector rs) {
    if (i instanceof AutoCloseable ac) {
      final Runnable r = () -> {
        try {
          ac.close();
        } catch (final RuntimeException | Error re) {
          throw re;
        } catch (final Exception e) {
          if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
          }
          throw new DestructionException(e.getMessage(), e);
        }
      };
      if (autoCloseableRegistry == null) {
        r.run();
      } else {
        try (autoCloseableRegistry) {
          r.run();
        } catch (final RuntimeException | Error re) {
          throw re;
        } catch (final Exception e) {
          if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
          }
          throw new DestructionException(e.getMessage(), e);
        }
      }
    }
  }

  /**
   * Returns an {@link Optional} containing the nominal descriptor for this instance, if one can be constructed, or an
   * {@linkplain Optional#isEmpty() empty <code>Optional</code>} if one cannot be constructed.
   *
   * <p>The default implementation of this method returns an {@link Optional} that contains a dynamic constant
   * representing an invocation of the implementation's constructor that takes no arguments.  <strong>The resolution of
   * this dynamic constant is undefined if the implementation does not declare such a constructor.</strong></p>
   *
   * @return an {@link Optional} containing the nominal descriptor for this instance, if one can be constructed, or an
   * {@linkplain Optional#isEmpty() empty <code>Optional</code>} if one cannot be constructed
   *
   * @threadsafety This method is safe for concurrent use by multiple threads.
   *
   * @idempotency This method is neither idempotent nor deterministic.
   */
  @Override // Constable
  public default Optional<? extends ConstantDesc> describeConstable() {
    return this.getClass()
      .describeConstable()
      .map(classDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                               MethodHandleDesc.ofConstructor(classDesc)));
  }

}
