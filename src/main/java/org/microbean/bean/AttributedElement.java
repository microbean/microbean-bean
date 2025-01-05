/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2024 microBean™.
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
import java.util.Objects;
import java.util.Optional;

import javax.lang.model.element.Element;

import javax.lang.model.type.TypeMirror;

import org.microbean.constant.Constables;

import org.microbean.qualifier.NamedAttributeMap;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_List;

/**
 * An {@link Element} that has been decorated with {@linkplain NamedAttributeMap attributes}.
 *
 * <p>Dependency injection frameworks often refer to this sort of thing as an <dfn>injection point</dfn>.</p>
 *
 * @param element a non-{@code null} {@link Element}
 *
 * @param attributes a non-{@code null} {@link List} of {@link NamedAttributeMap} instances representing attributes
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 */
public final record AttributedElement(Element element, List<NamedAttributeMap<?>> attributes) implements Constable {

  private static final ClassDesc CD_Element = ClassDesc.of("javax.lang.model.element.Element");
  
  public AttributedElement {
    Objects.requireNonNull(element, "element");
    attributes = List.copyOf(attributes);
  }

  public final TypeMirror type() {
    return this.element().asType();
  }

  public final AttributedType attributedType() {
    return new AttributedType(this.type(), this.attributes());
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
    return this.element() instanceof Constable e ? e.describeConstable() : Optional.<ConstantDesc>empty()
      .flatMap(elementDesc -> Constables.describeConstable(this.attributes())
               .map(attributesDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                             MethodHandleDesc.ofConstructor(ClassDesc.of(this.getClass().getName()),
                                                                                            CD_Element,
                                                                                            CD_List),
                                                             elementDesc,
                                                             attributesDesc)));
  }

}
