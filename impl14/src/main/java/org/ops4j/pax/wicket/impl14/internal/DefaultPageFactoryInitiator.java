package org.ops4j.pax.wicket.impl14.internal;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.protocol.http.WebApplication;
import org.ops4j.pax.wicket.util.PageFactoryInitiator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPageFactoryInitiator implements PageFactoryInitiator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPageFactoryInitiator.class);

    private final BundleContext bundleContext;

    private Map<String, PaxWicketPageFactory> registration =
        new HashMap<String, PaxWicketPageFactory>();

    public DefaultPageFactoryInitiator(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public boolean registerPaxWicketPageFactoryForApplication(WebApplication webApplication, String applicationName) {
        PaxWicketPageFactory pageFactory = new PaxWicketPageFactory(bundleContext, applicationName);
        synchronized (registration) {
            try {
                webApplication.getSessionSettings().setPageFactory(pageFactory);
            } catch (Exception e) {
                LOGGER.info("Wicket application does not seam to be impl14, but impl15.", e);
                return false;
            }
            pageFactory.initialize();
            registration.put(applicationName, pageFactory);
        }
        return true;
    }

    public boolean disposePaxWicketPageFactoryForApplication(String applicationName) {
        synchronized (registration) {
            PaxWicketPageFactory remove = registration.remove(applicationName);
            if (remove == null) {
                return false;
            }
            remove.dispose();
        }
        return true;
    }

}
