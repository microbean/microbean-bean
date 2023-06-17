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
package org.microbean.bean;

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;

import java.lang.reflect.Type;

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

import org.microbean.lang.type.DelegatingTypeMirror;

import org.microbean.qualifier.NamedAttributeMap;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_Collection;
import static java.lang.constant.ConstantDescs.FALSE;
import static java.lang.constant.ConstantDescs.TRUE;
import static java.lang.constant.DirectMethodHandleDesc.Kind.STATIC;

import static org.microbean.bean.ConstantDescs.CD_Selector;

import static org.microbean.bean.InterceptorBindings.Kind.ANY_INTERCEPTOR_BINDING;
import static org.microbean.bean.InterceptorBindings.Kind.INTERCEPTOR_BINDING;
import static org.microbean.bean.Qualifiers.defaultQualifiers;
import static org.microbean.bean.Qualifiers.Kind.ANY_QUALIFIER;
import static org.microbean.bean.Qualifiers.Kind.DEFAULT_QUALIFIER;
import static org.microbean.bean.Qualifiers.Kind.QUALIFIER;

public final class Selector implements Constable {


  /*
   * Static fields.
   */


  private static final ClassDesc CD_Assignability = ClassDesc.of(Assignability.class.getName());

  private static final ClassDesc CD_TypeMirror = ClassDesc.of("javax.lang.model.type.TypeMirror");

  private static final Equality EQUALITY_IGNORING_ANNOTATIONS = new Equality(false);

  private static final Equality SAME_TYPE_EQUALITY = new SameTypeEquality();


  /*
   * Instance fields.
   */

  private final Assignability assignability;

  private final TypeMirror type;

  private final List<NamedAttributeMap<?>> attributes;

  private final boolean box;


  /*
   * Constructors.
   */


  public Selector(final Type type) {
    this(Lang.typeAndElementSource().type(type), defaultQualifiers(), true);
  }

  public Selector(final Type type, final List<? extends NamedAttributeMap<?>> attributes) {
    this(Lang.typeAndElementSource().type(type), attributes, true);
  }

  public Selector(final TypeMirror type) {
    this(type, defaultQualifiers(), true);
  }

  public Selector(final TypeMirror type, final List<? extends NamedAttributeMap<?>> attributes) {
    this(type, attributes, true);
  }

  public Selector(final TypeMirror type, final List<? extends NamedAttributeMap<?>> attributes, final boolean box) {
    this(new Assignability(), type, attributes, box);
  }

  public Selector(final Assignability assignability, final TypeMirror type, final List<? extends NamedAttributeMap<?>> attributes, final boolean box) {
    super();
    this.assignability = Objects.requireNonNull(assignability, "assignability");
    this.type = DelegatingTypeMirror.of(validateType(type, box), Lang.typeAndElementSource(), SAME_TYPE_EQUALITY);
    this.attributes = List.copyOf(attributes);
    this.box = box;
  }


  /*
   * Instance methods.
   */


  public final TypeMirror type() {
    return this.type;
  }

  public final List<NamedAttributeMap<?>> attributes() {
    return this.attributes;
  }

  public final boolean box() {
    return this.box;
  }

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

  public final boolean selects(final Selector selector) {
    return this.selects(List.of(selector.type()), selector.attributes());
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

  public final Selector withType(final TypeMirror type) {
    if (type == this.type()) {
      return this;
    }
    return new Selector(this.assignability, type, this.attributes(), this.box());
  }

  public final Selector withAttributes(final List<? extends NamedAttributeMap<?>> attributes) {
    if (attributes == this.attributes()) {
      return this;
    }
    return new Selector(this.assignability, this.type(), List.copyOf(attributes), this.box());
  }

  public final Selector withBox(final boolean box) {
    if (box == this.box()) {
      return this;
    }
    return new Selector(this.assignability, this.type(), this.attributes(), box);
  }

  private final boolean selectsInterceptorBindings(final Collection<? extends NamedAttributeMap<?>> attributes) {
    final List<? extends NamedAttributeMap<?>> herBindings = attributes.stream().filter(INTERCEPTOR_BINDING::describes).toList();
    if (herBindings.isEmpty()) {
      return this.interceptorBindings().isEmpty();
    } else if (herBindings.size() == 1 && ANY_INTERCEPTOR_BINDING.describes(herBindings.iterator().next())) {
      return true;
    }
    final List<NamedAttributeMap<?>> ibs = this.interceptorBindings();
    return ibs.size() == herBindings.size() && ibs.containsAll(herBindings) && herBindings.containsAll(ibs);
  }

  private final boolean selectsQualifiers(final Collection<? extends NamedAttributeMap<?>> attributes) {
    final Collection<? extends NamedAttributeMap<?>> myQualifiers = this.qualifiers();
    final List<? extends NamedAttributeMap<?>> herQualifiers = attributes.stream().filter(QUALIFIER::describes).toList();
    if (myQualifiers.isEmpty()) {
      // Pretend I had [@Default] and she had [@Default, @Any].
      return herQualifiers.isEmpty() || containsAll(herQualifiers::contains, defaultQualifiers());
    } else if (herQualifiers.isEmpty()) {
      for (final NamedAttributeMap<?> myQualifier : myQualifiers) {
        if (ANY_QUALIFIER.describes(myQualifier) || DEFAULT_QUALIFIER.describes(myQualifier)) {
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
    return this.assignability.matchesOne(this.type(), types);
  }

  /*
  private final TypeMirror selectedTypeFrom(final Collection<? extends TypeMirror> types) {
    for (final TypeMirror t : types) {
      if (this.assignability.matches(this.type(), t)) {
        return t;
      }
    }
    return null;
  }
  */

  /*
  @Deprecated(forRemoval = true)
  private final TypeMirror oldSelectedTypeFrom(final Collection<? extends TypeMirror> types) {
    if (types.isEmpty()) {
      return null;
    }
    // (myType will have been validated.)
    final TypeMirror myType = DelegatingTypeMirror.unwrap(this.type());
    for (TypeMirror type : types) {
      type = DelegatingTypeMirror.unwrap(type);
      TypeKind k = kind(type);
      if (this.box && k.isPrimitive()) {
        type = Lang.boxedClass((PrimitiveType)type).asType();
        k = kind(type);
        assert k == TypeKind.DECLARED;
      }
      if (switch (k) {
        case ARRAY, BOOLEAN, BYTE, CHAR, DECLARED, DOUBLE, FLOAT, INT, LONG, TYPEVAR, SHORT -> this.assignable(myType, type);
        case ERROR, EXECUTABLE, INTERSECTION, MODULE, NONE, NULL, OTHER, PACKAGE, UNION, VOID, WILDCARD ->
          throw new IllegalArgumentException("types: " + types + "; type: " + type);
        }) {
        return type;
      }
    }
    return null;
  }
  */

  /*
  @Deprecated(forRemoval = true)
  private final boolean assignable(TypeMirror receiver, TypeMirror payload) {
    final TypeAndElementSource tes = this.typeAndElementSource;
    return
      // Boxing, if necessary, has already happened.
      tes.sameType(receiver, payload) ||
      (tes.sameType(tes.erasure(elementType(receiver)), tes.erasure(elementType(payload))) &&
       // TODO: I think? maybe? this takes care of all the CDI anomalies, given a "legal" receiver and payload. Now we
       // should just be able to use stock assignability semantics.
       //
       // Note: the order of this method's parameters is counterintuitive.
       tes.assignable(payload, receiver));
  }
  */

  @Override // Constable
  public final Optional<DynamicConstantDesc<Selector>> describeConstable() {
    return Constables.describeConstable(this.assignability)
      .flatMap(assignabilityDesc -> Lang.describeConstable(this.type())
               .flatMap(typeDesc -> Constables.describeConstable(this.attributes())
                        .map(attributesDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                                      MethodHandleDesc.ofMethod(STATIC,
                                                                                                CD_Selector,
                                                                                                "of",
                                                                                                MethodTypeDesc.of(CD_Selector,
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
      final Selector her = (Selector)other;
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


  @Deprecated
  private static final TypeKind kind(final TypeMirror t) {
    return t.getKind();
  }

  private static final TypeMirror validateType(final TypeMirror type, final boolean box) {
    final TypeKind k = kind(type);
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
    return switch (kind(t)) {
    case ARRAY -> ((ArrayType)t).getComponentType();
    default -> t;
    };
  }

  // Returns the element type of t if t is an array type. Returns t in all other cases.
  private static final TypeMirror elementType(final TypeMirror t) {
    return switch (kind(t)) {
    case ARRAY -> elementType(componentType(t));
    default -> t;
    };
  }

  private static final boolean containsAll(final Predicate<? super Object> p, final Iterable<?> i) {
    for (final Object o : i) {
      if (p.test(o)) {
        continue;
      }
      return false;
    }
    return true;
  }

  // Called by describeConstable().
  public static final Selector of(final Assignability a,
                                  final TypeMirror type,
                                  final Collection<? extends NamedAttributeMap<?>> attributes,
                                  final boolean box) {
    return new Selector(a, type, List.copyOf(attributes), box);
  }


  /*
   * Inner and nested classes.
   */


  private static final class SameTypeEquality extends Equality {

    private SameTypeEquality() {
      super(false);
    }

    @Override
    public final boolean equals(final Object o1, final Object o2) {
      if (o1 == o2) {
        return true;
      } else if (o1 == null || o2 == null) {
        return false;
      } else if (o1 instanceof TypeMirror t1 && o2 instanceof TypeMirror t2) {
        return Lang.sameType(t1, t2);
      }
      return false;
    }

  }

}
