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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

import org.microbean.construct.Domain;

import org.microbean.construct.type.UniversalType;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;

public abstract class AbstractTypeMatcher implements Constable {


  private static final ClassDesc CD_Domain = ClassDesc.of("org.microbean.construct.Domain");
  

  /*
   * Instance fields.
   */


  private final Domain domain;


  /*
   * Constructors.
   */


  protected AbstractTypeMatcher(final Domain domain) {
    super();
    this.domain = domain;
  }


  /*
   * Instance methods.
   */


  // Is payload's condensed super bound (lower bound) covariantly assignable to receiver?
  //
  // Since either of a wildcard type's bounds may be a type variable, and since a type variable's bound may be an
  // intersection type, it follows that after condensation either of a wildcard type's bounds may be a list of actual
  // types.
  //
  // Therefore this is really: Is there one bound among payload's actual super bounds that is assignable to receiver?
  /*
  protected boolean assignableFromCondensedSuperBound(final ReferenceType receiver, final WildcardType payload) {
    assert payload.getKind() == TypeKind.WILDCARD;
    final ReferenceType superBound = (ReferenceType)payload.getSuperBound();
    return superBound == null || switch (superBound.getKind()) {
    case ARRAY, DECLARED, TYPEVAR -> covariantlyAssignable(receiver, superBound);
    // case TYPEVAR -> covariantlyAssignable(receiver, (ReferenceType)this.types.condense(superBound));
    default -> throw new AssertionError();
    };
  }
  */

  // Is candidate covariantly assignable to w's condensed extends (upper) bound?
  /*
  protected boolean assignableToCondensedExtendsBound(final WildcardType w, final ReferenceType candidate) {
    assert w.getKind() == TypeKind.WILDCARD;
    final ReferenceType extendsBound = (ReferenceType)w.getExtendsBound();
    return extendsBound == null || switch (extendsBound.getKind()) {
    case ARRAY, DECLARED, TYPEVAR -> covariantlyAssignable(extendsBound, candidate);
    // case TYPEVAR -> covariantlyAssignable((ReferenceType)this.types.condense(extendsBound), List.of(candidate));
    default -> throw new AssertionError();
    };
  }
  */

  // Is the type argument represented by payload assignable to all of the receiver's condensed bounds?
  //
  // Pay close attention to how this is called, i.e. what is payload and what is receiver is often "backwards". For
  // example:
  //
  //   @Inject Foo<String> foo; <-- Bean<T extends CharSequence> // is the String "actual type" (payload) assignable to T's (receiver's) CharSequence bound?
  //   //          ^ payload!            ^ receiver!
  //
  // Recall that after condensing "T extends CharSequence" you get CharSequence.
  //
  // Recall that if, instead, you had T extends S and S extends CharSequence, condensing T still yields CharSequence.
  /*
  protected boolean assignableToCondensedTypeVariableBounds(final TypeVariable receiver, final TypeMirror payload) {
    assert receiver.getKind() == TypeKind.TYPEVAR;
    return this.covariantlyAssignable(receiver, payload);
    // return covariantlyAssignable(List.of(receiver), List.of(payload)); // deliberately List.of(payload) and not condense(payload)
  }
  */

  protected boolean contains(final TypeMirror t, final TypeMirror s) {
    return this.domain.contains(t, s);
  }

  // It's not immediately clear what CDI means by a type variable's upper bound. In javax.lang.model.type parlance, the
  // upper bound of a TypeVariable could be an IntersectionType, which java.lang.reflect.TypeVariable represents as a
  // collection of bounds. CDI blundered into this earlier: https://issues.redhat.com/browse/CDI-440 and
  // https://github.com/jakartaee/cdi/issues/682
  //
  // In what follows, note the strangeness as well of the reversal of assignment semantics. The *receiver* gets checked
  // to see if it is assignable *to* the *payload*. This method itself is not responsible for those semantics.
  //
  // "the upper bound of the required type parameter [receiver type argument] is assignable to the upper bound, if any,
  // of the bean type parameter [payload type argument]" (when both arguments are type variables) should *actually*
  // read:
  //
  // "for each bound, PTA, of the bean type parameter [payload type argument], there is at least one bound, RTA, of the
  // required type parameter [receiver type argument], which is assignable to PTA."
  //
  // (Spin through all PTAs, and for each PTA:
  //   (Spin through all RTAs, and find one assignable to PTA.)
  //
  // The TCK enforces this, even though it's not in the specification (!).
  //
  // Weld's implementation confuses type parameters with arguments, just like the specification. They have a series of
  // methods implemented as part of PR 614 (https://github.com/weld/core/pull/614) named "parametersMatch" [arguments
  // match].
  //
  // Weld also has methods named things like "getUppermostTypeVariableBounds" and "getUppermostBounds". These appear to
  // "condense" useless type variable extensions to "get to" the "real" types involved. So T extends S extends String
  // becomes String.
  //
  // Digging deeper, you (I) might think getUppermostTypeVariableBounds(tv) is just erase(tv) applied recursively. But I
  // think you would be wrong.
  //
  // Start with:
  // https://github.com/openjdk/jdk/blob/181845ae46157a9bb3bf8e2a328fa59eddc0273a/src/jdk.compiler/share/classes/com/sun/tools/javac/code/Types.java#L2450
  //
  // Compare vs.:
  // https://github.com/weld/core/blob/e894d1699ff1c91332605f5ecae5f53410effb81/impl/src/main/java/org/jboss/weld/resolution/AbstractAssignabilityRules.java#L57-L62
  //
  // To illustrate the difference between the two operations, recursive erasure (according to the Java Language
  // Specification) of the pseudo-declaration T extends S extends List<String> & Serializable would yield, simply, List
  // (T would erease to "the erasure of its leftmost bound", which would be the erasure of S, which would be "the
  // erasure of its leftmost bound", which would be the erasure of List<String>, which would be List. Serializable just
  // gets dropped.)
  //
  // By contrast, Weld's getUppermostTypeVariableBounds(T) operation would yield [List<String>, Serializable] (have not
  // tested this with code, just reading).
  //
  // I think this is what the javac compiler calls, somewhat confusingly, "classBound":
  // https://github.com/openjdk/jdk/blob/jdk-24%2B7/src/jdk.compiler/share/classes/com/sun/tools/javac/code/Types.java#L2760-L2796
  //
  // (With the javax.lang.model.type.* model, this condensing isn't needed.)
  // So then:
  //
  // For every bound in (condensed) receiverBounds, is there a bound in (condensed) payloadBounds that is covariantly
  // assignable to it?
  //
  // Note that the "backwards" nature of that sentence has already happened before we get to this method.
  //
  // (Is there one bound in (condensed) payloadBounds that matches all bounds in (condensed) receiverBounds?)
  //
  // (Because of the javax.lang.model.* isAssignable() semantics, condensing turns out to be entirely unnecessary.)
  protected boolean covariantlyAssignable(final List<? extends TypeMirror> receiverBounds, List<? extends TypeMirror> payloadBounds) {
    for (final TypeMirror receiverBound : receiverBounds) {
      if (!covariantlyAssignable(receiverBound, payloadBounds)) {
        return false;
      }
    }
    return true;
    /*
    payloadBounds = this.types.condense(payloadBounds);
    for (final TypeMirror receiverBound : this.types.condense(receiverBounds)) {
      if (!covariantlyAssignable(receiverBound, payloadBounds)) {
        return false;
      }
    }
    return true;
    */
  }

  // Is there a bound in payloadBounds that is assignable to receiver using Java, not CDI, assignability semantics?
  protected boolean covariantlyAssignable(final TypeMirror receiver, final List<? extends TypeMirror> payloadBounds) {
    for (final TypeMirror payloadBound : payloadBounds) {
      if (covariantlyAssignable(receiver, payloadBound)) {
        return true;
      }
    }
    return false;
    /*
    return switch (receiver.getKind()) {
    case ARRAY, DECLARED -> {
      for (final TypeMirror condensedPayloadBound : payloadBounds) {
        switch (condensedPayloadBound.getKind()) {
        case ARRAY:
        case DECLARED:
          if (covariantlyAssignable(receiver, (ReferenceType)condensedPayloadBound)) {
            yield true;
          }
          break;
        default:
          throw new IllegalArgumentException("payloadBounds: " + payloadBounds);
        }
      }
      yield false;
    }
    default -> throw new IllegalArgumentException("receiver: " + receiver + "; kind: " + receiver.getKind());
    };
    */
  }

  // Is classOrArrayTypePayload assignable to receiver following the rules of Java assignability
  // (i.e. covariance)?
  protected boolean covariantlyAssignable(final TypeMirror receiver, final TypeMirror payload) {
    return Objects.requireNonNull(receiver, "receiver") == payload || this.domain().assignable(payload, receiver); // yes, "backwards"
  }

  /**
   * Returns an {@link Optional} housing a {@link ConstantDesc} that represents this {@link AbstractTypeMatcher}
   * implementation.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>The default implementation of this method relies on the presence of a {@code public} constructor that accepts a
   * single {@link Domain}-typed argument.</p>
   *
   * <p>The {@link Optional} returned by an invocation of this method may be, and often will be, {@linkplain
   * Optional#isEmpty() empty}.</p>
   *
   * @return an {@link Optional} housing a {@link ConstantDesc} that represents this {@link AbstractTypeMatcher}
   * implementation; never {@code null}
   *
   * @see Constable#describeConstable()
   */
  @Override // Constable
  public Optional<? extends ConstantDesc> describeConstable() {
    return (this.domain() instanceof Constable c ? c.describeConstable() : Optional.<ConstantDesc>empty())
      .map(domainDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                MethodHandleDesc.ofConstructor(ClassDesc.of(this.getClass().getName()),
                                                                               CD_Domain),
                                                domainDesc));
  }

  // Is payload "identical to" receiver, following the intent of CDI? The relation "identical to" is not defined in the
  // specification. Does it mean ==? Does it mean equals()? Does it mean
  // javax.lang.model.util.Types#isSameType(TypeMirror, TypeMirror)? Something else?
  //
  // This implementation chooses Domain#sameType(TypeMirror, TypeMirror), but with one
  // change. Domain#sameType(TypeMirror, TypeMirror) is usually backed by the
  // javax.lang.model.util.Types#isSameType(TypeMirror, TypeMirror) method. That method will return false if either
  // argument is a wildcard type. This method first checks to see if the arguments are the same Java references (==),
  // regardless of type.
  protected boolean identical(final TypeMirror receiver, final TypeMirror payload) {
    // CDI has an undefined notion of "identical to". This method attempts to divine and implement the intent. Recall
    // that javax.lang.model.* compares types with "sameType" semantics.
    return
      Objects.requireNonNull(receiver, "receiver") == Objects.requireNonNull(payload, "payload") ||
      this.domain().sameType(receiver, payload);
  }

  // Return t if its element declares a non-generic class, or if it is the raw type usage of a generic class.
  protected TypeMirror nonGenericClassOrRawType(final TypeMirror t) {
    return this.yieldsRawType(t) ? this.rawType(t) : t;
  }

  /**
   * Returns the {@link Domain} used by this {@link AbstractTypeMatcher} implementation.
   *
   * @return the {@link Domain} used by this {@link AbstractTypeMatcher} implementation; never {@code
   * null}
   *
   * @see #AbstractTypeMatcher(Domain)
   *
   * @see Domain
   */
  protected final Domain domain() {
    return this.domain;
  }

  protected final TypeMirror elementType(final TypeMirror t) {
    return this.domain().elementType(t);
  }

  protected final boolean generic(final TypeMirror t) {
    final Domain domain = this.domain();
    return domain.generic(domain.asElement(t));
  }

  protected final boolean parameterized(final TypeMirror t) {
    return this.domain().parameterized(t);
  }

  protected final boolean raw(final TypeMirror t) {
    return this.domain().raw(t);
  }

  protected final TypeMirror rawType(final TypeMirror t) {
    return this.domain().rawType(t);
  }

  // Is t an unbounded type variable?
  //
  // CDI does not define what an "unbounded type variable" is. This method attempts to divine and implement the intent.
  //
  // Since according to the Java Language Specification, all type variables have an upper bound, which in the
  // pathological case is java.lang.Object, it would seem that "unbounded" means "has java.lang.Object as its upper
  // bound".
  //
  // It is not clear in the case of T extends S whether T is an unbounded TypeVariable or not. This interpretation
  // behaves as if it is.
  //
  // Weld seems to take the position that an unbounded type variable is one that has java.lang.Object as its sole upper
  // bound; see
  // https://github.com/weld/core/blob/5.1.2.Final/impl/src/main/java/org/jboss/weld/util/Types.java#L258. Under this
  // interpretation T extends S would not be considered an unbounded type variable. Type variable bounds are erased in
  // every other situation in CDI.
  protected final boolean unboundedTypeVariable(TypeMirror t) {
    UniversalType ut = UniversalType.of(t, this.domain());
    if (ut.getKind() == TypeKind.TYPEVAR) {
      ut = ut.getUpperBound();
      return this.domain().javaLangObject(ut) || this.unboundedTypeVariable(ut);
    }
    return false;
    /*
    return switch (t.getKind()) {
    case TYPEVAR -> {
      final List<? extends TypeMirror> condensedBounds = this.types.condense(((TypeVariable)t).getUpperBound());
      yield condensedBounds.size() == 1 && this.types.isJavaLangObject(condensedBounds.get(0));
    }
    default -> false;
    };
    */
  }

  // Can t *yield* a raw type?
  //
  // We say that to yield a raw type, t must be either:
  //
  // * a declared type with at least one type argument ("parameterized")
  // * an array type with a parameterized element type
  protected final boolean yieldsRawType(final TypeMirror t) {
    final TypeMirror rawT = this.domain().rawType(t);
    return rawT != null && rawT != t;
    // return parameterized(t) || t.getKind() == TypeKind.ARRAY && parameterized(elementType(t));
  }



  /*
   * Static methods.
   */


  // Do all supplied TypeMirrors, when used as type arguments, pass the test represented by the supplied Predicate?
  protected static final boolean allTypeArgumentsAre(final Iterable<? extends TypeMirror> typeArguments, final Predicate<? super TypeMirror> p) {
    for (final TypeMirror t : typeArguments) {
      if (!p.test(t)) {
        return false;
      }
    }
    return true;
  }

  // Is t a type that CDI considers to be "parameterized" in its own sense of the word, not the sense used by the Java
  // Language Specification?
  //
  // There are some cases, but not all, where CDI (incorrectly) considers an array type to be something that can be
  // parameterized, or else "bean" and "event type parameter" resolution would never work. See
  // https://stackoverflow.com/questions/76493672/when-cdi-speaks-of-a-parameterized-type-does-it-also-incorrectly-mean-array-typ.
  //
  // The semantics CDI wants to express are really: can t *yield* a raw type, for a certain definition of "yield"? See
  // Types#yieldsRawType(TypeMirror).
  protected final boolean cdiParameterized(final TypeMirror t) {
    return this.yieldsRawType(t);
  }

  // Returns a new IllegalArgumentException describing an illegal payload type.
  static IllegalArgumentException illegalPayload(final TypeMirror payload) {
    return new IllegalArgumentException("Illegal payload kind: " + payload.getKind() + "; payload: " + payload);
  }

  // Returns a new IllegalArgumentException describing an illegal receiver type.
  static IllegalArgumentException illegalReceiver(final TypeMirror receiver) {
    return new IllegalArgumentException("Illegal receiver kind: " + receiver.getKind() + "; receiver: " + receiver);
  }

  // Regardless of its reported TypeKind, does t's declaring TypeElement bear the supplied fully qualified name?
  //
  // Throws ClassCastException if the return value of t.asElement() is not a TypeElement.
  protected static final boolean named(final DeclaredType t, final CharSequence n) {
    // (No getKind() check on purpose.)
    return ((QualifiedNameable)t.asElement()).getQualifiedName().contentEquals(n);
  }


}
