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
package org.ops4j.pax.wicket.impl14.internal.injection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.wicket.Component;
import org.junit.Test;
import org.ops4j.pax.wicket.impl14.impl14.api.PaxWicketInjector;
import org.ops4j.pax.wicket.internal.injection.ComponentInstantiationListenerFacade;

public class ComponentInstantiationListenerFacadeTest {

    @Test
    public void testCallToFacade_shouldBeForwardedToRealClass() {
        Component component = Mockito.mock(Component.class);
        PaxWicketInjector injector = Mockito.mock(PaxWicketInjector.class);
        new ComponentInstantiationListenerFacade(injector).onInstantiation(component);
        Mockito.verify(injector).inject(component);
    }

}
