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

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import jakarta.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Factory for transaction facades in case of resource local transactions.
 */
@Singleton
class ResourceLocalTransactionFacadeFactory implements TransactionFacadeFactory {

  /**
   * The provider for the entity manager.
   */
  private final EntityManagerProvider emProvider;

  private final ThreadLocal<Outer> outerTransactionFacade = new ThreadLocal<>();

  /**
   * Constructor.
   *
   * @param emProvider the provider for the entity manager
   */
  @Inject
  ResourceLocalTransactionFacadeFactory(EntityManagerProvider emProvider) {
    this.emProvider = checkNotNull(emProvider, "emProvider is mandatory!");
  }

  /**
   * {@inheritDoc}
   */
  // @Override
  public TransactionFacade createTransactionFacade() {
    final EntityTransaction txn = emProvider.get()
        .getTransaction();
    if (txn.isActive()) {
      return new Inner(txn, outerTransactionFacade.get());
    } else {
      Outer outer = new Outer(txn);
      outerTransactionFacade.set(outer);
      return outer;
    }
  }

  /**
   * TransactionFacade representing an inner (nested) transaction.
   * Starting and committing a transaction has no effect.
   * This facade will set the rollbackOnly flag in case of a roll back.
   */
  private static class Inner implements TransactionFacade {
    private final EntityTransaction txn;

    private final TransactionFacade parent;

    Inner(EntityTransaction txn, @Nullable TransactionFacade parent) {
      this.txn = checkNotNull(txn, "txn is mandatory!");
      this.parent = parent;
    }

    /**
     * {@inheritDoc}
     */
    // @Override
    public void begin() {
      // Do nothing
    }

    /**
     * {@inheritDoc}
     */
    // @Override
    public void commit() {
      // Do nothing
    }

    /**
     * {@inheritDoc}
     */
    // @Override
    public void rollback() {
      txn.setRollbackOnly();
    }

    @Override
    public void addPostCommitCallback(Runnable callback) {
      Preconditions.checkNotNull(parent);
      parent.addPostCommitCallback(callback);
    }
  }


  /**
   * TransactionFacade representing an outer transaction.
   * This facade starts and ends the transaction.
   * If an inner transaction has set the rollbackOnly flag the transaction will be rolled back in any case.
   */
  private static class Outer implements TransactionFacade {
    private final EntityTransaction txn;

    private final List<Runnable> postCommitCallbacks = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    Outer(EntityTransaction txn) {
      this.txn = checkNotNull(txn, "txn is mandatory!");
    }

    /**
     * {@inheritDoc}
     */
    // @Override
    public void begin() {
      txn.begin();
    }

    /**
     * {@inheritDoc}
     */
    // @Override
    public synchronized void commit() {
      if (txn.getRollbackOnly()) {
        txn.rollback();
      } else {
        txn.commit();
        List<RuntimeException> exceptions = new ArrayList<>();
        for (Runnable callback : postCommitCallbacks) {
          try {
            callback.run();
          } catch (RuntimeException e) {
            exceptions.add(e);
          }
        }
        if (exceptions.size() >= 1) {
          RuntimeException e = exceptions.get(0);
          exceptions.subList(1, exceptions.size()).forEach(e::addSuppressed);
          throw e;
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    // @Override
    public void rollback() {
      txn.rollback();
    }

    @Override
    public synchronized void addPostCommitCallback(Runnable callback) {
      Preconditions.checkState(txn.isActive(), "Cannot add a commit callback with no transaction active");
      postCommitCallbacks.add(callback);
    }
  }

}
