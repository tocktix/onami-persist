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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Scopes;
import org.apache.onami.persist.EntityManagerProvider;
import org.apache.onami.persist.PersistenceFilter;
import org.apache.onami.persist.PersistenceModule;
import org.apache.onami.persist.test.multipersistenceunits.FirstPU;
import org.apache.onami.persist.test.multipersistenceunits.SecondPU;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Test which ensures that the @{link PersistenceFilter} fulfills the requirements of a guice servlet filter.
 */
public class PersistenceServletFilterTest {
  private EntityManagerProvider firstEmp;

  private EntityManagerProvider secondEmp;

  private PersistenceFilter persistenceFilter;

  private Injector injector;

  @Before
  public final void setUp() throws Exception {
    final PersistenceModule pm = createPersistenceModuleForTest();
    injector = Guice.createInjector(pm);

    persistenceFilter = injector.getInstance(Key.get(PersistenceFilter.class));
    persistenceFilter.init(mock(FilterConfig.class));

    firstEmp = injector.getInstance(Key.get(EntityManagerProvider.class, FirstPU.class));
    secondEmp = injector.getInstance(Key.get(EntityManagerProvider.class, SecondPU.class));
  }

  private PersistenceModule createPersistenceModuleForTest() {
    return new PersistenceModule() {

      @Override
      protected void configurePersistence() {
        bindApplicationManagedPersistenceUnit("firstUnit").annotatedWith(FirstPU.class);
        bindApplicationManagedPersistenceUnit("secondUnit").annotatedWith(SecondPU.class);
      }
    };
  }

  @After
  public final void tearDown() throws Exception {
    persistenceFilter.destroy();
  }


  @Test
  public void persistenceFilterShouldBeSingleton() {
    assertThat(isSingleton(PersistenceFilter.class), is(true));
  }

  private boolean isSingleton(Class<?> type) {
    return Scopes.isSingleton(injector.getBinding(type));
  }

  @Test
  public void shouldFilter() throws Exception {
    // given
    final ServletRequest request = mock(ServletRequest.class);
    final ServletResponse response = mock(ServletResponse.class);
    final FilterChain filterChain = mock(FilterChain.class);
    doAnswer(new ServletMock()).when(filterChain)
        .doFilter(request, response);

    // when
    persistenceFilter.doFilter(request, response, filterChain);
  }

  private class ServletMock implements Answer<Void> {

    public Void answer(InvocationOnMock invocation) throws Throwable {
      // given
      final TestEntity firstEntity = new TestEntity();
      final TestEntity secondEntity = new TestEntity();

      // when
      firstEmp.get()
          .persist(firstEntity);
      secondEmp.get()
          .persist(secondEntity);

      // then
      assertNotNull(firstEmp.get()
          .find(TestEntity.class, firstEntity.getId()));
      assertNotNull(secondEmp.get()
          .find(TestEntity.class, secondEntity.getId()));
      assertNull(firstEmp.get()
          .find(TestEntity.class, secondEntity.getId()));
      assertNull(secondEmp.get()
          .find(TestEntity.class, firstEntity.getId()));

      return null;
    }
  }
}
