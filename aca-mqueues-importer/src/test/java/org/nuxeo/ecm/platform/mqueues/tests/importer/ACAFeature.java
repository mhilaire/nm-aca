package org.nuxeo.ecm.platform.mqueues.tests.importer;

import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.SimpleFeature;


@Features({ CoreFeature.class, PlatformFeature.class})
@RepositoryConfig( init = ACARepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({ "com.nm.aca.mqueues.importer" })
public class ACAFeature extends SimpleFeature {
	
	
}
