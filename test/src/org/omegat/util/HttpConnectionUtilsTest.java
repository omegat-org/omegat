/*******************************************************************************
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura
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
 ******************************************************************************/

package org.omegat.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HttpConnectionUtilsTest {

    @Test
    public void testDecodeURLs() {
        String str = "https://fr.wikipedia.org/wiki/Science_du_syst%C3%A8me_Terre";
        assertEquals("https://fr.wikipedia.org/wiki/Science_du_système_Terre",
                HttpConnectionUtils.decodeHttpURLs(str));
    }

    @Test
    public void testDecodeURLsInText() {
        String str = "1. https://fr.wikipedia.org/wiki/Science_du_syst%C3%A8me_Terre";
        assertEquals("1. https://fr.wikipedia.org/wiki/Science_du_système_Terre",
                HttpConnectionUtils.decodeHttpURLs(str));
    }

    @Test
    public void testDecodeURLsMultipleLines() {
        String str = "1. https://google.com/\n2. bar\n"
                + "3. https://fr.wikipedia.org/wiki/Science_du_syst%C3%A8me_Terre";
        assertEquals(
                "1. https://google.com/\n2. bar\n"
                        + "3. https://fr.wikipedia.org/wiki/Science_du_système_Terre",
                HttpConnectionUtils.decodeHttpURLs(str));
    }

    @Test
    public void testEncodeURLs() {
        String base = "https://fr.wikipedia.org/";
        String path = "wiki/Science_du_système_Terre";
        String query = "?query=search&lang=en";
        assertEquals("https://fr.wikipedia.org/", HttpConnectionUtils.encodeHttpURLs(base));
        assertEquals("https://fr.wikipedia.org/wiki/Science_du_syst%C3%A8me_Terre",
                HttpConnectionUtils.encodeHttpURLs(base + path));
        assertEquals("https://fr.wikipedia.org/wiki/Science_du_syst%C3%A8me_Terre?query=search&lang=en",
                HttpConnectionUtils.encodeHttpURLs(base + path + query));
    }
}
