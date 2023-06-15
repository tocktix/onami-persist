package org.apache.onami.persist;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Injector;
import com.google.inject.Key;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/** All persistence units. This is a convenience wrapper for multiple persistence units. */
@Singleton
class AllPersistenceUnits implements AllPersistenceServices, AllUnitsOfWork {

  /** Collection of all known persistence services. */
  private final List<PersistenceService> persistenceServices = new ArrayList<>();

  /** Collection of all known units of work. */
  private final List<UnitOfWork> unitsOfWork = new ArrayList<>();

  /** Collection of the keys of all known persistence services. */
  private final Set<Key<PersistenceService>> persistenceServiceKeys = new HashSet<>();

  /** Collection of the keys of of all known units of work. */
  private final Set<Key<UnitOfWork>> unitOfWorkKeys = new HashSet<>();

  /**
   * Adds a persistence service and a unit of work to this collection.
   *
   * @param psKey the persistence service to add. Must not be {@code null}.
   * @param uowKey the unit of work to add. Must not be {@code null}.
   */
  void add(Key<PersistenceService> psKey, Key<UnitOfWork> uowKey) {
    persistenceServiceKeys.add(checkNotNull(psKey, "psKey is mandatory!"));
    unitOfWorkKeys.add(checkNotNull(uowKey, "ouwKey is mandatory!"));
  }

  @Inject
  private void init(Injector injector) {
    for (Key<PersistenceService> persistenceServiceKey : persistenceServiceKeys) {
      persistenceServices.add(injector.getInstance(persistenceServiceKey));
    }
    for (Key<UnitOfWork> unitOfWorkKey : unitOfWorkKeys) {
      unitsOfWork.add(injector.getInstance(unitOfWorkKey));
    }
  }

  /** {@inheritDoc} */
  // @Override
  public void startAllStoppedPersistenceServices() {
    AggregatedException.Builder exceptionBuilder = new AggregatedException.Builder();
    Queue<Exception> exceptions = new ConcurrentLinkedQueue<>();
    ExecutorService executorService = Executors.newCachedThreadPool();
    try {
      List<Future<Boolean>> futures = new ArrayList<>();
      for (PersistenceService ps : persistenceServices) {
        Future<Boolean> future =
            executorService.submit(
                () -> {
                  try {
                    if (!ps.isRunning()) {
                      ps.start();
                    }
                  } catch (Exception e) {
                    exceptions.add(e);
                  }
                  return true;
                });
        futures.add(future);
      }

      futures.forEach(
          future -> {
            try {
              future.get();
            } catch (Exception e) {
              exceptions.add(e);
            }
          });

      exceptions.forEach(exceptionBuilder::add);
      exceptionBuilder.throwRuntimeExceptionIfHasCauses(
          "multiple exception occurred while starting the persistence service");
    } finally {
      executorService.shutdown();
    }
  }

  /** {@inheritDoc} */
  // @Override
  public void stopAllPersistenceServices() {
    AggregatedException.Builder exceptionBuilder = new AggregatedException.Builder();
    for (PersistenceService ps : persistenceServices) {
      try {
        ps.stop();
      } catch (Exception e) {
        exceptionBuilder.add(e);
      }
    }
    exceptionBuilder.throwRuntimeExceptionIfHasCauses(
        "multiple exception occurred while stopping the persistence service");
  }

  /** {@inheritDoc} */
  // @Override
  public void beginAllUnitsOfWork() {
    AggregatedException.Builder exceptionBuilder = new AggregatedException.Builder();
    for (UnitOfWork unitOfWork : unitsOfWork) {
      try {
        unitOfWork.begin();
      } catch (Exception e) {
        exceptionBuilder.add(e);
      }
    }
    exceptionBuilder.throwRuntimeExceptionIfHasCauses(
        "multiple exception occurred while starting the unit of work");
  }

  /** {@inheritDoc} */
  // @Override
  public void beginAllInactiveUnitsOfWork() {
    AggregatedException.Builder exceptionBuilder = new AggregatedException.Builder();
    for (UnitOfWork unitOfWork : unitsOfWork) {
      try {
        if (!unitOfWork.isActive()) {
          unitOfWork.begin();
        }
      } catch (Exception e) {
        exceptionBuilder.add(e);
      }
    }
    exceptionBuilder.throwRuntimeExceptionIfHasCauses(
        "multiple exception occurred while starting the unit of work");
  }

  /** {@inheritDoc} */
  // @Override
  public void endAllUnitsOfWork() {
    AggregatedException.Builder exceptionBuilder = new AggregatedException.Builder();
    for (UnitOfWork unitOfWork : unitsOfWork) {
      try {
        unitOfWork.end();
      } catch (Exception e) {
        exceptionBuilder.add(e);
      }
    }
    exceptionBuilder.throwRuntimeExceptionIfHasCauses(
        "multiple exception occurred while ending the unit of work");
  }

  /** {@inheritDoc} */
  // @Override
  public List<EntityManager> getAllEntityManagers() {
    return unitsOfWork.stream().map(UnitOfWork::getEntityManager).collect(Collectors.toList());
  }
}
