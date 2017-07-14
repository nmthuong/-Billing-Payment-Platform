/*
 * Copyright 2016 Groupon, Inc
 * Copyright 2016 The Billing Project, LLC
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

package org.killbill.billing.payment.caching;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Named;

import org.killbill.automaton.DefaultStateMachineConfig;
import org.killbill.automaton.StateMachineConfig;
import org.killbill.billing.ErrorCode;
import org.killbill.billing.ObjectType;
import org.killbill.billing.callcontext.InternalTenantContext;
import org.killbill.billing.payment.api.PaymentApiException;
import org.killbill.billing.payment.glue.PaymentModule;
import org.killbill.billing.tenant.api.TenantInternalApi;
import org.killbill.billing.tenant.api.TenantInternalApi.CacheInvalidationCallback;
import org.killbill.billing.tenant.api.TenantKV.TenantKey;
import org.killbill.billing.util.cache.Cachable.CacheType;
import org.killbill.billing.util.cache.CacheController;
import org.killbill.billing.util.cache.CacheControllerDispatcher;
import org.killbill.billing.util.cache.CacheLoaderArgument;
import org.killbill.billing.util.cache.TenantStateMachineConfigCacheLoader.LoaderCallback;
import org.killbill.billing.util.callcontext.InternalCallContextFactory;
import org.killbill.xmlloader.XMLLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

public class EhCacheStateMachineConfigCache implements StateMachineConfigCache {

    private static final Logger logger = LoggerFactory.getLogger(EhCacheStateMachineConfigCache.class);

    private final TenantInternalApi tenantInternalApi;
    private final CacheController cacheController;
    private final CacheInvalidationCallback cacheInvalidationCallback;
    private final LoaderCallback loaderCallback;

    private StateMachineConfig defaultPaymentStateMachineConfig;

    @Inject
    public EhCacheStateMachineConfigCache(final TenantInternalApi tenantInternalApi,
                                          final CacheControllerDispatcher cacheControllerDispatcher,
                                          @Named(PaymentModule.STATE_MACHINE_CONFIG_INVALIDATION_CALLBACK) final CacheInvalidationCallback cacheInvalidationCallback) {
        this.tenantInternalApi = tenantInternalApi;
        // Can be null if mis-configured (e.g. missing in ehcache.xml)
        this.cacheController = cacheControllerDispatcher.getCacheController(CacheType.TENANT_PAYMENT_STATE_MACHINE_CONFIG);
        this.cacheInvalidationCallback = cacheInvalidationCallback;
        this.loaderCallback = new LoaderCallback() {
            public Object loadStateMachineConfig(final String stateMachineConfigXML) throws PaymentApiException {
                tenantInternalApi.initializeCacheInvalidationCallback(TenantKey.PLUGIN_PAYMENT_STATE_MACHINE_, cacheInvalidationCallback);

                try {
                    final InputStream stream = new ByteArrayInputStream(stateMachineConfigXML.getBytes());
                    return XMLLoader.getObjectFromStream(new URI("dummy"), stream, DefaultStateMachineConfig.class);
                } catch (final Exception e) {
                    // TODO 0.17 proper error code
                    throw new PaymentApiException(e, ErrorCode.PAYMENT_INTERNAL_ERROR, "Invalid payment state machine config");
                }
            }
        };
    }

    @Override
    public void loadDefaultPaymentStateMachineConfig(final String url) throws PaymentApiException {
        if (url != null) {
            try {
                defaultPaymentStateMachineConfig = XMLLoader.getObjectFromString(Resources.getResource(url).toExternalForm(), DefaultStateMachineConfig.class);
            } catch (final Exception e) {
                // TODO 0.17 proper error code
                throw new PaymentApiException(e, ErrorCode.PAYMENT_INTERNAL_ERROR, "Invalid default payment state machine config");
            }
        }
    }

    @Override
    public StateMachineConfig getPaymentStateMachineConfig(final String pluginName, final InternalTenantContext tenantContext) throws PaymentApiException {
        if (tenantContext.getTenantRecordId() == InternalCallContextFactory.INTERNAL_TENANT_RECORD_ID || cacheController == null) {
            return defaultPaymentStateMachineConfig;
        }

        final String pluginConfigKey = getCacheKeyName(pluginName, tenantContext);
        final CacheLoaderArgument cacheLoaderArgument = createCacheLoaderArgument(pluginName);
        try {
            StateMachineConfig pluginPaymentStateMachineConfig = (StateMachineConfig) cacheController.get(pluginConfigKey, cacheLoaderArgument);
            // It means we are using the default state machine config in a multi-tenant deployment
            if (pluginPaymentStateMachineConfig == null) {
                pluginPaymentStateMachineConfig = defaultPaymentStateMachineConfig;
                cacheController.add(pluginConfigKey, pluginPaymentStateMachineConfig);
            }
            return pluginPaymentStateMachineConfig;
        } catch (final IllegalStateException e) {
            // TODO 0.17 proper error code
            throw new PaymentApiException(e, ErrorCode.PAYMENT_INTERNAL_ERROR, "Invalid payment state machine");
        }
    }

    // See also DefaultTenantUserApi - we use the same conventions as the main XML cache (so we can re-use the invalidation code)
    private String getCacheKeyName(final String pluginName, final InternalTenantContext internalContext) {
        final StringBuilder tenantKey = new StringBuilder(TenantKey.PLUGIN_PAYMENT_STATE_MACHINE_.toString());
        tenantKey.append(pluginName);
        tenantKey.append(CacheControllerDispatcher.CACHE_KEY_SEPARATOR);
        tenantKey.append(internalContext.getTenantRecordId());
        return tenantKey.toString();
    }

    @Override
    public void clearPaymentStateMachineConfig(final String pluginName, final InternalTenantContext tenantContext) {
        if (tenantContext.getTenantRecordId() != InternalCallContextFactory.INTERNAL_TENANT_RECORD_ID && cacheController != null) {
            final String key = getCacheKeyName(pluginName, tenantContext);
            cacheController.remove(key);
        }
    }

    private CacheLoaderArgument createCacheLoaderArgument(final String pluginName) {
        final Object[] args = new Object[2];
        args[0] = loaderCallback;
        args[1] = pluginName;
        final ObjectType irrelevant = null;
        final InternalTenantContext notUsed = null;
        return new CacheLoaderArgument(irrelevant, args, notUsed);
    }
}
