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
import java.util.zip.GZIPOutputStream;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import ch.ethz.seb.sebserver.gbl.profile.WebServiceProfile;
import ch.ethz.seb.sebserver.webservice.servicelayer.sebconfig.ZipService;

@Lazy
@Component
@WebServiceProfile
public class ZipServiceImpl implements ZipService {

    private static final Logger log = LoggerFactory.getLogger(ZipServiceImpl.class);

    @Override
    public void write(final OutputStream out, final InputStream in) {

        if (log.isDebugEnabled()) {
            log.debug("*** Start streaming asynchronous zipping of SEB exam configuration data");
        }

        GZIPOutputStream zipOutputStream = null;
        try {

            zipOutputStream = new GZIPOutputStream(out);

            IOUtils.copyLarge(in, zipOutputStream);

            in.close();
            zipOutputStream.flush();
            zipOutputStream.close();

        } catch (final IOException e) {
            log.error("Error while streaming data to zipped output: ", e);
        } finally {
            try {
                if (zipOutputStream != null)
                    zipOutputStream.close();
            } catch (final IOException e) {
                log.error("Failed to close ZipOutputStream: ", e);
            }

            if (log.isDebugEnabled()) {
                log.debug("*** Finish streaming asynchronous zipping of SEB exam configuration data");
            }
        }
    }

    @Override
    public void read(final OutputStream out, final InputStream in) {
        // TODO Auto-generated method stub

    }

}