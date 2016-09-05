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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.bitext.BitextRule;
import org.languagetool.tools.Tools;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.util.Log;

public class LanguageToolNativeBridge implements ILanguageToolBridge {

    private ThreadLocal<JLanguageTool> sourceLt;
    private ThreadLocal<JLanguageTool> targetLt;
    private List<BitextRule> bRules;

    public LanguageToolNativeBridge(org.omegat.util.Language sourceLang, org.omegat.util.Language targetLang) {
        Optional<Language> sourceLtLang = getLTLanguage(sourceLang);
        Optional<Language> targetLtLang = getLTLanguage(targetLang);
        sourceLt = ThreadLocal.withInitial(
                () -> sourceLtLang.flatMap(LanguageToolNativeBridge::getLanguageToolInstance).orElse(null));
        targetLt = ThreadLocal.withInitial(
                () -> targetLtLang.flatMap(LanguageToolNativeBridge::getLanguageToolInstance).orElse(null));
        sourceLtLang.ifPresent(s -> targetLtLang.ifPresent(t -> {
            try {
                bRules = Tools.getBitextRules(s, t);
            } catch (Exception e) {
                Log.log(e);
            }
        }));
    }

    @Override
    public void stop() {
        // Nothing to do here
    }

    @Override
    public void applyRuleFilters(String disabledCategories, String disabledRules, String enabledRules) {
        if (targetLt == null) {
            return;
        }
        Set<CategoryId> dc = Stream.of(disabledCategories.split(",")).map(CategoryId::new).collect(Collectors.toSet());
        Set<String> dr = Stream.of(disabledRules.split(",")).collect(Collectors.toSet());
        Set<String> er = Stream.of(enabledRules.split(",")).collect(Collectors.toSet());
        Tools.selectRules(targetLt.get(), dc, Collections.emptySet(), dr, er, false);
        if (bRules != null) {
            bRules = bRules.stream().filter(rule -> !dr.contains(rule.getId())).collect(Collectors.toList());
        }
    }

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText)
            throws Exception {
        return getRuleMatches(sourceText, translationText).stream().map(match -> {
            Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, match.getFromPos(), match.getToPos());
            m.toolTipText = match.getMessage();
            m.painter = LanguageToolWrapper.PAINTER;
            return m;
        }).collect(Collectors.toList());
    }

    List<RuleMatch> getRuleMatches(String sourceText, String translationText) throws Exception {
        JLanguageTool ltTarget = targetLt.get();
        if (ltTarget == null) {
            // LT doesn't know anything about target language
            return Collections.emptyList();
        }
        JLanguageTool ltSource = sourceLt.get();

        if (ltSource != null && bRules != null) {
            // LT knows about source and target languages both and has bitext
            // rules.
            return Tools.checkBitext(sourceText, translationText, ltSource, ltTarget, bRules);
        } else {
            // LT knows about target language only
            return ltTarget.check(translationText);
        }
    }

    public static Optional<Language> getLTLanguage(org.omegat.util.Language lang) {
        String omLang = lang.getLanguageCode();
        return Languages.get().stream().filter(ltLang -> omLang.equalsIgnoreCase(ltLang.getShortName())).findFirst();
    }

    public static Optional<JLanguageTool> getLanguageToolInstance(Language ltLang) {
        try {
            JLanguageTool result = new JLanguageTool(ltLang);
            return Optional.of(result);
        } catch (Exception ex) {
            Log.log(ex);
        }

        return Optional.empty();
    }
}
