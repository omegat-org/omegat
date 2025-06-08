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
package org.omegat.util.html;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EntityUtilTest {
    private EntityUtil entityUtil;

    @Before
    public void setUp() {
        entityUtil = new EntityUtil();
    }

    @Test
    public void testEntitiesToCharsNamedEntities() {
        // Named entities
        assertEquals("<", entityUtil.entitiesToChars("&lt;"));
        assertEquals(">", entityUtil.entitiesToChars("&gt;"));
        assertEquals("&", entityUtil.entitiesToChars("&amp;"));
        assertEquals("\"", entityUtil.entitiesToChars("&quot;"));
    }

    @Test
    public void testEntitiesToCharsSpecialCharacters() {
        // Latin Extended-A
        assertEquals("Œ", entityUtil.entitiesToChars("&OElig;"));
        assertEquals("œ", entityUtil.entitiesToChars("&oelig;"));
        assertEquals("Š", entityUtil.entitiesToChars("&Scaron;"));
        assertEquals("š", entityUtil.entitiesToChars("&scaron;"));
        assertEquals("Ÿ", entityUtil.entitiesToChars("&Yuml;"));
    }

    @Test
    public void testEntitiesToCharsNumericEntities() {
        assertEquals("\"", entityUtil.entitiesToChars("&#34;"));
        assertEquals("\"", entityUtil.entitiesToChars("&#x22;"));
        assertEquals("©", entityUtil.entitiesToChars("&#169;"));
    }

    @Test
    public void testEntitiesToCharsInvalid() {
        // Invalid or unsupported entities
        assertEquals("&invalid;", entityUtil.entitiesToChars("&invalid;"));
        assertEquals("&;", entityUtil.entitiesToChars("&;"));
        assertEquals("& #;", entityUtil.entitiesToChars("& #;")); // malformed
    }

    @Test
    public void testCharsToEntitiesBasicEntities() {
        assertEquals("&lt;", entityUtil.charsToEntities("<", "UTF-8", Collections.emptyList()));
        assertEquals("&gt;", entityUtil.charsToEntities(">", "UTF-8", Collections.emptyList()));
        assertEquals("&amp;", entityUtil.charsToEntities("&", "UTF-8", Collections.emptyList()));
        assertEquals("&nbsp;", entityUtil.charsToEntities("\u00A0", "UTF-8", Collections.emptyList()));
    }

    @Test
    public void testCharsToEntitiesProtectedEntities() {
        List<String> protectedEntities = Arrays.asList("<b0>", "</b0>", "<c>", "</c>", "<u1>", "</u1>");
        assertEquals("Le gros <u1>chat</u1> <c>test</c> &amp; <b0>noir</b0> dors", entityUtil.charsToEntities(
                "Le gros <u1>chat</u1> <c>test</c> & <b0>noir</b0> dors", "UTF-8", protectedEntities));
    }

}
