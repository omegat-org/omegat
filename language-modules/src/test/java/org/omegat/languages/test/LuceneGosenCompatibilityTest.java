/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.languages.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Objects;

import net.java.sen.SenFactory;
import net.java.sen.StringTagger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRule;

import org.omegat.languagetools.LanguageDataBroker;
import org.omegat.languagetools.LanguageManager;

public class LuceneGosenCompatibilityTest {

    private static final String JAPANESE = "org.languagetool.language.Japanese";

    @BeforeClass
    public static void setUpClass() {
        // We don't use plugin loader in a test context
        // JLanguageTool.setClassBrokerBroker(new LanguageClassBroker());
        JLanguageTool.setDataBroker(new LanguageDataBroker());
        LanguageManager.registerLTLanguage("ja-JP", JAPANESE);
    }

    /**
     * Regression test for bugs#1204.
     * <a href="https://sourceforge.net/p/omegat/bugs/1204/">
     * LanguageTool6 lucene-gosen-ipadic from omegat project is incompatible</a>
     */
    @Test
    public void testLuceneGosenGetStringTagger6() {
        StringTagger stringTagger = SenFactory.getStringTagger(null, false);
        assertNotNull(stringTagger);
    }

    @Test
    public void testJapanese() throws Exception {
        Language lang = LanguageManager.getLTLanguage(new org.omegat.util.Language("ja-JP"));
        JLanguageTool lt = new JLanguageTool(Objects.requireNonNull(lang));
        List<RuleMatch> matches = lt.check("そんじゃそこらのやつらとは違う");
        assertEquals(1, matches.size());
        assertTrue(matches.get(0).getRule() instanceof PatternRule);
        assertEquals("SONJASOKORA", matches.get(0).getSpecificRuleId());
    }
}
