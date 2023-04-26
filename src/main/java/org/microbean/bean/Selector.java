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
import java.util.Objects;
import java.util.Optional;

import java.util.function.Function;
import java.util.function.Predicate;

import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.microbean.constant.Constables;

import org.microbean.lang.Lang;

import org.microbean.qualifier.AttributeBearing;
import org.microbean.qualifier.NamedAttributeMap;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_Collection;
import static java.lang.constant.ConstantDescs.FALSE;
import static java.lang.constant.ConstantDescs.TRUE;
import static java.lang.constant.DirectMethodHandleDesc.Kind.STATIC;

public final class Selector<V> implements AttributeBearing<V>, Constable {


  /*
   * Static fields.
   */


  private static final ClassDesc CD_Selector = ClassDesc.of("org.microbean.bean.Selector");

  private static final ClassDesc CD_TypeMirror = ClassDesc.of("javax.lang.model.type.TypeMirror");


  /*
   * Instance fields.
   */


  private final List<NamedAttributeMap<V>> attributes;

  private final TypeMirror type;

  private final boolean box;

  /*
   * Constructors.
   */


  public Selector(final Collection<? extends NamedAttributeMap<V>> attributes, final TypeMirror type, final boolean box) {
    super();
    this.box = box;
    this.attributes = List.copyOf(attributes);
    this.type = validateType(type, box);

  }


  /*
   * Instance methods.
   */


  @Override // AttributeBearing<V>
  public final List<NamedAttributeMap<V>> attributes() {
    return this.attributes;
  }

  public final List<NamedAttributeMap<V>> interceptorBindings() {
    return this.attributes().stream().filter(AttributeKind.INTERCEPTOR_BINDING::describes).toList();
  }

  public final List<NamedAttributeMap<V>> qualifiers() {
    return this.attributes().stream().filter(AttributeKind.QUALIFIER::describes).toList();
  }

  public final TypeMirror type() {
    return this.type;
  }

  public final boolean selects(final Selector<?> selector) {
    return this.selects(selector.attributes(), selector.type());
  }

  public final boolean selects(final Collection<? extends NamedAttributeMap<?>> attributes, final TypeMirror type) {
    return this.selectsQualifiers(attributes) && this.selectsInterceptorBindings(attributes) && this.selectsType(type);
  }

  private final boolean selectsInterceptorBindings(final Collection<? extends NamedAttributeMap<?>> herBindings) {
    if (herBindings.size() == 1 && AttributeKind.ANY_INTERCEPTOR_BINDING.describes(herBindings.iterator().next())) {
      return true;
    }
    return this.interceptorBindings().equals(herBindings);
  }

  private final boolean selectsQualifiers(final Collection<? extends NamedAttributeMap<?>> attributes) {
    final Collection<? extends NamedAttributeMap<V>> myQualifiers = this.qualifiers();
    final List<? extends NamedAttributeMap<?>> herQualifiers = attributes.stream().filter(AttributeKind.QUALIFIER::describes).toList();
    if (myQualifiers.isEmpty()) {
      return herQualifiers.isEmpty();
    } else if (herQualifiers.isEmpty()) {
      for (final NamedAttributeMap<V> a : myQualifiers) {
        if (AttributeKind.ANY_QUALIFIER.describes(a) || AttributeKind.DEFAULT_QUALIFIER.describes(a)) {
          return true;
        }
      }
      return false;
    } else if (herQualifiers.size() == 1) {
      return AttributeKind.ANY_QUALIFIER.describes(herQualifiers.get(0));
    } else {
      return containsAll(herQualifiers::contains, myQualifiers);
    }
  }

  private final boolean selectsType(TypeMirror type) {
    final TypeKind k = type.getKind();
    if (k.isPrimitive()) {
      if (this.box) {
        type = Lang.types().boxedClass((PrimitiveType)type).asType();
        assert type.getKind() == TypeKind.DECLARED;
      }
      assert this.type().getKind() == TypeKind.DECLARED;
      return Lang.types().isAssignable(type, this.type());
    }
    return switch (k) {
      // TODO: this applies Java type semantics; there are additional CDI restrictions that we need to take into account.
      // The ordering of the arguments to this method is counterintuitive; pay attention.
    case ARRAY, BOOLEAN, BYTE, CHAR, DECLARED, DOUBLE, FLOAT, INT, LONG, SHORT, TYPEVAR, WILDCARD -> Lang.types().isAssignable(type, this.type());
    case ERROR, EXECUTABLE, INTERSECTION, MODULE, NONE, NULL, OTHER, PACKAGE, UNION, VOID -> false;
    };
  }

  @Override // Constable
  public final Optional<DynamicConstantDesc<Selector<V>>> describeConstable() {
    return Constables.describeConstable(this.attributes())
      .flatMap(attributesDesc -> Constables.describeConstable(this.type())
               .map(typeDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                       MethodHandleDesc.ofMethod(STATIC,
                                                                                 CD_Selector,
                                                                                 "of",
                                                                                 MethodTypeDesc.of(CD_Selector,
                                                                                                   CD_Collection,
                                                                                                   CD_TypeMirror,
                                                                                                   CD_boolean)),
                                                       attributesDesc,
                                                       typeDesc,
                                                       this.box ? TRUE : FALSE)));
  }

  @Override // Object
  public final int hashCode() {
    int hashCode = 17;
    hashCode = 31 * hashCode + this.attributes().hashCode();
    hashCode = 31 * hashCode + this.type().hashCode();
    return hashCode;
  }

  @Override // Object
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other != null && other.getClass() == this.getClass()) {
      final Selector<?> her = (Selector<?>)other;
      return
        this.attributes().equals(her.attributes()) &&
        this.type().equals(her.type());
    } else {
      return false;
    }
  }

  @Override // Object
  public final String toString() {
    return new StringBuilder(this.getClass().getSimpleName())
      .append("[attributes=")
      .append(this.attributes())
      .append(", type=")
      .append(this.type())
      .append("]")
      .toString();
  }


  /*
   * Static methods.
   */

  private static TypeMirror validateType(final TypeMirror type, final boolean box) {
    TypeKind k = type.getKind();
    if (box && k.isPrimitive()) {
      return Lang.types().boxedClass((PrimitiveType)type).asType();
    }
    return switch (k) {
    case ARRAY, BOOLEAN, BYTE, CHAR, DECLARED, DOUBLE, FLOAT, INT, LONG, SHORT, TYPEVAR, WILDCARD -> type;
    case ERROR, EXECUTABLE, INTERSECTION, MODULE, NONE, NULL, OTHER, PACKAGE, UNION, VOID -> throw new IllegalArgumentException("type: " + type);
    };
  }

  private static final boolean containsAll(final Predicate<? super Object> p, final Iterable<?> i) {
    if (p == null || i == null) {
      return false;
    }
    for (final Object o : i) {
      if (!p.test(o)) {
        return false;
      }
    }
    return true;
  }

  // Called by describeConstable().
  public static final <V> Selector<V> of(final Collection<? extends NamedAttributeMap<V>> attributes,
                                         final TypeMirror type,
                                         final boolean box) {
    return new Selector<>(attributes, type, box);
  }


  /*
   * Inner and nested classes.
   */


  public enum AttributeKind {

    ANY_INTERCEPTOR_BINDING() {
      @Override
      public final boolean describes(final NamedAttributeMap<?> a) {
        if (a == null) {
          return false;
        }
        for (final NamedAttributeMap<?> m : a.metadata()) {
          if (m.name().equalsIgnoreCase("any") && m.containsKey("interceptorBinding")) {
            return true;
          }
        }
        return false;
      }
    },
    ANY_QUALIFIER() {
      @Override
      public final boolean describes(final NamedAttributeMap<?> a) {
        if (a == null) {
          return false;
        }
        for (final NamedAttributeMap<?> m : a.metadata()) {
          if (m.name().equalsIgnoreCase("any") && m.containsKey("qualifier")) {
            return true;
          }
        }
        return false;
      }
    },
    DEFAULT_QUALIFIER() {
      @Override
      public final boolean describes(final NamedAttributeMap<?> a) {
        if (a == null) {
          return false;
        }
        for (final NamedAttributeMap<?> m : a.metadata()) {
          if (m.name().equalsIgnoreCase("default") && m.containsKey("qualifier")) {
            return true;
          }
        }
        return false;
      }
    },
    INTERCEPTOR_BINDING() {
      @Override
        public final boolean describes(final NamedAttributeMap<?> a) {
        if (a == null) {
          return false;
        }
        for (final NamedAttributeMap<?> m : a.metadata()) {
          if (m.containsKey("interceptorBinding")) {
            return true;
          }
        }
        return false;
      }
    },
    QUALIFIER() {
      @Override
      public final boolean describes(final NamedAttributeMap<?> a) {
        if (a == null) {
          return false;
        }
        for (final NamedAttributeMap<?> m : a.metadata()) {
          if (m.containsKey("qualifier")) {
            return true;
          }
        }
        return false;
      }
    },
    OTHER() {
      @Override
      public final boolean describes(final NamedAttributeMap<?> a) {
        if (a == null) {
          return false;
        }
        for (final AttributeKind k : nonOther) {
          if (k.describes(a)) {
            return false;
          }
        }
        return true;
      }
    };

    private AttributeKind() {
    }

    public abstract boolean describes(final NamedAttributeMap<?> a);

    private static final EnumSet<AttributeKind> nonOther = EnumSet.complementOf(EnumSet.of(OTHER));

  }

}
