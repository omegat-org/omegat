/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 pierreldff
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

package org.omegat.filters2.latex;

import org.junit.Test;
import org.omegat.filters.TestFilterBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LatexFilterUnitTest extends TestFilterBase {

    @Test
    public void testParseBracedCommand() throws Exception {
        LatexFilter filter = new LatexFilter();

        // Valid \begin{env}
        assertEquals("verbatim", filter.parseBracedCommand("\\begin{verbatim}", "\\begin{"));
        assertEquals("verbatim*", filter.parseBracedCommand("\\begin{verbatim*}", "\\begin{"));

        // Valid \end{env}
        assertEquals("verbatim", filter.parseBracedCommand("\\end{verbatim}", "\\end{"));

        // No brace pair -> null
        assertNull(filter.parseBracedCommand("\\begin{verbatim", "\\begin{"));
        assertNull(filter.parseBracedCommand("\\begin", "\\begin{"));

        // Wrong prefix -> null
        assertNull(filter.parseBracedCommand("\\end{verbatim}", "\\begin{"));

        // No match at start
        assertNull(filter.parseBracedCommand("hello \\begin{verbatim}", "\\begin{"));
    }
}
