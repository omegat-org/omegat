/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import org.omegat.filters2.TranslationException;
import org.omegat.util.xml.*;

/**
 * Test the XML reader used to read OmegaT preference files.
 *
 * @author Aaron Madlon-Kay
 */
@Deprecated
@Ignore
public class XMLStreamReaderTest {

    @Test
    public void testLoadXML() throws Exception {
        XMLStreamReader xml = new XMLStreamReader();
        xml.killEmptyBlocks();

        xml.setStream(new File("test/data/xml/test.xml"));

        XMLBlock blk;
        List<XMLBlock> lst;

        assertNotNull(xml.advanceToTag("root"));
        assertNotNull(blk = xml.advanceToTag("body"));
        assertEquals("foo", blk.getAttribute("attr"));
        assertNotNull(lst = xml.closeBlock(blk));

        assertEquals(25, lst.size());

        assertOpenTag(lst.get(0), "ascii");
        assertText(lst.get(1), "bar");
        assertCloseTag(lst.get(2), "ascii");

        assertOpenTag(lst.get(3), "bmp");
        assertText(lst.get(4), "\u2603"); // SNOWMAN
        assertCloseTag(lst.get(5), "bmp");

        assertOpenTag(lst.get(6), "dec");
        assertText(lst.get(7), "\u2603"); // SNOWMAN
        assertCloseTag(lst.get(8), "dec");

        assertOpenTag(lst.get(9), "hex");
        assertText(lst.get(10), "\u2603"); // SNOWMAN
        assertCloseTag(lst.get(11), "hex");

        assertOpenTag(lst.get(12), "astral");
        assertText(lst.get(13), "\uD83C\uDCBF"); // PLAYING CARD RED JOKER
        assertCloseTag(lst.get(14), "astral");

        assertOpenTag(lst.get(15), "a-dec");
        assertText(lst.get(16), "\uD83C\uDCBF"); // PLAYING CARD RED JOKER
        assertCloseTag(lst.get(17), "a-dec");

        assertOpenTag(lst.get(18), "a-hex");
        assertText(lst.get(19), "\uD83C\uDCBF"); // PLAYING CARD RED JOKER
        assertCloseTag(lst.get(20), "a-hex");

        assertOpenTag(lst.get(21), "named");
        assertText(lst.get(22), "&<>'\"");
        assertCloseTag(lst.get(23), "named");

        assertStandaloneTag(lst.get(24), "standalone");

        xml.close();
    }

    @Test
    public void testBadEntity() throws Exception {
        try (XMLStreamReader xml = new XMLStreamReader()) {
            xml.killEmptyBlocks();

            XMLBlock blk;

            xml.setStream(new File("test/data/xml/test-badDecimalEntity.xml"));

            assertNotNull(xml.advanceToTag("root"));
            assertNotNull(blk = xml.advanceToTag("body"));
            try {
                assertNotNull(xml.closeBlock(blk));
                fail("XML parsing should fail on bad decimal entity");
            } catch (TranslationException ex) {
            }

            xml.setStream(new File("test/data/xml/test-badHexEntity.xml"));

            assertNotNull(xml.advanceToTag("root"));
            assertNotNull(blk = xml.advanceToTag("body"));
            try {
                assertNotNull(xml.closeBlock(blk));
                fail("XML parsing should fail on bad hex entity");
            } catch (TranslationException ex) {
            }
        }
    }

    private void assertOpenTag(XMLBlock block, String name) {
        assertTrue(block.isTag());
        assertEquals(name, block.getTagName());
        assertFalse(block.isClose());
        assertFalse(block.isStandalone());
    }

    private void assertText(XMLBlock block, String text) {
        assertFalse(block.isTag());
        assertEquals(text, block.getText());
    }

    private void assertCloseTag(XMLBlock block, String text) {
        assertTrue(block.isTag());
        assertEquals(text, block.getTagName());
        assertTrue(block.isClose());
    }

    private void assertStandaloneTag(XMLBlock block, String name) {
        assertTrue(block.isTag());
        assertEquals(name, block.getTagName());
        assertTrue(block.isStandalone());
    }
}
