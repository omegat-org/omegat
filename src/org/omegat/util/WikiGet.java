/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Kim Bruning
               2010 Alex Buloichik, Didier Briel, Rashid Umarov
               2011 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
import java.util.Map;

/**
 * Import pages from MediaWiki
 * 
 * @author Kim Bruning
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Rashid Umarov
 */
public class WikiGet {
    protected static final String CHARSET_MARK = "charset=";

    /**
     * ~inverse of String.split() refactor note: In future releases, this might
     * best be moved to a different file
     */
    public static String joinString(String separator, String[] items) {
        if (items.length < 1)
            return "";
        StringBuffer joined = new StringBuffer();
        for (int i = 0; i < items.length; i++) {
            joined.append(items[i]);
            if (i != items.length - 1)
                joined.append(separator);
        }
        return joined.toString();
    }

    /**
     * Gets mediawiki wiki-code data from remote server. The get strategy is
     * determined by the url format.
     * 
     * @param remote_url
     *            string representation of well-formed URL of wikipage to be
     *            retrieved
     * @param projectdir
     *            string representation of path to the project-dir where the
     *            file should be saved.
     */
    public static void doWikiGet(String remote_url, String projectdir) {
        try {
            String joined = null; // contains edited url
            String name = null; // contains a useful page name which we can use
                                // as our filename
            if (remote_url.indexOf("index.php?title=") > 0) {
                // We're directly calling the mediawiki index.php script
                String[] splitted = remote_url.split("index.php\\?title=");
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
                String[] splitted = remote_url.split("/");
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Print UTF-8 text to stdout (useful for debugging)
     * 
     * @param output
     *            The UTF-8 format string to be printed.
     */
    public static void printUTF8(String output) {
        try {
            BufferedWriter out = UTF8WriterBuilder(System.out);
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
    public static BufferedWriter UTF8WriterBuilder(OutputStream out) throws Exception {
        return new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
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
        try {
            // Page name can contain invalid characters, see [1878113]
            // Contributed by Anatoly Techtonik
            filename = filename.replaceAll("[\\\\/:\\*\\?\\\"\\|\\<\\>]", "_");
            File path = new File(dir, filename);
            FileOutputStream f = new FileOutputStream(path);
            BufferedWriter out = UTF8WriterBuilder(f);
            out.write(output);
            out.close();
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
        StringBuffer page = new StringBuffer();
        URL url = new URL(target);
        InputStream in = url.openStream();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            page.append(new String(b, 0, n, "UTF-8"));
        }
        return page.toString();
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
     * @return sever output
     */
    public static String get(String address, Map<String, String> params,
            Map<String, String> additionalHeaders) throws IOException {
        String url;
        if (params == null || params.isEmpty()) {
            url = address;
        } else {
            StringBuilder s = new StringBuilder();
            s.append(address).append('?');
            boolean next=false;
            for (Map.Entry<String, String> p : params.entrySet()) {
                if (next) {
                    s.append('&');
                }else {
                    next=true;
                }
                s.append(p.getKey());
                s.append('=');
                s.append(URLEncoder.encode(p.getValue(), OConsts.UTF8));
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

            // Added to pass through authenticated proxy
            String encodedUser = (Preferences.getPreference(Preferences.PROXY_USER_NAME));
            if (!StringUtil.isEmpty(encodedUser)) { // There is a proxy user
                String encodedPassword = (Preferences.getPreference(Preferences.PROXY_PASSWORD));
                try {
                    String pass = new String(org.omegat.util.Base64.decode(encodedUser));
                    pass += ":" + new String(org.omegat.util.Base64.decode(encodedPassword));
                    encodedPassword = org.omegat.util.Base64.encodeBytes(pass.getBytes());
                    conn.setRequestProperty("Proxy-Authorization", "Basic " + encodedPassword);
                } catch (IOException ex) {
                    Log.logErrorRB("LOG_DECODING_ERROR");
                    Log.log(ex);
                }
             }

            conn.setDoOutput(true);

            return getStringContent(conn);
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
     * @return sever output
     */
    public static String post(String address, Map<String, String> params,
            Map<String, String> additionalHeaders) throws IOException {
        URL url = new URL(address);

        ByteArrayOutputStream pout = new ByteArrayOutputStream();
        for (Map.Entry<String, String> p : params.entrySet()) {
            if (pout.size() > 0) {
                pout.write('&');
            }
            pout.write(p.getKey().getBytes(OConsts.UTF8));
            pout.write('=');
            pout.write(URLEncoder.encode(p.getValue(), OConsts.UTF8).getBytes(OConsts.UTF8));
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", Integer.toString(pout.size()));
            if (additionalHeaders != null) {
                for (Map.Entry<String, String> en : additionalHeaders.entrySet()) {
                    conn.setRequestProperty(en.getKey(), en.getValue());
                }
            }

            // Added to pass through authenticated proxy
            String encodedUser = (Preferences.getPreference(Preferences.PROXY_USER_NAME));
            if (!StringUtil.isEmpty(encodedUser)) { // There is a proxy user
                String encodedPassword = (Preferences.getPreference(Preferences.PROXY_PASSWORD));
                try {
                    String pass = new String(org.omegat.util.Base64.decode(encodedUser));
                    pass += ":" + new String(org.omegat.util.Base64.decode(encodedPassword));
                    encodedPassword = org.omegat.util.Base64.encodeBytes(pass.getBytes());
                    conn.setRequestProperty("Proxy-Authorization", "Basic " + encodedPassword);
                } catch (IOException ex) {
                    Log.logErrorRB("LOG_DECODING_ERROR");
                    Log.log(ex);
                }
             }

            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream cout = conn.getOutputStream();
            cout.write(pout.toByteArray());
            cout.flush();

            return getStringContent(conn);
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Parse response as string.
     */
    private static String getStringContent(HttpURLConnection conn) throws IOException {
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new ResponseError(conn);
        }
        String contentType = conn.getHeaderField("Content-Type");
        int cp = contentType != null ? contentType.indexOf(CHARSET_MARK) : -1;
        String charset = cp >= 0 ? contentType.substring(cp + CHARSET_MARK.length()) : "ISO8859-1";
        ByteArrayOutputStream res = new ByteArrayOutputStream();
        InputStream in = conn.getInputStream();
        try {
            LFileCopy.copy(in, res);
        } finally {
            in.close();
        }
        return new String(res.toByteArray(), charset);
    }

    /**
     * HTTP response error storage.
     */
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
