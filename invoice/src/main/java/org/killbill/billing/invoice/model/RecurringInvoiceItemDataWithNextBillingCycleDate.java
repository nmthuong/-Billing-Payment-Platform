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

package org.killbill.billing.invoice.model;

import java.util.List;

import org.joda.time.LocalDate;
import org.killbill.billing.catalog.api.BillingMode;
import org.killbill.billing.invoice.generator.BillingIntervalDetail;

public class RecurringInvoiceItemDataWithNextBillingCycleDate {

    private final List<RecurringInvoiceItemData> itemData;
    private final BillingIntervalDetail billingIntervalDetail;

    public RecurringInvoiceItemDataWithNextBillingCycleDate(final List<RecurringInvoiceItemData> itemData, final BillingIntervalDetail billingIntervalDetail) {
        this.itemData = itemData;
        this.billingIntervalDetail = billingIntervalDetail;
    }

    public List<RecurringInvoiceItemData> getItemData() {
        return itemData;
    }

    public LocalDate getNextBillingCycleDate() {
        return billingIntervalDetail.getNextBillingCycleDate();
    }
}
