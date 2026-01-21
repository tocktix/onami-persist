package org.apache.onami.persist.test.transaction.testframework.tasks;

import org.apache.onami.persist.TransactionHookManager;
import org.apache.onami.persist.Transactional;
import org.apache.onami.persist.test.TestEntity;
import org.apache.onami.persist.test.transaction.SpyBox;
import org.apache.onami.persist.test.transaction.testframework.TransactionalTask;
import org.apache.onami.persist.test.transaction.testframework.exceptions.RuntimeTestException;
import org.apache.onami.persist.test.transaction.testframework.exceptions.TestException;

import jakarta.inject.Inject;

public class TaskThatUpdatesSpyValueWithPostCommitHook extends TransactionalTask {

  private final TransactionHookManager transactionHookManager;
  private final SpyBox spyValue;

  @Inject
  TaskThatUpdatesSpyValueWithPostCommitHook(TransactionHookManager transactionHookManager,
      SpyBox spyValue) {
    this.transactionHookManager = transactionHookManager;
    this.spyValue = spyValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(ignore = Exception.class, onUnits = {})
  public void doTransactional() throws TestException, RuntimeTestException {
    storeEntity(new TestEntity());
    transactionHookManager.addPostCommitCallback(spyValue::setToTrue);
    doOtherTasks();
  }
}
