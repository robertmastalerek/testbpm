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
package org.eximeebpms.bpm.engine.impl;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.eximeebpms.bpm.engine.FormService;
import org.eximeebpms.bpm.engine.form.StartFormData;
import org.eximeebpms.bpm.engine.form.TaskFormData;
import org.eximeebpms.bpm.engine.impl.cmd.GetDeployedStartFormCmd;
import org.eximeebpms.bpm.engine.impl.cmd.GetFormKeyCmd;
import org.eximeebpms.bpm.engine.impl.cmd.GetRenderedStartFormCmd;
import org.eximeebpms.bpm.engine.impl.cmd.GetRenderedTaskFormCmd;
import org.eximeebpms.bpm.engine.impl.cmd.GetStartFormCmd;
import org.eximeebpms.bpm.engine.impl.cmd.GetStartFormVariablesCmd;
import org.eximeebpms.bpm.engine.impl.cmd.GetTaskFormCmd;
import org.eximeebpms.bpm.engine.impl.cmd.GetTaskFormVariablesCmd;
import org.eximeebpms.bpm.engine.impl.cmd.SubmitStartFormCmd;
import org.eximeebpms.bpm.engine.impl.cmd.SubmitTaskFormCmd;
import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.engine.variable.VariableMap;


/**
 * @author Tom Baeyens
 * @author Falko Menge (camunda)
 */
public class FormServiceImpl extends ServiceImpl implements FormService {

  public Object getRenderedStartForm(String processDefinitionId) {
    return commandExecutor.execute(new GetRenderedStartFormCmd(processDefinitionId, null));
  }

  public Object getRenderedStartForm(String processDefinitionId, String engineName) {
    return commandExecutor.execute(new GetRenderedStartFormCmd(processDefinitionId, engineName));
  }

  public Object getRenderedTaskForm(String taskId) {
    return commandExecutor.execute(new GetRenderedTaskFormCmd(taskId, null));
  }

  public Object getRenderedTaskForm(String taskId, String engineName) {
    return commandExecutor.execute(new GetRenderedTaskFormCmd(taskId, engineName));
  }

  public StartFormData getStartFormData(String processDefinitionId) {
    return commandExecutor.execute(new GetStartFormCmd(processDefinitionId));
  }

  public TaskFormData getTaskFormData(String taskId) {
    return commandExecutor.execute(new GetTaskFormCmd(taskId));
  }

  public ProcessInstance submitStartFormData(String processDefinitionId, Map<String, String> properties) {
    return commandExecutor.execute(new SubmitStartFormCmd(processDefinitionId, null, (Map) properties));
  }

  public ProcessInstance submitStartFormData(String processDefinitionId, String businessKey, Map<String, String> properties) {
	  return commandExecutor.execute(new SubmitStartFormCmd(processDefinitionId, businessKey, (Map) properties));
  }

  public ProcessInstance submitStartForm(String processDefinitionId, Map<String, Object> properties) {
    return commandExecutor.execute(new SubmitStartFormCmd(processDefinitionId, null, properties));
  }

  public ProcessInstance submitStartForm(String processDefinitionId, String businessKey, Map<String, Object> properties) {
    return commandExecutor.execute(new SubmitStartFormCmd(processDefinitionId, businessKey, properties));
  }

  public void submitTaskFormData(String taskId, Map<String, String> properties) {
    submitTaskForm(taskId, (Map) properties);
  }

  public void submitTaskForm(String taskId, Map<String, Object> properties) {
    commandExecutor.execute(new SubmitTaskFormCmd(taskId, properties, false, false));
  }

  public VariableMap submitTaskFormWithVariablesInReturn(String taskId, Map<String, Object> properties, boolean deserializeValues) {
    return commandExecutor.execute(new SubmitTaskFormCmd(taskId, properties, true, deserializeValues));
  }

  public String getStartFormKey(String processDefinitionId) {
    return commandExecutor.execute(new GetFormKeyCmd(processDefinitionId));
  }

  public String getTaskFormKey(String processDefinitionId, String taskDefinitionKey) {
    return commandExecutor.execute(new GetFormKeyCmd(processDefinitionId, taskDefinitionKey));
  }

  public VariableMap getStartFormVariables(String processDefinitionId) {
    return getStartFormVariables(processDefinitionId, null, true);
  }

  public VariableMap getStartFormVariables(String processDefinitionId, Collection<String> formVariables, boolean deserializeObjectValues) {
    return commandExecutor.execute(new GetStartFormVariablesCmd(processDefinitionId, formVariables, deserializeObjectValues));
  }

  public VariableMap getTaskFormVariables(String taskId) {
    return getTaskFormVariables(taskId, null, true);
  }

  public VariableMap getTaskFormVariables(String taskId, Collection<String> formVariables, boolean deserializeObjectValues) {
    return commandExecutor.execute(new GetTaskFormVariablesCmd(taskId, formVariables, deserializeObjectValues));
  }

  public InputStream getDeployedStartForm(String processDefinitionId) {
    return commandExecutor.execute(new GetDeployedStartFormCmd(processDefinitionId));
  }

  public InputStream getDeployedTaskForm(String taskId) {
    return commandExecutor.execute(new GetDeployedTaskFormCmd(taskId));
  }

}
