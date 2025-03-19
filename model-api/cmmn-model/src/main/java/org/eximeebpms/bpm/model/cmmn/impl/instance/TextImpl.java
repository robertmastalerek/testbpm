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
package org.eximeebpms.bpm.model.cmmn.impl.instance;

import static org.eximeebpms.bpm.model.cmmn.impl.CmmnModelConstants.CMMN11_NS;
import static org.eximeebpms.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_TEXT;

import org.eximeebpms.bpm.model.cmmn.instance.Text;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * @author Roman Smirnov
 *
 */
public class TextImpl extends CmmnModelElementInstanceImpl implements Text {

  public TextImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Text.class, CMMN_ELEMENT_TEXT)
      .namespaceUri(CMMN11_NS)
      .instanceProvider(new ModelTypeInstanceProvider<Text>() {
        public Text newInstance(ModelTypeInstanceContext instanceContext) {
          return new TextImpl(instanceContext);
        }
      });

    typeBuilder.build();
  }

}
