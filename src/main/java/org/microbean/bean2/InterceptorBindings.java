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
package org.microbean.bean2;

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
      private static final NamedAttributeMap<?> INSTANCE = new NamedAttributeMap<>("InterceptorBinding");

      @Override
        public final boolean describes(final NamedAttributeMap<?> a) {
        if (a == null) {
          return false;
        }
        for (final NamedAttributeMap<?> m : a.metadata()) {
          if (m.containsKey("InterceptorBinding")) {
            return true;
          }
        }
        return false;
      }

      @Override
      public final NamedAttributeMap<?> of() {
        return INSTANCE;
      }
    },
    
    ANY_INTERCEPTOR_BINDING() {
      private static final NamedAttributeMap<?> INSTANCE =
        new NamedAttributeMap<>("Any", Map.of(), Map.of(), List.of(INTERCEPTOR_BINDING.of()));

      @Override
      public final boolean describes(final NamedAttributeMap<?> a) {
        if (a == null) {
          return false;
        }
        for (final NamedAttributeMap<?> m : a.metadata()) {
          if (m.name().equalsIgnoreCase("Any") && m.containsKey("InterceptorBinding")) {
            return true;
          }
        }
        return false;
      }

      @Override
      public final NamedAttributeMap<?> of() {
        return INSTANCE;
      }
    },

    OTHER() {
      @Override
      public final boolean describes(final NamedAttributeMap<?> a) {
        if (a == null) {
          return false;
        }
        for (final Kind k : nonOther) {
          if (k.describes(a)) {
            return false;
          }
        }
        return true;
      }
    };

    private Kind() {
    }

    public abstract boolean describes(final NamedAttributeMap<?> a);

    public NamedAttributeMap<?> of() {
      throw new UnsupportedOperationException();
    }

    private static final EnumSet<Kind> nonOther = EnumSet.complementOf(EnumSet.of(OTHER));

  }

}
