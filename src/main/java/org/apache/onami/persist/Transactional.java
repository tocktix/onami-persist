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

import com.google.inject.Injector;
import com.google.inject.Key;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method or class to be executed within a transaction.
 * <p>
 * This will span a new transaction around the method unless there is already a running transaction.
 * In the case that there is a running transaction no new transaction is started.
 * If a rollback happens for a method which did not start the transaction the already existing
 * transaction will be marked as rollbackOnly.
 * <p>
 * Guice uses AOP to enhance a method annotated with {@link Transactional @Transactional} with a wrapper.
 * This means the {@link Transactional @Transactional} only works as expected when:
 * <ul>
 * <li>
 * The object on which the method is called has been created by guice.<br/>
 * This can be achieved by having it (or a {@link Provider}) injected into your class
 * or by calling {@link Injector#getInstance(Class)} or {@link Injector#getInstance(Key)}.
 * </li>
 * <li>
 * The method which should be run transactional is not private, not static and not final.
 * </li>
 * </ul>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Transactional {
  /**
   * A List of annotations for persistence units on which to start a transaction.
   * The caller can specify {} for un-annotated units.
   */
  Class<? extends Annotation>[] onUnits();

  /**
   * A list of exceptions to rollback on. Default is {@link Throwable}.
   */
  Class<? extends Throwable>[] rollbackOn() default Throwable.class;

  /**
   * A list of exceptions to <b>not<b> rollback on. Use this to exclude one ore more subclasses of
   * the exceptions defined in rollbackOn(). Default is none.
   */
  Class<? extends Throwable>[] ignore() default {};
}
