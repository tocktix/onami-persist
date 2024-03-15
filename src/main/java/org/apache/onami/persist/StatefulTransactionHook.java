package org.apache.onami.persist;

/**
 * A transaction hook that is run at the end of a @Transactional block both preCommit() and postCommit().
 */
public interface StatefulTransactionHook {
  /**
   * Call the preCommit() hook.
   */
  void preCommit();

  /**
   * Call the postCommit() hook.
   */
  void postCommit();
}
