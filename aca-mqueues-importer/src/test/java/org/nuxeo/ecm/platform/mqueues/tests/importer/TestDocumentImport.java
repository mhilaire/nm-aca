/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     bdelbosc
 */
package org.nuxeo.ecm.platform.mqueues.tests.importer;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.importer.mqueues.mqueues.MQManager;
import org.nuxeo.ecm.platform.importer.mqueues.pattern.consumer.ConsumerPolicy;
import org.nuxeo.ecm.platform.importer.mqueues.pattern.consumer.ConsumerPool;
import org.nuxeo.ecm.platform.importer.mqueues.pattern.consumer.ConsumerStatus;
import org.nuxeo.ecm.platform.importer.mqueues.pattern.message.DocumentMessage;
import org.nuxeo.ecm.platform.importer.mqueues.pattern.producer.ProducerPool;
import org.nuxeo.ecm.platform.importer.mqueues.pattern.producer.ProducerStatus;
import org.nuxeo.ecm.platform.mqueues.importer.consumer.ACADocumentMessageConsumerFactory;
import org.nuxeo.ecm.platform.mqueues.importer.producer.CSVDocumentMessageProducerFactory;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

@RunWith(FeaturesRunner.class)
@Features({ ACAFeature.class })
@Deploy("org.nuxeo.ecm.platform.dublincore")
public abstract class TestDocumentImport {

	protected static final Log log = LogFactory.getLog(TestDocumentImport.class);

	public abstract MQManager getManager() throws Exception;

	@Inject
	CoreSession session;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@SuppressWarnings("unchecked")
	@Test
	public void twoStepsCSVImport() throws Exception {
		final int NB_QUEUE = 5;
		final short NB_PRODUCERS = 1;
		final int NB_DOCUMENTS = 2;
		try (MQManager<DocumentMessage> manager = getManager()) {
			manager.createIfNotExists("document-import", NB_QUEUE);
			ProducerPool<DocumentMessage> producers = new ProducerPool<>("document-import", manager,
					new CSVDocumentMessageProducerFactory(
							TestDocumentImport.class.getResource("/test.csv").getFile()),
					NB_PRODUCERS);
			List<ProducerStatus> ret = producers.start().get();
			assertEquals(NB_PRODUCERS, (long) ret.size());
			assertEquals(NB_PRODUCERS * NB_DOCUMENTS, ret.stream().mapToLong(r -> r.nbProcessed).sum());

			DocumentModel root = session.getRootDocument();
			ConsumerPool<DocumentMessage> consumers = new ConsumerPool<>("document-import", manager,
					new ACADocumentMessageConsumerFactory(root.getRepositoryName(), root.getPathAsString()),
					ConsumerPolicy.BOUNDED);
			List<ConsumerStatus> ret2 = consumers.start().get();
			assertEquals(NB_QUEUE, (long) ret2.size());
			assertEquals(NB_PRODUCERS * NB_DOCUMENTS, ret2.stream().mapToLong(r -> r.committed).sum());
		}
	}

}
