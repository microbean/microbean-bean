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

import java.util.Collections;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;

import org.microbean.bean.Alternate.Resolver;

import org.microbean.constant.Constables;

import org.microbean.lang.TypeAndElementSource;

import static java.lang.Integer.signum;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_Collection;
import static java.lang.constant.ConstantDescs.CD_Map;

import static java.util.Collections.unmodifiableSequencedSet;

import static java.util.function.Predicate.not;

import static org.microbean.bean.ConstantDescs.CD_Assignability;
import static org.microbean.bean.ConstantDescs.CD_DefaultBeanSet;
import static org.microbean.bean.ConstantDescs.CD_Resolver;
import static org.microbean.bean.Qualifiers.anyAndDefaultQualifiers;
import static org.microbean.bean.Qualifiers.defaultQualifiers;
import static org.microbean.bean.Ranked.DEFAULT_RANK;

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

  private final Assignability assignability;

  private final SequencedSet<Bean<?>> beans;

  // A cache of Beans that were selected by a BeanSelectionCriteria
  private final ConcurrentMap<BeanSelectionCriteria, SequencedSet<Bean<?>>> selectionCache;

  // Guaranteed to be a submap of selectionCache.
  private final ConcurrentMap<BeanSelectionCriteria, Bean<?>> resolutionCache;


  /*
   * Constructors.
   */


  public DefaultBeanSet(final Collection<? extends Bean<?>> beans) {
    this(new Assignability(), beans, Map.of(), StockResolver.INSTANCE);
  }

  public DefaultBeanSet(final Assignability assignability, final Collection<? extends Bean<?>> beans) {
    this(assignability, beans, Map.of(), StockResolver.INSTANCE);
  }

  @SuppressWarnings("this-escape")
  public DefaultBeanSet(final Assignability assignability,
                        final Collection<? extends Bean<?>> beans,
                        final Map<? extends BeanSelectionCriteria, ? extends Bean<?>> preCalculatedResolutions,
                        final Resolver resolver) {
    super();
    this.assignability = assignability == null ? new Assignability() : assignability;
    this.resolver = resolver == null ? StockResolver.INSTANCE : resolver;
    this.resolutionCache = new ConcurrentHashMap<>();
    this.selectionCache = new ConcurrentHashMap<>();

    final List<Bean<?>> newBeans = new ArrayList<>(beans.size() + 2 + preCalculatedResolutions.size());
    newBeans.addAll(beans);
    for (final Entry<? extends BeanSelectionCriteria, ? extends Bean<?>> e : preCalculatedResolutions.entrySet()) {
      final BeanSelectionCriteria bsc = e.getKey();
      final Bean<?> b = e.getValue();
      if (!bsc.selects(b)) {
        throw new IllegalArgumentException("preCalculatedResolutions; beanSelectionCriteria (" + bsc + ") does not select bean (" + b + ")");
      }
      newBeans.add(b);
      this.resolutionCache.put(bsc, b);
    }
    newBeans.add(this.resolverBean());
    newBeans.add(this.bean());
    Collections.sort(newBeans, DefaultBeanSet::compareRanks);
    // Second pass to efficiently prime the selection cache now that validation has happened.
    for (final BeanSelectionCriteria bsc : preCalculatedResolutions.keySet()) {
      this.beans(bsc, newBeans);
    }
    this.beans = unmodifiableSequencedSet(new LinkedHashSet<>(newBeans));

    // Prime the selection and resolution caches with our beans.
    final TypeAndElementSource tes = assignability.typeAndElementSource();
    this.bean(new BeanSelectionCriteria(assignability, tes.declaredType(BeanSet.class), defaultQualifiers(), true), DefaultBeanSet::returnNull);
    this.bean(new BeanSelectionCriteria(assignability, tes.declaredType(Resolver.class), defaultQualifiers(), true), DefaultBeanSet::returnNull);
  }


  /*
   * Instance methods.
   */


  @Override // BeanSet
  public final Bean<?> bean(final BeanSelectionCriteria beanSelectionCriteria) {
    return this.bean(beanSelectionCriteria, Resolver::fail);
  }

  @Override // BeanSet
  public final Bean<?> bean(final BeanSelectionCriteria beanSelectionCriteria,
                            final BiFunction<? super BeanSelectionCriteria, ? super Collection<? extends Bean<?>>, ? extends Bean<?>> op) {
    return this.resolutionCache.computeIfAbsent(beanSelectionCriteria, s -> this.resolver.resolve(s, this.beans(s), op)); // volatile read
  }

  @Override // BeanSet
  public final SequencedSet<Bean<?>> beans() {
    return this.beans;
  }

  @Override // BeanSet
  public final SequencedSet<Bean<?>> beans(final BeanSelectionCriteria beanSelectionCriteria) {
    return this.beans(beanSelectionCriteria, this.beans);
  }

  @Override // Constable
  public Optional<DynamicConstantDesc<DefaultBeanSet>> describeConstable() {
    return this.assignability.describeConstable()
      .flatMap(aDesc -> Constables.describeConstable(this.beans)
               .flatMap(beansDesc -> Constables.describeConstable(this.resolutionCache)
                        .flatMap(pcrDesc -> Constables.describeConstable(this.resolver)
                                 .map(resolverDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                                             MethodHandleDesc.ofConstructor(CD_DefaultBeanSet,
                                                                                                            CD_Assignability,
                                                                                                            CD_Collection,
                                                                                                            CD_Map,
                                                                                                            CD_Resolver),
                                                                             aDesc,
                                                                             beansDesc,
                                                                             pcrDesc,
                                                                             resolverDesc)))));
  }

  public final Resolver resolver(final Resolver r) {
    return (Resolver)RESOLVER.getAndSet(this, Objects.requireNonNullElse(r, StockResolver.INSTANCE));
  }

  /*
   * Reporting- and snapshot-oriented methods.
   */

  public final Set<BeanSelectionCriteria> beanSelectionCriterias() {
    return this.selectionCache.keySet();
  }

  public final SequencedSet<Bean<?>> resolvedBeans() {
    return this.resolutionCache.values().stream().collect(new BeanCollector());
  }

  public final SequencedSet<Bean<?>> selectedBeans() {
    return this.selectionCache.values().stream().flatMap(Set::stream).collect(new BeanCollector());
  }

  public final SequencedSet<Bean<?>> unselectedBeans() {
    final SequencedSet<Bean<?>> selectedBeans = this.selectedBeans();
    return this.beans().stream().filter(not(selectedBeans::contains)).collect(new BeanCollector());
  }

  /*
   * Private methods.
   */

  private final Bean<DefaultBeanSet> bean() {
    final TypeAndElementSource tes = this.assignability.typeAndElementSource();
    return
      new Bean<>(new Id(new BeanTypeList(List.of(tes.declaredType(DefaultBeanSet.class),
                                                 tes.declaredType(BeanSet.class))),
                        anyAndDefaultQualifiers(),
                        SINGLETON_ID,
                        DEFAULT_RANK - 100),
                 new Singleton<>(this));
  }

  private final SequencedSet<Bean<?>> beans(final BeanSelectionCriteria beanSelectionCriteria,
                                            final Collection<? extends Bean<?>> beans) {
    return
      this.selectionCache.computeIfAbsent(beanSelectionCriteria,
                                          bsc -> beans.stream().filter(bsc::selects).collect(new BeanCollector()));
  }

  private final Bean<Resolver> resolverBean() {
    final TypeAndElementSource tes = this.assignability.typeAndElementSource();
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


  private static final int compareRanks(final Ranked b0, final Ranked b1) {
    return signum(b0.rank() - b1.rank());
  }

  private static final Bean<?> returnNull(final BeanSelectionCriteria x, final Collection<? extends Bean<?>> xx) {
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

    @Override // Constable
    public final Optional<DynamicConstantDesc<StockResolver>> describeConstable() {
      return Optional.empty();
    }

  }

  private static final class BeanCollector implements Collector<Bean<?>, LinkedHashSet<Bean<?>>, SequencedSet<Bean<?>>> {

    private BeanCollector() {
      super();
    }

    @Override // Collector<Bean<?>, LinkedHashSet<Bean<?>>, SequencedSet<Bean<?>>>
    public final BiConsumer<LinkedHashSet<Bean<?>>, Bean<?>> accumulator() {
      return LinkedHashSet::add;
    }

    @Override // Collector<Bean<?>, LinkedHashSet<Bean<?>>, SequencedSet<Bean<?>>>
    public final Set<Characteristics> characteristics() {
      return Set.of();
    }

    @Override // Collector<Bean<?>, LinkedHashSet<Bean<?>>, SequencedSet<Bean<?>>>
    public final BinaryOperator<LinkedHashSet<Bean<?>>> combiner() {
      return BeanCollector::merge;
    }

    @Override // Collector<Bean<?>, LinkedHashSet<Bean<?>>, SequencedSet<Bean<?>>>
    public final Function<LinkedHashSet<Bean<?>>, SequencedSet<Bean<?>>> finisher() {
      return Collections::unmodifiableSequencedSet;
    }

    @Override // Collector<Bean<?>, LinkedHashSet<Bean<?>>, SequencedSet<Bean<?>>>
    public final Supplier<LinkedHashSet<Bean<?>>> supplier() {
      return LinkedHashSet::new;
    }

    private static final <T, S extends Collection<T>> S merge(final S s0, final S s1) {
      s0.addAll(s1);
      return s0;
    }


  }

}
