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

package org.killbill.billing;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.killbill.billing.util.cache.CacheControllerDispatcher;
import org.killbill.commons.embeddeddb.EmbeddedDB;
import org.skife.jdbi.v2.IDBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

public class GuicyKillbillTestSuiteWithEmbeddedDB extends GuicyKillbillTestSuite {

    private static final Logger log = LoggerFactory.getLogger(GuicyKillbillTestSuiteWithEmbeddedDB.class);

    @Inject
    protected EmbeddedDB helper;

    @Inject
    protected DataSource dataSource;

    @Inject
    protected IDBI dbi;

    @Inject
    protected CacheControllerDispatcher controlCacheDispatcher;


    @BeforeSuite(groups = "slow")
    public void beforeSuite() throws Exception {
        DBTestingHelper.get().start();
    }

    @BeforeMethod(groups = "slow")
    public void beforeMethod() throws Exception {
        try {
            DBTestingHelper.get().getInstance().cleanupAllTables();
        } catch (final Exception ignored) {
        }
        controlCacheDispatcher.clearAll();
    }

    @AfterSuite(groups = "slow")
    public void afterSuite() throws Exception {
        if (hasFailed()) {
            log.error("**********************************************************************************************");
            log.error("*** TESTS HAVE FAILED - LEAVING DB RUNNING FOR DEBUGGING - MAKE SURE TO KILL IT ONCE DONE ****");
            log.error(DBTestingHelper.get().getInstance().getCmdLineConnectionString());
            log.error("**********************************************************************************************");
            return;
        }

        try {
            DBTestingHelper.get().getInstance().stop();
        } catch (final Exception ignored) {
        }
    }
}
