/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2025 microBean™.
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

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.lang.model.type.TypeMirror;

import org.microbean.constant.Constables;

import org.microbean.qualifier.NamedAttributeMap;

import org.microbean.scope.ScopeMember;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_List;
import static java.lang.constant.ConstantDescs.FALSE;
import static java.lang.constant.ConstantDescs.TRUE;

import static org.microbean.bean.BeanTypes.legalBeanType;

import static org.microbean.bean.ConstantDescs.CD_Id;

import static org.microbean.qualifier.ConstantDescs.CD_NamedAttributeMap;

/**
 * An identifier for a {@link Bean}.
 *
 * @param types a {@link BeanTypeList}
 *
 * @param attributes a {@link List} of {@link NamedAttributeMap}s
 *
 * @param governingScopeId a {@link NamedAttributeMap} identifying the <dfn>scope</dfn> to which this {@link Id}
 * logically belongs
 *
 * @param alternate whether this {@link Id} is to be considered an <dfn>alternate</dfn>
 *
 * @param rank the {@linkplain Ranked rank} of this {@link Id}
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see Ranked
 *
 * @see ScopeMember
 */
public final record Id(BeanTypeList types,
                       List<NamedAttributeMap<?>> attributes,
                       NamedAttributeMap<?> governingScopeId,
                       boolean alternate,
                       int rank)
  implements Constable, Ranked, ScopeMember {


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link Id} that is not an alternate and that has a {@linkplain Ranked#DEFAULT_RANK default rank}.
   *
   * @param types a {@link BeanTypeList}; must not be {@code null}; must not be {@linkplain List#isEmpty() empty}
   *
   * @param attributes a {@link List} of {@link NamedAttributeMap}s; must not be {@code null}
   *
   * @param governingScopeId a {@link NamedAttributeMap} identifying the <dfn>scope</dfn> to which this {@link Id}
   * logically belongs; must not be {@code null}
   *
   * @exception NullPointerException if {@code types}, {@code attributes}, or {@code governingScopeId} is {@code null}
   */
  public Id(final BeanTypeList types,
            final List<NamedAttributeMap<?>> attributes,
            final NamedAttributeMap<?> governingScopeId) {
    this(types, attributes, governingScopeId, false, Ranked.DEFAULT_RANK);
  }

  /**
   * Creates a new {@link Id} that is not an alternate.
   *
   * @param types a {@link BeanTypeList}; must not be {@code null}; must not be {@linkplain List#isEmpty() empty}
   *
   * @param attributes a {@link List} of {@link NamedAttributeMap}s; must not be {@code null}
   *
   * @param governingScopeId a {@link NamedAttributeMap} identifying the <dfn>scope</dfn> to which this {@link Id}
   * logically belongs; must not be {@code null}
   *
   * @param rank the {@linkplain Ranked rank} of this {@link Id}
   *
   * @exception NullPointerException if {@code types}, {@code attributes}, or {@code governingScopeId} is {@code null}
   */
  public Id(final BeanTypeList types,
            final List<NamedAttributeMap<?>> attributes,
            final NamedAttributeMap<?> governingScopeId,
            final int rank) {
    this(types, attributes, governingScopeId, false, rank);
  }

  /**
   * Creates a new {@link Id}.
   *
   * @param types a {@link BeanTypeList}; must not be {@code null}; must not be {@linkplain List#isEmpty() empty}
   *
   * @param attributes a {@link List} of {@link NamedAttributeMap}s; must not be {@code null}
   *
   * @param governingScopeId a {@link NamedAttributeMap} identifying the <dfn>scope</dfn> to which this {@link Id}
   * logically belongs; must not be {@code null}
   *
   * @param alternate whether this {@link Id} is to be considered an <dfn>alternate</dfn>
   *
   * @param rank the {@linkplain Ranked rank} of this {@link Id}
   *
   * @exception NullPointerException if {@code types}, {@code attributes}, or {@code governingScopeId} is {@code null}
   */
  public Id {
    Objects.requireNonNull(types, "types");
    attributes = List.copyOf(attributes);
    Objects.requireNonNull(governingScopeId, "governingScopeId");
  }


  /*
   * Instance methods.
   */

  
  @Override // Constable
  public final Optional<DynamicConstantDesc<Id>> describeConstable() {
    return Constables.describeConstable(this.attributes())
      .flatMap(attributesDesc -> this.governingScopeId().describeConstable()
               .flatMap(governingScopeIdDesc -> this.types().describeConstable()
                        .map(typesDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                                 MethodHandleDesc.ofConstructor(CD_Id,
                                                                                                CD_List,
                                                                                                CD_List,
                                                                                                CD_NamedAttributeMap,
                                                                                                CD_boolean,
                                                                                                CD_int),
                                                                 typesDesc,
                                                                 attributesDesc,
                                                                 governingScopeIdDesc,
                                                                 this.alternate() ? TRUE : FALSE,
                                                                 this.rank()))));
  }

}
