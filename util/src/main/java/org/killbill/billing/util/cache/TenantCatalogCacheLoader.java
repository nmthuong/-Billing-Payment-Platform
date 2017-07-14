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

package org.killbill.billing.util.cache;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.killbill.billing.callcontext.InternalTenantContext;
import org.killbill.billing.catalog.api.CatalogApiException;
import org.killbill.billing.tenant.api.TenantInternalApi;
import org.killbill.billing.util.cache.Cachable.CacheType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TenantCatalogCacheLoader extends BaseCacheLoader {

    private final Logger log = LoggerFactory.getLogger(TenantCatalogCacheLoader.class);

    private final TenantInternalApi tenantApi;

    @Inject
    public TenantCatalogCacheLoader(final TenantInternalApi tenantApi) {
        super();
        this.tenantApi = tenantApi;
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.TENANT_CATALOG;
    }

    @Override
    public Object load(final Object key, final Object argument) {
        checkCacheLoaderStatus();

        if (!(key instanceof Long)) {
            throw new IllegalArgumentException("Unexpected key type of " + key.getClass().getName());
        }
        if (!(argument instanceof CacheLoaderArgument)) {
            throw new IllegalArgumentException("Unexpected argument type of " + argument.getClass().getName());
        }

        final Long tenantRecordId = (Long) key;
        final InternalTenantContext internalTenantContext = new InternalTenantContext(tenantRecordId);
        final CacheLoaderArgument cacheLoaderArgument = (CacheLoaderArgument) argument;

        if (cacheLoaderArgument.getArgs() == null || !(cacheLoaderArgument.getArgs()[0] instanceof LoaderCallback)) {
            throw new IllegalArgumentException("Missing LoaderCallback from the arguments ");
        }

        final LoaderCallback callback = (LoaderCallback) cacheLoaderArgument.getArgs()[0];
        final List<String> catalogXMLs = tenantApi.getTenantCatalogs(internalTenantContext);
        if (catalogXMLs.isEmpty()) {
            return null;
        }
        try {
            log.info("Loading catalog cache for tenantRecordId='{}'", internalTenantContext.getTenantRecordId());
            return callback.loadCatalog(catalogXMLs, tenantRecordId);
        } catch (final CatalogApiException e) {
            throw new IllegalStateException(String.format("Failed to de-serialize catalog for tenantRecordId='%s'", internalTenantContext.getTenantRecordId()), e);
        }
    }

    public interface LoaderCallback {
        public Object loadCatalog(final List<String> catalogXMLs, final Long tenantRecordId) throws CatalogApiException;
    }
}
