/*
 * Copyright 2010-2012 Ning, Inc.
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

package org.killbill.billing.util.audit.dao;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.killbill.billing.ObjectType;
import org.killbill.billing.callcontext.InternalTenantContext;
import org.killbill.billing.util.api.AuditLevel;
import org.killbill.billing.util.audit.AuditLog;
import org.killbill.billing.util.audit.ChangeType;
import org.killbill.billing.util.audit.DefaultAccountAuditLogs;
import org.killbill.billing.util.audit.DefaultAccountAuditLogsForObjectType;
import org.killbill.billing.util.audit.DefaultAuditLog;
import org.killbill.billing.util.cache.CacheControllerDispatcher;
import org.killbill.billing.util.callcontext.InternalCallContextFactory;
import org.killbill.billing.util.dao.NonEntityDao;
import org.killbill.billing.util.dao.NonEntitySqlDao;
import org.killbill.billing.util.dao.RecordIdIdMappings;
import org.killbill.billing.util.dao.TableName;
import org.killbill.billing.util.entity.dao.EntitySqlDao;
import org.killbill.billing.util.entity.dao.EntitySqlDaoTransactionWrapper;
import org.killbill.billing.util.entity.dao.EntitySqlDaoTransactionalJdbiWrapper;
import org.killbill.billing.util.entity.dao.EntitySqlDaoWrapperFactory;
import org.killbill.clock.Clock;
import org.skife.jdbi.v2.IDBI;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public class DefaultAuditDao implements AuditDao {

    private final NonEntitySqlDao nonEntitySqlDao;
    private final EntitySqlDaoTransactionalJdbiWrapper transactionalSqlDao;

    @Inject
    public DefaultAuditDao(final IDBI dbi, final Clock clock, final CacheControllerDispatcher cacheControllerDispatcher, final NonEntityDao nonEntityDao, final InternalCallContextFactory internalCallContextFactory) {
        this.nonEntitySqlDao = dbi.onDemand(NonEntitySqlDao.class);
        this.transactionalSqlDao = new EntitySqlDaoTransactionalJdbiWrapper(dbi, clock, cacheControllerDispatcher, nonEntityDao, internalCallContextFactory);
    }

    @Override
    public DefaultAccountAuditLogs getAuditLogsForAccountRecordId(final AuditLevel auditLevel, final InternalTenantContext context) {
        final UUID accountId = nonEntitySqlDao.getIdFromObject(context.getAccountRecordId(), TableName.ACCOUNT.getTableName());

        // Lazy evaluate records to minimize the memory footprint (these can yield a lot of results)
        // We usually always want to wrap our queries in an EntitySqlDaoTransactionWrapper... except here.
        // Since we want to stream the results out, we don't want to auto-commit when this method returns.
        final EntitySqlDao auditSqlDao = transactionalSqlDao.onDemandForStreamingResults(EntitySqlDao.class);
        final Iterator<AuditLogModelDao> auditLogsForAccountRecordId = auditSqlDao.getAuditLogsForAccountRecordId(context);
        final Iterator<AuditLog> allAuditLogs = buildAuditLogsFromModelDao(auditLogsForAccountRecordId, context);

        return new DefaultAccountAuditLogs(accountId, auditLevel, allAuditLogs);
    }

    @Override
    public DefaultAccountAuditLogsForObjectType getAuditLogsForAccountRecordId(final TableName tableName, final AuditLevel auditLevel, final InternalTenantContext context) {
        final String actualTableName;
        if (tableName.hasHistoryTable()) {
            actualTableName = tableName.getHistoryTableName().name(); // upper cased
        } else {
            actualTableName = tableName.getTableName();
        }

        // Lazy evaluate records to minimize the memory footprint (these can yield a lot of results)
        // We usually always want to wrap our queries in an EntitySqlDaoTransactionWrapper... except here.
        // Since we want to stream the results out, we don't want to auto-commit when this method returns.
        final EntitySqlDao auditSqlDao = transactionalSqlDao.onDemandForStreamingResults(EntitySqlDao.class);
        final Iterator<AuditLogModelDao> auditLogsForTableNameAndAccountRecordId = auditSqlDao.getAuditLogsForTableNameAndAccountRecordId(actualTableName, context);
        final Iterator<AuditLog> allAuditLogs = buildAuditLogsFromModelDao(auditLogsForTableNameAndAccountRecordId, context);

        return new DefaultAccountAuditLogsForObjectType(auditLevel, allAuditLogs);
    }

    private Iterator<AuditLog> buildAuditLogsFromModelDao(final Iterator<AuditLogModelDao> auditLogsForAccountRecordId, final InternalTenantContext tenantContext) {
        final Map<TableName, Map<Long, UUID>> recordIdIdsCache = new HashMap<TableName, Map<Long, UUID>>();
        final Map<TableName, Map<Long, UUID>> historyRecordIdIdsCache = new HashMap<TableName, Map<Long, UUID>>();
        return Iterators.<AuditLogModelDao, AuditLog>transform(auditLogsForAccountRecordId,
                                                               new Function<AuditLogModelDao, AuditLog>() {
                                                                   @Override
                                                                   public AuditLog apply(final AuditLogModelDao input) {
                                                                       // If input is for e.g. TAG_DEFINITION_HISTORY, retrieve TAG_DEFINITIONS
                                                                       // For tables without history, e.g. TENANT, originalTableNameForHistoryTableName will be null
                                                                       final TableName originalTableNameForHistoryTableName = findTableNameForHistoryTableName(input.getTableName());

                                                                       final ObjectType objectType;
                                                                       final UUID auditedEntityId;
                                                                       if (originalTableNameForHistoryTableName != null) {
                                                                           // input point to a history entry
                                                                           objectType = originalTableNameForHistoryTableName.getObjectType();

                                                                           if (historyRecordIdIdsCache.get(originalTableNameForHistoryTableName) == null) {
                                                                               if (TableName.ACCOUNT.equals(originalTableNameForHistoryTableName)) {
                                                                                   final Iterable<RecordIdIdMappings> mappings = nonEntitySqlDao.getHistoryRecordIdIdMappingsForAccountsTable(originalTableNameForHistoryTableName.getTableName(),
                                                                                                                                                                                              input.getTableName().getTableName(),
                                                                                                                                                                                              tenantContext);
                                                                                   historyRecordIdIdsCache.put(originalTableNameForHistoryTableName, RecordIdIdMappings.toMap(mappings));
                                                                               } else if (TableName.TAG_DEFINITIONS.equals(originalTableNameForHistoryTableName)) {
                                                                                   final Iterable<RecordIdIdMappings> mappings = nonEntitySqlDao.getHistoryRecordIdIdMappingsForTablesWithoutAccountRecordId(originalTableNameForHistoryTableName.getTableName(),
                                                                                                                                                                                                             input.getTableName().getTableName(),
                                                                                                                                                                                                             tenantContext);
                                                                                   historyRecordIdIdsCache.put(originalTableNameForHistoryTableName, RecordIdIdMappings.toMap(mappings));
                                                                               } else {
                                                                                   final Iterable<RecordIdIdMappings> mappings = nonEntitySqlDao.getHistoryRecordIdIdMappings(originalTableNameForHistoryTableName.getTableName(),
                                                                                                                                                                              input.getTableName().getTableName(),
                                                                                                                                                                              tenantContext);
                                                                                   historyRecordIdIdsCache.put(originalTableNameForHistoryTableName, RecordIdIdMappings.toMap(mappings));

                                                                               }
                                                                           }

                                                                           auditedEntityId = historyRecordIdIdsCache.get(originalTableNameForHistoryTableName).get(input.getTargetRecordId());
                                                                       } else {
                                                                           objectType = input.getTableName().getObjectType();

                                                                           if (recordIdIdsCache.get(input.getTableName()) == null) {
                                                                               final Iterable<RecordIdIdMappings> mappings = nonEntitySqlDao.getRecordIdIdMappings(input.getTableName().getTableName(),
                                                                                                                                                                   tenantContext);
                                                                               recordIdIdsCache.put(input.getTableName(), RecordIdIdMappings.toMap(mappings));
                                                                           }

                                                                           auditedEntityId = recordIdIdsCache.get(input.getTableName()).get(input.getTargetRecordId());
                                                                       }

                                                                       return new DefaultAuditLog(input, objectType, auditedEntityId);
                                                                   }

                                                                   private TableName findTableNameForHistoryTableName(final TableName historyTableName) {
                                                                       for (final TableName tableName : TableName.values()) {
                                                                           if (historyTableName.equals(tableName.getHistoryTableName())) {
                                                                               return tableName;
                                                                           }
                                                                       }

                                                                       return null;
                                                                   }
                                                               });
    }

    @Override
    public List<AuditLog> getAuditLogsForId(final TableName tableName, final UUID objectId, final AuditLevel auditLevel, final InternalTenantContext context) {
        if (tableName.hasHistoryTable()) {
            return doGetAuditLogsViaHistoryForId(tableName, objectId, auditLevel, context);
        } else {
            return doGetAuditLogsForId(tableName, objectId, auditLevel, context);
        }
    }

    private List<AuditLog> doGetAuditLogsForId(final TableName tableName, final UUID objectId, final AuditLevel auditLevel, final InternalTenantContext context) {
        final Long recordId = nonEntitySqlDao.getRecordIdFromObject(objectId.toString(), tableName.getTableName());
        if (recordId == null) {
            return ImmutableList.<AuditLog>of();
        } else {
            return getAuditLogsForRecordId(tableName, objectId, recordId, auditLevel, context);
        }
    }

    private List<AuditLog> doGetAuditLogsViaHistoryForId(final TableName tableName, final UUID objectId, final AuditLevel auditLevel, final InternalTenantContext context) {
        final TableName historyTableName = tableName.getHistoryTableName();
        if (historyTableName == null) {
            throw new IllegalStateException("History table shouldn't be null for " + tableName);
        }

        final Long targetRecordId = nonEntitySqlDao.getRecordIdFromObject(objectId.toString(), tableName.getTableName());
        final List<AuditLog> allAuditLogs = transactionalSqlDao.execute(new EntitySqlDaoTransactionWrapper<List<AuditLog>>() {
            @Override
            public List<AuditLog> inTransaction(final EntitySqlDaoWrapperFactory entitySqlDaoWrapperFactory) throws Exception {
                final List<AuditLogModelDao> auditLogsViaHistoryForTargetRecordId = entitySqlDaoWrapperFactory.become(EntitySqlDao.class).getAuditLogsViaHistoryForTargetRecordId(historyTableName.name(),
                                                                                                                                                                                  historyTableName.getTableName().toLowerCase(),
                                                                                                                                                                                  targetRecordId,
                                                                                                                                                                                  context);
                return buildAuditLogsFromModelDao(auditLogsViaHistoryForTargetRecordId, tableName.getObjectType(), objectId);
            }
        });
        return filterAuditLogs(auditLevel, allAuditLogs);
    }

    private List<AuditLog> getAuditLogsForRecordId(final TableName tableName, final UUID auditedEntityId, final Long targetRecordId, final AuditLevel auditLevel, final InternalTenantContext context) {
        final List<AuditLog> allAuditLogs = transactionalSqlDao.execute(new EntitySqlDaoTransactionWrapper<List<AuditLog>>() {
            @Override
            public List<AuditLog> inTransaction(final EntitySqlDaoWrapperFactory entitySqlDaoWrapperFactory) throws Exception {
                final List<AuditLogModelDao> auditLogsForTargetRecordId = entitySqlDaoWrapperFactory.become(EntitySqlDao.class).getAuditLogsForTargetRecordId(tableName.name(),
                                                                                                                                                              targetRecordId,
                                                                                                                                                              context);
                return buildAuditLogsFromModelDao(auditLogsForTargetRecordId, tableName.getObjectType(), auditedEntityId);
            }
        });
        return filterAuditLogs(auditLevel, allAuditLogs);
    }

    private List<AuditLog> buildAuditLogsFromModelDao(final List<AuditLogModelDao> auditLogsForAccountRecordId, final ObjectType objectType, final UUID auditedEntityId) {
        return Lists.<AuditLogModelDao, AuditLog>transform(auditLogsForAccountRecordId,
                                                           new Function<AuditLogModelDao, AuditLog>() {
                                                               @Override
                                                               public AuditLog apply(final AuditLogModelDao input) {
                                                                   return new DefaultAuditLog(input, objectType, auditedEntityId);
                                                               }
                                                           });
    }

    private List<AuditLog> filterAuditLogs(final AuditLevel auditLevel, final List<AuditLog> auditLogs) {
        // TODO Do the filtering in the query
        if (AuditLevel.FULL.equals(auditLevel)) {
            return auditLogs;
        } else if (AuditLevel.MINIMAL.equals(auditLevel) && !auditLogs.isEmpty()) {
            if (ChangeType.INSERT.equals(auditLogs.get(0).getChangeType())) {
                return ImmutableList.<AuditLog>of(auditLogs.get(0));
            } else {
                // We may be coming here via the history code path - only a single mapped history record id
                // will be for the initial INSERT
                return ImmutableList.<AuditLog>of();
            }
        } else if (AuditLevel.NONE.equals(auditLevel)) {
            return ImmutableList.<AuditLog>of();
        } else {
            return auditLogs;
        }
    }
}
