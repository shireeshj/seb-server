/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.sebconfig.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import ch.ethz.seb.sebserver.gbl.model.sebconfig.AttributeType;
import ch.ethz.seb.sebserver.gbl.model.sebconfig.ConfigurationAttribute;
import ch.ethz.seb.sebserver.gbl.model.sebconfig.ConfigurationValue;
import ch.ethz.seb.sebserver.gbl.profile.WebServiceProfile;
import ch.ethz.seb.sebserver.webservice.servicelayer.sebconfig.ConfigurationValueValidator;

@Lazy
@Component
@WebServiceProfile
public class IntegerTypeValidator implements ConfigurationValueValidator {

    @Override
    public String name() {
        return AttributeType.INTEGER.name();
    }

    @Override
    public boolean validate(
            final ConfigurationValue value,
            final ConfigurationAttribute attribute) {

        // if value is not an integer type or another specific validation is defined --> skip
        if (attribute.type != AttributeType.INTEGER ||
                StringUtils.isNoneBlank(attribute.validator)) {

            return true;
        }

        if (StringUtils.isBlank(value.value)) {
            return true;
        }

        try {
            Integer.parseInt(value.value);
            return true;
        } catch (final NumberFormatException nfe) {
            return false;
        }
    }

}