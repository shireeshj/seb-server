/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.sebconfig.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collection;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import ch.ethz.seb.sebserver.WebSecurityConfig;
import ch.ethz.seb.sebserver.gbl.api.API;
import ch.ethz.seb.sebserver.gbl.model.institution.Institution;
import ch.ethz.seb.sebserver.gbl.model.sebconfig.SebClientConfig;
import ch.ethz.seb.sebserver.gbl.profile.WebServiceProfile;
import ch.ethz.seb.sebserver.gbl.util.Result;
import ch.ethz.seb.sebserver.webservice.servicelayer.client.ClientCredentialService;
import ch.ethz.seb.sebserver.webservice.servicelayer.client.ClientCredentials;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.InstitutionDAO;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.SebClientConfigDAO;
import ch.ethz.seb.sebserver.webservice.servicelayer.sebconfig.SebClientConfigService;
import ch.ethz.seb.sebserver.webservice.servicelayer.sebconfig.SebConfigEncryptionService;
import ch.ethz.seb.sebserver.webservice.servicelayer.sebconfig.SebConfigEncryptionService.Strategy;
import ch.ethz.seb.sebserver.webservice.servicelayer.sebconfig.ZipService;
import ch.ethz.seb.sebserver.webservice.servicelayer.sebconfig.impl.SebConfigEncryptionServiceImpl.EncryptionContext;

@Lazy
@Service
@WebServiceProfile
public class SebClientConfigServiceImpl implements SebClientConfigService {

    private static final Logger log = LoggerFactory.getLogger(SebClientConfigServiceImpl.class);

    private final InstitutionDAO institutionDAO;
    private final SebClientConfigDAO sebClientConfigDAO;
    private final ClientCredentialService clientCredentialService;
    private final SebConfigEncryptionService sebConfigEncryptionService;
    @Autowired
    @Qualifier(WebSecurityConfig.CLIENT_PASSWORD_ENCODER_BEAN_NAME)
    private PasswordEncoder clientPasswordEncoder;
    private final ZipService zipService;
    private final String httpScheme;
    private final String serverAddress;
    private final String serverPort;

    protected SebClientConfigServiceImpl(
            final InstitutionDAO institutionDAO,
            final SebClientConfigDAO sebClientConfigDAO,
            final ClientCredentialService clientCredentialService,
            final SebConfigEncryptionService sebConfigEncryptionService,
            final ZipService zipService,
            @Value("${sebserver.webservice.http.scheme}") final String httpScheme,
            @Value("${server.address}") final String serverAddress,
            @Value("${server.port}") final String serverPort) {

        this.institutionDAO = institutionDAO;
        this.sebClientConfigDAO = sebClientConfigDAO;
        this.clientCredentialService = clientCredentialService;
        this.sebConfigEncryptionService = sebConfigEncryptionService;
        this.zipService = zipService;
        this.httpScheme = httpScheme;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Override
    public boolean hasSebClientConfigurationForInstitution(final Long institutionId) {
        final Result<Collection<SebClientConfig>> all = this.sebClientConfigDAO.all(institutionId, true);
        return all != null && !all.hasError() && !all.getOrThrow().isEmpty();
    }

    @Override
    public Result<SebClientConfig> autoCreateSebClientConfigurationForInstitution(final Long institutionId) {
        return Result.tryCatch(() -> {
            final Institution institution = this.institutionDAO
                    .byPK(institutionId)
                    .getOrThrow();

            return new SebClientConfig(
                    null,
                    institutionId,
                    institution.name + "_" + UUID.randomUUID(),
                    null,
                    null,
                    null,
                    true);
        })
                .flatMap(this.sebClientConfigDAO::createNew);
    }

    @Override
    public Result<String> getEncodedClientSecret(final String clientId) {
        return Result.tryCatch(() -> {
            final Collection<SebClientConfig> clientConfigs = this.sebClientConfigDAO.all(extractInstitution(), true)
                    .getOrThrow();

            final ClientCredentials clientCredentials = findClientCredentialsFor(clientId, clientConfigs);
            return this.clientPasswordEncoder.encode(
                    this.clientCredentialService.getPlainClientSecret(clientCredentials));

        });
    }

    public ClientCredentials findClientCredentialsFor(final String clientId,
            final Collection<SebClientConfig> clientConfigs) {
        for (final SebClientConfig config : clientConfigs) {
            try {
                final ClientCredentials clientCredentials =
                        this.sebClientConfigDAO.getSebClientCredentials(config.getModelId())
                                .getOrThrow();
                if (clientId.equals(this.clientCredentialService.getPlainClientId(clientCredentials))) {
                    return clientCredentials;
                }
            } catch (final Exception e) {
                log.error("Unexpected error while trying to fetch client credentials: ", e);
            }
        }

        return null;
    }

    private Long extractInstitution() {
        try {
            final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            final HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            return Long.parseLong(request.getParameter(API.PARAM_INSTITUTION_ID));
        } catch (final Exception e) {
            log.error(
                    "Failed to extract institution from current request. Search client Id over all active client configurations");
            return null;
        }
    }

    @Override
    public void exportSebClientConfiguration(
            final OutputStream output,
            final String modelId) {

        final SebClientConfig config = this.sebClientConfigDAO
                .byModelId(modelId).getOrThrow();

        final ClientCredentials sebClientCredentials = this.sebClientConfigDAO
                .getSebClientCredentials(config.getModelId())
                .getOrThrow();

        final CharSequence encryptionPassword = this.sebClientConfigDAO
                .getConfigPasswortCipher(config.getModelId())
                .getOrThrow();

        final CharSequence plainClientId = this.clientCredentialService
                .getPlainClientId(sebClientCredentials);
        final CharSequence plainClientSecret = this.clientCredentialService
                .getPlainClientSecret(sebClientCredentials);

        final String plainTextConfig = String.format(
                SEB_CLIENT_CONFIG_EXAMPLE_XML,
                getServerURL(),
                String.valueOf(config.institutionId),
                plainClientId,
                plainClientSecret,
                "TODO:/exam-api/discovery");

        PipedOutputStream pOut = null;
        PipedInputStream pIn = null;
        try {

            // zip the plain text
            final InputStream plainIn = IOUtils.toInputStream(plainTextConfig, "UTF-8");
            pOut = new PipedOutputStream();
            pIn = new PipedInputStream(pOut);

            this.zipService.write(pOut, plainIn);

            if (encryptionPassword != null) {
                passwordEncryption(output, encryptionPassword, pIn);
            } else {
                this.sebConfigEncryptionService.streamEncrypted(
                        output,
                        pIn,
                        EncryptionContext.contextOfPlainText());
            }

        } catch (final Exception e) {
            log.error("Error while zip and encrypt seb client config stream: ", e);
            try {
                if (pIn != null)
                    pIn.close();
            } catch (final IOException e1) {
                log.error("Failed to close PipedInputStream: ", e1);
            }
            try {
                if (pOut != null)
                    pOut.close();
            } catch (final IOException e1) {
                log.error("Failed to close PipedOutputStream: ", e1);
            }
        }
    }

    @Override
    public String getServerURL() {
        return UriComponentsBuilder.newInstance()
                .scheme(this.httpScheme)
                .host(this.serverAddress)
                .port(this.serverPort)
                .toUriString();
    }

    private void passwordEncryption(
            final OutputStream output,
            final CharSequence encryptionPassword,
            final InputStream input) {

        if (log.isDebugEnabled()) {
            log.debug("*** Seb client configuration with password based encryption");
        }

        final CharSequence encryptionPasswordPlaintext = this.clientCredentialService
                .decrypt(encryptionPassword);

        this.sebConfigEncryptionService.streamEncrypted(
                output,
                input,
                EncryptionContext.contextOf(
                        Strategy.PASSWORD_PSWD,
                        encryptionPasswordPlaintext));

        if (log.isDebugEnabled()) {
            log.debug("*** Finished Seb client configuration with password based encryption");
        }
    }

}
