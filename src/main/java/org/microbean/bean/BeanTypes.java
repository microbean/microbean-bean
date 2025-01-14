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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;

import java.util.function.Predicate;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.microbean.assign.Types;

import org.microbean.construct.Domain;

import static java.lang.System.Logger.Level.WARNING;

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


  private final Map<TypeMirror, List<? extends TypeMirror>> beanTypesCache;


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
   * Returns an immutable {@link List} of {@linkplain #legalBeanType(TypeMirror) legal bean types} that the supplied
   * {@link TypeMirror} bears.
   *
   * <p>The returned {@link List} may be empty.</p>
   *
   * @param t a {@link TypeMirror}; must not be {@code null}
   *
   * @return an immutable {@link List} of {@linkplain #legalBeanType(TypeMirror) legal bean types} that the supplied
   * {@link TypeMirror} bears; never {@code null}
   *
   * @exception NullPointerException if {@code t} is {@code null}
   *
   * @microbean.nullability This method never returns {@code null}.
   *
   * @microbean.idempotency This method is idempotent and returns determinate values.
   *
   * @microbean.threadsafety This method is safe for concurrent use by multiple threads.
   */
  public final List<? extends TypeMirror> beanTypes(final TypeMirror t) {
    // https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#assignable_parameters
    // https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#legal_bean_types
    // https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#managed_bean_types
    // https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#producer_field_types
    // https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#producer_method_types
    return switch (t.getKind()) {
    case ARRAY -> this.beanTypesCache.computeIfAbsent(t, t0 -> legalBeanType(t0) ? List.of(t0, this.domain().javaLangObject().asType()) : List.of());
    case BOOLEAN, BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT -> this.beanTypesCache.computeIfAbsent(t, t0 -> List.of(t0, this.domain().javaLangObject().asType()));
    case DECLARED, TYPEVAR -> this.beanTypesCache.computeIfAbsent(t, t0 -> this.supertypes(t0, BeanTypes::legalBeanType));
    default -> {
      assert !legalBeanType(t);
      yield List.of();
    }
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
   * Returns {@code true} if and only if the supplied {@link TypeMirror} is a <dfn>legal bean type</dfn>.
   *
   * <p>Legal bean types are, exactly:</p>
   *
   * <ol>
   *
   * <li>{@linkplain TypeKind#ARRAY Array} types whose {@linkplain ArrayType#getComponentType() component type}s are
   * legal bean types</li>
   *
   * <li>{@linkplain TypeKind#isPrimitive() Primitive} types</li>
   *
   * <li>{@linkplain TypeKind#DECLARED Declared} types that contain no {@linkplain TypeKind#WILDCARD wildcard type}s for
   * every level of containment</li>
   *
   * </ol>
   *
   * @param t a {@link TypeMirror}; must not be {@code null}
   *
   * @return {@code true} if and only if {@code t} is a legal bean type; {@code false} otherwise
   *
   * @exception NullPointerException if {@code t} is {@code null}
   *
   * @microbean.idempotency This method is idempotent and deterministic.
   *
   * @microbean.threadsafety This method itself is safe for concurrent use by multiple threads, but {@link TypeMirror}
   * implementations and {@link Domain} implementations may not be safe for such use.
   */
  public static final boolean legalBeanType(final TypeMirror t) {
    // https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#assignable_parameters
    // https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#legal_bean_types
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

    // "A bean type may be a parameterized type with actual [see below] type parameters [arguments] and type variables."
    //
    // "However, some Java types are not legal bean types: [...] A parameterized type that contains [see below] a
    // wildcard type parameter [argument] is not a legal bean type."
    //
    // Some ink has been spilled on what it means for a "parameterized" (generic) type to "contain" a "wildcard type
    // parameter [argument]" (https://issues.redhat.com/browse/CDI-502). Because it turns out that "actual type"
    // apparently means, among other things, a non-wildcard type, it follows that *no* wildcard type argument appearing
    // *anywhere* in a bean type is permitted. Note that the definition of "actual type" does not appear in the CDI
    // specification, but only in a closed JIRA issue
    // (https://issues.redhat.com/browse/CDI-502?focusedId=13036118&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-13036118).
    //
    // This still seems way overstrict to me but there you have it.
    case DECLARED -> {
      for (final TypeMirror ta : ((DeclaredType)t).getTypeArguments()) {
        if (ta.getKind() != TypeKind.TYPEVAR && !legalBeanType(ta)) { // note recursion
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

}
