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
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.editor.UnderlineFactory;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;
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
public class LanguageToolWrapper implements IMarker, IProjectEventListener, IApplicationEventListener {
    static final HighlightPainter PAINTER = new UnderlineFactory.WaveUnderline(
            Styles.EditorColor.COLOR_LANGUAGE_TOOLS.getColor());

    public enum BridgeType {
        NATIVE, REMOTE_URL, LOCAL_INSTALLATION
    }

    private ILanguageToolBridge bridge;

    public LanguageToolWrapper() throws Exception {
        CoreEvents.registerProjectChangeListener(this);
        CoreEvents.registerApplicationEventListener(this);

        bridge = createBridgeFromPrefs();
    }

    /**
     * Create LanguageTool bridge based on user preferences. Falls back to
     * {@link BridgeType#NATIVE} if non-native bridges fail to initialize (bad
     * config, etc.).
     */
    static ILanguageToolBridge createBridgeFromPrefs() {
        // If configured try to create network bridge and fallback to native on
        // fail
        BridgeType type = Preferences.getPreferenceEnumDefault(Preferences.LANGUAGETOOL_BRIDGE_TYPE,
                BridgeType.NATIVE);
        try {
            switch (type) {
            case LOCAL_INSTALLATION:
                String localDir = Preferences.getPreference(Preferences.LANGUAGETOOL_LOCAL_DIR);
                return new LanguageToolNetworkBridge(localDir, 8081);
            case REMOTE_URL:
                String remoteUrl = Preferences.getPreference(Preferences.LANGUAGETOOL_REMOTE_URL);
                return new LanguageToolNetworkBridge(remoteUrl);
            case NATIVE:
            default:
                return new LanguageToolNativeBridge();
            }
        } catch (Exception e) {
            Log.logWarningRB("LT_BAD_CONFIGURATION");
            return new LanguageToolNativeBridge();
        }
    }

    @Override
    public synchronized void onApplicationShutdown() {
        bridge.destroy();
    }

    @Override
    public synchronized void onApplicationStartup() {
    }

    public boolean isEnabled() {
        return Core.getEditor().getSettings().isMarkLanguageChecker();
    }

    @Override
    public synchronized void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
        switch (eventType) {
        case CREATE:
        case LOAD:
            bridge.onProjectLoad();
            break;
        case CLOSE:
            bridge.onProjectClose();
            break;
        default:
            // Nothing
        }
    }

    @Override
    public synchronized List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText,
            boolean isActive) throws Exception {

        if (translationText == null || !isEnabled()) {
            // Return when disabled or translation text is empty
            return null;
        }

        // LanguageTool claims to expect text in NFKC, but that actually causes problems for some rules:
        // https://github.com/languagetool-org/languagetool/issues/379
        // From the discussion it seems the intent behind NFKC was to break up multi-letter codepoints such as
        // U+FB00 LATIN SMALL LIGATURE FF. These are unlikely to be found in user input in our case, so
        // instead we will use NFC. We already normalize our source to NFC when loading, so we only need to
        // handle the translation here:
        translationText = StringUtil.normalizeUnicode(translationText);

        return bridge.getMarksForEntry(ste, sourceText, translationText);
    }
}
