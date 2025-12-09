/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

package org.omegat.filters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.omegat.filters3.xml.helpandmanual.HelpAndManualFilter;

/**
 * Tests for Help & Manual filter behavior.
 */
public class HelpAndManualFilterTest extends TestFilterBase {

    @Test
    public void testTranslateAttributeFalseIsSkipped() throws Exception {
        HelpAndManualFilter filter = new HelpAndManualFilter();
        List<String> entries = parse(filter, "test/data/filters/helpandmanual/translate-attr.xml");

        // Positive extractions
        assertTrue(entries.contains("Visible Title"));
        assertTrue(entries.contains("Visible Body"));

        // Ensure values marked non-translatable are not extracted
        assertFalse("Should skip translate=\"false\"", entries.contains("Hidden A"));
        assertFalse("Should skip translate=\"no\"", entries.contains("Hidden B"));
        assertFalse("Should skip translate=\"0\"", entries.contains("Hidden C"));
    }

    @Test
    public void testParagraphTagsAreExtracted() throws Exception {
        HelpAndManualFilter filter = new HelpAndManualFilter();
        List<String> entries = parse(filter, "test/data/filters/helpandmanual/paragraph-tags.xml");

        // Text coming from each tag declared via defineParagraphTags in the dialect
        assertTrue("caption text should be extracted", entries.contains("Caption Text"));
        assertTrue("config-value text should be extracted", entries.contains("Config Value Text"));
        assertTrue("variable text should be extracted", entries.contains("Variable Text"));
        assertTrue("para/text content should be extracted", entries.contains("Para Text"));
        assertTrue("title text should be extracted", entries.contains("Title Text"));
        assertTrue("keyword text should be extracted", entries.contains("Keyword Text"));
        assertTrue("li text should be extracted", entries.contains("List Item Text"));
    }
}
