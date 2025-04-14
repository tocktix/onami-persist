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


import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * Interface for aggregation of multiple {@link UnitOfWork UnitsOfWork}.
 */
public interface AllUnitsOfWork {

  /**
   * Calls {@link UnitOfWork#begin()} on all units of work which are not active.
   */
  void beginAllInactiveUnitsOfWork();

    /**
   * Calls {@link UnitOfWork#begin()} on all units of works, none of the units should be running before this call.
   */
  void beginAllUnitsOfWork();

    /*
   * @return the {@link EntityManager}.
   * @throws IllegalStateException if {@link UnitOfWork#isActive()} returns false.
   */
  List<EntityManager> getAllEntityManagers() throws IllegalStateException;

  /**
   * Calls {@link UnitOfWork#end()} on all units of work.
   */
  void endAllUnitsOfWork();

}
