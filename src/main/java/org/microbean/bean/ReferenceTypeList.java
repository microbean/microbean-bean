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
import org.microbean.lang.NameTypeMirrorComparator;
import org.microbean.lang.SpecializationDepthTypeMirrorComparator;
import org.microbean.lang.TestingTypeMirrorComparator;

import org.microbean.lang.type.DelegatingTypeMirror;

import org.microbean.lang.visitor.Visitors;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_List;

import static org.microbean.bean.ConstantDescs.CD_ReferenceTypeList;

import static org.microbean.lang.Lang.sameTypeEquality;

public final class ReferenceTypeList implements Constable {

  private static final ClassDesc CD_ElementSource = ClassDesc.of(ElementSource.class.getName());

  private static final ClassDesc CD_Equality = ClassDesc.of(Equality.class.getName());

  private final Equality equality;

  private final List<DelegatingTypeMirror> types;

  // We shouldn't have to keep this around, but we need to for describeConstable().
  private final ElementSource elementSource;

  private final int classesIndex;

  private final int arraysIndex;

  private final int interfacesIndex;

  public ReferenceTypeList(final TypeMirror type) {
    this(List.of(type), ReferenceTypeList::validateType, Lang.elementSource(), Lang.sameTypeEquality());
  }

  public ReferenceTypeList(final Collection<? extends TypeMirror> types) {
    this(types, ReferenceTypeList::validateType, Lang.elementSource(), Lang.sameTypeEquality());
  }

  public ReferenceTypeList(final Collection<? extends TypeMirror> types, final ElementSource elementSource) {
    this(types, ReferenceTypeList::validateType, elementSource, Lang.sameTypeEquality());
  }

  public ReferenceTypeList(final Collection<? extends TypeMirror> types, final Predicate<? super TypeMirror> typeFilter, final ElementSource elementSource) {
    this(types, typeFilter, elementSource, Lang.sameTypeEquality());
  }

  public ReferenceTypeList(final Collection<? extends TypeMirror> types,
                           Predicate<? super TypeMirror> typeFilter, // the type supplied will be a DeclaredType and will be ARRAY, DECLARED or TYPEVAR
                           final ElementSource elementSource,
                           final Equality equality) {
    super();
    this.elementSource = elementSource == null ? Lang.elementSource() : elementSource;
    this.equality = equality == null ? Lang.sameTypeEquality() : equality;
    if (types.isEmpty()) {
      this.types = List.of();
      this.classesIndex = -1;
      this.arraysIndex = -1;
      this.interfacesIndex = -1;
    } else {
      if (typeFilter == null) {
        typeFilter = ReferenceTypeList::validateType;
      } else {
        typeFilter = ((Predicate<TypeMirror>)ReferenceTypeList::validateType).and(typeFilter);
      }
      final List<DelegatingTypeMirror> newTypes = new ArrayList<>(types.size());
      for (final TypeMirror t : types) {
        final DelegatingTypeMirror dt = DelegatingTypeMirror.of(t, this.elementSource, this.equality);
        if (!this.seen(newTypes, dt) && typeFilter.test(dt)) {
          newTypes.add(dt);
        }
      }
      if (newTypes.isEmpty()) {
        this.types = List.of();
        this.classesIndex = -1;
        this.arraysIndex = -1;
        this.interfacesIndex = -1;
      } else {
        Collections.sort(newTypes,
                         // Type variables first...
                         new TestingTypeMirrorComparator(t -> t.getKind() == TypeKind.TYPEVAR)
                         // ...then non-interface classes...
                         .thenComparing(new TestingTypeMirrorComparator(t -> t.getKind() == TypeKind.DECLARED && !((DeclaredType)t).asElement().getKind().isInterface()))
                         // ...then array types...
                         .thenComparing(new TestingTypeMirrorComparator(t -> t.getKind() == TypeKind.ARRAY))
                         // ...then interfaces...
                         .thenComparing(new TestingTypeMirrorComparator(t -> t.getKind() == TypeKind.DECLARED && ((DeclaredType)t).asElement().getKind().isInterface()))
                         // ...order by specialization depth within those categories to the extent possible...
                         .thenComparing(new SpecializationDepthTypeMirrorComparator(this.elementSource, this.equality))
                         // ...and break any ties with somewhat artificial but deterministic naming semantics.
                         .thenComparing(NameTypeMirrorComparator.INSTANCE));
        int classesIndex = -1;
        int arraysIndex = -1;
        int interfacesIndex = -1;
        LOOP:
        for (int i = 0; i < newTypes.size(); i++) {
          final TypeMirror newType = newTypes.get(i);
          switch (newType.getKind()) {
          case ARRAY:
            if (arraysIndex < 0) {
              arraysIndex = i;
            }
            break;
          case DECLARED:
            if (((DeclaredType)newType).asElement().getKind().isInterface()) {
              interfacesIndex = i;
              break LOOP;
            } else if (classesIndex < 0) {
              classesIndex = i;
            }
            break;
          case TYPEVAR:
            break;
          default:
            throw new AssertionError("non-reference type: " + newType);
          }
        }
        this.types = Collections.unmodifiableList(newTypes);
        this.classesIndex = classesIndex;
        this.arraysIndex = arraysIndex;
        this.interfacesIndex = interfacesIndex;
      }
    }
  }

  // Deliberately unvalidated private constructor for use by describeConstable() only.
  private ReferenceTypeList(final List<DelegatingTypeMirror> types,
                            final int classesIndex,
                            final int arraysIndex,
                            final int interfacesIndex,
                            final ElementSource elementSource,
                            final Equality equality) {
    super();
    this.types = types;
    this.classesIndex = classesIndex;
    this.arraysIndex = arraysIndex;
    this.interfacesIndex = interfacesIndex;
    this.elementSource = elementSource;
    this.equality = equality;
  }

  @Override // Constable
  public final Optional<DynamicConstantDesc<ReferenceTypeList>> describeConstable() {
    return Constables.describeConstable(this.types(), Lang::describeConstable)
      .flatMap(typesDesc -> Constables.describeConstable(this.elementSource)
               .flatMap(elementSourceDesc -> this.equality.describeConstable()
                        .map(equalityDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                                    MethodHandleDesc.ofConstructor(CD_ReferenceTypeList,
                                                                                                   CD_List,
                                                                                                   CD_int,
                                                                                                   CD_int,
                                                                                                   CD_int,
                                                                                                   CD_ElementSource,
                                                                                                   CD_Equality),
                                                                    typesDesc,
                                                                    this.classesIndex,
                                                                    this.arraysIndex,
                                                                    this.interfacesIndex,
                                                                    elementSourceDesc,
                                                                    equalityDesc))));
  }

  public final List<? extends TypeMirror> types() {
    return this.types;
  }

  public final List<? extends TypeMirror> classes() {
    if (this.classesIndex < 0) {
      return List.of();
    } else if (this.arraysIndex < 0) {
      if (this.classesIndex == 0) {
        return this.interfacesIndex < 0 ? this.types : this.types.subList(0, this.interfacesIndex);
      } else {
        return this.types.subList(this.classesIndex, this.interfacesIndex < 0 ? this.types.size() : this.interfacesIndex);
      }
    }
    return this.types.subList(this.classesIndex, this.arraysIndex);
  }

  public final List<? extends TypeMirror> arrays() {
    if (this.arraysIndex < 0) {
      return List.of();
    } else if (this.arraysIndex == 0) {
      return this.interfacesIndex < 0 ? this.types : this.types.subList(0, this.interfacesIndex);
    }
    return this.types.subList(this.arraysIndex, this.interfacesIndex < 0 ? this.types.size() : this.interfacesIndex);
  }

  public final List<? extends TypeMirror> interfaces() {
    if (this.interfacesIndex < 0) {
      return List.of();
    } else if (this.interfacesIndex == 0) {
      return this.types;
    }
    return this.types.subList(this.interfacesIndex, this.types.size());
  }

  public final List<? extends TypeMirror> typeVariables() {
    return this.classesIndex <= 0 ? List.of() : this.types.subList(0, this.classesIndex);
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
    return closure(t, ReferenceTypeList::validateType, new Visitors(Lang.elementSource()));
  }

  public static final ReferenceTypeList closure(final TypeMirror t, final Visitors visitors) {
    return closure(t, ReferenceTypeList::validateType, visitors);
  }

  public static final ReferenceTypeList closure(final TypeMirror t,
                                                Predicate<? super TypeMirror> typeFilter,
                                                final ElementSource elementSource) {
    return closure(t, typeFilter, new Visitors(elementSource));
  }

  public static final ReferenceTypeList closure(final TypeMirror t,
                                                Predicate<? super TypeMirror> typeFilter,
                                                final Visitors visitors) {
    return new ReferenceTypeList(visitors.typeClosureVisitor().visit(t).toList(),
                                 typeFilter,
                                 visitors.elementSource());
  }

  private static final boolean validateType(final TypeMirror t) {
    return switch (t.getKind()) {
    case ARRAY, DECLARED, TYPEVAR -> true;
    default -> throw new IllegalArgumentException("t is not a reference type: " + t);
    };
  }

  private final boolean seen(final Iterable<? extends TypeMirror> seen, final TypeMirror t) {
    for (final TypeMirror s : seen) {
      if (s == t || this.equality.equals(s, t)) {
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

}
