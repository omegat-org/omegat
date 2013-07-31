/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Aaron Madlon-Kay
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

package org.omegat.core.tagvalidation;

import junit.framework.TestCase;

/**
 * @author Aaron Madlon-Kay
 */
public class TagRepairTest extends TestCase {

    public void testRepairTags() {
        
        // Fix extraneous
        StringBuilder text = new StringBuilder("Foo bar baz bar bonkers");
        TagRepair.fixExtraneous(text, "bar");
        assertEquals("Foo  baz  bonkers", text.toString());
        
        // Fix missing: before
        text = new StringBuilder("Foo bar {tag2}baz");
        String[] tags = {"{tag1}", "{tag2}"};
        TagRepair.fixMissing(TagValidationTest.getList(tags), text, "{tag1}");
        assertEquals("Foo bar {tag1}{tag2}baz", text.toString());
        
        // Fix missing: after
        text = new StringBuilder("Foo bar {tag2}baz");
        String[] tags2 = {"{tag2}", "{tag1}"};
        TagRepair.fixMissing(TagValidationTest.getList(tags2), text, "{tag1}");
        assertEquals("Foo bar {tag2}{tag1}baz", text.toString());
        
        // Fix missing: no anchor
        text = new StringBuilder("Foo bar baz");
        String[] tags3 = {"{tag1}"};
        TagRepair.fixMissing(TagValidationTest.getList(tags3), text, "{tag1}");
        assertEquals("Foo bar baz{tag1}", text.toString());
        
        // Fix maformed
        text = new StringBuilder("Foo bar {tag2}baz{tag1}");
        String[] tags4 = {"{tag1}", "{tag2}"};
        TagRepair.fixMalformed(TagValidationTest.getList(tags4), text, "{tag1}");
        assertEquals("Foo bar {tag1}{tag2}baz", text.toString());
        
        // Fix whitespace
        text = new StringBuilder("\nFoo\n");
        TagRepair.fixWhitespace(text, "Foo");
        assertEquals("Foo", text.toString());
        
        // Fix whitespace
        text = new StringBuilder("Foo");
        TagRepair.fixWhitespace(text, "\nFoo\n");
        assertEquals("\nFoo\n", text.toString());
    }
}
