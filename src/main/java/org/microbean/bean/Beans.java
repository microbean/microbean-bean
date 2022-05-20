/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2022 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.microbean.type.JavaType;
import org.microbean.type.Type;

import static org.microbean.scope.Scope.SINGLETON;

/**
 * A read-only repository of {@link Bean} instances.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see Bean
 *
 * @see Resolver
 */
public final class Beans implements AutoCloseable {


  /*
   * Instance fields.
   */


  private final List<Bean<?>> beans;

  private final Resolver resolver;

  private final ConcurrentMap<Selector, List<Bean<?>>> cache;


  /*
   * Constructors.
   */


  public Beans() {
    this(CompositeBeanListSource.of().beanList());
  }
  
  public Beans(final Collection<? extends Bean<?>> beans) {
    this(beans, new Resolver());
  }

  public Beans(final Collection<? extends Bean<?>> beans, final Resolver resolver) {
    super();
    this.cache = new ConcurrentHashMap<>();
    this.resolver = resolver == null ? new Resolver() : resolver;
    final Bean<?> resolverBean = this.resolver instanceof BeanSource bs ? bs.bean() : Bean.of(this.resolver);
    if (beans == null || beans.isEmpty()) {
      this.beans = List.of(Bean.of(this), resolverBean);
    } else {
      final ArrayList<Bean<?>> newBeans = new ArrayList<>(beans.size() + 2);
      newBeans.add(Bean.of(this));
      newBeans.add(resolverBean);
      newBeans.addAll(beans);
      this.beans = Collections.unmodifiableList(newBeans);
    }
  }


  /*
   * Instance methods.
   */

  
  /**
   * Returns a {@link Stream} of {@link Bean} instances contained by
   * this {@link Beans}.
   *
   * <p>All characteristics of the returned {@link Stream} are
   * deliberately undefined.</p>
   *
   * @return a {@link Stream} of {@link Bean} instances conained by
   * this {@link Beans}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public final Stream<Bean<?>> beans() {
    return this.beans.stream();
  }

  /**
   * Returns a {@link Stream} of {@link Bean} instances {@linkplain
   * Selector#selects(Bean) selected by} the supplied {@link
   * Selector}.
   *
   * <p>All characteristics of the returned {@link Stream} are
   * deliberately undefined.</p>
   *
   * @param selector a {@link Selector}; may be {@code null} in which
   * case the result of an invocation of the {@link #beans()} method
   * will be returned
   *
   * @return a {@link Stream} of {@link Bean} instances {@linkplain
   * Selector#selects(Bean) selected by} the supplied {@link
   * Selector}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public final Stream<Bean<?>> beans(final Selector selector) {
    return selector == null ? this.beans() : this.cache.computeIfAbsent(selector, this::computeCachedBeans).stream();
  }

  /**
   * Returns the sole {@link Bean} both {@linkplain
   * Selector#selects(Bean) selected by} the supplied {@link Selector}
   * and {@linkplain Resolver#resolve(Selector, Stream) resolved by}
   * this {@link Beans}'s {@link Resolver}.
   *
   * @param selector a {@link Selector}; may be {@code null}
   *
   * @return the sole {@link Bean} both {@linkplain
   * Selector#selects(Bean) selected by} the supplied {@link Selector}
   * and {@linkplain Resolver#resolve(Selector, Stream) resolved by}
   * this {@link Beans}'s {@link Resolver}; depending on the {@link
   * Resolver} in question may be {@code null}
   *
   * @exception AmbiguousResolutionException if the {@link Resolver}
   * supplied {@linkplain #Beans(Collection, Resolver) at construction
   * time} throws an {@link AmbiguousResolutionException}
   *
   * @exception UnsatisfiedResolutionException if the {@link Resolver}
   * supplied {@linkplain #Beans(Collection, Resolver) at construction
   * time} throws an {@link UnsatisfiedResolutionException}
   *
   * @nullability This method may return {@code null} depending on the
   * {@link Resolver} {@linkplain #Beans(List, Resolver) supplied at
   * construction time}.
   *
   * @idempotency This method is as deterministic and idempotent as the
   * {@link Resolver} {@linkplain #Beans(List, Resolver) supplied at
   * construction time}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see Resolver#Resolver(BiFunction)
   *
   * @see Resolver#resolve(Selector, Stream)
   */
  public final Bean<?> bean(final Selector selector) {
    return this.resolver.resolve(selector, this.beans(selector));
  }

  /**
   * Calls the {@link #clear()} method.
   *
   * @idempotency This method is deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #clear()
   */
  @Override // AutoCloseable
  public final void close() {
    this.clear();
  }

  /**
   * Clears internal caches.
   *
   * <p>This {@link Beans} remains functional after this method is
   * called.</p>
   *
   * @idempotency This method is deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public final void clear() {
    this.cache.clear();
  }

  /**
   * Returns a {@link Predicate} representing the filtering behavior
   * modeled by the supplied {@link Selector}.
   *
   * <p>The default implementation of this method returns a method
   * reference to the {@link Selector#selects(Bean)} method.</p>
   *
   * @param s the {@link Selector} to represent; must not be {@code
   * null}
   *
   * @return a {@link Predicate}
   *
   * @exception NullPointerException if {@code s} is {@code null}
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @idempotency This method is both idempotent and deterministic but
   * its overrides need not be either.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @see Selector#selects(Bean)
   */
  protected Predicate<? super Bean<?>> predicate(final Selector s) {
    return s::selects;
  }

  private final List<Bean<?>> computeCachedBeans(final Selector s) {
    return this.beans().filter(this.predicate(s)).toList();
  }

}
