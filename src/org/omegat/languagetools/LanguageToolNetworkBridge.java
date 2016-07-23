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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.arnx.jsonic.JSON;
import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.mark.Mark;
import static org.omegat.languagetools.LanguageToolWrapper.PAINTER;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;
import org.omegat.util.StaticUtils;


public class LanguageToolNetworkBridge extends LanguageToolAbstractBridge {

    /* Constants */
    private final static String URL_PATH = "/v2/check";
    private final static String SERVER_JAR_NAME = "languagetool-server.jar";
    private final static String SERVER_CLASS_NAME = "org.languagetool.server.HTTPServer";

    /* Instance scope fields */

    private Process server;
    private int localPort;
    private String serverUrl;

    /* Project scope fields */
    private String sourceLang, targetLang;

    /**
     * Get instance talking to remote server
     * @param url URL of remote LanguageTool server
     * @return new LanguageToolNetworkBridge instance
     * @throws java.lang.Exception
     */
    public static LanguageToolNetworkBridge getRemoteInstance(String url) throws Exception {
        return new LanguageToolNetworkBridge(false, url, null, 0, false);
    }

    /**
     * Get instance spawning and talking to local server
     * @param path local LanguageTool directory
     * @param port local port for spawned server to listen
     * @return new LanguageToolNetworkBridge instance
     * @throws java.lang.Exception
     */
    public static LanguageToolNetworkBridge getLocalInstance(String path, int port) throws Exception {
        return new LanguageToolNetworkBridge(true, null, getClassPath(path, false), port, false);
    }

    /**
     * Get test instance. Server started from bundled libs.
     * @return new LanguageToolNetworkBridge instance
     * @throws java.lang.Exception
     */
    public static LanguageToolNetworkBridge getLocalTestInstance() throws Exception {
        return new LanguageToolNetworkBridge(true, null, getClassPath(null, true), 8081, true);
    }


    private LanguageToolNetworkBridge (boolean doSpawn, String url, String classPath, int port, boolean test) throws Exception {
        // Remember port
        localPort = port;

        if (doSpawn) {
            if (!test) {
                // Check if ClassPath points to a real file
                if (!new File(classPath).exists()) {
                    Log.logWarningRB("LT_BAD_LOCAL_PATH");
                    throw new Exception();
                }

                // Check if socket is available
                try {
                    (new ServerSocket(localPort)).close();
                }
                catch (Exception e) {
                    Log.logWarningRB("LT_BAD_SOCKET");
                    throw new Exception();
                }
            }
            // Run the server
            ProcessBuilder pb = new ProcessBuilder("java",
                    "-cp",
                    classPath,
                    SERVER_CLASS_NAME,
                    "--port",
                    Integer.toString(localPort));
            pb.inheritIO();

            server = pb.start();

            // Wait for server to start
            int timeout = 10000;
            int timeWaiting = 0;
            int interval = 10;
            while (true) {
                Thread.sleep(interval);
                timeWaiting += interval;
                try {
                    (new Socket("localhost", localPort)).close();
                    break;
                }
                catch (Exception e) {}
                if (timeWaiting >= timeout) {
                    Log.logWarningRB("LT_SERVER_START_TIMEOUT");
                    server.destroy();
                    if (!test) throw new Exception();
                }
            }

            serverUrl = "http://localhost:" + Integer.toString(localPort) + URL_PATH;
            Log.log(OStrings.getString("LT_SERVER_STARTED"));
        }
        else {
            // Try to connect URL
            if (!testServer(url)) {
                Log.logWarningRB("LT_BAD_URL");
                throw new Exception();
            }
            // OK, URL seems valid, let's use it.
            serverUrl = url;
        }
    }

    @Override
    public void onProjectLoad() {
        sourceLang = Core.getProject().getProjectProperties().getSourceLanguage().toString();
        targetLang = Core.getProject().getProjectProperties().getTargetLanguage().toString();
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
                    }
                    catch (Exception e) {
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
        writer.write(buildPostData(sourceText, translationText));
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
            m.painter = PAINTER;
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
    private String buildPostData(String sourceText, String targetText) throws UnsupportedEncodingException {
        String encoding = "UTF-8";
        String result = "text=" + URLEncoder.encode(targetText, encoding) +
                "&language=" + URLEncoder.encode(targetLang, encoding);
        if (sourceText != null) {
            result += "&srctext=" + URLEncoder.encode(sourceText, encoding) +
                    "&motherTongue=" + URLEncoder.encode(sourceLang, encoding);
        }
        // Exclude spelling rules
        result += "&disabledCategories=TYPOS";
        // Exclude bitext rules
        result += "&disabledRules=" + URLEncoder.encode("SAME_TRANSLATION,TRANSLATION_LENGTH", encoding);
        if (!useDifferentPunctuationRule) result += URLEncoder.encode(",DIFFERENT_PUNCTUATION", encoding);
        return result;
    }

    /**
     * Try to talk with LT server and return result
     */
    private boolean testServer(String testUrl) {
        boolean result = false;
        String targetLangBackup = targetLang;
        targetLang = "en-US";
        try {
            URL url = new URL(testUrl);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                writer.write(buildPostData(null, "Test"));
                writer.flush();
            }
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            result = headerFields.get(null).toString().indexOf("200") > 0;
        }
        catch (Exception e) {
            // Do nothing
        }
        targetLang = targetLangBackup;
        return result;
    }

    /**
     * Get ClassPath for local server
     */
    private static String getClassPath(String dir, boolean test) {
        if (test) {
            return "lib/auto/languagetool-server-*" + (Platform.isWindows() ? ";" : ":") + "lib/auto/*";
        } else {
            return Paths.get(dir, SERVER_JAR_NAME).toString();
        }
    }
}
