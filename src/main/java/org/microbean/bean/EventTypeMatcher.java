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

import java.util.List;
import java.util.Objects;

import java.util.function.Predicate;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import org.microbean.construct.Domain;

// Experimental and basically located in the wrong package and module.
public final class EventTypeMatcher extends AbstractTypeMatcher implements Matcher<TypeMirror, TypeMirror> {

  public EventTypeMatcher(final Domain domain) {
    super(domain);
  }

  @Override // Matcher<TypeMirror, TypeMirror>
  public final boolean test(final TypeMirror receiver, final TypeMirror payload) {
    // https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#observer_resolution
    return switch (receiver.getKind()) {
      // Interestingly array types are never discussed explicitly in the specification's sections on observer
      // resolution, but clearly they must be possible.
      case ARRAY                                                -> switch (payload.getKind()) {
        case ARRAY    -> this.identical(elementType(receiver), elementType(payload)); // never spelled out in the spec but inferred
        case DECLARED -> false;
        default       -> throw illegalPayload(payload);
      };
      // There is no mention in the specification of primitive and wrapper type interchangeability but it looks like
      // that is where the specification is headed, and all existing implementations behave as if it were fully
      // specified (see https://github.com/jakartaee/cdi/issues/829).
      case BOOLEAN                                              -> switch (payload.getKind()) {
        case ARRAY    -> false;
        case DECLARED -> named((DeclaredType)payload, "java.lang.Boolean");
        default       -> throw illegalPayload(payload);
      };
      case BYTE                                                 -> switch (payload.getKind()) {
        case ARRAY    -> false;
        case DECLARED -> named((DeclaredType)payload, "java.lang.Byte");
        default       -> throw illegalPayload(payload);
      };
      case CHAR                                                 -> switch (payload.getKind()) {
        case ARRAY    -> false;
        case DECLARED -> named((DeclaredType)payload, "java.lang.Character");
        default       -> throw illegalPayload(payload);
      };
      case DOUBLE                                               -> switch (payload.getKind()) {
        case ARRAY    -> false;
        case DECLARED -> named((DeclaredType)payload, "java.lang.Double");
        default       -> throw illegalPayload(payload);
      };
      case FLOAT                                                -> switch (payload.getKind()) {
        case ARRAY    -> false;
        case DECLARED -> named((DeclaredType)payload, "java.lang.Float");
        default       -> throw illegalPayload(payload);
      };
      case INT                                                  -> switch (payload.getKind()) {
        case ARRAY    -> false;
        case DECLARED -> named((DeclaredType)payload, "java.lang.Integer");
        default       -> throw illegalPayload(payload);
      };
      case LONG                                                 -> switch (payload.getKind()) {
        case ARRAY    -> false;
        case DECLARED -> named((DeclaredType)payload, "java.lang.Long");
        default       -> throw illegalPayload(payload);
      };
      case SHORT                                                -> switch (payload.getKind()) {
        case ARRAY    -> false;
        case DECLARED -> named((DeclaredType)payload, "java.lang.Short");
        default       -> throw illegalPayload(payload);
      };
      case DECLARED                                             -> switch (payload.getKind()) {
        case ARRAY    -> false; // implied
        case DECLARED ->
          this.identical(receiver, payload) || // interestingly this is never spelled out in the spec
          this.assignable((DeclaredType)receiver, (DeclaredType)payload);
        default       -> throw illegalPayload(payload);
      };
      // "An event type [payload] is considered assignable to a type variable [receiver] if the event type [payload] is
      // assignable to the [condensed] upper bound [of the type variable [receiver], which may be an intersection type],
      // if any [in which case the upper bound will be java.lang.Object]."
      case TYPEVAR                                              -> switch (payload.getKind()) {
        // TODO: I think this is fully handled by covariantlyAssignable(rta, pta).
        // case ARRAY, DECLARED -> assignableToCondensedTypeVariableBounds((TypeVariable)receiver, payload);
        case ARRAY, DECLARED -> this.covariantlyAssignable(receiver, payload);
        default              -> throw illegalPayload(payload);
      };
      default                                                   -> throw illegalReceiver(receiver);
    };
  }

  private final boolean assignable(final DeclaredType receiver, final DeclaredType payload) {
    assert receiver.getKind() == TypeKind.DECLARED;
    assert payload.getKind() == TypeKind.DECLARED;
    return switch (payload) {

      case DeclaredType parameterizedPayload when parameterized(payload) -> switch (receiver) {
        // "A parameterized event type [parameterizedPayload] is considered assignable to..."

        case DeclaredType nonGenericOrRawReceiver when !generic(receiver) || raw(receiver) ->
          // "...a [non-generic class or] raw observed event type [nonGenericOrRawReceiver] if the [non-generic class
          // or] raw types are identical [undefined]."
          this.identical(this.nonGenericClassOrRawType(nonGenericOrRawReceiver),
                         this.nonGenericClassOrRawType(parameterizedPayload));

        case DeclaredType parameterizedReceiver -> {
          // "...a parameterized observed event type [parameterizedReceiver]..."
          assert parameterized(receiver);
          if (this.identical(this.rawType(parameterizedReceiver),
                             this.rawType(parameterizedPayload))) {
            // "...if they have identical raw type[s] [really if their declarations/elements are 'identical']..."

            final List<? extends TypeMirror> rtas = parameterizedReceiver.getTypeArguments();
            final List<? extends TypeMirror> ptas = parameterizedPayload.getTypeArguments();
            assert rtas.size() == ptas.size();
            for (int i = 0; i < rtas.size(); i++) {
              final TypeMirror rta = rtas.get(i);
              final TypeMirror pta = ptas.get(i);
              // "...and for each parameter [type argument pair]..."

              switch (rta.getKind()) {

              case ARRAY: // rta
              case DECLARED: // rta
                // "...the observed event type parameter [receiver type argument, rta] is an actual type [a non-type
                // variable, non-wildcard reference type] with identical raw type to the event type parameter [and is a
                // non-generic class (or has a raw type) identical to the payload type argument (pta)]..."
                if (this.identical(this.nonGenericClassOrRawType(rta),
                                   pta)) {
                  // "...and, if the type [?] is parameterized [?]..."
                  if (cdiParameterized(rta)) { // really just yieldsRawType(rta)
                    assert cdiParameterized(pta); // ...because otherwise their raw types would not have been "identical"
                    // "...the event type parameter [receiver type argument, rta] is assignable to the observed event
                    // type parameter according to [all of] these rules..."
                    if (this.test(rta, pta)) { // note recursion
                      continue;
                    }
                  } else {
                    continue; // yes, trust me; vetted
                  }
                }
                yield false;

              case TYPEVAR: // rta
                // "...the observed event type parameter [receiver type argument, rta] is a type variable and the event
                // type parameter [payload type argument, pta] is assignable to the upper bound, if any [type variables
                // have multiple bounds], of the type variable [receiver type argument, rta]"
                /*
                if (assignableToCondensedTypeVariableBounds((TypeVariable)rta, (ReferenceType)pta)) {
                  // TODO: I think this is fully handled by covariantlyAssignable(rta, pta).
                  continue;
                }
                */
                if (this.covariantlyAssignable(rta, pta)) {
                  continue;
                }
                yield false;

              case WILDCARD: // rta
                // "...the observed event type parameter [receiver type argument, rta] is a wildcard and the event type
                // parameter [payload type argument, pta] is assignable to the upper bound, if any, of the wildcard and
                // assignable from the lower bound, if any, of the wildcard..."
                /*
                if (assignableToCondensedExtendsBound((WildcardType)rta, (ReferenceType)pta) &&
                    assignableFromCondensedSuperBound((ReferenceType)pta, (WildcardType)rta)) {
                    // TODO: is this simply the contains() relationship? So basically: does rta contain pta?
                  continue;
                }
                */
                if (this.contains(rta, pta)) {
                  continue;
                }
                yield false;

              default: // rta
                throw new AssertionError("rta: " + rta); // type arguments in Java can't be anything else
              } // end switch(rta.getKind())

            } // end type argument loop
            yield true; // we passed all the type argument assignability tests

          } // end this.identical() check
          yield false; // the type arguments' non-generic classes or raw types were not identical
        }

      }; // end switch(receiver); end parameterizedPayload case

      case DeclaredType nonGenericOrRawPayload -> switch (receiver) {
        // "A [non-generic or] raw event type [nonGenericOrRawayload] is considered assignable..."

        case DeclaredType parameterizedReceiver when parameterized(receiver) -> {
          // "...to a parameterized observed event type [parameterizedReceiver] if the[ir] [non-generic classes or] raw
          // types are identical and all type parameters [type arguments] of the required type [observed event type,
          // receiver] are either unbounded type variables or java.lang.Object."
          yield
            this.identical(this.nonGenericClassOrRawType(parameterizedReceiver),
                           nonGenericOrRawPayload) &&
            allTypeArgumentsAre(parameterizedReceiver.getTypeArguments(),
                                ((Predicate<TypeMirror>)this::unboundedTypeVariable).or(this.domain()::javaLangObject));
        }

        // [Otherwise the payload is not assignable to the receiver; identity checking should have already happened in
        // test(), not here.]
        case DeclaredType nonGenericOrRawReceiver -> false; // or this.identical(nonGenericOrRawReceiver, nonGenericOrRawPayload);

      }; // end switch(receiver); end nonGenericOrRawPayload case

    }; // end switch(payload)
  }

}
