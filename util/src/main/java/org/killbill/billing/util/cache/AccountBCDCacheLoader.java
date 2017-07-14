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

import java.util.UUID;

import org.killbill.billing.callcontext.InternalTenantContext;
import org.killbill.billing.util.cache.Cachable.CacheType;

public class AccountBCDCacheLoader extends BaseCacheLoader {

    @Override
    public CacheType getCacheType() {
        return CacheType.ACCOUNT_BCD;
    }

    @Override
    public Object load(final Object key, final Object argument) {

        checkCacheLoaderStatus();

        if (!(key instanceof UUID)) {
            throw new IllegalArgumentException("Unexpected key type of " + key.getClass().getName());
        }

        if (!(argument instanceof CacheLoaderArgument)) {
            throw new IllegalArgumentException("Unexpected argument type of " + argument.getClass().getName());
        }

        final CacheLoaderArgument cacheLoaderArgument = (CacheLoaderArgument) argument;

        if (cacheLoaderArgument.getArgs() == null ||
            !(cacheLoaderArgument.getArgs()[0] instanceof LoaderCallback)) {
            throw new IllegalArgumentException("Missing LoaderCallback from the arguments ");
        }

        final LoaderCallback callback = (LoaderCallback) cacheLoaderArgument.getArgs()[0];
        return callback.loadAccountBCD((UUID) key, cacheLoaderArgument.getInternalTenantContext());
    }

    public interface LoaderCallback {
        Object loadAccountBCD(final UUID accountId, final InternalTenantContext context);
    }
}
