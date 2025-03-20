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
package org.eximeebpms.bpm.integrationtest.functional.transactions.beans;

import javax.inject.Named;

import org.eximeebpms.bpm.engine.delegate.DelegateExecution;
import org.eximeebpms.bpm.engine.delegate.JavaDelegate;
import org.eximeebpms.bpm.engine.impl.cfg.TransactionListener;
import org.eximeebpms.bpm.engine.impl.cfg.TransactionState;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;

@Named
public class FailingTransactionListenerDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    Context.getCommandContext().getTransactionContext().addTransactionListener(TransactionState.COMMITTING, new TransactionListener() {

      @Override
      public void execute(CommandContext context) {
        throw new RuntimeException("exception in transaction listener");
      }
    });
  }

}
