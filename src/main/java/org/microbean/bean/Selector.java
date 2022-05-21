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

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import java.util.function.Predicate;

import org.microbean.development.annotation.Convenience;

import org.microbean.interceptor.InterceptorBinding;
import org.microbean.interceptor.InterceptorBindings;

import org.microbean.qualifier.Binding;
import org.microbean.qualifier.Bindings;
import org.microbean.qualifier.Qualified;
import org.microbean.qualifier.Qualifier;
import org.microbean.qualifier.Qualifiers;

import org.microbean.type.JavaType;
import org.microbean.type.Type;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.NULL;

import static java.lang.constant.DirectMethodHandleDesc.Kind.STATIC;

import static org.microbean.bean.ConstantDescs.CD_Selector;

import static org.microbean.interceptor.ConstantDescs.CD_InterceptorBindings;

import static org.microbean.qualifier.ConstantDescs.CD_Qualified;

public final record Selector(Qualified<?, ? extends Type<?>> qualifiedType, InterceptorBindings<?> interceptorBindings)
  implements Constable {


  /*
   * Static fields.
   */


  public static final InterceptorBinding<?> ANY_INTERCEPTOR_BINDING = InterceptorBinding.of("ANY");

  public static final InterceptorBindings<?> ANY_INTERCEPTOR_BINDINGS = InterceptorBindings.of(ANY_INTERCEPTOR_BINDING);

  public static final Qualifier<?> ANY_QUALIFIER = Qualifier.of("ANY");

  public static final Qualifiers<?> ANY_QUALIFIERS = Qualifiers.of(ANY_QUALIFIER);

  public static final Qualifier<?> DEFAULT_QUALIFIER = Qualifier.of("DEFAULT");

  public static final Qualifiers<?> DEFAULT_QUALIFIERS = Qualifiers.of(DEFAULT_QUALIFIER);

  public static final Qualifiers<?> ANY_AND_DEFAULT_QUALIFIERS = Qualifiers.ofDisparate(List.of(ANY_QUALIFIER, DEFAULT_QUALIFIER));


  /*
   * Constructors.
   */


  @Deprecated
  public Selector(final Qualified<?, ? extends Type<?>> qualifiedType) {
    this(qualifiedType, null);
  }

  @Deprecated
  public Selector {
    Objects.requireNonNull(qualifiedType, "qualifiedType");
  }


  /*
   * Instance methods.
   */


  public final boolean selects(final Bean<?> bean) {
    return bean != null && this.selects(bean.id().selector());
  }

  public final boolean selects(final Qualified<?, ? extends Type<?>> qualified) {
    return
      qualified != null &&
      this.selects(qualified.qualifiers()) &&
      this.selects(qualified.qualified()) &&
      this.selects((InterceptorBindings<?>)null);
  }

  public final boolean selects(final Id id) {
    return id != null && this.selects(id.selector());
  }

  public final boolean selects(final Selector selector) {
    return
      selector != null &&
      this.selects(selector.qualifiedType().qualifiers()) &&
      this.selects(selector.qualifiedType().qualified()) &&
      this.selects(selector.interceptorBindings());
  }

  public final boolean selects(final Qualifiers<?> qualifiers) {
    final Qualifiers<?> myQualifiers = this.qualifiedType().qualifiers();
    if (myQualifiers == null || myQualifiers.isEmpty()) {
      return
        qualifiers == null ||
        qualifiers.isEmpty() ||
        qualifiers.contains(DEFAULT_QUALIFIER) ||
        qualifiers.size() == 1 && qualifiers.contains(ANY_QUALIFIER);
    } else if (qualifiers == null || qualifiers.isEmpty()) {
      return
        myQualifiers.contains(DEFAULT_QUALIFIER) ||
        myQualifiers.contains(ANY_QUALIFIER);
    } else {
      return
        qualifiers.size() == 1 && qualifiers.contains(ANY_QUALIFIER) ||
        containsAll(qualifiers::contains, myQualifiers);
    }
  }

  public final boolean selects(final Type<?> type) {
    return type != null && Type.CdiSemantics.INSTANCE.assignable(this.qualifiedType().qualified(), type);
  }

  public final boolean selects(final InterceptorBindings<?> interceptorBindings) {
    final InterceptorBindings<?> myInterceptorBindings = this.interceptorBindings();
    if (myInterceptorBindings == null || ANY_INTERCEPTOR_BINDINGS.equals(interceptorBindings)) {
      return true;
    } else if (interceptorBindings == null) {
      return false;
    } else {
      return myInterceptorBindings.equals(interceptorBindings);
    }
  }

  @Convenience
  public final Selector with(final Type<?> type) {
    return this.with(Qualified.of(this.qualifiedType().qualifiers(), type));
  }

  @Convenience
  public final Selector with(final Qualifiers<?> qualifiers) {
    return this.with(Qualified.of(qualifiers, this.qualifiedType().qualified()));
  }

  @Convenience
  public final Selector with(final Qualified<?, Type<?>> qualifiedType) {
    return of(qualifiedType(), this.interceptorBindings());
  }

  @Convenience
  public final Selector with(final InterceptorBindings<?> interceptorBindings) { // null is significant
    return of(this.qualifiedType(), interceptorBindings);
  }

  @Override // Constable
  public final Optional<? extends ConstantDesc> describeConstable() {
    final ConstantDesc qualifiedTypeCd = this.qualifiedType().describeConstable().orElse(null);
    if (qualifiedTypeCd != null) {
      final InterceptorBindings<?> interceptorBindings = this.interceptorBindings();
      final ConstantDesc interceptorBindingsCd =
        interceptorBindings == null ? NULL : interceptorBindings.describeConstable().orElse(null);
      if (interceptorBindingsCd != null) {
        return
          Optional.of(DynamicConstantDesc.of(BSM_INVOKE,
                                             MethodHandleDesc.ofMethod(STATIC,
                                                                       CD_Selector,
                                                                       "of",
                                                                       MethodTypeDesc.of(CD_Selector,
                                                                                         CD_Qualified,
                                                                                         CD_InterceptorBindings)),
                                             qualifiedTypeCd,
                                             interceptorBindingsCd));
      }
    }
    return Optional.empty();
  }


  /*
   * Static methods.
   */


  public static final Selector ofAny(final java.lang.reflect.Type type) {
    return of(ANY_QUALIFIERS, type);
  }

  public static final Selector ofDefault(final java.lang.reflect.Type type) {
    return of(DEFAULT_QUALIFIERS, type);
  }

  public static final Selector ofAnyAndDefault(final java.lang.reflect.Type type) {
    return of(ANY_AND_DEFAULT_QUALIFIERS, type);
  }

  public static final Selector ofAny(final Type<?> type) {
    return of(ANY_QUALIFIERS, type);
  }

  public static final Selector ofDefault(final Type<?> type) {
    return of(DEFAULT_QUALIFIERS, type);
  }

  public static final Selector ofAnyAndDefault(final Type<?> type) {
    return of(ANY_AND_DEFAULT_QUALIFIERS, type);
  }

  public static final Selector of(final Qualifiers<?> qualifiers, final java.lang.reflect.Type type) {
    return of(qualifiers, JavaType.of(type));
  }

  public static final Selector of(final Qualifiers<?> qualifiers, final Type<?> type) {
    return of(Qualified.of(qualifiers, type));
  }

  public static final Selector of(final Qualified<?, ? extends Type<?>> qualifiedType) {
    return of(qualifiedType, null);
  }

  // This method is referenced by the describeConstable() method.
  public static final Selector of(final Qualified<?, ? extends Type<?>> qualifiedType,
                                  final InterceptorBindings<?> interceptorBindings) { // null is significant
    return new Selector(qualifiedType, interceptorBindings);
  }

  private static final boolean containsAll(final Predicate<? super Object> p, final Iterable<?> i) {
    if (p == null || i == null) {
      return false;
    } else if (p == i) {
      return true;
    }
    for (final Object o : i) {
      if (!p.test(o)) {
        return false;
      }
    }
    return true;
  }

}
