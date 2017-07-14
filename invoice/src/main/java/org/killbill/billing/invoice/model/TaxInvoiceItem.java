/*
 * Copyright 2014 Groupon, Inc
 *
 * Groupon licenses this file to you under the Apache License, version 2.0
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

import java.math.BigDecimal;
import java.util.UUID;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.invoice.api.InvoiceItemType;
import org.killbill.billing.util.UUIDs;

public class TaxInvoiceItem extends InvoiceItemBase {

    public TaxInvoiceItem(final UUID invoiceId, final UUID accountId, @Nullable final UUID bundleId, @Nullable final String description,
                          final LocalDate date, final BigDecimal amount, final Currency currency) {
        this(UUIDs.randomUUID(), invoiceId, accountId, bundleId, description, date, amount, currency);
    }

    public TaxInvoiceItem(final UUID id, final UUID invoiceId, final UUID accountId, @Nullable final UUID bundleId,
                          @Nullable final String description, final LocalDate date, final BigDecimal amount, final Currency currency) {
        this(id, null, invoiceId, accountId, bundleId, null, null, null, null, date, description, amount, currency, null);
    }

    public TaxInvoiceItem(final UUID id, @Nullable final DateTime createdDate, final UUID invoiceId, final UUID accountId, @Nullable final UUID bundleId,
                          @Nullable final UUID subscriptionId, @Nullable final String planName, @Nullable final String phaseName, @Nullable final String usageName,
                          final LocalDate date, @Nullable final String description, final BigDecimal amount, final Currency currency, @Nullable final UUID linkedItemId) {
        super(id, createdDate, invoiceId, accountId, bundleId, subscriptionId, description, planName, phaseName, usageName, date, null, amount, currency, linkedItemId);
    }

    @Override
    public String getDescription() {
        if (description != null) {
            return description;
        }

        return "Tax";
    }

    @Override
    public InvoiceItemType getInvoiceItemType() {
        return InvoiceItemType.TAX;
    }
}
