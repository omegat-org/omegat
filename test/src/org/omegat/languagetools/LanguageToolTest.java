/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010-2013 Alex Buloichik
               2024 Hiroshi Miura
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.Belarusian;
import org.languagetool.language.CanadianEnglish;
import org.languagetool.language.English;
import org.languagetool.language.French;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.UppercaseSentenceStartRule;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.server.HTTPServer;
import org.languagetool.server.HTTPServerConfig;

import org.omegat.util.Language;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;

/**
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class LanguageToolTest {
    private static final Language SOURCE_LANG = new Language(Locale.FRENCH);
    private static final Language TARGET_LANG = new Language(Locale.ENGLISH);

    @Before
    public final void setUp() throws Exception {
        TestPreferencesInitializer.init();
    }

    @Test
    public void testExecute() throws Exception {
        JLanguageTool lt = new JLanguageTool(new Belarusian());

        // The test string is Belarusian; originally it was actual UTF-8,
        // but that causes the test to fail when environment encodings aren't set
        // correctly, so we are now using Unicode literals.
        List<RuleMatch> matches = lt.check("\u0441\u043F\u0440\u0430\u0443\u0434\u0437\u0456\u043C.");
        assertEquals(3, matches.size());
        assertTrue(matches.get(0).getRule() instanceof MorfologikSpellerRule);
        assertTrue(matches.get(1).getRule() instanceof UppercaseSentenceStartRule);
        assertTrue(matches.get(2).getRule() instanceof PatternRule);
    }

    @Test
    public void testFrench() throws Exception {
        JLanguageTool lt = new JLanguageTool(new French());

        List<RuleMatch> matches = lt.check("Directeur production du groupe");
        assertEquals(1, matches.size());
        assertTrue(matches.get(0).getRule() instanceof PatternRule);
    }

    @Test
    public void testEnglish() throws Exception {
        JLanguageTool lt = new JLanguageTool(new AmericanEnglish());

        List<RuleMatch> matches = lt.check("Check test");
        assertEquals(0, matches.size());
    }

    private static final int[] FREE_PORT_RANGE = {8081, 10080, 10081, 10082, 10083, 10084, 10085, 10086};

    private int getFreePort() {
        for (int p : FREE_PORT_RANGE) {
            try (ServerSocket serverSocket = new ServerSocket(p)) {
                return serverSocket.getLocalPort();
            } catch (IOException ignored) {
                // skip to next candiates
            }
        }
        return -1;
    }

    @Test
    public void testRemoteServer() throws Exception {
        int port = getFreePort();
        assertNotEquals("Port has been already used.", -1, port);
        HTTPServerConfig config = new HTTPServerConfig(port);
        HTTPServer server = new HTTPServer(config);
        try {
            server.run();

            String urlBase = "http://localhost:" + port;

            assertThrows("URL not specifying API actions should fail due to missing argument.",
                    java.lang.Exception.class,
                    () -> new LanguageToolNetworkBridge(SOURCE_LANG, TARGET_LANG, urlBase));

            ILanguageToolBridge bridge = new LanguageToolNetworkBridge(SOURCE_LANG, TARGET_LANG,
                    urlBase + "/v2/check");

            // Set some rules to prevent the server from looking at config files.
            // User config files can specify languages we aren't providing at test
            // runtime, in which case queries will fail.
            bridge.applyRuleFilters(Collections.singleton("FOO"), Collections.emptySet(),
                    Collections.emptySet());

            // We don't care about the actual content of the results as long as
            // there are some: we just want to make sure we are parsing the JSON
            // result correctly.
            List<LanguageToolResult> results = bridge.getCheckResults("foo", "foo bar");
            assertFalse(results.isEmpty());
        } finally {
            server.stop();
        }
    }

    @Test
    public void testNativeBridge() {
        ILanguageToolBridge bridge = new LanguageToolNativeBridge(SOURCE_LANG, TARGET_LANG);

        // We don't care about the actual content of the results as long as
        // there are some: we just want to make sure we are wrapping the result
        // correctly.
        List<LanguageToolResult> results = bridge.getCheckResults("foo", "foo bar");
        assertFalse(results.isEmpty());
    }

    @Test
    public void testWrapperInit() {
        // Defaults: Local implementation
        ILanguageToolBridge bridge = LanguageToolWrapper.createBridgeFromPrefs(SOURCE_LANG, TARGET_LANG);
        assertTrue(bridge instanceof LanguageToolNativeBridge);

        // Bad URL: fall back to local implementation
        Preferences.setPreference(Preferences.LANGUAGETOOL_BRIDGE_TYPE, LanguageToolWrapper.BridgeType.REMOTE_URL);
        Preferences.setPreference(Preferences.LANGUAGETOOL_REMOTE_URL, "blah");
        bridge = LanguageToolWrapper.createBridgeFromPrefs(SOURCE_LANG, TARGET_LANG);
        assertTrue(bridge instanceof LanguageToolNativeBridge);
    }

    @Test
    public void testLanguageMapping() {
        {
            org.languagetool.Language lang = LanguageToolNativeBridge.getLTLanguage(new Language("en-US"));
            assertEquals(AmericanEnglish.class, lang.getClass());
        }
        {
            org.languagetool.Language lang = LanguageToolNativeBridge.getLTLanguage(new Language("en-CA"));
            assertEquals(CanadianEnglish.class, lang.getClass());
        }
        {
            org.languagetool.Language lang = LanguageToolNativeBridge.getLTLanguage(new Language("en"));
            assertEquals(English.class, lang.getClass());
        }
        {
            // Unknown region--fall back to generic class
            org.languagetool.Language lang = LanguageToolNativeBridge.getLTLanguage(new Language("en-JA"));
            assertEquals(English.class, lang.getClass());
        }
        {
            org.languagetool.Language lang = LanguageToolNativeBridge.getLTLanguage(new Language("be-BY"));
            assertEquals(Belarusian.class, lang.getClass());
        }
        {
            // Belarusian is offered in be-BY only; ensure hit with just "be"
            org.languagetool.Language lang = LanguageToolNativeBridge.getLTLanguage(new Language("be"));
            assertEquals(Belarusian.class, lang.getClass());
        }
        {
            org.languagetool.Language lang = LanguageToolNativeBridge.getLTLanguage(new Language("xyz"));
            assertNull(lang);
        }
    }
}
