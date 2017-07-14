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

package org.killbill.billing.invoice.notification;

import java.util.UUID;

import org.joda.time.DateTime;
import org.killbill.billing.util.jackson.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestNextBillingDateNotificationKey {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test(groups = "fast")
    public void testBasic() throws Exception {

        final UUID uuidKey = UUID.randomUUID();
        final DateTime targetDate = new DateTime();
        final Boolean isDryRunForInvoiceNotification = Boolean.FALSE;

        final NextBillingDateNotificationKey key = new NextBillingDateNotificationKey(uuidKey, targetDate, isDryRunForInvoiceNotification);
        final String json = mapper.writeValueAsString(key);

        final NextBillingDateNotificationKey result = mapper.readValue(json, NextBillingDateNotificationKey.class);
        Assert.assertEquals(result.getUuidKey(), uuidKey);
        Assert.assertEquals(result.getTargetDate().compareTo(targetDate), 0);
        Assert.assertEquals(result.isDryRunForInvoiceNotification(), isDryRunForInvoiceNotification);
    }

    @Test(groups = "fast")
    public void testWithMissingFields() throws Exception {
        final String json = "{\"uuidKey\":\"a38c363f-b25b-4287-8ebc-55964e116d2f\"}";
        final NextBillingDateNotificationKey result = mapper.readValue(json, NextBillingDateNotificationKey.class);
        Assert.assertEquals(result.getUuidKey().toString(), "a38c363f-b25b-4287-8ebc-55964e116d2f");
        Assert.assertNull(result.getTargetDate());
        Assert.assertNull(result.isDryRunForInvoiceNotification());

    }
}
