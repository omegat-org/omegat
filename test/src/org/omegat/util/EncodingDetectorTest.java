/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2025 Hiroshi Miura
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
import static org.junit.Assume.assumeTrue;
import static org.omegat.util.EncodingDetector.detectHtmlEncoding;

public class EncodingDetectorTest {

    @Test
    public void testDetectHTMLEncoding() {
        // Test with UTF-8 content-type provided in meta
        String utf8File = "test/data/util/file-HTMLUtils-utf8-content-type.html";
        assertEquals("UTF-8", detectHtmlEncoding(utf8File, null).name());

        // Test with UTF-16 BE BOM
        String utf16BEFile = "test/data/util/file-HTMLUtils-utf16_be_with_bom.html";
        assertEquals("UTF-16BE", detectHtmlEncoding(utf16BEFile, null).name());

        // Test with UTF-16 LE BOM
        String utf16LEFile = "test/data/util/file-HTMLUtils-utf16_le_with_bom.html";
        assertEquals("UTF-16LE", detectHtmlEncoding(utf16LEFile, null).name());

        // Test with UTF-8 BOM
        String utf8BomFile = "test/data/util/file-HTMLUtils-utf8_with_bom.html";
        assertEquals("UTF-8", detectHtmlEncoding(utf8BomFile, null).name());

        // Test with UTF-8 xml declaration.
        String utf8XmlFile = "test/data/util/file-HTMLUtils-utf8-xml-declaration.html";
        assertEquals("UTF-8", detectHtmlEncoding(utf8XmlFile, null).name());

        // Test with no BOM and default encoding provided
        String noBomFile = "test/data/util/file-HTMLUtils-no_header_no_bom.html";
        assertEquals("ISO-8859-1", detectHtmlEncoding(noBomFile, "ISO-8859-1").name());

        // Test with no BOM and no default encoding
        assertEquals("UTF-8", detectHtmlEncoding(noBomFile, null).name());
    }

    @Test
    public void testDetectHTMLEncodingSpecialCase() {
        // Test with charset="x-user-defined"
        String userDefinedFile = "test/data/util/file-HTMLUtils-x-user-defined-charset.html";
        assertEquals("windows-1252", detectHtmlEncoding(userDefinedFile, null).name());

        // Test with content="x-user-defined"
        String userDefined2File = "test/data/util/file-HTMLUtils-x-user-defined-content.html";
        assertEquals("windows-1252", detectHtmlEncoding(userDefined2File, null).name());

        // Test with content="UTF16-BE"
        String utf16BE2File = "test/data/util/file-HTMLUtils-utf16_be-charset.html";
        assertEquals("UTF-8", detectHtmlEncoding(utf16BE2File, null).name());

        String windows1252File = "test/data/util/file-HTMLUtils-windows-1252.html";
        assertEquals("windows-1252", detectHtmlEncoding(windows1252File, "windows-1252").name());
    }

    @Test
    public void testDetectHTMLEncodingWindows1252() {
        assumeTrue(System.getProperty("os.name").toLowerCase().contains("windows"));
        assumeTrue(Charset.defaultCharset().name().equalsIgnoreCase("windows-1252"));

        String windows1252File = "test/data/util/file-HTMLUtils-windows-1252.html";
        assertEquals(Charset.defaultCharset().name(), detectHtmlEncoding(windows1252File, null).name());
    }
}
