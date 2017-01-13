package org.apache.onami.persist.test.transaction.testframework.tasks;

import org.apache.onami.persist.TransactionHookManager;
import org.apache.onami.persist.Transactional;
import org.apache.onami.persist.test.TestEntity;
import org.apache.onami.persist.test.transaction.SpyBox;
import org.apache.onami.persist.test.transaction.testframework.TransactionalTask;
import org.apache.onami.persist.test.transaction.testframework.exceptions.RuntimeTestException;
import org.apache.onami.persist.test.transaction.testframework.exceptions.TestException;

import javax.inject.Inject;

public class TaskThatUpdatesSpyValueWithPostCommitHookAndThenRollsBack extends TransactionalTask {

  private final TransactionHookManager transactionHookManager;
  private final SpyBox spyValue;

  @Inject
  TaskThatUpdatesSpyValueWithPostCommitHookAndThenRollsBack(TransactionHookManager transactionHookManager,
      SpyBox spyValue) {
    this.transactionHookManager = transactionHookManager;
    this.spyValue = spyValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(rollbackOn = TestException.class, onUnits = {})
  public void doTransactional() throws TestException, RuntimeTestException {
    storeEntity(new TestEntity());
    transactionHookManager.addPostCommitCallback(spyValue);
    doOtherTasks();
    throw new TestException(getClass().getSimpleName());
  }

}
