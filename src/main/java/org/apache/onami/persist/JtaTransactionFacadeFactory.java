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
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Factory for transaction facades in case of JTA transactions.
 */
@Singleton
class JtaTransactionFacadeFactory implements TransactionFacadeFactory {

  /**
   * The facade to the user transaction.
   */
  private final UserTransactionFacade utFacade;

  /**
   * Provider for the entity manager.
   * The entity manager will be joined to the the transaction.
   */
  private final EntityManagerProvider emProvider;

  /**
   * The outermost transaction facade associated with each thread, if any.
   */
  private final ThreadLocal<Outer> outerTransactionFacade = new ThreadLocal<>();

  /**
   * Constructor.
   *
   * @param utFacade   the user transaction facade.
   * @param emProvider the entity manager provider.
   */
  @Inject
  public JtaTransactionFacadeFactory(UserTransactionFacade utFacade, EntityManagerProvider emProvider) {
    this.utFacade = checkNotNull(utFacade, "utFacade is mandatory!");
    this.emProvider = checkNotNull(emProvider, "emProvider is mandatory!");
  }

  /**
   * {@inheritDoc}
   */
  // @Override
  public TransactionFacade createTransactionFacade() {
    if (utFacade.isActive()) {
      return new Inner(utFacade, emProvider.get(), outerTransactionFacade.get());
    } else {
      Outer outer = new Outer(utFacade, emProvider.get());
      outerTransactionFacade.set(outer);
      return outer;
    }
  }

  /**
   * TransactionFacade representing an inner (nested) transaction. Starting and
   * committing a transaction has no effect. This Facade will set the
   * rollbackOnly flag on the underlying transaction in case of a rollback.
   */
  private static class Inner implements TransactionFacade {
    private final UserTransactionFacade txn;

    private final EntityManager em;

    private final TransactionFacade parent;

    Inner(UserTransactionFacade txn, EntityManager em, @Nullable TransactionFacade parent) {
      this.txn = checkNotNull(txn, "txn is mandatory!");
      this.em = checkNotNull(em, "em is mandatory!");
      this.parent = parent;
    }

    /**
     * {@inheritDoc}
     */
    // @Override
    public void begin() {
      em.joinTransaction();
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
   * TransactionFacade representing an outer transaction. This Facade starts
   * and ends the transaction. If an inner transaction has set the rollbackOnly
   * flag the transaction will be rolled back in any case.
   */
  private static class Outer implements TransactionFacade {
    private final UserTransactionFacade txn;

    private final EntityManager em;

    private final List<Runnable> postCommitCallbacks = new ArrayList<>();

    Outer(UserTransactionFacade txn, EntityManager em) {
      this.txn = checkNotNull(txn, "txn is mandatory!");
      this.em = checkNotNull(em, "em is mandatory!");
    }

    /**
     * {@inheritDoc}
     */
    // @Override
    public void begin() {
      txn.begin();
      em.joinTransaction();
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
