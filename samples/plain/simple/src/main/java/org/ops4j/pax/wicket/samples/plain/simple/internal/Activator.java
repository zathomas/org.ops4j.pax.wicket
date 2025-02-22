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

import org.ops4j.pax.wicket.util.DefaultWebApplicationFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

  private static BundleContext bundleContext;

  private DefaultWebApplicationFactory applicationFactory;

  public void start(BundleContext context) throws Exception {
    applicationFactory =
      new DefaultWebApplicationFactory(context, WicketApplication.class, "plain.simple", "plain/simple");
    applicationFactory.register();
    this.bundleContext = context;
  }

  public void stop(BundleContext context) throws Exception {
    applicationFactory.dispose();
  }

  public static BundleContext getBundleContext() {
    return bundleContext;
  }

}
