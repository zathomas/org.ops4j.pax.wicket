package org.ops4j.pax.wicket.injection.spring.internal;

import java.lang.reflect.Field;
import java.util.Map;

import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.util.proxy.IProxyTargetLocator;
import org.ops4j.pax.wicket.util.proxy.ProxyTargetLocatorFactory;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;

public class SpringBeanProxyTargetLocatorFactory implements ProxyTargetLocatorFactory, BundleContextAware {

    private BundleContext bundleContext;

    public IProxyTargetLocator createProxyTargetLocator(Field field, Class<?> beanType, Class<?> parent,
            PaxWicketBean annotation, Map<String, String> overwrites) {
        return new SpringBeanProxyTargetLocator(bundleContext, annotation, beanType, parent, overwrites);
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

}
