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
package org.eximeebpms.bpm.spring.boot.starter.jdbc;

import javax.sql.DataSource;

import org.eximeebpms.bpm.spring.boot.starter.CamundaBpmAutoConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@EnableAutoConfiguration(exclude = CamundaBpmAutoConfiguration.class)
public class HistoryLevelDeterminatorJdbcTemplateImplTestApplication {

  @Bean
  public CamundaBpmProperties camundaBpmProperties() {
    return new CamundaBpmProperties();
  }

  @Bean
  public DataSource dataSource() {
    EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
    EmbeddedDatabase db = builder.setName("testdbForHistoryLevelDetermination").setType(EmbeddedDatabaseType.H2)
        .addScript("/org/eximeebpms/bpm/engine/db/create/activiti.h2.create.engine.sql").addScript("db/sql/insert-history-data.sql").continueOnError(true).build();
    return db;
  }

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  public HistoryLevelDeterminator historyLevelDeterminator(JdbcTemplate jdbcTemplate, CamundaBpmProperties camundaBpmProperties) {
    return HistoryLevelDeterminatorJdbcTemplateImpl.createHistoryLevelDeterminator(camundaBpmProperties, jdbcTemplate);
  }
}
