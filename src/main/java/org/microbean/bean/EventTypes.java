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
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import org.microbean.construct.Domain;

import org.microbean.construct.element.UniversalElement;

import org.microbean.construct.type.UniversalType;

import static java.lang.System.Logger.Level.WARNING;

// Experimental and basically located in the wrong package and module.
public final class EventTypes extends Types {


  /*
   * Static fields.
   */


  private static final Logger LOGGER = System.getLogger(EventTypes.class.getName());


  /*
   * Constructors.
   */


  public EventTypes(final Domain domain) {
    super(domain);
  }


  /*
   * Instance methods.
   */


  public final List<? extends TypeMirror> eventTypes(final TypeMirror t) {
    final UniversalType ut = UniversalType.of(t, this.domain());
    // https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#event_types_and_qualifier_types
    if (ut.getKind() == TypeKind.DECLARED) {
      final UniversalElement e = ut.asElement();
      if (e.getKind().isInterface() || !e.getKind().isClass() || e.getModifiers().contains(Modifier.ABSTRACT)) {
        // "An event object is an instance of a concrete Java class...."
        if (LOGGER.isLoggable(WARNING)) {
          LOGGER.log(WARNING, ut + " is an illegal event type");
        }
        return List.of();
      }
    }
    // "The event types of the event include all superclasses and interfaces of the [concrete] runtime class of the
    // event object."
    return this.supertypes(ut, this::legalEventType);
  }

  public final boolean legalEventType(final TypeMirror t) {
    return legalEventType(UniversalType.of(t, this.domain()));
  }

  public final boolean legalObservedEventType(final TypeMirror t) {
    return legalObservedEventType(this.domain(), t);
  }


  /*
   * Static methods.
   */


  public static final boolean legalEventType(final Domain domain, final TypeMirror t) {
    return legalEventType(UniversalType.of(t, domain));
  }

  static final boolean legalEventType(final UniversalType ut) {
    // https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#event_types_and_qualifier_types
    return switch (ut.getKind()) {
    case ARRAY -> {
      // Recurse into the component type.
      if (!legalEventType(ut.getComponentType())) { // note recursion
        if (LOGGER.isLoggable(WARNING)) {
          LOGGER.log(WARNING, ut + " has a component type that is an illegal event type (" + ut.getComponentType());
        }
        yield false;
      }
      yield true;
    }

    // You can't fire a primitive event as of this writing, but there's nothing stopping a primitive event type from
    // being legal otherwise.
    // case BOOLEAN, BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT -> true;

    case DECLARED -> {
      // "An event type may not contain an unresolvable type variable. A wildcard type is not considered an unresolvable
      // type variable."
      //
      // We interpret "contain" to mean "have as a type argument, recursively, anywhere".
      for (final UniversalType uta : ut.getTypeArguments()) {
        if (uta.getKind() != TypeKind.WILDCARD && !legalEventType(uta)) { // note recursion
          if (LOGGER.isLoggable(WARNING)) {
            LOGGER.log(WARNING, ut + " has a type argument that is an illegal event type (" + uta + ")");
          }
          yield false;
        }
      }
      yield true;
    }

    default -> {
      if (LOGGER.isLoggable(WARNING)) {
        LOGGER.log(WARNING, ut + " is an illegal event type");
      }
      yield false;
    }
    };

  }

  public static final boolean legalObservedEventType(final Domain domain, final TypeMirror t) {
    return legalObservedEventType(UniversalType.of(t, domain));
  }

  static final boolean legalObservedEventType(final UniversalType ut) {
    // https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#event_types_and_qualifier_types
    // "Any Java type [that a method parameter element may bear] may be an observed event type."
    return switch (ut.getKind()) {
    case ARRAY, BOOLEAN, BYTE, CHAR, DECLARED, DOUBLE, FLOAT, INT, LONG, SHORT, TYPEVAR -> true;
    default -> false;
    };
  }


}
