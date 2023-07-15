/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023 microBean™.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class Assignments {

  private final ReadWriteLock lock;
  
  private final List<Assignment<?, ?>> assignments;
  
  public Assignments() {
    super();
    this.assignments = new ArrayList<>();
    this.lock = new ReentrantReadWriteLock();
  }

  public final boolean add(final Assignment<?, ?> a) {
    this.lock.writeLock().lock();
    try {
      return this.assignments.add(a);
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  public final List<Assignment<?, ?>> toList() {
    this.lock.readLock().lock();
    try {
      return List.copyOf(this.assignments);
    } finally {
      this.lock.readLock().unlock();
    }
  }

  @Override // Object
  public final String toString() {
    this.lock.readLock().lock();
    try {
      return this.assignments.toString();
    } finally {
      this.lock.readLock().unlock();
    }
  }

}
