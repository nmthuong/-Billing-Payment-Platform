/*
 * Copyright 2014 The Billing Project, LLC
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

package org.killbill.billing.invoice.usage;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.killbill.billing.catalog.api.BillingMode;
import org.killbill.billing.catalog.api.Tier;
import org.killbill.billing.catalog.api.TieredBlock;
import org.killbill.billing.catalog.api.Usage;
import org.killbill.billing.catalog.api.UsageType;
import org.killbill.billing.junction.BillingEvent;
import org.killbill.billing.junction.BillingEventSet;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class UsageUtils {

    public static List<TieredBlock> getConsumableInArrearTieredBlocks(final Usage usage, final String unitType) {

        Preconditions.checkArgument(usage.getBillingMode() == BillingMode.IN_ARREAR && usage.getUsageType() == UsageType.CONSUMABLE);
        Preconditions.checkArgument(usage.getTiers().length > 0);

        final List<TieredBlock> result = Lists.newLinkedList();
        for (Tier tier : usage.getTiers()) {
            for (TieredBlock tierBlock : tier.getTieredBlocks()) {
                if (tierBlock.getUnit().getName().equals(unitType)) {
                    result.add(tierBlock);
                }
            }
        }
        return result;
    }

    public static Set<String> getConsumableInArrearUnitTypes(final Usage usage) {

        Preconditions.checkArgument(usage.getBillingMode() == BillingMode.IN_ARREAR && usage.getUsageType() == UsageType.CONSUMABLE);
        Preconditions.checkArgument(usage.getTiers().length > 0);

        final Set<String> result = new HashSet<String>();
        for (Tier tier : usage.getTiers()) {
            for (TieredBlock tierBlock : tier.getTieredBlocks()) {
                result.add(tierBlock.getUnit().getName());
            }
        }
        return result;
    }

}
