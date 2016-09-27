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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.IOUtils;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;


public class LanguageToolNetworkBridge extends BaseLanguageToolBridge {

    /* Constants */
    private final static String URL_PATH = "/v2/check";
    private final static String SERVER_CLASS_NAME = "org.languagetool.server.HTTPServer";
    private final static String API_VERSION = "1";

    /* Instance scope fields */
    private Process server;
    private int localPort;
    private String serverUrl;
    private ScriptEngine engine;

    /* Project scope fields */
    private Language sourceLang, targetLang;
    private String disabledCategories, disabledRules, enabledRules;

    /**
     * Get instance talking to remote server
     *
     * @param url
     *            URL of remote LanguageTool server
     * @return new LanguageToolNetworkBridge instance
     * @throws java.lang.Exception
     */
    public LanguageToolNetworkBridge(Language sourceLang, Language targetLang, String url) throws Exception {
        // Try to connect URL
        if (!testServer(url)) {
            Log.logWarningRB("LT_BAD_URL");
            throw new Exception();
        }
        // OK, URL seems valid, let's use it.
        serverUrl = url;
        init(sourceLang, targetLang);
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
    public LanguageToolNetworkBridge(Language sourceLang, Language targetLang, String path, int port) throws Exception {
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

        // Create thread to consume server output
        new Thread(() -> {
            try (InputStream is = server.getInputStream()) {
                @SuppressWarnings("unused")
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

        serverUrl = "http://localhost:" + port + URL_PATH;
        Log.log(OStrings.getString("LT_SERVER_STARTED"));
        init(sourceLang, targetLang);
    }

    /**
     * Common initialization for both constructors
     */
    private void init(Language sourceLang, Language targetLang) {
        this.sourceLang = sourceLang;
        this.targetLang = targetLang;
        engine = new ScriptEngineManager().getEngineByName("javascript");
    }

    @Override
    public void stop() {
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
    public void applyRuleFilters(Set<String> disabledCategories,
            Set<String> disabledRules, Set<String> enabledRules) {
        this.disabledCategories = String.join(",", disabledCategories);
        this.disabledRules = String.join(",", disabledRules);
        this.enabledRules = String.join(",", enabledRules);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<LanguageToolResult> getCheckResultsImpl(String sourceText, String translationText) throws Exception {

        URL url = new URL(serverUrl);
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", OStrings.getNameAndVersion());
        conn.setDoOutput(true);
        try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
            writer.write(buildPostData(sourceLang.toString(), targetLang.toString(),
                    sourceText, translationText, disabledCategories, disabledRules, enabledRules));
            writer.flush();
        }

        // Read response into string specially wrapped for Nashorn
        String json = "";
        try (InputStream in = conn.getInputStream()) {
            json = IOUtils.toString(in);
        }

        Map<String, Object> response = (Map<String, Object>) engine.eval("Java.asJSONCompatible(" + json + ')');
        Map<String, String> software = (Map<String, String>) response.get("software");

        if (!software.get("apiVersion").equals(API_VERSION)) {
            Log.logWarningRB("LT_API_VERSION_MISMATCH");
        }

        List<Map<String, Object>> matches = (List<Map<String, Object>>) response.get("matches");

        return matches.stream().map(match -> {
            String message = addSuggestionTags((String) match.get("message"));
            int start = (int) match.get("offset");
            int end = start + (int) match.get("length");
            Map<String, Object> rule = (Map<String, Object>) match.get("rule");
            String ruleId = (String) rule.get("id");
            String ruleDescription = (String) rule.get("description");
            return new LanguageToolResult(message, start, end, ruleId, ruleDescription);
        }).collect(Collectors.toList());
     }

    /**
     * Replace double quotes with <suggestion></suggestion> tags
     * in error message to imitate native LanguageTool behavior
     */
    static String addSuggestionTags(String str) {
        return str.replaceAll("^([^:]+:\\s?)\"([^']+)\"", "$1<suggestion>$2</suggestion>");
    }

    /**
     * Construct POST request data
     */
    static String buildPostData(String sourceLang, String targetLang, String sourceText,
            String targetText, String disabledCategories, String disabledRules, String enabledRules)
            throws UnsupportedEncodingException {
        String encoding = "UTF-8";
        StringBuilder result = new StringBuilder();
        result.append("text=").append(URLEncoder.encode(targetText, encoding)).append("&language=")
                .append(URLEncoder.encode(targetLang, encoding));
        if (sourceText != null) {
            result.append("&srctext=").append(URLEncoder.encode(sourceText, encoding)).append("&motherTongue=")
                    .append(URLEncoder.encode(sourceLang, encoding));
        }
        if (disabledCategories != null) {
            result.append("&disabledCategories=").append(URLEncoder.encode(disabledCategories, encoding));
        }
        if (disabledRules != null) {
            result.append("&disabledRules=").append(URLEncoder.encode(disabledRules, encoding));
        }
        if (enabledRules != null) {
            result.append("&enabledRules=").append(URLEncoder.encode(enabledRules, encoding));
        }
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
                // Supply a dummy disabled category to force the server to take
                // its configuration from this query only, not any server-side
                // config.
                writer.write(buildPostData(null, "en-US", null, "Test", "FOO", null, null));
                writer.flush();
            }
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            if (!headerFields.get(null).toString().contains("200")) {
                return false;
            }
            try (InputStream in = conn.getInputStream()) {
                String response = IOUtils.toString(in);
                if (response.contains("<?xml")) {
                    Log.logErrorRB("LT_WRONG_FORMAT_RESPONSE");
                    return false;
                } else {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

}
