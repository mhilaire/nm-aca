/*
 * (C) Copyright 2017 Nuxeo SA (http://nuxeo.com/) and others.
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
package org.nuxeo.ecm.platform.mqueues.importer.producer;

import java.io.File;
import java.util.Collections;

import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.importer.mqueues.mqueues.MQManager;
import org.nuxeo.ecm.platform.importer.mqueues.mqueues.MQPartition;
import org.nuxeo.ecm.platform.importer.mqueues.pattern.message.DocumentMessage;
import org.nuxeo.ecm.platform.importer.mqueues.pattern.producer.ProducerFactory;
import org.nuxeo.ecm.platform.importer.mqueues.pattern.producer.ProducerIterator;

/**
 * @since 9.1
 */
public class CSVDocumentMessageProducerFactory implements ProducerFactory<DocumentMessage> {
	protected final MQManager manager;
	protected final String mqName;
	protected final String csvPath;

	/**
	 * Import and create document from CSV file row
	 */
	public CSVDocumentMessageProducerFactory(String csvPath) {
		this.manager = null;
		this.mqName = null;
		this.csvPath = csvPath;
	}

	/**
	 * Import and create document from CSV file row
	 */
	public CSVDocumentMessageProducerFactory(String csvPath, MQManager manager, String mqBlobInfoName) {
		this.manager = manager;
		this.mqName = mqBlobInfoName;
		this.csvPath = csvPath;
	}

	@Override
	public ProducerIterator<DocumentMessage> createProducer(int producerId) {
		if (manager != null) {
			manager.createTailer(getGroupName(producerId), Collections.singleton(MQPartition.of(mqName, 0)));
		}

		File csvFile = new File(this.csvPath);
		if (!csvFile.exists()) {
			throw new NuxeoException("CSV file does not exist");
		}
		try {
			return new CSVDocumentMessageProducer(producerId, csvFile);
		} catch (Exception e) {
			throw new NuxeoException("Error while creating CSV producer", e);
		}
	}

	protected String getGroupName(int producerId) {
		return "CSVDocumentMessageProducer." + producerId;
	}

}
