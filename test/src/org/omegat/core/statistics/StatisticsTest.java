/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
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

package org.omegat.core.statistics;

import junit.framework.TestCase;

/**
 * Tests for statistics calculation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class StatisticsTest extends TestCase {
    public void testNumberOfWords() {
        assertEquals(3, Statistics.numberOfWords("one two three"));
        assertEquals(3, Statistics.numberOfWords("one , \b two three"));
        assertEquals(5, Statistics.numberOfWords("o\bne <b>two</b>"));
    }
    public void testNumberOfChars() {
        assertEquals(3, Statistics.numberOfCharactersWithoutSpaces("1 2\b3"));
        assertEquals(4, Statistics.numberOfCharactersWithSpaces("1 2\b3"));
    }
}
