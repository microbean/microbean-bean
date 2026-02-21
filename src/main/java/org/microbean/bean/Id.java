/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2026 microBean™.
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

import java.lang.constant.Constable;
import java.lang.constant.DynamicConstantDesc;

import java.util.List;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;

import org.microbean.constant.Constables;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_List;

import static java.lang.constant.MethodHandleDesc.ofConstructor;

import static java.util.Objects.requireNonNull;

/**
 * An identifier for a {@link Bean}.
 *
 * <p>Note that because an {@link Id} houses a {@link BeanTypeList}, and because a {@link BeanTypeList} houses {@link
 * javax.lang.model.type.TypeMirror} instances, and because many {@link javax.lang.model.type.TypeMirror} instances may
 * model the same type, two {@link Id}s that might appear as equal at first glance may not be.</p>
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see javax.lang.model.type.TypeMirror#equals(Object)
 */
public final class Id implements Constable {

  private final BeanTypeList types;

  private final List<AnnotationMirror> annotations;

  /**
   * Creates a new {@link Id}.
   *
   * @param types a non-{@code null} {@link BeanTypeList}
   *
   * @param annotations a non-{@code null} {@link List} of {@link AnnotationMirror}s further describing the supplied
   * {@code types}
   *
   * @see BeanTypeList
   *
   * @see BeanTypes#beanTypes(java.util.Collection)
   */
  public Id(final BeanTypeList types, final List<AnnotationMirror> annotations) {
    super();
    this.types = requireNonNull(types, "types");
    this.annotations = List.copyOf(annotations);
  }

  /**
   * Returns a non-{@code null}, immutable, determinate {@link List} of {@link AnnotationMirror}s describing this {@link
   * Id}.
   *
   * @return a non-{@code null}, immutable, determinate {@link List} of {@link AnnotationMirror}s describing this {@link
   * Id}
   *
   * @see #Id(BeanTypeList, List)
   */
  public final List<AnnotationMirror> annotations() {
    return this.annotations;
  }

  @Override // Constable
  public final Optional<DynamicConstantDesc<Id>> describeConstable() {
    return Constables.describeConstable(this.annotations)
      .flatMap(annotationsDesc -> this.types.describeConstable()
               .map(typesDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                        ofConstructor(this.getClass().describeConstable().orElseThrow(),
                                                                      BeanTypeList.class.describeConstable().orElseThrow(),
                                                                      CD_List),
                                                        typesDesc,
                                                        annotationsDesc)));
  }

  @Override // Object
  public final boolean equals(final Object other) {
    return this == other || switch (other) {
    case null -> false;
    case Id i when this.getClass() == i.getClass() -> this.types.equals(i.types) && this.annotations.equals(i.annotations());
    default -> false;
    };
  }

  @Override // Object
  public final int hashCode() {
    return this.types.hashCode() ^ this.annotations.hashCode();
  }

  @Override // Object
  public final String toString() {
    return this.annotations.toString() + " " + this.types.toString(); // TODO: improve
  }

  /**
   * Returns a non-{@code null} determinate {@link BeanTypeList} describing this {@link Id}.
   *
   * @return a non-{@code null} determinate {@link BeanTypeList} describing this {@link Id}
   *
   * @see #Id(BeanTypeList, List)
   *
   * @see BeanTypeList
   *
   * @see BeanTypes#beanTypes(java.util.Collection)
   */
  public final BeanTypeList types() {
    return this.types;
  }
  
}
