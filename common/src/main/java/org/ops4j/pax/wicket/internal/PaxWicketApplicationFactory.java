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

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.ops4j.pax.wicket.api.Constants;
import org.ops4j.pax.wicket.internal.injection.DelegatingComponentInstanciationListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An internal wrapper for the {@link IWebApplicationFactory} exported by clients who want to register an application.
 * This class adds all the logic to extract the required properties from the osgi service and wrapping the created
 * application factory with the classloading, injection and other tricks required to run the application.
 */
public class PaxWicketApplicationFactory implements IWebApplicationFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaxWicketApplicationFactory.class);

    private final BundleContext bundleContext;
    private final IWebApplicationFactory webApplicationFactory;
    private final String applicationName;
    private final String mountPoint;
    private final Map<String, String> contextParams;
    private final File tmpDir;
    private final FilterDelegator filterDelegator;
    private final ComponentInstantiationRegistratorTracker componentInstantiationRegistratorTracker;
    private final PageFactoryInitiatorTracker pageFactoryInitiatorTracker;

    @SuppressWarnings("unchecked")
    public static PaxWicketApplicationFactory createPaxWicketApplicationFactory(BundleContext bundleContext,
            IWebApplicationFactory webApplicationFactory, ServiceReference reference,
            ComponentInstantiationRegistratorTracker componentInstantiationRegistratorTracker,
            PageFactoryInitiatorTracker pageFactoryInitiatorTracker) {
        File tmpDir = retrieveTmpFile(bundleContext);
        tmpDir.mkdirs();
        String mountPoint = (String) reference.getProperty(Constants.MOUNTPOINT);
        String applicationName = (String) reference.getProperty(Constants.APPLICATION_NAME);
        Map<String, String> contextParams = (Map<String, String>) reference.getProperty(Constants.CONTEXT_PARAMS);
        FilterDelegator filterDelegator =
            new FilterDelegator(reference.getBundle().getBundleContext(), applicationName);
        return new PaxWicketApplicationFactory(bundleContext, webApplicationFactory, applicationName, mountPoint,
            contextParams, tmpDir, filterDelegator, componentInstantiationRegistratorTracker,
            pageFactoryInitiatorTracker);
    }

    private static File retrieveTmpFile(BundleContext bundleContext) {
        File tmpDir = bundleContext.getDataFile("tmp-dir");
        if (tmpDir == null) {
            throw new IllegalStateException("Platform needs file system access to work correctly.");
        }
        return tmpDir;
    }

    private PaxWicketApplicationFactory(BundleContext bundleContext, IWebApplicationFactory webApplicationFactory,
            String applicationName, String mountPoint, Map<String, String> contextParams, File tmpDir,
            FilterDelegator filterDelegator,
            ComponentInstantiationRegistratorTracker componentInstantiationRegistratorTracker,
            PageFactoryInitiatorTracker pageFactoryInitiatorTracker) {
        this.bundleContext = bundleContext;
        this.webApplicationFactory = webApplicationFactory;
        this.applicationName = applicationName;
        this.mountPoint = mountPoint;
        this.contextParams = contextParams;
        this.tmpDir = tmpDir;
        this.filterDelegator = filterDelegator;
        this.componentInstantiationRegistratorTracker = componentInstantiationRegistratorTracker;
        this.pageFactoryInitiatorTracker = pageFactoryInitiatorTracker;
    }

    public boolean isValidFactory() {
        return applicationName != null && mountPoint != null;
    }

    public WebApplication createApplication(WicketFilter filter) {
        WebApplication application = webApplicationFactory.createApplication(filter);
        // TODO: [PAXWICKET-230] find a better solution here than using only the class of the real factory...
        Enhancer e = new Enhancer();
        e.setSuperclass(application.getClass());
        e.setCallback(new WebApplicationWrapper());
        return (WebApplication) e.create();
    }

    private class WebApplicationWrapper implements MethodInterceptor {

        private DelegatingClassResolver delegatingClassResolver;
        private DelegatingComponentInstanciationListener delegatingComponentInstanciationListener;

        // TODO: [PAXWICKET-255] reintroduce
        // private PageMounterTracker mounterTracker;

        public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            if (isFinalizeMethod(method)) {
                // swallow finalize call
                return null;
            } else if (isEqualsMethod(method)) {
                return equals(args[0]) ? Boolean.TRUE : Boolean.FALSE;
            } else if (isHashCodeMethod(method)) {
                return new Integer(hashCode());
            } else if (isToStringMethod(method)) {
                return toString();
            } else if (isInitMethod(method)) {
                handleInit((WebApplication) object);
            } else if (isOnDestoryMethod(method)) {
                handleOnDestroy((WebApplication) object);
            }
            method.setAccessible(true);
            return methodProxy.invokeSuper(object, args);
        }

        /**
         * Checks if the method is derived from Object.equals()
         *
         * @param method method being tested
         * @return true if the method is derived from Object.equals(), false otherwise
         */
        private boolean isEqualsMethod(Method method) {
            return method.getReturnType() == boolean.class && method.getParameterTypes().length == 1 &&
                    method.getParameterTypes()[0] == Object.class && method.getName().equals("equals");
        }

        /**
         * Checks if the method is derived from Object.hashCode()
         *
         * @param method method being tested
         * @return true if the method is defined from Object.hashCode(), false otherwise
         */
        private boolean isHashCodeMethod(Method method) {
            return method.getReturnType() == int.class && method.getParameterTypes().length == 0 &&
                    method.getName().equals("hashCode");
        }

        /**
         * Checks if the method is derived from Object.toString()
         *
         * @param method method being tested
         * @return true if the method is defined from Object.toString(), false otherwise
         */
        private boolean isToStringMethod(Method method) {
            return method.getReturnType() == String.class && method.getParameterTypes().length == 0 &&
                    method.getName().equals("toString");
        }

        /**
         * Checks if the method is derived from Object.finalize()
         *
         * @param method method being tested
         * @return true if the method is defined from Object.finalize(), false otherwise
         */
        private boolean isFinalizeMethod(Method method) {
            return method.getReturnType() == void.class && method.getParameterTypes().length == 0 &&
                    method.getName().equals("finalize");
        }

        private boolean isInitMethod(Method method) {
            return method.getReturnType() == void.class && method.getParameterTypes().length == 0 &&
                    method.getName().equals("init");
        }

        private boolean isOnDestoryMethod(Method method) {
            return method.getReturnType() == void.class && method.getParameterTypes().length == 0 &&
                    method.getName().equals("onDestroy");
        }

        private void handleInit(WebApplication application) {
            initializeInjectionComponent(application);
            initializePageFactory(application);
            initializeClassResolver(application);
            // TODO [PAXWICKET-228] What should happen if two are created?
            // TODO: [PAXWICKET-255] reintroduce
            // mounterTracker = new PageMounterTracker(bundleContext, application, getApplicationName());
            // mounterTracker.open();
        }

        private void initializeClassResolver(WebApplication application) {
            delegatingClassResolver = new DelegatingClassResolver(bundleContext, applicationName);
            delegatingClassResolver.intialize();
            application.getApplicationSettings().setClassResolver(delegatingClassResolver);
        }

        private void initializePageFactory(WebApplication application) {
            pageFactoryInitiatorTracker.initiatePageFactoryForWicketApplication(application,applicationName);
        }

        private void initializeInjectionComponent(WebApplication application) {
            delegatingComponentInstanciationListener =
                new DelegatingComponentInstanciationListener(bundleContext, applicationName);
            delegatingComponentInstanciationListener.intialize();
            componentInstantiationRegistratorTracker.registerPaxWicketInjectorForApplication(
                delegatingComponentInstanciationListener, application);
        }

        private void handleOnDestroy(WebApplication application) {
            delegatingClassResolver.dispose();
            delegatingComponentInstanciationListener.dispose();
            componentInstantiationRegistratorTracker.removePaxWicketInjectorForApplication(application);
            pageFactoryInitiatorTracker.disposePageFactoryForWicketApplication(applicationName);
            // TODO: [PAXWICKET-255] reintroduce
            // mounterTracker.close();
            filterDelegator.dispose();
        }

    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public IWebApplicationFactory getWebApplicationFactory() {
        return webApplicationFactory;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public Map<String, String> getContextParams() {
        return contextParams;
    }

    public File getTmpDir() {
        return tmpDir;
    }

    public FilterDelegator getFilterDelegator() {
        return filterDelegator;
    }

    public void destroy(WicketFilter filter) {
        // Wicket 1.5 requires us to implement this method. Since it does not matter for Wicket14 we simple add it here.
        // nothing to do here I assume at least...
    }

}
