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
 */
package org.nuxeo.ecm.platform.mqueues.importer.producer;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.platform.importer.mqueues.pattern.message.DocumentMessage;
import org.nuxeo.ecm.platform.importer.mqueues.pattern.producer.AbstractProducer;

/**
 * @since 9.1
 */
public class CSVDocumentMessageProducer extends AbstractProducer<DocumentMessage> {
	private static final Log log = LogFactory.getLog(CSVDocumentMessageProducer.class);

	private static final String DEFAULT_SEPARATOR = ",";

	protected Scanner scanner;

	public CSVDocumentMessageProducer(int producerId, File csvFile) throws Exception {
		super(producerId);
		scanner = new Scanner(csvFile);
		log.info("CSVDocumentMessageProducer created, from csv: " + csvFile.getAbsolutePath());
	}

	@Override
	public int getPartition(DocumentMessage message, int partitions) {
		// You can use map key value where key is the parent path and value is the partition key
		// using message.getParentPath()
		// </path/folder1,key1>,</path/folder2,key2>
		return getProducerId() % partitions;
	}

	@Override
	public boolean hasNext() {
		return scanner.hasNext();
	}

	@Override
	public DocumentMessage next() {
		String[] columns = scanner.nextLine().split(DEFAULT_SEPARATOR);
		return createDocument(columns);
	}

	protected HashMap<String, Serializable> parseProperties(String[] col) {
		// TODO Use CSV header to get values
		HashMap<String, Serializable> ret = new HashMap<>();
		ret.put("dc:title", col[0]);
		ret.put("dc:description", col[2]);
		// dc:title,dc:description,my:customproperty
		// myTitle,myDesc,Test1
		// TODO loop for each column
		// ret.put(currentHeaderCol, colValue);
		
		// You need to add here all document properties parsing the CSV columns
		// JSONObject jsonObject = new JSONObject(col[X]);
		// ret.put("my:mycomplexproperty",JSONObject???)
		return ret;
	}

	protected DocumentMessage createDocument(String[] col) {
		// CSV format
		// title,type,description,path,otherProperties
		// NOTE: path must point to an existing parent path
		// The importer doesn't yet handle creating parent folder during import
		HashMap<String, Serializable> properties = parseProperties(col);
		String name = col[0];
		String type = col[1];
		// Path must be RELATIVE path in nuxeo, and folder MUST EXIST before
		// If importing from default-domain:
		// workspaces/administrator/myFolder
		// If importing from /:
		// /default-domain/workspaces/administrator/myFolder
		String parentPath = col[3];
		
		DocumentMessage.Builder builder = DocumentMessage.builder(type, parentPath, name).setProperties(properties);
		// builder.setBlob(attachBlob());
		
		return builder.build();
	}

	protected Blob attachBlob() {
		// TODO Use custom blobProvider to return blob object
		
		return null;
	}

	@Override
	public void close() throws Exception {
		super.close();
		if (scanner != null) {
			scanner.close();
		}
	}

}
