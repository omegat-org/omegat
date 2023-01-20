/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2022 Hiroshi Miura
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
package org.omegat.externalfinder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.omegat.core.TestCore;
import org.omegat.externalfinder.item.ExternalFinderConfiguration;
import org.omegat.externalfinder.item.ExternalFinderItem;
import org.omegat.util.FileUtil;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;

public class ExternalFinderTest extends TestCore {

    private static final String FINDER_XML = "finder.xml";

    @Before
    public void preUp() throws Exception {
        File configDir = new File(StaticUtils.getConfigDir());
        File[] config = new File[]{new File(new File("test/data/externalfinder/"), FINDER_XML)};
        FileUtil.copyFilesTo(configDir, config, null);
    }

    @Test
    public void testGetProjectConfig() {
        ExternalFinderConfiguration configuration = ExternalFinder.getProjectConfig();
        assertNull(configuration);
    }

    @Test
    public void testGetItems() {
        List<ExternalFinderItem> externalFinderItems = ExternalFinder.getItems();
        assertEquals(6, externalFinderItems.size());
        assertEquals("Google", externalFinderItems.get(0).getName());
        assertFalse(externalFinderItems.get(0).isAsciiOnly());
        assertFalse(externalFinderItems.get(0).isNonAsciiOnly());
        assertTrue(externalFinderItems.get(2).isAsciiOnly());
        String expectation = OStrings.getString("EXTERNALFINDER_CONTENT_TEMPLATE",
                OStrings.getString("EXTERNALFINDER_CONTENT_URLS"), 2);
        assertEquals(expectation, externalFinderItems.get(0).getContentSummary().toString());
    }

    @Test
    public void testGetItemCommand() {
        List<ExternalFinderItem> externalFinderItems = ExternalFinder.getItems();
        assertEquals(1, externalFinderItems.get(5).getCommands().size());
        assertEquals("/usr/bin/open|dict://{target}", externalFinderItems.get(5).getCommands().get(0).getCommand());
        assertEquals("shift ctrl pressed K", externalFinderItems.get(5).getKeystroke().toString());
    }

    @Test
    public void testGetItemUrl() {
        List<ExternalFinderItem> externalFinderItems = ExternalFinder.getItems();
        assertEquals(2, externalFinderItems.get(0).getURLs().size());
        assertEquals("https://www.google.com/search?q={target}",
                externalFinderItems.get(0).getURLs().get(0).getURL());
        assertEquals("https://www.google.com/search?q=define%3A{target}",
                externalFinderItems.get(0).getURLs().get(1).getURL());
    }

    @Test
    public void testGetItemPopup() {
        List<ExternalFinderItem> externalFinderItems = ExternalFinder.getItems();
        assertTrue(externalFinderItems.get(0).isNopopup());
    }
}
