/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
 with fuzzy matching, translation memory, keyword search,
 glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010-2013 Alex Buloichik
               2015 Aaron Madlon-Kay
               2016 Lev Abashkin
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.languagetools;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.bitext.BitextRule;
import org.languagetool.tools.Tools;
import org.omegat.util.Log;

public class LanguageToolNativeBridge extends BaseLanguageToolBridge {

    private final Language sourceLtLang;
    private final Language targetLtLang;
    private ThreadLocal<JLanguageTool> sourceLt;
    private ThreadLocal<JLanguageTool> targetLt;
    private List<BitextRule> bRules;

    public LanguageToolNativeBridge(org.omegat.util.Language sourceLang, org.omegat.util.Language targetLang) {
        sourceLtLang = getLTLanguage(sourceLang);
        targetLtLang = getLTLanguage(targetLang);
        Log.log("Selected LanguageTool source language: "
                + (sourceLtLang == null ? null : sourceLtLang.getShortNameWithCountryAndVariant()));
        Log.log("Selected LanguageTool target language: "
                + (targetLtLang == null ? null : targetLtLang.getShortNameWithCountryAndVariant()));
        sourceLt = ThreadLocal.withInitial(
                () -> sourceLtLang == null ? null : new JLanguageTool(sourceLtLang));
        targetLt = ThreadLocal.withInitial(
                () -> targetLtLang == null ? null : new JLanguageTool(targetLtLang));
        if (sourceLtLang != null && targetLtLang != null) {
            try {
                bRules = Tools.getBitextRules(sourceLtLang, targetLtLang);
            } catch (Exception e) {
                Log.log(e);
            }
        }
    }

    @Override
    public void stop() {
        // Nothing to do here
    }

    @Override
    public void applyRuleFilters(Set<String> disabledCategories,
            Set<String> disabledRules, Set<String> enabledRules) {
        if (targetLtLang != null) {
            Set<CategoryId> dc = disabledCategories.stream().map(CategoryId::new).collect(Collectors.toSet());
            targetLt = reinitWithRules(targetLtLang, dc, disabledRules, enabledRules);
        }
        if (bRules != null) {
            bRules = bRules.stream().filter(rule -> !disabledRules.contains(rule.getId())).collect(Collectors.toList());
        }
    }

    ThreadLocal<JLanguageTool> reinitWithRules(Language lang, Set<CategoryId> disabledCategories,
            Set<String> disabledRules, Set<String> enabledRules) {
        return ThreadLocal.withInitial(() -> {
            JLanguageTool lt = new JLanguageTool(lang);
            Tools.selectRules(lt, disabledCategories, Collections.emptySet(), disabledRules, enabledRules,
                    false);
            return lt;
        });
    }

    @Override
    protected List<LanguageToolResult> getCheckResultsImpl(String sourceText, String translationText) throws Exception {
        return getRuleMatches(sourceText, translationText).stream().map(m -> new LanguageToolResult(m.getMessage(),
                m.getFromPos(), m.getToPos(), m.getRule().getId(), m.getRule().getDescription()))
                .collect(Collectors.toList());
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

    /**
     * Get the closest LanguageTool language to the supplied OmegaT language, or null if none can be found.
     *
     * @see <code>getLanguageForLanguageNameAndCountry</code> and <code>getLanguageForLanguageNameOnly</code>
     *      in {@link Languages}.
     */
    public static Language getLTLanguage(org.omegat.util.Language lang) {
        List<Language> ltLangs = Languages.get();

        // Search for full xx-YY match
        String omLang = lang.getLanguageCode();
        String omCountry = lang.getCountryCode();
        for (Language ltLang : ltLangs) {
            if (omLang.equalsIgnoreCase(ltLang.getShortName())) {
                List<String> countries = Arrays.asList(ltLang.getCountries());
                if (countries.contains(omCountry)) {
                    return ltLang;
                }
            }
        }

        // Search for just xx match
        for (Language ltLang : ltLangs) {
            if (omLang.equalsIgnoreCase(ltLang.getShortName()) && ltLang.hasVariant()) {
                Language defaultVariant = ltLang.getDefaultLanguageVariant();
                if (defaultVariant != null) {
                    return ltLang;
                }
            }
        }
        for (Language ltLang : ltLangs) {
            if (omLang.equalsIgnoreCase(ltLang.getShortName()) && !ltLang.hasVariant()) {
                return ltLang;
            }
        }
        return null;
    }
}
