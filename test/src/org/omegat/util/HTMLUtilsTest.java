/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2009 Alex Buloichik
 *                2015 Didier Briel
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.omegat.util;

import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

public class HTMLUtilsTest {

    @Test
    public void testEntitiesToChars() {
        // Named entities
        assertEquals("<", HTMLUtils.entitiesToChars("&lt;"));
        assertEquals(">", HTMLUtils.entitiesToChars("&gt;"));
        assertEquals("&", HTMLUtils.entitiesToChars("&amp;"));
        assertEquals("\"", HTMLUtils.entitiesToChars("&quot;"));

        // Numeric entities
        assertEquals("\"", HTMLUtils.entitiesToChars("&#34;"));
        assertEquals("\"", HTMLUtils.entitiesToChars("&#x22;"));
        assertEquals("©", HTMLUtils.entitiesToChars("&#169;"));

        // Invalid or unsupported entities
        assertEquals("&invalid;", HTMLUtils.entitiesToChars("&invalid;"));
        assertEquals("&;", HTMLUtils.entitiesToChars("&;"));
        assertEquals("& #;", HTMLUtils.entitiesToChars("& #;")); // malformed
    }

    @Test
    public void getSpacePrefix() {
        //no blank prefix
        assertEquals("", HTMLUtils.getSpacePrefix("a", true));
        assertEquals("", HTMLUtils.getSpacePrefix("́a", true));
        assertEquals("", HTMLUtils.getSpacePrefix("\u00A0a", true));
        assertEquals("", HTMLUtils.getSpacePrefix("\u2007a", true));
        assertEquals("", HTMLUtils.getSpacePrefix("\u202Fa", true));

        //all sorts of space characters
        assertEquals("\n", HTMLUtils.getSpacePrefix("\na", true));
        assertEquals("\r", HTMLUtils.getSpacePrefix("\ra", true));
        assertEquals("\u2028", HTMLUtils.getSpacePrefix("\u2028a", true)); //line separator
        assertEquals("\u2029", HTMLUtils.getSpacePrefix("\u2029a", true)); //paragraph separator
        assertEquals("\t", HTMLUtils.getSpacePrefix("\ta", true)); //h tab
        assertEquals("\u000B", HTMLUtils.getSpacePrefix("\u000Ba", true)); //v tab
        assertEquals("\f", HTMLUtils.getSpacePrefix("\fa", true)); //form feed
        assertEquals("\u001C", HTMLUtils.getSpacePrefix("\u001Ca", true)); //file separator
        assertEquals("\u001D", HTMLUtils.getSpacePrefix("\u001Da", true)); //group separator
        assertEquals("\u001E", HTMLUtils.getSpacePrefix("\u001Ea", true)); //record separator
        assertEquals("\u001F", HTMLUtils.getSpacePrefix("\u001Fa", true)); //unit separator

        // u0301 is an accent, in a multi-code point character.
        assertEquals("one space is one space", " ", HTMLUtils.getSpacePrefix(" ́a", true));
        assertEquals("multiple spaces is compressed to one", " ", HTMLUtils.getSpacePrefix("  ́a", true));
        assertEquals("multiple spaces stay multiple spaces uncompressed", "    ",
                HTMLUtils.getSpacePrefix("    ́ap", false));
        String allWhite = "\n\r\u2028\u2029\t\n\u000B\f\n\u001C\u001D\u001E\u001F ";
        assertEquals("multiple different space types compress to the first whitespace character", "\n",
                HTMLUtils.getSpacePrefix(allWhite + "a", true));
        assertEquals("multiple different whtiespace characters stay that uncompressed", allWhite,
                HTMLUtils.getSpacePrefix(allWhite + "a", false));
    }

    @Test
    public void getSpacePostfix() {
        // no whitespace prefix
        assertEquals("", HTMLUtils.getSpacePostfix("a", true));
        assertEquals("", HTMLUtils.getSpacePostfix("á", true));
        assertEquals("", HTMLUtils.getSpacePostfix("a\u00A0", true));
        assertEquals("", HTMLUtils.getSpacePostfix("a\u2007", true));
        assertEquals("", HTMLUtils.getSpacePostfix("a\u202F", true));

        // all sorts of whitespace characters
        assertEquals("\n", HTMLUtils.getSpacePostfix("a\n", true));
        assertEquals("\r", HTMLUtils.getSpacePostfix("a\r", true));
        assertEquals("\u2028", HTMLUtils.getSpacePostfix("a\u2028", true)); //line separator
        assertEquals("\u2029", HTMLUtils.getSpacePostfix("a\u2029", true)); //paragraph separator
        assertEquals("\t", HTMLUtils.getSpacePostfix("a\t", true)); //h tab
        assertEquals("\u000B", HTMLUtils.getSpacePostfix("a\u000B", true)); //v tab
        assertEquals("\f", HTMLUtils.getSpacePostfix("a\f", true)); //form feed
        assertEquals("\u001C", HTMLUtils.getSpacePostfix("a\u001C", true)); //file separator
        assertEquals("\u001D", HTMLUtils.getSpacePostfix("a\u001D", true)); //group separator
        assertEquals("\u001E", HTMLUtils.getSpacePostfix("a\u001E", true)); //record separator
        assertEquals("\u001F", HTMLUtils.getSpacePostfix("a\u001F", true)); //unit separator

        // u0301 is an accent, in a multi-code point character.
        assertEquals("one space is one space", " ", HTMLUtils.getSpacePostfix("aé ", true));
        assertEquals("multiple spaces is compressed to one", " ", HTMLUtils.getSpacePostfix("aé  ", true));
        assertEquals("multiple spaces stay multiple spaces uncompressed", "    ",
                HTMLUtils.getSpacePostfix("aé    ", false));
        String allWhite = "\n\r\u2028\u2029\t\n\u000B\f\n\u001C\u001D\u001E\u001F ";
        assertEquals("multiple different space types compress to the first whitespace character", "\n",
                HTMLUtils.getSpacePostfix("aé" + allWhite, true));
        assertEquals("multiple different whitespace characters stay that uncompressed", allWhite,
                HTMLUtils.getSpacePostfix("aé" + allWhite, false));

    }

    @Test
    public void testDetectEncoding() {
        // Test with UTF-8
        String utf8File = "test/data/util/file-HTMLUtils-utf8.html";
        assertEquals("UTF-8", HTMLUtils.detectEncoding(utf8File, null).name());

        // Test with UTF-16 BE BOM
        String utf16BEFile = "test/data/util/file-HTMLUtils-utf16_be_with_bom.html";
        assertEquals("UTF-16BE", HTMLUtils.detectEncoding(utf16BEFile, null).name());

        // Test with UTF-16 LE BOM
        String utf16LEFile = "test/data/util/file-HTMLUtils-utf16_le_with_bom.html";
        assertEquals("UTF-16LE", HTMLUtils.detectEncoding(utf16LEFile, null).name());

        // Test with no BOM and default encoding provided
        String noBomFile = "test/data/util/file-HTMLUtils-no_header_no_bom.html";
        assertEquals("ISO-8859-1", HTMLUtils.detectEncoding(noBomFile, "ISO-8859-1").name());

        // Test with no BOM and no default encoding
        assertEquals(Charset.defaultCharset().name(), HTMLUtils.detectEncoding(noBomFile, null).name());
    }

}
