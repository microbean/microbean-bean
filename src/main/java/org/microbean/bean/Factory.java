/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2025 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.microbean.bean;

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;

import java.util.Optional;

import org.microbean.assign.Aggregate;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;

/**
 * A source of (normally new) contextual instances.
 *
 * @param <I> the contextual instance type
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #create(Creation)
 *
 * @see Aggregate
 */
public interface Factory<I> extends Aggregate, Constable {

  /**
   * Returns a (normally new) contextual instance, which may be {@code null}.
   *
   * @param creation a {@link Creation}; may be {@code null}
   *
   * @return a contextual instance, which may be {@code null}
   *
   * @see Creation
   */
  public I create(final Creation<I> creation);

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
   * @microbean.threadsafety This method is safe for concurrent use by multiple threads.
   *
   * @microbean.idempotency This method is neither idempotent nor deterministic.
   */
  @Override // Constable
  public default Optional<? extends ConstantDesc> describeConstable() {
    return
      Optional.of(DynamicConstantDesc.of(BSM_INVOKE,
                                         MethodHandleDesc.ofConstructor(ClassDesc.of(this.getClass().getCanonicalName()))));
  }

  /**
   * Destroys the supplied contextual instance.
   *
   * <p>The default implementation of this method {@linkplain AutoCloseable#close() closes} the supplied contextual
   * instance if it is an instance of {@link AutoCloseable}, and {@linkplain AutoCloseable#close() closes} the supplied
   * {@link Destruction} if it is non-{@code null}.</p>
   *
   * @param i the contextual instance to destroy; may be {@code null} in which case no action must be taken
   *
   * @param creation the object supplied to the {@link #create(Creation)} method represented here as a {@link
   * Destruction}; may be {@code null}; must have an idempotent {@link AutoCloseable#close() close()} method
   *
   * @see #create(Creation)
   *
   * @see Destruction
   *
   * @see Creation
   */
  @SuppressWarnings("try")
  public default void destroy(final I i, final Destruction creation) {
    if (creation == null) {
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
    } else if (!(creation instanceof Creation<I>)) {
      throw new IllegalArgumentException("creation: " + creation);
    } else if (creation instanceof AutoCloseable cac) {
      try (cac) {
        if (i instanceof AutoCloseable iac) {
          iac.close();
        }
      } catch (final RuntimeException | Error e) {
        throw e;
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new DestructionException(e.getMessage(), e);
      } catch (final Exception e) {
        throw new DestructionException(e.getMessage(), e);
      }
    } else if (i instanceof AutoCloseable ac) {
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

  /**
   * Returns {@code true} if this {@link Factory} implementation {@linkplain #destroy(Object, Destruction) destroys} its
   * {@linkplain #create(Creation) created} contextual instances in some way, or {@code false} if it does not.
   *
   * <p>The default implementation of this method returns {@code true}.</p>
   *
   * <p>Overrides of this method must be idempotent and return a determinate value.</p>
   *
   * @return {@code true} if this {@link Factory} implementation {@linkplain #destroy(Object, Destruction) destroys} its
   * {@linkplain #create(Creation) created} contextual instances in some way; {@code false} otherwise
   *
   * @see #destroy(Object, Destruction)
   */
  public default boolean destroys() {
    return true;
  }

  /**
   * Returns the sole contextual instance of this {@link Factory}'s type, if there is one, or {@code null} in the very
   * common case that there is not.
   *
   * <p>The default implementation of this method returns {@code null}.</p>
   *
   * <p>Overrides of this method should not call {@link #create(Creation)}.</p>
   *
   * <p>Overrides of this method must be idempotent and must return a determinate value.</p>
   *
   * @return the sole contextual instance of this {@link Factory}'s type, or (commonly) {@code null}
   */
  public default I singleton() {
    return null;
  }

}
