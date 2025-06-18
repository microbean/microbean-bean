/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023–2025 microBean™.
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
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.lang.model.type.TypeMirror;

import org.microbean.attributes.Attributed;
import org.microbean.attributes.Attributes;

import org.microbean.constant.Constables;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_List;
import static java.lang.constant.ConstantDescs.FALSE;
import static java.lang.constant.ConstantDescs.TRUE;

import static org.microbean.bean.BeanTypes.legalBeanType;

/**
 * An identifier for a {@link Bean}.
 *
 * @param types a {@link BeanTypeList}
 *
 * @param attributes a {@link List} of {@link Attributes}s
 *
 * @param alternate whether this {@link Id} is to be considered an <dfn>alternate</dfn>
 *
 * @param rank the {@linkplain Ranked#rank() rank} of this {@link Id}; often {@link Ranked#DEFAULT_RANK} to indicate no
 * particular rank; <strong>always {@link Ranked#DEFAULT_RANK} if {@code alternate} is {@code false}</strong>
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see Ranked
 */
public final record Id(BeanTypeList types,
                       List<Attributes> attributes,
                       boolean alternate,
                       int rank)
  implements Attributed, Constable, Ranked {


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link Id} that is not an alternate and that therefore has a {@linkplain Ranked#DEFAULT_RANK default
   * rank}.
   *
   * @param types a {@link BeanTypeList}; must not be {@code null}; must not be {@linkplain List#isEmpty() empty}
   *
   * @param attributes a {@link List} of {@link Attributes}s; must not be {@code null}
   *
   * @exception NullPointerException if {@code types} or {@code attributes} is {@code null}
   */
  public Id(final BeanTypeList types,
            final List<Attributes> attributes) {
    this(types, attributes, false, DEFAULT_RANK);
  }

  /**
   * Creates a new {@link Id}.
   *
   * @param types a {@link BeanTypeList}; must not be {@code null}; must not be {@linkplain List#isEmpty() empty}
   *
   * @param attributes a {@link List} of {@link Attributes}s; must not be {@code null}
   *
   * @param alternate whether this {@link Id} is to be considered an <dfn>alternate</dfn>
   *
   * @param rank the {@linkplain Ranked#rank() rank} of this {@link Id}; often {@link Ranked#DEFAULT_RANK} to indicate
   * no particular rank; <strong>always {@link Ranked#DEFAULT_RANK} if {@code alternate} is {@code false}</strong>
   *
   * @exception NullPointerException if {@code types} or {@code attributes} is {@code null}
   */
  public Id {
    Objects.requireNonNull(types, "types");
    attributes = List.copyOf(attributes);
    if (!alternate) {
      rank = DEFAULT_RANK;
    }
  }


  /*
   * Instance methods.
   */


  @Override // Constable
  public final Optional<DynamicConstantDesc<Id>> describeConstable() {
    return Constables.describeConstable(this.attributes())
      .flatMap(attributesDesc -> this.types().describeConstable()
               .map(typesDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                        MethodHandleDesc.ofConstructor(this.getClass()
                                                                                         .describeConstable()
                                                                                         .orElseThrow(),
                                                                                       CD_List,
                                                                                       CD_List,
                                                                                       CD_boolean,
                                                                                       CD_int),
                                                        typesDesc,
                                                        attributesDesc,
                                                        this.alternate() ? TRUE : FALSE,
                                                        this.rank())));
  }

}
