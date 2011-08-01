package org.ops4j.pax.wicket.util.proxy;

import java.lang.reflect.Field;
import java.util.Map;

import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.osgi.framework.BundleContext;

public interface ProxyTargetLocatorFactory {

    IProxyTargetLocator createProxyTargetLocator(BundleContext bundleContext, Field field, Class<?> beanType,
            Class<?> parent, PaxWicketBean annotation, Map<String, String> overwrites);

}
