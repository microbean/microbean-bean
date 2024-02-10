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

import java.lang.constant.ClassDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;

import java.lang.System.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import java.util.function.Predicate;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.microbean.constant.Constables;

import org.microbean.lang.TypeAndElementSource;
import org.microbean.lang.Equality;
import org.microbean.lang.Lang;

import org.microbean.lang.type.DelegatingTypeMirror;

import org.microbean.lang.visitor.Visitors;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_List;

import static java.lang.System.Logger.Level.WARNING;

import static org.microbean.lang.Lang.typeAndElementSource;
import static org.microbean.lang.Lang.sameTypeEquality;

public final class BeanTypeList extends ReferenceTypeList {

  private static final Logger LOGGER = System.getLogger(BeanTypeList.class.getName());

  public BeanTypeList(final TypeMirror type) {
    this(List.of(type), null, typeAndElementSource(), sameTypeEquality());
  }

  public BeanTypeList(final Collection<? extends TypeMirror> types) {
    this(types, null, typeAndElementSource(), sameTypeEquality());
  }

  public BeanTypeList(final Collection<? extends TypeMirror> types,
                      final TypeAndElementSource typeAndElementSource,
                      final Equality equality) {
    this(types, null, typeAndElementSource, equality);
  }

  public BeanTypeList(final Collection<? extends TypeMirror> types,
                      final Predicate<? super TypeMirror> typeFilter, // the type supplied will be a DeclaredType and will be ARRAY, DECLARED or TYPEVAR
                      final TypeAndElementSource typeAndElementSource,
                      final Equality equality) {
    super(types, typeFilter(typeFilter), typeAndElementSource, equality);
  }

  // Deliberately unvalidated constructor for use by describeConstable() only.
  @Deprecated
  BeanTypeList(final List<DelegatingTypeMirror> types,
               final int classesIndex,
               final int arraysIndex,
               final int interfacesIndex,
               final Equality equality) {
    super(types, classesIndex, arraysIndex, interfacesIndex, equality);
  }

  @Override // Constable
  public final Optional<DynamicConstantDesc<?>> describeConstable() {
    return Constables.describeConstable(this.types(), Lang::describeConstable)
      .flatMap(typesDesc -> this.equality.describeConstable()
               .map(equalityDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                           MethodHandleDesc.ofConstructor(ClassDesc.of(this.getClass().getName()),
                                                                                          CD_List,
                                                                                          CD_int,
                                                                                          CD_int,
                                                                                          CD_int,
                                                                                          CD_Equality),
                                                           typesDesc,
                                                           this.classesIndex,
                                                           this.arraysIndex,
                                                           this.interfacesIndex,
                                                           equalityDesc)));
  }


  /*
   * Static methods.
   */


  public static final BeanTypeList closure(final TypeMirror t) {
    return closure(t, BeanTypeList::legalBeanType, new Visitors(typeAndElementSource()));
  }

  public static final BeanTypeList closure(final TypeMirror t, final TypeAndElementSource tes) {
    return closure(t, BeanTypeList::legalBeanType, new Visitors(tes));
  }

  public static final BeanTypeList closure(final TypeMirror t, final Visitors visitors) {
    return closure(t, BeanTypeList::legalBeanType, visitors);
  }

  public static final BeanTypeList closure(final TypeMirror t,
                                           final Predicate<? super TypeMirror> typeFilter,
                                           final TypeAndElementSource tes) {
    return closure(t, typeFilter, new Visitors(tes));
  }

  /**
   * Returns a non-{@code null} {@link BeanTypeList} containing the set of types {@code t} bears.
   *
   * @param t the {@link TypeMirror} whose type closure will be returned as a {@link BeanTypeList}; must not be {@code
   * null}; must be either a {@linkplain TypeKind#DECLARED declared type}, an {@linkplain TypeKind#INTERSECTION
   * intersection type} or a {@linkplain TypeKind#TYPEVAR type variable}
   *
   * @param typeFilter a {@link Predicate} used to {@linkplain Predicate#test(Object) test} a given {@link TypeMirror};
   * if its {@link Predicate#test(Object) test(Object)} method returns {@code true} for a given {@link TypeMirror}, the
   * {@link TypeMirror} will be included in the returned {@link BeanTypeList}; may be {@code null}
   *
   * @param visitors a {@link Visitors} providing access to a {@linkplain Visitors#typeClosureVisitor() type closure
   * visitor}; must not be {@code null}
   *
   * @return a non-{@code null} {@link BeanTypeList}
   *
   * @exception NullPointerException if {@code t} or {@code visitors} is {@code null}
   *
   * @exception IllegalArgumentException if {@code t} is neither a {@linkplain TypeKind#DECLARED declared type}, an
   * {@linkplain TypeKind#INTERSECTION intersection type} nor a {@linkplain TypeKind#TYPEVAR type variable}
   *
   * @see Visitors#typeClosureVisitor()
   */
  public static final BeanTypeList closure(final TypeMirror t,
                                           final Predicate<? super TypeMirror> typeFilter,
                                           final Visitors visitors) {
    return new BeanTypeList(visitors.typeClosureVisitor().visit(t).toList(),
                            typeFilter,
                            visitors.typeAndElementSource(),
                            sameTypeEquality());
  }

  private static final Predicate<? super TypeMirror> typeFilter(final Predicate<? super TypeMirror> typeFilter) {
    return
      typeFilter == null ? BeanTypeList::legalBeanType : ((Predicate<TypeMirror>)BeanTypeList::legalBeanType).and(typeFilter);
  }

  /**
   * Returns {@code true} if and only if {@code t} is non-{@code null} and a <a
   * href="https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0.html#legal_bean_types">legal bean type</a>.
   *
   * @param t the {@link TypeMirror} in question; must not be {@code null}
   *
   * @return {@code true} if and only if {@code t} is non-{@code null} and a <a
   * href="https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0.html#legal_bean_types">legal bean type</a>;
   * {@code false} otherwise
   *
   * @exception NullPointerException if {@code t} is {@code null}
   */
  public static final boolean legalBeanType(final TypeMirror t) {
    // https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0.html#legal_bean_types
    // https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0.html#assignable_parameters
    switch (t.getKind()) {

    // "A bean type may be an array type."
    //
    // "However, some Java types are not legal bean types: [...] An array type whose component type is not a legal bean
    // type"
    case ARRAY:
      if (!legalBeanType(((ArrayType)t).getComponentType())) { // note recursion
        if (LOGGER.isLoggable(WARNING)) {
          LOGGER.log(WARNING, t + " has a component type that is an illegal bean type (" + ((ArrayType)t).getComponentType() + ")");
        }
        return false;
      }
      return true;

    // "A bean type may be a primitive type. Primitive types are considered to be identical to their corresponding
    // wrapper types in java.lang."
    //
    // When used as a type filter in this class, you'll want to box first.
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
      return true;

    // "However, some Java types are not legal bean types: [...] A parameterized type that contains [see below] a
    // wildcard type parameter [type argument] is not a legal bean type."
    //
    // Some ink has been spilled on what it means to "contain" a "wildcard type parameter [type argument]"
    // (https://issues.redhat.com/browse/CDI-502). Because it turns out that "actual type" apparently means, among other
    // things, a non-wildcard type, it follows that *no* type argument appearing anywhere in a bean type is
    // permitted. Note that the definition of "actual type" does not appear in the CDI specification, but only in a
    // closed JIRA issue
    // (https://issues.redhat.com/browse/CDI-502?focusedId=13036118&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-13036118).
    //
    // This still seems way overstrict to me but there you have it.
    case DECLARED:
      for (final TypeMirror typeArgument : ((DeclaredType)t).getTypeArguments()) {
        if (typeArgument.getKind() != TypeKind.TYPEVAR && !legalBeanType(typeArgument)) { // note recursion
          if (LOGGER.isLoggable(WARNING)) {
            LOGGER.log(WARNING, t + " is parameterized with an illegal bean type (" + typeArgument + ")");
          }
          return false;
        }
      }
      return true;

    // "A type variable is not a legal bean type."
    case TYPEVAR:
      if (LOGGER.isLoggable(WARNING)) {
        LOGGER.log(WARNING, t + " is a type variable and hence an illegal bean type");
      }
      return false;

    default:
      if (LOGGER.isLoggable(WARNING)) {
        LOGGER.log(WARNING, t + " is an illegal bean type");
      }
      return false;
    }
  }

}
