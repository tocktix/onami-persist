package org.apache.onami.persist;

public interface TransactionHookManager {
  /**
   * Adds a stateful callback to run upon successful completion of the current transaction before commit and after.
   * @param callback the callback to run
   */
  void addPostCommitCallback(StatefulTransactionHook callback);
}
