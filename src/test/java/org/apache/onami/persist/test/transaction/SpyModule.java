package org.apache.onami.persist.test.transaction;

import com.google.inject.AbstractModule;

/**
 * Module that adds a configured {@link SpyBox} to an injector.
 */
final class SpyModule extends AbstractModule {
  private final SpyBox spyValue;
  SpyModule(SpyBox spyValue) {
    this.spyValue = spyValue;
  }

  @Override
  protected void configure() {
    bind(SpyBox.class).toInstance(spyValue);
  }
}
