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

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.lang.model.type.TypeMirror;

import org.microbean.constant.Constables;

import org.microbean.qualifier.NamedAttributeMap;

import org.microbean.scope.ScopeMember;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_List;

import static org.microbean.bean2.ConstantDescs.CD_Id;
import static org.microbean.bean2.ConstantDescs.CD_BeanTypeList;

import static org.microbean.qualifier.ConstantDescs.CD_NamedAttributeMap;

public final record Id(BeanTypeList types,
                       List<NamedAttributeMap<?>> attributes,
                       NamedAttributeMap<?> governingScopeId,
                       int rank)
  implements Alternate, Constable, ScopeMember {

  public Id(final TypeMirror type,
            final List<NamedAttributeMap<?>> attributes,
            final NamedAttributeMap<?> governingScopeId) {
    this(new BeanTypeList(type), attributes, governingScopeId, Ranked.DEFAULT_RANK);
  }

  public Id(final List<? extends TypeMirror> types,
            final List<NamedAttributeMap<?>> attributes,
            final NamedAttributeMap<?> governingScopeId) {
    this(new BeanTypeList(types), attributes, governingScopeId, Ranked.DEFAULT_RANK);
  }

  public Id(final BeanTypeList types,
            final List<NamedAttributeMap<?>> attributes,
            final NamedAttributeMap<?> governingScopeId) {
    this(types, attributes, governingScopeId, Ranked.DEFAULT_RANK);
  }

  public Id {
    types = Objects.requireNonNull(types, "types");
    attributes = List.copyOf(attributes);
    governingScopeId = Objects.requireNonNull(governingScopeId, "governingScopeId");
  }

  @Override // Constable
  public final Optional<DynamicConstantDesc<Id>> describeConstable() {
    return Constables.describeConstable(this.attributes())
      .flatMap(attributesDesc -> this.governingScopeId().describeConstable()
               .flatMap(governingScopeIdDesc -> types.describeConstable()
                        .map(typesDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                                 MethodHandleDesc.ofConstructor(CD_Id,
                                                                                                CD_BeanTypeList,
                                                                                                CD_List,
                                                                                                CD_NamedAttributeMap,
                                                                                                CD_int),
                                                                 typesDesc,
                                                                 attributesDesc,
                                                                 governingScopeIdDesc,
                                                                 this.rank()))));
  }

}
