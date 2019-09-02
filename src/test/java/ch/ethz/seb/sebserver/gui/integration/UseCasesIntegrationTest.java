/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gui.integration;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.jdbc.Sql;

import ch.ethz.seb.sebserver.gbl.api.API;
import ch.ethz.seb.sebserver.gbl.model.Domain;
import ch.ethz.seb.sebserver.gbl.model.EntityProcessingReport;
import ch.ethz.seb.sebserver.gbl.model.institution.Institution;
import ch.ethz.seb.sebserver.gbl.model.user.PasswordChange;
import ch.ethz.seb.sebserver.gbl.model.user.UserInfo;
import ch.ethz.seb.sebserver.gbl.model.user.UserRole;
import ch.ethz.seb.sebserver.gbl.util.Result;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.RestServiceImpl;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.institution.ActivateInstitution;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.institution.GetInstitution;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.institution.GetInstitutionNames;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.institution.NewInstitution;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.useraccount.NewUserAccount;

public class UseCasesIntegrationTest extends GuiIntegrationTest {

    @BeforeAll
    @Sql(scripts = { "classpath:schema-test.sql", "classpath:data-test.sql" })
    public void init() {

    }

    @AfterAll
    @Sql(scripts = { "classpath:schema-test.sql", "classpath:data-test.sql" })
    public void cleanup() {

    }

    @Test
    @Order(1)
    // *************************************
    // Use Case 1: SEB Administrator creates a new institution and activate this new institution

    public void testUsecase1() {
        final RestServiceImpl restService = createRestServiceForUser(
                "admin",
                "admin",
                new NewInstitution(),
                new ActivateInstitution(),
                new GetInstitution());

        final Result<Institution> result = restService.getBuilder(NewInstitution.class)
                .withFormParam(Domain.INSTITUTION.ATTR_NAME, "Test Institution")
                .call();

        assertNotNull(result);
        assertFalse(result.hasError());
        Institution institution = result.get();
        assertEquals("Test Institution", institution.name);
        assertFalse(institution.active);

        final Result<EntityProcessingReport> resultActivation = restService.getBuilder(ActivateInstitution.class)
                .withURIVariable(API.PARAM_MODEL_ID, String.valueOf(institution.id))
                .call();

        assertNotNull(resultActivation);
        assertFalse(resultActivation.hasError());

        final Result<Institution> resultGet = restService.getBuilder(GetInstitution.class)
                .withURIVariable(API.PARAM_MODEL_ID, String.valueOf(institution.id))
                .call();

        assertNotNull(resultGet);
        assertFalse(resultGet.hasError());
        institution = resultGet.get();
        assertEquals("Test Institution", institution.name);
        assertTrue(institution.active);

    }

    @Test
    @Order(2)
    // *************************************
    // Use Case 2: SEB Administrator creates a new Institutional Administrator user for the
    // newly created institution and activate this user

    public void testUsecase2() {
        final RestServiceImpl restService = createRestServiceForUser(
                "admin",
                "admin",
                new GetInstitution(),
                new GetInstitutionNames(),
                new NewUserAccount());

        final String instId = restService.getBuilder(GetInstitutionNames.class)
                .call()
                .getOrThrow()
                .stream()
                .filter(inst -> "Test Institution".equals(inst.name))
                .findFirst()
                .get().modelId;

        assertNotNull(instId);

        final UserInfo userInfo = restService.getBuilder(NewUserAccount.class)
                .withFormParam(Domain.USER.ATTR_INSTITUTION_ID, instId)
                .withFormParam(Domain.USER.ATTR_NAME, "TestInstAdmin")
                .withFormParam(Domain.USER.ATTR_USERNAME, "TestInstAdmin")
                .withFormParam(Domain.USER.ATTR_EMAIL, "test@test.ch")
                .withFormParam(Domain.USER_ROLE.REFERENCE_NAME, UserRole.INSTITUTIONAL_ADMIN.name())
                .withFormParam(PasswordChange.ATTR_NAME_NEW_PASSWORD, "12345678")
                .withFormParam(PasswordChange.ATTR_NAME_CONFIRM_NEW_PASSWORD, "12345678")
                .call()
                .getOrThrow();

        assertNotNull(userInfo);

    }

}
