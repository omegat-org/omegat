/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
 with fuzzy matching, translation memory, keyword search,
 glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Lev Abashkin
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
package org.omegat.languagetools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

import net.arnx.jsonic.JSON;

public class LanguageToolNetworkBridge implements ILanguageToolBridge {

    /* Constants */
    private final static String URL_PATH = "/v2/check";
    private final static String SERVER_CLASS_NAME = "org.languagetool.server.HTTPServer";

    /* Instance scope fields */

    private Process server;
    private int localPort;
    private String serverUrl;

    /* Project scope fields */
    private Language sourceLang, targetLang;

    /**
     * Get instance talking to remote server
     *
     * @param url
     *            URL of remote LanguageTool server
     * @return new LanguageToolNetworkBridge instance
     * @throws java.lang.Exception
     */
    public LanguageToolNetworkBridge(String url) throws Exception {
        // Try to connect URL
        if (!testServer(url)) {
            Log.logWarningRB("LT_BAD_URL");
            throw new Exception();
        }
        // OK, URL seems valid, let's use it.
        serverUrl = url;
    }

    /**
     * Get instance spawning and talking to local server
     *
     * @param path
     *            local LanguageTool directory
     * @param port
     *            local port for spawned server to listen
     * @return new LanguageToolNetworkBridge instance
     * @throws java.lang.Exception
     */
    public LanguageToolNetworkBridge(String path, int port) throws Exception {
        // Remember port
        localPort = port;

        File serverJar = new File(path);

        // Check if ClassPath points to a real file
        if (!serverJar.isFile()) {
            Log.logWarningRB("LT_BAD_LOCAL_PATH");
            throw new Exception();
        }

        // Check if socket is available
        try {
            new ServerSocket(port).close();
        } catch (Exception e) {
            Log.logWarningRB("LT_BAD_SOCKET");
            throw new Exception();
        }
        // Run the server
        ProcessBuilder pb = new ProcessBuilder("java", "-cp", serverJar.getAbsolutePath(), SERVER_CLASS_NAME, "--port",
                Integer.toString(port));
        pb.redirectErrorStream(true);
        server = pb.start();

        // Create a thread to consume server output
        new Thread(() -> {
            try (InputStream is = server.getInputStream()) {
                int b;
                while ((b = is.read()) != -1) {
                    // Discard
                }
            } catch (IOException e) {
                // Do nothing
            }
        }).start();

        // Wait for server to start
        int timeout = 10000;
        int timeWaiting = 0;
        int interval = 10;
        while (true) {
            Thread.sleep(interval);
            timeWaiting += interval;
            try {
                new Socket("localhost", port).close();
                break;
            } catch (Exception e) {
            }
            if (timeWaiting >= timeout) {
                Log.logWarningRB("LT_SERVER_START_TIMEOUT");
                server.destroy();
                throw new Exception();
            }
        }

        serverUrl = "http://localhost:" + Integer.toString(port) + URL_PATH;
        Log.log(OStrings.getString("LT_SERVER_STARTED"));
    }

    @Override
    public void onProjectLoad() {
        sourceLang = Core.getProject().getProjectProperties().getSourceLanguage();
        targetLang = Core.getProject().getProjectProperties().getTargetLanguage();
    }

    @Override
    public void onProjectClose() {
        sourceLang = null;
        targetLang = null;
    }

    @Override
    public void destroy() {
        if (server != null) {
            try {
                server.destroy();
                // Wait for server to release socket
                while (true) {
                    try {
                        (new Socket("localhost", localPort)).close();
                    } catch (Exception e) {
                        break;
                    }
                }

                Log.log(OStrings.getString("LT_SERVER_TERMINATED"));
                server = null;
            } catch (Exception ex) {
                Log.log(ex);
            }
        }
    }

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText)
            throws Exception {

        URL url = new URL(serverUrl);
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
        writer.write(buildPostData(sourceLang.toString(), targetLang.toString(), sourceText, translationText));
        writer.flush();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        LanguageToolJSONResponse response = JSON.decode(reader, LanguageToolJSONResponse.class);
        writer.close();
        reader.close();

        if (!response.apiVersionIsValid()) {
            Log.logWarningRB("LT_API_VERSION_MISMATCH");
        }

        List<Mark> r = new ArrayList<>();
        for (LanguageToolJSONResponse.Match match : response.matches) {
            Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION,
                              match.offset.intValue(),
                              match.offset.intValue() + match.length.intValue());
            m.toolTipText = addSuggestionTags(match.message);
            m.painter = LanguageToolWrapper.PAINTER;
            r.add(m);
        }
        return r;
    }

    /**
     * Replace double quotes with <suggestion></suggestion> tags
     * in error message to imitate native LanguageTool behavior
     */
    private static String addSuggestionTags(String str) {
        return str.replaceAll("^([^:]+:\\s?)\"([^']+)\"", "$1<suggestion>$2</suggestion>");
    }

    /**
     * Construct POST request data
     */
    static String buildPostData(String sourceLang, String targetLang, String sourceText, String targetText)
            throws UnsupportedEncodingException {
        String encoding = "UTF-8";
        StringBuilder result = new StringBuilder();
        result.append("text=").append(URLEncoder.encode(targetText, encoding)).append("&language=")
                .append(URLEncoder.encode(targetLang, encoding));
        if (sourceText != null) {
            result.append("&srctext=").append(URLEncoder.encode(sourceText, encoding)).append("&motherTongue=")
                    .append(URLEncoder.encode(sourceLang, encoding));
        }
        // Exclude spelling rules
        result.append("&disabledCategories=TYPOS");
        // Exclude bitext rules
        result.append("&disabledRules=").append(URLEncoder.encode("SAME_TRANSLATION,TRANSLATION_LENGTH", encoding));
        return result.toString();
    }

    /**
     * Try to talk with LT server and return result
     */
    static boolean testServer(String testUrl) {
        try {
            URL url = new URL(testUrl);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                writer.write(buildPostData(null, "en-US", null, "Test"));
                writer.flush();
            }
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            return headerFields.get(null).toString().indexOf("200") > 0;
        } catch (Exception e) {
            return false;
        }
    }

}
