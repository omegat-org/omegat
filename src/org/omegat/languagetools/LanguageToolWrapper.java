/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010-2013 Alex Buloichik
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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.text.Highlighter.HighlightPainter;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.bitext.BitextRule;
import org.languagetool.rules.bitext.DifferentLengthRule;
import org.languagetool.rules.bitext.SameTranslationRule;
import org.languagetool.tools.Tools;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.editor.UnderlineFactory;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * Marker implementation for LanguageTool support.
 * 
 * Bilingual check described on http://languagetool.wikidot.com/checking-translations-bilingual-texts
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class LanguageToolWrapper implements IMarker, IProjectEventListener {
    protected static final HighlightPainter PAINTER = new UnderlineFactory.WaveUnderline(Color.BLUE);

    private JLanguageTool sourceLt, targetLt;
    private List<BitextRule> bRules;

    private JCheckBoxMenuItem menuItem;

    protected boolean disabled = true;

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerMarkerClass(LanguageToolWrapper.class);
    }

    public static void unloadPlugins() {
    }

    public LanguageToolWrapper() throws Exception {
        disabled = Preferences.isPreferenceDefault(Preferences.LT_DISABLED, true);

        menuItem = new JCheckBoxMenuItem(OStrings.getString("LT_OPTIONS_MENU_ENABLED"));
        menuItem.addActionListener(menuItemActionListener);
        menuItem.setSelected(!disabled);

        Core.getMainWindow().getMainMenu().getOptionsMenu().add(menuItem);

        CoreEvents.registerProjectChangeListener(this);
    }

    public synchronized void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
        switch (eventType) {
        case CREATE:
        case LOAD:
            Language sourceLang = getLTLanguage(Core.getProject().getProjectProperties().getSourceLanguage());
            Language targetLang = getLTLanguage(Core.getProject().getProjectProperties().getTargetLanguage());
            sourceLt = getLanguageToolInstance(sourceLang);
            targetLt = getLanguageToolInstance(targetLang);
            if (sourceLt != null && targetLt != null) {
                bRules = getBiTextRules(sourceLang, targetLang);
            }
            break;
        case CLOSE:
            sourceLt = null;
            targetLt = null;
            break;
        }
    }

    protected JLanguageTool getLanguageToolInstance(Language ltLang) {
        JLanguageTool result = null;

        if (ltLang != null) {
            try {
                result = new JLanguageTool(ltLang);
                result.activateDefaultPatternRules();
            } catch (Exception ex) {
                result = null;
                Log.log(ex);
            }
        }

        return result;
    }

    @Override
    public synchronized List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText, boolean isActive)
            throws Exception {
        if (translationText == null || disabled) {
            return null;
        }

        JLanguageTool ltSource = sourceLt;
        JLanguageTool ltTarget = targetLt;
        if (ltTarget == null) {
            // LT doesn't know anything about source language
            return null;
        }

        List<Mark> r = new ArrayList<Mark>();
        List<RuleMatch> matches;
        if (ltSource != null && bRules != null) {
            // LT knows about source and target languages both and has bitext rules
            matches = Tools.checkBitext(sourceText, translationText, ltSource, ltTarget, bRules);
        } else {
            // LT knows about target language only
            matches = ltTarget.check(translationText);
        }

        for (RuleMatch match : matches) {
            Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, match.getFromPos(), match.getToPos());
            m.toolTipText = match.getMessage();
            m.painter = PAINTER;
            r.add(m);
        }

        return r;
    }

    private Language getLTLanguage(org.omegat.util.Language lang) {
        String omLang = lang.getLanguageCode();
        for (Language ltLang : Language.LANGUAGES) {
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
                result.remove(i);
                i--;
                continue;
            }
            if (result.get(i) instanceof SameTranslationRule) {
                result.remove(i);
                i--;
                continue;
            }
        }
        return result;
    }

    protected ActionListener menuItemActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            disabled = !menuItem.isSelected();
            Preferences.setPreference(Preferences.LT_DISABLED, disabled);
            Core.getEditor().remarkOneMarker(LanguageToolWrapper.class.getName());
        }
    };
}
