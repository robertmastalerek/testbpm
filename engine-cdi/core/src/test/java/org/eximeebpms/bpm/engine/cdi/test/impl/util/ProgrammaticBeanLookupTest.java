/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eximeebpms.bpm.engine.cdi.test.impl.util;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.eximeebpms.bpm.engine.cdi.impl.ProcessEngineServicesProducer;
import org.eximeebpms.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.eximeebpms.bpm.engine.cdi.test.impl.beans.SpecializedTestBean;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Ronny Bräunlich
 *
 */
@RunWith(Arquillian.class)
public class ProgrammaticBeanLookupTest {

  /**
   * Because of all alternatives and specializations I have to handle deployment myself
   */
  @ArquillianResource
  private Deployer deployer;

  @Deployment(name = "normal", managed = false)
  public static JavaArchive createDeployment() {
    return ShrinkWrap.create(JavaArchive.class)
        .addClass(ProgrammaticBeanLookup.class)
        .addClass(ProcessEngineServicesProducer.class)
        .addAsManifestResource("org/eximeebpms/bpm/engine/cdi/test/impl/util/beans.xml", "beans.xml");
  }

  @Deployment(name = "withAlternative", managed = false)
  public static JavaArchive createDeploymentWithAlternative() {
    return ShrinkWrap.create(JavaArchive.class)
        .addClass(ProgrammaticBeanLookup.class)
        .addClass(ProcessEngineServicesProducer.class)
        .addClass(AlternativeTestBean.class)
        .addAsManifestResource("org/eximeebpms/bpm/engine/cdi/test/impl/util/beansWithAlternative.xml", "beans.xml");
  }

  @Deployment(name = "withSpecialization", managed = false)
  public static JavaArchive createDeploymentWithSpecialization() {
    return ShrinkWrap.create(JavaArchive.class)
        .addClass(ProgrammaticBeanLookup.class)
        .addClass(ProcessEngineServicesProducer.class)
        .addClass(SpecializedTestBean.class)
        .addAsManifestResource("org/eximeebpms/bpm/engine/cdi/test/impl/util/beans.xml", "beans.xml");
  }

  @Deployment(name = "withProducerMethod", managed = false)
  public static JavaArchive createDeploymentWithProducerMethod() {
    return ShrinkWrap.create(JavaArchive.class)
        .addClass(ProgrammaticBeanLookup.class)
        .addClass(ProcessEngineServicesProducer.class)
        .addClass(BeanWithProducerMethods.class)
        .addAsManifestResource("org/eximeebpms/bpm/engine/cdi/test/impl/util/beans.xml", "beans.xml");
  }

  @Test
  public void testLookupBean() {
    deployer.deploy("normal");
    Object lookup = ProgrammaticBeanLookup.lookup("testOnly");
    assertThat(lookup, instanceOf(TestBean.class));
    deployer.undeploy("normal");
  }

  @Test
  public void testLookupShouldFindAlternative() {
    deployer.deploy("withAlternative");
    Object lookup = ProgrammaticBeanLookup.lookup("testOnly");
    assertThat(lookup.getClass().getName(),
        is(equalTo(AlternativeTestBean.class.getName())));
    deployer.undeploy("withAlternative");
  }

  @Test
  public void testLookupShouldFindSpecialization() {
    deployer.deploy("withSpecialization");
    Object lookup = ProgrammaticBeanLookup.lookup("testOnly");
    assertThat(lookup.getClass().getName(),
        is(equalTo(SpecializedTestBean.class.getName())));
    deployer.undeploy("withSpecialization");
  }

  @Test
  public void testLookupShouldSupportProducerMethods() {
    deployer.deploy("withProducerMethod");
    assertEquals("exampleString", ProgrammaticBeanLookup.lookup("producedString"));
    deployer.undeploy("withProducerMethod");
  }

  @Named("testOnly")
  public static class TestBean extends Object {
  }

  @Alternative
  @Named("testOnly")
  public static class AlternativeTestBean extends TestBean {
  }
}