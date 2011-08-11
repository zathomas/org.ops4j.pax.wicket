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
package org.ops4j.pax.wicket.util.injection;

import net.sf.cglib.proxy.Factory;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.api.PaxWicketInjector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPaxWicketInjector implements PaxWicketInjector {

    protected List<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<Field>();

        while (clazz != null && !isBoundaryClass(clazz)) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(PaxWicketBean.class)) {
                    continue;
                }
                fields.add(field);
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }


    protected boolean isBoundaryClass(Class<?> clazz) {
        // Check names with reflection to avoid a static dependency
        String className = clazz.getName();
        if (className.equals("org.apache.wicket.WebPage")
                || className.equals("org.apache.wicket.Page")
                || className.equals("org.apache.wicket.markup.html.panel.Panel")
                || className.equals("org.apache.wicket.MarkupContainer")
                || className.equals("org.apache.wicket.Component")
                || className.equals("org.apache.wicket.protocol.http.WebSession")
                || className.equals("org.apache.wicket.Session")
                || clazz.equals(Object.class)) {
            return true;
        }
        return false;
    }

    protected void setField(Object component, Field field, Object proxy) {
        try {
            checkAccessabilityOfField(field);
            field.set(component, proxy);
        } catch (Exception e) {
            throw new RuntimeException("Bumm", e);
        }
    }

    private void checkAccessabilityOfField(Field field) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    protected Class<?> getBeanType(Field field) {
        Class<?> beanType = field.getType();
        return beanType;
    }

    protected boolean doesComponentContainPaxWicketBeanAnnotatedFields(Object component) {
        Class<?> clazz = component.getClass();
        if (Factory.class.isInstance(component)) {
            clazz = clazz.getSuperclass();
        }
        while (clazz != null && !isBoundaryClass(clazz)) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(PaxWicketBean.class)) {
                    return true;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }
}
