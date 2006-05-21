/*
 * Copyright 2006 Niclas Hedhman.
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
package org.ops4j.pax.wicket.service.internal;

import wicket.IPageFactory;
import wicket.Page;
import wicket.PageParameters;
import org.osgi.framework.BundleContext;
import java.util.List;

public class DelegatingPageFactory
    implements IPageFactory
{
    private PageFactoryTracker m_pageFactoryTracker;

    public DelegatingPageFactory( BundleContext bundleContext, String applicationName )
    {
        m_pageFactoryTracker = new PageFactoryTracker( bundleContext, applicationName );
    }

    /**
     * Creates a new page using a page class.
     *
     * @param pageClass The page class to instantiate
     *
     * @return The page
     *
     * @throws wicket.WicketRuntimeException Thrown if the page cannot be constructed
     */
    public Page newPage( final Class pageClass )
    {
        List<IPageFactory> pageFactories = m_pageFactoryTracker.getPageFactories();
        for( IPageFactory factory: pageFactories )
        {
            Page page = factory.newPage( pageClass );
            if( page != null )
            {
                return page;
            }
        }
        return null;
    }

    /**
     * Creates a new Page, passing PageParameters to the Page constructor if
     * such a constructor exists. If no such constructor exists and the
     * parameters argument is null or empty, then any available default
     * constructor will be used.
     *
     * @param pageClass  The class of Page to create
     * @param parameters Any parameters to pass to the Page's constructor
     *
     * @return The new page
     *
     * @throws wicket.WicketRuntimeException Thrown if the page cannot be constructed
     */
    public Page newPage( final Class pageClass, final PageParameters parameters )
    {
        List<IPageFactory> pageFactories = m_pageFactoryTracker.getPageFactories();
        for( IPageFactory factory: pageFactories )
        {
            Page page = factory.newPage( pageClass, parameters );
            if( page != null )
            {
                return page;
            }
        }
        return null;
    }
}
