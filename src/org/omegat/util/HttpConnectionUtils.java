/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Kim Bruning
               2010 Alex Buloichik, Didier Briel, Rashid Umarov
               2011 Alex Buloichik
               2021 Hiroshi Miura
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;


/**
 * Utility collection for http connections.
 *
 * @author Kim Bruning
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Rashid Umarov
 * @author Hiroshi Miura
 */
public final class HttpConnectionUtils {
    protected static final String DEFAULT_RESPONSE_CHARSET = "ISO-8859-1";
    protected static final String CHARSET_MARK = "charset=";
    /**
     * Buffer size for downloading.
     */
    private static final int BUFFER_SIZE = 8196;

    /**
     * default timeout
     */
    private static final int TIMEOUT_MS = 10_000;

    /**
     * Don't instantiate util class.
     */
    private HttpConnectionUtils() {}

    /**
     * Get resource from URL with default timeout.
     * @param url resource URL.
     * @return string returned from server.
     * @throws IOException raises when connection is failed.
     */
    public static String getURL(URL url) throws IOException {
        return getURL(url, TIMEOUT_MS);
    }

    /**
     * Download a file to memory.
     *
     * @param url resource URL to download
     * @param timeout timeout to connect and read.
     * @return returned string
     * @throws IOException when connection and read method error.
     */
    public static String getURL(URL url, int timeout) throws IOException {
        URLConnection urlConn = url.openConnection();
        urlConn.setConnectTimeout(timeout);
        urlConn.setReadTimeout(timeout);
        try (InputStream in = urlConn.getInputStream()) {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        }
    }

    /**
     * Download Zip file from remote site and extract it to specified directory.
     *
     * @param url URL of zip file resource to download.
     * @param dir target directory to extract
     * @param expectedFiles filter extract file names
     * @throws IOException raises when extraction is failed, maybe flaky download happened.
     */
    public static void downloadZipFileAndExtract(URL url, File dir, List<String> expectedFiles) throws IOException {
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        try (InputStream in = conn.getInputStream()) {
            List<String> extracted = StaticUtils.extractFromZip(in, dir, expectedFiles::contains);
            if (!expectedFiles.containsAll(extracted)) {
                throw new FileNotFoundException("Could not extract expected files from zip; expected: "
                        + expectedFiles + ", extracted: " + extracted);
            }
        } catch (IllegalArgumentException ex) {
            throw new FlakyDownloadException(ex);
        }
    }

    /**
     * Downloads a binary file from a URL.
     * @param fileURL HTTP URL of the file to be downloaded
     * @param headers Additional HTTP headers
     * @param expectedMime Mime type expected and check against such as ["application/octet-stream",
     *                    "application/jar-archive"]. If getting type is differed, return false.
     * @param saveFilePath path of the file
     * @return true when succeeded, otherwise false.
     * @throws IOException raise when connection and file write failed.
     * @throws FlakyDownloadException raise when downloaded file length differs from expected content length.
     */
    public static boolean downloadBinaryFile(final URL fileURL, final Map<String, String> headers,
                                             final Set<String> expectedMime, final File saveFilePath)
            throws IOException, FlakyDownloadException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) fileURL.openConnection();
        headers.forEach(httpURLConnection::setRequestProperty);
        httpURLConnection.setConnectTimeout(TIMEOUT_MS);
        httpURLConnection.setReadTimeout(TIMEOUT_MS);
        try {
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.logErrorRB("HCU_RESPONSE_ERROR", responseCode);
                return false;
            }
            String contentType = httpURLConnection.getContentType();
            if (!expectedMime.contains(contentType)) {
                Log.logErrorRB("HCU_MIME_ERROR", contentType);
                return false;
            }
            try (InputStream inputStream = httpURLConnection.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(saveFilePath)) {
                long transferred = IOUtils.copy(inputStream, outputStream, BUFFER_SIZE);
                if (transferred != httpURLConnection.getContentLength()) {
                    httpURLConnection.disconnect();
                    throw new FlakyDownloadException(
                            "Downloaded file length differs from expected content length reported in header.");
                }
                return true;
            } catch (Exception ex) {
                Log.log(ex);
                return false;
            }
        } finally {
            httpURLConnection.disconnect();
        }
    }

    /**
     * Obtain byte array context from remote URL.
     *
     * @param target
     *            String representation of well-formed URL.
     * @return byte array or null if status is not 200 OK
     * @throws IOException raise when connection failed.
     */
    public static byte[] getURLasByteArray(String target) throws IOException {
        URL url = new URL(target);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            return null;
        }
        try (InputStream in = conn.getInputStream()) {
            return IOUtils.toByteArray(in);
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Method call without additional headers for possible calls from plugins.
     *
     * @param address URL to post
     * @param params post parameters in Map
     * @return result string returned from server.
     * @throws IOException raises when connection failed.
     */
    public static String post(String address, Map<String, String> params) throws IOException {
        return post(address, params, null);
    }

    /**
     * Get data from the remote URL.
     *
     * @param address
     *            address to post
     * @param params
     *            parameters
     * @param additionalHeaders
     *            additional headers for request, can be null
     * @return server output
     */
    public static String get(String address, Map<String, String> params,
            Map<String, String> additionalHeaders) throws IOException {
        return get(address, params, additionalHeaders, DEFAULT_RESPONSE_CHARSET);
    }

    /**
     * Get data from the remote URL.
     *
     * @param address
     *            address to post
     * @param params
     *            parameters
     * @param additionalHeaders
     *            additional headers for request, can be null
     * @param defaultOutputCharset
     *            default charset used to interpret the response
     * @return server output
     */
    public static String get(String address, Map<String, String> params,
            Map<String, String> additionalHeaders, String defaultOutputCharset) throws IOException {
        String url;
        if (params == null || params.isEmpty()) {
            url = address;
        } else {
            StringBuilder s = new StringBuilder();
            s.append(address).append('?');
            boolean next = false;
            for (Map.Entry<String, String> p : params.entrySet()) {
                if (next) {
                    s.append('&');
                } else {
                    next = true;
                }
                s.append(p.getKey());
                s.append('=');
                s.append(URLEncoder.encode(p.getValue(), StandardCharsets.UTF_8.name()));
            }
            url = s.toString();
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try {
            conn.setRequestMethod("GET");
            if (additionalHeaders != null) {
                for (Map.Entry<String, String> en : additionalHeaders.entrySet()) {
                    conn.setRequestProperty(en.getKey(), en.getValue());
                }
            }

            addProxyAuthentication(conn);

            conn.setDoOutput(true);

            return getStringContent(conn, defaultOutputCharset);
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Post data to the remote URL.
     *
     * @param address
     *            address to post
     * @param params
     *            parameters
     * @param additionalHeaders
     *            additional headers for request, can be null
     * @return Server output
     */
    public static String post(String address, Map<String, String> params,
            Map<String, String> additionalHeaders) throws IOException {
        URL url = new URL(address);

        ByteArrayOutputStream pout = new ByteArrayOutputStream();
        if (params != null) {
            for (Map.Entry<String, String> p : params.entrySet()) {
                if (pout.size() > 0) {
                    pout.write('&');
                }
                pout.write(p.getKey().getBytes(StandardCharsets.UTF_8));
                pout.write('=');
                pout.write(URLEncoder.encode(p.getValue(), StandardCharsets.UTF_8.name())
                        .getBytes(StandardCharsets.UTF_8));
            }
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(pout.size()));
        if (additionalHeaders != null) {
            for (Map.Entry<String, String> en : additionalHeaders.entrySet()) {
                conn.setRequestProperty(en.getKey(), en.getValue());
            }
        }

        addProxyAuthentication(conn);

        conn.setDoInput(true);
        conn.setDoOutput(true);

        try (OutputStream cout = conn.getOutputStream()) {
            cout.write(pout.toByteArray());
            cout.flush();
            return getStringContent(conn);
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Post JSON data to the remote URL.
     *
     * @param address
     *            address to post
     * @param json
     *            JSON-encoded data
     * @param additionalHeaders
     *            additional headers for request, can be null
     * @return Server output
     */
    public static String postJSON(String address, String json,
            Map<String, String> additionalHeaders) throws IOException {
        URL url = new URL(address);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Length", Integer.toString(json.length()));
        if (additionalHeaders != null) {
            for (Map.Entry<String, String> en : additionalHeaders.entrySet()) {
                conn.setRequestProperty(en.getKey(), en.getValue());
            }
        }

        addProxyAuthentication(conn);

        conn.setDoInput(true);
        conn.setDoOutput(true);

        try (OutputStream cout = conn.getOutputStream()) {
            cout.write(json.getBytes(StandardCharsets.UTF_8));
            cout.flush();
            return getStringContent(conn, "utf-8");
        } finally {
            conn.disconnect();
        }
    }

    private static void addProxyAuthentication(HttpURLConnection conn) {
        // Added to pass through authenticated proxy
        String encodedUser = Preferences.getPreference(Preferences.PROXY_USER_NAME);
        if (!StringUtil.isEmpty(encodedUser)) { // There is a proxy user
            String encodedPassword = Preferences.getPreference(Preferences.PROXY_PASSWORD);
            try {
                String userPass = StringUtil.decodeBase64(encodedUser, StandardCharsets.ISO_8859_1) + ":"
                        + StringUtil.decodeBase64(encodedPassword, StandardCharsets.ISO_8859_1);
                String encodedUserPass = Base64.getMimeEncoder()
                        .encodeToString(userPass.getBytes(StandardCharsets.ISO_8859_1));
                conn.setRequestProperty("Proxy-Authorization", "Basic " + encodedUserPass);
            } catch (IllegalArgumentException ex) {
                Log.logErrorRB("LOG_DECODING_ERROR");
                Log.log(ex);
            }
        }
    }

    private static String getStringContent(HttpURLConnection conn) throws IOException {
        return getStringContent(conn, DEFAULT_RESPONSE_CHARSET);
    }

    /**
     * Parse response as string.
     */
    private static String getStringContent(HttpURLConnection conn, String defaultCharset) throws IOException {
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new ResponseError(conn);
        }
        String contentType = conn.getHeaderField("Content-Type");
        int cp = contentType != null ? contentType.indexOf(CHARSET_MARK) : -1;
        String charset = cp >= 0 ? contentType.substring(cp + CHARSET_MARK.length()) : defaultCharset;

        try (InputStream in = conn.getInputStream()) {
            return IOUtils.toString(in, charset);
        }
    }

    /**
     * Downloaded file error.
     */
    @SuppressWarnings("serial")
    public static class FlakyDownloadException extends RuntimeException {
        public FlakyDownloadException(Exception cause) {
            super(cause);
        }

        public FlakyDownloadException(String cause) {
            super(cause);
        }
    }

    /**
     * HTTP response error storage.
     */
    @SuppressWarnings("serial")
    public static class ResponseError extends IOException {
        public final int code;
        public final String message;
        public final String body;

        public ResponseError(HttpURLConnection conn) throws IOException {
            super(conn.getResponseCode() + ": " + conn.getResponseMessage());
            code = conn.getResponseCode();
            message = conn.getResponseMessage();
            try (InputStream in = conn.getErrorStream()) {
                body = IOUtils.toString(in, StandardCharsets.UTF_8.name());
            }
        }
    }
}
