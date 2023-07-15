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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import java.util.List;
import java.util.Objects;

import java.util.function.Supplier;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.element.TypeElement;

import javax.lang.model.type.TypeMirror;

import org.microbean.lang.Equality;
import org.microbean.lang.Lang;
import org.microbean.lang.TypeAndElementSource;

import org.microbean.lang.element.DelegatingElement;

import org.microbean.lang.type.DelegatingTypeMirror;

import org.microbean.qualifier.NamedAttributeMap;

// A placeholder for something that can receive a reference.
public final class Variable<T> {

  private static final VarHandle VALUE_SUPPLIER;

  static {
    try {
      VALUE_SUPPLIER = MethodHandles.lookup().findVarHandle(Variable.class, "valueSupplier", Supplier.class);
    } catch (final IllegalAccessException | NoSuchFieldException e) {
      throw (ExceptionInInitializerError)new ExceptionInInitializerError(e.getMessage()).initCause(e);
    }
  }

  private final Assignability assignability;

  private final DelegatingElement enclosingElement;

  private final String name;

  private final TypeMirror type;

  private final int parameterIndex;

  private final List<NamedAttributeMap<?>> attributes;

  private final boolean isTransient;

  private volatile Supplier<T> valueSupplier;

  // Field
  public Variable(final VariableElement variableElement,
                  final List<? extends NamedAttributeMap<?>> attributes) {
    this(new Assignability(Lang.typeAndElementSource()), Lang.typeAndElementSource(), Lang.sameTypeEquality(), variableElement, attributes);
  }

  // Field
  public Variable(final Assignability assignability,
                  final VariableElement variableElement,
                  final List<? extends NamedAttributeMap<?>> attributes) {
    this(assignability, Lang.typeAndElementSource(), Lang.sameTypeEquality(), variableElement, attributes);
  }

  // Field
  public Variable(final Assignability assignability,
                  final TypeAndElementSource typeAndElementSource,
                  final Equality equality,
                  final VariableElement variableElement,
                  final List<? extends NamedAttributeMap<?>> attributes) {
    super();
    final DelegatingElement field = DelegatingElement.of(variableElement, typeAndElementSource, equality);
    if (field.getKind() != ElementKind.FIELD) {
      throw new IllegalArgumentException("variableElement: " + field + "; kind: " + field.getKind());
    }
    this.assignability = Objects.requireNonNull(assignability, "assignability");
    this.enclosingElement = (DelegatingElement)field.getEnclosingElement();
    this.name = field.getSimpleName().toString();
    this.type = field.asType();
    assert this.type instanceof DelegatingTypeMirror;
    this.parameterIndex = -1;
    this.attributes = List.copyOf(attributes);
    this.isTransient = false;
  }

  // Parameter
  public Variable(final ExecutableElement executableElement,
                  final int parameterIndex,
                  final List<? extends NamedAttributeMap<?>> attributes,
                  final boolean isTransient) {
    this(new Assignability(Lang.typeAndElementSource()), Lang.typeAndElementSource(), Lang.sameTypeEquality(), executableElement, parameterIndex, attributes, isTransient);
  }

  // Parameter
  public Variable(final Assignability assignability,
                  final ExecutableElement executableElement,
                  final int parameterIndex,
                  final List<? extends NamedAttributeMap<?>> attributes,
                  final boolean isTransient) {
    this(assignability, Lang.typeAndElementSource(), Lang.sameTypeEquality(), executableElement, parameterIndex, attributes, isTransient);
  }

  // Parameter
  public Variable(final Assignability assignability,
                  final TypeAndElementSource typeAndElementSource,
                  final Equality equality,
                  final ExecutableElement executableElement,
                  final int parameterIndex,
                  final List<? extends NamedAttributeMap<?>> attributes,
                  final boolean isTransient) {
    super();
    final DelegatingElement p = (DelegatingElement)DelegatingElement.of(executableElement, typeAndElementSource, equality).getParameters().get(parameterIndex);
    this.assignability = Objects.requireNonNull(assignability, "assignability");
    this.enclosingElement = validateEnclosingElement((DelegatingElement)p.getEnclosingElement());
    this.name = p.getSimpleName().toString();
    this.type = p.asType();
    assert this.type instanceof DelegatingTypeMirror;
    this.parameterIndex = parameterIndex;
    this.attributes = List.copyOf(attributes);
    this.isTransient = isTransient;
  }

  // "Anonymous" usage
  public Variable(final TypeMirror type,
                  final List<? extends NamedAttributeMap<?>> attributes) {
    this(new Assignability(Lang.typeAndElementSource()), Lang.typeAndElementSource(), Lang.sameTypeEquality(), null, type, attributes);
  }

  // "Anonymous" usage within a method or class
  public Variable(final Element enclosingElement,
                  final TypeMirror type,
                  final List<? extends NamedAttributeMap<?>> attributes) {
    this(new Assignability(Lang.typeAndElementSource()), Lang.typeAndElementSource(), Lang.sameTypeEquality(), enclosingElement, type, attributes);
  }

  // "Anonymous" usage
  public Variable(final Assignability assignability,
                  final TypeMirror type,
                  final List<? extends NamedAttributeMap<?>> attributes) {
    this(assignability, Lang.typeAndElementSource(), Lang.sameTypeEquality(), null, type, attributes);
  }

  // "Anonymous" usage within a method or class
  public Variable(final Assignability assignability,
                  final Element enclosingElement,
                  final TypeMirror type,
                  final List<? extends NamedAttributeMap<?>> attributes) {
    this(assignability, Lang.typeAndElementSource(), Lang.sameTypeEquality(), enclosingElement, type, attributes);
  }

  // "Anonymous" usage
  public Variable(final Assignability assignability,
                  final TypeAndElementSource typeAndElementSource,
                  final Equality equality,
                  final TypeMirror type,
                  final List<? extends NamedAttributeMap<?>> attributes) {
    this(assignability, typeAndElementSource, equality, null, type, attributes);
  }

  // "Anonymous" usage within a method or class
  public Variable(final Assignability assignability,
                  final TypeAndElementSource typeAndElementSource,
                  final Equality equality,
                  final Element enclosingElement,
                  final TypeMirror type,
                  final List<? extends NamedAttributeMap<?>> attributes) {
    super();
    this.assignability = Objects.requireNonNull(assignability, "assignability");
    this.enclosingElement = enclosingElement == null ? null : validateEnclosingElement(DelegatingElement.of(enclosingElement, typeAndElementSource, equality));
    this.name = null;
    this.type = validateType(DelegatingTypeMirror.of(type, typeAndElementSource, equality));
    this.parameterIndex = -1;
    this.attributes = List.copyOf(attributes);
    this.isTransient = false;
  }

  public final Element enclosingElement() {
    return this.enclosingElement;
  }

  public final String name() {
    return this.name;
  }

  public final TypeMirror type() {
    return this.type;
  }

  public final List<NamedAttributeMap<?>> attributes() {
    return this.attributes;
  }

  public final int parameterIndex() {
    return this.parameterIndex;
  }

  public final boolean local() {
    return this.name() == null;
  }
  
  public final boolean parameter() {
    return this.name() != null && this.parameterIndex() >= 0;
  }

  public final boolean field() {
    return this.name() != null && this.parameterIndex() < 0;
  }

  public final boolean isTransient() {
    return this.isTransient;
  }

  public final Selector selector() {
    return Selector.of(this.assignability, this.type(), this.attributes(), true);
  }

  public final T assign(final References<?> r) {
    Supplier<T> s = this.valueSupplier; // volatile read
    if (s == null) {
      final T value = r.supplyReference(this.selector());
      s = () -> value;
      if (!VALUE_SUPPLIER.compareAndSet(this, null, s)) { // volatile write
        s = this.valueSupplier; // volatile read
      }
    }
    return s.get();
  }

  public final T value() {
    final Supplier<? extends T> s = this.valueSupplier; // volatile read
    if (s == null) {
      throw new IllegalStateException("never assigned");
    }
    return s.get();
  }

  @Override // Object
  public final int hashCode() {
    // value and assignability are deliberately excluded
    int hashCode = 17;
    Object x = this.enclosingElement();
    if (x != null) {
      hashCode = 31 * hashCode + x.hashCode();
    }
    x = this.name();
    if (x != null) {
      hashCode = 31 * hashCode + x.hashCode();
    }
    hashCode = 31 * hashCode + this.type().hashCode();
    hashCode = 31 * hashCode + this.parameterIndex();
    hashCode = 31 * hashCode + this.attributes().hashCode();
    hashCode = 31 * hashCode + (this.isTransient() ? 1 : 0);
    return hashCode;
  }

  @Override // Object
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other != null && other.getClass() == this.getClass()) {
      final Variable<?> her = (Variable<?>)other;
      // value and assignability are deliberately excluded
      return
        Objects.equals(this.enclosingElement(), her.enclosingElement()) &&
        Objects.equals(this.name(), her.name()) &&
        Objects.equals(this.type(), her.type()) &&
        this.parameterIndex() == her.parameterIndex() &&
        Objects.equals(this.attributes(), her.attributes()) &&
        this.isTransient() == her.isTransient();
    } else {
      return false;
    }
  }

  @Override // Object
  public final String toString() {
    return
      this.getClass().getSimpleName() +
      "[enclosingElement=" + this.enclosingElement() +
      ", name=" + this.name() +
      ", type=" + this.type() +
      ", parameterIndex=" + this.parameterIndex() +
      ", attributes=" + this.attributes() +
      ", isTransient=" + this.isTransient() +
      "]";
  }


  /*
   * Static methods.
   */


  private static final DelegatingTypeMirror validateType(final DelegatingTypeMirror t) {
    return switch (t.getKind()) {
    case BOOLEAN, BYTE, CHAR, DECLARED, DOUBLE, FLOAT, INT, LONG, SHORT -> t;
    default -> throw new IllegalArgumentException("t: " + t + "; kind: " + t.getKind());
    };
  }

  private static final DelegatingElement validateEnclosingElement(final DelegatingElement e) {
    return switch (e.getKind()) {
    case CLASS, CONSTRUCTOR, ENUM, INTERFACE, METHOD -> e;
    default -> throw new IllegalArgumentException("e: " + e + "; kind: " + e.getKind());
    };
  }

};
