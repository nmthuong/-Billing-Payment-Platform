/*
 * Copyright 2010-2013 Ning, Inc.
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

package org.killbill.billing.util.cache;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.killbill.billing.ObjectType;
import org.killbill.billing.util.cache.Cachable.CacheType;
import org.killbill.billing.util.dao.NonEntityDao;
import org.skife.jdbi.v2.Handle;

import net.sf.ehcache.loader.CacheLoader;

@Singleton
public class TenantRecordIdCacheLoader extends BaseIdCacheLoader implements CacheLoader {

    private final NonEntityDao nonEntityDao;

    @Inject
    public TenantRecordIdCacheLoader(final NonEntityDao nonEntityDao) {
        super();
        this.nonEntityDao = nonEntityDao;
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.TENANT_RECORD_ID;
    }

    @Override
    protected Object doRetrieveOperation(final String rawKey, final ObjectType objectType, final Handle handle) {
        return nonEntityDao.retrieveTenantRecordIdFromObjectInTransaction(UUID.fromString(rawKey), objectType, null, handle);
    }
}
