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

/**
 * Provides classes and interfaces related to <dfn>demand</dfn> modeled by injection points.
 *
 * <p>This package is experimental, incomplete, and subject to change at any moment.</p>
 *
 * <p>The general idea is:</p>
 *
 * <ul>
 *
 * <li>You start with a list of {@link org.microbean.bean.Bean}s ({@link org.microbean.assign.Aggregate}s, really). Each
 * element will have {@linkplain org.microbean.bean.Bean#dependencies() dependencies}. A dependency is represented by an
 * {@link org.microbean.assign.AttributedElement}. (For any dependency, it will not represent the bean that supplies
 * it.)</li>
 *
 * <li>From this list, you can extract a <em>set</em> of {@link org.microbean.assign.AttributedType}s. These represent
 * all the <dfn>demand</dfn> in the system.</li>
 *
 * <li>For each such {@link org.microbean.assign.AttributedType}, you can perform typesafe resolution and find a {@link
 * org.microbean.bean.Bean} that <dfn>satisfies</dfn> the demand.</li>
 *
 * <li>The resulting <dfn>model</dfn> should not have any "holes". That is, every {@link
 * org.microbean.assign.AttributedType} should be matched up with exactly one {@link org.microbean.bean.Bean}.</li>
 *
 * </ul>
 *
 * @author <a href="https://about.me/lairdnelson" target="_parent">Laird Nelson</a>
 */
package org.microbean.bean.model;
