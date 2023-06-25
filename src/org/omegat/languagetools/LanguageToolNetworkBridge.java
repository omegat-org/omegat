/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
 with fuzzy matching, translation memory, keyword search,
 glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Lev Abashkin
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
package org.omegat.languagetools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

public class LanguageToolNetworkBridge extends BaseLanguageToolBridge {

    /* Constants */
    private static final String CHECK_PATH = "/v2/check";
    private static final String LANGS_PATH = "/v2/languages";
    private static final String SERVER_CLASS_NAME = "org.languagetool.server.HTTPServer";
    private static final String API_VERSION = "1";

    /* Instance scope fields */
    private Process server;
    private int localPort;
    private String serverUrl;

    /* Project scope fields */
    private Language sourceLang, targetLang;
    private String disabledCategories, disabledRules, enabledRules;

    /**
     * Get instance talking to remote server
     *
     * @param url
     *            URL of remote LanguageTool server
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
    public LanguageToolNetworkBridge(Language sourceLang, Language targetLang, String path, int port)
            throws Exception {
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
        ProcessBuilder pb = new ProcessBuilder("java", "-cp", serverJar.getAbsolutePath(), SERVER_CLASS_NAME,
                "--port", Integer.toString(port));
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

        serverUrl = "http://localhost:" + port + CHECK_PATH;
        Log.log(OStrings.getString("LT_SERVER_STARTED"));
        try {
            init(sourceLang, targetLang);
        } catch (Exception ex) {
            stop();
            throw ex;
        }
    }

    /**
     * Common initialization for both constructors
     *
     * @throws Exception
     *             If unable to determine the server's supported languages
     */
    private void init(Language sourceLang, Language targetLang) throws Exception {
        JsonNode serverLanguages = getSupportedLanguages();
        this.sourceLang = negotiateLanguage(serverLanguages, sourceLang);
        this.targetLang = negotiateLanguage(serverLanguages, targetLang);
        Log.log("Negotiated LanguageTool source language: " + this.sourceLang);
        Log.log("Negotiated LanguageTool target language: " + this.targetLang);
    }

    @Override
    public synchronized void stop() {
        if (server != null) {
            try {
                server.destroy();
                // Wait for server to release socket
                while (true) {
                    try {
                        new Socket("localhost", localPort).close();
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
    public void applyRuleFilters(Set<String> disabledCategories, Set<String> disabledRules,
            Set<String> enabledRules) {
        this.disabledCategories = String.join(",", disabledCategories);
        this.disabledRules = String.join(",", disabledRules);
        this.enabledRules = String.join(",", enabledRules);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<LanguageToolResult> getCheckResultsImpl(String sourceText, String translationText)
            throws Exception {
        if (targetLang == null) {
            return Collections.emptyList();
        }

        URL url = new URL(serverUrl);
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", OStrings.getNameAndVersion());
        conn.setDoOutput(true);
        try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(),
                StandardCharsets.UTF_8)) {
            String srcLang = sourceLang == null ? null : sourceLang.toString();
            writer.write(buildPostData(srcLang, targetLang.toString(), sourceText, translationText,
                    disabledCategories, disabledRules, enabledRules));
            writer.flush();
        }

        checkHttpError(conn);

        String json = "";
        try (InputStream in = conn.getInputStream()) {
            json = IOUtils.toString(in, StandardCharsets.UTF_8);
        }
        ObjectMapper mapper = new ObjectMapper();

        JsonNode response = mapper.readTree(json);
        String apiVersion = response.get("software").get("apiVersion").asText();

        if (!API_VERSION.equals(apiVersion)) {
            Log.logWarningRB("LT_API_VERSION_MISMATCH");
        }

        JsonNode matches = response.get("matches");

        return StreamSupport.stream(matches.spliterator(), true).map(match -> {
            String message = addSuggestionTags(match.get("message").asText());
            int start = match.get("offset").asInt();
            int end = start + match.get("length").asInt();
            JsonNode rule = match.get("rule");
            String ruleId = rule.get("id").asText();
            String ruleDescription = rule.get("description").asText();
            return new LanguageToolResult(message, start, end, ruleId, ruleDescription);
        }).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    protected JsonNode getSupportedLanguages() throws Exception {
        // This is a really stupid way to get the /languages endpoint URL, but
        // it'll do for now.
        String langsUrl = serverUrl.replace(CHECK_PATH, LANGS_PATH);

        URL url = new URL(langsUrl);
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", OStrings.getNameAndVersion());
        conn.setDoOutput(true);

        checkHttpError(conn);

        String json = "";
        try (InputStream in = conn.getInputStream()) {
            json = IOUtils.toString(in, StandardCharsets.UTF_8);
        }

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(json);
    }

    static void checkHttpError(URLConnection conn) throws Exception {
        if (conn instanceof HttpURLConnection) {
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            if (httpConn.getResponseCode() != 200) {
                try (InputStream err = httpConn.getErrorStream()) {
                    String errMsg = IOUtils.toString(err, StandardCharsets.UTF_8);
                    throw new Exception(errMsg);
                }
            }
        }
    }

    /**
     * Replace double quotes with <suggestion></suggestion> tags in error
     * message to imitate native LanguageTool behavior
     */
    static String addSuggestionTags(String str) {
        return str.replaceAll("^([^:]+:\\s?)\"([^']+)\"", "$1<suggestion>$2</suggestion>");
    }

    /**
     * Construct POST request data
     */
    static String buildPostData(String sourceLang, String targetLang, String sourceText, String targetText,
            String disabledCategories, String disabledRules, String enabledRules)
            throws UnsupportedEncodingException {
        String encoding = "UTF-8";
        StringBuilder result = new StringBuilder();
        result.append("text=").append(URLEncoder.encode(targetText, encoding)).append("&language=")
                .append(URLEncoder.encode(targetLang, encoding));
        if (sourceText != null && sourceLang != null) {
            result.append("&srctext=").append(URLEncoder.encode(sourceText, encoding))
                    .append("&motherTongue=").append(URLEncoder.encode(sourceLang, encoding));
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
        if (testUrl.trim().toLowerCase(Locale.ENGLISH).startsWith("https://languagetool.org/api/v2/check")) {
            // Blacklist the official LanguageTool public API specifically
            // because this is what users are most likely to try, but they ask
            // not to send automated requests:
            // http://wiki.languagetool.org/public-http-api
            return false;
        }
        try {
            URL url = new URL(testUrl);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(),
                    StandardCharsets.UTF_8)) {
                // Supply a dummy disabled category to force the server to take
                // its configuration from this query only, not any server-side
                // config.
                writer.write(buildPostData(null, "en-US", null, "Test", "FOO", null, null));
                writer.flush();
            }
            checkHttpError(conn);
            try (InputStream in = conn.getInputStream()) {
                String response = IOUtils.toString(in, StandardCharsets.UTF_8);
                if (response.contains("<?xml")) {
                    Log.logErrorRB("LT_WRONG_FORMAT_RESPONSE");
                    return false;
                } else {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.log(e);
            return false;
        }
    }

    /**
     * Find the best-matching language from the provided options.
     *
     * @param serverLangs
     *            The raw response objects from {@link #getSupportedLanguages()}
     * @param desiredLang
     *            The language to match
     * @return The best-matching language, or null if no languages matched at
     *         all
     */
    @SuppressWarnings("unchecked")
    static Language negotiateLanguage(JsonNode serverLangs, Language desiredLang) {
        // Search for full xx-YY match
        String omLocale = desiredLang.getLanguage();
        for (JsonNode lang : serverLangs) {
            if (omLocale.equalsIgnoreCase(lang.get("longCode").asText())) {
                return desiredLang;
            }
        }

        // Search for just xx match
        String omLang = desiredLang.getLanguageCode();
        for (JsonNode lang : serverLangs) {
            if (omLang.equalsIgnoreCase(lang.get("longCode").asText())) {
                return new Language(desiredLang.getLanguageCode());
            }
        }
        for (JsonNode lang : serverLangs) {
            if (omLang.equalsIgnoreCase(lang.get("code").asText())) {
                return new Language(desiredLang.getLanguageCode());
            }
        }
        return null;
    }
}
