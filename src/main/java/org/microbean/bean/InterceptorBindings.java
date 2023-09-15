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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.microbean.qualifier.NamedAttributeMap;

public final class InterceptorBindings {

  private InterceptorBindings() {
    super();
  }

  public static final List<NamedAttributeMap<?>> interceptorBindings(final Collection<? extends NamedAttributeMap<?>> c) {
    if (c.isEmpty()) {
      return List.of();
    }
    final ArrayList<NamedAttributeMap<?>> list = new ArrayList<>(c.size());
    for (final NamedAttributeMap<?> a : c) {
      if (Kind.INTERCEPTOR_BINDING.describes(a)) {
        list.add(a);
      }
    }
    list.trimToSize();
    return Collections.unmodifiableList(list);
  }

  public static final NamedAttributeMap<?> anyInterceptorBinding() {
    return Kind.ANY_INTERCEPTOR_BINDING.of();
  }

  public enum Kind {

    INTERCEPTOR_BINDING() {
      private static final NamedAttributeMap<?> DESIGNATOR = new NamedAttributeMap<>("InterceptorBinding");

      /**
       * Returns {@code true} if {@code a} is non-{@code null}, and if an element of its {@linkplain
       * NamedAttributeMap#metadata() metadata} is itself the interceptor binding designator, or if its {@linkplain
       * NamedAttributeMap#metadata() metadata} contains a {@link NamedAttributeMap} that meets these conditions.
       *
       * @param a {@link NamedAttributeMap}; may be {@code null} in which case {@code false} will be returned
       *
       * @return {@code true} if {@code a} is non-{@code null} and is either the qualifier designator itself, or if its
       * {@linkplain NamedAttributeMap#metadata() metadata} contains a {@link NamedAttributeMap} that meets these
       * conditions
       *
       * @see NamedAttributeMap
       */
      @Override
      public final boolean describes(final NamedAttributeMap<?> a) {
        return a != null && this.describes(a.metadata());
      }

      public final boolean describes(final Iterable<? extends NamedAttributeMap<?>> mds) {
        for (final NamedAttributeMap<?> md : mds) {
          if ((md.equals(DESIGNATOR) && md.metadata().isEmpty()) || this.describes(md)) {
            return true;
          }
        }
        return false;
      }

      @Override
      public final NamedAttributeMap<?> of() {
        return DESIGNATOR;
      }
    },

    ANY_INTERCEPTOR_BINDING() {
      private static final NamedAttributeMap<?> INSTANCE =
        new NamedAttributeMap<>("Any", Map.of(), Map.of(), List.of(INTERCEPTOR_BINDING.of()));

      @Override
      public final boolean describes(final NamedAttributeMap<?> a) {
        return a != null && a.equals(INSTANCE) && INTERCEPTOR_BINDING.describes(a);
      }

      @Override
      public final NamedAttributeMap<?> of() {
        return INSTANCE;
      }
    },
    
    TARGET_CLASS_INTERCEPTOR_BINDING() {
      private static final NamedAttributeMap<?> INSTANCE =
        new NamedAttributeMap<>("TargetClass", Map.of(), Map.of(), List.of(INTERCEPTOR_BINDING.of()));
      
      @Override
      public final boolean describes(final NamedAttributeMap<?> a) {
        return a != null && a.equals(INSTANCE) && INTERCEPTOR_BINDING.describes(a);
      }

      public final NamedAttributeMap<?> of() {
        return INSTANCE;
      }
    };

    private Kind() {
    }

    public abstract boolean describes(final NamedAttributeMap<?> a);

    public abstract NamedAttributeMap<?> of();

  }

}
