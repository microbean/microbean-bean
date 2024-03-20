/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2024 microBean™.
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

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * An abstract {@link Instantiator} implementation that is capable of performing dependency resolution.
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 *
 * @see #instantiate()
 */
// Experimental, but it sure feels like it's on the right track.
//
// Subordinate to Producer<I> (and therefore Factory<I>).
//
// Creates something, performing constructor injection. Does absolutely nothing else at the moment. Will, certainly,
// NEVER do constructor or business method interception.
//
// Not used by org.microbean.bean itself, but incredibly useful.
//
// TODO: this isn't really specific to instantiation; can be used for invoking any method, for example
public abstract class AbstractInstantiator<R, I> implements Instantiator<I> {


  /*
   * Instance fields.
   */


  private final Supplier<I> s;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link AbstractInstantiator} implementation (convenience constructor).
   *
   * @see #AbstractInstantiator(Object, IntFunction)
   */
  // For creating something that has no dependencies via a constructor (for example)
  protected AbstractInstantiator() {
    this(null);
  }

  /**
   * Creates a new {@link AbstractInstantiator} implementation (convenience constructor).
   *
   * @param df an {@link IntFunction} that accepts a zero-based <em>position</em> (such as that of a method or
   * constructor parameter) and returns a (possibly {@code null}) resolved dependency suitable for that position; may be
   * {@code null}
   *
   * @see #AbstractInstantiator(Supplier, IntFunction)
   */
  protected AbstractInstantiator(final IntFunction<?> df) {
    this((Supplier<? extends R>)null, df);
  }

  /**
   * Creates a new {@link AbstractInstantiator} implementation (convenience constructor).
   *
   * @param receiver a receiver whose instance method (or similar) will be used to instantiate the product; may be
   * {@code null} if such a receiver is not needed
   *
   * @param df an {@link IntFunction} that accepts a zero-based <em>position</em> (such as that of a method or
   * constructor parameter) and returns a (possibly {@code null}) resolved dependency suitable for that position; may be
   * {@code null}
   *
   * @see #AbstractInstantiator(Supplier, IntFunction)
   */
  protected AbstractInstantiator(final R receiver, final IntFunction<?> df) {
    super();
    final IntFunction<?> f = df == null ? AbstractInstantiator::returnNull : df;
    this.s = () -> this.instantiate(receiver, f);
  }

  /**
   * Creates a new {@link AbstractInstantiator} implementation (preferred constructor).
   *
   * @param rs a {@link Supplier} of a receiver whose instance method (or similar) will be used to instantiate the
   * product; may be {@code null} if such a receiver is not needed
   *
   * @param df an {@link IntFunction} that accepts a zero-based <em>position</em> (such as that of a method or
   * constructor parameter) and returns a (possibly {@code null}) resolved dependency suitable for that position; may be
   * {@code null}
   */
  protected AbstractInstantiator(final Supplier<? extends R> rs, final IntFunction<?> df) {
    super();
    final Supplier<? extends R> s = rs == null ? AbstractInstantiator::returnNull : rs;
    final IntFunction<?> f = df == null ? AbstractInstantiator::returnNull : df;
    this.s = () -> this.instantiate(s.get(), f);
  }

  /**
   * Creates a new {@link AbstractInstantiator} implementation (convenience constructor).
   *
   * @param c a {@link Creation}; may be {@code null}
   *
   * @param bscs a {@link Collection} of {@link BeanSelectionCriteria} that will be used to resolve required
   * dependencies; may be {@code null} if there are no dependencies required for the instantiation mechanism
   *
   * @param rs a {@link ReferenceSelector}; may be {@code null} if {@code beanClassSelectionCriteria} is {@code null}
   * and there are no dependencies to resolve
   *
   * @exception NullPointerException in certain cases if {@code rs} is {@code null}
   *
   * @see #AbstractInstantiator(BeanSelectionCriteria, Creation, IntFunction, PositionalCreationBiFunction,
   * ReferenceSelector)
   */
  protected AbstractInstantiator(final Creation<?> c,
                                 final Collection<? extends BeanSelectionCriteria> bscs,
                                 final ReferenceSelector rs) {
    this(null, c, bscf(bscs, null), null, rs);
  }

  /**
   * Creates a new {@link AbstractInstantiator} implementation (convenience constructor).
   *
   * @param c a {@link Creation}; may be {@code null}
   *
   * @param bscs a {@link Collection} of {@link BeanSelectionCriteria} that will be used to resolve required
   * dependencies; may be {@code null} if there are no dependencies required for the instantiation mechanism
   *
   * @param pcbf a {@link PositionalCreationBiFunction} that supplies a {@link Creation} given an initial {@link
   * Creation} and a zero-based <em>position</em> (such as that of a method or constructor parameter); may be {@code
   * null}; the first argument supplied to this {@link PositionalCreationBiFunction} will be {@code c}
   *
   * @param rs a {@link ReferenceSelector}; may be {@code null} if {@code beanClassSelectionCriteria} is {@code null}
   * and there are no dependencies to resolve
   *
   * @exception NullPointerException in certain cases if {@code rs} is {@code null}
   *
   * @see #AbstractInstantiator(BeanSelectionCriteria, Creation, IntFunction, PositionalCreationBiFunction,
   * ReferenceSelector)
   */
  protected AbstractInstantiator(final Creation<?> c,
                                 final Collection<? extends BeanSelectionCriteria> bscs,
                                 final PositionalCreationBiFunction pcbf,
                                 final ReferenceSelector rs) {
    this(null, c, bscf(bscs, null), pcbf, rs);
  }

  /**
   * Creates a new {@link AbstractInstantiator} implementation (convenience constructor).
   *
   * @param beanClassSelectionCriteria a {@link BeanSelectionCriteria} that can select the receiver whose instance method
   * (or similar) will be used to instantiate the product; may be {@code null} if such a receiver is not needed
   *
   * @param c a {@link Creation}; may be {@code null}
   *
   * @param bscs a {@link Collection} of {@link BeanSelectionCriteria} that will be used to resolve required
   * dependencies; may be {@code null} if there are no dependencies required for the instantiation mechanism
   *
   * @param zeroBasedPositions an even-length array of {@code int}s whose even-indexed elements are "positions" (such as
   * of parameters of a method or constructor) and whose odd-indexed elements are corresponding indices into a {@link
   * List} formed from the supplied {@code bscs} {@link Collection}; the array thus serves as a kind of map; may be
   * {@code null}
   *
   * @param pcbf a {@link PositionalCreationBiFunction} that supplies a {@link Creation} given an initial {@link
   * Creation} and a zero-based <em>position</em> (such as that of a method or constructor parameter); may be {@code
   * null}; the first argument supplied to this {@link PositionalCreationBiFunction} will be {@code c}
   *
   * @param rs a {@link ReferenceSelector}; may be {@code null} if {@code beanClassSelectionCriteria} is {@code null}
   * and there are no dependencies to resolve
   *
   * @exception NullPointerException in certain cases if {@code rs} is {@code null}
   *
   * @exception IllegalArgumentException if {@code zeroBasedPositions} is not empty and {@code bscs} is empty, or if
   * {@code zeroBasedPositions} is empty and {@code bscs} is not empty, or if the length of the {@code int} array is
   * greater than the size of the {@code bscs} {@link Collection}
   *
   * @exception IndexOutOfBoundsException if {@code zeroBasedPositions} contains an {@code int} that is less than {@code
   * 0}
   *
   * @see #AbstractInstantiator(BeanSelectionCriteria, Creation, IntFunction, PositionalCreationBiFunction,
   * ReferenceSelector)
   */
  protected AbstractInstantiator(final BeanSelectionCriteria beanClassSelectionCriteria,
                                 final Creation<?> c,
                                 final Collection<? extends BeanSelectionCriteria> bscs,
                                 final int[] zeroBasedPositions,
                                 final PositionalCreationBiFunction pcbf,
                                 final ReferenceSelector rs) {
    this(beanClassSelectionCriteria,
         c,
         bscf(bscs, zeroBasedPositions),
         pcbf,
         rs);
  }

  /**
   * Creates a new {@link AbstractInstantiator} implementation (preferred constructor).
   *
   * @param beanClassSelectionCriteria a {@link BeanSelectionCriteria} that can select the receiver whose instance
   * method (or similar) will be used to instantiate the product; may be {@code null} if such a receiver is not needed
   *
   * @param c a {@link Creation}; may be {@code null}
   *
   * @param f an {@link IntFunction} that accepts a zero-based <em>position</em> (such as that of a method or
   * constructor parameter) and returns a (possibly {@code null} {@link BeanSelectionCriteria} corresponding to that
   * position; may be {@code null} if there are no dependencies to be resolved
   *
   * @param pcbf a {@link PositionalCreationBiFunction} that supplies a {@link Creation} given an initial {@link
   * Creation} and a zero-based <em>position</em> (such as that of a method or constructor parameter); may be {@code
   * null}; the first argument supplied to this {@link PositionalCreationBiFunction} will be {@code c}
   *
   * @param rs a {@link ReferenceSelector}; may be {@code null} if {@code beanClassSelectionCriteria} is {@code null}
   * and there are no dependencies to resolve
   *
   * @exception NullPointerException in certain cases if {@code rs} is {@code null}
   */
  protected AbstractInstantiator(final BeanSelectionCriteria beanClassSelectionCriteria, // nullable
                                 final Creation<?> c,
                                 final IntFunction<? extends BeanSelectionCriteria> f,
                                 final PositionalCreationBiFunction pcbf,
                                 final ReferenceSelector rs) {
    super();
    final PositionalCreationBiFunction cf = pcbf == null ? AbstractInstantiator::ignorePosition : pcbf;
    if (beanClassSelectionCriteria == null) {
      if (f == null) {
        this.s = () -> this.instantiate(null, AbstractInstantiator::returnNull);
      } else {
        Objects.requireNonNull(rs, "rs");
        this.s = () -> this.instantiate(null, p -> rs.reference(f.apply(p), cf.apply(c, p)));
      }
    } else {
      Objects.requireNonNull(rs, "rs");
      if (f == null) {
        this.s = () -> this.instantiate(rs.reference(beanClassSelectionCriteria, Creation.cast(c)),
                                        AbstractInstantiator::returnNull);
      } else {
        this.s = () -> this.instantiate(rs.reference(beanClassSelectionCriteria, Creation.cast(c)),
                                        p -> rs.reference(f.apply(p), cf.apply(c, p)));
      }
    }
  }


  /*
   * Instance methods.
   */


  /**
   * Instantiates the product and returns it.
   *
   * @return the result of instantiation; never {@code null}
   *
   * @see #instantiate(Object, IntFunction)
   */
  @Override // Instantiator<I>
  public final I instantiate() {
    return this.s.get();
  }

  /**
   * Instantiates the product and returns it, possibly using the supplied <em>receiver</em> and dependency resolver
   * function.
   *
   * <p>Implementations of this method must not return {@code null}.</p>
   *
   * <p>This method is invoked by the {@link #instantiate()} method. Implementations of this method must not call {@link
   * #instantiate()} or an infinite loop may result.</p>
   *
   * @param r a receiver on which an instance method may be invoked, if that is the means of instantiation; may be
   * {@code null} if no such receiver is required or could be located
   *
   * @param f an {@link IntFunction} that, given a zero-based <em>position</em> (such as that of a method or constructor
   * parameter), returns a resolved depdencency suitable for that location, or {@code null}; must not be {@code null}
   *
   * @return the product; never {@code null}
   *
   * @exception NullPointerException if {@code f} is {@code null}
   *
   * @exception IndexOutOfBoundsException if {@code f} is supplied with a negative {@code int} as its second argument
   */
  // r: a receiver on which an instance method may be invoked
  // f: an IntFunction taking a position (such as of a parameter) and returning a dependency (which may be null)
  protected abstract I instantiate(final R r, final IntFunction<?> f);


  /*
   * Static methods.
   */


  private static <T> T returnNull() {
    return null;
  }

  private static <T> T returnNull(final int ignored) {
    return null;
  }

  private static <T> T ignorePosition(final T returnValue, final int ignored) {
    return returnValue;
  }

  private static IntFunction<? extends BeanSelectionCriteria> bscf(final Collection<? extends BeanSelectionCriteria> bscs,
                                                                   final int[] zeroBasedPositions) {
    final List<BeanSelectionCriteria> finalBscs = bscs == null || bscs.isEmpty() ? List.of() : List.copyOf(bscs);
    if (zeroBasedPositions == null) {
      return finalBscs::get;
    }
    final int[] ps = zeroBasedPositions.clone();
    if (ps.length <= 0) {
      if (!finalBscs.isEmpty()) {
        throw new IllegalArgumentException();
      }
      return AbstractInstantiator::returnNull;
    } else if (ps.length % 2 != 0) {
      throw new IllegalArgumentException();
    } else {
      for (int i = 0; i < ps.length; i++) {
        if (ps[i] < 0) {
          throw new IndexOutOfBoundsException("ps[" + i + "]: " + ps[i]);
        }
      }
      return p -> {
        for (int i = 0; i < ps.length; i++) {
          if (ps[i++] == p) {
            return finalBscs.get(ps[i]);
          }
        }
        return null;
      };
    }
  }


  /*
   * Inner and nested classes.
   */


  /**
   * A {@linkplain FunctionalInterface functional interface} that returns a suitable {@link Creation} given a zero-based
   * <em>position</em> (such as that of a method or constructor parameter).
   *
   * @see #apply(Creation, int)
   */
  @FunctionalInterface
  public static interface PositionalCreationBiFunction {

    /**
     * Returns a (possibly {@code null}) {@link Creation}, which might be the supplied {@link Creation}, suitable for
     * the supplied, zero-based <em>position</em> (such as that of a method or constructor parameter).
     *
     * <p>Implementations of this method may return {@code null}.</p>
     *
     * @param c a {@link Creation}; may be {@code null}
     *
     * @param p a zero-based <em>position</em> (such as that of a method or constructor parameter); must not be negative
     *
     * @exception IndexOutOfBoundsException if {@code p} is negative
     */
    public Creation<?> apply(final Creation<?> c, final int p);

  }

}
