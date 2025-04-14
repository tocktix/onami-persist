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

import javax.inject.Provider;
import jakarta.persistence.EntityManager;

/**
 * Provider for {@link EntityManager}.
 * <p>
 * Even though this class extends the {@link Provider} interface it is not bound as a provider of
 * {@link EntityManager}. The reason for this is that the {@link EntityManager} objects do have a
 * life cycle and should therefore not be stored in instance/member variables.
 * <p>
 * The {@link UnitOfWork} defines the life cycle of the {@link EntityManager}. An entity manager
 * will be created when the unit of work is started. It is open and valid for use during the entire
 * unit of work. When the unit of work ends the entity manager is closed and can no longer
 * be used.
 * <p>
 * It is a good practice to store this provider in a instance/member variable and only obtain an
 * {@link EntityManager} instance in a method where it is used. This ensures that the method always
 * has access to a valid {@link EntityManager}.
 * <p>
 * The {@link EntityManagerProvider} is thread save.
 */
public interface EntityManagerProvider extends Provider<EntityManager> {

  /**
   * @return the {@link EntityManager}.
   * @throws IllegalStateException if {@link UnitOfWork#isActive()} returns false.
   */
  EntityManager get() throws IllegalStateException;

}
