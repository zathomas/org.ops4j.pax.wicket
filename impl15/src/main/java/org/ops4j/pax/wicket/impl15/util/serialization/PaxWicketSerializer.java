package org.ops4j.pax.wicket.impl15.util.serialization;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.apache.wicket.settings.IApplicationSettings;
import org.ops4j.pax.wicket.impl15.util.serialization.deployment.PaxWicketObjectInputStream;
import org.ops4j.pax.wicket.impl15.util.serialization.deployment.PaxWicketObjectOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author <a href="mailto:topping@codehaus.org">Brian Topping</a>
 * @version $Id$
 * @date 8/11/11 4:35 PM
 */

public class PaxWicketSerializer extends JavaSerializer {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(PaxWicketSerializer.class);

    public PaxWicketSerializer(String applicationKey) {
        super(applicationKey);
    }

    @Override
    protected ObjectInputStream newObjectInputStream(InputStream in) throws IOException {
        IApplicationSettings appSettings = WebApplication.get().getApplicationSettings();
        return new PaxWicketObjectInputStream(in, appSettings.getClassResolver());
    }

    @Override
    protected ObjectOutputStream newObjectOutputStream(OutputStream out) throws IOException {
        return new PaxWicketObjectOutputStream(out);
    }
}
