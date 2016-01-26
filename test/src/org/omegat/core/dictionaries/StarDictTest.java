/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2015 Hiroshi Miura, Aaron Madlon-Kay
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
import java.util.List;

import org.junit.Test;
import org.omegat.core.TestCore;
import org.omegat.core.dictionaries.StarDict.StarDictDict;

/**
 * Dictionary test
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Hiroshi Miura
 * @author Aaron Madlon-Kay
 */
public class StarDictTest extends TestCore {

    @Test
    public void testReadFileDict() throws Exception {
        StarDictDict dict = (StarDictDict) new StarDict().loadDict(new File("test/data/dicts/latin-francais.ifo"));
        assertEquals(10451, dict.data.size());
        
        String word = "testudo";
        Object data = dict.data.get(word);
        assertNotNull(data);
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("dinis, f. : tortue", result.get(0).getArticle());
    }
    
    @Test
    public void testReadZipDict() throws Exception {
        StarDictDict dict = (StarDictDict) new StarDict()
                .loadDict(new File("test/data/dicts-zipped/latin-francais.ifo"));
        assertEquals(10451, dict.data.size());
        
        String word = "testudo";
        Object data = dict.data.get(word);
        assertNotNull(data);
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("dinis, f. : tortue", result.get(0).getArticle());
    }
}
