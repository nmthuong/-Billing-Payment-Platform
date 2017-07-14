/*
 * Copyright 2010-2012 Ning, Inc.
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

package org.killbill.billing.util.dao;

import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.killbill.billing.util.UtilTestSuiteWithEmbeddedDB;
import org.killbill.billing.util.entity.Entity;
import org.killbill.billing.util.entity.dao.EntityModelDao;
import org.killbill.billing.util.entity.dao.EntitySqlDao;
import org.killbill.billing.util.entity.dao.EntitySqlDaoStringTemplate;

public class TestStringTemplateInheritanceWithJdbi extends UtilTestSuiteWithEmbeddedDB {

    private static interface Kombucha extends Entity {}

    private static interface KombuchaModelDao extends EntityModelDao<Kombucha> {}

    @EntitySqlDaoStringTemplate("/org/killbill/billing/util/dao/Kombucha.sql.stg")
    private static interface KombuchaSqlDao extends EntitySqlDao<KombuchaModelDao, Kombucha> {

        @SqlQuery
        public boolean isIsTimeForKombucha();
    }

    @Test(groups = "slow")
    public void testInheritQueries() throws Exception {
        final KombuchaSqlDao dao = dbi.onDemand(KombuchaSqlDao.class);

        // Verify non inherited template
        Assert.assertEquals(dao.isIsTimeForKombucha(), clock.getUTCNow().getHourOfDay() == 17);

        // Verify inherited templates
        Assert.assertFalse(dao.getAll(internalCallContext).hasNext());
    }
}
