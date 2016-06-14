package org.apache.onami.persist;

interface TransactionStateObserver {
  interface TransactionHolder extends AutoCloseable {
    TransactionFacade getTransaction();
  }

  /**
   * Sets the current transaction for the unit of work on the current thread
   */
  TransactionHolder withTransaction(TransactionFacade transactionFacade);
}
