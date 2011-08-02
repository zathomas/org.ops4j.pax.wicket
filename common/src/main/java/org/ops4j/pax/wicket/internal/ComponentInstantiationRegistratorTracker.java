package org.ops4j.pax.wicket.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.protocol.http.WebApplication;
import org.ops4j.pax.wicket.api.PaxWicketInjector;
import org.ops4j.pax.wicket.util.injection.ComponentInstantiationRegistrator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class ComponentInstantiationRegistratorTracker extends ServiceTracker {

    private Map<ServiceReference, ComponentInstantiationRegistrator> registrationHandlers =
        new HashMap<ServiceReference, ComponentInstantiationRegistrator>();

    public ComponentInstantiationRegistratorTracker(BundleContext context) {
        super(context, ComponentInstantiationRegistrator.class.getName(), null);
    }

    @Override
    public Object addingService(ServiceReference reference) {
        Object service = super.addingService(reference);
        synchronized (registrationHandlers) {
            registrationHandlers.put(reference, (ComponentInstantiationRegistrator) service);
        }
        return service;
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
        synchronized (registrationHandlers) {
            registrationHandlers.put(reference, (ComponentInstantiationRegistrator) service);
        }
        super.modifiedService(reference, service);
    }

    @Override
    public void remove(ServiceReference reference) {
        synchronized (registrationHandlers) {
            registrationHandlers.remove(reference);
        }
        super.remove(reference);
    }

    public boolean registerPaxWicketInjectorForApplication(PaxWicketInjector injector, WebApplication application) {
        synchronized (registrationHandlers) {
            Collection<ComponentInstantiationRegistrator> values = registrationHandlers.values();
            for (ComponentInstantiationRegistrator componentInstantiationRegistrator : values) {
                if (componentInstantiationRegistrator.registerPaxWicketInjectorForApplication(injector, application)) {
                    return true;
                }
            }
        }
        return false;
    }

}
