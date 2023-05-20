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

import org.microbean.lang.Equality;
import org.microbean.lang.Lang;

import org.microbean.lang.type.DelegatingTypeMirror;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_Collection;

import static org.microbean.bean.ConstantDescs.CD_ReferenceTypeList;

public final class ReferenceTypeList implements Constable {

  private static final Equality SAME_TYPE_EQUALITY = new SameTypeEquality();

  private final List<TypeMirror> types; // all DelegatingTypeMirrors

  private final int interfaceIndex;

  public ReferenceTypeList(final Collection<? extends TypeMirror> types) {
    this(types, ReferenceTypeList::seen, ReferenceTypeList::validateType);
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
      final List<DelegatingTypeMirror> newTypes = new ArrayList<>(types.size());
      for (final TypeMirror t : types) {
        final DelegatingTypeMirror dt = DelegatingTypeMirror.of(t, Lang.elementSource(), SAME_TYPE_EQUALITY);
        if (!seen.test(newTypes, dt) && typeFilter.test(dt)) {
          newTypes.add(dt);
        }
      }
      Collections.sort(newTypes, TypeMirrorComparator.INSTANCE);
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
      .map(typesDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                               MethodHandleDesc.ofConstructor(CD_ReferenceTypeList,
                                                                              CD_Collection),
                                               typesDesc));
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


  private static final ElementKind kind(final Element e) {
    return Lang.kind(e);
  }

  private static final TypeKind kind(final TypeMirror t) {
    // XXX TODO FIXME: OOF. This is a hack indicating a much larger problem. TypeMirrors (Types) in the compiler
    // representing declared types cause their Symbols to "complete" lazily when you call certain methods, getKind()
    // among them. Completion can cause ClassFinder and ClassReader to get involved. ClassFinder can "fill in" only
    // one thing at a time. Once a Symbol has been completed, its completion mechanism is set to a
    // no-op. Synchronizing on the actual TypeMirror here allows completion to continue serially.  We uncover this
    // case only because we have JUnit tests running in parallel.
    //
    // Completion as remarked fundamentally happens on Symbols (Elements). It can be triggered by an invocation of any
    // of the following Element or Element-subclass methods:
    //
    // getModifiers()
    // getSuperclass()
    // getKind()
    // getAnnotationMirrors()
    // getEnclosedElements()
    // getDirectives() // modules
    // getInterfaces()
    // getNestingKind()
    // getParameters()
    //
    // On TypeMirrors:
    //
    // getKind()
    // getTypeArguments()
    //
    // The larger problem is that types and symbols related to a given type may not yet be completed and this same
    // synchronization hack may need to be applied to them as well.
    //
    // For now we trigger completion inside Lang itself.
    //
    // Sample stack trace:
    /*
      java.lang.AssertionError: Filling jrt:/java.base/java/io/Serializable.class during DirectoryFileObject[/modules/java.base:java/lang/CharSequence.class]
      at jdk.compiler/com.sun.tools.javac.util.Assert.error(Assert.java:162)
      at jdk.compiler/com.sun.tools.javac.code.ClassFinder.fillIn(ClassFinder.java:365)
      at jdk.compiler/com.sun.tools.javac.code.ClassFinder.complete(ClassFinder.java:301)
      at jdk.compiler/com.sun.tools.javac.code.Symtab$1.complete(Symtab.java:326)
      at jdk.compiler/com.sun.tools.javac.code.Symbol.complete(Symbol.java:682)
      at jdk.compiler/com.sun.tools.javac.code.Symbol$ClassSymbol.complete(Symbol.java:1410)
      at jdk.compiler/com.sun.tools.javac.code.Symbol.apiComplete(Symbol.java:688)
      at jdk.compiler/com.sun.tools.javac.code.Type$ClassType.getKind(Type.java:1181)
      at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.ReferenceTypeList.specializationDepth(ReferenceTypeList.java:168)
      at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.ReferenceTypeList.specializationDepth(ReferenceTypeList.java:181)
    */
    return Lang.kind(t);
  }

  public static final ReferenceTypeList closure(final TypeMirror t) {
    return closure(t, ReferenceTypeList::seen, ReferenceTypeList::validateType);
  }

  public static final ReferenceTypeList closure(final TypeMirror t,
                                                BiPredicate<Iterable<? extends TypeMirror>, TypeMirror> seen,
                                                Predicate<? super TypeMirror> typeFilter) {
    final List<TypeMirror> list = new ArrayList<>();
    list.add(t);
    list.addAll(Lang.directSupertypes(t));
    return new ReferenceTypeList(list, seen, typeFilter);
  }

  public static final int specializationDepth(final TypeMirror t) {
    return specializationDepth(DelegatingTypeMirror.of(t, Lang.elementSource(), SAME_TYPE_EQUALITY));
  }

  @SuppressWarnings("fallthrough")
  private static final int specializationDepth(final DelegatingTypeMirror t) {

    final TypeKind k = kind(t);

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

        sd = Math.max(sd, specializationDepth(DelegatingTypeMirror.of(s, Lang.elementSource(), SAME_TYPE_EQUALITY))); // RECURSIVE
      }
      return sd + 1;
    case ERROR:
      throw new AssertionError("javac bug; t is reported as having an ERROR kind: " + t);
    default:
      throw new IllegalArgumentException("t: " + t + "; t.getKind(): " + kind(t));
    }
  }

  private static final boolean validateType(final TypeMirror t) {
    return switch (kind(t)) {
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
    return e != null && kind(e).isInterface();
  }

  private static final Element element(final TypeMirror t) {
    if (t == null) {
      return null;
    }
    return switch (kind(t)) {
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

    private static final TypeMirrorComparator INSTANCE = new TypeMirrorComparator();

    private TypeMirrorComparator() {
      super();
    }

    @Override // Comparator<TypeMirror>
    public final int compare(final DelegatingTypeMirror t, final DelegatingTypeMirror s) {
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
