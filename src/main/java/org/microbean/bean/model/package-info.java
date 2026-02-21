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

/**
 * Provides classes and interfaces related to modeling dependency resolutions.
 *
 * <p>This package is <strong>experimental</strong>, incomplete, and subject to change at any moment.</p>
 *
 * <p>The general idea is:</p>
 *
 * <ul>
 *
 * <li>You start with a list of {@link org.microbean.bean.Bean}s ({@link org.microbean.assign.Aggregate}s, really). Each
 * element will have {@linkplain org.microbean.bean.Bean#dependencies() dependencies}. A dependency is represented by an
 * {@link javax.lang.model.AnnotatedConstruct} (normally a {@link javax.lang.model.element.Element}. (A dependency will
 * not represent the {@linkplain org.microbean.bean.Bean bean} that {@linkplain org.microbean.bean.Bean#dependencies()
 * supplies} it.)
 *
 * <li>From this list, you can extract a <em>set</em> of {@link javax.lang.model.AnnotatedConstruct}s. These represent
 * all the <dfn>demand</dfn> in the system.</li>
 *
 * <li>For each such {@link javax.lang.model.AnnotatedConstruct}, you can perform {@linkplain
 * org.microbean.bean.Beans#typesafeFilteringSelectable(java.util.Collection, org.microbean.assign.Matcher) typesafe
 * resolution} and find a {@link org.microbean.bean.Bean} that <dfn>satisfies</dfn> the demand.</li>
 *
 * <li>The resulting <dfn>model</dfn> should not have any "holes". That is, every {@link
 * javax.lang.model.AnnotatedConstruct} should be matched up with exactly one {@link org.microbean.bean.Bean}. When this
 * is not the case, the model is <dfn>invalid</dfn>.</li>
 *
 * </ul>
 *
 * @author <a href="https://about.me/lairdnelson" target="_parent">Laird Nelson</a>
 */
package org.microbean.bean.model;
