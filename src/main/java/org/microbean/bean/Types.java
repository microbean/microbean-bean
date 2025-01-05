/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2024–2025 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.microbean.bean;

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import java.util.function.Predicate;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Parameterizable;
import javax.lang.model.element.QualifiedNameable;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import org.microbean.construct.Domain;

import org.microbean.construct.element.UniversalElement;

import org.microbean.construct.type.UniversalType;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;

import static java.util.HashSet.newHashSet;

import static java.util.stream.Stream.concat;

public class Types implements Constable {


  /*
   * Static fields.
   */


  private static final ClassDesc CD_Domain = ClassDesc.of("org.microbean.construct.Domain");


  /*
   * Instance fields.
   */


  private final Comparator<UniversalType> c;

  private final Domain domain;


  /*
   * Constructors.
   */


  public Types(final Domain domain) {
    super();
    this.domain = Objects.requireNonNull(domain, "domain");
    this.c = new SpecializationComparator();
  }


  /*
   * Instance methods.
   */


  /**
   * Returns an {@link Optional} housing a {@link ConstantDesc} that represents this {@link Types}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>The default implementation of this method relies on the presence of a {@code public} constructor that accepts a
   * single {@link Domain}-typed argument.</p>
   *
   * <p>The {@link Optional} returned by an invocation of this method may be, and often will be, {@linkplain
   * Optional#isEmpty() empty}.</p>
   *
   * @return an {@link Optional} housing a {@link ConstantDesc} that represents this {@link Types}; never {@code null}
   *
   * @see Constable#describeConstable()
   */
  @Override // Constable
  public Optional<? extends ConstantDesc> describeConstable() {
    return (this.domain instanceof Constable c ? c.describeConstable() : Optional.<ConstantDesc>empty())
      .map(domainDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                MethodHandleDesc.ofConstructor(ClassDesc.of(this.getClass().getName()),
                                                                               CD_Domain),
                                                domainDesc));
  }

  public final Domain domain() {
    return this.domain;
  }

  @Override // Object
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other != null && other.getClass() == this.getClass()) {
      return this.domain().equals(((Types)other).domain());
    } else {
      return false;
    }
  }

  @Override // Object
  public int hashCode() {
    return this.domain().hashCode();
  }

  private final boolean isInterface(final UniversalElement e) {
    return e.getKind().isInterface();
  }
  
  private final boolean isInterface(final UniversalType t) {
    return t.getKind() == TypeKind.DECLARED && isInterface(t.asElement());
  }

  final String name(final TypeMirror t) {
    final UniversalType ut = UniversalType.of(t, this.domain());
    return switch (ut.getKind()) {
    case ARRAY -> name(ut.getComponentType()) + "[]";
    case BOOLEAN -> "boolean";
    case BYTE -> "byte";
    case CHAR -> "char";
    case DECLARED, TYPEVAR -> name(ut.asElement());
    case DOUBLE -> "double";
    case FLOAT -> "float";
    case INT -> "int";
    case INTERSECTION -> {
      final StringJoiner sj = new java.util.StringJoiner("&");
      for (final UniversalType bound : ut.getBounds()) {
        sj.add(name(bound));
      }
      yield sj.toString();
    }
    case LONG -> "long";
    case SHORT -> "short";
    default -> ut.toString();
    };
  }

  public final List<? extends TypeMirror> supertypes(final TypeMirror t) {
    return this.supertypes(t, Types::returnTrue);
  }

  public final List<? extends TypeMirror> supertypes(final TypeMirror t, final Predicate<? super TypeMirror> p) {
    final ArrayList<UniversalType> nonInterfaceTypes = new ArrayList<>(7); // arbitrary size
    final ArrayList<UniversalType> interfaceTypes = new ArrayList<>(17); // arbitrary size
    supertypes(UniversalType.of(t, this.domain()), p, nonInterfaceTypes, interfaceTypes, newHashSet(13)); // arbitrary size
    nonInterfaceTypes.trimToSize();
    interfaceTypes.trimToSize();
    return
      concat(nonInterfaceTypes.stream(), // non-interface supertypes are already sorted from most-specific to least
             interfaceTypes.stream().sorted(this.c)) // have to sort interfaces because you can extend them in any order
      .toList();
  }

  private final void supertypes(final UniversalType t,
                                final Predicate<? super TypeMirror> p,
                                final ArrayList<? super UniversalType> nonInterfaceTypes,
                                final ArrayList<? super UniversalType> interfaceTypes,
                                final Set<? super String> seen) {
    if (seen.add(name(t))) {
      if (p.test(t)) {
        if (isInterface(t)) {
          interfaceTypes.add(t); // reflexive
        } else {
          nonInterfaceTypes.add(t); // reflexive
        }
      }
      for (final TypeMirror directSupertype : domain.directSupertypes(t)) {
        this.supertypes(UniversalType.of(directSupertype, this.domain()), p, nonInterfaceTypes, interfaceTypes, seen);
      }
    }
  }

  /*
   * Static methods.
   */


  private static final String name(final Element e) {
    return e instanceof QualifiedNameable qn ? name(qn) : name(e.getSimpleName());
  }

  private static final String name(final QualifiedNameable qn) {
    final CharSequence n = qn.getQualifiedName();
    return n == null || n.isEmpty() ? name(qn.getSimpleName()) : name(n);
  }

  private static final String name(final CharSequence cs) {
    return cs instanceof String s ? s : cs.toString();
  }

  private static final <T> boolean returnTrue(final T ignored) {
    return true;
  }


  /*
   * Inner and nested classes.
   */


  private final class SpecializationComparator implements Comparator<UniversalType> {

    private SpecializationComparator() {
      super();
    }

    @Override
    public final int compare(final UniversalType t, final UniversalType s) {
      if (t == s) {
        return 0;
      } else if (t == null) {
        return 1; // nulls right
      } else if (s == null) {
        return -1; // nulls right
      } else if (domain.sameType(t, s)) {
        return 0;
      } else if (domain.subtype(t, s)) {
        // t is a subtype of s; s is a proper supertype of t
        return -1;
      } else if (domain.subtype(s, t)) {
        // s is a subtype of t; t is a proper supertype of s
        return 1;
      } else {
        return name(t).compareTo(name(s));
      }
    }

  }

}
