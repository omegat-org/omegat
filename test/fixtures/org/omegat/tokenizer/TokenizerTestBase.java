/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
               2024 Hiroshi Miura
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
package org.omegat.tokenizer;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import org.omegat.util.Token;

public class TokenizerTestBase {

    protected void assertVerbatim(String[] expected, String[] test, Token[] testTok, String origString) {
        assertResult(expected, test);
        Assert.assertEquals(StringUtils.join(expected), StringUtils.join(test));
        assertEquals(expected.length, testTok.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], testTok[i].getTextFromString(origString));
        }
    }

    protected void assertResult(String[] expected, String[] test) {
        assertEquals(expected.length, test.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], test[i]);
        }
    }
}
