/*
 * Copyright 2010-2012 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
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

package org.killbill.billing.util.entity.dao;

/**
 * Transaction closure for EntitySqlDao queries
 *
 * @param <ReturnType> object type to return from the transaction
 */
public interface EntitySqlDaoTransactionWrapper<ReturnType> {

    /**
     * @param entitySqlDaoWrapperFactory factory to create EntitySqlDao instances
     * @return result from the transaction of type ReturnType
     */
    ReturnType inTransaction(EntitySqlDaoWrapperFactory entitySqlDaoWrapperFactory) throws Exception;
}
