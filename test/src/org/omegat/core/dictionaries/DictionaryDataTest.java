/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.core.dictionaries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.junit.Test;
import org.omegat.util.Language;

public class DictionaryDataTest {

    @Test
    public void testLookup() {
        DictionaryData<String> data = new DictionaryData<>(new Language(Locale.ENGLISH));
        data.add("foobar", "bazbiz");
        data.add("foobar", "buzzfizz");
        data.add("ho\u0308ge", "hogehoge");
        data.add("blah", "blooh");
        data.add("BLAH", "blooh2");

        // Test pre-finalized state
        assertEquals(-1, data.size());
        try {
            data.lookUp("foobar");
            fail();
        } catch (IllegalStateException ex) {
            // Not finalized yet
        }
        try {
            data.lookUpPredictive("foobar");
            fail();
        } catch (IllegalStateException ex) {
            // Not finalized yet
        }

        data.done();

        assertEquals(4, data.size());

        // Test normal lookup
        List<Entry<String, String>> result = data.lookUp("foobar");
        assertEquals(2, result.size());
        assertEquals("bazbiz", result.get(0).getValue());
        assertEquals("buzzfizz", result.get(1).getValue());

        List<Entry<String, String>> presult = data.lookUpPredictive("foobar");
        assertEquals(2, result.size());
        assertEquals("bazbiz", presult.get(0).getValue());
        assertEquals("buzzfizz", presult.get(1).getValue());

        // Test case matching
        result = data.lookUp("FOOBAR");
        assertEquals(2, result.size());
        assertEquals("bazbiz", result.get(0).getValue());
        assertEquals("foobar", result.get(0).getKey());
        assertEquals("buzzfizz", result.get(1).getValue());
        assertEquals("foobar", result.get(1).getKey());

        // Test case differentiation
        result = data.lookUp("blah");
        assertEquals(2, result.size());
        assertEquals("blooh", result.get(0).getValue());
        assertEquals("blah", result.get(0).getKey());
        assertEquals("blooh2", result.get(1).getValue());
        assertEquals("blah", result.get(1).getKey());

        result = data.lookUp("BLAH");
        assertEquals(1, result.size());
        assertEquals("blooh2", result.get(0).getValue());
        assertEquals("BLAH", result.get(0).getKey());

        // Test prediction
        presult = data.lookUpPredictive("foo");
        assertEquals(2, presult.size());
        assertEquals("bazbiz", presult.get(0).getValue());
        assertEquals("foobar", presult.get(0).getKey());
        assertEquals("buzzfizz", presult.get(1).getValue());
        assertEquals("foobar", presult.get(1).getKey());

        result = data.lookUp("foo");
        assertTrue(result.isEmpty());

        // Test Unicode normalization
        result = data.lookUp("h\u00f6ge");
        assertEquals(1, result.size());
        assertEquals("hogehoge", result.get(0).getValue());

        // Test non-existent key
        result = data.lookUp("zzzz");
        assertTrue(result.isEmpty());
    }
}
