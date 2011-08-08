package org.ops4j.pax.wicket.util;

import org.apache.wicket.protocol.http.WebApplication;

/**
 * Since the registration of a page factory differs between Wicket 14 and Wicket 15 this interface is used to delegate
 * the registration to the various implementations.
 */
public interface PageFactoryInitiator {

    /**
     * After this method is called it is assumed that a PageFactory had been registered for the WicketApplication.
     */
    boolean registerPaxWicketPageFactoryForApplication(WebApplication application, String applicationName);

    /**
     * After this method is called it is assumed that a PageFactory had been disposed for the WicketApplication.
     */
    boolean disposePaxWicketPageFactoryForApplication(String applicationName);

}
