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

import java.util.List;
import java.util.Optional;

import javax.lang.model.type.TypeMirror;

import org.microbean.attributes.Attributed;
import org.microbean.attributes.Attributes;

import org.microbean.constant.Constables;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_List;
import static java.util.Arrays.asList;

/**
 * A pairing of a {@link TypeMirror} with a {@link List} of {@link Attributes}s.
 *
 * @param type a {@link TypeMirror}
 *
 * @param attributes a {@link List} of {@link Attributes}s
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 */
public final record AttributedType(TypeMirror type, List<Attributes> attributes) implements Attributed, Constable {

  /**
   * Creates a new {@link AttributedType}.
   *
   * @param type a {@link TypeMirror}; must not be {@code null}; must be a {@linkplain
   * javax.lang.model.type.TypeKind#isPrimitive() primitive}, {@linkplain javax.lang.model.type.TypeKind#ARRAY array} or
   * {@linkplain javax.lang.model.type.TypeKind#DECLARED declared} type
   *
   * @param attributes an array of {@link Attributes}; may be {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}
   *
   * @exception IllegalArgumentException if {@code type} is the wrong kind of type
   */
  public AttributedType(final TypeMirror type, final Attributes... attributes) {
    this(type, attributes == null || attributes.length <= 0 ? List.of() : asList(attributes));
  }

  /**
   * Creates a new {@link AttributedType}.
   *
   * @param type a {@link TypeMirror}; must not be {@code null}; must be a {@linkplain
   * javax.lang.model.type.TypeKind#isPrimitive() primitive}, {@linkplain javax.lang.model.type.TypeKind#ARRAY array} or
   * {@linkplain javax.lang.model.type.TypeKind#DECLARED declared} type
   *
   * @param attributes a {@link List} of {@link Attributes}; must not be {@code null}
   *
   * @exception NullPointerException if either argument is {@code null}
   *
   * @exception IllegalArgumentException if {@code type} is the wrong kind of type
   */
  public AttributedType {
    switch (type.getKind()) {
    case ARRAY, BOOLEAN, BYTE, CHAR, DECLARED, DOUBLE, FLOAT, INT, LONG, SHORT:
      break;
    case ERROR, EXECUTABLE, INTERSECTION, MODULE, NONE, NULL, OTHER, PACKAGE, TYPEVAR, UNION, VOID, WILDCARD:
    default:
      throw new IllegalArgumentException("type: " + type);
    }
    attributes = List.copyOf(attributes);
  }

  /**
   * Creates a new {@link AttributedType}.
   *
   * @param type a {@link TypeMirror}; must not be {@code null}; must be a {@linkplain
   * javax.lang.model.type.TypeKind#isPrimitive() primitive}, {@linkplain javax.lang.model.type.TypeKind#ARRAY array} or
   * {@linkplain javax.lang.model.type.TypeKind#DECLARED declared} type
   *
   * @exception NullPointerException if {@code type} is {@code null}
   *
   * @exception IllegalArgumentException if {@code type} is the wrong kind of type
   */
  public AttributedType(final TypeMirror type) {
    this(type, List.of());
  }

  /**
   * Returns an {@link Optional} containing a {@link ConstantDesc} describing this {@link AttributedType}, or an
   * {@linkplain Optional#isEmpty() empty <code>Optional</code>} if it could not be described.
   *
   * @return an {@link Optional} containing a {@link ConstantDesc} describing this {@link AttributedType}, or an
   * {@linkplain Optional#isEmpty() empty <code>Optional</code>} if it could not be describe; never {@code null}
   */
  @Override // Constable
  public Optional<? extends ConstantDesc> describeConstable() {
    return this.type() instanceof Constable t ? t.describeConstable() : Optional.<ConstantDesc>empty()
      .flatMap(typeDesc -> Constables.describeConstable(this.attributes())
               .map(attributesDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                             MethodHandleDesc.ofConstructor(ClassDesc.of(this.getClass().getName()),
                                                                                            ClassDesc.of(TypeMirror.class.getName()),
                                                                                            CD_List),
                                                             typeDesc,
                                                             attributesDesc)));
  }

  /**
   * Returns an {@link AttributedType} comprising the supplied arguments.
   *
   * @param type a {@link TypeMirror}; must not be {@code null}; must be a {@linkplain
   * javax.lang.model.type.TypeKind#isPrimitive() primitive}, {@linkplain javax.lang.model.type.TypeKind#ARRAY array} or
   * {@linkplain javax.lang.model.type.TypeKind#DECLARED declared} type
   *
   * @param attributes an array of {@link Attributes}; may be {@code null}
   *
   * @return a non-{@code null} {@link AttributedType}
   *
   * @exception NullPointerException if {@code type} is {@code null}
   *
   * @exception IllegalArgumentException if {@code type} is the wrong kind of type
   */
  public static final AttributedType of(final TypeMirror type, Attributes... attributes) {
    return new AttributedType(type, attributes == null || attributes.length <= 0 ? List.of() : asList(attributes));
  }

}
