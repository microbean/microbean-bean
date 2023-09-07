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

public class Destructor<I> {

  private final PreDestructor<I> preDestructor;

  // @GuardedBy("this")
  private volatile boolean destroyed;

  public Destructor() {
    this(null);
  }

  public Destructor(final PreDestructor<I> preDestructor) {
    super();
    this.preDestructor = preDestructor == null ? Destructor::noopDestroying : preDestructor;
  }

  public synchronized final void destroy(final I i, final AutoCloseable destructionRegistry, final References<?> r) {
    if (this.destroyed) {
      return;
    }
    if (destructionRegistry == null) {
      this.preDestructor.destroying(i, r);
    } else {
      Throwable t = null;
      try (destructionRegistry) {
        this.destroy(this.preDestructor.destroying(i, r));
      } catch (final RuntimeException | Error e) {
        throw e;
      } catch (final Exception e) {
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
        throw new DestructionException(e.getMessage(), e);
      }
    }
    this.destroyed = true;
  }

  // Users should NOT call this.
  //
  // MUST NOT call destroy(I, Destruction). It's the other way around.
  // MUST be idempotent
  protected void destroy(final I i) {
    if (i instanceof AutoCloseable ac) {
      try {
        ac.close();
      } catch (final RuntimeException | Error re) {
        throw re;
      } catch (final Exception e) {
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
        throw new DestructionException(e.getMessage(), e);
      }
    }
  }

  private static final <I> I noopDestroying(final I i, final References<?> r) {
    return i;
  }

}
