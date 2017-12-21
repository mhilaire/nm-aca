package org.nuxeo.ecm.platform.mqueues.tests.importer;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.annotations.RepositoryInit;

public class ACARepositoryInit implements RepositoryInit {
	@Override
	public void populate(CoreSession session) {
		createDefaultDomain(session);
		session.save();
	}

	private void createDefaultDomain(CoreSession session) {
		DocumentModel doc = session.createDocumentModel("/", "default-domain", "Domain");
		doc = session.saveDocument(doc);
	}
}