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

package org.omegat.languagetools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.java.sen.SenFactory;
import net.java.sen.StringTagger;

import org.junit.Test;

import org.languagetool.JLanguageTool;
import org.languagetool.language.Japanese;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRule;

public class LuceneGosenCompatibilityTest {

    /**
     * Regression test for bugs#1204.
     * https://sourceforge.net/p/omegat/bugs/1204/
     */
    @Test
    public void testLuceneGosenGetStringTagger6() {
        StringTagger stringTagger = SenFactory.getStringTagger(null, false);
        assertNotNull(stringTagger);
    }

    @Test
    public void testJapanese() throws Exception {
        JLanguageTool lt = new JLanguageTool(new Japanese());
        List<RuleMatch> matches = lt.check("そんじゃそこらのやつらとは違う");
        assertEquals(1, matches.size());
        assertTrue(matches.get(0).getRule() instanceof PatternRule);
        assertEquals("SONJASOKORA", matches.get(0).getSpecificRuleId());
    }
}
