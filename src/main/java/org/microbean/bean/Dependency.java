/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2024 microBean™.
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
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.lang.model.element.VariableElement;

import javax.lang.model.type.TypeMirror;

import org.microbean.lang.Lang;
import org.microbean.lang.Lang.SameTypeEquality;

import org.microbean.lang.element.DelegatingElement;

import org.microbean.qualifier.NamedAttributeMap;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;

import static org.microbean.bean.ConstantDescs.CD_BeanSelectionCriteria;
import static org.microbean.bean.ConstantDescs.CD_Dependency;

import static org.microbean.lang.ConstantDescs.CD_VariableElement;

/**
 * A dependency that a given type has on some other qualified type.
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 */
public final class Dependency implements Constable {


  /*
   * Instance fields.
   */


  private final BeanSelectionCriteria bsc;

  private final DelegatingElement e;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link Dependency}.
   *
   * @param a an {@link Assignability}; may be {@code null} in which case a default {@link Assignability} will be used
   * instead
   *
   * @param attributes a {@link List} of {@link NamedAttributeMap}s; must not be {@code null}
   *
   * @param box whether or not to box primitive types
   *
   * @param e a {@link VariableElement} whose {@link VariableElement#getKind() kind} property is {@link
   * javax.lang.model.element.ElementKind#FIELD FIELD} or {@link javax.lang.model.element.ElementKind#PARAMETER
   * PARAMETER}; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code e}'s {@link javax.lang.model.element.Element#getKind() kind} property
   * is neither {@link javax.lang.model.element.ElementKind#FIELD FIELD} nor {@link
   * javax.lang.model.element.ElementKind#PARAMETER PARAMETER}
   *
   * @exception NullPointerException if {@code attributes} or {@code e} is {@code null}
   */
  public Dependency(Assignability a,
                    final List<? extends NamedAttributeMap<?>> attributes,
                    final boolean box,
                    final VariableElement e) {
    super();
    if (a == null) {
      a = new Assignability();
    }
    this.e = DelegatingElement.of(e, a.typeAndElementSource());
    this.bsc = new BeanSelectionCriteria(a, this.e.asType(), List.copyOf(attributes), box);
    switch (this.e.getKind()) {
    case FIELD:
    case PARAMETER:
      break;
    default:
      throw new IllegalArgumentException("e: " + e);
    }
  }

  /**
   * Creates a new {@link Dependency}.
   *
   * @param bsc a {@link BeanSelectionCriteria}; must not be {@code null}
   *
   * @exception NullPointerException if {@code bsc} is {@code null}
   *
   * @see #Dependency(BeanSelectionCriteria, VariableElement)
   */
  public Dependency(final BeanSelectionCriteria bsc) {
    this(bsc, null);
  }

  /**
   * Creates a new {@link Dependency}.
   *
   * @param bsc a {@link BeanSelectionCriteria}; must not be {@code null}; its {@link BeanSelectionCriteria#type() type}
   * property will be ignored in favor of the type encapsulated by the supplied {@link VariableElement} argument
   * instead, if that argument is non-{@code null}
   *
   * @param e a {@link VariableElement} representing a field or executable parameter; may be {@code null}; if non-{@code
   * null}, must have a {@link javax.lang.model.element.Element#getKind() kind} property whose value is either {@link
   * javax.lang.model.element.ElementKind#FIELD FIELD} or {@link javax.lang.model.element.ElementKind#PARAMETER
   * PARAMETER}
   *
   * @exception IllegalArgumentException if {@code e} is non-{@code null} and {@code e}'s {@link
   * javax.lang.model.element.Element#getKind() kind} property is neither {@link
   * javax.lang.model.element.ElementKind#FIELD FIELD} nor {@link javax.lang.model.element.ElementKind#PARAMETER
   * PARAMETER}
   *
   * @exception NullPointerException if {@code bsc} is {@code null}
   */
  public Dependency(final BeanSelectionCriteria bsc, final VariableElement e) {
    super();
    if (e == null) {
      this.e = null;
      this.bsc = Objects.requireNonNull(bsc, "bsc");
    } else {
      final Assignability a = bsc.assignability();
      this.e = DelegatingElement.of(e, a.typeAndElementSource());
      this.bsc = new BeanSelectionCriteria(a, e == null ? bsc.type() : e.asType(), bsc.attributes(), bsc.box());
      switch (this.e.getKind()) {
      case FIELD:
      case PARAMETER:
        break;
      default:
        throw new IllegalArgumentException("e: " + e);
      }
    }
  }


  /*
   * Instance methods.
   */


  /**
   * Returns the {@link BeanSelectionCriteria} associated with this {@link Dependency}.
   *
   * <p>This method never returns {@code null}.
   *
   * @return the {@link BeanSelectionCriteria} associated with this {@link Dependency}; never {@code null}
   */
  public final BeanSelectionCriteria beanSelectionCriteria() {
    return this.bsc;
  }

  /**
   * Returns the {@link VariableElement} associated with this {@link Dependency}, or {@code null}.
   *
   * @return the {@link VariableElement} associated with this {@link Dependency}, or {@code null}
   */
  public final VariableElement element() {
    return this.e;
  }

  @Override // Constable
  public final Optional<DynamicConstantDesc<Dependency>> describeConstable() {
    return Lang.describeConstable((VariableElement)this.e)
      .flatMap(eDesc -> this.bsc.describeConstable()
               .map(bscDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                      MethodHandleDesc.ofConstructor(CD_Dependency,
                                                                                     CD_BeanSelectionCriteria,
                                                                                     CD_VariableElement),
                                                      bscDesc,
                                                      eDesc)));
  }

  @Override // Object
  public final int hashCode() {
    int hashCode = 17;
    hashCode = 37 * hashCode + this.beanSelectionCriteria().hashCode();
    hashCode = 37 * hashCode + this.element().hashCode();
    return hashCode;
  }

  @Override // Object
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other != null && other.getClass() == this.getClass()) {
      final Dependency her = (Dependency)other;
      return
        Objects.equals(this.beanSelectionCriteria(), her.beanSelectionCriteria()) &&
        Objects.equals(this.element(), her.element());
    } else {
      return false;
    }
  }

}
