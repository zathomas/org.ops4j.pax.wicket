package org.ops4j.pax.wicket.impl14.internal;

import org.apache.wicket.util.lang.Objects;
import org.ops4j.pax.wicket.impl14.util.serialization.PaxWicketObjectStreamFactory;
import org.ops4j.pax.wicket.util.PageFactoryInitiator;
import org.ops4j.pax.wicket.util.injection.ComponentInstantiationRegistrator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    public static final String SYMBOLIC_NAME = "org.ops4j.pax.wicket.common";
    private ServiceRegistration componentInstantionRegistrator;
    private ServiceRegistration pageFactoryInitiator;

    public void start(BundleContext context) throws Exception {
        // TODO: [PAXWICKET-257] This have to be solved via an SPI in the common package
        Objects.setObjectStreamFactory(new PaxWicketObjectStreamFactory(true));

        componentInstantionRegistrator = context.registerService(ComponentInstantiationRegistrator.class.getName(),
            new DefaultComponentInstantiationRegistrator(), null);

        pageFactoryInitiator =
            context.registerService(PageFactoryInitiator.class.getName(), new DefaultPageFactoryInitiator(context),
                null);
    }

    public void stop(BundleContext context) throws Exception {
        componentInstantionRegistrator.unregister();
        pageFactoryInitiator.unregister();
    }

}
