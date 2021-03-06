/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and others.
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
package org.nuxeo.ecm.platform.mqueues.importer.automation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.importer.mqueues.chronicle.ChronicleConfig;
import org.nuxeo.ecm.platform.importer.mqueues.kafka.KafkaConfigService;
import org.nuxeo.ecm.platform.importer.mqueues.mqueues.MQManager;
import org.nuxeo.ecm.platform.importer.mqueues.mqueues.chronicle.ChronicleMQManager;
import org.nuxeo.ecm.platform.importer.mqueues.mqueues.kafka.KafkaMQManager;
import org.nuxeo.ecm.platform.importer.mqueues.pattern.message.DocumentMessage;
import org.nuxeo.ecm.platform.importer.mqueues.pattern.producer.ProducerPool;
import org.nuxeo.ecm.platform.mqueues.importer.producer.CSVDocumentMessageProducerFactory;
import org.nuxeo.runtime.api.Framework;

/**
 * @since 9.1
 */
@Operation(id = CSVDocumentProducers.ID, category = Constants.CAT_SERVICES, label = "Produces random blobs", since = "9.1", description = "Produces random blobs in a mqueues.")
public class CSVDocumentProducers {
	private static final Log log = LogFactory.getLog(CSVDocumentProducers.class);
	public static final String ID = "ACA.runCSVDocumentProducers";
	public static final String DEFAULT_MQ_NAME = "mq-doc";

	@Context
	protected OperationContext ctx;

	@Param(name = "csvPath", required = true)
	protected String csvPath = "";

	@Param(name = "nbThreads", required = false)
	protected Integer nbThreads = 8;

	@Param(name = "mqName", required = false)
	protected String mqName;

	@Param(name = "mqSize", required = false)
	protected Integer mqSize;

	@Param(name = "blobInfoPath", required = false)
	protected String blobInfoPath;

	@Param(name = "kafkaConfig", required = false)
	protected String kafkaConfig;

	@OperationMethod
	public void run() {
		checkAccess(ctx);
		try (MQManager<DocumentMessage> manager = getManager()) {
			manager.createIfNotExists(getMQName(), getMQSize());
			ProducerPool<DocumentMessage> producers;
			if (blobInfoPath != null) {
				producers = new ProducerPool<>(getMQName(), manager, new CSVDocumentMessageProducerFactory(csvPath),
						nbThreads.shortValue());
			} else {
				producers = new ProducerPool<>(getMQName(), manager, new CSVDocumentMessageProducerFactory(csvPath),
						nbThreads.shortValue());
			}
			producers.start().get();
			producers.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	protected int getMQSize() {
		if (mqSize != null && mqSize > 0) {
			return mqSize;
		}
		return nbThreads;
	}

	protected String getMQName() {
		if (mqName != null) {
			return mqName;
		}
		return DEFAULT_MQ_NAME;
	}

	protected MQManager<DocumentMessage> getManager() {
		if (kafkaConfig == null || kafkaConfig.isEmpty()) {
			return new ChronicleMQManager<>(ChronicleConfig.getBasePath("import"),
					ChronicleConfig.getRetentionDuration());
		}
		KafkaConfigService service = Framework.getService(KafkaConfigService.class);
		return new KafkaMQManager<>(service.getZkServers(kafkaConfig), service.getTopicPrefix(kafkaConfig),
				service.getProducerProperties(kafkaConfig), service.getConsumerProperties(kafkaConfig));
	}

	protected static void checkAccess(OperationContext context) {
		NuxeoPrincipal principal = (NuxeoPrincipal) context.getPrincipal();
		if (principal == null || !principal.isAdministrator()) {
			throw new RuntimeException("Unauthorized access: " + principal);
		}
	}

}
