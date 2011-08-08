package org.ops4j.pax.wicket.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.protocol.http.WebApplication;
import org.ops4j.pax.wicket.util.PageFactoryInitiator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class PageFactoryInitiatorTracker extends ServiceTracker {

    private Map<ServiceReference, PageFactoryInitiator> initiators =
        new HashMap<ServiceReference, PageFactoryInitiator>();

    public PageFactoryInitiatorTracker(BundleContext context) {
        super(context, PageFactoryInitiator.class.getName(), null);
    }

    @Override
    public Object addingService(ServiceReference reference) {
        PageFactoryInitiator pageFactory = (PageFactoryInitiator) super.addingService(reference);
        synchronized (initiators) {
            initiators.put(reference, pageFactory);
        }
        return pageFactory;
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
        synchronized (initiators) {
            initiators.put(reference, (PageFactoryInitiator) service);
        }
        super.modifiedService(reference, service);
    }

    @Override
    public void remove(ServiceReference reference) {
        synchronized (initiators) {
            initiators.remove(reference);
        }
        super.remove(reference);
    }

    public void initiatePageFactoryForWicketApplication(WebApplication webApplication, String applicationName) {
        synchronized (initiators) {
            Collection<PageFactoryInitiator> values = initiators.values();
            for (PageFactoryInitiator initiator : values) {
                if (initiator.registerPaxWicketPageFactoryForApplication(webApplication, applicationName)) {
                    return;
                }
            }
        }
        throw new IllegalStateException(
            "None of the available registrators was able to register a PageFactory for the application.");
    }

    public void disposePageFactoryForWicketApplication(String applicationName) {
        synchronized (initiators) {
            Collection<PageFactoryInitiator> values = initiators.values();
            for (PageFactoryInitiator initiator : values) {
                if (initiator.disposePaxWicketPageFactoryForApplication(applicationName)) {
                    return;
                }
            }
        }
        throw new IllegalStateException(
            "None of the available registrators was able to dispose a PageFactory for the application.");
    }

}
