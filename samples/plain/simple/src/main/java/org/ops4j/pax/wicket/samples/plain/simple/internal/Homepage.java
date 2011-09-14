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
package org.ops4j.pax.wicket.samples.plain.simple.internal;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * Very simple page providing entry points into various other examples.
 */
public class Homepage extends WebPage {

  private static final Logger LOG = LoggerFactory.getLogger(Homepage.class);

  private static final long serialVersionUID = 1L;

  public Homepage() {
    add(new Label("oneComponent", "Welcome to the most simple pax-wicket application"));
    Session adminSession = null;
    try {
      Mongo m = new Mongo("127.0.0.1");
      DB db = m.getDB("test");
      Set<String> collectionNames = db.getCollectionNames();


      DBCollection foo = db.getCollection("foo");
      foo.insert(new com.mongodb.BasicDBObject().append("visitedAt", new Date()));

      final DBCursor cur = foo.find();
      List<DBObject> results = new ArrayList<DBObject>();
      while (cur.hasNext()) {
        results.add(cur.next());
      }

      add(new ListView("visits", results) {
        // This method is called for each 'entry' in the list.
        @Override
        protected void populateItem(ListItem item) {
          item.add(new Label("visit", item.getModelObject().toString()));
        }
      });

      BundleContext bundleContext = Activator.getBundleContext();
      List<ServiceReference> serviceReferences = filterServiceRefs(bundleContext.getAllServiceReferences(null, null));
      add(new ListView("services", serviceReferences) {
        @Override
        protected void populateItem(ListItem item) {
          LOG.info(item.getModelObject().toString());
          item.add(new Label("service", item.getModelObject().toString()));
        }
      });

      Repository repository = (Repository) bundleContext.getService(bundleContext.getServiceReference("org.sakaiproject.nakamura.api.lite.Repository"));
      EventAdmin eventAdmin = (EventAdmin) bundleContext.getService(bundleContext.getServiceReference("org.osgi.service.event.EventAdmin"));

      adminSession = repository.loginAdministrative();
      Authorizable zach = adminSession.getAuthorizableManager().findAuthorizable("zach");

      add(new Label("emailAddress", (String) zach.getProperty("email")));

      eventAdmin.sendEvent(new Event("hello-from-wicket", new Hashtable()));

    } catch (UnknownHostException e) {
      LOG.error(e.getMessage());
    } catch (InvalidSyntaxException e) {
      LOG.error(e.getMessage());
    } catch (AccessDeniedException e) {
      LOG.error(e.getMessage());
    } catch (ClientPoolException e) {
      LOG.error(e.getMessage());
    } catch (StorageClientException e) {
      LOG.error(e.getMessage());
    } finally {
      if (adminSession != null) {
        try {
          adminSession.logout();
        } catch (ClientPoolException e) {
          LOG.error(e.getMessage());
        }
      }
    }

  }

  private List<ServiceReference> filterServiceRefs(ServiceReference[] allServiceReferences) {
    List<ServiceReference> filteredServiceReferences = new ArrayList<ServiceReference>();
    for (ServiceReference reference : allServiceReferences) {
      String serviceId = (String) reference.getBundle().getSymbolicName();
      LOG.info("service ID [{}]", serviceId);
      if (serviceId.startsWith("org.sakaiproject")) {
        filteredServiceReferences.add(reference);
      }
    }
    return filteredServiceReferences;
  }
}
