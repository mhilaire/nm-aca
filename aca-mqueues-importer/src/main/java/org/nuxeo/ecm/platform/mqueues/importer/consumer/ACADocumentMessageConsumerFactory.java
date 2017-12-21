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
package org.nuxeo.ecm.platform.mqueues.importer.consumer;

import org.nuxeo.ecm.platform.importer.mqueues.pattern.consumer.Consumer;
import org.nuxeo.ecm.platform.importer.mqueues.pattern.consumer.ConsumerFactory;
import org.nuxeo.ecm.platform.importer.mqueues.pattern.consumer.DocumentMessageConsumer;
import org.nuxeo.ecm.platform.importer.mqueues.pattern.message.DocumentMessage;

/**
 * @since 9.1
 */

public class ACADocumentMessageConsumerFactory implements ConsumerFactory<DocumentMessage> {
    private final String repositoryName;
    private final String rootPath;

    public ACADocumentMessageConsumerFactory(String repositoryName, String rootPath) {
        this.repositoryName = repositoryName;
        this.rootPath = rootPath;
    }

    @Override
    public Consumer<DocumentMessage> createConsumer(String consumerId) {
        return new ACADocumentMessageConsumer(consumerId, repositoryName, rootPath);
    }
}
