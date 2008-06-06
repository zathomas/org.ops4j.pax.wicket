/*
 * Copyright 2006 Niclas Hedhman.
 * Copyright 2006 Edward F. Yakop
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.wicket.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.settings.IApplicationSettings;
import org.apache.wicket.settings.IDebugSettings;
import org.apache.wicket.settings.IExceptionSettings;
import org.apache.wicket.settings.IFrameworkSettings;
import org.apache.wicket.settings.IMarkupSettings;
import org.apache.wicket.settings.IPageSettings;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.settings.ISecuritySettings;
import org.apache.wicket.settings.ISessionSettings;
import static org.ops4j.lang.NullArgumentException.validateNotEmpty;
import static org.ops4j.lang.NullArgumentException.validateNotNull;
import static org.ops4j.pax.wicket.api.ContentSource.APPLICATION_NAME;
import org.ops4j.pax.wicket.api.MountPointInfo;
import org.ops4j.pax.wicket.api.PageMounter;
import org.ops4j.pax.wicket.api.PaxWicketAuthenticator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public final class PaxAuthenticatedWicketApplication extends AuthenticatedWebApplication
{

    private static final Roles EMPTY_ROLES = new Roles();

    private static final AuthenticatedToken TOKEN_NOT_AUTHENTICATED = new AuthenticatedToken();

    private final BundleContext bundleContext;
    private final String applicationName;
    private final PageMounter pageMounter;
    private PageMounterTracker mounterTracker;

    protected Class<? extends Page> homepageClass;
    private final PaxWicketPageFactory factory;
    private final DelegatingClassResolver delegatingClassResolver;
    private final PaxWicketAuthenticator authenticator;
    private final Class<? extends WebPage> signInPage;
    private final HashMap<AuthenticatedToken, Roles> roles;
    private final List<ServiceRegistration> serviceRegistrations;

    public PaxAuthenticatedWicketApplication(
        BundleContext aBundleContext,
        String anApplicationName,
        PageMounter aPageMounter,
        Class<? extends Page> aHomePageClass,
        PaxWicketPageFactory aFactory,
        DelegatingClassResolver aDelegatingClassResolver,
        PaxWicketAuthenticator anAuthenticator,
        Class<? extends WebPage> aSignInPage )
        throws IllegalArgumentException
    {
        validateNotNull( aBundleContext, "aBundleContext" );
        validateNotEmpty( anApplicationName, "anApplicationName" );
        validateNotNull( aHomePageClass, "aHomePageClass" );
        validateNotNull( aFactory, "aFactory" );
        validateNotNull( aDelegatingClassResolver, "aDelegatingClassResolver" );
        validateNotNull( anAuthenticator, "anAuthenticator" );
        validateNotNull( aSignInPage, "aSignInPage" );

        bundleContext = aBundleContext;
        applicationName = anApplicationName;
        pageMounter = aPageMounter;
        factory = aFactory;
        homepageClass = aHomePageClass;
        delegatingClassResolver = aDelegatingClassResolver;
        authenticator = anAuthenticator;
        signInPage = aSignInPage;
        roles = new HashMap<AuthenticatedToken, Roles>();
        serviceRegistrations = new ArrayList<ServiceRegistration>();
    }

    /**
     * Application subclasses must specify a home page class by implementing this abstract method.
     *
     * @return Home page class for this application
     */
    @Override
    public Class<? extends Page> getHomePage()
    {
        return homepageClass;
    }

    @Override
    protected final void init()
    {
        super.init();

        IApplicationSettings applicationSettings = getApplicationSettings();
        applicationSettings.setClassResolver( delegatingClassResolver );
        addWicketService( IApplicationSettings.class, applicationSettings );

        ISessionSettings sessionSettings = getSessionSettings();
        sessionSettings.setPageFactory( factory );
        addWicketService( ISessionSettings.class, sessionSettings );

//        addWicketService( IAjaxSettings.class, getAjaxSettings() );
        addWicketService( IDebugSettings.class, getDebugSettings() );
        addWicketService( IExceptionSettings.class, getExceptionSettings() );
        addWicketService( IFrameworkSettings.class, getFrameworkSettings() );
        addWicketService( IMarkupSettings.class, getMarkupSettings() );
        addWicketService( IPageSettings.class, getPageSettings() );
        addWicketService( IRequestCycleSettings.class, getRequestCycleSettings() );
        addWicketService( IResourceSettings.class, getResourceSettings() );
        addWicketService( ISecuritySettings.class, getSecuritySettings() );

        if( null != pageMounter )
        {
            for( MountPointInfo bookmark : pageMounter.getMountPoints() )
            {
                mount( bookmark.getCodingStrategy() );
            }
        }

        // Now add a tracker so we can still mount pages later
        mounterTracker = new PageMounterTracker( bundleContext, this, applicationName );
        mounterTracker.open();
    }

    @Override
    protected void onDestroy()
    {
        if( mounterTracker != null )
        {
            mounterTracker.close();
            mounterTracker = null;
        }

        for( ServiceRegistration reg : serviceRegistrations )
        {
            reg.unregister();
        }
        serviceRegistrations.clear();

        super.onDestroy();
    }

    /**
     * @return AuthenticatedWebSession subclass to use in this authenticated web application.
     */
    @Override
    protected final Class<? extends AuthenticatedWebSession> getWebSessionClass()
    {
        return PaxWicketSession.class;
    }

    @Override
    public final Session newSession( Request request, Response response )
    {
        return new PaxWicketSession( request );
    }

    /**
     * @return Subclass of sign-in page
     */
    @Override
    protected Class<? extends WebPage> getSignInPageClass()
    {
        return signInPage;
    }

    final AuthenticatedToken authenticate( String username, String password )
    {
        if( authenticator == null )
        {
            return TOKEN_NOT_AUTHENTICATED;
        }

        Roles roles = authenticator.authenticate( username, password );
        if( roles != null )
        {
            AuthenticatedToken authenticatedToken = new AuthenticatedToken();
            this.roles.put( authenticatedToken, roles );
            return authenticatedToken;
        }

        return null;
    }

    final Roles getRoles( AuthenticatedToken token )
    {
        if( token == null || token == TOKEN_NOT_AUTHENTICATED )
        {
            return EMPTY_ROLES;
        }

        return roles.get( token );
    }

    private <T> void addWicketService( final Class<T> service, final T implementation )
    {
        Properties props = new Properties();
        props.setProperty( "applicationId", applicationName );
        props.setProperty( APPLICATION_NAME, applicationName );

        serviceRegistrations.add( bundleContext.registerService( service.getName(), implementation, props ) );
    }

    @Override
    protected final WebRequest newWebRequest( HttpServletRequest aRequest )
    {
        return new PaxWicketRequest( aRequest );
    }
}
