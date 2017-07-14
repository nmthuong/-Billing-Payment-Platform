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

package org.killbill.billing.payment.dao;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

import org.killbill.billing.payment.dao.TransactionStatusCollectionBinder.TransactionStatusCollectionBinderFactory;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;

@BindingAnnotation(TransactionStatusCollectionBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface TransactionStatusCollectionBinder {

    public static class TransactionStatusCollectionBinderFactory implements BinderFactory {

        @Override
        public Binder build(Annotation annotation) {
            return new Binder<TransactionStatusCollectionBinder, Collection<String>>() {

                @Override
                public void bind(SQLStatement<?> query, TransactionStatusCollectionBinder bind, Collection<String> allTransactionStatus) {
                    query.define("statuses", allTransactionStatus);

                    int idx = 0;
                    for (String transactionStatus : allTransactionStatus) {
                        query.bind("status_" + idx, transactionStatus);
                        idx++;
                    }
                }
            };
        }
    }
}
