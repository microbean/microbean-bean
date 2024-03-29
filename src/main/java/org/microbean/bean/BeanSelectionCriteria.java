/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2024 microBean™.
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

import java.lang.constant.Constable;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import java.util.function.Predicate;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.microbean.constant.Constables;

import org.microbean.lang.Equality;
import org.microbean.lang.Lang;
import org.microbean.lang.Lang.SameTypeEquality;

import org.microbean.lang.type.DelegatingTypeMirror;

import org.microbean.qualifier.NamedAttributeMap;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_Collection;
import static java.lang.constant.ConstantDescs.FALSE;
import static java.lang.constant.ConstantDescs.TRUE;
import static java.lang.constant.DirectMethodHandleDesc.Kind.STATIC;

import static org.microbean.bean.ConstantDescs.CD_Assignability;
import static org.microbean.bean.ConstantDescs.CD_BeanSelectionCriteria;
import static org.microbean.bean.InterceptorBindings.anyInterceptorBinding;
import static org.microbean.bean.Qualifiers.anyQualifier;
import static org.microbean.bean.Qualifiers.defaultQualifier;
import static org.microbean.bean.Qualifiers.defaultQualifiers;

import static org.microbean.lang.ConstantDescs.CD_TypeMirror;

public final record BeanSelectionCriteria(Assignability assignability, // not included in equality/hashcode
                                          TypeMirror type,
                                          List<NamedAttributeMap<?>> attributes,
                                          boolean box)
  implements Constable {


  /*
   * Static fields.
   */


  private static final Equality EQUALITY_IGNORING_ANNOTATIONS = new Equality(false);


  /*
   * Instance fields.
   */


  /*
   * Constructors.
   */


  public BeanSelectionCriteria(final TypeMirror type) {
    this(new Assignability(), type);
  }

  public BeanSelectionCriteria(final TypeMirror type, final List<NamedAttributeMap<?>> attributes) {
    this(new Assignability(), type, attributes);
  }

  public BeanSelectionCriteria(final Assignability assignability, final TypeMirror type) {
    this(assignability, type, defaultQualifiers());
  }

  public BeanSelectionCriteria(final Assignability assignability,
                               final TypeMirror type,
                               final List<NamedAttributeMap<?>> attributes) {
    this(assignability, type, attributes, true);
  }

  public BeanSelectionCriteria {
    if (assignability == null) {
      assignability = new Assignability();
    }
    type = DelegatingTypeMirror.of(validateType(type, box), assignability.typeAndElementSource(), SameTypeEquality.INSTANCE);
    attributes = List.copyOf(attributes);
  }


  /*
   * Instance methods.
   */


  public final List<NamedAttributeMap<?>> interceptorBindings() {
    return InterceptorBindings.interceptorBindings(this.attributes());
  }

  public final List<NamedAttributeMap<?>> qualifiers() {
    return Qualifiers.qualifiers(this.attributes());
  }

  public final boolean selects(final Id id) {
    return this.selects(id.types().types(), id.attributes());
  }

  public final boolean selects(final Bean<?> bean) {
    return this.selects(bean.id().types().types(), bean.id().attributes());
  }

  public final boolean selects(final BeanSelectionCriteria beanSelectionCriteria) {
    return this.selects(List.of(beanSelectionCriteria.type()), beanSelectionCriteria.attributes());
  }

  public final boolean selects(final TypeMirror type) {
    return this.selects(List.of(type), List.of());
  }

  public final boolean selects(final Collection<? extends TypeMirror> types) {
    return this.selects(types, List.of());
  }

  public final boolean selects(final TypeMirror type, final Collection<? extends NamedAttributeMap<?>> attributes) {
    return this.selects(List.of(type), attributes);
  }

  public final boolean selects(final Collection<? extends TypeMirror> types,
                               final Collection<? extends NamedAttributeMap<?>> attributes) {
    return this.selectsQualifiers(attributes) && this.selectsInterceptorBindings(attributes) && this.selectsTypeFrom(types);
  }

  private final boolean selectsInterceptorBindings(final Collection<? extends NamedAttributeMap<?>> attributes) {
    final List<? extends NamedAttributeMap<?>> herBindings = InterceptorBindings.interceptorBindings(attributes);
    if (herBindings.isEmpty()) {
      return this.interceptorBindings().isEmpty();
    } else if (herBindings.size() == 1 && anyInterceptorBinding(herBindings.iterator().next())) {
      return true;
    }
    final List<NamedAttributeMap<?>> ibs = this.interceptorBindings();
    return ibs.size() == herBindings.size() && ibs.containsAll(herBindings) && herBindings.containsAll(ibs);
  }

  private final boolean selectsQualifiers(final Collection<? extends NamedAttributeMap<?>> attributes) {
    final Collection<? extends NamedAttributeMap<?>> myQualifiers = this.qualifiers();
    final List<? extends NamedAttributeMap<?>> herQualifiers = Qualifiers.qualifiers(attributes);
    if (myQualifiers.isEmpty()) {
      // Pretend I had [@Default] and she had [@Default, @Any].
      return herQualifiers.isEmpty() || containsAll(herQualifiers::contains, defaultQualifiers());
    } else if (herQualifiers.isEmpty()) {
      for (final NamedAttributeMap<?> myQualifier : myQualifiers) {
        if (anyQualifier(myQualifier) || defaultQualifier(myQualifier)) {
          // I had [@Default] or [@Any] or [@Default, @Any]; pretend she had [@Default, @Any].
          return true;
        }
      }
      return false;
    } else {
      return containsAll(herQualifiers::contains, myQualifiers);
    }
  }

  final boolean selectsTypeFrom(final Collection<? extends TypeMirror> types) {
    return this.assignability.oneMatches(this.type(), types);
  }

  @Override // Constable
  public final Optional<DynamicConstantDesc<BeanSelectionCriteria>> describeConstable() {
    return this.assignability().describeConstable()
      .flatMap(assignabilityDesc -> Lang.describeConstable(this.type())
               .flatMap(typeDesc -> Constables.describeConstable(this.attributes())
                        .map(attributesDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                                      MethodHandleDesc.ofMethod(STATIC,
                                                                                                CD_BeanSelectionCriteria,
                                                                                                "of",
                                                                                                MethodTypeDesc.of(CD_BeanSelectionCriteria,
                                                                                                                  CD_Assignability,
                                                                                                                  CD_TypeMirror,
                                                                                                                  CD_Collection,
                                                                                                                  CD_boolean)),
                                                                      assignabilityDesc,
                                                                      typeDesc,
                                                                      attributesDesc,
                                                                      this.box ? TRUE : FALSE))));
  }

  @Override // Object
  public final int hashCode() {
    int hashCode = 17;
    hashCode = 31 * hashCode + this.type().hashCode();
    hashCode = 31 * hashCode + this.attributes().hashCode();
    hashCode = 31 * hashCode + (this.box() ? 1 : 0);
    return hashCode;
  }

  @Override // Object
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other != null && other.getClass() == this.getClass()) {
      final BeanSelectionCriteria her = (BeanSelectionCriteria)other;
      return
        Objects.equals(this.type(), her.type()) &&
        Objects.equals(this.attributes(), her.attributes()) &&
        Objects.equals(this.box(), her.box());
    } else {
      return false;
    }
  }

  @Override
  public final String toString() {
    return
      this.getClass().getSimpleName() + "[" +
      "type=" + this.type() + ", " +
      "attributes=" + this.attributes() + ", " +
      "box=" + this.box() +
      "]";
  }


  /*
   * Static methods.
   */


  private static final TypeMirror validateType(final TypeMirror type, final boolean box) {
    final TypeKind k = type.getKind();
    if (box && k.isPrimitive()) {
      return Lang.boxedClass((PrimitiveType)type).asType();
    }
    return switch (k) {
    case ARRAY, BOOLEAN, BYTE, CHAR, DECLARED, DOUBLE, FLOAT, INT, LONG, SHORT -> type;
    case ERROR, EXECUTABLE, INTERSECTION, MODULE, NONE, NULL, OTHER, PACKAGE, TYPEVAR, UNION, VOID, WILDCARD ->
      throw new IllegalArgumentException("type: " + type);
    };
  }

  // Returns the component type of t if is an array type. Returns t in all other cases.
  private static final TypeMirror componentType(final TypeMirror t) {
    return t.getKind() == TypeKind.ARRAY ? ((ArrayType)t).getComponentType() : t;
  }

  // Returns the element type of t if t is an array type. Returns t in all other cases.
  private static final TypeMirror elementType(final TypeMirror t) {
    return t.getKind() == TypeKind.ARRAY ? elementType(componentType(t)) : t;
  }

  private static final boolean containsAll(final Predicate<? super Object> p, final Iterable<?> i) {
    for (final Object o : i) {
      if (!p.test(o)) {
        return false;
      }
    }
    return true;
  }

  // Called by describeConstable().
  public static final BeanSelectionCriteria of(final Assignability a,
                                               final TypeMirror type,
                                               final Collection<? extends NamedAttributeMap<?>> attributes,
                                               final boolean box) {
    return new BeanSelectionCriteria(a, type, List.copyOf(attributes), box);
  }

}
