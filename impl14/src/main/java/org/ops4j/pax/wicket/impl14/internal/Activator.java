package org.ops4j.pax.wicket.impl14.internal;

import org.apache.wicket.util.lang.Objects;
import org.ops4j.pax.wicket.impl14.util.serialization.PaxWicketObjectStreamFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    public static final String SYMBOLIC_NAME = "org.ops4j.pax.wicket.common";

    public void start(BundleContext context) throws Exception {
        Objects.setObjectStreamFactory(new PaxWicketObjectStreamFactory(true));
    }

    public void stop(BundleContext context) throws Exception {
    }

}
