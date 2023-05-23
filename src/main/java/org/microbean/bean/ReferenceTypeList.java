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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.QualifiedNameable;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import org.microbean.constant.Constables;

import org.microbean.lang.ElementSource;
import org.microbean.lang.Equality;
import org.microbean.lang.Lang;

import org.microbean.lang.type.DelegatingTypeMirror;

import org.microbean.lang.visitor.Visitors;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_Collection;

import static org.microbean.bean.ConstantDescs.CD_ReferenceTypeList;

public final class ReferenceTypeList implements Constable {

  private static final ClassDesc CD_ElementSource = ClassDesc.of(ElementSource.class.getName());

  private static final Equality SAME_TYPE_EQUALITY = new SameTypeEquality();

  private final List<TypeMirror> types; // all DelegatingTypeMirrors

  // We shouldn't have to keep this around, but we need to for describeConstable().
  private final ElementSource elementSource;

  private final int interfaceIndex;

  public ReferenceTypeList(final Collection<? extends TypeMirror> types) {
    this(types, ReferenceTypeList::seen, ReferenceTypeList::validateType, Lang.elementSource());
  }

  public ReferenceTypeList(final Collection<? extends TypeMirror> types, final ElementSource elementSource) {
    this(types, ReferenceTypeList::seen, ReferenceTypeList::validateType, elementSource);
  }

  public ReferenceTypeList(final Collection<? extends TypeMirror> types,
                           BiPredicate<Iterable<? extends TypeMirror>, TypeMirror> seen,
                           Predicate<? super TypeMirror> typeFilter,
                           final ElementSource elementSource) {
    super();
    this.elementSource = elementSource == null ? Lang.elementSource() : elementSource;
    if (types.isEmpty()) {
      this.types = List.of();
      this.interfaceIndex = -1;
    } else {
      if (seen == null) {
        seen = ReferenceTypeList::seen;
      }
      if (typeFilter == null) {
        typeFilter = ReferenceTypeList::validateType;
      }
      final List<DelegatingTypeMirror> newTypes = new ArrayList<>(types.size());
      for (final TypeMirror t : types) {
        final DelegatingTypeMirror dt = DelegatingTypeMirror.of(t, this.elementSource, SAME_TYPE_EQUALITY);
        if (!seen.test(newTypes, dt) && typeFilter.test(dt)) {
          newTypes.add(dt);
        }
      }
      Collections.sort(newTypes, new TypeMirrorComparator(this.elementSource));
      int interfaceIndex = -1;
      for (int i = 0; i < newTypes.size(); i++) {
        if (isInterface(newTypes.get(i))) {
          interfaceIndex = i;
          break;
        }
      }
      this.types = Collections.unmodifiableList(newTypes);
      this.interfaceIndex = interfaceIndex;
    }
  }

  @Override // Constable
  public final Optional<DynamicConstantDesc<ReferenceTypeList>> describeConstable() {
    return Constables.describeConstable(this.types(), Lang::describeConstable)
      .flatMap(typesDesc -> Constables.describeConstable(this.elementSource)
               .map(elementSourceDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                                MethodHandleDesc.ofConstructor(CD_ReferenceTypeList,
                                                                                               CD_Collection,
                                                                                               CD_ElementSource),
                                                                typesDesc,
                                                                elementSourceDesc)));
  }

  public final List<TypeMirror> types() {
    return this.types;
  }

  public final List<TypeMirror> classTypes() {
    if (this.interfaceIndex < 0) {
      return this.types;
    } else if (this.interfaceIndex == 0) {
      return List.of();
    } else {
      final List<TypeMirror> sublist = this.types.subList(0, this.interfaceIndex);
      final List<TypeMirror> list = new ArrayList<>(sublist.size() + 1);
      list.addAll(sublist);
      list.add(this.types.get(this.types.size() - 1)); // Object
      return Collections.unmodifiableList(list);
    }               
  }

  public final List<TypeMirror> interfaceTypes() {
    return
      this.interfaceIndex < 0 ? List.of() :
      this.interfaceIndex == 0 ? this.types :
      this.types.subList(this.interfaceIndex, this.types.size() - 1); // Object will be last; omit it
  }

  @Override // Object
  public final int hashCode() {
    int hashCode = 17;
    return 31 * hashCode + this.types().hashCode(); // each TypeMirror will be a DelegatingTypeMirror using value-based hashCode semantics
  }

  @Override // Object
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other != null && other.getClass() == this.getClass()) {
      final ReferenceTypeList her = (ReferenceTypeList)other;
      return Objects.equals(this.types(), her.types()); // each TypeMirror will be a DelegatingTypeMirror using proper equality semantics
    } else {
      return false;
    }
  }

  @Override // Object
  public final String toString() {
    return String.valueOf(this.types());
  }


  /*
   * Static methods.
   */


  public static final ReferenceTypeList closure(final TypeMirror t) {
    return closure(t, ReferenceTypeList::seen, ReferenceTypeList::validateType, new Visitors(Lang.elementSource()));
  }

  public static final ReferenceTypeList closure(final TypeMirror t, final Visitors visitors) {
    return closure(t, ReferenceTypeList::seen, ReferenceTypeList::validateType, visitors);
  }

  public static final ReferenceTypeList closure(final TypeMirror t,
                                                BiPredicate<Iterable<? extends TypeMirror>, TypeMirror> seen,
                                                Predicate<? super TypeMirror> typeFilter,
                                                final ElementSource elementSource) {
    return closure(t, seen, typeFilter, new Visitors(elementSource));
  }

  public static final ReferenceTypeList closure(final TypeMirror t,
                                                BiPredicate<Iterable<? extends TypeMirror>, TypeMirror> seen,
                                                Predicate<? super TypeMirror> typeFilter,
                                                final Visitors visitors) {
    return new ReferenceTypeList(visitors.typeClosureVisitor().visit(t).toList(),
                                 seen,
                                 typeFilter,
                                 visitors.elementSource());
  }

  public static final int specializationDepth(final TypeMirror t, final ElementSource elementSource) {
    return specializationDepth(DelegatingTypeMirror.of(t, elementSource, SAME_TYPE_EQUALITY), elementSource);
  }

  @SuppressWarnings("fallthrough")
  private static final int specializationDepth(final DelegatingTypeMirror t, final ElementSource elementSource) {

    final TypeKind k = t.getKind();

    // See
    // https://github.com/openjdk/jdk/blob/2e340e855b760e381793107f2a4d74095bd40199/src/jdk.compiler/share/classes/com/sun/tools/javac/code/Types.java#L3570-L3615.
    switch (k) {
    case DECLARED:
      if (((QualifiedNameable)((DeclaredType)t).asElement()).getQualifiedName().contentEquals("java.lang.Object")) {
        return 0;
      }
      // fall through
    // case INTERSECTION:
    case ARRAY:
    case TYPEVAR:
      int sd = 0;
      for (final TypeMirror s : Lang.directSupertypes(t)) {

        // The directSupertypes() call is guaranteed to set up a particular partial order (class types first, interface types
        // second).

        sd = Math.max(sd, specializationDepth(DelegatingTypeMirror.of(s, elementSource, SAME_TYPE_EQUALITY), elementSource)); // RECURSIVE
      }
      return sd + 1;
    case ERROR:
      throw new AssertionError("javac bug; t is reported as having an ERROR kind: " + t);
    default:
      throw new IllegalArgumentException("t: " + t + "; t.getKind(): " + t.getKind());
    }
  }

  private static final boolean validateType(final TypeMirror t) {
    return switch (t.getKind()) {
    case ARRAY, DECLARED, TYPEVAR -> true;
    default -> throw new IllegalArgumentException("t is not a reference type: " + t);
    };
  }

  private static final boolean seen(final Iterable<? extends TypeMirror> seen, final TypeMirror t) {
    for (final TypeMirror s : seen) {
      if (s == t || Lang.sameType(s, t)) {
        return true;
      }
    }
    return false;
  }

  private static final boolean isInterface(final TypeMirror t) {
    return isInterface(element(t));
  }

  private static final boolean isInterface(final Element e) {
    return e != null && e.getKind().isInterface();
  }

  private static final Element element(final TypeMirror t) {
    if (t == null) {
      return null;
    }
    return switch (t.getKind()) {
      case DECLARED -> ((DeclaredType)t).asElement();
      case TYPEVAR -> ((TypeVariable)t).asElement();
      default -> null;
    };
  }


  /*
   * Inner and nested classes.
   */


  // *NOT* consistent with equals().
  private static final class TypeMirrorComparator implements Comparator<DelegatingTypeMirror> {

    private final ElementSource elementSource;

    private TypeMirrorComparator(final ElementSource elementSource) {
      super();
      this.elementSource = elementSource == null ? Lang.elementSource() : elementSource;
    }

    @Override // Comparator<TypeMirror>
    public final int compare(final DelegatingTypeMirror t, final DelegatingTypeMirror s) {
      return
        t == null ? s == null ? 0 : 1 :
        s == null ? -1 :
        Integer.signum(specializationDepth(s, this.elementSource) - specializationDepth(t, this.elementSource));
    }

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
