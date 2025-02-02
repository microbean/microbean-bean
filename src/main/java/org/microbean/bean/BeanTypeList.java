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
package org.microbean.bean;

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.microbean.assign.ClassesThenInterfacesElementKindComparator;
import org.microbean.assign.PrimitiveTypesThenDeclaredTypesTypeKindComparator;
import org.microbean.assign.SpecializationComparator;
import org.microbean.assign.SupertypeList;
import org.microbean.assign.TypeVariablesFirstTypeKindComparator;
import org.microbean.assign.Types;

import org.microbean.constant.Constables;

import org.microbean.construct.Domain;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_Collection;

import static java.lang.constant.DirectMethodHandleDesc.Kind.STATIC;

import static java.util.Collections.unmodifiableList;

import static org.microbean.bean.BeanTypes.legalBeanType;

/**
 * An immutable {@link AbstractList} of {@link TypeMirror}s that contains only {@linkplain
 * BeanTypes#legalBeanType(TypeMirror) legal bean types}, sorted in a specific manner.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #of(Domain, Collection)
 */
public final class BeanTypeList extends AbstractList<TypeMirror> implements Constable {

  private final Domain domain; // for Constable only

  private final List<TypeMirror> types;

  private final int interfaceIndex;

  private BeanTypeList(final Domain domain, final Collection<? extends TypeMirror> types) {
    super();
    Objects.requireNonNull(types, "types");
    if (types instanceof BeanTypeList btl) {
      this.domain = btl.domain;
      this.types = btl.types;
      this.interfaceIndex = btl.interfaceIndex;
    } else {
      this.domain = domain;
      final int size = types.size();
      if (size == 0) {
        this.types = List.of();
        this.interfaceIndex = -1;
      } else {
        final ArrayList<TypeMirror> newTypes;
        if (types instanceof SupertypeList stl) {
          // SupertypeList instances are already sorted, which is why we check here. Now we can (potentially) avoid
          // copying, too, since we can use List#copyOf() in the sunny-day case.
          int i = 0;
          for (; i < size; i++) {
            if (!legalBeanType(stl.get(i))) {
              break;
            }
          }
          if (i == size) {
            // All types were legal, everything is sorted
            newTypes = null;
            this.types = List.copyOf(types);
            this.interfaceIndex = stl.interfaceIndex();
          } else {
            newTypes = new ArrayList<>(size);
            for (int j = 0; j < i; j++) {
              newTypes.add(stl.get(j)); // the type is known to be legal
            }
            ++i; // skip past the illegal type that was encountered
            for (; i < size; i++) {
              final TypeMirror t = stl.get(i);
              if (legalBeanType(t)) {
                newTypes.add(t);
              }
            }
            newTypes.trimToSize();
            this.types = newTypes.isEmpty() ? List.of() : unmodifiableList(newTypes);
            this.interfaceIndex = this.types.isEmpty() || stl.interfaceIndex() >= this.types.size() ? -1 : stl.interfaceIndex();
          }
        } else {
          newTypes = new ArrayList<>(size);
          for (final TypeMirror t : types) {
            if (legalBeanType(t)) {
              newTypes.add(t);
            }
          }
          if (newTypes.isEmpty()) {
            this.types = List.of();
            this.interfaceIndex = -1;
          } else {
            newTypes.trimToSize();
            if (newTypes.size() > 1) {
              // You might be tempted to include a Comparator that verifies that type variables come first. Normally that
              // would be good, but a type variable is not a legal bean type.
              Collections.sort(newTypes,
                               Comparator.comparing(TypeMirror::getKind,
                                                    PrimitiveTypesThenDeclaredTypesTypeKindComparator.INSTANCE)
                               .thenComparing(new SpecializationComparator(domain))
                               .thenComparing(this::elementKind,
                                              ClassesThenInterfacesElementKindComparator.INSTANCE)
                               .thenComparing(Types::erasedName));
            }
            this.types = unmodifiableList(newTypes);
            int interfaceIndex = -1;
            for (int i = 0; i < newTypes.size(); i++) {
              final ElementKind k = this.elementKind(newTypes.get(i));
              if (k != null && k.isInterface()) {
                interfaceIndex = i;
                break;
              }
            }
            this.interfaceIndex = interfaceIndex;
          }
        }
      }
    }
  }

  private ElementKind elementKind(final TypeMirror t) {
    final Element e = this.domain.element(t);
    return e == null ? null : e.getKind();
  }

  @Override // AbstractList<TypeMirror>
  public final TypeMirror get(final int index) {
    return this.types.get(index);
  }

  /**
   * Returns the index of the first interface type this {@link BeanTypeList} contains, or a negative value if it
   * contains no interface types.
   *
   * @return the index of the first interface type this {@link BeanTypeList} contains, or a negative value if it
   * contains no interface types
   */
  public final int interfaceIndex() {
    return this.interfaceIndex;
  }

  @Override // AbstractList<TypeMirror>
  public final boolean isEmpty() {
    return this.types.isEmpty();
  }

  @Override // Constable
  public final Optional<? extends ConstantDesc> describeConstable() {
    return (this.domain instanceof Constable c ? c.describeConstable() : Optional.<ConstantDesc>empty())
      .flatMap(domainDesc -> Constables.describeConstable(this.types)
               .map(typesDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                        MethodHandleDesc.ofMethod(STATIC,
                                                                                  ClassDesc.of(this.getClass().getName()),
                                                                                  "of",
                                                                                  MethodTypeDesc.of(ClassDesc.of(BeanTypeList.class.getName()),
                                                                                                    ClassDesc.of(Domain.class.getName()),
                                                                                                    CD_Collection)),
                                                        domainDesc,
                                                        typesDesc)));
  }

  @Override // AbstractList<TypeMirror>
  public final int size() {
    return this.types.size();
  }


  /*
   * Static methods.
   */


  /**
   * Returns a non-{@code null} {@link BeanTypeList} suitable for the supplied arguments.
   *
   * @param domain a {@link Domain} that may be used to determine {@linkplain Domain#subtype(TypeMirror, TypeMirror)
   * subtype relationships}; may be {@code null} if {@code types} is itself a {@link BeanTypeList}
   *
   * @param types a non-{@code null} {@link Collection} of {@link TypeMirror}s; must not be {@code null}
   *
   * @return a non-{@code null} {@link BeanTypeList}
   *
   * @exception NullPointerException if {@code types} is {@code null}, or if {@code types} is not a {@link BeanTypeList}
   * and {@code domain} is {@code null}
   */
  // Called by describeConstable()
  public static final BeanTypeList of(final Domain domain, final Collection<? extends TypeMirror> types) {
    return types instanceof BeanTypeList btl ? btl : new BeanTypeList(domain, types);
  }

}
