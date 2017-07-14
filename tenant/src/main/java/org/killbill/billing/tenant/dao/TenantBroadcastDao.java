/*
 * Copyright 2014 Groupon, Inc
 * Copyright 2014 The Billing Project, LLC
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

package org.killbill.billing.tenant.dao;

import java.util.List;

import org.killbill.billing.callcontext.InternalTenantContext;
import org.killbill.billing.tenant.api.TenantApiException;
import org.killbill.billing.util.entity.Entity;
import org.killbill.billing.util.entity.dao.EntityDao;

public interface TenantBroadcastDao extends EntityDao<TenantBroadcastModelDao, Entity, TenantApiException> {

    public List<TenantBroadcastModelDao> getLatestEntriesFrom(final Long recordId);

    public TenantBroadcastModelDao getLatestEntry();
}
