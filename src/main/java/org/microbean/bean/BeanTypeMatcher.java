/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2025 microBean™.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import java.util.function.Predicate;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Parameterizable;
import javax.lang.model.element.QualifiedNameable;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import org.microbean.assign.AbstractTypeMatcher;

import org.microbean.construct.Domain;

/**
 * An {@link AbstractTypeMatcher} encapsulating <a
 * href="https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#performing_typesafe_resolution">CDI-compatible
 * type matching rules</a>.
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 *
 * @see #test(TypeMirror, TypeMirror)
 */
public final class BeanTypeMatcher extends AbstractTypeMatcher {


  /*
   * Static fields.
   */


  private static final Logger LOGGER = System.getLogger(BeanTypeMatcher.class.getName());


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link BeanTypeMatcher}.
   *
   * @param domain a {@link Domain}; must not be {@code null}
   *
   * @exception NullPointerException if {@code domain} is {@code null}
   *
   * @see Domain
   */
  public BeanTypeMatcher(final Domain domain) {
    super(domain);
  }


  /*
   * Instance methods.
   */


  /*
   * Public methods.
   */

  /**
   * Returns {@code true} if and only if the supplied {@code payload} argument <a
   * href="https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#performing_typesafe_resolution"><em>matches</em></a>
   * the supplied {@code receiver} argument, according to the rules defined by <a
   * href="https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#performing_typesafe_resolution">section
   * 2.4.2.1 of the CDI specification</a>.
   *
   * @param receiver the left hand side of a type assignment; must not be {@code null}
   *
   * @param payload the right hand side of a type assignment; must not be {@code null}
   *
   * @return {@code true} if and only if the supplied {@code payload} argument <a
   * href="https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#performing_typesafe_resolution">matches</a>
   * the supplied {@code receiver} argument, according to the rules defined by <a
   * href="https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#performing_typesafe_resolution">section
   * 2.4.2.1 of the CDI specification</a>; {@code false} otherwise
   *
   * @exception NullPointerException if either argument is {@code null}
   *
   * @exception IllegalArgumentException if either type is any type other than an {@linkplain TypeKind#ARRAY array
   * type}, a {@link TypeKind#isPrimitive() primitive type}, or a {@linkplain TypeKind#DECLARED declared type}
   */
  // Is the payload assignable to the receiver? That is, does the payload "match the receiver", in CDI parlance?
  @Override // Matcher<TypeMirror, TypeMirror>
  public final boolean test(final TypeMirror receiver, final TypeMirror payload) {
    // "A bean is assignable to a given injection point if:
    //
    // "The bean has a [legal] bean type [payload] that matches the [legal] required type [receiver]. For this
    // purpose..."
    return receiver == Objects.requireNonNull(payload, "payload") || switch (receiver.getKind()) {

      case ARRAY    -> switch (payload.getKind()) {
        // "...array types are considered to match only if their element types [note: not component types] are
        // identical ['identical' is actually undefined in the specification]..."
        case ARRAY                                                -> this.identical(this.domain().elementType((ArrayType)receiver), this.domain().elementType((ArrayType)payload));
        case BOOLEAN, BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT -> false;
        case DECLARED                                             -> false;
        default                                                   -> throw illegalPayload(payload);
      };

      // "...primitive types are considered to match their corresponding wrapper types in java.lang..."
      case BOOLEAN  -> switch (payload.getKind()) {
        case ARRAY                                                -> false;
        case BOOLEAN                                              -> true;
        case          BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT -> false;
        case DECLARED                                             -> named((DeclaredType)payload, "java.lang.Boolean");
        default                                                   -> throw illegalPayload(payload);
      };

      case BYTE     -> switch (payload.getKind()) {
        case ARRAY                                                -> false;
        case BYTE                                                 -> true;
        case BOOLEAN,       CHAR, DOUBLE, FLOAT, INT, LONG, SHORT -> false;
        case DECLARED                                             -> named((DeclaredType)payload, "java.lang.Byte");
        default                                                   -> throw illegalPayload(payload);
      };

      case CHAR     -> switch (payload.getKind()) {
        case ARRAY                                                -> false;
        case CHAR                                                 -> true;
        case BOOLEAN, BYTE,       DOUBLE, FLOAT, INT, LONG, SHORT -> false;
        case DECLARED                                             -> named((DeclaredType)payload, "java.lang.Character");
        default                                                   -> throw illegalPayload(payload);
      };

      case DOUBLE   -> switch (payload.getKind()) {
        case ARRAY                                                -> false;
        case DOUBLE                                               -> true;
        case BOOLEAN, BYTE, CHAR,         FLOAT, INT, LONG, SHORT -> false;
        case DECLARED                                             -> named((DeclaredType)payload, "java.lang.Double");
        default                                                   -> throw illegalPayload(payload);
      };

      case FLOAT    -> switch (payload.getKind()) {
        case ARRAY                                                -> false;
        case FLOAT                                                -> true;
        case BOOLEAN, BYTE, CHAR, DOUBLE,        INT, LONG, SHORT -> false;
        case DECLARED                                             -> named((DeclaredType)payload, "java.lang.Float");
        default                                                   -> throw illegalPayload(payload);
      };

      case INT      -> switch (payload.getKind()) {
        case ARRAY                                                -> false;
        case INT                                                  -> true;
        case BOOLEAN, BYTE, CHAR, DOUBLE, FLOAT,      LONG, SHORT -> false;
        case DECLARED                                             -> named((DeclaredType)payload, "java.lang.Integer");
        default                                                   -> throw illegalPayload(payload);
      };

      case LONG     -> switch (payload.getKind()) {
        case ARRAY                                                -> false;
        case LONG                                                 -> true;
        case BOOLEAN, BYTE, CHAR, DOUBLE, FLOAT, INT,       SHORT -> false;
        case DECLARED                                             -> named((DeclaredType)payload, "java.lang.Long");
        default                                                   -> throw illegalPayload(payload);
      };

      case SHORT    -> switch (payload.getKind()) {
        case ARRAY                                                -> false;
        case SHORT                                                -> true;
        case BOOLEAN, BYTE, CHAR, DOUBLE, FLOAT, INT, LONG        -> false;
        case DECLARED                                             -> named((DeclaredType)payload, "java.lang.Short");
        default                                                   -> throw illegalPayload(payload);
      };

      case DECLARED -> switch (payload.getKind()) {
        // Implied.
        case ARRAY    -> false;

        // "...primitive types are considered to match their corresponding wrapper types in java.lang..."
        case BOOLEAN  -> named((DeclaredType)receiver, "java.lang.Boolean");
        case BYTE     -> named((DeclaredType)receiver, "java.lang.Byte");
        case CHAR     -> named((DeclaredType)receiver, "java.lang.Character");
        case DOUBLE   -> named((DeclaredType)receiver, "java.lang.Double");
        case FLOAT    -> named((DeclaredType)receiver, "java.lang.Float");
        case INT      -> named((DeclaredType)receiver, "java.lang.Integer");
        case LONG     -> named((DeclaredType)receiver, "java.lang.Long");
        case SHORT    -> named((DeclaredType)receiver, "java.lang.Short");

        // "Parameterized and raw types [and non-generic classes, and non-array types]..."
        case DECLARED ->
          // "...are considered to match if they are identical [undefined]..."
          this.identical(receiver, payload) ||
          // "...or if the bean type [payload] is assignable to [see #assignable(TypeMirror, TypeMirror)] the required type
          // [receiver]...."
          this.assignable((DeclaredType)receiver, (DeclaredType)payload);

        default       -> throw illegalPayload(payload);
      };

      default       -> throw illegalReceiver(receiver);
    };
  }

  /*
   * Private methods.
   */

  private final boolean assignable(final DeclaredType receiver, final DeclaredType payload) {
    assert receiver.getKind() == TypeKind.DECLARED;
    assert payload.getKind() == TypeKind.DECLARED;
    return switch (payload) {

      case DeclaredType parameterizedPayload when this.domain().parameterized(payload) -> switch (receiver) {
        // "A parameterized bean type [parameterizedPayload] is considered assignable..."

        case DeclaredType nonGenericOrRawReceiver when !this.domain().generic(receiver) || this.domain().raw(receiver) ->
          // "...to a [non-generic class or] raw required type [nonGenericOrRawReceiver] if the [non-generic class or]
          // raw types are identical [undefined] and all type parameters [type arguments] of the bean type
          // [parameterizedPayload] are either unbounded type variables [undefined] or java.lang.Object."
          this.identical(this.nonGenericClassOrRawType(nonGenericOrRawReceiver),
                         this.nonGenericClassOrRawType(parameterizedPayload)) &&
          allAre(parameterizedPayload.getTypeArguments(),
                 ((Predicate<TypeMirror>)this::unboundedTypeVariable).or(this.domain()::javaLangObject));

        case DeclaredType parameterizedReceiver -> {
          // "...to a parameterized required type [parameterizedReceiver]..."

          if (this.identical(this.domain().rawType(parameterizedReceiver), this.domain().rawType(parameterizedPayload))) {
            // "...if they have identical raw type [really if their declarations/elements are 'identical']..."

            final List<? extends TypeMirror> rtas = parameterizedReceiver.getTypeArguments();
            final List<? extends TypeMirror> ptas = parameterizedPayload.getTypeArguments();
            assert rtas.size() == ptas.size();
            for (int i = 0; i < rtas.size(); i++) {
              final TypeMirror rta = rtas.get(i);
              final TypeMirror pta = ptas.get(i);
              // "...and for each parameter [type argument pair] the required type parameter [receiver type argument,
              // rta] and the bean type parameter [payload type argument, pta]..."

              switch (rta.getKind()) {
              case ARRAY: // rta
              case DECLARED: // rta
                switch (pta.getKind()) {

                case ARRAY: // pta
                case DECLARED: // pta
                  // "...are actual types [non-type variable, non-wildcard reference types]..."
                  //
                  // CDI mentions actual types but does not define what they are. This method attempts to divine and
                  // implement the intent.
                  //
                  // A comment in a closed bug report (CDI-502)
                  // (https://issues.redhat.com/browse/CDI-502?focusedId=13036118&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-13036118)
                  // by one of the reference implementation's authors (Jozef Hartinger) provides the only definition:
                  //
                  // "An actual type is a type that is not a wildcard nor [sic] an unresolved [sic] type variable."
                  //
                  // The Java Language Specification does not mention anything about "actual types" or type variable
                  // "resolution". CDI does not mention anything about type variable "resolution".
                  //
                  // (Clearly type variable "resolution" must simply be the process of supplying a type argument for a
                  // type parameter.)
                  //
                  // Presumably the null type is not intended to be an actual type either.
                  //
                  // More strictly, therefore, the intent seems to be that an actual type is an array, declared or
                  // primitive type, and none other.
                  //
                  // See also:
                  // https://github.com/weld/core/blob/5.1.2.Final/impl/src/main/java/org/jboss/weld/util/Types.java#L213

                  if (this.identical(this.nonGenericClassOrRawType(rta), this.nonGenericClassOrRawType(pta))) {
                    // "...with identical [non-generic classes or] raw types[s], and, if the type [?] is parameterized
                    // [?]..."
                    //
                    // For a maximally illustrative example, let rta and pta be array types with parameterized element
                    // types, such as List<Number>[] and List<String>[]. Then per the Java Language Specification (JLS)
                    // their raw types are List[] and List[], and their parameterized element types are List<Number> and
                    // List<String>. According to the JLS, neither List<Number>[] nor List<String>[] is parameterized.
                    //
                    // In this example, if we get here, we have only proven that List[] is "identical to" List[].
                    //
                    // The "if the type is parameterized" clause is tough. Which type is under discussion? "the type"
                    // seems to refer to the "identical raw type". But a raw type by definition is not parameterized, so
                    // this clause would seem to be superfluous and thus never apply, and so List<Number>[] :=
                    // List<String>[] is OK. Oops. So not that interpretation.
                    //
                    // What if instead the "if the type is parameterized" clause means the receiver type itself and
                    // somehow loosely the payload type as well? Well, clearly it cannot correctly do this, since an
                    // array type is never a parameterized type. Same bad result. Oops. So not that interpretation.
                    //
                    // Or what if "identical raw type" really means "identical raw type (or identical component type if
                    // they are array types)"? That would be most efficient, since it would rule out List<Number>[] :=
                    // List<String>[] right off the bat: we wouldn't even get here. But that really doesn't seem to be
                    // what is meant. So probably not that interpretation.
                    //
                    // What if the "if the type is parameterized" clause really means "if the element type declaration
                    // used by both the receiver and payload types is generic"? That would work. That's also
                    // semantically equivalent to something like: "...if at least one of the two arguments is a
                    // parameterized type [e.g. List<Number>, not List<Number>[], not String, not List], or at least one
                    // of the two types is an array type with a parameterized element type [e.g. List<Number>[], not
                    // List[], not String[]]..."
                    //
                    // That is the interpretation we apply here. So:
                    //
                    //   "...and, if the type [?] is parameterized [?]..."
                    //
                    // becomes:
                    //
                    //   "...and, if at least one of the two type arguments is a parameterized type, or if at least one
                    //   of the two types is an array type with a parameterized element type..."
                    //
                    // That, in turn, designates any type capable of properly yielding a raw type, while ruling out
                    // those that can't! That means it is exactly equal to the yieldsRawType(TypeMirror) method,
                    if (yieldsRawType(rta)) {
                      assert yieldsRawType(pta); // ...because otherwise their raw types would not have been "identical"
                      // "...the bean type parameter [type argument] is assignable to the required type parameter [type
                      // argument] according to [all of] these rules [including 'matching']..."
                      if (test(rta, pta)) { // note recursion
                        continue; // or break
                      }
                    } else {
                      assert !yieldsRawType(pta);
                      continue; // yes, trust me; vetted (or break)
                    }
                  }
                  yield false;

                case TYPEVAR: // pta
                  // "...the required type parameter [receiver type argument, rta] is an actual type [an array or
                  // declared type], the bean type parameter [payload type argument, pta] is a type variable, and the
                  // actual type [rta] is assignable to the upper bound, if any, of the type variable [pta]..."
                  /*
                  if (assignableToCondensedTypeVariableBounds((TypeVariable)pta, rta)) {
                    //
                    // (This is weird. Yes, this is "backwards"; the *receiver* type argument is being tested to see if
                    // it is assignable to the *payload* type argument.)
                    //
                    // TODO: I think this is fully handled by covariantlyAssignable(pta, rta)
                    continue;
                  }
                  */
                  if (this.covariantlyAssignable(pta, rta)) {
                    continue;
                  }
                  yield false;

                case WILDCARD: // pta
                  throw illegalPayload(payload); // bean types can't have wildcard type arguments

                default: // pta
                  throw new AssertionError();
                }

              case TYPEVAR: // rta
                switch (pta.getKind()) {

                case ARRAY: // pta
                case DECLARED: // pta
                  yield false; // implied

                case TYPEVAR: // pta
                  // "...the required type parameter [receiver type argument, rta] and the bean type parameter [payload
                  // type argument, pta] are both type variables, and the upper bound of the required type parameter
                  // [rta] [a type variable has many bounds?] is assignable to the upper bound [a type variable has many
                  // bounds?], if any, of the bean type parameter [pta]..."
                  /*
                  if (condensedTypeVariableBoundsAssignableToCondensedTypeVariableBounds((TypeVariable)pta, (TypeVariable)rta)) {
                    //
                    // (This is weird. Yes, the *receiver* type argument is being tested to see if it is assignable to
                    // the *payload* type argument.)
                    //
                    // TODO: I think this is fully handled by covariantlyAssignable(pta, rta).
                    continue;
                  }
                  */
                  if (this.covariantlyAssignable(pta, rta)) {
                    continue;
                  }
                  yield false;

                case WILDCARD: // pta
                  throw illegalPayload(payload); // bean types can't have wildcard type arguments

                default: // pta
                  throw new AssertionError();
                }

              case WILDCARD: // rta
                // "...the required type parameter [receiver type argument, rta] is a wildcard..."
                switch (pta.getKind()) {

                case ARRAY: // pta
                case DECLARED: // pta
                  // "...the bean type parameter [payload type argument, pta] is an actual type [a non-type variable,
                  // non-wildcard reference type], and the actual type [pta] is assignable to the upper bound, if any,
                  // of the wildcard [receiver type argument, rta] and assignable from the lower bound, if any of the
                  // wildcard [rta]..."
                  /*
                  if (assignableToCondensedExtendsBound((WildcardType)rta, (ReferenceType)pta) &&
                      assignableFromCondensedSuperBound((ReferenceType)pta, (WildcardType)rta)) {
                    // TODO: is this simply the contains() relationship? So basically: does rta contain pta?
                    continue; // (or break)
                  }
                  */
                  if (this.domain().contains(rta, pta)) {
                    continue;
                  }
                  yield false;

                case TYPEVAR: // pta
                  // "...the required type parameter [receiver type argument, rta] is a wildcard, the bean type
                  // parameter [payload type argument, pta] is a type variable, and the upper bound of the type variable
                  // [a type variable has many bounds!] [pta] is assignable to or assignable from the upper bound, if
                  // any, of the wildcard [rta] and assignable from the lower bound, if any, of the wildcard [rta]"
                  /*
                  if ((condensedTypeVariableBoundsAssignableToCondensedExtendsBound((WildcardType)rta, (TypeVariable)pta) ||
                       condensedTypeVariableBoundsAssignableFromCondensedExtendsBound((TypeVariable)pta, (WildcardType)rta)) &&
                      condensedTypeVariableBoundsAssignableFromCondensedSuperBound((TypeVariable)pta, (WildcardType)rta)) {
                    // TODO: is this simply the contains() relationship? So basically: does rta contain pta?
                    continue;
                  }
                  */
                  if (this.domain().contains(rta, pta)) {
                    continue;
                  }
                  yield false;

                case WILDCARD: // pta
                  throw illegalPayload(payload); // bean types can't have wildcard type arguments

                default: // pta
                  throw new AssertionError();
                }

              default: // rta
                throw new AssertionError();
              }

            } // end type argument for loop
            yield true; // we passed all the type argument assignability tests

          } // end this.identical() check
          yield false; // the type arguments' non-generic classes or raw types were not identical
        }
      };

      case DeclaredType nonGenericOrRawPayload -> switch (receiver) {
        // "A [non-generic or] raw bean type [nonGenericOrRawPayload] is considered assignable..."

        case DeclaredType parameterizedReceiver when this.domain().parameterized(receiver) ->
          // "...to a parameterized required type [parameterizedReceiver] if the[ir] [non-generic classes or] raw types
          // are identical and all type parameters [type arguments] of the required type [parameterizedReceiver] are
          // either unbounded type variables [undefined] or java.lang.Object."
          this.identical(this.nonGenericClassOrRawType(parameterizedReceiver), nonGenericOrRawPayload) &&
          allAre(parameterizedReceiver.getTypeArguments(),
                 ((Predicate<TypeMirror>)this::unboundedTypeVariable).or(this.domain()::javaLangObject));

        // [Otherwise the payload is not assignable to the receiver; identity checking should have already happened in
        // test(), not here.]
        case DeclaredType nonGenericOrRawReceiver -> false; // or this.identical(nonGenericOrRawReceiver, nonGenericOrRawPayload);
      };
    };
  }

  // Are payload's condensed bounds assignable to receiver's condensed extends bound (upper bound)?
  /*
  private final boolean condensedTypeVariableBoundsAssignableToCondensedExtendsBound(final WildcardType receiver, final TypeVariable payload) {
    assert receiver.getKind() == TypeKind.WILDCARD;
    assert payload.getKind() == TypeKind.TYPEVAR;
    // "...the upper bound of the type variable [a type variable has many bounds?] is assignable TO [...] the upper
    // bound, if any, of the wildcard..."
    final TypeMirror extendsBound = receiver.getExtendsBound();
    // No need to condense arguments to eliminate useless type variables and intersection types so that Java covariant
    // semantics will work properly in this case; #covariantlyAssignable(List, List) does this already.
    return extendsBound == null || this.covariantlyAssignable(List.of((ReferenceType)extendsBound), List.of(payload));
  }
  */

  // Is payload's condensed extends bound (upper bound) covariantly assignable to receiver's condensed bounds?
  /*
  private final boolean condensedTypeVariableBoundsAssignableFromCondensedExtendsBound(final TypeVariable receiver, final WildcardType payload) {
    assert receiver.getKind() == TypeKind.TYPEVAR;
    assert payload.getKind() == TypeKind.WILDCARD;
    // "...the upper bound of the type variable [a type variable has many bounds?] is assignable [...] FROM the upper
    // bound, if any, of the wildcard..."
    final TypeMirror extendsBound = payload.getExtendsBound();
    // No need to condense arguments to eliminate useless type variables and intersection types so that Java covariant
    // semantics will work properly in this case; #covariantlyAssignable(List, List) does this already.
    return extendsBound == null || this.covariantlyAssignable(List.of(receiver), List.of((ReferenceType)extendsBound));
  }
  */

  // Is payload's super bound (lower bound) covariantly assignable to receiver's condensed bounds?
  /*
  private final boolean condensedTypeVariableBoundsAssignableFromCondensedSuperBound(final TypeVariable receiver, final WildcardType payload) {
    assert receiver.getKind() == TypeKind.TYPEVAR;
    assert payload.getKind() == TypeKind.WILDCARD;
    final TypeMirror superBound = payload.getSuperBound();
    // No need to condense arguments to eliminate useless type variables and intersection types so that Java covariant
    // semantics will work properly in this case; #covariantlyAssignable(List, List) does this already.
    return superBound == null || this.covariantlyAssignable(List.of(receiver), List.of(superBound));
  }
  */

  // Are payload's condensed bounds covariantly assignable to receiver's condensed bounds?
  /*
  private final boolean condensedTypeVariableBoundsAssignableToCondensedTypeVariableBounds(final TypeVariable receiver, final TypeVariable payload) {
    assert receiver.getKind() == TypeKind.TYPEVAR;
    assert payload.getKind() == TypeKind.TYPEVAR;
    // No need to condense arguments to eliminate useless type variables and intersection types so that Java covariant
    // semantics will work properly in this case; #covariantlyAssignable(List, List) does this already.
    return this.covariantlyAssignable(List.of(receiver), List.of(payload));
  }
  */


  /*
   * Static methods.
   */

  
  // Returns a new IllegalArgumentException describing an illegal payload type.
  private static IllegalArgumentException illegalPayload(final TypeMirror payload) {
    return new IllegalArgumentException("Illegal payload kind: " + payload.getKind() + "; payload: " + payload);
  }

  // Returns a new IllegalArgumentException describing an illegal receiver type.
  private static IllegalArgumentException illegalReceiver(final TypeMirror receiver) {
    return new IllegalArgumentException("Illegal receiver kind: " + receiver.getKind() + "; receiver: " + receiver);
  }

  
}
