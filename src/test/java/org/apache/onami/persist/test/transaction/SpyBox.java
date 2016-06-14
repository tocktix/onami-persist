package org.apache.onami.persist.test.transaction;

/**
 * Simple container that can be used to verify that a callback ran.
 */
public class SpyBox {
  private boolean value = false;
  public void setToTrue() {
    value = true;
  }

  public boolean getValue() {
    return value;
  }
}
