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

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import java.util.function.Function;
import java.util.function.Predicate;

import javax.lang.model.element.Element;

import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.microbean.constant.Constables;

import org.microbean.lang.Equality;
import org.microbean.lang.Lang;

import org.microbean.lang.type.DelegatingTypeMirror;

import org.microbean.qualifier.AttributeBearing;
import org.microbean.qualifier.NamedAttributeMap;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_Collection;
import static java.lang.constant.ConstantDescs.FALSE;
import static java.lang.constant.ConstantDescs.TRUE;
import static java.lang.constant.DirectMethodHandleDesc.Kind.STATIC;

import static org.microbean.bean.ConstantDescs.CD_Selector;

public final record Selector<V>(TypeMirror type, List<NamedAttributeMap<V>> attributes, boolean box) implements AttributeBearing<V>, Constable {


  /*
   * Static fields.
   */


  private static final ClassDesc CD_TypeMirror = ClassDesc.of("javax.lang.model.type.TypeMirror");

  private static final Equality EQUALITY_IGNORING_ANNOTATIONS = new Equality(false);

  private static final Equality SAME_TYPE_EQUALITY = new SameTypeEquality();


  /*
   * Constructors.
   */


  public Selector(final TypeMirror type, final List<? extends NamedAttributeMap<V>> attributes) {
    this(type, List.copyOf(attributes), true);
  }

  public Selector {
    attributes = List.copyOf(attributes);
    type = DelegatingTypeMirror.of(validateType(type, box), Selector::elementSource, SAME_TYPE_EQUALITY);
  }


  /*
   * Instance methods.
   */


  public final List<NamedAttributeMap<V>> interceptorBindings() {
    return InterceptorBindings.interceptorBindings(this.attributes());
  }

  public final List<NamedAttributeMap<V>> qualifiers() {
    return Qualifiers.qualifiers(this.attributes());
  }

  public final boolean selects(final Id id) {
    return this.selects(id.attributes(), id.types().types());
  }

  public final boolean selects(final Bean<?> bean) {
    return this.selects(bean.id().attributes(), bean.id().types().types());
  }

  public final boolean selects(final Selector<?> selector) {
    return this.selects(selector.attributes(), List.of(selector.type()));
  }

  public final boolean selects(final TypeMirror type) {
    return this.selects(List.of(), List.of(type));
  }

  public final boolean selects(final Iterable<? extends TypeMirror> types) {
    return this.selects(List.of(), types);
  }

  public final boolean selects(final Collection<? extends NamedAttributeMap<?>> attributes, final TypeMirror type) {
    return this.selects(attributes, List.of(type));
  }

  public final boolean selects(final Collection<? extends NamedAttributeMap<?>> attributes,
                               final Iterable<? extends TypeMirror> types) {
    System.out.println("*** selects qualifiers? (" + attributes + ") " + this.selectsQualifiers(attributes));
    System.out.println("*** selects interceptorBindings? (" + attributes + ") " + this.selectsInterceptorBindings(attributes));
    System.out.println("*** selects types? (" + types + ") " + this.selectsTypeFrom(types));
    return this.selectsQualifiers(attributes) && this.selectsInterceptorBindings(attributes) && this.selectsTypeFrom(types);
  }

  private final boolean selectsInterceptorBindings(final Collection<? extends NamedAttributeMap<?>> attributes) {
    final List<? extends NamedAttributeMap<?>> herBindings =
      attributes.stream().filter(InterceptorBindings.Kind.INTERCEPTOR_BINDING::describes).toList();
    if (herBindings.isEmpty()) {
      return this.interceptorBindings().isEmpty();
    } else if (herBindings.size() == 1 && InterceptorBindings.Kind.ANY_INTERCEPTOR_BINDING.describes(herBindings.iterator().next())) {
      return true;
    }
    final List<NamedAttributeMap<V>> ibs = this.interceptorBindings();
    return ibs.size() == herBindings.size() && ibs.containsAll(herBindings) && herBindings.containsAll(ibs);
  }

  private final boolean selectsQualifiers(final Collection<? extends NamedAttributeMap<?>> attributes) {
    final Collection<? extends NamedAttributeMap<V>> myQualifiers = this.qualifiers();
    final List<? extends NamedAttributeMap<?>> herQualifiers =
      attributes.stream().filter(Qualifiers.Kind.QUALIFIER::describes).toList();
    if (myQualifiers.isEmpty()) {
      return herQualifiers.isEmpty();
    } else if (herQualifiers.isEmpty()) {
      for (final NamedAttributeMap<V> myQualifier : myQualifiers) {
        if (Qualifiers.Kind.ANY_QUALIFIER.describes(myQualifier) || Qualifiers.Kind.DEFAULT_QUALIFIER.describes(myQualifier)) {
          return true;
        }
      }
      return false;
    } else if (herQualifiers.size() == 1) {
      return Qualifiers.Kind.ANY_QUALIFIER.describes(herQualifiers.get(0));
    } else {
      return containsAll(herQualifiers::contains, myQualifiers);
    }
  }

  private final boolean selectsTypeFrom(final Iterable<? extends TypeMirror> types) {
    for (TypeMirror type : types) {
      type = DelegatingTypeMirror.unwrap(type);
      final TypeKind k = type.getKind();
      if (k.isPrimitive()) {
        if (this.box) {
          type = Lang.boxedClass((PrimitiveType)type).asType();
          assert type.getKind() == TypeKind.DECLARED;
        }
        assert this.type().getKind() == TypeKind.DECLARED;
        return Lang.assignable(type, DelegatingTypeMirror.unwrap(this.type()));
      }
      if (switch (k) {
          // TODO: this applies Java type semantics; there are additional CDI restrictions that we need to take into account.
          // The ordering of the arguments to this method is counterintuitive; pay attention.
        case ARRAY, DECLARED, TYPEVAR, WILDCARD -> Lang.assignable(type, DelegatingTypeMirror.unwrap(this.type()));
        case ERROR, EXECUTABLE, INTERSECTION, MODULE, NONE, NULL, OTHER, PACKAGE, UNION, VOID -> false;
        case BOOLEAN, BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT -> throw new AssertionError("Should have already been handled");
        }) {
        return true;
      }
    }
    return false;
  }

  @Override // Constable
  public final Optional<DynamicConstantDesc<Selector<V>>> describeConstable() {
    return Constables.describeConstable(this.attributes())
      .flatMap(attributesDesc -> Lang.describeConstable(this.type())
               .map(typeDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                       MethodHandleDesc.ofMethod(STATIC,
                                                                                 CD_Selector,
                                                                                 "of",
                                                                                 MethodTypeDesc.of(CD_Selector,
                                                                                                   CD_TypeMirror,
                                                                                                   CD_Collection,
                                                                                                   CD_boolean)),
                                                       typeDesc,
                                                       attributesDesc,
                                                       this.box ? TRUE : FALSE)));
  }


  /*
   * Static methods.
   */


  private static TypeMirror validateType(final TypeMirror type, final boolean box) {
    TypeKind k = type.getKind();
    if (box && k.isPrimitive()) {
      return Lang.boxedClass((PrimitiveType)type).asType();
    }
    return switch (k) {
    case ARRAY, BOOLEAN, BYTE, CHAR, DECLARED, DOUBLE, FLOAT, INT, LONG, SHORT, TYPEVAR, WILDCARD -> type;
    case ERROR, EXECUTABLE, INTERSECTION, MODULE, NONE, NULL, OTHER, PACKAGE, UNION, VOID ->
      throw new IllegalArgumentException("type: " + type);
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
  public static final <V> Selector<V> of(final Collection<? extends NamedAttributeMap<V>> attributes,
                                         final TypeMirror type,
                                         final boolean box) {
    return new Selector<>(type, List.copyOf(attributes), box);
  }

  private static final Element elementSource(final String moduleName, final String typeName) {
    return Lang.typeElement(Lang.moduleElement(moduleName), typeName);
  }

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
