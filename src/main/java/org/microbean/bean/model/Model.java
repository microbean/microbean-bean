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
package org.microbean.bean.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.microbean.assign.Aggregate;
import org.microbean.assign.AttributedElement;
import org.microbean.assign.AttributedType;
import org.microbean.assign.Selectable;

import org.microbean.bean.Bean;

import static java.util.HashMap.newHashMap;

import static java.util.HashSet.newHashSet;

import static java.util.Objects.requireNonNull;

import static java.util.stream.Collectors.toUnmodifiableSet;

public final class Model {

  private final Set<DependencyResolution> dependencyResolutions;

  private volatile boolean valid;

  private Model() {
    this(Set.of());
  }
  
  private Model(Set<? extends DependencyResolution> dependencyResolutions) {
    super();
    if (dependencyResolutions == null || dependencyResolutions.isEmpty()) {
      this.dependencyResolutions = Set.of();
      this.valid = true;
    } else {
      this.dependencyResolutions = Set.copyOf(dependencyResolutions);
    }
  }

  @Override // Object
  public final boolean equals(final Object other) {
    return switch (other) {
    case null -> false;
    case Model m when this.getClass() == m.getClass() -> this.dependencyResolutions.equals(m.dependencyResolutions);
    default -> false;
    };
  }

  @Override // Object
  public final int hashCode() {
    return this.dependencyResolutions.hashCode();
  }

  public final boolean valid() {
    if (this.valid) { // volatile read
      return true;
    }
    for (final DependencyResolution dependencyResolution : this.dependencyResolutions) {
      if (dependencyResolution.beans().size() != 1) {
        return false;
      }
    }
    this.valid = true; // volatile write
    return true;
  }

  public final Set<DependencyResolution> ambiguousDependencyResolutions() {
    return this.dependencyResolutions.stream().filter(DependencyResolution::ambiguous).collect(toUnmodifiableSet());
  }

  public final Set<DependencyResolution> unsatisfiedDependencyResolutions() {
    return this.dependencyResolutions.stream().filter(DependencyResolution::unsatisfied).collect(toUnmodifiableSet());
  }

  // So you can do:
  // org.microbean.assign.Selectables.caching(selectable, model.toSelectionCache());
  public final BiFunction<? super AttributedType, Function<? super AttributedType, ? extends List<Bean<?>>>, ? extends List<Bean<?>>> toSelectionCache() {
    if (!this.valid()) {
      throw new IllegalStateException("not valid");
    }
    final Map<AttributedType, List<Bean<?>>> m = newHashMap(this.dependencyResolutions.size()); // too big but whatever
    for (final DependencyResolution r : this.dependencyResolutions) {
      m.putIfAbsent(r.attributedType(), r.beans());
    }
    return m::computeIfAbsent; // m is mutable on purpose; we can revisit if we decide that a Model is the absolute source of truth
  }

  // s would normally be typesafeFiltering and ambiguityReducing but not necessarily cached
  @SuppressWarnings("unchecked")
  public static Model of(final Selectable<? super AttributedType, ? extends Bean<?>> s) {
    final Collection<? extends Aggregate> allBeans = s.select(null);
    if (allBeans.isEmpty()) {
      return new Model(Set.of());
    }
    final Map<AttributedType, List<Bean<?>>> m2 = newHashMap(allBeans.size() * 5); // estimate;
    final Set<DependencyResolution> dependencyResolutions = newHashSet(allBeans.size() * 5);
    for (final Aggregate bean : allBeans) {
      for (final AttributedElement dependency : bean.dependencies()) {
        dependencyResolutions.add(new DependencyResolution(dependency,
                                 m2.computeIfAbsent(dependency.attributedType(),
                                                    at -> (List<Bean<?>>)s.select(at))));
      }
    }
    return new Model(dependencyResolutions);
  }

  public static final record DependencyResolution(AttributedElement attributedElement, List<Bean<?>> beans) {

    public DependencyResolution {
      requireNonNull(attributedElement, "attributedElement");
      beans = beans == null ? List.of() : List.copyOf(beans);
    }

    public final AttributedType attributedType() {
      return this.attributedElement.attributedType();
    }
    
    public final boolean ambiguous() {
      return this.beans().size() > 1;
    }

    public final boolean unsatisfied() {
      return this.beans().isEmpty();
    }

  }

}
