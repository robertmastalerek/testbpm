package org.camunda.bpm.container.impl.jboss.extension.resource;

import org.camunda.bpm.container.impl.jboss.extension.BpmPlatformExtension;
import org.camunda.bpm.container.impl.jboss.extension.Constants;
import org.camunda.bpm.container.impl.jboss.extension.ModelConstants;
import org.camunda.bpm.container.impl.jboss.extension.handler.JobAcquisitionAdd;
import org.camunda.bpm.container.impl.jboss.extension.handler.JobAcquisitionRemove;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PersistentResourceDefinition;

import java.util.Arrays;
import java.util.Collection;

public class JobAcquisitionDefinition extends PersistentResourceDefinition {

  public static final JobAcquisitionDefinition INSTANCE = new JobAcquisitionDefinition();

  private JobAcquisitionDefinition() {
    super(BpmPlatformExtension.JOB_ACQUISTIONS_PATH,
        BpmPlatformExtension.getResourceDescriptionResolver(ModelConstants.JOB_ACQUISITION),
        JobAcquisitionAdd.INSTANCE,
        JobAcquisitionRemove.INSTANCE);
  }

  @Override
  public Collection<AttributeDefinition> getAttributes() {
    return Arrays.asList(Constants.JOB_ACQUISITION_ATTRIBUTES);
  }

}
