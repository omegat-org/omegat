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

import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Belarusian;
import org.languagetool.language.English;
import org.languagetool.language.French;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.UppercaseSentenceStartRule;
import org.languagetool.rules.patterns.PatternRule;

/**
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class LanguageToolTest extends TestCase {
    @Test
    public void testExecute() throws Exception {
        JLanguageTool lt = new JLanguageTool(new Belarusian());
        lt.activateDefaultPatternRules();

        List<RuleMatch> matches = lt.check("спраудзім.");
        assertEquals(2, matches.size());
        assertTrue(matches.get(0).getRule() instanceof UppercaseSentenceStartRule);
        assertTrue(matches.get(1).getRule() instanceof PatternRule);
    }

    @Test
    public void testFrench() throws Exception {
        JLanguageTool lt = new JLanguageTool(new French());
        lt.activateDefaultPatternRules();

        List<RuleMatch> matches = lt.check("Directeur production du groupe");
        assertEquals(1, matches.size());
        assertTrue(matches.get(0).getRule() instanceof PatternRule);
    }

    @Test
    public void testEnglish() throws Exception {
        JLanguageTool lt = new JLanguageTool(new English());
        lt.activateDefaultPatternRules();

        List<RuleMatch> matches = lt.check("Check test");
        assertEquals(0, matches.size());
    }
}
