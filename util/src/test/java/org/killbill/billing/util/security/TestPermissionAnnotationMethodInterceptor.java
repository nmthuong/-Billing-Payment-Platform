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

package org.killbill.billing.util.security;

import javax.inject.Singleton;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.killbill.billing.util.glue.TestSecurityModuleNoDB;
import org.killbill.billing.util.glue.TestUtilModuleNoDB.ShiroModuleNoDB;
import org.mockito.Mockito;
import org.skife.jdbi.v2.IDBI;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.killbill.billing.security.Permission;
import org.killbill.billing.security.RequiresPermissions;
import org.killbill.billing.util.UtilTestSuiteNoDB;
import org.killbill.billing.util.glue.KillBillShiroAopModule;
import org.killbill.billing.util.glue.KillBillShiroModule;
import org.killbill.billing.util.glue.SecurityModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import net.sf.ehcache.CacheManager;

public class TestPermissionAnnotationMethodInterceptor extends UtilTestSuiteNoDB {

    public static interface IAopTester {

        @RequiresPermissions(Permission.PAYMENT_CAN_REFUND)
        public void createRefund();
    }

    public static class AopTesterImpl implements IAopTester {

        @Override
        public void createRefund() {}
    }

    @Singleton
    public static class AopTester implements IAopTester {

        @RequiresPermissions(Permission.PAYMENT_CAN_REFUND)
        public void createRefund() {}
    }

    @Test(groups = "fast")
    public void testAOPForClass() throws Exception {
        // Make sure it works as expected without any AOP magic
        final IAopTester simpleTester = new AopTester();
        try {
            simpleTester.createRefund();
        } catch (Exception e) {
            Assert.fail(e.getLocalizedMessage());
        }

        // Now, verify the interception works
        configureShiro();
        // Shutdown the cache manager to avoid duplicate exceptions
        CacheManager.getInstance().shutdown();
        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       new ShiroModuleNoDB(configSource),
                                                       new KillBillShiroAopModule(),
                                                       new TestSecurityModuleNoDB(configSource),
                                                       new AbstractModule() {
                                                           @Override
                                                           protected void configure() {
                                                               bind(IDBI.class).toInstance(Mockito.mock(IDBI.class));
                                                           }
                                                       });
        final AopTester aopedTester = injector.getInstance(AopTester.class);
        verifyAopedTester(aopedTester);
    }

    @Test(groups = "fast")
    public void testAOPForInterface() throws Exception {
        // Make sure it works as expected without any AOP magic
        final IAopTester simpleTester = new AopTesterImpl();
        try {
            simpleTester.createRefund();
        } catch (Exception e) {
            Assert.fail(e.getLocalizedMessage());
        }

        // Now, verify the interception works
        configureShiro();
        // Shutdown the cache manager to avoid duplicate exceptions
        CacheManager.getInstance().shutdown();
        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       new ShiroModuleNoDB(configSource),
                                                       new KillBillShiroAopModule(),
                                                       new TestSecurityModuleNoDB(configSource),
                                                       new AbstractModule() {
                                                           @Override
                                                           public void configure() {
                                                               bind(IDBI.class).toInstance(Mockito.mock(IDBI.class));
                                                               bind(IAopTester.class).to(AopTesterImpl.class).asEagerSingleton();
                                                           }
                                                       });
        final IAopTester aopedTester = injector.getInstance(IAopTester.class);
        verifyAopedTester(aopedTester);
    }

    private void verifyAopedTester(final IAopTester aopedTester) {
        // Anonymous user
        logout();
        try {
            aopedTester.createRefund();
            Assert.fail();
        } catch (UnauthenticatedException e) {
            // Good!
        } catch (Exception e) {
            Assert.fail(e.getLocalizedMessage());
        }

        // pierre can credit, but not refund
        login("pierre");
        try {
            aopedTester.createRefund();
            Assert.fail();
        } catch (AuthorizationException e) {
            // Good!
        } catch (Exception e) {
            Assert.fail(e.getLocalizedMessage());
        }

        // stephane can refund
        login("stephane");
        aopedTester.createRefund();
    }
}
