/**
 * Copyright OPS4J
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.wicket.internal;

import org.ops4j.pax.wicket.internal.extender.BundleDelegatingExtensionTracker;
import org.ops4j.pax.wicket.internal.extender.PaxWicketBundleListener;
import org.ops4j.pax.wicket.internal.injection.ProxyTargetLocatorFactoryTracker;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private HttpTracker httpTracker;
    private ServiceTracker applicationFactoryTracker;

    private static BundleContext bundleContext;

    private PaxWicketBundleListener paxWicketBundleListener;
    private BundleDelegatingExtensionTracker bundleDelegatingExtensionTracker;
    private ProxyTargetLocatorFactoryTracker proxyTargetLocatorFactoryTracker;
    private ComponentInstantiationRegistratorTracker componentInstantiationRegistratorTracker;

    private PageFactoryInitiatorTracker pageFactoryInitiatorTracker;

    public final void start(BundleContext context) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            Bundle bundle = context.getBundle();
            String bundleSymbolicName = bundle.getSymbolicName();

            LOGGER.debug("Initializing [" + bundleSymbolicName + "] bundle.");
        }

        bundleContext = context;

        httpTracker = new HttpTracker(context);
        httpTracker.open();

        componentInstantiationRegistratorTracker = new ComponentInstantiationRegistratorTracker(context);
        componentInstantiationRegistratorTracker.open(true);

        pageFactoryInitiatorTracker = new PageFactoryInitiatorTracker(context);
        pageFactoryInitiatorTracker.open(true);

        applicationFactoryTracker =
            new PaxWicketAppFactoryTracker(context, httpTracker, componentInstantiationRegistratorTracker,
                pageFactoryInitiatorTracker);
        applicationFactoryTracker.open(true);

        proxyTargetLocatorFactoryTracker = new ProxyTargetLocatorFactoryTracker(context);
        proxyTargetLocatorFactoryTracker.open();
        bundleDelegatingExtensionTracker =
            new BundleDelegatingExtensionTracker(context, proxyTargetLocatorFactoryTracker);
        bundleDelegatingExtensionTracker.open(true);

        paxWicketBundleListener = new PaxWicketBundleListener(bundleDelegatingExtensionTracker);
        context.addBundleListener(paxWicketBundleListener);
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }

    public static BundleContext getBundleContextByBundleId(long bundleId) {
        Bundle bundle = bundleContext.getBundle(bundleId);
        if (bundle != null) {
            return bundle.getBundleContext();
        } else {
            return null;
        }
    }

    public final void stop(BundleContext context) throws Exception {
        context.removeBundleListener(paxWicketBundleListener);
        applicationFactoryTracker.close();
        httpTracker.close();
        componentInstantiationRegistratorTracker.close();
        pageFactoryInitiatorTracker.close();
        proxyTargetLocatorFactoryTracker.close();
        bundleDelegatingExtensionTracker.close();

        applicationFactoryTracker = null;
        httpTracker = null;
        componentInstantiationRegistratorTracker = null;
        pageFactoryInitiatorTracker = null;
        proxyTargetLocatorFactoryTracker = null;
        bundleDelegatingExtensionTracker = null;
        bundleContext = null;

        if (LOGGER.isDebugEnabled()) {
            Bundle bundle = context.getBundle();
            String bundleSymbolicName = bundle.getSymbolicName();

            LOGGER.debug("Bundle [" + bundleSymbolicName + "] stopped.");
        }
    }

}
