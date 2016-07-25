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
    protected static final HighlightPainter PAINTER = new UnderlineFactory.WaveUnderline(Styles.EditorColor.COLOR_LANGUAGE_TOOLS.getColor());


    // Preferences cache
    private boolean useNative, spawnServer;
    private String remoteUrl, localDir;
    private int port;

    public enum BRIDGE_TYPE {CONFIGURED, TEST_NATIVE, TEST_NETWORK};
    private ILanguageToolBridge bridge;

    public LanguageToolWrapper() throws Exception {
        this(BRIDGE_TYPE.CONFIGURED);
    }

    public LanguageToolWrapper(BRIDGE_TYPE type) throws Exception {
        CoreEvents.registerProjectChangeListener(this);
        CoreEvents.registerApplicationEventListener(this);

        switch (type) {
            case CONFIGURED:
                // Cache preferences…
                useNative = Preferences.isPreferenceDefault(Preferences.LANGUAGETOOL_USE_NATIVE, true);
                spawnServer = Preferences.isPreferenceDefault(Preferences.LANGUAGETOOL_SPAWN_SERVER, true);
                remoteUrl = Preferences.getPreferenceDefault(Preferences.LANGUAGETOOL_REMOTE_URL,"");
                port = Preferences.getPreferenceDefault(Preferences.LANGUAGETOOL_LOCAL_PORT, 8081);
                localDir = Preferences.getPreferenceDefault(Preferences.LANGUAGETOOL_LOCAL_DIR, "");
                // … and create bridge based on them
                createBridge();
                LanguageToolAbstractBridge.setDifferentPunctuationRule(Preferences.isPreferenceDefault(Preferences.LANGUAGETOOL_USE_DIFF_PUNCTUATION_RULE, false));
                break;
            case TEST_NATIVE:
                this.bridge = new LanguageToolNativeBridge();
                break;
            case TEST_NETWORK:
                this.bridge = LanguageToolNetworkBridge.getLocalTestInstance();
                break;
            default:
                // Nothing
        }

    }

    /**
     * Create and assign LanguageTool bridge
     */
    private void createBridge() {
        // If configured try to create network bridge and fallback to native on fail
        if (!useNative) {
            try {
                if (spawnServer) bridge = LanguageToolNetworkBridge.getLocalInstance(localDir, port);
                else bridge = LanguageToolNetworkBridge.getRemoteInstance(remoteUrl);
            }
            catch (Exception e) {
                Log.logWarningRB("LT_BAD_CONFIGURATION");
                useNative = true;
            }
        }

        if (useNative) bridge = new LanguageToolNativeBridge();
    }

    @Override
    public synchronized void onApplicationShutdown(){
        bridge.destroy();
    }

    @Override
    public synchronized void onApplicationStartup(){}

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
