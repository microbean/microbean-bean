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
 * A skeletal {@link Factory} implementation intended primarily for generated subclasses.
 *
 * @author <a href="https://about.me/lairdnelson" target="_parent">Laird Nelson</a>
 *
 * @see Factory
 */
// This class may be used by various bytecode generators so change it with extraordinary care.
public abstract class AbstractGeneratedFactory<I> implements Constable, Factory<I> {


  /*
   * Constructors.
   */


  protected AbstractGeneratedFactory() {
    super();
  }


  /*
   * Instance methods.
   */


  protected abstract boolean dependentProducer(final Creation<?> c);

  // To be called by generated code that knows whether it has transient references or not.
  // c really should not be null.
  protected final I produce(final Creation<I> c, final boolean hasTransientReferences) {
    if (hasTransientReferences) {
      final Creation<?> transientC = c == null ? null : c.clone();
      try {
        return this.dependentProducer(c) ? this.produce(transientC, c, transientC) : this.produce(c, c, transientC);
      } finally {
        if (transientC != null) {
          transientC.destruction().close();
        }
      }
    } else if (this.dependentProducer(c)) {
      final Creation<?> transientC = c == null ? null : c.clone();
      try {
        return this.produce(transientC, c, null);
      } finally {
        if (transientC != null) {
          transientC.destruction().close();
        }
      }
    } else {
      return this.produce(c, c, null);
    }
  }

  // Called by #produce(Creation, boolean) above.
  protected abstract I produce(final Creation<?> producerC,
                               final Creation<I> c,
                               final Creation<?> transientC);

  // c really should not be null.
  protected final I initialize(final I i, final Creation<I> c, final boolean hasTransientReferences) {
    if (hasTransientReferences) {
      final Creation<?> transientC = c == null ? null : c.clone();
      try {
        return this.initialize(i, c, transientC);
      } finally {
        if (transientC != null) {
          transientC.destruction().close();
        }
      }
    }
    return this.initialize(i, c, null);
  }

  protected abstract I initialize(final I i, final Creation<I> c, final Creation<?> transientC);

  /**
   * Destroys the supplied instance by first calling the {@link #destroyWithoutClosing(Object, Destruction)} method and
   * then calling the {@link Destruction#close()} method as if in a {@code finally} block.
   *
   * @param i the instance to destroy; may be {@code null}
   *
   * @param d the {@link Destruction} in effect; must not be {@code null}
   *
   * @exception NullPointerException if {@code d} is {@code null}
   *
   * @threadsafety This method is safe for concurrent use by multiple threads.
   *
   * @idempotency This method is neither idempotent nor deterministic.
   *
   * @see #destroyWithoutClosing(Object, Destruction)
   */
  @Override // Factory<I>
  public final void destroy(final I i, final Destruction d) {
    try {
      this.destroyWithoutClosing(i, d);
    } finally {
      if (d != null) {
        d.close();
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
   * @param d the {@link Destruction} in effect; must not be {@code null}
   *
   * @exception NullPointerException if {@code bc} is {@code null}
   *
   * @threadsafety This method is safe for concurrent use by multiple threads.
   *
   * @idempotency This method is neither idempotent nor deterministic.
   *
   * @see #destroy(Object)
   */
  protected void destroyWithoutClosing(final I i, final Destruction d) {
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
