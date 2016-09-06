/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010-2013 Alex Buloichik
               2015 Aaron Madlon-Kay
               2016 Lev Abashkin
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

import java.util.List;

import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.gui.editor.UnderlineFactory;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.Preferences;
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
public class LanguageToolWrapper {
    static final HighlightPainter PAINTER = new UnderlineFactory.WaveUnderline(
            Styles.EditorColor.COLOR_LANGUAGE_TOOLS.getColor());

    private final static String DEFAULT_DISABLED_CATEGORIES = "SPELL,TYPOS";
    private final static String DEFAULT_DISABLED_RULES = "SAME_TRANSLATION,TRANSLATION_LENGTH,DIFFERENT_PUNCTUATION";

    public enum BridgeType {
        NATIVE, REMOTE_URL, LOCAL_INSTALLATION
    }

    private static volatile ILanguageToolBridge BRIDGE;

    private LanguageToolWrapper() {
    }

    public static void init() {

        Core.registerMarker(new LanguageToolMarker());
        
        CoreEvents.registerProjectChangeListener(e -> {
            switch (e) {
                case CREATE:
                case LOAD:
                    setBridgeFromCurrentProject();
                    break;
                case CLOSE:
                    BRIDGE.stop();
                    BRIDGE = null;
                    break;
                default:
                    // Nothing
                }
        });

        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            @Override
            public synchronized void onApplicationShutdown() {
                if (BRIDGE != null) {
                    BRIDGE.stop();
                }
            }

            @Override
            public synchronized void onApplicationStartup() {
            }
        });

        Preferences.addPropertyChangeListener(evt -> {
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            // This property is changed in the end of configuration dialog
            // saving, so at this point every other related properties are
            // already changed.
            if (evt.getPropertyName().equals(Preferences.LANGUAGETOOL_PREFS_CHANGED_AT)) {
                setBridgeFromCurrentProject();
            }
        });
    }

    static class LanguageToolMarker implements IMarker {
        @Override
        public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText,
                boolean isActive) throws Exception {

            if (translationText == null || !isEnabled()) {
                // Return when disabled or translation text is empty
                return null;
            }

            // LanguageTool claims to expect text in NFKC, but that actually
            // causes problems for some rules:
            // https://github.com/languagetool-org/languagetool/issues/379
            // From the discussion it seems the intent behind NFKC was to break
            // up multi-letter codepoints such as U+FB00 LATIN SMALL LIGATURE
            // FF. These are unlikely to be found in user input in our case, so
            // instead we will use NFC. We already normalize our source to NFC
            // when loading, so we only need to handle the translation here:
            translationText = StringUtil.normalizeUnicode(translationText);

            // sourceText represents the displayed source text: it may be null
            // (not displayed) or have extra bidi characters for display. Since
            // we need it for linguistic comparison here, if it's null then we
            // pull from the SourceTextEntry, which is guaranteed not to be
            // null. It doesn't need to be normalized because OmegaT normalizes
            // all source text to NFC on load.
            if (sourceText == null) {
                sourceText = ste.getSrcText();
            }

            return BRIDGE.getMarksForEntry(ste, sourceText, translationText);
        }

        protected boolean isEnabled() {
            return Core.getEditor().getSettings().isMarkLanguageChecker();
        }
    }

    /**
     * Set this instance's LanguageTool bridge based on the current project.
     */
    static void setBridgeFromCurrentProject() {
        if (BRIDGE != null) {
            BRIDGE.stop();
        }
        Language sourceLang = Core.getProject().getProjectProperties().getSourceLanguage();
        Language targetLang = Core.getProject().getProjectProperties().getTargetLanguage();
        BRIDGE = createBridgeFromPrefs(sourceLang, targetLang);
    }

    /**
     * Create LanguageTool bridge based on user preferences. Falls back to
     * {@link BridgeType#NATIVE} if non-native bridges fail to initialize (bad
     * config, etc.).
     */
    static ILanguageToolBridge createBridgeFromPrefs(Language sourceLang, Language targetLang) {
        // If configured try to create network bridge and fallback to native on
        // fail
        ILanguageToolBridge bridge;
        BridgeType type = Preferences.getPreferenceEnumDefault(Preferences.LANGUAGETOOL_BRIDGE_TYPE,
                BridgeType.NATIVE);
        try {
            switch (type) {
            case LOCAL_INSTALLATION:
                String localServerJarPath = Preferences.getPreference(Preferences.LANGUAGETOOL_LOCAL_SERVER_JAR_PATH);
                bridge = new LanguageToolNetworkBridge(sourceLang, targetLang, localServerJarPath, 8081);
                break;
            case REMOTE_URL:
                String remoteUrl = Preferences.getPreference(Preferences.LANGUAGETOOL_REMOTE_URL);
                bridge = new LanguageToolNetworkBridge(sourceLang, targetLang, remoteUrl);
                break;
            case NATIVE:
            default:
                bridge = new LanguageToolNativeBridge(sourceLang, targetLang);
            }
        } catch (Exception e) {
            Log.logWarningRB("LT_BAD_CONFIGURATION");
            bridge = new LanguageToolNativeBridge(sourceLang, targetLang);
        }
        String disabledCategories = Preferences.getPreferenceDefault(
                Preferences.LANGUAGETOOL_DISABLED_CATEGORIES, DEFAULT_DISABLED_CATEGORIES);

        String lc = targetLang.getLanguageCode();
        String disabledRules = Preferences.getPreferenceDefault(
                Preferences.LANGUAGETOOL_DISABLED_RULES_PREFIX + "_" + lc, DEFAULT_DISABLED_RULES);
        String enabledRules = Preferences.getPreferenceDefault(
                Preferences.LANGUAGETOOL_ENABLED_RULES_PREFIX + "_" + lc, "");

        bridge.applyRuleFilters(disabledCategories, disabledRules, enabledRules);
        return bridge;
    }
}
