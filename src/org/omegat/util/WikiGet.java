/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Kim Bruning
               2010 Alex Buloichik, Didier Briel, Rashid Umarov
               2011 Alex Buloichik
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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.apache.commons.io.IOUtils;

/**
 * Import pages from MediaWiki
 *
 * @author Kim Bruning
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Rashid Umarov
 */
public final class WikiGet {

    private WikiGet() {
    }

    protected static final String CHARSET_MARK = "charset=";
    protected static final String DEFAULT_RESPONSE_CHARSET = "ISO-8859-1";

    /**
     * ~inverse of String.split() refactor note: In future releases, this might
     * best be moved to a different file
     */
    public static String joinString(String separator, String[] items) {
        if (items.length < 1) {
            return "";
        }
        StringBuilder joined = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            joined.append(items[i]);
            if (i != items.length - 1) {
                joined.append(separator);
            }
        }
        return joined.toString();
    }

    /**
     * Gets mediawiki wiki-code data from remote server. The get strategy is
     * determined by the url format.
     *
     * @param remoteUrl
     *            string representation of well-formed URL of wikipage to be
     *            retrieved
     * @param projectdir
     *            string representation of path to the project-dir where the
     *            file should be saved.
     * @throws IOException
     */
    public static void doWikiGet(String remoteUrl, String projectdir) throws IOException {
        String joined = null; // contains edited url
        String name = null; // contains a useful page name which we can use
                            // as our filename
        if (remoteUrl.indexOf("index.php?title=") > 0) {
            // We're directly calling the mediawiki index.php script
            String[] splitted = remoteUrl.split("index.php\\?title=");
            String s = splitted[splitted.length - 1];
            name = s;
            s = s.replaceAll(" ", "_");
            // s=URLEncoder.encode(s, "UTF-8"); // breaks previously
            // correctly encoded page names
            splitted[splitted.length - 1] = s;
            joined = joinString("index.php?title=", splitted);
            joined = joined + "&action=raw";
        } else {
            // assume script is behind some sort
            // of url-rewriting
            String[] splitted = remoteUrl.split("/");
            String s = splitted[splitted.length - 1];
            name = s;
            s = s.replaceAll(" ", "_");
            // s=URLEncoder.encode(s, "UTF-8");
            splitted[splitted.length - 1] = s;
            joined = joinString("/", splitted);
            joined = joined + "?action=raw";
        }
        String page = getURL(joined);
        saveUTF8(projectdir, name + ".UTF8", page);
    }

    /**
     * Print UTF-8 text to stdout (useful for debugging)
     *
     * @param output
     *            The UTF-8 format string to be printed.
     */
    public static void printUTF8(String output) {
        try (BufferedWriter out = utf8WriterBuilder(System.out)) {
            out.write(output);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates new BufferedWriter configured for UTF-8 output and connects it to
     * an OutputStream
     *
     * @param out
     *            Outputstream to connect to.
     */
    public static BufferedWriter utf8WriterBuilder(OutputStream out) throws Exception {
        return new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8.name()));
    }

    /**
     * Save UTF-8 format data to file.
     *
     * @param dir
     *            directory to write to.
     * @param filename
     *            filename of file to write.
     * @param output
     *            UTF-8 format text to write
     */
    public static void saveUTF8(String dir, String filename, String output) {
        // Page name can contain invalid characters, see [1878113]
        // Contributed by Anatoly Techtonik
        filename = filename.replaceAll("[\\\\/:\\*\\?\\\"\\|\\<\\>]", "_");
        File path = new File(dir, filename);
        try (BufferedWriter out = utf8WriterBuilder(new FileOutputStream(path))) {
            out.write(output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtain UTF-8 format text from remote URL.
     *
     * @param target
     *            String representation of well-formed URL.
     * @throws IOException
     */
    public static String getURL(String target) throws IOException {
        StringBuilder page = new StringBuilder();
        URL url = new URL(target);
        InputStream in = url.openStream();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            page.append(new String(b, 0, n, "UTF-8"));
        }
        return page.toString();
    }

    /**
     * Obtain byte array context from remote URL.
     *
     * @param target
     *            String representation of well-formed URL.
     * @return byte array or null if status is not 200 OK
     * @throws IOException
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
     * HTTP response error storage.
     */
    @SuppressWarnings("serial")
    public static class ResponseError extends IOException {
        public final int code;
        public final String message;

        public ResponseError(HttpURLConnection conn) throws IOException {
            super(conn.getResponseCode() + ": " + conn.getResponseMessage());
            code = conn.getResponseCode();
            message = conn.getResponseMessage();
        }
    }
}
