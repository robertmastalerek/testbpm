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
package org.eximeebpms.bpm.model.bpmn.impl.instance;

import org.eximeebpms.bpm.model.bpmn.instance.BaseElement;
import org.eximeebpms.bpm.model.bpmn.instance.CorrelationKey;
import org.eximeebpms.bpm.model.bpmn.instance.CorrelationProperty;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;
import org.eximeebpms.bpm.model.xml.type.attribute.Attribute;
import org.eximeebpms.bpm.model.xml.type.child.SequenceBuilder;
import org.eximeebpms.bpm.model.xml.type.reference.ElementReferenceCollection;

import java.util.Collection;

import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN correlationKey element
 *
 * @author Sebastian Menski
 */
public class CorrelationKeyImpl extends BaseElementImpl implements CorrelationKey {

  protected static Attribute<String> nameAttribute;
  protected static ElementReferenceCollection<CorrelationProperty, CorrelationPropertyRef> correlationPropertyRefCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CorrelationKey.class, BPMN_ELEMENT_CORRELATION_KEY)
      .namespaceUri(BPMN20_NS)
      .extendsType(BaseElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<CorrelationKey>() {
        public CorrelationKey newInstance(ModelTypeInstanceContext instanceContext) {
          return new CorrelationKeyImpl(instanceContext);
        }
      });

    nameAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_NAME)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    correlationPropertyRefCollection = sequenceBuilder.elementCollection(CorrelationPropertyRef.class)
      .qNameElementReferenceCollection(CorrelationProperty.class)
      .build();

    typeBuilder.build();
  }


  public CorrelationKeyImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getName() {
    return nameAttribute.getValue(this);
  }

  public void setName(String name) {
    nameAttribute.setValue(this, name);
  }

  public Collection<CorrelationProperty> getCorrelationProperties() {
    return correlationPropertyRefCollection.getReferenceTargetElements(this);
  }
}
