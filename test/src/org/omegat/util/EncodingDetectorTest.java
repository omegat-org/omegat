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
import static org.omegat.util.EncodingDetector.detectHtmlEncoding;

public class EncodingDetectorTest {

    @Test
    public void testDetectHTMLEncoding() {
        // Test with UTF-8
        String utf8File = "test/data/util/file-HTMLUtils-utf8.html";
        assertEquals("UTF-8", detectHtmlEncoding(utf8File, null).name());

        // Test with UTF-16 BE BOM
        String utf16BEFile = "test/data/util/file-HTMLUtils-utf16_be_with_bom.html";
        assertEquals("UTF-16BE", detectHtmlEncoding(utf16BEFile, null).name());

        // Test with UTF-16 LE BOM
        String utf16LEFile = "test/data/util/file-HTMLUtils-utf16_le_with_bom.html";
        assertEquals("UTF-16LE", detectHtmlEncoding(utf16LEFile, null).name());

        // Test with no BOM and default encoding provided
        String noBomFile = "test/data/util/file-HTMLUtils-no_header_no_bom.html";
        assertEquals("ISO-8859-1", detectHtmlEncoding(noBomFile, "ISO-8859-1").name());

        // Test with no BOM and no default encoding
        assertEquals(Charset.defaultCharset().name(), detectHtmlEncoding(noBomFile, null).name());
    }
}
