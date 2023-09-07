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
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import java.util.function.BiFunction;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.lang.model.type.TypeMirror;

import org.microbean.bean.Alternate.Resolver;

import org.microbean.constant.Constables;

import org.microbean.lang.TypeAndElementSource;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_Collection;

import static java.util.Collections.unmodifiableSet;

import static java.util.function.Predicate.not;

import static java.util.stream.Collectors.toUnmodifiableSet;

import static org.microbean.bean.ConstantDescs.CD_DefaultBeanSet;
import static org.microbean.bean.ConstantDescs.CD_Resolver;
import static org.microbean.bean.Qualifiers.anyAndDefaultQualifiers;
import static org.microbean.bean.Qualifiers.defaultQualifiers;
import static org.microbean.bean.Ranked.DEFAULT_RANK;

import static org.microbean.lang.Lang.typeAndElementSource;

import static org.microbean.scope.Scope.SINGLETON_ID;

// Experimenting with making this non-final so the no-arg constructor can supply its own beans via some other
// mechanism. Think ldc.
public class DefaultBeanSet implements BeanSet, Constable {


  /*
   * Static fields.
   */


  private static final VarHandle RESOLVER;

  static {
    try {
      RESOLVER = MethodHandles.lookup().findVarHandle(DefaultBeanSet.class, "resolver", Resolver.class);
    } catch (final IllegalAccessException | NoSuchFieldException e) {
      throw (Error)new ExceptionInInitializerError(e.getMessage()).initCause(e);
    }
  }


  /*
   * Instance fields.
   */


  private volatile Resolver resolver;

  private final TypeAndElementSource tes;

  private final Set<Bean<?>> beans;

  // A cache of Beans that were selected by a selector
  private final ConcurrentMap<Selector, Set<Bean<?>>> selectionCache;

  // Guaranteed to be a submap of selectionCache.
  private final ConcurrentMap<Selector, Bean<?>> resolutionCache;


  /*
   * Constructors.
   */


  public DefaultBeanSet(final Collection<? extends Bean<?>> beans) {
    this(beans, Map.of(), StockResolver.INSTANCE);
  }

  public DefaultBeanSet(final Assignability assignability, final TypeAndElementSource tes, final Collection<? extends Bean<?>> beans) {
    this(assignability, tes, beans, Map.of(), StockResolver.INSTANCE);
  }

  public DefaultBeanSet(final Collection<? extends Bean<?>> beans, final Resolver resolver) {
    this(beans, Map.of(), resolver);
  }

  public DefaultBeanSet(final Collection<? extends Bean<?>> beans,
                        final Map<? extends Selector, ? extends Bean<?>> preCalculatedResolutions) {
    this(beans, preCalculatedResolutions, StockResolver.INSTANCE);
  }

  public DefaultBeanSet(final Map<? extends Selector, ? extends Bean<?>> preCalculatedResolutions,
                        final Resolver resolver) {
    this(Set.of(), preCalculatedResolutions, resolver);
  }

  public DefaultBeanSet(final Map<? extends Selector, ? extends Bean<?>> preCalculatedResolutions) {
    this(Set.of(), preCalculatedResolutions, StockResolver.INSTANCE);
  }

  public DefaultBeanSet(final Collection<? extends Bean<?>> beans,
                        final Map<? extends Selector, ? extends Bean<?>> preCalculatedResolutions,
                        final Resolver resolver) {
    this(new Assignability(typeAndElementSource()), typeAndElementSource(), beans, preCalculatedResolutions, resolver);
  }

  public DefaultBeanSet(final TypeAndElementSource tes,
                        final Collection<? extends Bean<?>> beans,
                        final Map<? extends Selector, ? extends Bean<?>> preCalculatedResolutions,
                        final Resolver resolver) {
    this(new Assignability(tes), tes, beans, preCalculatedResolutions, resolver);
  }

  public DefaultBeanSet(final Assignability assignability,
                        final TypeAndElementSource tes,
                        final Collection<? extends Bean<?>> beans,
                        final Map<? extends Selector, ? extends Bean<?>> preCalculatedResolutions,
                        final Resolver resolver) {
    super();
    this.resolver = resolver == null ? StockResolver.INSTANCE : resolver;
    this.tes = Objects.requireNonNull(tes, "tes");
    this.resolutionCache = new ConcurrentHashMap<>();
    this.selectionCache = new ConcurrentHashMap<>();

    if (!preCalculatedResolutions.isEmpty()) {
      final Set<Bean<?>> newBeans = new HashSet<>(beans);
      newBeans.add(this.bean());
      newBeans.add(this.resolverBean());
      for (final Entry<? extends Selector, ? extends Bean<?>> e : preCalculatedResolutions.entrySet()) {
        final Selector s = e.getKey();
        final Bean<?> b = e.getValue();
        if (!s.selects(b)) {
          throw new IllegalArgumentException("preCalculatedResolutions; selector (" + s + ") does not select bean (" + b + ")");
        }
        newBeans.add(b);
        this.beans(s, newBeans); // prime the selection cache
        this.resolutionCache.put(s, b); // prime the resolution cache
      }
      this.beans = unmodifiableSet(newBeans);
    } else if (beans.isEmpty()) {
      this.beans = Set.of(this.bean(), this.resolverBean());
    } else {
      final Set<Bean<?>> newBeans = new HashSet<>(beans);
      newBeans.add(this.bean());
      newBeans.add(this.resolverBean());
      this.beans = unmodifiableSet(newBeans);
    }

    // Prime the selection and resolution caches.
    this.bean(new Selector(assignability, tes.declaredType(BeanSet.class), defaultQualifiers()), DefaultBeanSet::returnNull);
    this.bean(new Selector(assignability, tes.declaredType(Resolver.class), defaultQualifiers()), DefaultBeanSet::returnNull);

  }


  /*
   * Instance methods.
   */


  @Override
  public Optional<DynamicConstantDesc<DefaultBeanSet>> describeConstable() {
    return Constables.describeConstable(this.resolver)
      .flatMap(resolverDesc -> Constables.describeConstable(this.beans)
               .map(beansDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                        MethodHandleDesc.ofConstructor(CD_DefaultBeanSet,
                                                                                       CD_Collection,
                                                                                       CD_Resolver),
                                                        beansDesc,
                                                        resolverDesc)));
  }

  public final Resolver resolver(final Resolver r) {
    return (Resolver)RESOLVER.getAndSet(this, r == null ? StockResolver.INSTANCE : r);
  }

  public final Bean<DefaultBeanSet> bean() {
    return
      new Bean<>(new Id(new BeanTypeList(List.of(tes.declaredType(DefaultBeanSet.class),
                                                 tes.declaredType(BeanSet.class))),
                        anyAndDefaultQualifiers(),
                        SINGLETON_ID,
                        DEFAULT_RANK - 100),
                 new Singleton<>(this));
  }

  @Override // BeanSet
  public final Bean<?> bean(final Selector selector) {
    return this.bean(selector, Resolver::fail);
  }

  @Override // BeanSet
  public final Bean<?> bean(final Selector selector,
                            final BiFunction<? super Selector, ? super Collection<? extends Bean<?>>, ? extends Bean<?>> op) {
    return this.resolutionCache.computeIfAbsent(selector, s -> this.resolver.resolve(s, this.beans(s), op)); // volatile read
  }

  @Override // BeanSet
  public final Set<Bean<?>> beans() {
    return this.beans;
  }

  @Override // BeanSet
  public final Set<Bean<?>> beans(final Selector selector) {
    return this.beans(selector, this.beans);
  }

  /*
   * Reporting- and snapshot-oriented methods.
   */

  public final Set<Bean<?>> selectedBeans() {
    return this.selectionCache.values().stream().flatMap(Set::stream).collect(toUnmodifiableSet());
  }

  public final Set<Bean<?>> unselectedBeans() {
    final Set<Bean<?>> selectedBeans = this.selectedBeans();
    return this.beans().stream().filter(not(selectedBeans::contains)).collect(toUnmodifiableSet());
  }

  public final Set<Bean<?>> resolvedBeans() {
    return this.resolutionCache.values().stream().collect(toUnmodifiableSet());
  }

  public final Set<Selector> selectors() {
    return this.selectionCache.keySet();
  }

  /*
   * Private methods.
   */

  private final Set<Bean<?>> beans(final Selector selector, final Set<? extends Bean<?>> beans) {
    return this.selectionCache.computeIfAbsent(selector, s -> beans.stream().filter(s::selects).collect(toUnmodifiableSet()));
  }

  private final Bean<Resolver> resolverBean() {
    return
      new Bean<>(new Id(new BeanTypeList(List.of(tes.declaredType(null, tes.typeElement(Resolver.class)))),
                        anyAndDefaultQualifiers(),
                        SINGLETON_ID,
                        DEFAULT_RANK),
                 new Singleton<>(this.resolver)); // volatile read
  }


  /*
   * Static methods.
   */


  private static final Bean<?> returnNull(final Selector x, final Collection<? extends Bean<?>> xx) {
    return null;
  }


  /*
   * Inner and nested classes.
   */


  private static final class StockResolver implements Constable, Resolver {

    private static final Resolver INSTANCE = new StockResolver();

    private StockResolver() {
      super();
    }

    @Override
    public final Optional<DynamicConstantDesc<StockResolver>> describeConstable() {
      return Optional.empty();
    }

  }

}
