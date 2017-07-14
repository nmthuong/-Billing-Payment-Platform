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

package org.killbill.billing.jaxrs.mappers;

import javax.inject.Singleton;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.killbill.billing.ErrorCode;
import org.killbill.billing.subscription.api.timeline.SubscriptionBaseRepairException;

@Singleton
@Provider
public class SubscriptionRepairExceptionMapper extends ExceptionMapperBase implements ExceptionMapper<SubscriptionBaseRepairException> {

    private final UriInfo uriInfo;

    public SubscriptionRepairExceptionMapper(@Context final UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Override
    public Response toResponse(final SubscriptionBaseRepairException exception) {
        if (exception.getCode() == ErrorCode.SUB_NO_ACTIVE_SUBSCRIPTIONS.getCode()) {
            return buildBadRequestResponse(exception, uriInfo);
        } else {
            return fallback(exception, uriInfo);
        }
    }
}
