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
import javax.lang.model.element.QualifiedNameable;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import org.microbean.constant.Constables;

import org.microbean.lang.Equality;
import org.microbean.lang.Lang;

import org.microbean.lang.type.DelegatingTypeMirror;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_List;

import static org.microbean.bean.ConstantDescs.CD_ReferenceTypeList;

public final class ReferenceTypeList implements Constable {

  private static final Equality SAME_TYPE_EQUALITY = new SameTypeEquality();

  private final List<TypeMirror> types;

  private final int interfaceIndex;

  public ReferenceTypeList(final Collection<? extends TypeMirror> types) {
    this(types, ReferenceTypeList::seen, ReferenceTypeList::validateType);
  }

  // CAUTION: not validated on purpose; for use by describeConstable() etc.
  private ReferenceTypeList(final List<TypeMirror> types, final int interfaceIndex) {
    super();
    this.types = types.isEmpty() ? List.of() : List.copyOf(types);
    this.interfaceIndex = interfaceIndex;
  }

  public ReferenceTypeList(final Collection<? extends TypeMirror> types,
                           BiPredicate<Iterable<? extends TypeMirror>, TypeMirror> seen,
                           Predicate<? super TypeMirror> typeFilter) {
    super();
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
      final List<TypeMirror> newTypes = new ArrayList<>(types.size());
      for (TypeMirror t : types) {
        t = DelegatingTypeMirror.of(t, ReferenceTypeList::elementSource, SAME_TYPE_EQUALITY);
        if (!seen.test(newTypes, t) && typeFilter.test(t)) {
          newTypes.add(t);
        }
      }
      Collections.sort(newTypes, TypeMirrorComparator.INSTANCE);
      int interfaceIndex = -1;
      for (int i = 0; i < newTypes.size(); i++) {
        final TypeMirror t = newTypes.get(i);
        final Element e = element(t);
        if (e != null && e.getKind().isInterface()) {
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
      .map(typesDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                               MethodHandleDesc.ofConstructor(CD_ReferenceTypeList,
                                                                              CD_List,
                                                                              CD_int),
                                               typesDesc,
                                               this.interfaceIndex));
  }

  public final List<TypeMirror> types() {
    return this.types;
  }

  public final List<TypeMirror> classTypes() {
    return this.interfaceIndex < 0 ? this.types : this.types.subList(0, this.interfaceIndex);
  }

  public final List<TypeMirror> interfaceTypes() {
    return this.interfaceIndex < 0 ? List.of() : this.types.subList(this.interfaceIndex, this.types.size());
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
    return closure(t, ReferenceTypeList::seen, ReferenceTypeList::validateType);
  }

  public static final ReferenceTypeList closure(final TypeMirror t,
                                                BiPredicate<Iterable<? extends TypeMirror>, TypeMirror> seen,
                                                Predicate<? super TypeMirror> typeFilter) {
    final List<TypeMirror> list = new ArrayList<>();
    list.add(t instanceof DelegatingTypeMirror dt ? dt.delegate() : t);
    list.addAll(Lang.directSupertypes(t));
    return new ReferenceTypeList(list, seen, typeFilter);
  }

  @SuppressWarnings("fallthrough")
  public static final int specializationDepth(final TypeMirror t) {
    // See
    // https://github.com/openjdk/jdk/blob/2e340e855b760e381793107f2a4d74095bd40199/src/jdk.compiler/share/classes/com/sun/tools/javac/code/Types.java#L3570-L3615.
    switch (t.getKind()) {
    case DECLARED:
      if (((QualifiedNameable)((DeclaredType)t).asElement()).getQualifiedName().contentEquals("java.lang.Object")) {
        return 0;
      }
      // fall through
    // case INTERSECTION:
    case ARRAY:
    case TYPEVAR:
      int sd = 0;
      for (final TypeMirror s : Lang.directSupertypes(DelegatingTypeMirror.unwrap(t))) {
        // The directSupertypes() call is guaranteed to set up a particular partial order (class types first, interface types
        // second).
        sd = Math.max(sd, specializationDepth(s)); // RECURSIVE
      }
      return sd + 1;
    default:
      throw new IllegalArgumentException("t: " + t);
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
      if (s == t || Lang.sameType(DelegatingTypeMirror.unwrap(s), DelegatingTypeMirror.unwrap(t))) {
        return true;
      }
    }
    return false;
  }

  private static final Element element(final TypeMirror t) {
    return switch (t.getKind()) {
      case DECLARED -> ((DeclaredType)t).asElement();
      case TYPEVAR -> ((TypeVariable)t).asElement();
      default -> null;
    };
  }

  private static final Element elementSource(final String moduleName, final String typeName) {
    return Lang.typeElement(Lang.moduleElement(moduleName), typeName);
  }


  /*
   * Inner and nested classes.
   */


  // *NOT* consistent with equals().
  private static final class TypeMirrorComparator implements Comparator<TypeMirror> {

    private static final TypeMirrorComparator INSTANCE = new TypeMirrorComparator();

    private TypeMirrorComparator() {
      super();
    }

    @Override // Comparator<TypeMirror>
    public final int compare(final TypeMirror t, final TypeMirror s) {
      return t == null ? s == null ? 0 : 1 : s == null ? -1 : Integer.signum(specializationDepth(s) - specializationDepth(t));
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
