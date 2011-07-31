package org.ops4j.pax.wicket.injection.aries.internal;

import java.lang.reflect.Field;
import java.util.Map;

import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.util.proxy.IProxyTargetLocator;
import org.ops4j.pax.wicket.util.proxy.ProxyTargetLocatorFactory;
import org.osgi.framework.BundleContext;

public class BlueprintBeanProxyTargetLocatorFactory implements ProxyTargetLocatorFactory {

    private BundleContext bundleContext;

    public IProxyTargetLocator createProxyTargetLocator(Field field, Class<?> beanType, Class<?> parent,
            PaxWicketBean annotation, Map<String, String> overwrites) {
        return new BlueprintBeanProxyTargetLocator(bundleContext, annotation, beanType, parent, overwrites);
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

}
