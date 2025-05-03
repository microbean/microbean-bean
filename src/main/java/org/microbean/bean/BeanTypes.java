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

import java.lang.System.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.util.concurrent.ConcurrentHashMap;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.QualifiedNameable;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.microbean.assign.Types;

import org.microbean.construct.Domain;

import static java.lang.System.Logger.Level.WARNING;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.SEALED;
import static javax.lang.model.element.Modifier.STATIC;

import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.lang.model.type.TypeKind.TYPEVAR;

/**
 * A utility for working with <dfn>bean types</dfn>.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #beanTypes(TypeMirror)
 *
 * @see #legalBeanType(TypeMirror)
 */
public final class BeanTypes extends Types {


  /*
   * Static fields.
   */


  private static final Logger LOGGER = System.getLogger(BeanTypes.class.getName());


  /*
   * Instance fields.
   */


  // TODO: is this cache actually worth it?
  private final Map<TypeMirror, BeanTypeList> beanTypesCache;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link BeanTypes}.
   *
   * @param domain a {@link Domain}; must not be {@code null}
   *
   * @exception NullPointerException if {@code domain} is {@code null}
   */
  public BeanTypes(final Domain domain) {
    super(domain);
    this.beanTypesCache = new ConcurrentHashMap<>();
  }


  /*
   * Instance methods.
   */


  /**
   * Returns a {@link BeanTypeList} of {@linkplain #legalBeanType(TypeMirror) legal bean types} that the supplied {@link
   * TypeMirror} bears.
   *
   * <p>The returned {@link BeanTypeList} may be empty.</p>
   *
   * @param t a {@link TypeMirror}; must not be {@code null}
   *
   * @return a {@link BeanTypeList} of {@linkplain #legalBeanType(TypeMirror) legal bean types} that the supplied {@link
   * TypeMirror} bears; never {@code null}
   *
   * @exception NullPointerException if {@code t} is {@code null}
   *
   * @microbean.nullability This method never returns {@code null}.
   *
   * @microbean.idempotency This method is idempotent and returns determinate values.
   *
   * @microbean.threadsafety This method is safe for concurrent use by multiple threads.
   *
   * @see #supertypes(TypeMirror, java.util.function.Predicate)
   */
  public final BeanTypeList beanTypes(final TypeMirror t) {
    // https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#assignable_parameters
    // https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#legal_bean_types
    // https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#managed_bean_types
    // https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#producer_field_types
    // https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#producer_method_types
    final Domain d = this.domain();
    return switch (t.getKind()) {
    case ARRAY ->
      this.beanTypesCache.computeIfAbsent(t, t0 -> BeanTypeList.of(d,
                                                                   (legalBeanType(t0) ?
                                                                    List.of(t0, d.javaLangObject().asType()) :
                                                                    List.of())));
    case BOOLEAN, BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT ->
      this.beanTypesCache.computeIfAbsent(t, t0 -> BeanTypeList.of(d,
                                                                   List.of(t0, d.javaLangObject().asType())));
    case DECLARED, TYPEVAR ->
      this.beanTypesCache.computeIfAbsent(t, t0 -> BeanTypeList.of(d,
                                                                   this.supertypes(t0, BeanTypes::legalBeanType)));
    default ->
      BeanTypeList.of(d, List.of());
    };
  }

  /**
   * Clears caches that may be used internally by this {@link BeanTypes}.
   *
   * @microbean.idempotency This method may clear internal state but otherwise has no side effects.
   *
   * @microbean.threadsafety This method is safe for concurrent use by multiple threads.
   */
  public final void clearCaches() {
    this.beanTypesCache.clear();
  }


  /*
   * Static methods.
   */


  /**
   * Returns {@code true} if and only if the supplied {@link TypeMirror} is a <dfn>legal bean type</dfn> as defined by
   * the <a href="https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#legal_bean_types">CDI
   * specification</a>.
   *
   * <p>Legal bean types are, exactly:</p>
   *
   * <ol>
   *
   * <li>{@linkplain javax.lang.model.type.TypeKind#ARRAY Array} types whose {@linkplain ArrayType#getComponentType()
   * component type}s are legal bean types</li>
   *
   * <li>{@linkplain javax.lang.model.type.TypeKind#isPrimitive() Primitive} types</li>
   *
   * <li>{@linkplain javax.lang.model.type.TypeKind#DECLARED Declared} types that <dfn>contain</dfn> no {@linkplain
   * javax.lang.model.type.TypeKind#WILDCARD wildcard type}s for every interpretation and level of containment</li>
   *
   * </ol>
   *
   * @param t a {@link TypeMirror}; must not be {@code null}
   *
   * @return {@code true} if and only if {@code t} is a legal bean type; {@code false} otherwise
   *
   * @exception NullPointerException if {@code t} is {@code null}
   *
   * @spec https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#legal_bean_types CDI Specification, version
   * 4.1, section 2.2.1
   *
   * @see <a
   * href="https://issues.redhat.com/browse/CDI-502?focusedId=13036118&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-13036118">CDI-502</a>
   *
   * @see <a href="https://github.com/jakartaee/cdi/issues/823">CDI issue 823</a>
   *
   * @see <a href="https://issues.redhat.com/browse/WELD-1492">WELD-1492</a>
   *
   * @microbean.idempotency This method is idempotent and deterministic.
   *
   * @microbean.threadsafety This method itself is safe for concurrent use by multiple threads, but {@link TypeMirror}
   * implementations and {@link Domain} implementations may not be safe for such use.
   */
  public static final boolean legalBeanType(final TypeMirror t) {
    // https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#assignable_parameters
    // https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#legal_bean_types
    return switch (t.getKind()) {

    // "A bean type may be an array type."
    //
    // "However, some Java types are not legal bean types: [...] An array type whose component type is not a legal bean
    // type"
    case ARRAY -> {
      if (!legalBeanType(((ArrayType)t).getComponentType())) { // note recursion
        if (LOGGER.isLoggable(WARNING)) {
          LOGGER.log(WARNING, t + " has a component type that is an illegal bean type (" + ((ArrayType)t).getComponentType() + ")");
        }
        yield false;
      }
      yield true;
    }

    // "A bean type may be a primitive type. Primitive types are considered to be identical to their corresponding
    // wrapper types in java.lang."
    case BOOLEAN, BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT -> true;

    // "A bean type may be a parameterized type with actual [non-wildcard, non-type-variable, see below] type parameters
    // [arguments] and type variables." (A bean type may be a parameterized type whose type arguments are either array
    // types, declared types, or type variables.)
    //
    // "However, some Java types are not legal bean types: [...] A parameterized type that contains [anywhere, see
    // below] a wildcard type parameter [argument] is not a legal bean type."
    //
    // Some ink has been spilled on what it means for a "parameterized" (generic) type to "contain" a "wildcard type
    // parameter [argument]" (https://issues.redhat.com/browse/CDI-502). Because it turns out that an "actual type" is a
    // non-wildcard, non-type-variable type, it follows that *no* wildcard type argument appearing *anywhere* in a bean
    // type's declaration is permitted. Note that this definition of "actual type" does not appear in the CDI
    // specification, but only in a (closed) JIRA issue raised against the specification
    // (https://issues.redhat.com/browse/CDI-502?focusedId=13036118&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-13036118):
    // "An actual type is a type that is not a wildcard nor [sic] an unresolved [sic] type variable."
    //
    // This still seems way overstrict to me but there you have it.
    case DECLARED -> {
      for (final TypeMirror ta : ((DeclaredType)t).getTypeArguments()) {
        if (ta.getKind() != TYPEVAR && !legalBeanType(ta)) { // note recursion
          if (LOGGER.isLoggable(WARNING)) {
            LOGGER.log(WARNING, t + " has a type argument that is an illegal bean type (" + ta + ")");
          }
          yield false;
        }
      }
      yield true;
    }

    // "A type variable is not a legal bean type." (Nothing else is either.)
    default -> {
      if (LOGGER.isLoggable(WARNING)) {
        LOGGER.log(WARNING, t + " is an illegal bean type");
      }
      yield false;
    }
    };
  }

  /**
   * Returns {@code true} if and only if the supplied {@link TypeMirror} is a {@linkplain #legalBeanType(TypeMirror)
   * legal}, {@linkplain javax.lang.model.type.TypeKind#DECLARED declared}, <dfn>proxiable bean type</dfn> as defined by
   * the <a href="https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#unproxyable">CDI specification</a>.
   *
   * @param t a {@link TypeMirror}; must not be {@code null}
   *
   * @return {@code true} if and only if the supplied {@link TypeMirror} is a <dfn>proxiable bean type</dfn>
   *
   * @exception NullPointerException if {@code t} is {@code null}
   *
   * @see #proxiableElement(Element)
   *
   * @spec https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#unproxyable CDI Specification, version 4.1,
   * section 3.10
   *
   * @microbean.idempotency This method is idempotent and deterministic.
   *
   * @microbean.threadsafety This method itself is safe for concurrent use by multiple threads, but {@link TypeMirror}
   * implementations and {@link Domain} implementations may not be safe for such use.
   */
  public static final boolean proxiableBeanType(final TypeMirror t) {
    // https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#unproxyable
    return
      t.getKind() == DECLARED &&
      legalBeanType(t) &&
      proxiableElement(((DeclaredType)t).asElement());
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Element} is an {@linkplain ElementKind#INTERFACE
   * interface}, or a <dfn>proxiable</dfn> {@linkplain ElementKind#CLASS class}, as defined by the <a
   * href="https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#unproxyable">CDI specification</a>.
   *
   * @param e an {@link Element}; must not be {@code null}
   *
   * @return {@code true} if and only if the supplied {@link Element} is <dfn>proxiable</fn>
   *
   * @excepiton NullPointerException if {@code e} is {@code null}
   *
   * @spec https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1unproxyable CDI Specification, version 4.1,
   * section 3.10
   *
   * @microbean.idempotency This method is idempotent and deterministic.
   *
   * @microbean.threadsafety This method itself is safe for concurrent use by multiple threads, but {@link TypeMirror}
   * implementations and {@link Domain} implementations may not be safe for such use.
   */
  static final boolean proxiableElement(final Element e) {
    // https://jakarta.ee/specifications/cdi/4.1/jakarta-cdi-spec-4.1#unproxyable
    switch (e.getKind()) {
    case CLASS:
      if (e.getModifiers().contains(FINAL) ||
          e.getModifiers().contains(SEALED) ||
          ((QualifiedNameable)e).getQualifiedName().contentEquals("java.lang.Object")) { // cheap optimization for a common case
        return false;
      }
      boolean hasNonPrivateZeroArgumentConstructor = false;
      for (final Element ee : e.getEnclosedElements()) {
        switch (ee.getKind()) {
        case CONSTRUCTOR:
          if (!hasNonPrivateZeroArgumentConstructor &&
              !ee.getModifiers().contains(PRIVATE) &&
              ((ExecutableElement)ee).getParameters().isEmpty()) {
            hasNonPrivateZeroArgumentConstructor = true;
          }
          break;
        case METHOD:
          final Collection<?> modifiers = ((ExecutableElement)ee).getModifiers();
          if (modifiers.contains(FINAL) &&
              !modifiers.contains(STATIC) &&
              !modifiers.contains(PRIVATE)) {
            return false;
          }
          break;
        }
      }
      return hasNonPrivateZeroArgumentConstructor;
    case INTERFACE:
      return true;
    default:
      return false;
    }
  }

}
