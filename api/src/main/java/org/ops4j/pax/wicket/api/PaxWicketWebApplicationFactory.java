package org.ops4j.pax.wicket.api;

/**
 * @author <a href="mailto:topping@codehaus.org">Brian Topping</a>
 * @version $Id$
 * @date 8/11/11 2:33 PM
 */

public interface PaxWicketWebApplicationFactory {
    void register();

    void dispose();

    String getApplicationName();

    void setApplicationName(String applicationName);

    String getMountPoint();

    void setMountPoint(String mountPoint);
}
