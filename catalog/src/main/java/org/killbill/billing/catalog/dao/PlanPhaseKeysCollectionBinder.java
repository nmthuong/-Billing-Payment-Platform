/*
 * Copyright 2014-2015 Groupon, Inc
 * Copyright 2014-2015 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.catalog.dao;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

import org.killbill.billing.catalog.dao.PlanPhaseKeysCollectionBinder.PlanPhaseKeysCollectionBinderFactory;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;

@BindingAnnotation(PlanPhaseKeysCollectionBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface PlanPhaseKeysCollectionBinder {

    public static class PlanPhaseKeysCollectionBinderFactory implements BinderFactory {

        @Override
        public Binder build(Annotation annotation) {
            return new Binder<PlanPhaseKeysCollectionBinder, Collection<String>>() {

                @Override
                public void bind(SQLStatement<?> query, PlanPhaseKeysCollectionBinder bind, Collection<String> keys) {
                    query.define("keys", keys);

                    int idx = 0;
                    for (String state : keys) {
                        query.bind("key_" + idx, state);
                        idx++;
                    }
                }
            };
        }
    }
}
