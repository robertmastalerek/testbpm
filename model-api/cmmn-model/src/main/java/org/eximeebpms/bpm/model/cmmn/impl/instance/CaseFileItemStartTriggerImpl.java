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
import static org.eximeebpms.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_SOURCE_REF;
import static org.eximeebpms.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_CASE_FILE_ITEM_START_TRIGGER;

import org.eximeebpms.bpm.model.cmmn.CaseFileItemTransition;
import org.eximeebpms.bpm.model.cmmn.instance.CaseFileItem;
import org.eximeebpms.bpm.model.cmmn.instance.CaseFileItemStartTrigger;
import org.eximeebpms.bpm.model.cmmn.instance.CaseFileItemTransitionStandardEvent;
import org.eximeebpms.bpm.model.cmmn.instance.StartTrigger;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.eximeebpms.bpm.model.xml.type.child.ChildElement;
import org.eximeebpms.bpm.model.xml.type.child.SequenceBuilder;
import org.eximeebpms.bpm.model.xml.type.reference.AttributeReference;

/**
 * @author Roman Smirnov
 *
 */
public class CaseFileItemStartTriggerImpl extends StartTriggerImpl implements CaseFileItemStartTrigger {

  protected static AttributeReference<CaseFileItem> sourceRefAttribute;
  protected static ChildElement<CaseFileItemTransitionStandardEvent> standardEventChild;

  public CaseFileItemStartTriggerImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public CaseFileItem getSource() {
    return sourceRefAttribute.getReferenceTargetElement(this);
  }

  public void setSource(CaseFileItem source) {
    sourceRefAttribute.setReferenceTargetElement(this, source);
  }

  public CaseFileItemTransition getStandardEvent() {
    CaseFileItemTransitionStandardEvent child = standardEventChild.getChild(this);
    return child.getValue();
  }

  public void setStandardEvent(CaseFileItemTransition standardEvent) {
    CaseFileItemTransitionStandardEvent child = standardEventChild.getChild(this);
    child.setValue(standardEvent);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CaseFileItemStartTrigger.class, CMMN_ELEMENT_CASE_FILE_ITEM_START_TRIGGER)
        .extendsType(StartTrigger.class)
        .namespaceUri(CMMN11_NS)
        .instanceProvider(new ModelTypeInstanceProvider<CaseFileItemStartTrigger>() {
          public CaseFileItemStartTrigger newInstance(ModelTypeInstanceContext instanceContext) {
            return new CaseFileItemStartTriggerImpl(instanceContext);
          }
        });

    sourceRefAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_SOURCE_REF)
        .idAttributeReference(CaseFileItem.class)
        .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    standardEventChild = sequenceBuilder.element(CaseFileItemTransitionStandardEvent.class)
        .build();

    typeBuilder.build();
  }

}
