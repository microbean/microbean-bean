/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2024–2026 microBean™.
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
package org.microbean.bean.model;

import java.util.ArrayList;
import java.util.List;

import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

// Experimental and possibly incomplete.
final class Elements {

  private Elements() {
    super();
  }

  static final Stream<Element> streamBreadthFirst(final Element element) {
    return Trees.streamBreadthFirst(element, Element::getEnclosedElements);
  }

  static final Stream<Element> streamDepthFirst(final Element element) {
    return Trees.streamDepthFirst(element, Element::getEnclosedElements);
  }

  // Convenience method invokable by reference to include method parameters as enclosed "children" of a CONSTRUCTOR or
  // METHOD Element. For some reason getEnclosedElements() invoked on a CONSTRUCTOR or METHOD Element does not return
  // its parameters, but invoking getEnclosingElement() on a PARAMETER Element returns the CONSTRUCTOR or METHOD Element
  // that in fact logically encloses it.
  static final List<? extends Element> parametersAndEnclosedElements(final Element e) {
    return switch (e.getKind()) {
    case CONSTRUCTOR, METHOD -> {
      final List<? extends Element> parameters = ((ExecutableElement)e).getParameters();
      final List<? extends Element> enclosedElements = e.getEnclosedElements();
      if (parameters.isEmpty()) {
        yield enclosedElements.isEmpty() ? List.of() : enclosedElements;
      } else if (enclosedElements.isEmpty()) {
        yield parameters;
      }
      final List<Element> list = new ArrayList<>(parameters.size() + enclosedElements.size());
      list.addAll(parameters);
      list.addAll(enclosedElements);
      yield list;
    }
    default -> e.getEnclosedElements();
    };
  }

  private static final boolean isNonNull(final Object x) {
    return x != null;
  }

}
