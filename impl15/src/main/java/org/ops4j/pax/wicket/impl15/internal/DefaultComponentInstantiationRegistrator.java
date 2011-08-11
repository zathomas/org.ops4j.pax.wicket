package org.ops4j.pax.wicket.impl15.internal;

import org.apache.wicket.protocol.http.WebApplication;
import org.ops4j.pax.wicket.api.PaxWicketInjector;
import org.ops4j.pax.wicket.util.injection.ComponentInstantiationRegistrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultComponentInstantiationRegistrator implements ComponentInstantiationRegistrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultComponentInstantiationRegistrator.class);

    public boolean registerPaxWicketInjectorForApplication(PaxWicketInjector injector, WebApplication application) {
        try {
            application.getComponentInstantiationListeners().add(new ComponentInstantiationListenerFacade(injector));
        } catch (Exception e) {
            LOGGER.info("Wicket application does not seam to be impl14, but impl15.", e);
            return false;
        }
        return true;
    }

    public boolean disposePaxWicketInjectorForApplictation(WebApplication application) {
        return false;
    }
}
