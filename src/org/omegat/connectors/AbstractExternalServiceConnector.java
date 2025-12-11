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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
import org.omegat.util.CredentialsManager;
import org.omegat.util.HttpConnectionUtils;
import org.omegat.util.OStrings;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

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
    public List<ExternalResource> listResources(ServiceTarget target, String keyword)
            throws ConnectorException {
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

    /**
     * Sends an HTTP GET request to the specified URL and retrieves the response
     * as a string. If the URL is invalid or cannot be reached, a warning
     * message is displayed, and a {@link ConnectorException} is thrown.
     *
     * @param url
     *            the URL to which the HTTP GET request should be sent
     * @return the response from the server as a string
     * @throws ConnectorException
     *             if the URL is invalid or an error occurs during the request
     */
    protected String httpGet(String url) throws ConnectorException {
        if (!HttpConnectionUtils.checkUrl(url)) {
            JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(),
                    OStrings.getString("TF_WIKI_IMPORT_URL_ERROR"),
                    OStrings.getString("TF_WIKI_IMPORT_URL_ERROR_TITLE"), JOptionPane.WARNING_MESSAGE);
            throw new ConnectorException(OStrings.getString("TF_WIKI_IMPORT_URL_ERROR"));
        }
        return getURL(url, null, 10000);
    }

    /**
     * Download a file to memory.
     *
     * @param url
     *            resource URL to download
     * @param cred
     *            BotPassword credential, can be null.
     * @param timeout
     *            timeout to connect and read.
     * @return returned string
     * @throws ConnectorException
     *             when connection and read method error.
     */
    public String getURL(String url, @Nullable String cred, int timeout) throws ConnectorException {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout).setSocketTimeout(timeout)
                .setConnectionRequestTimeout(timeout).build();

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy()).setDefaultRequestConfig(config)
                .useSystemProperties().build()) {
            HttpGet httpGet = new HttpGet(url);
            if (cred != null && !cred.isEmpty()) {
                httpGet.setHeader("Authorization",
                        "Basic " + Base64.getEncoder().encodeToString(cred.getBytes(StandardCharsets.UTF_8)));
            }
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                } else {
                    throw new ConnectorException("Unexpected response to GET: " + url, status);
                }
            }
        } catch (IOException e) {
            throw new ConnectorException(url, e);
        }
    }

    /**
     * Retrieve a credential with the given ID. First checks temporary system
     * properties, then falls back to the program's persistent preferences.
     * Store a credential with {@link #setCredential(String, String, boolean)}.
     *
     * @param id
     *            ID or key of the credential to retrieve
     * @return the credential value in plain text
     */
    protected String getCredential(String id) {
        String property = System.getProperty(id);
        if (property != null) {
            return property;
        }
        return CredentialsManager.getInstance().retrieve(id).orElse("");
    }

    /**
     * Store a credential. Credentials are stored in temporary system properties
     * and, if <code>temporary</code> is <code>false</code>, in the program's
     * persistent preferences encoded in Base64. Retrieve a credential with
     * {@link #getCredential(String)}.
     *
     * @param id
     *            ID or key of the credential to store
     * @param value
     *            value of the credential to store
     * @param temporary
     *            if <code>false</code>, encode with Base64 and store in
     *            persistent preferences as well
     */
    protected void setCredential(String id, String value, boolean temporary) {
        System.setProperty(id, value);
        CredentialsManager.getInstance().store(id, temporary ? "" : value);
    }

    protected String @Nullable [] askCredentials(String title, String message) {
        JTextField userField = new JTextField("", 20);
        JPasswordField passField = new JPasswordField(20);
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.GridLayout(0, 1));
        panel.add(new JLabel(OStrings.getString("LOGIN_USER")));
        panel.add(userField);
        panel.add(new JLabel(OStrings.getString("LOGIN_PASSWORD")));
        panel.add(passField);
        panel.add(new JLabel(message));
        int result = JOptionPane.showConfirmDialog(Core.getMainWindow().getApplicationFrame(), panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            if (!u.isEmpty() && !p.isEmpty()) {
                return new String[] { u, p };
            }
        }
        return null;
    }
}
