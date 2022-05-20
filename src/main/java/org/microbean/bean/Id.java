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

import java.util.Objects;
import java.util.Optional;

import org.microbean.development.annotation.Convenience;

import org.microbean.interceptor.InterceptorBindings;

import org.microbean.qualifier.Qualified;
import org.microbean.qualifier.Qualifier;
import org.microbean.qualifier.Qualifiers;

import org.microbean.scope.ScopeMember;

import org.microbean.type.Type;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.NULL;

import static java.lang.constant.DirectMethodHandleDesc.Kind.STATIC;

import static org.microbean.bean.ConstantDescs.CD_Id;
import static org.microbean.bean.ConstantDescs.CD_Selector;

import static org.microbean.interceptor.ConstantDescs.CD_InterceptorBindings;

import static org.microbean.qualifier.ConstantDescs.CD_Qualified;
import static org.microbean.qualifier.ConstantDescs.CD_Qualifier;

public final record Id(Selector selector, Qualifier<?, ?> governingScopeId, int priority)
  implements Alternate, Constable, Qualified<String, Object, Type<?>>, ScopeMember {

  public Id {
    Objects.requireNonNull(selector, "selector");
  }

  public final Id with(final Selector selector) {
    return of(selector, this.governingScopeId(), this.priority());
  }
  
  @Override // Alternate
  public final boolean alternate() {
    return Alternate.super.alternate() && !this.interceptor();
  }

  @Convenience
  public final boolean interceptor() {
    return !this.selector().interceptorBindings().isEmpty();
  }

  @Convenience
  public final InterceptorBindings<String, Object> interceptorBindings() {
    return this.selector().interceptorBindings();
  }

  @Convenience
  @Override // Qualified<String, Object, Type<?>>
  public final Qualifiers<String, Object> qualifiers() {
    return this.selector().qualifiedType().qualifiers();
  }

  @Convenience
  @Override // Qualified<String, Object, Type<?>>
  public final Type<?> qualified() {
    return this.selector().qualifiedType().qualified();
  }

  @Convenience
  public final Qualified<String, Object, Type<?>> qualifiedType() {
    return this.selector().qualifiedType();
  }

  @Override // Constable
  public final Optional<? extends ConstantDesc> describeConstable() {
    final ConstantDesc selectorCd = this.selector().describeConstable().orElse(null);
    if (selectorCd != null) {
      final Constable governingScopeId = this.governingScopeId();
      final ConstantDesc governingScopeIdCd = governingScopeId == null ? NULL : governingScopeId.describeConstable().orElse(null);
      if (governingScopeIdCd != null) {
        return
            Optional.of(DynamicConstantDesc.of(BSM_INVOKE,
                                               MethodHandleDesc.ofMethod(STATIC,
                                                                         CD_Id,
                                                                         "of",
                                                                         MethodTypeDesc.of(CD_Id,
                                                                                           CD_Selector,
                                                                                           CD_int)),
                                               selectorCd,
                                               governingScopeIdCd,
                                               Integer.valueOf(this.priority())));
      }
    }
    return Optional.empty();                                                                             
  }

  public static final Id of(final Selector selector,
                            final Qualifier<?, ?> scopeId) {
    return of(selector, scopeId, Prioritized.DEFAULT_PRIORITY);
  }
  
  // This method is referenced by describeConstable().
  public static final Id of(final Selector selector,
                            final Qualifier<?, ?> scopeId,
                            final int priority) {
    return new Id(selector, scopeId, priority);
  }

}
