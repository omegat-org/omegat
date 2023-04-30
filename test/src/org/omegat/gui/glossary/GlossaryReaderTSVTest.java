/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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
package org.omegat.gui.glossary;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.omegat.core.TestCore;

/**
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public class GlossaryReaderTSVTest extends TestCore {
    @Test
    public void testRead() throws Exception {
        List<GlossaryEntry> g = GlossaryReaderTSV.read(new File(
                "test/data/glossaries/test.tab"), false);
        assertEquals(2, g.size());
        assertEquals(g.get(0).getSrcText(), "kde");
        assertEquals(g.get(0).getLocText(), "koo moo");
        assertEquals(g.get(1).getSrcText(), "question");
        assertEquals(g.get(1).getLocText(), "qqqqq");

        g = GlossaryReaderTSV.read(new File(
                "test/data/glossaries/testUTF16LE.txt"), false);
        assertEquals(2, g.size());
        assertEquals(g.get(0).getSrcText(), "UTF");
        assertEquals(g.get(0).getLocText(), "Unicode Transformation Format");
        assertEquals(g.get(0).getCommentText(), "Comment #1");
        assertEquals(g.get(1).getSrcText(), "LE");
        assertEquals(g.get(1).getLocText(), "Little Endian");
        assertEquals(g.get(1).getCommentText(), "Comment #2");
    }

    @Test
    public void testCharset() throws Exception {
        // TODO
    }
}
