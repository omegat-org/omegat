/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Hiroshi Miura, Aaron Madlon-Kay
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
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import junit.framework.TestCase;

import org.omegat.gui.dictionaries.IDictionariesPane;

/**
 *
 * @author Hiroshi Miura
 * @author Aaron Madlon-Kay
 */
public class DictionariesManagerTest extends TestCase {
    
    private static final String DICTS_DIR = "test/data/dicts";
    private static final File IGNORE_FILE = new File(DICTS_DIR, DictionariesManager.IGNORE_FILE);
    private static final File DICT_FILE = new File(DICTS_DIR, "latin-francais.ifo");
    
    private static final String IGNORE_WORD = "testor";

    private DictionariesManager manager;
    
    @Before
    public void setUp() throws Exception {
        manager = new DictionariesManager(new IDictionariesPane() {
            @Override
            public void refresh() {}
        });
        PrintWriter fw = new PrintWriter(IGNORE_FILE, "UTF-8");
        fw.println(IGNORE_WORD);
        fw.close();
        assertFalse(fw.checkError());
    }
    
    @After
    public void tearDown() {
        assertTrue(IGNORE_FILE.delete());
    }

    /**
     * Test of fileChanged method
     * with StarDict dict file.
     */
    @Test
    public void testFileChanged() {
        
        // Nothing is loaded so there should be no result.
        List<DictionaryEntry> result = manager.findWords(Arrays.asList(IGNORE_WORD));
        assertTrue(result.isEmpty());

        // Load sample dictionary.
        manager.fileChanged(DICT_FILE);
        
        // Now we find the result.
        result = manager.findWords(Arrays.asList(IGNORE_WORD));
        assertFalse(result.isEmpty());
        
        // Load ignore list.
        manager.fileChanged(IGNORE_FILE);
        
        // Now we should not find the result.
        result = manager.findWords(Arrays.asList(IGNORE_WORD));
        assertTrue(result.isEmpty());
    }
    
    /**
     * Test of loadIgnoreWords method
     */
    @Test
    public void testLoadIgnoreWords() throws Exception {
        manager.fileChanged(DICT_FILE);
                
        List<DictionaryEntry> result = manager.findWords(Arrays.asList(IGNORE_WORD));
        assertFalse(result.isEmpty());
        
        manager.loadIgnoreWords(IGNORE_FILE);
        result = manager.findWords(Arrays.asList(IGNORE_WORD));
        assertTrue(result.isEmpty());
    }

    /**
     * Test of addIgnoreWord method
     */
    @Test
    public void testAddIgnoreWord() {
        manager.fileChanged(DICT_FILE);
        
        String word = "testudo";
        
        List<DictionaryEntry> result = manager.findWords(Arrays.asList(word));
        assertFalse(result.isEmpty());
        
        manager.addIgnoreWord(word);
        result = manager.findWords(Arrays.asList(word));
        assertTrue(result.isEmpty());
    }

    /**
     * Test of findWords method
     */
    @Test
    public void testFindWords() throws Exception {
        manager.fileChanged(DICT_FILE);
        manager.loadIgnoreWords(IGNORE_FILE);
        
        String find1 = "testudo";
        String find2 = "tete";
        List<DictionaryEntry> result = manager.findWords(Arrays.asList(IGNORE_WORD, find1, find2));
        assertEquals(2, result.size());
        assertFalse(resultContains(result, IGNORE_WORD));
        assertTrue(resultContains(result, find1));
        assertTrue(resultContains(result, find2));
    }
    
    private static boolean resultContains(Collection<DictionaryEntry> result, String word) {
        for (DictionaryEntry entry : result) {
            if (entry.getWord().equals(word)) {
                return true;
            }
        }
        return false;
    }
}
