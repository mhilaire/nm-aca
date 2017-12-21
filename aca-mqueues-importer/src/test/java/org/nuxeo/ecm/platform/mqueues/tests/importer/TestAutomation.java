/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Benoit Delbosc
 */
package org.nuxeo.ecm.platform.mqueues.tests.importer;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.mqueues.importer.automation.ACADocumentConsumers;
import org.nuxeo.ecm.platform.mqueues.importer.automation.CSVDocumentProducers;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

@RunWith(FeaturesRunner.class)
@Features({ ACAFeature.class })
@Deploy({ "org.nuxeo.ecm.mqueues.importer", "org.nuxeo.ecm.automation.core", "org.nuxeo.ecm.core.io" })
public abstract class TestAutomation {

	@Inject
	CoreSession session;

	@Inject
	AutomationService automationService;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	public abstract void addExtraParams(Map<String, Object> params);

	@Test
	public void testDocumentImport() throws Exception {
		final int nbThreads = 1;
		final long nbDocuments = 2;

		OperationContext ctx = new OperationContext(session);

		Map<String, Object> params = new HashMap<>();
		params.put("nbDocuments", nbDocuments);
		params.put("nbThreads", nbThreads);
		params.put("csvPath", TestDocumentImport.class.getResource("/test.csv").getFile());
		addExtraParams(params);
		automationService.run(ctx, CSVDocumentProducers.ID, params);

		params.clear();
		params.put("rootFolder", "/");
		addExtraParams(params);
		automationService.run(ctx, ACADocumentConsumers.ID, params);

		// start a new transaction to prevent db isolation to hide our new documents
		TransactionHelper.commitOrRollbackTransaction();
		TransactionHelper.startTransaction();

		DocumentModelList ret = session.query("SELECT * FROM Document WHERE ecm:primaryType IN ('File', 'Folder')");
		assertEquals(nbDocuments, ret.size());
	}

}
