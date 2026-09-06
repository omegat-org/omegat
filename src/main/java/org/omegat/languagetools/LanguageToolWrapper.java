/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010-2013 Alex Buloichik
               2015 Aaron Madlon-Kay
               2016 Lev Abashkin
               2026 Stephan Pakebusch
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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.text.Highlighter.HighlightPainter;

import org.jetbrains.annotations.Nullable;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.gui.editor.UnderlineFactory;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.gui.issues.IssueProviders;
import org.omegat.util.Language;
import org.omegat.util.Log;
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
public final class LanguageToolWrapper {

    public enum BridgeType {
        NATIVE, REMOTE_URL, LOCAL_INSTALLATION
    }

    private static volatile ILanguageToolBridge bridge;

    private LanguageToolWrapper() {
    }

    public static void init() {

        Core.registerMarker(new LanguageToolMarker());
        IssueProviders.addIssueProvider(new LanguageToolIssueProvider());

        CoreEvents.registerProjectChangeListener(e -> {
            switch (e) {
                case CREATE:
                case LOAD:
                    setBridgeFromCurrentProject();
                    break;
                case CLOSE:
                    if (bridge != null) {
                        bridge.stop();
                    }
                    bridge = null;
                    break;
                default:
                    // Nothing
                }
        });

        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            @Override
            public void onApplicationShutdown() {
                if (bridge != null) {
                    bridge.stop();
                }
            }

            @Override
            public void onApplicationStartup() {
            }
        });
    }

    static ILanguageToolBridge getBridge() {
        return bridge;
    }

    /**
     * Drop matches that lie entirely inside an occurrence of one of the
     * entry's protected parts in the translation text. Rules routinely
     * misread placeholder syntax (for example a currency rule firing on the
     * "3$" inside the placeholder "%3$@"), and protected parts are not
     * prose the translator can fix. Matches reaching beyond a single
     * occurrence are kept on purpose, even ones spanning two adjacent
     * placeholders: they may point at a real problem around them. Protected
     * part texts are assumed to match the checked text character-wise, as
     * everywhere else in OmegaT.
     *
     * @param matches
     *            check results whose offsets refer to translationText
     * @param ste
     *            source entry providing the protected parts; may be null
     *            (e.g. when the marker is invoked without an entry, as in
     *            tests), in which case nothing is filtered
     * @param translationText
     *            the text that was checked
     * @return the matches without those inside protected parts
     */
    static List<LanguageToolResult> filterProtectedParts(List<LanguageToolResult> matches,
            @Nullable SourceTextEntry ste, String translationText) {
        if (matches.isEmpty() || ste == null || ste.getProtectedParts().length == 0) {
            return matches;
        }
        List<int[]> occurrences = ProtectedPart.occurrencesIn(translationText, ste.getProtectedParts());
        if (occurrences.isEmpty()) {
            return matches;
        }
        return matches.stream()
                .filter(m -> occurrences.stream().noneMatch(o -> m.start >= o[0] && m.end <= o[1]))
                .collect(Collectors.toList());
    }

    static class LanguageToolMarker implements IMarker {
        static final HighlightPainter PAINTER = new UnderlineFactory.WaveUnderline(
                Styles.EditorColor.COLOR_LANGUAGE_TOOLS.getColor());

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

            List<LanguageToolResult> results = filterProtectedParts(
                    bridge.getCheckResults(sourceText, translationText), ste, translationText);
            return results.stream().map(match -> {
                Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, match.start, match.end);
                m.toolTipText = match.message;
                m.painter = PAINTER;
                return m;
            }).collect(Collectors.toList());
        }

        protected boolean isEnabled() {
            return Core.getEditor().getSettings().isMarkLanguageChecker();
        }
    }

    /**
     * Set this instance's LanguageTool bridge based on the current project.
     */
    public static void setBridgeFromCurrentProject() {
        if (bridge != null) {
            bridge.stop();
        }
        if (Core.getProject().isProjectLoaded()) {
            Language sourceLang = Core.getProject().getProjectProperties().getSourceLanguage();
            Language targetLang = Core.getProject().getProjectProperties().getTargetLanguage();
            bridge = createBridgeFromPrefs(sourceLang, targetLang);
        }
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
        BridgeType type = LanguageToolPrefs.getBridgeType();
        try {
            switch (type) {
            case LOCAL_INSTALLATION:
                String localServerJarPath = LanguageToolPrefs.getLocalServerJarPath();
                String languageModelPath = LanguageToolPrefs.getLanguageModelPath();
                if (languageModelPath == null) {
                    languageModelPath = LanguageToolPrefs.getLanguageModelDefaultPath();
                }
                int port = getFreePort();
                bridge = new LanguageToolNetworkBridge(sourceLang, targetLang, localServerJarPath, port,
                        languageModelPath);
                break;
            case REMOTE_URL:
                String remoteUrl = LanguageToolPrefs.getRemoteUrl();
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

        LanguageToolPrefs.applyRules(bridge, targetLang.getLanguageCode());
        return bridge;
    }

    private static final int[] FREE_PORT_RANGE = {8081, 10080, 10081, 10082, 10083, 10084, 10085, 10086};

    private static int getFreePort() {
        for (int p : FREE_PORT_RANGE) {
            try (ServerSocket serverSocket = new ServerSocket(p)) {
                return serverSocket.getLocalPort();
            } catch (IOException ignored) {
            }
        }
        return -1;
    }

}
