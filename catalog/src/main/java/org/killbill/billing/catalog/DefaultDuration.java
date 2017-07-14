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

package org.killbill.billing.catalog;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import org.killbill.billing.ErrorCode;
import org.killbill.billing.catalog.api.CatalogApiException;
import org.killbill.billing.catalog.api.Duration;
import org.killbill.billing.catalog.api.TimeUnit;
import org.killbill.xmlloader.ValidatingConfig;
import org.killbill.xmlloader.ValidationError;
import org.killbill.xmlloader.ValidationErrors;

@XmlAccessorType(XmlAccessType.NONE)
public class DefaultDuration extends ValidatingConfig<StandaloneCatalog> implements Duration {

    @XmlElement(required = true)
    private TimeUnit unit;

    @XmlElement(required = false)
    private Integer number;

    /* (non-Javadoc)
      * @see org.killbill.billing.catalog.IDuration#getUnit()
      */
    @Override
    public TimeUnit getUnit() {
        return unit;
    }

    /* (non-Javadoc)
	 * @see org.killbill.billing.catalog.IDuration#getLength()
	 */
    @Override
    public int getNumber() {
        return number;
    }

    public DefaultDuration() {
    }

    @Override
    public DateTime addToDateTime(final DateTime dateTime) throws CatalogApiException {
        if ((number == null) && (unit != TimeUnit.UNLIMITED)) {
            return dateTime;
        }

        switch (unit) {
            case DAYS:
                return dateTime.plusDays(number);
            case MONTHS:
                return dateTime.plusMonths(number);
            case YEARS:
                return dateTime.plusYears(number);
            case UNLIMITED:
            default:
                throw new CatalogApiException(ErrorCode.CAT_UNDEFINED_DURATION, unit);
        }
    }

    @Override
    public LocalDate addToLocalDate(final LocalDate localDate) throws CatalogApiException {
        if ((number == null) && (unit != TimeUnit.UNLIMITED)) {
            return localDate;
        }

        switch (unit) {
            case DAYS:
                return localDate.plusDays(number);
            case MONTHS:
                return localDate.plusMonths(number);
            case YEARS:
                return localDate.plusYears(number);
            case UNLIMITED:
            default:
                throw new CatalogApiException(ErrorCode.CAT_UNDEFINED_DURATION, unit);
        }
    }

    @Override
    public ValidationErrors validate(final StandaloneCatalog catalog, final ValidationErrors errors) {

        // Safety check
        if (number == null) {
            throw new IllegalStateException("number should have been automatically been initialized with DEFAULT_NON_REQUIRED_INTEGER_FIELD_VALUE ");
        }

        //Validation: TimeUnit UNLIMITED if number == -1
        if ((unit == TimeUnit.UNLIMITED && number != CatalogSafetyInitializer.DEFAULT_NON_REQUIRED_INTEGER_FIELD_VALUE)) {
            errors.add(new ValidationError("Duration can only have 'UNLIMITED' unit if the number is omitted",
                                           catalog.getCatalogURI(), DefaultDuration.class, ""));
        } else if ((unit != TimeUnit.UNLIMITED) && number == CatalogSafetyInitializer.DEFAULT_NON_REQUIRED_INTEGER_FIELD_VALUE) {
            errors.add(new ValidationError("Finite Duration must have a well defined length",
                                           catalog.getCatalogURI(), DefaultDuration.class, ""));
        }
        return errors;
    }

    @Override
    public void initialize(final StandaloneCatalog root, final URI uri) {
        super.initialize(root, uri);
        CatalogSafetyInitializer.initializeNonRequiredNullFieldsWithDefaultValue(this);
    }


    public DefaultDuration setUnit(final TimeUnit unit) {
        this.unit = unit;
        return this;
    }

    public DefaultDuration setNumber(final Integer number) {
        this.number = number;
        return this;
    }

    @Override
    public Period toJodaPeriod() {
        if ((number == null) && (unit != TimeUnit.UNLIMITED)) {
            return new Period();
        }

        switch (unit) {
            case DAYS:
                return new Period().withDays(number);
            case MONTHS:
                return new Period().withMonths(number);
            case YEARS:
                return new Period().withYears(number);
            case UNLIMITED:
                return new Period().withYears(100);
            default:
                return new Period();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultDuration)) {
            return false;
        }

        final DefaultDuration that = (DefaultDuration) o;

        if (number != null ? !number.equals(that.number) : that.number != null) {
            return false;
        }
        if (unit != that.unit) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = unit != null ? unit.hashCode() : 0;
        result = 31 * result + (number != null ? number.hashCode() : 0);
        return result;
    }
}
