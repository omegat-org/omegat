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

import java.util.ArrayList;
import java.util.List;
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
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.mark.Mark;
import static org.omegat.languagetools.LanguageToolWrapper.PAINTER;
import org.omegat.util.Log;

public class LanguageToolNativeBridge extends LanguageToolAbstractBridge {

    private JLanguageTool sourceLt, targetLt;
    private List<BitextRule> bRules;

    @Override
    public void onProjectLoad() {
        Language sl = getLTLanguage(Core.getProject().getProjectProperties().getSourceLanguage());
        Language tl = getLTLanguage(Core.getProject().getProjectProperties().getTargetLanguage());
        sourceLt = getLanguageToolInstance(sl);
        targetLt = getLanguageToolInstance(tl);
        if (sourceLt != null && targetLt != null) {
            bRules = getBiTextRules(sl, tl);
        }
    }

    @Override
    public void onProjectClose() {
        sourceLt = null;
        targetLt = null;
    }

    @Override
    public void destroy() {}

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText)
            throws Exception {

        if (targetLt == null) {
            // LT doesn't know anything about target language
            return null;
        }

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
            if (result.get(i) instanceof DifferentPunctuationRule && !useDifferentPunctuationRule) {
                result.remove(i--);
            }
        }
        return result;
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
}
