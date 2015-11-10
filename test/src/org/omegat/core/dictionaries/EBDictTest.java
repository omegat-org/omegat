/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Hiroshi Miura
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

package org.omegat.core.dictionaries;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.junit.Test;
import static org.junit.Assert.*;

import org.omegat.core.TestCore;

/**
 *
 * @author Hiroshi Miura
 */
public class EBDictTest {
    /**
     * Test of searchExactMatch method
     * @throws java.lang.Exception
     */
    @Test
    public void testSearchExactMatch() throws Exception {
        EBDict e = new EBDict(new File("test/data/dicts/epwing/CATALOGS"));
        String word = "Here";
        Object result = e.searchExactMatch(word);
        assertNotNull(result);
        assertTrue(result instanceof String);
    }

    /**
     * Test of readArticle method
     * @throws java.lang.Exception
     */
    @Test
    public void testReadArticle() throws Exception {
        EBDict e = new EBDict(new File("test/data/dicts/epwing/CATALOGS"));
        String word = "Tokyo";
        Object data = e.searchExactMatch(word);
        String result = e.readArticle(word, data);
        assertEquals("&nbsp;Tokyo<br>&nbsp;&nbsp;\u6771\u4eac<br>&nbsp;", result);
    }

    /**
     * Test of zipped dictionary
     * @throws java.lang.Exception
     */
    @Test
    public void testEBZipReadArticle() throws Exception {
        EBDict e = new EBDict(new File("test/data/dicts-zipped/epwing/CATALOGS"));
        String word = "Tokyo";
        Object data = e.searchExactMatch(word);
        String result = e.readArticle(word, data);
        assertEquals("&nbsp;Tokyo<br>&nbsp;&nbsp;\u6771\u4eac<br>&nbsp;", result);
    }

    /**
     * Test of prefixMatch method
     * @throws java.lang.Exception
     */
    @Test
    public void testPrefixMatch() throws Exception {
        Object data;
        List<DictionaryEntry> result = new ArrayList<DictionaryEntry>();
        EBDict e = new EBDict(new File("test/data/dicts/epwing/CATALOGS"));
        String word = "Tokyo";
        String prefix = "Tok";
        Map<String, Object> resultMap = e.searchPrefixMatch(prefix);
        for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
            data = entry.getValue();
            retrieveArticle(entry.getKey(), entry.getValue(), e, result);
        }
        assertTrue(resultMap.containsKey(word));
    }

    /**
     * Test of prefixMatch method
     * @throws java.lang.Exception
     */
    @Test
    public void testPrefixMatchRetrieveArticles() throws Exception {
        Object data = null;
        List<DictionaryEntry> result = new ArrayList<DictionaryEntry>();
        EBDict e = new EBDict(new File("test/data/dicts/epwing/CATALOGS"));
        String word = "Tokyo";
        Map<String, String> expected = new HashMap<String, String>();
        expected.put(word, "&nbsp;Tokyo<br>&nbsp;&nbsp;\u6771\u4eac<br>&nbsp;");
        Map<String, Object> resultMap = e.searchPrefixMatch(word);
        if (resultMap.containsKey(word)) {
            data = resultMap.get(word);
            assertEquals(expected.get(word), (String) data);
        } else {
            for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                retrieveArticle(entry.getKey(), entry.getValue(), e, result);
            }
            for (DictionaryEntry entry: result) {
                assertEquals(expected.get(entry.getWord()), entry.getArticle());
            }
        }
    }

    private void retrieveArticle(String word, Object data,
                IDictionary di, final List<DictionaryEntry> result) throws Exception {
        if (data.getClass().isArray()) {
            for (Object d : (Object[]) data) {
                String a = di.readArticle(word, d);
                result.add(new DictionaryEntry(word, a));
            }
        } else {
            String a = di.readArticle(word, data);
            result.add(new DictionaryEntry(word, a));
        }
    }
}
