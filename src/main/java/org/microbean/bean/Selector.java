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

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.microbean.constant.Constables;

import org.microbean.lang.ElementSource;
import org.microbean.lang.Equality;
import org.microbean.lang.Lang;

import org.microbean.lang.type.Types;

import org.microbean.lang.visitor.AsSuperVisitor;
import org.microbean.lang.visitor.AssignableVisitor;
import org.microbean.lang.visitor.CaptureVisitor;
import org.microbean.lang.visitor.ContainsTypeVisitor;
import org.microbean.lang.visitor.ConvertibleVisitor;
import org.microbean.lang.visitor.EraseVisitor;
import org.microbean.lang.visitor.MemberTypeVisitor;
import org.microbean.lang.visitor.PrecedesPredicate;
import org.microbean.lang.visitor.SameTypeVisitor;
import org.microbean.lang.visitor.SubtypeUncheckedVisitor;
import org.microbean.lang.visitor.SubtypeVisitor;
import org.microbean.lang.visitor.SupertypeVisitor;
import org.microbean.lang.visitor.TypeClosureVisitor;
import org.microbean.lang.visitor.Visitors;

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

import static org.microbean.bean.InterceptorBindings.Kind.ANY_INTERCEPTOR_BINDING;
import static org.microbean.bean.InterceptorBindings.Kind.INTERCEPTOR_BINDING;
import static org.microbean.bean.Qualifiers.Kind.ANY_QUALIFIER;
import static org.microbean.bean.Qualifiers.Kind.DEFAULT_QUALIFIER;
import static org.microbean.bean.Qualifiers.Kind.QUALIFIER;

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


  @Override
  public final TypeMirror type() {
    return this.type; // TODO: or possibly DelegatingTypeMirror.unwrap(this.type)?
  }
  
  public final List<NamedAttributeMap<V>> interceptorBindings() {
    return InterceptorBindings.interceptorBindings(this.attributes());
  }

  public final List<NamedAttributeMap<V>> qualifiers() {
    return Qualifiers.qualifiers(this.attributes());
  }

  public final boolean selects(final Id id) {
    return this.selects(id.types().types(), id.attributes());
  }

  public final boolean selects(final Bean<?> bean) {
    return this.selects(bean.id().types().types(), bean.id().attributes());
  }

  public final boolean selects(final Selector<?> selector) {
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

  private final boolean selectsInterceptorBindings(final Collection<? extends NamedAttributeMap<?>> attributes) {
    final List<? extends NamedAttributeMap<?>> herBindings = attributes.stream().filter(INTERCEPTOR_BINDING::describes).toList();
    if (herBindings.isEmpty()) {
      return this.interceptorBindings().isEmpty();
    } else if (herBindings.size() == 1 && ANY_INTERCEPTOR_BINDING.describes(herBindings.iterator().next())) {
      return true;
    }
    final List<NamedAttributeMap<V>> ibs = this.interceptorBindings();
    return ibs.size() == herBindings.size() && ibs.containsAll(herBindings) && herBindings.containsAll(ibs);
  }

  private final boolean selectsQualifiers(final Collection<? extends NamedAttributeMap<?>> attributes) {
    final Collection<? extends NamedAttributeMap<V>> myQualifiers = this.qualifiers();
    final List<? extends NamedAttributeMap<?>> herQualifiers = attributes.stream().filter(QUALIFIER::describes).toList();
    if (myQualifiers.isEmpty()) {
      return herQualifiers.isEmpty();
    } else if (herQualifiers.isEmpty()) {
      for (final NamedAttributeMap<V> myQualifier : myQualifiers) {
        if (ANY_QUALIFIER.describes(myQualifier) || DEFAULT_QUALIFIER.describes(myQualifier)) {
          return true;
        }
      }
      return false;
    } else if (herQualifiers.size() == 1) {
      return ANY_QUALIFIER.describes(herQualifiers.get(0));
    } else {
      return containsAll(herQualifiers::contains, myQualifiers);
    }
  }

  final boolean selectsTypeFrom(final Collection<? extends TypeMirror> types) {
    return selectedTypeFrom(types) != null;
  }

  private final TypeMirror selectedTypeFrom(final Collection<? extends TypeMirror> types) {
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
  
  private final boolean assignable(TypeMirror receiver, TypeMirror payload) {
    /*
    final class VisitorsHolder {
      private static final Visitors visitors = new Visitors((m, n) -> Lang.typeElement(Lang.moduleElement(m), n));
      static {
        final ElementSource es = (m, n) -> Lang.typeElement(Lang.moduleElement(m), n);
        final Types types = new Types(es);
        final boolean subtypeCapture = false;
        final boolean wildcardsCompatible = true;

        // No special bean semantics here.
        final EraseVisitor eraseVisitor = new EraseVisitor(es, types);
        final ContainsTypeVisitor containsTypeVisitor = new ContainsTypeVisitor(es, types); // not valid till passed to a SubtypeVisitor constructor
        final SupertypeVisitor supertypeVisitor = new SupertypeVisitor(es, types, eraseVisitor);
        final AsSuperVisitor asSuperVisitor = new AsSuperVisitor(es, null, types, supertypeVisitor);
        
        // Bean semantics for these guys
        final SupertypeVisitor beanSupertypeVisitor = new SupertypeVisitor(es, types, eraseVisitor, t -> false);
        final AsSuperVisitor beanAsSuperVisitor = new AsSuperVisitor(es, null, types, beanSupertypeVisitor);

        // No special bean semantics here.
        final MemberTypeVisitor memberTypeVisitor = new MemberTypeVisitor(es, null, types, asSuperVisitor, eraseVisitor, supertypeVisitor);

        // No special bean semantics here.
        final SameTypeVisitor sameTypeVisitor = new SameTypeVisitor(es, containsTypeVisitor, supertypeVisitor, wildcardsCompatible);

        // No special bean semantics here.
        final CaptureVisitor captureVisitor = new CaptureVisitor(es, null, types, supertypeVisitor, memberTypeVisitor);

        final SubtypeVisitor subtypeVisitor =
          new SubtypeVisitor(es,
                             null,
                             types,
                             asSuperVisitor,
                             supertypeVisitor,
                             sameTypeVisitor,
                             containsTypeVisitor,
                             captureVisitor,
                             subtypeCapture);
        final SubtypeUncheckedVisitor subtypeUncheckedVisitor = new SubtypeUncheckedVisitor(types, subtypeVisitor, asSuperVisitor, sameTypeVisitor, subtypeCapture);
        final ConvertibleVisitor convertibleVisitor = new ConvertibleVisitor(types, subtypeUncheckedVisitor, subtypeVisitor);
        final AssignableVisitor assignableVisitor = new AssignableVisitor(types, convertibleVisitor);

        final SubtypeVisitor beanSubtypeVisitor =
          new SubtypeVisitor(es,
                             null,
                             types,
                             beanAsSuperVisitor,
                             beanSupertypeVisitor,
                             sameTypeVisitor,
                             containsTypeVisitor,
                             captureVisitor,
                             subtypeCapture);
        final SubtypeUncheckedVisitor beanSubtypeUncheckedVisitor = new SubtypeUncheckedVisitor(types, beanSubtypeVisitor, beanAsSuperVisitor, sameTypeVisitor, subtypeCapture);
        final ConvertibleVisitor beanConvertibleVisitor = new ConvertibleVisitor(types, beanSubtypeUncheckedVisitor, beanSubtypeVisitor);
        final AssignableVisitor beanAssignableVisitor = new AssignableVisitor(types, beanConvertibleVisitor);

        final PrecedesPredicate precedesPredicate = new PrecedesPredicate(null, supertypeVisitor, subtypeVisitor);
        final TypeClosureVisitor typeClosureVisitor = new TypeClosureVisitor(es, supertypeVisitor, precedesPredicate);
        captureVisitor.setTypeClosureVisitor(typeClosureVisitor);
        
      }
    }
    */
    // Boxing, if necessary, has already happened.
    if (Lang.sameType(receiver, payload)) {
      return true;
    }
    if (Lang.sameType(Lang.erasure(elementType(receiver)), Lang.erasure(elementType(payload)))) {
      // TODO: I think? maybe? this takes care of all the CDI anomalies, given a "legal" receiver and payload. Now we
      // should just be able to use stock assignability semantics.
      //
      // The order of this method's parameters is counterintuitive; pay attention.
      return Lang.assignable(payload, receiver);
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


  private static final TypeKind kind(final TypeMirror t) {
    return Lang.kind(t);
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
