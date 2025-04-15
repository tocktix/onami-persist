package org.apache.onami.persist.test;

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

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

/**
 * Entities which can be created during transaction tests.
 * The ID will be unique in every run of a test.
 */
@Entity
public final class TestEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  private UUID id = UUID.randomUUID();

  @Id
  public UUID getId() {
    return id;
  }

  @SuppressWarnings("unused")
  // used by the persistence framework
  private void setId(UUID id) {
    this.id = id;
  }

}
