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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.lang.model.type.TypeMirror;

import org.microbean.assign.SupertypeList;

import org.microbean.constant.Constables;

import org.microbean.construct.Domain;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_Collection;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.FALSE;
import static java.lang.constant.ConstantDescs.TRUE;

import static java.lang.constant.DirectMethodHandleDesc.Kind.STATIC;
import static java.lang.constant.DirectMethodHandleDesc.Kind.VIRTUAL;

import static java.util.Objects.requireNonNull;

/**
 * An immutable {@link AbstractList} of {@link TypeMirror}s that contains only {@linkplain
 * BeanTypes#legalBeanType(TypeMirror) legal bean types}, sorted in a specific manner.
 *
 * <p><strong>Note:</strong> Two {@link TypeMirror} instances may represent the {@linkplain
 * org.microbean.construct.Domain#sameType(TypeMirror, TypeMirror) same type} while not being {@linkplain
 * TypeMirror#equals(Object) equal to} one another. {@link List} implementations such as this one that contain {@link
 * TypeMirror} elements may also represent the same types without being equal to each other.</p>
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see BeanTypes#beanTypes(TypeMirror)
 *
 * @see TypeMirror#equals(Object)
 *
 * @see org.microbean.construct.Domain#sameType(TypeMirror, TypeMirror)
 */
public final class BeanTypeList extends AbstractList<TypeMirror> implements Constable {

  private static final BeanTypeList EMPTY_LIST = new BeanTypeList();

  private final BeanTypes beanTypes; // needed only for Constable purposes. This is dumb.
  
  private final List<TypeMirror> types;

  private final int interfaceIndex;

  private final boolean proxiable;

  private BeanTypeList() {
    this.beanTypes = null;
    this.types = List.of();
    this.interfaceIndex = -1;
    this.proxiable = false;
  }

  private BeanTypeList(final BeanTypes beanTypes,
                       final Collection<? extends TypeMirror> types,
                       final int interfaceIndex,
                       final boolean proxiable) {
    super();
    this.beanTypes = requireNonNull(beanTypes, "beanTypes");
    if (types.isEmpty()) {
      this.types = List.of();
      this.interfaceIndex = -1;
      this.proxiable = proxiable;
    } else if (types instanceof BeanTypeList btl) {
      this.types = btl;
      this.interfaceIndex = btl.interfaceIndex;
      this.proxiable = btl.proxiable;
    } else if (types instanceof SupertypeList stl) {
      this.types = stl;
      this.interfaceIndex = stl.interfaceIndex();
      this.proxiable = proxiable;
    } else {
      this.types = List.copyOf(types);
      this.interfaceIndex = interfaceIndex;
      this.proxiable = proxiable;
    }
  }

  @Override // Constable
  public final Optional<? extends ConstantDesc> describeConstable() {
    if (this.beanTypes == null) {
      assert this.isEmpty();
      return Optional.of(DynamicConstantDesc.of(BSM_INVOKE,
                                                MethodHandleDesc.ofMethod(STATIC,
                                                                          ClassDesc.of(this.getClass().getName()),
                                                                          "of",
                                                                          MethodTypeDesc.of(ClassDesc.of(this.getClass().getName())))));
    }
    return Constables.describeConstable(this.types)
      .flatMap(typesDesc -> this.beanTypes.describeConstable()
               .map(beanTypesDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                            MethodHandleDesc.ofMethod(VIRTUAL,
                                                                                      ClassDesc.of(this.beanTypes.getClass().getName()),
                                                                                      "beanTypes",
                                                                                      MethodTypeDesc.of(ClassDesc.of(this.getClass().getName()),
                                                                                                        CD_Collection)),
                                                            beanTypesDesc,
                                                            typesDesc)));
  }

  @Override // AbstractList<TypeMirror>
  public final TypeMirror get(final int index) {
    return this.types.get(index);
  }

  /**
   * Returns a non-{@code null}, immutable {@link List} containing only interface types.
   *
   * <p>The returned {@link List} may be {@linkplain List#isEmpty() empty}.</p>
   *
   * @return a non-{@code null}, immutable {@link List} containing only interface types
   */
  public final List<TypeMirror> interfaces() {
    final int i = this.interfaceIndex;
    return i < 0 ? List.of() : this.subList(i, this.size());
  }

  /**
   * Returns {@code true} if and only if this {@link BeanTypeList} is <dfn>proxiable</dfn>.
   *
   * @return {@code true} if and only if this {@link BeanTypeList} is <dfn>proxiable</dfn>
   *
   * @see BeanTypes#proxiableElement(Element)
   *
   * @spec https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#unproxyable CDI Specification, version 4.0,
   * section 2.2.10
   */
  public final boolean proxiable() {
    return this.proxiable;
  }

  @Override // AbstractList<TypeMirror>
  public final int size() {
    return this.types.size();
  }


  /*
   * Static methods.
   */


  /**
   * Returns a non-{@code null} {@link BeanTypeList} that {@linkplain #isEmpty() is empty}.
   *
   * @return a non-{@code null} {@link BeanTypeList} that {@linkplain #isEmpty() is empty}
   *
   * @see BeanTypes#beanTypes(Collection)
   */
  public static final BeanTypeList of() {
    return EMPTY_LIST;
  }

  static final BeanTypeList of(final BeanTypes beanTypes, final Collection<? extends TypeMirror> types) {
    return of(beanTypes, types, -1, false);
  }

  static final BeanTypeList of(final BeanTypes beanTypes, final Collection<? extends TypeMirror> types, final boolean proxiable) {
    return of(beanTypes, types, -1, proxiable);
  }

  // Called only by BeanTypes. No validation is performed.
  static final BeanTypeList of(final BeanTypes beanTypes,
                               final Collection<? extends TypeMirror> types,
                               final int interfaceIndex,
                               final boolean proxiable) {
    return switch (types) {
    case null -> throw new NullPointerException("types");
    case Collection<?> c when c.isEmpty() -> EMPTY_LIST;
    case BeanTypeList btl -> btl;
    case SupertypeList stl -> new BeanTypeList(beanTypes, stl, stl.interfaceIndex(), proxiable);
    default -> new BeanTypeList(beanTypes, types, interfaceIndex, proxiable);
    };
  }

}
