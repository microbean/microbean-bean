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

import java.util.function.BiFunction;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.util.stream.Collectors;

import javax.lang.model.type.TypeMirror;

import org.microbean.lang.JavaLanguageModel;

import static java.util.stream.Collectors.toUnmodifiableSet;

import static org.microbean.bean.Qualifiers.anyAndDefaultQualifiers;
import static org.microbean.bean.Qualifiers.defaultQualifier;

import static org.microbean.scope.Scope.SINGLETON_ID;

public final class Beans implements BeanProvider<Beans>, BeanSet {


  /*
   * Instance fields.
   */


  private final Set<Bean<?>> beans;

  private final Alternate.Resolver<Bean<?>> resolver;

  private final ConcurrentMap<Selector<?>, Set<Bean<?>>> selectionCache;

  private final ConcurrentMap<Selector<?>, Bean<?>> resolutionCache;


  /*
   * Constructors.
   */


  public Beans(final Set<? extends Bean<?>> beans) {
    this(beans, Resolver.INSTANCE);
  }
  
  public Beans(final Set<? extends Bean<?>> beans, final Alternate.Resolver<Bean<?>> resolver) {
    super();
    this.resolver = resolver == null ? Resolver.INSTANCE : resolver;
    final JavaLanguageModel jlm = new JavaLanguageModel();
    final Selector<?> s = new Selector<>(jlm.type(BeanSet.class), List.of(defaultQualifier()), true);
    if (beans.isEmpty()) {
      final Bean<Beans> me = this.bean();
      this.beans = Set.of(me);
      this.selectionCache = new ConcurrentHashMap<>(1);
      this.resolutionCache = new ConcurrentHashMap<>(1);
      this.selectionCache.put(s, this.beans);
      this.resolutionCache.put(s, me);
    } else if (beans.stream().anyMatch(s::selects)) {
      // The supplied Set of Beans contained a Bean<BeanSet>, but there's not really anything we can do about it because
      // we don't traffic in instances, so we can't instantiate it. See DefaultBeanContext, which *can* do something
      // about it.
      this.beans = Set.copyOf(beans);
      this.selectionCache = new ConcurrentHashMap<>(beans.size());
      this.resolutionCache = new ConcurrentHashMap<>(beans.size());
    } else {
      final Bean<Beans> me = this.bean();
      final Set<Bean<?>> set = new HashSet<>(beans);
      set.add(me);
      this.selectionCache = new ConcurrentHashMap<>(beans.size() + 1);
      this.resolutionCache = new ConcurrentHashMap<>(beans.size() + 1);
      this.selectionCache.put(s, Set.of(me));
      this.resolutionCache.put(s, me);
      this.beans = Collections.unmodifiableSet(set);
    }
  }


  /*
   * Instance methods.
   */


  @Override // BeanProvider<Beans>
  public final Bean<Beans> bean() {
    final JavaLanguageModel jlm = new JavaLanguageModel();
    return
      new Bean<>(new Id(new ReferenceTypeList(List.of(jlm.type(Beans.class),
                                                      jlm.type(BeanSet.class),
                                                      jlm.type(null, BeanProvider.class, Beans.class))),
                        anyAndDefaultQualifiers(),
                        SINGLETON_ID),
                 c -> Beans.this);
  }

  @Override // BeanSet
  public final Bean<?> bean(final Selector<?> selector) {
    return this.bean(selector, Alternate.Resolver::fail);
  }

  @Override // BeanSet
  public final Bean<?> bean(final Selector<?> selector,
                            final BiFunction<? super Selector<?>, ? super Collection<? extends Bean<?>>, ? extends Bean<?>> op) {
    return this.resolutionCache.computeIfAbsent(selector, s -> this.resolver.resolve(s, this.beans(s), op));
  }

  @Override // BeanSet
  public final Set<Bean<?>> beans() {
    return this.beans;
  }

  @Override // BeanSet
  public final Set<Bean<?>> beans(final Selector<?> selector) {
    return this.selectionCache.computeIfAbsent(selector, s -> this.beans.stream().filter(s::selects).collect(toUnmodifiableSet()));
  }


  /*
   * Inner and nested classes.
   */


  private static final class Resolver implements Alternate.Resolver<Bean<?>> {

    private static final Resolver INSTANCE = new Resolver();

    private Resolver() {
      super();
    }

  }

}
