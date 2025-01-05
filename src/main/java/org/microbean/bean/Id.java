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

import org.microbean.construct.Domain;

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

public final record Id(Domain domain,
                       List<TypeMirror> types,
                       List<NamedAttributeMap<?>> attributes,
                       NamedAttributeMap<?> governingScopeId,
                       boolean alternate,
                       int rank)
  implements Constable, Ranked, ScopeMember {

  private static final ClassDesc CD_Domain = ClassDesc.of(Domain.class.getName());

  public Id(final Domain domain,
            final List<TypeMirror> types,
            final List<NamedAttributeMap<?>> attributes,
            final NamedAttributeMap<?> governingScopeId) {
    this(domain, types, attributes, governingScopeId, false, Ranked.DEFAULT_RANK);
  }

  public Id(final Domain domain,
            final List<TypeMirror> types,
            final List<NamedAttributeMap<?>> attributes,
            final NamedAttributeMap<?> governingScopeId,
            final int rank) {
    this(domain, types, attributes, governingScopeId, false, rank);
  }

  public Id {
    // The code below jumps through some hoops to avoid copying the types list if possible.
    final int size = types.size();
    if (size == 0) {
      throw new IllegalArgumentException("types.isEmpty()");
    }
    int i = 0;
    for (; i < size; i++) {
      if (!legalBeanType(domain, types.get(i))) {
        break;
      }
    }
    if (i == size) {
      types = List.copyOf(types);
    } else {
      final ArrayList<TypeMirror> newTypes = new ArrayList<>(size);
      for (int j = 0; j < i; j++) {
        newTypes.add(types.get(j)); // the type is known to be legal
      }
      ++i; // skip past the illegal type i was pointing to
      for (; i < size; i++) {
        final TypeMirror t = types.get(i);
        if (legalBeanType(domain, t)) {
          newTypes.add(t);
        }
      }
      if (newTypes.isEmpty()) {
        throw new IllegalArgumentException("types contains no legal bean types: " + types);
      }
      newTypes.trimToSize();
      types = Collections.unmodifiableList(newTypes);
    }
    attributes = List.copyOf(attributes);
    Objects.requireNonNull(governingScopeId, "governingScopeId");
  }

  @Override // Constable
  public final Optional<DynamicConstantDesc<Id>> describeConstable() {
    return (this.domain() instanceof Constable c ? c.describeConstable() : Optional.<ConstantDesc>empty())
      .flatMap(domainDesc ->  Constables.describeConstable(this.attributes())
               .flatMap(attributesDesc -> this.governingScopeId().describeConstable()
                        .flatMap(governingScopeIdDesc -> Constables.describeConstable(this.types())
                                 .map(typesDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                                          MethodHandleDesc.ofConstructor(CD_Id,
                                                                                                         CD_Domain,
                                                                                                         CD_List,
                                                                                                         CD_List,
                                                                                                         CD_NamedAttributeMap,
                                                                                                         CD_boolean,
                                                                                                         CD_int),
                                                                          typesDesc,
                                                                          attributesDesc,
                                                                          governingScopeIdDesc,
                                                                          this.alternate() ? TRUE : FALSE,
                                                                          this.rank())))));
  }

}
