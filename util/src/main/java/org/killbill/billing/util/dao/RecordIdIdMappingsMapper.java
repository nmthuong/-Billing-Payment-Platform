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

package org.killbill.billing.util.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import org.killbill.billing.callcontext.DefaultCallContext;
import org.killbill.billing.util.audit.ChangeType;
import org.killbill.billing.util.audit.dao.AuditLogModelDao;
import org.killbill.billing.util.callcontext.CallContext;

public class RecordIdIdMappingsMapper extends MapperBase implements ResultSetMapper<RecordIdIdMappings> {

    @Override
    public RecordIdIdMappings map(final int index, final ResultSet r, final StatementContext ctx) throws SQLException {
        final long recordId = r.getLong("record_id");
        final UUID id = getUUID(r, "id");
        return new RecordIdIdMappings(recordId, id);
    }
}
