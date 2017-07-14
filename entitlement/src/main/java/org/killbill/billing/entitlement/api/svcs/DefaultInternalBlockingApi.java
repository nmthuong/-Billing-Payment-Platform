/*
 * Copyright 2010-2013 Ning, Inc.
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

package org.killbill.billing.entitlement.api.svcs;

import java.util.List;
import java.util.UUID;

import org.killbill.billing.callcontext.InternalCallContext;
import org.killbill.billing.callcontext.InternalTenantContext;
import org.killbill.billing.overdue.OverdueService;
import org.killbill.clock.Clock;
import org.killbill.billing.entitlement.api.BlockingState;
import org.killbill.billing.entitlement.api.BlockingStateType;
import org.killbill.billing.entitlement.dao.BlockingStateDao;
import org.killbill.billing.entitlement.engine.core.EntitlementUtils;
import org.killbill.billing.junction.BlockingInternalApi;
import org.killbill.billing.junction.DefaultBlockingState;

import com.google.inject.Inject;

public class DefaultInternalBlockingApi implements BlockingInternalApi {

    private final EntitlementUtils entitlementUtils;
    private final BlockingStateDao dao;
    private final Clock clock;

    @Inject
    public DefaultInternalBlockingApi(final EntitlementUtils entitlementUtils, final BlockingStateDao dao, final Clock clock) {
        this.entitlementUtils = entitlementUtils;
        this.dao = dao;
        this.clock = clock;
    }

    @Override
    public BlockingState getBlockingStateForService(final UUID overdueableId, final BlockingStateType blockingStateType, final String serviceName, final InternalTenantContext context) {
        return dao.getBlockingStateForService(overdueableId, blockingStateType, serviceName, context);
    }

    @Override
    public List<BlockingState> getBlockingAllForAccount(final InternalTenantContext context) {
        return dao.getBlockingAllForAccountRecordId(context);
    }

    @Override
    public void setBlockingState(final BlockingState state, final InternalCallContext context) {
        entitlementUtils.setBlockingStateAndPostBlockingTransitionEvent(state, context);
    }
}
