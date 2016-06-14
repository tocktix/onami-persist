package org.apache.onami.persist;

public interface TransactionHookManager {
  /**
   * Adds a callback to run upon successful completion of the current transaction.
   * @param callback the callback to run
   */
  void addPostCommitCallback(Runnable callback);
}
