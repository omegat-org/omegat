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
        String str2 = "2. https://ja.wikipedia.org/wiki/2024%E5%B9%B4%E3%81%AE%E3%82%AB%E3%82%BF%E3%83%BC%E3"
                + "%83%AB%E3%82%B0%E3%83%A9%E3%83%B3%E3%83%97%E3%83%AA"
                + "_%28%E3%83%AD%E3%83%BC%E3%83%89%E3%83%AC%E3%83%BC%E3%82%B9%29 参照";
        assertEquals("2. https://ja.wikipedia.org/wiki/2024年のカタールグランプリ_(ロードレース) 参照",
                HttpConnectionUtils.decodeHttpURLs(str2));
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
        String bracket = "https://fr.wikipedia.org/wiki/Doughnut_(modèle_économique)";
        assertEquals("https://fr.wikipedia.org/wiki/Doughnut_%28mod%C3%A8le_%C3%A9conomique%29",
                HttpConnectionUtils.encodeHttpURLs(bracket));
        String multibyte = "2. https://ja.wikipedia.org/wiki/2024年のカタールグランプリ_(ロードレース)";
        assertEquals(
                "2. https://ja.wikipedia.org/wiki/2024%E5%B9%B4%E3%81%AE%E3%82%AB%E3%82%BF%E3%83%BC%E3"
                        + "%83%AB%E3%82%B0%E3%83%A9%E3%83%B3%E3%83%97%E3%83%AA"
                        + "_%28%E3%83%AD%E3%83%BC%E3%83%89%E3%83%AC%E3%83%BC%E3%82%B9%29",
                HttpConnectionUtils.encodeHttpURLs(multibyte));
    }
}
