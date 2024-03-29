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

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import java.util.function.Predicate;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Parameterizable;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import org.microbean.lang.Lang;
import org.microbean.lang.TypeAndElementSource;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;

import static org.microbean.bean.ConstantDescs.CD_Assignability;

import static org.microbean.lang.ConstantDescs.CD_TypeAndElementSource;

/**
 * A class encapsulating CDI-compatible assignability ("matching") rules as applied to {@link TypeMirror}s.
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 *
 * @see #matches(TypeMirror, TypeMirror)
 */
// Applies CDI assignability semantics to types.
public final class Assignability implements Constable {


  /*
   * Instance fields.
   */


  private final TypeAndElementSource tes;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link Assignability}.
   *
   * @see #Assignability(TypeAndElementSource)
   */
  public Assignability() {
    super();
    this.tes = Lang.typeAndElementSource();
  }

  /**
   * Creates a new {@link Assignability}.
   *
   * @param tes a {@link TypeAndElementSource}; may be {@code null} in which case the return value of an invocation of
   * {@link Lang#typeAndElementSource()} will be used instead
   *
   * @see TypeAndElementSource
   *
   * @see Lang#typeAndElementSource()
   */
  public Assignability(final TypeAndElementSource tes) {
    super();
    this.tes = tes == null ? Lang.typeAndElementSource() : tes;
  }


  /*
   * Instance methods.
   */


  @Override // Constable
  public final Optional<? extends ConstantDesc> describeConstable() {
    if (this.tes instanceof Constable c) {
      return c.describeConstable()
        .map(tesDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                               MethodHandleDesc.ofConstructor(CD_Assignability,
                                                                              CD_TypeAndElementSource),
                                               tesDesc));
    }
    return Optional.empty();
  }

  /**
   * Returns {@code true} if and only if at least one {@link TypeMirror} present in the supplied {@code payloads}
   * argument {@linkplain #matches(TypeMirror, TypeMirror) matches} the supplied {@code receiver} argument.
   *
   * @param receiver the left hand side of a type assignment; must not be {@code null}
   *
   * @param payloads an {@link Iterable} of {@link TypeMirror}s each representing a potential right hand side of a type
   * assignment; must not be {@code null}
   *
   * @return {@code true} if and only if at least one {@link TypeMirror} present in the supplied {@code payloads}
   * argument {@linkplain #matches(TypeMirror, TypeMirror) matches} the supplied {@code receiver} argument; {@code
   * false} otherwise
   *
   * @exception NullPointerException if either argument is {@code null}
   *
   * @see #matches(TypeMirror, TypeMirror)
   */
  // Is at least one payload assignable to the receiver? That is, does at least one payload "match the receiver" in CDI
  // parlance?
  public final boolean oneMatches(final TypeMirror receiver, final Iterable<? extends TypeMirror> payloads) {
    for (final TypeMirror payload : payloads) {
      if (matches(receiver, payload)) {
        return true;
      }
    }
    return false;
  }

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
   */
  // Is the payload assignable to the receiver? That is, does the payload "match the receiver", in CDI parlance?
  public final boolean matches(final TypeMirror receiver, final TypeMirror payload) {
    // "A bean is assignable to a given injection point if:
    //
    // "The bean has a bean type [payload] that matches the required type [receiver]. For this purpose..."
    return receiver == Objects.requireNonNull(payload, "payload") || switch (receiver.getKind()) {
      // "...primitive types are considered to match their corresponding wrapper types in java.lang..."
      case BOOLEAN -> payload.getKind() == TypeKind.BOOLEAN || declaredTypeNamed(payload, "java.lang.Boolean");
      case BYTE    -> payload.getKind() == TypeKind.BYTE    || declaredTypeNamed(payload, "java.lang.Byte");
      case CHAR    -> payload.getKind() == TypeKind.CHAR    || declaredTypeNamed(payload, "java.lang.Character");
      case DOUBLE  -> payload.getKind() == TypeKind.DOUBLE  || declaredTypeNamed(payload, "java.lang.Double");
      case FLOAT   -> payload.getKind() == TypeKind.FLOAT   || declaredTypeNamed(payload, "java.lang.Float");
      case INT     -> payload.getKind() == TypeKind.INT     || declaredTypeNamed(payload, "java.lang.Integer");
      case LONG    -> payload.getKind() == TypeKind.LONG    || declaredTypeNamed(payload, "java.lang.Long");
      case SHORT   -> payload.getKind() == TypeKind.SHORT   || declaredTypeNamed(payload, "java.lang.Short");
      // "...and array types are considered to match only if their element types [note: not component types] are
      // identical [undefined]..."
      case ARRAY -> payload.getKind() == TypeKind.ARRAY && identical(elementType(receiver), elementType(payload));
      case DECLARED -> switch (payload.getKind()) {
        // "...primitive types are considered to match their corresponding wrapper types in java.lang..."
        case BOOLEAN -> named((DeclaredType)receiver, "java.lang.Boolean");
        case BYTE    -> named((DeclaredType)receiver, "java.lang.Byte");
        case CHAR    -> named((DeclaredType)receiver, "java.lang.Character");
        case DOUBLE  -> named((DeclaredType)receiver, "java.lang.Double");
        case FLOAT   -> named((DeclaredType)receiver, "java.lang.Float");
        case INT     -> named((DeclaredType)receiver, "java.lang.Integer");
        case LONG    -> named((DeclaredType)receiver, "java.lang.Long");
        case SHORT   -> named((DeclaredType)receiver, "java.lang.Short");
        // "Parameterized and raw types [and non-generic classes, and non-array types]..."
        case DECLARED ->
          // "...are considered to match if they are identical [undefined]..."
          identical(receiver, payload) ||
          // "...or if the bean type [payload] is assignable [see #assignable(TypeMirror, TypeMirror)] to the required
          // type [receiver]...."
          assignable((DeclaredType)receiver, (DeclaredType)payload);
        default -> throw new IllegalArgumentException("Illegal payload kind: " + payload.getKind() + "; payload: " + payload);
      };
      default -> throw new IllegalArgumentException("Illegal receiver kind: " + receiver.getKind() + "; receiver: " + receiver);
    };
  }

  /**
   * Returns the {@link TypeAndElementSource} used by this {@link Assignability}.
   *
   * @return the {@link TypeAndElementSource} used by this {@link Assignability}; never {@code null}
   *
   * @see #Assignability(TypeAndElementSource)
   *
   * @see TypeAndElementSource
   */
  public final TypeAndElementSource typeAndElementSource() {
    return this.tes;
  }

  /*
   * Private methods.
   */

  private final boolean assignable(final TypeMirror receiver, final TypeMirror payload) {
    return switch (payload) {
      // "A parameterized bean type [payload] is considered assignable..."
      case DeclaredType parameterizedPayload when parameterized(payload) -> switch (receiver) {
        // "...to a [non-generic class or] raw required type [receiver]..."
        case TypeMirror rawReceiver when !generic(receiver) || raw(receiver) -> {
          // "...if the [non-generic class or] raw types are identical [undefined] and all type parameters [arguments]
          // of the bean type [payload] are either unbounded type variables [undefined] or java.lang.Object."
          yield
            identical(nonGenericClassOrRawType(rawReceiver), nonGenericClassOrRawType(parameterizedPayload)) &&
            allTypeArgumentsAre(parameterizedPayload.getTypeArguments(),
                                ((Predicate<TypeMirror>)Assignability::unboundedTypeVariable).or(Assignability::isJavaLangObject));
        }
        // "...to a parameterized required type [receiver]..."
        case DeclaredType parameterizedReceiver when parameterized(receiver) -> {
          // "...if they have identical raw type [really if their declarations/elements are identical]..."
          if (identical(rawType(receiver), rawType(parameterizedPayload))) {
            // "...and for each parameter [type argument] [pair]..."
            final List<? extends TypeMirror> rtas = parameterizedReceiver.getTypeArguments();
            final List<? extends TypeMirror> ptas = parameterizedPayload.getTypeArguments();
            assert rtas.size() == ptas.size();
            for (int i = 0; i < rtas.size(); i++) {
              final TypeMirror rta = rtas.get(i);
              final TypeMirror pta = ptas.get(i);
              // "...the required [receiver] type parameter [argument] and the bean [payload] type parameter [argument]
              // are actual [non-type variable, non-wildcard reference] types..."
              if (actual(rta)) {
                if (actual(pta)) {
                  // "...with identical [non-generic classes or] raw type[s]..."
                  if (identical(nonGenericClassOrRawType(rta), nonGenericClassOrRawType(pta))) {
                    // "...and, if the type [?] is parameterized [?]..."
                    //
                    // Let rta and pta be array types with parameterized element types, such as List<Number>[] and
                    // List<String>[]. Then their raw types are List[] and List[]. Their parameterized element types are
                    // List<Number> and List<String>. According to the JLS, neither List<Number>[] nor List<String>[] is
                    // parameterized.
                    //
                    // In this example, if we get here, we have only proven that List[] is "identical" to List[].
                    //
                    // The "if the type is parameterized" clause is tough. Which type is under discussion? "the type"
                    // seems to refer to the "identical raw type". But a raw type by definition is not parameterized, so
                    // this clause would seem to be superfluous and thus never apply, and so List<Number>[] :=
                    // List<String>[] is OK. Oops.
                    //
                    // Or what if the "if the type is parameterized" clause means the receiver type itself and somehow
                    // loosely the payload type as well? Well, clearly it cannot correctly do this, since an array type
                    // is never a parameterized type. Same bad result. Oops.
                    //
                    // Or what if "identical raw type" really means "identical raw type (or identical component type if
                    // they are array types)"? That would be most efficient, since it would rule out List<Number>[] :=
                    // List<String>[] right off the bat: we wouldn't even get here. But that really doesn't seem to be
                    // what is meant.
                    //
                    // What if the "if the type is parameterized" clause really means "if the type declaration used by
                    // both the receiver and payload types is generic"? That would work. That's also equivalent to
                    // something like: "...if at least one of the two arguments is a parameterized type [e.g. List<Number>,
                    // not List<Number>[], not String, not List], or at least one of the two types is an array type with
                    // a parameterized element type [e.g. List<Number>[], not List[], not String[]]..."
                    //
                    // That is the interpretation we apply here. So:
                    //
                    //   "...and, if the type [?] is parameterized..."
                    //
                    // becomes:
                    //
                    //   "...and, if at least one of the two type arguments is a parameterized type, or if at least one
                    //   of the two types is an array type with a parameterized element type..."
                    //
                    // That, in turn, designates any type capable of properly yielding a raw type, while ruling out
                    // those that can't! That means it is exactly equal to our yieldsRawType(TypeMirror) method, and so
                    // that's what cdiParameterized(TypeMirror) returns.
                    if (cdiParameterized(rta)) {
                      assert cdiParameterized(pta); // ...because otherwise their raw types would not have been "identical"
                      // "...the bean type parameter [argument] is assignable to the required type parameter [argument]
                      // according to [all of] these rules [including 'matching']..."
                      if (matches(rta, pta)) {
                        continue;
                      }
                      yield false;
                    } else {
                      assert !cdiParameterized(pta);
                      continue;
                    }
                  }
                  yield false;
                } else if (pta.getKind() == TypeKind.TYPEVAR) {
                  // "...the required [receiver] type parameter [argument] is an actual [non-type variable, non-wildcard
                  // reference] type, the bean [payload] type parameter [argument] is a type variable and the actual
                  // [non-type variable, non-wildcard reference] type [required type argument, receiver] is assignable
                  // to the upper bound, if any, of the type variable [bean type argument, payload] [type variables have
                  // multiple bounds]..."
                  if (assignableToCondensedTypeVariableBounds((TypeVariable)pta, (ReferenceType)receiver)) {
                    continue;
                  }
                  yield false;
                } else {
                  throw new AssertionError("Unexpected payload type argument kind: " + pta.getKind());
                }
              } else if (rta.getKind() == TypeKind.WILDCARD) {
                // "...the required type parameter [argument] is a wildcard, the bean type parameter [argument] is an
                // actual [non-type variable, non-wildcard reference] type..."
                if (actual(pta)) {
                  // "...and the actual [non-type variable, non-wildcard reference] type is assignable to the upper
                  // bound, if any, of the wildcard and assignable from the lower bound, if any of the wildcard"
                  if (assignableToExtendsBound((WildcardType)rta, (ReferenceType)pta) &&
                      assignableFromSuperBound((ReferenceType)pta, (WildcardType)rta)) {
                    continue;
                  }
                  yield false;
                } else if (pta.getKind() == TypeKind.TYPEVAR) {
                  // "...the required [receiver] type parameter [argument] is a wildcard, the bean [payload] type
                  // parameter [argument] is a type variable and the upper bound of the type variable [a type variable
                  // has many bounds?] is assignable to or assignable from the upper bound, if any, of the wildcard and
                  // assignable from the lower bound, if any, of the wildcard"
                  if ((condensedTypeVariableBoundsAssignableToExtendsBound((WildcardType)rta, (TypeVariable)pta) ||
                       condensedTypeVariableBoundsAssignableFromExtendsBound((TypeVariable)pta, (WildcardType)rta)) &&
                      condensedTypeVariableBoundsAssignableFromSuperBound((TypeVariable)pta, (WildcardType)rta)) {
                    continue;
                  }
                  yield false;
                } else {
                  throw new AssertionError("Unexpected payload type argument kind: " + pta.getKind());
                }
              } else if (rta.getKind() == TypeKind.TYPEVAR) {
                if (pta.getKind() == TypeKind.TYPEVAR) {
                  // "...the required type parameter [argument] and the bean type parameter [argument] are both type
                  // variables and the upper bound of the required type parameter [argument] [a type variable has many
                  // bounds?] is assignable to the upper bound [a type variable has many bounds?], if any, of the bean
                  // type parameter [argument]"
                  if (condensedTypeVariableBoundsAssignableToCondensedTypeVariableBounds((TypeVariable)pta, (TypeVariable)rta)) {
                    continue;
                  }
                  yield false;
                } else {
                  throw new AssertionError("Unexpected payload type argument kind: " + pta.getKind());
                }
              } else {
                throw new AssertionError("Unexpected receiver type argument kind: " + rta.getKind());
              }
            }
            yield true; // we passed all the tests
          }
          yield false; // non-generic classes or raw types were not identical
        }
        default -> throw new AssertionError("Unexpected receiver kind: " + receiver.getKind());
      };
      // "A [non-generic or] raw bean type [payload] is considered assignable..."
      case TypeMirror nonGenericOrRawPayload -> switch (receiver) {
        // "...to a parameterized required type [receiver]..."
        case DeclaredType parameterizedReceiver when parameterized(receiver) -> {
          // "...if the[ir] [non-generic classes or] raw types are identical and all type parameters [arguments] of the
          // required type [receiver] are either unbounded type variables or java.lang.Object."
          yield
            identical(nonGenericClassOrRawType(parameterizedReceiver), nonGenericOrRawPayload) &&
            allTypeArgumentsAre(parameterizedReceiver.getTypeArguments(),
                                ((Predicate<TypeMirror>)Assignability::unboundedTypeVariable).or(Assignability::isJavaLangObject));
        }
        // [Otherwise the payload is not assignable to the receiver; identity checking should have already happened in
        // matches(), not here.]
        case DeclaredType nonGenericOrRawReceiver when receiver.getKind() == TypeKind.DECLARED -> {
          yield false; // or yield identical(nonGenericOrRawReceiver, nonGenericOrRawPayload);
        }
        default -> throw new AssertionError("Unexpected payload kind: " + payload.getKind() + "; receiver: " + receiver + "; payload: " + payload);
      };
    };
  }

  // Is payload "identical to" receiver, following the intent of CDI?
  private final boolean identical(final TypeMirror receiver, final TypeMirror payload) {
    // CDI has an undefined notion of "identical to". This method attempts to divine and implement the intent. Recall
    // that javax.lang.model.* compares types with "sameType" semantics.
    return receiver == payload || this.tes.sameType(receiver, payload);
  }

  // Is the "actual" type argument represented by payload assignable to the receiver's condensed bounds?
  //
  // @Inject Foo<String> foo; <-- Bean<T extends CharSequence> // is the "actual" String type (payload) assignable to T's CharSequence bound (receiver)?
  //
  // Recall that after condensing "T extends CharSequence" you get CharSequence.
  //
  // Recall that if, instead, you had T extends S and S extends CharSequence, condensing T still yields CharSequence.
  private final boolean assignableToCondensedTypeVariableBounds(final TypeVariable receiver, final ReferenceType payload) {
    assert receiver.getKind() == TypeKind.TYPEVAR;
    assert actual(payload);
    return covariantlyAssignable(List.of(receiver), List.of(payload)); // deliberately List.of(payload) and not condense(payload)
  }

  private final boolean assignableToExtendsBound(final WildcardType w, final ReferenceType candidate) {
    assert w.getKind() == TypeKind.WILDCARD;
    assert actual(candidate);
    final TypeMirror extendsBound = w.getExtendsBound();
    return extendsBound == null || covariantlyAssignable((ReferenceType)extendsBound, candidate);
  }

  // It's not immediately clear what CDI means by a type variable's upper bound. In javax.lang.model.type parlance,
  // the upper bound of a TypeVariable could be an IntersectionType, which java.lang.reflect.TypeVariable represents
  // as a collection of bounds. CDI blundered into this earlier: https://issues.redhat.com/browse/CDI-440 and
  // https://github.com/jakartaee/cdi/issues/682
  //
  // "the upper bound of the required type parameter [receiver argument] is assignable to the upper bound, if any, of the bean
  // type parameter [argument]" (when both arguments are type variables) should read:
  //
  // "for each bound, PA, of the bean type parameter [payload argument], there is at least one bound, RA, of the
  // required type parameter [receiver argument], which is assignable to PA."
  //
  // The TCK enforces this, even though it's not in the specification (!).
  //
  // Weld's implementation confuses type parameters with arguments, just like the specification. They have a series of
  // methods implemented as part of PR 614 (https://github.com/weld/core/pull/614) named "parametersMatch" [arguments
  // match].
  //
  // Weld also has methods named things like "getUppermostTypeVariableBounds" and "getUppermostBounds". I think this is
  // Weld's excruciatingly clumsy way of trying to express the JLS notion of "leftmost bound" (which is also
  // undefined). See
  // https://www.oreilly.com/library/view/learning-java-4th/9781449372477/ch08s07.html#learnjava3-CHP-8-SECT-7.1.
  //
  // Digging deeper, I think getUppermostTypeVariableBounds(tv) is just erase(tv) applied recursively, maybe? (No, it
  // turns out; read on.)
  //
  // https://github.com/openjdk/jdk/blob/181845ae46157a9bb3bf8e2a328fa59eddc0273a/src/jdk.compiler/share/classes/com/sun/tools/javac/code/Types.java#L2450
  //
  // Compare vs.:
  // https://github.com/weld/core/blob/e894d1699ff1c91332605f5ecae5f53410effb81/impl/src/main/java/org/jboss/weld/resolution/AbstractAssignabilityRules.java#L57-L62
  //
  // So maybe not quite. getUppermostTypeVariableBounds() just gets to "actual" types. So given T extends S and S extends
  // String, T's "uppermost type variable bounds" are String.

  // For every bound in (condensed) receiverBounds, is there a bound in (condensed) payloadBounds that is covariantly
  // assignable to it?
  //
  // (Is there one bound in (condensed) payloadBounds that matches all bounds in (condensed) receiver bounds?
  //
  // Throws ClassCastException if, after condensing, any encountered bound is not either an ArrayType or a DeclaredType.
  private final boolean covariantlyAssignable(final List<? extends TypeMirror> receiverBounds, List<? extends TypeMirror> payloadBounds) {
    payloadBounds = condense(payloadBounds);
    for (final TypeMirror condensedReceiverBound : condense(receiverBounds)) { // eliminate type variables via condense() call
      if (!covariantlyAssignable((ReferenceType)condensedReceiverBound, payloadBounds)) {
        return false;
      }
    }
    return true;
  }

  // Is there a DeclaredType-or-ArrayType bound in condensedPayloadBounds that is assignable to classOrArrayTypeReceiver
  // using Java, not CDI, assignability semantics?
  //
  // It is assumed condensedPayloadBounds is the result of a condense() call.
  //
  // Throws ClassCastException or IllegalArgumentException if any encountered type is not either an ArrayType or a
  // DeclaredType.
  private final boolean covariantlyAssignable(final ReferenceType classOrArrayTypeReceiver, final List<? extends TypeMirror> condensedPayloadBounds) {
    return switch (classOrArrayTypeReceiver.getKind()) {
    case ARRAY, DECLARED -> {
      for (final TypeMirror condensedPayloadBound : condensedPayloadBounds) {
        if (covariantlyAssignable(classOrArrayTypeReceiver, (ReferenceType)condensedPayloadBound)) {
          yield true;
        }
      }
      yield false;
    }
    default -> throw new IllegalArgumentException("classOrArrayTypeReceiver: " + classOrArrayTypeReceiver + "; kind: " + classOrArrayTypeReceiver.getKind());
    };
  }

  // Is classOrArrayTypePayload assignable to classOrArrayTypeReceiver following the rules of Java assignability
  // (i.e. covariance)?
  //
  // The types are ReferenceTypes because this is only ever invoked in the context of type arguments.
  private final boolean covariantlyAssignable(final ReferenceType classOrArrayTypeReceiver, final ReferenceType classOrArrayTypePayload) {
    assert actual(classOrArrayTypeReceiver);
    assert actual(classOrArrayTypePayload);
    return
      classOrArrayTypeReceiver == classOrArrayTypePayload || // Optimization
      // Note that TypeAndElementSource#assignable(TypeMirror, TypeMirror) follows the lead of
      // javax.lang.model.util.Types#isAssignable(TypeMirror, TypeMirror) where the "payload" is the *first* parameter
      // and the "receiver" is the *second* argument.
      this.tes.assignable(classOrArrayTypePayload, classOrArrayTypeReceiver); // yes, "backwards"
  }

  // Are payload's condensed bounds assignable to receiver's condensed extends bound (upper bound)?
  private final boolean condensedTypeVariableBoundsAssignableToExtendsBound(final WildcardType receiver, final TypeVariable payload) {
    assert receiver.getKind() == TypeKind.WILDCARD;
    assert payload.getKind() == TypeKind.TYPEVAR;
    // "...the upper bound of the type variable [a type variable has many bounds?] is assignable TO [...] the upper
    // bound, if any, of the wildcard..."
    final TypeMirror extendsBound = receiver.getExtendsBound();
    // Condense arguments to eliminate useless type variables and intersection types so that Java covariant semantics
    // will work properly in this case.
    return extendsBound == null || covariantlyAssignable(List.of((ReferenceType)extendsBound), List.of(payload)); // No need to condense; #covariantlyAssignable(List, List) does this already
  }

  // Is payload's condensed extends bound (upper bound) covariantly assignable to receiver's condensed bounds?
  private final boolean condensedTypeVariableBoundsAssignableFromExtendsBound(final TypeVariable receiver, final WildcardType payload) {
    assert receiver.getKind() == TypeKind.TYPEVAR;
    assert payload.getKind() == TypeKind.WILDCARD;
    // "...the upper bound of the type variable [a type variable has many bounds?] is assignable [...] FROM the upper
    // bound, if any, of the wildcard..."
    final TypeMirror extendsBound = payload.getExtendsBound();
    return extendsBound == null || covariantlyAssignable(List.of(receiver), List.of((ReferenceType)extendsBound));
  }

  // Is payload's super bound (lower bound) covariantly assignable to receiver's condensed bounds?
  private final boolean condensedTypeVariableBoundsAssignableFromSuperBound(final TypeVariable receiver, final WildcardType payload) {
    assert receiver.getKind() == TypeKind.TYPEVAR;
    assert payload.getKind() == TypeKind.WILDCARD;
    final TypeMirror superBound = payload.getSuperBound();
    return superBound == null || covariantlyAssignable(List.of(receiver), List.of(superBound));
  }

  // Are payload's condensed bounds covariantly assignable to receiver's condensed bounds?
  private final boolean condensedTypeVariableBoundsAssignableToCondensedTypeVariableBounds(final TypeVariable receiver, final TypeVariable payload) {
    assert receiver.getKind() == TypeKind.TYPEVAR;
    assert payload.getKind() == TypeKind.TYPEVAR;
    return covariantlyAssignable(List.of(receiver), List.of(payload));
  }

  // Is payload's super bound (lower bound) covariantly assignable to receiver?
  private final boolean assignableFromSuperBound(final ReferenceType receiver, final WildcardType payload) {
    assert payload.getKind() == TypeKind.WILDCARD;
    assert actual(receiver);
    final ReferenceType superBound = (ReferenceType)payload.getSuperBound();
    return superBound == null || covariantlyAssignable(receiver, superBound);
  }

  // Get the raw type yielded by t, assuming t is the sort of type that can yield a raw type.
  //
  // An array type with a parameterized element type can yield a raw type.
  //
  // A declared type that is parameterized can yield a raw type.
  //
  // No other type yields a raw type.
  private final TypeMirror rawType(final TypeMirror t) {
    return switch (t.getKind()) {
    case ARRAY -> {
      final TypeMirror et = elementType(t);
      if (!parameterized(et)) {
        throw new IllegalArgumentException("t is an array type whose element type is not parameterized so cannot yield a raw type");
      }
      yield arrayType(erasure(et));
    }
    case DECLARED -> {
      if (!parameterized(t)) {
        throw new IllegalArgumentException("t is a declared type that is not parameterized so cannot yield a raw type");
      }
      yield erasure(t);
    }
    default -> throw new IllegalArgumentException("t is a " + t.getKind() + " type and so cannot yield a raw type");
    };
  }

  // Return t if its element declares a non-generic class, or if it is the raw type usage of a generic class.
  private final TypeMirror nonGenericClassOrRawType(final TypeMirror t) {
    return yieldsRawType(t) ? rawType(t) : t;
  }

  // Erase t and return its erasure. Erasing is a complex business that can involve the creation of new types.
  private final TypeMirror erasure(final TypeMirror t) {
    return this.tes.erasure(t);
  }

  // Return a possibly new ArrayType whose component type is the supplied componentType.
  private final ArrayType arrayType(final TypeMirror componentType) {
    return this.tes.arrayTypeOf(componentType);
  }


  /*
   * Static methods.
   */


  // If t is null, returns an empty List.
  //
  // If t is a type variable, returns the result of condensing its upper bound.
  //
  // If t is an intersection type, returns the result of condensing its bounds.
  //
  // In all other cases returns List.of(t).
  //
  // See #condense(List) below.
  private static final List<? extends TypeMirror> condense(final TypeMirror t) {
    return t == null ? List.of() : switch (t.getKind()) {
    case INTERSECTION -> condense(((IntersectionType)t).getBounds());
    case TYPEVAR -> condense(((TypeVariable)t).getUpperBound());
    default -> List.of(t); // t's bounds are defined solely by itself, or it's a wildcard and we didn't say which bounds
    };
  }

  // If ts is null or empty, returns an empty List.
  //
  // If ts consists of a single element, returns the result of condensing t (see #condense(TypeMirror) above).
  //
  // If ts's first element (of several) is an intersection type i, returns the result of condensing the list formed by
  // effectively replacing ts's first element with i's bounds.
  //
  // If ts's first element (of several) is a type variable tv, returns the result of condensing the list formed by
  // effectively replacing ts's first element with tv's upper bound.
  //
  // In all other cases, returns ts.
  private static final List<? extends TypeMirror> condense(final List<? extends TypeMirror> ts) {
    final int size = ts == null ? 0 : ts.size();
    if (size <= 0) {
      return List.of();
    }
    final TypeMirror ts0 = ts.get(0);
    if (size == 1) {
      return condense(ts0);
    }
    return switch (ts0.getKind()) {
    case INTERSECTION -> {
      final ArrayList<TypeMirror> newBounds = new ArrayList<>();
      newBounds.addAll(((IntersectionType)ts0).getBounds()); // first elements are now ts0's bounds, not ts0
      newBounds.addAll(ts.subList(1, size)); // drop ts0; add whatever was left
      newBounds.trimToSize();
      yield condense(newBounds);
    }
    case TYPEVAR -> {
      final ArrayList<TypeMirror> newBounds = new ArrayList<>();
      newBounds.add(((TypeVariable)ts0).getUpperBound()); // first element is now ts0's upper bound, not ts0
      newBounds.addAll(ts.subList(1, size)); // drop ts0; add whatever was left
      newBounds.trimToSize();
      yield condense(newBounds);
    }
    default -> ts;
    };
  }

  // Does e represent a generic declaration?
  //
  // A declaration is generic if it declares one or more type parameters.
  //
  // Since an annotation interface cannot declare type parameters, it follows that TypeElements representing annotation
  // instances are never generic.
  private static final boolean generic(final Element e) {
    return switch (e.getKind()) {
    case CLASS, CONSTRUCTOR, ENUM, INTERFACE, METHOD, RECORD -> !((Parameterizable)e).getTypeParameters().isEmpty();
    default -> false;
    };
  }

  // Is t the usage of a generic class, i.e. a usage (whether raw or parameterized) of a generic class declaration?
  //
  // t is deemed to be generic if it is a declared type whose defining element (its type declaration) is generic.
  //
  // Array types are never generic.
  private static final boolean generic(final TypeMirror t) {
    return t.getKind() == TypeKind.DECLARED && generic(((DeclaredType)t).asElement());
  }

  // Is t a parameterized type (and not a raw type) according to the rules of the Java Language Specification?
  //
  // A type is parameterized if it is a declared type with a non-empty list of type arguments. No other type is
  // parameterized.
  //
  // "parameterized" means "supplies arguments for type parameters"
  private static final boolean parameterized(final TypeMirror t) {
    return t.getKind() == TypeKind.DECLARED && !((DeclaredType)t).getTypeArguments().isEmpty();
  }

  // Is t a type that CDI considers to be "parameterized"?
  //
  // There are some cases, but not all, where CDI (incorrectly) considers an array type to be something that can be
  // parameterized, or else "bean type parameter" resolution would never work. See
  // https://stackoverflow.com/questions/76493672/when-cdi-speaks-of-a-parameterized-type-does-it-also-incorrectly-mean-array-typ.
  //
  // The semantics CDI wants to express are really: can t yield a raw type? See #yieldsRawType(TypeMirror) below.
  private static final boolean cdiParameterized(final TypeMirror t) {
    return yieldsRawType(t);
  }

  // Can t yield a raw type?
  //
  // To yield a raw type, t must be either:
  //
  // * an array type with a parameterized element type
  // * a declared type with at least one type argument
  private static final boolean yieldsRawType(final TypeMirror t) {
    return parameterized(t) || t.getKind() == TypeKind.ARRAY && parameterized(elementType(t));
  }

  // Is t a raw type, following the rules of the Java Language Specification, not CDI?
  //
  // A raw type is either "the erasure of a parameterized type" (so List<String>'s raw type is List, clearly not
  // List<?>, and not List<E>) or "an array type whose element type is a raw type" (so List<String>[]'s raw type is
  // List[]). (String is not a raw type; its element defines no type parameters.)
  //
  // No other type is a raw type.
  //
  // CDI gets confused and uses "raw" in different senses. The sense used here is that of the Java Language
  // Specification.
  private static final boolean raw(final TypeMirror t) {
    return switch (t.getKind()) {
    case ARRAY -> raw(elementType((ArrayType)t));
    case DECLARED -> generic(t) && ((DeclaredType)t).getTypeArguments().isEmpty();
    case TYPEVAR -> false; // called out explicitly just so you realize it
    default -> false;
    };
  }

  // Is t a declared type that bears the supplied fully qualified name?
  private static final boolean declaredTypeNamed(final TypeMirror t, final CharSequence n) {
    return t.getKind() == TypeKind.DECLARED && named(((DeclaredType)t), n);
  }

  // Regardless of its reported TypeKind, does t's declaring TypeElement bear the supplied fully qualified name?
  //
  // Throws ClassCastException if the return value of t.asElement() is not a TypeElement.
  private static final boolean named(final DeclaredType t, final CharSequence n) {
    // (No getKind() check on purpose.)
    return ((TypeElement)t.asElement()).getQualifiedName().contentEquals(n);
  }

  // Do all supplied TypeMirrors pass the test represented by the supplied Predicate?
  private static final boolean allTypeArgumentsAre(final Iterable<? extends TypeMirror> typeArguments, final Predicate<? super TypeMirror> p) {
    for (final TypeMirror t : typeArguments) {
      if (!p.test(t)) {
        return false;
      }
    }
    return true;
  }

  // Is e a TypeElement representing java.lang.Object?
  private static final boolean isJavaLangObject(final Element e) {
    return
      e.getKind() == ElementKind.CLASS &&
      ((QualifiedNameable)e).getQualifiedName().contentEquals("java.lang.Object");
  }

  // Is t a DeclaredType whose asElement() method returns an Element representing java.lang.Object?
  private static final boolean isJavaLangObject(final TypeMirror t) {
    return
      t.getKind() == TypeKind.DECLARED &&
      isJavaLangObject(((DeclaredType)t).asElement());
  }

  // Is t an "actual type"?
  //
  // CDI mentions actual types but does not define what they are. This method attempts to divine and implement the
  // intent.
  //
  // A comment in a closed bug report (CDI-502)
  // (https://issues.redhat.com/browse/CDI-502?focusedId=13036118&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-13036118)
  // by one of the reference implementation's authors (Jozef Hartinger) provides the only definition:
  //
  // "An actual type is a type that is not a wildcard nor [sic] an unresolved [sic] type variable."
  //
  // The Java Language Specification does not mention anything about "actual types" or type variable resolution. CDI
  // does not mention anything about type variable resolution.
  //
  // More strictly, therefore, the intent seems to be that an actual type is an array, declared or primitive type, and
  // none other.
  private static final boolean actual(final TypeMirror t) {
    return switch (t.getKind()) {
    case ARRAY, BOOLEAN, BYTE, CHAR, DECLARED, DOUBLE, FLOAT, INT, LONG, SHORT -> true;
    default -> false;
    };
  }

  // Is t an unbounded type variable?
  //
  // CDI does not define what an "unbounded type variable" is. This method attempts to divine and implement the intent.
  //
  // TODO: do we need to condense? Given T extends S and S extends Object, is T an unbounded TypeVariable or not?
  private static final boolean unboundedTypeVariable(final TypeMirror t) {
    return
      t.getKind() == TypeKind.TYPEVAR &&
      isJavaLangObject(((TypeVariable)t).getUpperBound());
  }

  // Returns the element type of t.
  //
  // The element type of an array type is the element type of its component type.
  //
  // The element type of every other kind of type is the type itself.
  private static final TypeMirror elementType(final TypeMirror t) {
    return t.getKind() == TypeKind.ARRAY ? elementType(((ArrayType)t).getComponentType()) : t;
  }

}
