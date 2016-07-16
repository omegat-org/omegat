/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010-2013 Alex Buloichik
               2015 Aaron Madlon-Kay
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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Highlighter.HighlightPainter;

import net.arnx.jsonic.JSON;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.bitext.BitextRule;
import org.languagetool.rules.bitext.DifferentLengthRule;
import org.languagetool.rules.bitext.DifferentPunctuationRule;
import org.languagetool.rules.bitext.SameTranslationRule;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tools.Tools;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.editor.UnderlineFactory;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.util.FileUtil;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.Styles;

/**
 * Marker implementation for LanguageTool support.
 *
 * Bilingual check described <a href=
 * "http://languagetool.wikidot.com/checking-translations-bilingual-texts">here
 * </a>
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 * @author Lev Abashkin
 */
public class LanguageToolWrapper implements IMarker, IProjectEventListener, IApplicationEventListener {
    protected static final HighlightPainter PAINTER = new UnderlineFactory.WaveUnderline(Styles.EditorColor.COLOR_LANGUAGE_TOOLS.getColor());

    /* Constants */
    private final static int DEFAULT_LOCAL_PORT = 8081;
    private final static String URL_PATH = "/v2/check";
    private final static String SERVER_JAR_NAME = "languagetool-server.jar";
    private final static String SERVER_CLASS_NAME = "org.languagetool.server.HTTPServer";

    /* Properties of built-in LT */
    private JLanguageTool sourceLt, targetLt;
    private List<BitextRule> bRules;

    /* Properties of external LT */
    private Process server;
    private String sourceLang, targetLang, serverUrl, ltPath;
    private int serverPort;
    private boolean spawnServer, ruleDiffPunctuation;
    private Boolean useBuiltIn = null;

    public LanguageToolWrapper() throws Exception {
        this(null);
    }

    public LanguageToolWrapper(Boolean useBuiltIn) throws Exception {
        this.useBuiltIn = useBuiltIn;
        CoreEvents.registerProjectChangeListener(this);
        CoreEvents.registerApplicationEventListener(this);
        loadPreferences();

    }

    public synchronized void onApplicationShutdown(){
        // Terminate spawned server on application shutdown without closing a project before.
        terminateLanguageToolServer();
    }

    public synchronized void onApplicationStartup(){}

    public boolean isEnabled() {
        return Core.getEditor().getSettings().isMarkLanguageChecker();
    }

    public synchronized void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
        switch (eventType) {
        case CREATE:
        case LOAD:
            if (useBuiltIn) {
                Language sl = getLTLanguage(Core.getProject().getProjectProperties().getSourceLanguage());
                Language tl = getLTLanguage(Core.getProject().getProjectProperties().getTargetLanguage());
                sourceLt = getLanguageToolInstance(sl);
                targetLt = getLanguageToolInstance(tl);
                if (sourceLt != null && targetLt != null) {
                    bRules = getBiTextRules(sl, tl);
                }
            } else {
                sourceLang = Core.getProject().getProjectProperties().getSourceLanguage().toString();
                targetLang = Core.getProject().getProjectProperties().getTargetLanguage().toString();
                spawnLanguageToolServer();
            }
            break;
        case CLOSE:
            sourceLt = null;
            targetLt = null;
            terminateLanguageToolServer();
            break;
        default:
            // Nothing
        }
    }

    private JLanguageTool getLanguageToolInstance(Language ltLang) {
        if (ltLang == null) {
            return null;
        }
        JLanguageTool result = null;

        try {
            result = new JLanguageTool(ltLang);
            result.getAllRules().stream().filter(rule -> rule instanceof SpellingCheckRule).map(Rule::getId)
                    .forEach(result::disableRule);
        } catch (Exception ex) {
            Log.log(ex);
        }

        return result;
    }

    @Override
    public synchronized List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText,
            boolean isActive) throws Exception {

        if (translationText == null || !isEnabled()) {
            // Return when disabled or translation text is empty
            return null;
        }

        if (!useBuiltIn && server == null && spawnServer) {
            // Return when external LT is misconfigured or not available
            return null;
        }

        if (useBuiltIn && targetLt == null) {
            // LT doesn't know anything about target language
            return null;
        }

        // LanguageTool claims to expect text in NFKC, but that actually causes problems for some rules:
        // https://github.com/languagetool-org/languagetool/issues/379
        // From the discussion it seems the intent behind NFKC was to break up multi-letter codepoints such as
        // U+FB00 LATIN SMALL LIGATURE FF. These are unlikely to be found in user input in our case, so
        // instead we will use NFC. We already normalize our source to NFC when loading, so we only need to
        // handle the translation here:
        translationText = StringUtil.normalizeUnicode(translationText);

        if (useBuiltIn) {
            return getMarksForEntryBuiltIn(ste, sourceText, translationText);
        } else {
            return getMarksForEntryExternal(sourceText, translationText);
        }


    }

    /**
     * Get marks for build-in path
     */
    private List<Mark> getMarksForEntryBuiltIn(SourceTextEntry ste, String sourceText, String translationText)
            throws Exception {
        List<Mark> r = new ArrayList<>();
        List<RuleMatch> matches;
        if (sourceLt != null && bRules != null) {
            // LT knows about source and target languages both and has bitext rules.

            // sourceText represents the displayed source text: it may be null (not displayed) or have extra
            // bidi characters for display. Since we need it for linguistic comparison here, if it's null then
            // we pull from the SourceTextEntry, which is guaranteed not to be null.
            matches = Tools.checkBitext(sourceText == null ? ste.getSrcText() : sourceText, translationText,
                    sourceLt, targetLt, bRules);
        } else {
            // LT knows about target language only
            matches = targetLt.check(translationText);
        }

        for (RuleMatch match : matches) {
            Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, match.getFromPos(), match.getToPos());
            m.toolTipText = match.getMessage();
            m.painter = PAINTER;
            r.add(m);
        }

        return r;
    }

    /**
     * Get marks for external path
     */
    private List<Mark> getMarksForEntryExternal(String sourceText, String translationText)
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

    private Language getLTLanguage(org.omegat.util.Language lang) {
        String omLang = lang.getLanguageCode();
        for (Language ltLang : Languages.get()) {
            if (omLang.equalsIgnoreCase(ltLang.getShortName())) {
                return ltLang;
            }
        }
        return null;
    }

    /**
     * Retrieve bitext rules for specified languages, but remove some rules, which not required in OmegaT
     */
    private List<BitextRule> getBiTextRules(Language sourceLang, Language targetLang) {
        List<BitextRule> result;
        try {
            result = Tools.getBitextRules(sourceLang, targetLang);
        } catch (Exception ex) {
            // bitext rules can be not defined
            return null;
        }
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof DifferentLengthRule) {
                result.remove(i--);
                continue;
            }
            if (result.get(i) instanceof SameTranslationRule) {
                result.remove(i--);
                continue;
            }
            if (result.get(i) instanceof DifferentPunctuationRule && !ruleDiffPunctuation) {
                result.remove(i--);
            }
        }
        return result;
    }

    /**
     * Load and initialize preferences
     */
    private void loadPreferences() {
        if (useBuiltIn == null) {
            useBuiltIn = Preferences.isPreferenceDefault(Preferences.LANGUAGETOOL_USE_BUILDIN, true);
        }
        spawnServer = Preferences.isPreferenceDefault(Preferences.LANGUAGETOOL_SPAWN_SERVER, true);
        serverUrl = Preferences.getPreferenceDefault(Preferences.LANGUAGETOOL_EXTERNAL_SERVER_URL,"");
        serverPort = Preferences.getPreferenceDefault(Preferences.LANGUAGETOOL_LOCAL_PORT, DEFAULT_LOCAL_PORT);
        ltPath = Preferences.getPreferenceDefault(Preferences.LANGUAGETOOL_INSTALL_DIR, "");
        ruleDiffPunctuation = Preferences.isPreferenceDefault(Preferences.LANGUAGETOOL_BITEXT_RULE_DIFF_PUNCTUATION, false);

        if (useBuiltIn) return;

        if (!spawnServer) {
            try {
                URL url = new URL(serverUrl);
                // URL is valid, nothing to do
                return;
            } catch (MalformedURLException e) {
                spawnServer = true;
            }
        }

        if (FileUtil.isRelative(ltPath)) {
            ltPath = Paths.get(StaticUtils.installDir(), ltPath).toString();
        }

        ltPath = Paths.get(ltPath, SERVER_JAR_NAME).toString();
        serverUrl = "http://localhost:" + Integer.toString(serverPort) + URL_PATH;
    }

    /**
     * Spawn LanguageTool server instanse
     */
    private void spawnLanguageToolServer() {
        if (!useBuiltIn && spawnServer) {
            try {
                ProcessBuilder pb = new ProcessBuilder("java",
                        "-cp",
                        ltPath,
                        SERVER_CLASS_NAME,
                        "--port",
                        Integer.toString(serverPort));
                pb.inheritIO();
                server = pb.start();
                Log.log(OStrings.getString("LT_SERVER_STARTED"));
            } catch (IOException ex) {
                Log.log(ex);
                server = null;
            }
        } else {
            server = null;
        }
    }

    /**
     * Terminate LanguageTool server instance
     */
    private void terminateLanguageToolServer() {
        if (server != null) {
            try {
                server.destroy();
                Log.log(OStrings.getString("LT_SERVER_TERMINATED"));
            } catch (Exception ex) {
                Log.log(ex);
            }
        }
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
        if (!ruleDiffPunctuation) result += URLEncoder.encode(",DIFFERENT_PUNCTUATION", encoding);
        return result;
    }
}
