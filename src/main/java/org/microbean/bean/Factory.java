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
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;

import java.util.Optional;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;

@FunctionalInterface // see #produce(Creation)
public interface Factory<I> extends Constable {


  /*
   * Static fields.
   */


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

  public default I singleton() {
    return null;
  }

  /**
   * Returns a contextual instance equivalent to that which would be returned by an invocation of the following: {@code
   * return }{@link #intercept(Object, Creation) intercept}{@code (}{@link #initialize(Object, Creation)
   * initialize}{@code (}{@link #interceptedProduce(Creation) interceptedProduce}{@code (c), c), c);}
   *
   * <p>The default implementation performs exactly that invocation.  Implementations of the {@link Factory} interface
   * are discouraged from overriding this default implementation.</p>
   *
   * <p>Callers in need of contextual instances will typically call this method and {@link #singleton()}.  Other
   * production-related methods in this interface are more special-purpose and are typically not called directly by
   * external callers.</p>
   *
   * <p>Implementations of this method must not call any of the following methods or undefined behavior may result:</p>
   *
   * <ul>
   *
   * <li>{@link #destroy(Object)}</li>
   *
   * <li>{@link #destroy(Object, Destruction)}</li>
   *
   * </ul>
   *
   * @param c the {@link Creation} describing the context of the creation request; must not be {@code null}
   *
   * @return a new or cached contextual instance, or {@code null}
   *
   * @exception NullPointerException if {@code c} is {@code null}
   *
   * @nullability Implementations of this method may return {@code
   * null}.
   *
   * @threadsafety Implementations of this method must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency Implementations of this method may not be idempotent
   * or deterministic.
   *
   * @see #intercept(Object, Creation)
   *
   * @see #initialize(Object, Creation)
   *
   * @see #interceptedProduce(Creation)
   *
   * @see #produce(Creation)
   */
  // i.e. run the whole thing: constructor interception, if any, initializer methods, if any,
  // post-construct/initialized() stuff, then wrap it with an interception proxy if needed and return it
  public default I create(final Creation<I> c) {
    final I s = this.singleton();
    return s == null ? this.intercept(this.initialized(this.initialize(this.interceptedProduce(c), c), c), c) : s;
  }

  /**
   * Applies <strong>constructor-level interception semantics</strong>, if any, to the return value of the {@link
   * #produce(Creation)} method and returns the result.
   *
   * <p>The default implementation of this method simply invokes {@link #produce(Creation)} and returns its result.</p>
   *
   * <p>Very few implementations of the {@link Factory} interface will find a need to override this default
   * implementation.</p>
   *
   * <p>Implementations of this method must not call any of the following methods or undefined behavior may result:</p>
   *
   * <ul>
   *
   * <li>{@link #create(Creation)}</li>
   *
   * <li>{@link #intercept(Object, Creation)}</li>
   *
   * <li>{@link #initialize(Object, Creation)}</li>
   *
   * <li>{@link #destroy(Object)}</li>
   *
   * <li>{@link #destroy(Object, Destruction)}</li>
   *
   * </ul>
   *
   * @param c the {@link Creation} describing the context of the creation request; must not be {@code null}
   *
   * @return a new or cached contextual instance, together with the machinery responsible for applying constructor-level
   * interception, if any, or {@code null}
   *
   * @exception NullPointerException if {@code c} is {@code null}
   *
   * @nullability Implementations of this method may return {@code null}.
   *
   * @threadsafety Implementations of this method must be safe for concurrent use by multiple threads.
   *
   * @idempotency Implementations of this method may not be idempotent or deterministic.
   *
   * @see #produce(Creation)
   */
  // i.e. do constructor interception to make an I
  public default I interceptedProduce(final Creation<I> c) {
    return this.produce(c); // basically do nothing
  }

  /**
   * Augments a contextual instance (usually by some kind of proxying machinery) such that method-level interception
   * semantics, if any, will be applied to relevant method invocations on that contextual instance, and returns the
   * augmented contextual instance.
   *
   * <p>The default implementation of this method simply returns the supplied contextual instance as-is.</p>
   *
   * <p>Very few implementations of the {@link Factory} interface will find a need to override this default
   * implementation.</p>
   *
   * <p>Implementations of this method must not call any of the following methods or undefined behavior may result:</p>
   *
   * <ul>
   *
   * <li>{@link #create(Creation)}</li>
   *
   * <li>{@link #interceptedProduce(Creation)}</li>
   *
   * <li>{@link #produce(Creation)}</li>
   *
   * <li>{@link #destroy(Object)}</li>
   *
   * <li>{@link #destroy(Object, Destruction)}</li>
   *
   * </ul>
   *
   * @param i the contextual instance to augment; may be {@code null}
   *
   * @param c the {@link Creation} describing the context of the creation request; must not be {@code null}
   *
   * @return an augmented contextual instance derived from (or identical to) the supplied contextual instance with
   * method-level interception semantics applied, or {@code null}
   *
   * @exception NullPointerException if {@code c} is {@code null}
   *
   * @nullability Implementations of this method may return {@code null}.
   *
   * @threadsafety Implementations of this method must be safe for concurrent use by multiple threads.
   *
   * @idempotency Implementations of this method may not be idempotent or deterministic.
   */
  // i.e. make a proxy for i (not a client proxy, but an interception proxy)
  public default I intercept(final I i, final Creation<I> c) {
    return i; // do nothing by default
  }

  /**
   * Initializes the supplied instance by configuring it in some way, or, if initialization is not appropriate, simply
   * returns the supplied instance as-is.
   *
   * <p>The default implementation of this method simply returns {@code instance}.</p>
   *
   * <p>Implementors should expect an invocation of this method to be supplied with the result of an immediately prior
   * invocation of the {@link #interceptedProduce(Creation)} method on the same thread.</p>
   *
   * <p>Implementations of this method that do not support any initialization must simply return the supplied {@code
   * instance}.</p>
   *
   * @param i the instance to initialize; usually from an immediately prior call to {@link #produce(Creation)}; may be
   * {@code null}
   *
   * @param c a {@link Creation} associated with the instance; must not be {@code null}
   *
   * @return an initialized instance corresponding to the supplied instance; normally the same instance after, e.g.,
   * dependency injection or other initialization or configuration
   *
   * @exception NullPointerException if {@code c} is {@code null}
   *
   * @nullability Implementations of this method may return {@code null} but certain scopes may not be prepared for
   * this.
   *
   * @threadsafety Implementations of this method are not expected to be safe for concurrent use by multiple threads.
   *
   * @idempotency No guarantees about idempotency or determinism are made about implementations of this method.
   */
  // This is experimental because I am deliberately making the most fundamental contract a two-step process, instead of
  // a one-step process.  CDI says that a Contextual<I> "creates" an instance, but what it really does is
  // creates-an-instance-with-constructor-injection-and-then-injects-it-if-it-supports-injection.  I am making that
  // process explicit here, and I'm not entirely sure of what the overall repercussions will be.  I am hoping that it
  // simplifies the CDI need for a Producer/InjectionTarget interface, where an "ordinary" bean is distinguished from a
  // managed bean.
  //
  // This is where "setters" are called.
  public default I initialize(final I i, final Creation<I> c) {
    return i; // do nothing by default
  }

  // This is where "post constructs" are called, maybe?
  public default I initialized(final I i, final Creation<I> c) {
    return i; // do nothing by default
  }

  /**
   * Creates a new instance, performing constructor injection if necessary, and returns it.
   *
   * <p>Implementors should expect that the result of an invocation of an implementation of this method will be
   * immediately supplied to an invocation of an implementation of the {@link #initialize(Object, Creation)} method.</p>
   *
   * <p>Implementors should expect that the immediate caller of this method will be an implementation of the {@link
   * #interceptedProduce(Creation)} method.</p>
   *
   * @param c a {@link Creation} to associate with the creation; must not be {@code null}
   *
   * @return a new instance ready to be {@linkplain #initialize(Object, Creation) initialized}, or {@code null}
   *
   * @exception NullPointerException if {@code c} is {@code null}
   *
   * @nullability Implementations of this method may return {@code null} but certain scopes may not be prepared for
   * this.
   *
   * @threadsafety Implementations of this method are not expected to be safe for concurrent use by multiple threads.
   *
   * @idempotency No guarantees about idempotency or determinism are made about implementations of this method.
   *
   * @see #initialize(Object, Creation)
   */
  public I produce(final Creation<I> c);

  // Is custom destruction necessary?  Or can we just ignore it?  It's necessary by default because we don't know what
  // this Factory implementation is doing.  Many Factory instances will return false here which is just fine.
  public default boolean destroys() {
    return true; // by default
  }

  // Users call this and it calls #destroy(Object).
  public default void destroy(final I i, final Destruction d) {
    if (d == null) {
      this.destroying(i, null);
    } else {
      try (d) {
        this.destroy(this.destroying(i, d.references()));
      }
    }
  }

  // MUST NOT call destroy(I) or destroy(I, Destruction).
  //
  // This is where "pre destroys" happen.
  public default I destroying(final I i, final References<?> r) {
    return i;
  }

  // Users should NOT call this.
  //
  // MUST NOT call destroy(I, Destruction).  It's the other way around.
  public default void destroy(final I i) {
    if (i instanceof AutoCloseable ac) {
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
    }
  }



  // Purely for convenience. May delete later.
  public static <I> Factory<I> of(final I singleton) {
    return new SingletonFactory<>(singleton);
  }

}
