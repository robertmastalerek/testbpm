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
package org.eximeebpms.bpm.engine.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.eximeebpms.bpm.engine.rest.dto.CountResultDto;
import org.eximeebpms.bpm.engine.rest.dto.batch.BatchDto;
import org.eximeebpms.bpm.engine.rest.dto.externaltask.ExternalTaskDto;
import org.eximeebpms.bpm.engine.rest.dto.externaltask.ExternalTaskQueryDto;
import org.eximeebpms.bpm.engine.rest.dto.externaltask.FetchExternalTasksExtendedDto;
import org.eximeebpms.bpm.engine.rest.dto.externaltask.SetRetriesForExternalTasksDto;
import org.eximeebpms.bpm.engine.rest.sub.externaltask.ExternalTaskResource;

/**
 * @author Thorben Lindhauer
 *
 */
public interface ExternalTaskRestService {

  public static final String PATH = "/external-task";

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<ExternalTaskDto> getExternalTasks(@Context UriInfo uriInfo,
      @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults);

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  List<ExternalTaskDto> queryExternalTasks(ExternalTaskQueryDto query,
      @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto getExternalTasksCount(@Context UriInfo uriInfo);

  @POST
  @Path("/count")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto queryExternalTasksCount(ExternalTaskQueryDto query);

  @POST
  @Path("/fetchAndLock")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  void fetchAndLock(FetchExternalTasksExtendedDto dto, @Suspended final AsyncResponse asyncResponse);

  @Path("/{id}")
  ExternalTaskResource getExternalTask(@PathParam("id") String externalTaskId);

  @PUT
  @Path("/retries")
  @Consumes(MediaType.APPLICATION_JSON)
  void setRetries(SetRetriesForExternalTasksDto retriesDto);

  @POST
  @Path("/retries-async")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  BatchDto setRetriesAsync(SetRetriesForExternalTasksDto retriesDto);

  @GET
  @Path("/topic-names")
  @Produces(MediaType.APPLICATION_JSON)
  List<String> getTopicNames(@QueryParam("withLockedTasks") boolean withLockedTasks,
      @QueryParam("withUnlockedTasks") boolean withUnlockedTasks,
      @QueryParam("withRetriesLeft") boolean withRetriesLeft);

}
