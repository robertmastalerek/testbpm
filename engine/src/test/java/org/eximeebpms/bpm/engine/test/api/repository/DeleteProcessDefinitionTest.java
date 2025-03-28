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
package org.eximeebpms.bpm.engine.test.api.repository;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eximeebpms.bpm.engine.test.api.repository.RedeploymentTest.DEPLOYMENT_NAME;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eximeebpms.bpm.engine.HistoryService;
import org.eximeebpms.bpm.engine.ManagementService;
import org.eximeebpms.bpm.engine.ProcessEngineException;
import org.eximeebpms.bpm.engine.RepositoryService;
import org.eximeebpms.bpm.engine.RuntimeService;
import org.eximeebpms.bpm.engine.delegate.ExecutionListener;
import org.eximeebpms.bpm.engine.exception.NotFoundException;
import org.eximeebpms.bpm.engine.exception.NullValueException;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.history.HistoryLevel;
import org.eximeebpms.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.eximeebpms.bpm.engine.repository.Deployment;
import org.eximeebpms.bpm.engine.repository.DeploymentWithDefinitions;
import org.eximeebpms.bpm.engine.repository.ProcessDefinition;
import org.eximeebpms.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.engine.test.api.runtime.util.IncrementCounterListener;
import org.eximeebpms.bpm.engine.test.util.ProcessEngineTestRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.eximeebpms.bpm.model.bpmn.Bpmn;
import org.eximeebpms.bpm.model.bpmn.BpmnModelInstance;
import org.eximeebpms.commons.utils.cache.Cache;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class DeleteProcessDefinitionTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  @Rule
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  protected HistoryService historyService;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected Deployment deployment;

  @Before
  public void initServices() {
    historyService = engineRule.getHistoryService();
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    processEngineConfiguration = (ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration();
  }

  @After
  public void cleanUp() {
    if (deployment != null) {
      repositoryService.deleteDeployment(deployment.getId(), true);
      deployment = null;
    }
  }

  protected static final String IO_MAPPING_PROCESS_KEY = "ioMappingProcess";
  protected static final BpmnModelInstance IO_MAPPING_PROCESS = Bpmn.createExecutableProcess(IO_MAPPING_PROCESS_KEY)
    .startEvent()
    .userTask()
      .camundaOutputParameter("inputParameter", "${notExistentVariable}")
    .endEvent()
    .done();

  @Test
  public void testDeleteProcessDefinitionNullId() {

    // when/then
    assertThatThrownBy(() -> repositoryService.deleteProcessDefinition(null))
      .isInstanceOf(NullValueException.class)
      .hasMessageContaining("processDefinitionId is null");
  }

  @Test
  public void testDeleteNonExistingProcessDefinition() {

    // when/then
    assertThatThrownBy(() -> repositoryService.deleteProcessDefinition("notexist"))
      .isInstanceOf(NotFoundException.class)
      .hasMessageContaining("No process definition found with id 'notexist': processDefinition is null");
  }

  @Test
  public void testDeleteProcessDefinition() {
    // given deployment with two process definitions in one xml model file
    deployment = repositoryService.createDeployment()
            .addClasspathResource("org/eximeebpms/bpm/engine/test/repository/twoProcesses.bpmn20.xml")
            .deploy();
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();

    //when a process definition is been deleted
    repositoryService.deleteProcessDefinition(processDefinitions.get(0).getId());

    //then only one process definition should remain
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
  }

  @Test
  public void testDeleteProcessDefinitionWithProcessInstance() {
    // given process definition and a process instance
    BpmnModelInstance bpmnModel = Bpmn.createExecutableProcess("process").startEvent().userTask().endEvent().done();
    deployment = repositoryService.createDeployment()
                                  .addModelInstance("process.bpmn", bpmnModel)
                                  .deploy();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("process").singleResult();
    runtimeService.createProcessInstanceByKey("process").executeWithVariablesInReturn();

    //when the corresponding process definition is deleted from the deployment
    try {
      repositoryService.deleteProcessDefinition(processDefinition.getId());
      fail("Should fail, since there exists a process instance");
    } catch (ProcessEngineException pex) {
      // then Exception is expected, the deletion should fail since there exist a process instance
      // and the cascade flag is per default false
      assertTrue(pex.getMessage().contains("Deletion of process definition without cascading failed."));
    }
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
  }

  @Test
  public void testDeleteProcessDefinitionCascade() {
    // given process definition and a process instance
    BpmnModelInstance bpmnModel = Bpmn.createExecutableProcess("process").startEvent().userTask().endEvent().done();
    deployment = repositoryService.createDeployment()
                                  .addModelInstance("process.bpmn", bpmnModel)
                                  .deploy();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("process").singleResult();
    runtimeService.createProcessInstanceByKey("process").executeWithVariablesInReturn();

    //when the corresponding process definition is cascading deleted from the deployment
    repositoryService.deleteProcessDefinition(processDefinition.getId(), true);

    //then exist no process instance and no definition
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().count());
    if (processEngineConfiguration.getHistoryLevel().getId() >= HistoryLevel.HISTORY_LEVEL_ACTIVITY.getId()) {
      assertEquals(0, engineRule.getHistoryService().createHistoricActivityInstanceQuery().count());
    }
  }

  @Test
  public void testDeleteProcessDefinitionClearsCache() {
    // given process definition and a process instance
    BpmnModelInstance bpmnModel = Bpmn.createExecutableProcess("process").startEvent().userTask().endEvent().done();
    deployment = repositoryService.createDeployment()
                                  .addModelInstance("process.bpmn", bpmnModel)
                                  .deploy();
    String processDefinitionId = repositoryService.createProcessDefinitionQuery()
                                                  .processDefinitionKey("process")
                                                  .singleResult()
                                                  .getId();

    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();

    // ensure definitions and models are part of the cache
    assertNotNull(deploymentCache.getProcessDefinitionCache().get(processDefinitionId));
    assertNotNull(deploymentCache.getBpmnModelInstanceCache().get(processDefinitionId));

    repositoryService.deleteProcessDefinition(processDefinitionId, true);

    // then the definitions and models are removed from the cache
    assertNull(deploymentCache.getProcessDefinitionCache().get(processDefinitionId));
    assertNull(deploymentCache.getBpmnModelInstanceCache().get(processDefinitionId));
  }

  @Test
  public void testDeleteProcessDefinitionAndRefillDeploymentCache() {
    // given a deployment with two process definitions in one xml model file
    deployment = repositoryService.createDeployment()
            .addClasspathResource("org/eximeebpms/bpm/engine/test/repository/twoProcesses.bpmn20.xml")
            .deploy();
    ProcessDefinition processDefinitionOne =
        repositoryService.createProcessDefinitionQuery().processDefinitionKey("one").singleResult();
    ProcessDefinition processDefinitionTwo =
        repositoryService.createProcessDefinitionQuery().processDefinitionKey("two").singleResult();

    String idOne = processDefinitionOne.getId();
    //one is deleted from the deployment
    repositoryService.deleteProcessDefinition(idOne);

    //when clearing the deployment cache
    processEngineConfiguration.getDeploymentCache().discardProcessDefinitionCache();

    //then creating process instance from the existing process definition
    ProcessInstanceWithVariables procInst = runtimeService.createProcessInstanceByKey("two").executeWithVariablesInReturn();
    assertNotNull(procInst);
    assertTrue(procInst.getProcessDefinitionId().contains("two"));

    //should refill the cache
    Cache cache = processEngineConfiguration.getDeploymentCache().getProcessDefinitionCache();
    assertNotNull(cache.get(processDefinitionTwo.getId()));
    //The deleted process definition should not be recreated after the cache is refilled
    assertNull(cache.get(processDefinitionOne.getId()));
  }

  @Test
  public void testDeleteProcessDefinitionAndRedeploy() {
    // given a deployment with two process definitions in one xml model file
    deployment = repositoryService.createDeployment()
            .addClasspathResource("org/eximeebpms/bpm/engine/test/repository/twoProcesses.bpmn20.xml")
            .deploy();

    ProcessDefinition processDefinitionOne =
        repositoryService.createProcessDefinitionQuery().processDefinitionKey("one").singleResult();

    //one is deleted from the deployment
    repositoryService.deleteProcessDefinition(processDefinitionOne.getId());

    //when the process definition is redeployed
    Deployment deployment2 = repositoryService.createDeployment()
            .name(DEPLOYMENT_NAME)
            .addDeploymentResources(deployment.getId())
            .deploy();

    //then there should exist three process definitions
    //two of the redeployment and the remaining one
    assertEquals(3, repositoryService.createProcessDefinitionQuery().count());

    //clean up
    repositoryService.deleteDeployment(deployment2.getId(), true);
  }

  @Test
  public void shouldRestorePreviousStartTimerDefinitions() {
    // given
    BpmnModelInstance processV1 = Bpmn.createExecutableProcess()
        .id("one")
        .startEvent()
        .timerWithCycle("R/PT15M")
        .userTask("aTaskName")
        .endEvent()
        .done();

    BpmnModelInstance processV2 = Bpmn.createExecutableProcess()
        .id("one")
        .startEvent()
        .endEvent()
        .done();

    testHelper.deploy(processV1);
    DeploymentWithDefinitions deployment = testHelper.deploy(processV2);

    //when
    repositoryService.deleteProcessDefinition(deployment.getDeployedProcessDefinitions().get(0).getId());

    //then
    long timerDefinitions = managementService.createJobQuery().processDefinitionKey("one").count();

    assertThat(timerDefinitions).isEqualTo(1);
  }

  @Test
  public void testDeleteProcessDefinitionsByNotExistingKey() {

    // when/then
    assertThatThrownBy(() -> repositoryService.deleteProcessDefinitions()
        .byKey("no existing key")
        .withoutTenantId()
        .delete())
      .isInstanceOf(NotFoundException.class)
      .hasMessageContaining("No process definition found");
  }

  @Test
  public void testDeleteProcessDefinitionsByKeyIsNull() {

    // when/then
    assertThatThrownBy(() -> repositoryService.deleteProcessDefinitions()
        .byKey(null)
        .withoutTenantId()
        .delete())
      .isInstanceOf(NullValueException.class)
      .hasMessageContaining("cannot be null");

  }

  @Test
  public void testDeleteProcessDefinitionsByKey() {
    // given
    for (int i = 0; i < 3; i++) {
      deployTwoProcessDefinitions();
    }

    // when
    repositoryService.deleteProcessDefinitions()
      .byKey("processOne")
      .withoutTenantId()
      .delete();

    // then
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(3L);
  }

  @Test
  public void testDeleteProcessDefinitionsByKeyWithRunningProcesses() {
    // given
    for (int i = 0; i < 3; i++) {
      deployTwoProcessDefinitions();
    }
    runtimeService.startProcessInstanceByKey("processOne");

    // when/then
    assertThatThrownBy(() -> repositoryService.deleteProcessDefinitions()
        .byKey("processOne")
        .withoutTenantId()
        .delete())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Deletion of process definition");
  }

  @Test
  public void testDeleteProcessDefinitionsByKeyCascading() {
    // given
    for (int i = 0; i < 3; i++) {
      deployTwoProcessDefinitions();
    }

    Map<String, Object> variables = new HashMap<String, Object>();

    for (int i = 0; i < 3; i++) {
      variables.put("varName" + i, "varValue");
    }

    for (int i = 0; i < 3; i++) {
      runtimeService.startProcessInstanceByKey("processOne", variables);
      runtimeService.startProcessInstanceByKey("processTwo", variables);
    }

    // when
    repositoryService.deleteProcessDefinitions()
      .byKey("processOne")
      .withoutTenantId()
      .cascade()
      .delete();

    repositoryService.deleteProcessDefinitions()
      .byKey("processTwo")
      .withoutTenantId()
      .cascade()
      .delete();

    // then
    assertThat(historyService.createHistoricVariableInstanceQuery().count()).isEqualTo(0L);
    assertThat(historyService.createHistoricProcessInstanceQuery().count()).isEqualTo(0L);
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(0L);
  }

  @Test
  public void testDeleteProcessDefinitionsByKeyWithCustomListenersSkipped() {
    // given
    IncrementCounterListener.counter = 0;
    for (int i = 0; i < 3; i++) {
      deployTwoProcessDefinitions();
    }

    runtimeService.startProcessInstanceByKey("processOne");

    // when
    repositoryService.deleteProcessDefinitions()
      .byKey("processOne")
      .withoutTenantId()
      .cascade()
      .skipCustomListeners()
      .delete();

    // then
    assertThat(IncrementCounterListener.counter).isEqualTo(0);
  }

  @Test
  public void testDeleteProcessDefinitionsByKeyWithIoMappingsSkipped() {
    // given
    testHelper.deploy(IO_MAPPING_PROCESS);
    runtimeService.startProcessInstanceByKey(IO_MAPPING_PROCESS_KEY);

    testHelper.deploy(IO_MAPPING_PROCESS);
    runtimeService.startProcessInstanceByKey(IO_MAPPING_PROCESS_KEY);

    // when
    repositoryService.deleteProcessDefinitions()
      .byKey(IO_MAPPING_PROCESS_KEY)
      .withoutTenantId()
      .cascade()
      .skipIoMappings()
      .delete();

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();

    // then
    assertThat(processDefinitions.size()).isEqualTo(0);
  }

  @Test
  public void testDeleteProcessDefinitionsByNotExistingIds() {

    // when/then
    assertThatThrownBy(() -> repositoryService.deleteProcessDefinitions()
        .byIds("not existing", "also not existing")
        .delete())
      .isInstanceOf(NotFoundException.class)
      .hasMessageContaining("No process definition found");
  }

  @Test
  public void testDeleteProcessDefinitionsByIdIsNull() {

    // when/then
    assertThatThrownBy(() -> repositoryService.deleteProcessDefinitions()
        .byIds(null)
        .delete())
      .isInstanceOf(NullValueException.class)
      .hasMessageContaining("cannot be null");
  }

  @Test
  public void testDeleteProcessDefinitionsByIds() {
    // given
    for (int i = 0; i < 3; i++) {
      deployTwoProcessDefinitions();
    }

    String[] processDefinitionIds = findProcessDefinitionIdsByKey("processOne");

    // when
    repositoryService.deleteProcessDefinitions()
      .byIds(processDefinitionIds)
      .delete();

    // then
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(3L);
  }

  @Test
  public void testDeleteProcessDefinitionsByIdsWithRunningProcesses() {
    // given
    for (int i = 0; i < 3; i++) {
      deployTwoProcessDefinitions();
    }
    String[] processDefinitionIds = findProcessDefinitionIdsByKey("processOne");
    runtimeService.startProcessInstanceByKey("processOne");


    // when/then
    assertThatThrownBy(() -> repositoryService.deleteProcessDefinitions()
        .byIds(processDefinitionIds)
        .delete())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Deletion of process definition");

  }

  @Test
  public void testDeleteProcessDefinitionsByIdsCascading() {
    // given
    for (int i = 0; i < 3; i++) {
      deployTwoProcessDefinitions();
    }
    String[] processDefinitionIdsOne = findProcessDefinitionIdsByKey("processOne");
    String[] processDefinitionIdsTwo = findProcessDefinitionIdsByKey("processTwo");
    Map<String, Object> variables = new HashMap<String, Object>();

    for (int i = 0; i < 3; i++) {
      variables.put("varName" + i, "varValue");
    }

    for (int i = 0; i < 3; i++) {
      runtimeService.startProcessInstanceByKey("processOne", variables);
      runtimeService.startProcessInstanceByKey("processTwo", variables);
    }

    // when
    repositoryService.deleteProcessDefinitions()
      .byIds(processDefinitionIdsOne)
      .cascade()
      .delete();

    repositoryService.deleteProcessDefinitions()
      .byIds(processDefinitionIdsTwo)
      .cascade()
      .delete();

    // then
    assertThat(historyService.createHistoricVariableInstanceQuery().count()).isEqualTo(0L);
    assertThat(historyService.createHistoricProcessInstanceQuery().count()).isEqualTo(0L);
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(0L);
  }

  @Test
  public void testDeleteProcessDefinitionsByIdsWithCustomListenersSkipped() {
    // given
    IncrementCounterListener.counter = 0;
    for (int i = 0; i < 3; i++) {
      deployTwoProcessDefinitions();
    }
    String[] processDefinitionIds = findProcessDefinitionIdsByKey("processOne");
    runtimeService.startProcessInstanceByKey("processOne");

    // when
    repositoryService.deleteProcessDefinitions()
      .byIds(processDefinitionIds)
      .cascade()
      .skipCustomListeners()
      .delete();

    // then
    assertThat(IncrementCounterListener.counter).isEqualTo(0);
  }

  @Test
  public void testDeleteProcessDefinitionsByIdsWithIoMappingsSkipped() {
    // given
    testHelper.deploy(IO_MAPPING_PROCESS);
    runtimeService.startProcessInstanceByKey(IO_MAPPING_PROCESS_KEY);

    testHelper.deploy(IO_MAPPING_PROCESS);
    runtimeService.startProcessInstanceByKey(IO_MAPPING_PROCESS_KEY);

    String[] processDefinitionIds = findProcessDefinitionIdsByKey(IO_MAPPING_PROCESS_KEY);

    // when
    repositoryService.deleteProcessDefinitions()
      .byIds(processDefinitionIds)
      .cascade()
      .skipIoMappings()
      .delete();

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();

    // then
    assertThat(processDefinitions.size()).isEqualTo(0);
  }

  private void deployTwoProcessDefinitions() {
    testHelper.deploy(
      Bpmn.createExecutableProcess("processOne")
        .startEvent()
        .userTask()
          .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, IncrementCounterListener.class.getName())
        .endEvent()
        .done(),
      Bpmn.createExecutableProcess("processTwo")
        .startEvent()
        .userTask()
        .endEvent()
        .done());
  }

  private String[] findProcessDefinitionIdsByKey(String processDefinitionKey) {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey(processDefinitionKey).list();
    List<String> processDefinitionIds = new ArrayList<String>();
    for (ProcessDefinition processDefinition: processDefinitions) {
      processDefinitionIds.add(processDefinition.getId());
    }

    return processDefinitionIds.toArray(new String[0]);
  }
}
