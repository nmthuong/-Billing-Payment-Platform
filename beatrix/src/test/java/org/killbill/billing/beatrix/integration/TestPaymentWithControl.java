/*
 * Copyright 2014-2016 Groupon, Inc
 * Copyright 2014-2016 The Billing Project, LLC
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

package org.killbill.billing.beatrix.integration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.killbill.billing.account.api.Account;
import org.killbill.billing.account.api.AccountData;
import org.killbill.billing.api.TestApiListener.NextEvent;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.osgi.api.OSGIServiceDescriptor;
import org.killbill.billing.osgi.api.OSGIServiceRegistration;
import org.killbill.billing.payment.api.Payment;
import org.killbill.billing.payment.api.PaymentOptions;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.control.plugin.api.OnFailurePaymentControlResult;
import org.killbill.billing.control.plugin.api.OnSuccessPaymentControlResult;
import org.killbill.billing.control.plugin.api.PaymentControlApiException;
import org.killbill.billing.control.plugin.api.PaymentControlContext;
import org.killbill.billing.control.plugin.api.PaymentControlPluginApi;
import org.killbill.billing.control.plugin.api.PriorPaymentControlResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class TestPaymentWithControl extends TestIntegrationBase {

    private static final String TEST_PAYMENT_WITH_CONTROL = "TestPaymentWithControl";

    private TestPaymentControlPluginApi testPaymentControlWithControl;
    private List<PluginProperty> properties;
    private PaymentOptions paymentOptions;

    @Inject
    private OSGIServiceRegistration<PaymentControlPluginApi> pluginRegistry;

    @BeforeClass(groups = "slow")
    public void beforeClass() throws Exception {
        super.beforeClass();

        this.testPaymentControlWithControl = new TestPaymentControlPluginApi();
        pluginRegistry.registerService(new OSGIServiceDescriptor() {
            @Override
            public String getPluginSymbolicName() {
                return TEST_PAYMENT_WITH_CONTROL;
            }

            @Override
            public String getPluginName() {
                return TEST_PAYMENT_WITH_CONTROL;
            }

            @Override
            public String getRegistrationName() {
                return TEST_PAYMENT_WITH_CONTROL;
            }
        }, testPaymentControlWithControl);

        properties = new ArrayList<PluginProperty>();
        paymentOptions = new PaymentOptions() {
            @Override
            public boolean isExternalPayment() {
                return false;
            }

            @Override
            public List<String> getPaymentControlPluginNames() {
                return ImmutableList.of(TEST_PAYMENT_WITH_CONTROL);
            }
        };

        properties.add(new PluginProperty("name", TEST_PAYMENT_WITH_CONTROL, false));

    }

    @BeforeMethod(groups = "slow")
    public void beforeMethod() throws Exception {
        super.beforeMethod();
        testPaymentControlWithControl.reset();
    }

    @Test(groups = "slow")
    public void testAuthCaptureWithPaymentControl() throws Exception {

        final AccountData accountData = getAccountData(1);
        final Account account = createAccountWithNonOsgiPaymentMethod(accountData);

        busHandler.pushExpectedEvents(NextEvent.PAYMENT);
        final Payment payment = paymentApi.createAuthorizationWithPaymentControl(account, account.getPaymentMethodId(), null, BigDecimal.ONE, account.getCurrency(), null, null,
                                                                                 properties, paymentOptions, callContext);
        assertListenerStatus();

        busHandler.pushExpectedEvents(NextEvent.PAYMENT);
        paymentApi.createCaptureWithPaymentControl(account, payment.getId(), BigDecimal.ONE, account.getCurrency(), null, properties, paymentOptions, callContext);
        assertListenerStatus();

        Assert.assertEquals(testPaymentControlWithControl.getCalls().size(), 2);
        Assert.assertEquals(testPaymentControlWithControl.getCalls().get(TransactionType.AUTHORIZE.toString()), new Integer(1));
        Assert.assertEquals(testPaymentControlWithControl.getCalls().get(TransactionType.CAPTURE.toString()), new Integer(1));
    }

    @Test(groups = "slow")
    public void testAuthVoidWithPaymentControl() throws Exception {
        final AccountData accountData = getAccountData(1);
        final Account account = createAccountWithNonOsgiPaymentMethod(accountData);

        busHandler.pushExpectedEvents(NextEvent.PAYMENT);
        final Payment payment = paymentApi.createAuthorizationWithPaymentControl(account, account.getPaymentMethodId(), null, BigDecimal.ONE, account.getCurrency(), null, null,
                                                                                 properties, paymentOptions, callContext);
        assertListenerStatus();

        busHandler.pushExpectedEvents(NextEvent.PAYMENT);
        paymentApi.createVoidWithPaymentControl(account, payment.getId(), null, properties, paymentOptions, callContext);
        assertListenerStatus();
        Assert.assertEquals(testPaymentControlWithControl.getCalls().size(), 2);
        Assert.assertEquals(testPaymentControlWithControl.getCalls().get(TransactionType.AUTHORIZE.toString()), new Integer(1));
        Assert.assertEquals(testPaymentControlWithControl.getCalls().get(TransactionType.VOID.toString()), new Integer(1));
    }

    public class TestPaymentControlPluginApi implements PaymentControlPluginApi {

        private final Map<String, Integer> calls;

        public TestPaymentControlPluginApi() {
            calls = new HashMap<String, Integer>();
        }

        public Map<String, Integer> getCalls() {
            return calls;
        }

        public void reset() {
            calls.clear();
        }

        @Override
        public PriorPaymentControlResult priorCall(final PaymentControlContext paymentControlContext, final Iterable<PluginProperty> properties) throws PaymentControlApiException {
            return new PriorPaymentControlResult() {
                @Override
                public boolean isAborted() {
                    return false;
                }
                @Override
                public BigDecimal getAdjustedAmount() {
                    return null;
                }
                @Override
                public Currency getAdjustedCurrency() {
                    return null;
                }
                @Override
                public UUID getAdjustedPaymentMethodId() {
                    return null;
                }
                @Override
                public Iterable<PluginProperty> getAdjustedPluginProperties() {
                    return null;
                }
            };
        }

        @Override
        public OnSuccessPaymentControlResult onSuccessCall(final PaymentControlContext paymentControlContext, final Iterable<PluginProperty> properties) throws PaymentControlApiException {
            final PluginProperty nameProperty = Iterables.tryFind(properties, new Predicate<PluginProperty>() {
                @Override
                public boolean apply(final PluginProperty input) {
                    return input.getKey().equals("name");
                }
            }).orNull();
            if (nameProperty != null && nameProperty.getValue().equals(TEST_PAYMENT_WITH_CONTROL)) {
                final Integer result = calls.get(paymentControlContext.getTransactionType());
                calls.put(paymentControlContext.getTransactionType().toString(), result == null ? new Integer(1) : new Integer(result.intValue() + 1));
            }
            return new OnSuccessPaymentControlResult() {
                @Override
                public Iterable<PluginProperty> getAdjustedPluginProperties() {
                    return null;
                }
            };
        }

        @Override
        public OnFailurePaymentControlResult onFailureCall(final PaymentControlContext paymentControlContext, final Iterable<PluginProperty> properties) throws PaymentControlApiException {
            return null;
        }
    }
}
