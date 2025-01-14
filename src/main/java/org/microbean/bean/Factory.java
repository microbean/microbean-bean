/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2025 microBean™.
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

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;

import java.util.Optional;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;

/**
 * A creator and destroyer of contextual instances of a particular type.
 *
 * @param <I> the type of the contextual instances this {@link Factory} creates and destroys
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 */
@FunctionalInterface
public interface Factory<I> extends Aggregate, Constable {

  /**
   * Creates a new contextual instance, possibly using the supplied {@link Request}, if it is non-{@code null}, to
   * acquire its {@linkplain #dependencies() dependencies}.
   *
   * <p>Implementations of this method must not call {@link #singleton()}.</p>
   *
   * <p>Implementations of this method should consider calling {@link Creation#created(Object)} on the supplied {@link
   * Request} with the contextual instance about to be returned.</p>
   *
   * @param r a {@link Request} responsible for the demand for creation and used for {@linkplain ReferenceSelector
   * acquiring any needed dependencies}; <strong>may be {@code null}</strong> in early, uncommon bootstrap-like
   * situations
   *
   * @return a new contextual instance, or {@code null}
   *
   * @exception CreationException if an error occurs
   *
   * @see Request
   *
   * @see ReferenceSelector
   *
   * @see Creation
   */
  public I create(final Request<I> r);

  /**
   * Returns the sole contextual instance of this {@link Factory}'s type, if there is one, or {@code null} in the very
   * common case that there is not.
   *
   * <p>The default implementation of this method returns {@code null}.</p>
   *
   * <p>Overrides of this method should not call {@link #create(Request)}.</p>
   *
   * <p>Overrides of this method must be idempotent and must return a determinate value.</p>
   *
   * @return the sole contextual instance of this {@link Factory}'s type, or (commonly) {@code null}
   */
  public default I singleton() {
    return null;
  }

  /**
   * Returns {@code true} if this {@link Factory} implementation destroys its {@linkplain #create(Request) created}
   * contextual instances in some way, or {@code false} if it does not.
   *
   * <p>The default implementation of this method returns {@code true}.</p>
   *
   * <p>Overrides of this method must be idempotent and return a determinate value.</p>
   *
   * @return {@code true} if this {@link Factory} implementation destroys its {@linkplain #create(Request) created}
   * contextual instances in some way; {@code false} otherwise
   *
   * @see #destroy(Object, Request)
   */
  public default boolean destroys() {
    return true;
  }

  /**
   * Destroys the supplied contextual instance.
   *
   * @param i the contextual instance; may be {@code null} if this {@link Factory} suports returning {@code null} from
   * its {@link #create(Request)} method
   *
   * @param creationRequest the {@link Request} that was {@linkplain #create(Request) present at creation time}; may be
   * {@code null} if it was {@code null} at creation time
   */
  // MUST be idempotent
  // If i is an AutoCloseable, MUST be idempotent
  public default void destroy(final I i, final Request<I> creationRequest) {
    if (i instanceof AutoCloseable ac) {
      try {
        ac.close();
      } catch (final RuntimeException | Error re) {
        throw re;
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new DestructionException(e.getMessage(), e);
      } catch (final Exception e) {
        throw new DestructionException(e.getMessage(), e);
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
   * @microbean.threadsafety This method is safe for concurrent use by multiple threads.
   *
   * @microbean.idempotency This method is neither idempotent nor deterministic.
   */
  @Override // Constable
  public default Optional<? extends ConstantDesc> describeConstable() {
    return this.getClass()
      .describeConstable()
      .map(classDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                               MethodHandleDesc.ofConstructor(classDesc)));
  }

}
