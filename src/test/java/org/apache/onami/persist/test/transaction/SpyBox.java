package org.apache.onami.persist.test.transaction;

import org.apache.onami.persist.StatefulTransactionHook;

/**
 * Simple container that can be used to verify that a StatefulTransactionHook ran.
 */
public class SpyBox implements StatefulTransactionHook {
  private boolean preCommit = false;
  private boolean postCommit = false;

  public boolean getPreCommit() {
    return preCommit;
  }

  public boolean getPostCommit() {
    return postCommit;
  }


  @Override
  public void preCommit() {
    preCommit = true;
  }

  @Override
  public void postCommit() {
    postCommit = true;
  }
}
