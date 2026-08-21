/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Stephan Pakebusch
 */
public class TagPatternsStorageTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File configFile;

    @Before
    public void setUp() throws Exception {
        configFile = new File(folder.getRoot(), TagPatternsStorage.FILE_TAG_PATTERNS);
    }

    @Test
    public void testRoundTrip() throws Exception {
        TagPatternsStorage.TagPatterns patterns = new TagPatternsStorage.TagPatterns();
        patterns.setCustomTagPattern("%[a-z]+");
        patterns.setRemoveTextPattern("\\{[0-9]+\\}");
        TagPatternsStorage.save(patterns, configFile);
        assertTrue(configFile.exists());

        TagPatternsStorage.TagPatterns loaded = TagPatternsStorage.load(configFile);
        assertEquals("%[a-z]+", loaded.getCustomTagPattern());
        assertEquals("\\{[0-9]+\\}", loaded.getRemoveTextPattern());
    }

    @Test
    public void testEmptyStringSurvivesTheRoundTrip() throws Exception {
        // The whole null-vs-empty semantics rests on an empty element coming
        // back as an empty string: null means the global preference applies,
        // while the empty string switches the expression off.
        TagPatternsStorage.TagPatterns patterns = new TagPatternsStorage.TagPatterns();
        patterns.setCustomTagPattern("");
        TagPatternsStorage.save(patterns, configFile);

        TagPatternsStorage.TagPatterns loaded = TagPatternsStorage.load(configFile);
        assertEquals("", loaded.getCustomTagPattern());
        assertNull(loaded.getRemoveTextPattern());
    }

    @Test
    public void testInheritingProjectLeavesNoFile() throws Exception {
        TagPatternsStorage.TagPatterns patterns = new TagPatternsStorage.TagPatterns();
        patterns.setCustomTagPattern("%[a-z]+");
        TagPatternsStorage.save(patterns, configFile);
        assertTrue(configFile.exists());

        TagPatternsStorage.save(new TagPatternsStorage.TagPatterns(), configFile);
        assertFalse(configFile.exists());
        assertNull(TagPatternsStorage.load(configFile));
    }

    @Test
    public void testUnreadableFileRaisesAndStaysInPlace() throws Exception {
        // The caller falls back to the global preferences but must know
        // about the failure, so a routine save does not delete a file the
        // user could still repair.
        Files.write(configFile.toPath(), "no xml at all".getBytes(StandardCharsets.UTF_8));
        try {
            TagPatternsStorage.load(configFile);
            fail("An unparseable file must raise an IOException");
        } catch (IOException expected) {
        }
        assertTrue(configFile.exists());
    }

    @Test
    public void testUnknownElementsAreTolerated() throws Exception {
        // Forward compatibility: a file written by a newer OmegaT with
        // additional elements must still load in this version.
        Files.write(configFile.toPath(),
                ("<?xml version='1.0' encoding='UTF-8'?>\n" + "<tag_patterns>\n"
                        + "  <custom_tag_pattern>%[a-z]+</custom_tag_pattern>\n"
                        + "  <shiny_future_pattern>x</shiny_future_pattern>\n" + "</tag_patterns>\n")
                        .getBytes(StandardCharsets.UTF_8));
        TagPatternsStorage.TagPatterns loaded = TagPatternsStorage.load(configFile);
        assertEquals("%[a-z]+", loaded.getCustomTagPattern());
        assertNull(loaded.getRemoveTextPattern());
    }
}
