/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
               2015 Aaron Madlon-Kay
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
package org.omegat.util.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import org.omegat.core.Core;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.gui.editor.EditorUtils;
import org.omegat.gui.editor.IEditor.CHANGE_CASE_TO;
import org.omegat.gui.glossary.GlossaryEntry;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.util.Language;

public class EditorUtilsTest extends TestCase {
    
    public void testRemoveDirectionChars() {
        assertEquals("|", EditorUtils.removeDirectionChars("|"));
        assertEquals("", EditorUtils.removeDirectionChars("\u202A"));
        assertEquals("", EditorUtils.removeDirectionChars("\u202B"));
        assertEquals("", EditorUtils.removeDirectionChars("\u202C"));
        assertEquals("zz", EditorUtils.removeDirectionChars("\u202Az\u202Bz\u202C"));
        assertEquals("zz", EditorUtils.removeDirectionChars("zz"));
    }
    
    public void testChangeCase() {
        Locale locale = Locale.ENGLISH;
        ITokenizer tokenizer = new LuceneEnglishTokenizer();

        String input = "a I've GOT a {crazy} text hErE including 1 \u65e5\u672c\u8a9e!";
        String round1 = EditorUtils.doChangeCase(input, CHANGE_CASE_TO.CYCLE, locale, tokenizer);
        assertEquals("A I'VE GOT A {CRAZY} TEXT HERE INCLUDING 1 \u65e5\u672c\u8a9e!", round1);
        assertEquals(round1, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.UPPER, locale, tokenizer));
        String round2 = EditorUtils.doChangeCase(round1, CHANGE_CASE_TO.CYCLE, locale, tokenizer);
        assertEquals("a i've got a {crazy} text here including 1 \u65e5\u672c\u8a9e!", round2);
        assertEquals(round2, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.LOWER, locale, tokenizer));
        String round3 = EditorUtils.doChangeCase(round2, CHANGE_CASE_TO.CYCLE, locale, tokenizer);
        assertEquals("A i've got a {crazy} text here including 1 \u65e5\u672c\u8a9e!", round3);
        assertEquals(round3, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.SENTENCE, locale, tokenizer));
        String round4 = EditorUtils.doChangeCase(round3, CHANGE_CASE_TO.CYCLE, locale, tokenizer);
        assertEquals("A I've Got A {Crazy} Text Here Including 1 \u65e5\u672c\u8a9e!", round4);
        assertEquals(round4, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.TITLE, locale, tokenizer));
        String round5 = EditorUtils.doChangeCase(round4, CHANGE_CASE_TO.CYCLE, locale, tokenizer);
        assertEquals(round1, round5);
        
        input = "lower case only";
        assertEquals(input, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.LOWER, locale, tokenizer));
        assertEquals("LOWER CASE ONLY", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.UPPER, locale, tokenizer));
        assertEquals("Lower case only", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.SENTENCE, locale, tokenizer));
        assertEquals("Lower Case Only", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.TITLE, locale, tokenizer));
        assertEquals("Lower case only", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.CYCLE, locale, tokenizer));
        
        input = "UPPER CASE ONLY";
        assertEquals("upper case only", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.LOWER, locale, tokenizer));
        assertEquals(input, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.UPPER, locale, tokenizer));
        assertEquals("Upper case only", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.SENTENCE, locale, tokenizer));
        assertEquals("Upper Case Only", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.TITLE, locale, tokenizer));
        assertEquals("upper case only", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.CYCLE, locale, tokenizer));
        
        input = "Title Case Only";
        assertEquals("title case only", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.LOWER, locale, tokenizer));
        assertEquals("TITLE CASE ONLY", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.UPPER, locale, tokenizer));
        assertEquals("Title case only", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.SENTENCE, locale, tokenizer));
        assertEquals(input, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.TITLE, locale, tokenizer));
        assertEquals("TITLE CASE ONLY", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.CYCLE, locale, tokenizer));
        
        input = "Sentence case string";
        assertEquals("sentence case string", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.LOWER, locale, tokenizer));
        assertEquals("SENTENCE CASE STRING", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.UPPER, locale, tokenizer));
        assertEquals(input, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.SENTENCE, locale, tokenizer));
        assertEquals("Sentence Case String", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.TITLE, locale, tokenizer));
        assertEquals("Sentence Case String", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.CYCLE, locale, tokenizer));
        
        input = "mIxed CaSe oNly";
        assertEquals("mixed case only", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.LOWER, locale, tokenizer));
        assertEquals("MIXED CASE ONLY", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.UPPER, locale, tokenizer));
        assertEquals("Mixed case only", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.SENTENCE, locale, tokenizer));
        assertEquals("Mixed Case Only", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.TITLE, locale, tokenizer));
        assertEquals("MIXED CASE ONLY", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.CYCLE, locale, tokenizer));
        
        // Ambiguous only
        input = "A B C";
        assertEquals("a b c", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.LOWER, locale, tokenizer));
        assertEquals(input, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.UPPER, locale, tokenizer));
        assertEquals("A b c", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.SENTENCE, locale, tokenizer));
        assertEquals(input, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.TITLE, locale, tokenizer));
        round2 = EditorUtils.doChangeCase(input, CHANGE_CASE_TO.CYCLE, locale, tokenizer);
        assertEquals("a b c", round2);
        round3 = EditorUtils.doChangeCase(round2, CHANGE_CASE_TO.CYCLE, locale, tokenizer);
        assertEquals("A b c", round3);
        round4 = EditorUtils.doChangeCase(round3, CHANGE_CASE_TO.CYCLE, locale, tokenizer);
        assertEquals(input, round4);
        
        // No letter-containing tokens
        input = "{!} 1 \u65e5\u672c\u8a9e";
        assertEquals(input, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.LOWER, locale, tokenizer));
        assertEquals(input, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.UPPER, locale, tokenizer));
        assertEquals(input, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.SENTENCE, locale, tokenizer));
        assertEquals(input, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.TITLE, locale, tokenizer));
        assertEquals(input, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.CYCLE, locale, tokenizer));
        
        // Single tokens
        
        input = "lower";
        assertEquals(input, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.LOWER, locale, tokenizer));
        assertEquals("LOWER", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.UPPER, locale, tokenizer));
        assertEquals("Lower", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.SENTENCE, locale, tokenizer));
        assertEquals("Lower", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.TITLE, locale, tokenizer));
        assertEquals("Lower", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.CYCLE, locale, tokenizer));
        
        input = "UPPER";
        assertEquals("upper", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.LOWER, locale, tokenizer));
        assertEquals(input, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.UPPER, locale, tokenizer));
        assertEquals("Upper", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.SENTENCE, locale, tokenizer));
        assertEquals("Upper", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.TITLE, locale, tokenizer));
        assertEquals("upper", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.CYCLE, locale, tokenizer));
        
        input = "Title";
        assertEquals("title", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.LOWER, locale, tokenizer));
        assertEquals("TITLE", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.UPPER, locale, tokenizer));
        assertEquals(input, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.SENTENCE, locale, tokenizer));
        assertEquals(input, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.TITLE, locale, tokenizer));
        assertEquals("TITLE", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.CYCLE, locale, tokenizer));
                
        input = "mIxed";
        assertEquals("mixed", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.LOWER, locale, tokenizer));
        assertEquals("MIXED", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.UPPER, locale, tokenizer));
        assertEquals("Mixed", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.SENTENCE, locale, tokenizer));
        assertEquals("Mixed", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.TITLE, locale, tokenizer));
        assertEquals("MIXED", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.CYCLE, locale, tokenizer));
        
        // Ambiguous
        input = "A";
        assertEquals("a", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.LOWER, locale, tokenizer));
        assertEquals(input, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.UPPER, locale, tokenizer));
        assertEquals(input, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.SENTENCE, locale, tokenizer));
        assertEquals(input, EditorUtils.doChangeCase(input, CHANGE_CASE_TO.TITLE, locale, tokenizer));
        assertEquals("a", EditorUtils.doChangeCase(input, CHANGE_CASE_TO.CYCLE, locale, tokenizer));
    }

    public void testReplaceGlossaryEntries() {
        List<GlossaryEntry> entries = new ArrayList<GlossaryEntry>();
        entries.add(new GlossaryEntry("snowman", "sneeuwpop", "", false));
        entries.add(new GlossaryEntry("Bob", "Blub", "", false));
        
        String srcText = "Snowman Bob went to the snowman party. SnOwMaN!";
        String expected = "Sneeuwpop Blub went to the sneeuwpop party. sneeuwpop!";
        Locale locale = Locale.ENGLISH;
        ITokenizer tokenizer = new LuceneEnglishTokenizer();
        assertEquals(expected, EditorUtils.replaceGlossaryEntries(srcText, entries,
                locale, tokenizer));
        
        // Empty cases
        assertNull(EditorUtils.replaceGlossaryEntries(null, entries, locale, tokenizer));
        assertEquals("", EditorUtils.replaceGlossaryEntries("", entries, locale, tokenizer));
        assertSame(srcText, EditorUtils.replaceGlossaryEntries(srcText, null, locale, tokenizer));
        assertSame(srcText, EditorUtils.replaceGlossaryEntries(srcText, new ArrayList<GlossaryEntry>(), locale, tokenizer));
        try {
            EditorUtils.replaceGlossaryEntries(srcText, entries, null, tokenizer);
            fail("Should give NPE when given null locale");
        } catch (NullPointerException ex) {
        }
        try {
            EditorUtils.replaceGlossaryEntries(srcText, entries, locale, null);
            fail("Should give NPE when given null tokenizer");
        } catch (NullPointerException ex) {
        }
    }
}
