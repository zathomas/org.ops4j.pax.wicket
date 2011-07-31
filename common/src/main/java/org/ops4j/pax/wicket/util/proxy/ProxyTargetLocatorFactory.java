package org.ops4j.pax.wicket.util.proxy;

import java.lang.reflect.Field;
import java.util.Map;

import org.ops4j.pax.wicket.api.PaxWicketBean;

public interface ProxyTargetLocatorFactory {

    IProxyTargetLocator createProxyTargetLocator(Field field, Class<?> page,
            PaxWicketBean annotation, Map<String, String> overwrites);

}
