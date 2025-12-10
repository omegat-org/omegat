/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.connectors;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.jspecify.annotations.Nullable;
import org.omegat.connectors.dto.ExternalResource;
import org.omegat.connectors.spi.IExternalServiceConnector;
import org.omegat.connectors.spi.ConnectorException;
import org.omegat.connectors.dto.ServiceTarget;
import org.omegat.core.Core;
import org.omegat.util.HttpConnectionUtils;
import org.omegat.util.OStrings;

import javax.swing.JOptionPane;

/**
 * Base class for External service connectors with common helpers and defaults.
 */
public abstract class AbstractExternalServiceConnector implements IExternalServiceConnector {

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public abstract String getPreferenceName();

    @Override
    public @Nullable String getDefaultBaseUrl() {
        return null;
    }

    @Override
    public List<ExternalResource> listResources(ServiceTarget target, String keyword) throws ConnectorException {
        return Collections.emptyList();
    }

    @Override
    public InputStream fetchResource(ServiceTarget target, String resourceId) throws ConnectorException {
        throw new ConnectorException("Fetch not implemented");
    }

    @Override
    public InputStream fetchResource(String url) throws ConnectorException {
        throw new ConnectorException("Fetch not implemented");
    }

    @Override
    public void pushTranslation(String projectId, String resourceId, InputStream translated)
            throws ConnectorException {
        throw new ConnectorException("Push not supported");
    }

    protected String httpGet(String url) throws ConnectorException {
        if (!HttpConnectionUtils.checkUrl(url)) {
            JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(),
                    OStrings.getString("TF_WIKI_IMPORT_URL_ERROR"),
                    OStrings.getString("TF_WIKI_IMPORT_URL_ERROR_TITLE"), JOptionPane.WARNING_MESSAGE);
            throw new ConnectorException(OStrings.getString("TF_WIKI_IMPORT_URL_ERROR"));
        }
        try {
            return getURL(new URL(url), 10000);
        } catch (IOException e) {
            throw new ConnectorException("GET failed: " + url, e);
        }
    }

    /**
     * Download a file to memory.
     *
     * @param url
     *            resource URL to download
     * @param timeout
     *            timeout to connect and read.
     * @return returned string
     * @throws IOException
     *             when connection and read method error.
     */
    public String getURL(URL url, int timeout) throws IOException {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .build();

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultRequestConfig(config)
                .useSystemProperties()
                .build()) {
            HttpGet httpGet = new HttpGet(url.toString());
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                } else {
                    throw new IOException("Unexpected response status: " + status);
                }
            }
        }
    }
}
