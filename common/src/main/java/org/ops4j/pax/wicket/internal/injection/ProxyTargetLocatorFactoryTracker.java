package org.ops4j.pax.wicket.internal.injection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.util.proxy.IProxyTargetLocator;
import org.ops4j.pax.wicket.util.proxy.ProxyTargetLocatorFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class ProxyTargetLocatorFactoryTracker extends ServiceTracker {

    Map<ServiceReference, ProxyTargetLocatorFactory> factories =
        new HashMap<ServiceReference, ProxyTargetLocatorFactory>();

    public ProxyTargetLocatorFactoryTracker(BundleContext context) {
        super(context, ProxyTargetLocatorFactory.class.getName(), null);
    }

    @Override
    public Object addingService(ServiceReference reference) {
        synchronized (factories) {
            ProxyTargetLocatorFactory service = (ProxyTargetLocatorFactory) super.addingService(reference);
            factories.put(reference, service);
            return service;
        }
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
        synchronized (factories) {
            factories.put(reference, (ProxyTargetLocatorFactory) service);
        }
        super.modifiedService(reference, service);
    }

    @Override
    public void remove(ServiceReference reference) {
        synchronized (factories) {
            factories.remove(reference);
        }
        super.remove(reference);
    }

    public List<IProxyTargetLocator> getAllProxyTargetLocators(BundleContext bundleContext, Field field,
            Class<?> beanType, Class<?> parent, PaxWicketBean annotation, Map<String, String> overwrites) {
        List<IProxyTargetLocator> targetLocators = new ArrayList<IProxyTargetLocator>();
        synchronized (factories) {
            Collection<ProxyTargetLocatorFactory> values = factories.values();
            for (ProxyTargetLocatorFactory proxyTargetLocatorFactory : values) {
                targetLocators.add(proxyTargetLocatorFactory.createProxyTargetLocator(bundleContext, field, beanType,
                    parent, annotation, overwrites));
            }
        }
        return Collections.unmodifiableList(targetLocators);
    }
}
