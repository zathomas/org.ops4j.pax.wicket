package org.ops4j.pax.wicket.impl14.internal;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.protocol.http.WebApplication;
import org.ops4j.pax.wicket.api.PaxWicketInjector;
import org.ops4j.pax.wicket.util.injection.ComponentInstantiationRegistrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultComponentInstantiationRegistrator implements ComponentInstantiationRegistrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultComponentInstantiationRegistrator.class);

    private Map<WebApplication, ComponentInstantiationListenerFacade> registration =
        new HashMap<WebApplication, ComponentInstantiationListenerFacade>();

    public boolean registerPaxWicketInjectorForApplication(PaxWicketInjector injector, WebApplication application) {
        try {
            ComponentInstantiationListenerFacade componentInstantiationListenerFacade =
                new ComponentInstantiationListenerFacade(injector);
            application.addComponentInstantiationListener(componentInstantiationListenerFacade);
            registration.put(application, componentInstantiationListenerFacade);
        } catch (Exception e) {
            LOGGER.info("Wicket application does not seam to be impl14, but impl15.", e);
            return false;
        }
        return true;
    }

    public boolean disposePaxWicketInjectorForApplictation(WebApplication application) {
        try {
            application.removeComponentInstantiationListener(registration.remove(application));
        } catch (Exception e) {
            LOGGER.info("Wicket application does not seam to be impl14, but impl15.", e);
            return false;
        }
        return true;
    }

}
