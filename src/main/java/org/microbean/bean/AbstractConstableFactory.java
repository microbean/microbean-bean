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

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;

import java.util.Optional;

import java.util.function.Supplier;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.MethodHandleDesc.ofConstructor;

/**
 * A skeletal {@link Factory} implementation that requires its implementations to also be {@link Constable}
 * implementations.
 *
 * <p><strong>Please note that implementing {@link Constable} forces stringent requirements upon
 * subclasses.</strong></p>
 *
 * @author <a href="https://about.me/lairdnelson" target="_parent">Laird Nelson</a>
 *
 * @see #describeConstable()
 *
 * @see Constable
 *
 * @see Factory
 */
// This class is used by various bytecode generators so change it with
// extraordinary care.
public abstract class AbstractConstableFactory<I> implements Constable, Factory<I> {


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link AbstractConstableFactory}.
   */
  protected AbstractConstableFactory() {
    super();
  }


  /*
   * Instance methods.
   */


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
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is neither idempotent nor deterministic.
   */
  @Override // Constable
  public Optional<? extends ConstantDesc> describeConstable() {
    return this.getClass().describeConstable().map(c -> DynamicConstantDesc.of(BSM_INVOKE, ofConstructor(c)));
  }

  protected abstract boolean dependentProducer(final Creation<?> bc);

  // To be called by generated code.
  protected final I produce(final Creation<I> bc, final boolean hasTransientReferences) {
    if (hasTransientReferences) {
      final Creation<?> transientBc = bc.clone();
      try {
        return this.dependentProducer(bc) ? this.produce(transientBc, bc, transientBc) : this.produce(bc, bc, transientBc);
      } finally {
        transientBc.destruction().close();
      }
    } else if (this.dependentProducer(bc)) {
      final Creation<?> transientBc = bc.clone();
      try {
        return this.produce(transientBc, bc, null);
      } finally {
        transientBc.destruction().close();
      }
    } else {
      return this.produce(bc, bc, null);
    }
  }

  // Called by #produce(Creation, boolean) above.
  protected abstract I produce(final Creation<?> producerBc,
                               final Creation<I> bc,
                               final Creation<?> transientBc);

  protected final I initialize(final I i, final Creation<I> bc, final boolean hasTransientReferences) {
    if (hasTransientReferences) {
      final Creation<?> transientBc = bc.clone();
      try {
        return this.initialize(i, bc, transientBc);
      } finally {
        transientBc.destruction().close();
      }
    }
    return this.initialize(i, bc, null);
  }

  protected abstract I initialize(final I i, final Creation<I> bc, final Creation<?> transientBc);

  /**
   * Destroys the supplied instance by first calling the {@link #destroyWithoutClosing(Object, Creation)} method and
   * then calling the {@link BeanContext#close()} method as if in a {@code finally} block.
   *
   * @param i the instance to destroy; may be {@code null}
   *
   * @param bc the {@link Destruction} in effect; must not be {@code null}
   *
   * @exception NullPointerException if {@code bc} is {@code null}
   *
   * @threadsafety This method is safe for concurrent use by multiple threads.
   *
   * @idempotency This method is neither idempotent nor deterministic.
   *
   * @see #destroyWithoutClosing(Object, Destruction)
   */
  @Override // Factory<I>
  public final void destroy(final I i, final Destruction bc) {
    try {
      this.destroyWithoutClosing(i, bc);
    } finally {
      if (bc != null) {
        bc.close();
      }
    }
  }

  /**
   * Destroys the supplied instance.
   *
   * <p>The default implementation simply calls {@link #destroy(Object)}.<p>
   *
   * <p>Overrides must not call {@link Destruction#close()}.</p>
   *
   * <p>Overrides must not call {@link #destroy(Object, Destruction)}.</p>
   *
   * @param i the instance to destroy; may be {@code null}
   *
   * @param bc the {@link Destruction} in effect; must not be {@code null}
   *
   * @exception NullPointerException if {@code bc} is {@code null}
   *
   * @threadsafety This method is safe for concurrent use by multiple threads.
   *
   * @idempotency This method is neither idempotent nor deterministic.
   *
   * @see #destroy(Object)
   */
  protected void destroyWithoutClosing(final I i, final Destruction bc) {
    this.destroy(i);
  }

  @Override // Object
  public final int hashCode() {
    return super.hashCode();
  }

  @Override // Object
  public final boolean equals(final Object other) {
    return super.equals(other);
  }

}
