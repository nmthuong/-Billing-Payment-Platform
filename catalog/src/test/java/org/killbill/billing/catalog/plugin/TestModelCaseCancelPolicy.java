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

package org.killbill.billing.catalog.plugin;

import org.killbill.billing.catalog.api.BillingActionPolicy;
import org.killbill.billing.catalog.api.BillingPeriod;
import org.killbill.billing.catalog.api.PhaseType;
import org.killbill.billing.catalog.api.PriceList;
import org.killbill.billing.catalog.api.Product;
import org.killbill.billing.catalog.api.ProductCategory;
import org.killbill.billing.catalog.api.rules.CaseCancelPolicy;

public class TestModelCaseCancelPolicy extends TestModelCasePhase implements CaseCancelPolicy {

    private final BillingActionPolicy billingActionPolicy;

    public TestModelCaseCancelPolicy(final Product product,
                                     final ProductCategory productCategory,
                                     final BillingPeriod billingPeriod,
                                     final PriceList priceList,
                                     final PhaseType phaseType,
                                     final BillingActionPolicy billingActionPolicy) {
        super(product, productCategory, billingPeriod, priceList, phaseType);
        this.billingActionPolicy = billingActionPolicy;
    }

    @Override
    public BillingActionPolicy getBillingActionPolicy() {
        return billingActionPolicy;
    }
}
