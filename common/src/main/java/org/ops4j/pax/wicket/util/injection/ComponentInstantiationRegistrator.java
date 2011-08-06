package org.ops4j.pax.wicket.util.injection;

import org.apache.wicket.protocol.http.WebApplication;
import org.ops4j.pax.wicket.api.PaxWicketInjector;

/**
 * Interface which controls the registration of the ComponentInstanciationListener.
 */
public interface ComponentInstantiationRegistrator {

    /**
     * Since the task of adding a component instanciation listener is dependent on the wicket version the task should be
     * delegated to the implementations. This method takes the injector and the application on which the injector should
     * be used. It returns if the registration was successful.
     */
    boolean registerPaxWicketInjectorForApplication(PaxWicketInjector injector, WebApplication application);

    /**
     * The same service which regsitered the listener has to made sure that it is removed again.
     */
    boolean disposePaxWicketInjectorForApplictation(WebApplication application);

}
