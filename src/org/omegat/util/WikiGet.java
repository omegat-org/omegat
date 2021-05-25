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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
     * @param remoteUrl  string representation of well-formed URL of wikipage to be
     *                   retrieved
     * @param projectdir string representation of path to the project-dir where the
     *                   file should be saved.
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
        String page = HttpConnectionUtils.getURL(new URL(joined));
        saveUTF8(projectdir, name + ".UTF8", page);
    }

    /**
     * Print UTF-8 text to stdout (useful for debugging)
     *
     * @param output The UTF-8 format string to be printed.
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
     * @param out Outputstream to connect to.
     */
    public static BufferedWriter utf8WriterBuilder(OutputStream out) throws Exception {
        return new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8.name()));
    }

    /**
     * Save UTF-8 format data to file.
     *
     * @param dir      directory to write to.
     * @param filename filename of file to write.
     * @param output   UTF-8 format text to write
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
     * @deprecated
     * This method is moved to HttpConnectionUtils class.
     *
     * @param target
     *            String representation of well-formed URL.
     * @throws IOException
     */
    @Deprecated
    public static String getURL(String target) throws IOException {
        return HttpConnectionUtils.getURL(new URL(target));
    }

    /**
     * Obtain byte array context from remote URL.
     * @deprecated
     * This method is moved to HttpConnectionUtils class.
     *
     * @param target
     *            String representation of well-formed URL.
     * @return byte array or null if status is not 200 OK
     * @throws IOException
     */
    @Deprecated
    public static byte[] getURLasByteArray(String target) throws IOException {
        return HttpConnectionUtils.getURLasByteArray(target);
    }

    /**
     * Method call without additional headers for possible calls from plugins.(deprecated)
     * @deprecated
     * This method is moved to HttpConnectionUtils class.
     */
    @Deprecated
    public static String post(String address, Map<String, String> params) throws IOException {
        return HttpConnectionUtils.post(address, params, null);
    }

    /**
     * Get data from the remote URL.
     * @deprecated
     * This method is moved to HttpConnectionUtils class.
     *
     * @param address
     *            address to post
     * @param params
     *            parameters
     * @param additionalHeaders
     *            additional headers for request, can be null
     * @return server output
     */
    @Deprecated
    public static String get(String address, Map<String, String> params,
            Map<String, String> additionalHeaders) throws IOException {
        return HttpConnectionUtils.get(address, params, additionalHeaders);
    }

    /**
     * Get data from the remote URL.
     * @deprecated
     * This method is moved to HttpConnectionUtils class.
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
    @Deprecated
    public static String get(String address, Map<String, String> params,
            Map<String, String> additionalHeaders, String defaultOutputCharset) throws IOException {
        return HttpConnectionUtils.get(address, params, additionalHeaders, defaultOutputCharset);
    }

    /**
     * Post data to the remote URL.
     * @deprecated
     * This method is moved to HttpConnectionUtils class.
     *
     * @param address
     *            address to post
     * @param params
     *            parameters
     * @param additionalHeaders
     *            additional headers for request, can be null
     * @return Server output
     */
    @Deprecated
    public static String post(String address, Map<String, String> params,
            Map<String, String> additionalHeaders) throws IOException {
        return HttpConnectionUtils.post(address, params, additionalHeaders);
    }

    /**
     * Post JSON data to the remote URL.
     * @deprecated
     * This method is moved to HttpConnectionUtils class.
     *
     * @param address
     *            address to post
     * @param json
     *            JSON-encoded data
     * @param additionalHeaders
     *            additional headers for request, can be null
     * @return Server output
     */
    @Deprecated
    public static String postJSON(String address, String json,
            Map<String, String> additionalHeaders) throws IOException {
        return HttpConnectionUtils.postJSON(address, json, additionalHeaders);
    }

    /**
     * HTTP response error storage.
     */
    @SuppressWarnings("serial")
    @Deprecated
    public static class ResponseError extends HttpConnectionUtils.ResponseError {
        public ResponseError(HttpURLConnection conn) throws IOException {
            super(conn);
        }
    }
}
