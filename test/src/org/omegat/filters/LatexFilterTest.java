/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
               2023 Hiroshi Miura
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

package org.omegat.filters;

import org.junit.Test;
import org.omegat.core.data.IProject;
import org.omegat.filters2.latex.LatexFilter;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class LatexFilterTest extends TestFilterBase {

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/Latex/latexexample.tex";
        IProject.FileInfo fi = loadSourceFiles(new LatexFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("LaTeX Typesetting By Example", null, null, "",
                "Phil Farrell<br0> Stanford University School of Earth Sciences", null);
    }

    @Test
    public void testLoadItemize() throws Exception {
        String f = "test/data/filters/Latex/file-latex-items.tex";
        IProject.FileInfo fi = loadSourceFiles(new LatexFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("LaTeX Itemize example", null, null, "", "Itemize", null);
        checkMulti("Itemize", null, null, "LaTeX Itemize example", "<r0> INTERRUTTORE GENERALE ON/OFF (I/0)",
                null);
        checkMulti("<r0> INTERRUTTORE GENERALE ON/OFF (I/0)", null, null, "Itemize",
                "<r0> SPIA PRESENZA TENSIONE", null);
        checkMulti("<r0> SPIA PRESENZA TENSIONE", null, null, "<r0> INTERRUTTORE GENERALE ON/OFF (I/0)",
                "<r0> SPIA PREALLARME", null);
        checkMulti("<r0> SPIA PREALLARME", null, null, "<r0> SPIA PRESENZA TENSIONE",
                "<r0> PULPITO/PANNELLO DI COMANDO", null);
        checkMulti("<r0> PULPITO/PANNELLO DI COMANDO", null, null, "<r0> SPIA PREALLARME", "", null);
        checkMultiEnd();
    }

    @Test
    public void testParseItemize() throws Exception {
        translate(new LatexFilter(), "test/data/filters/Latex/file-latex-items.tex");
        compareBinary(new File("test/data/filters/Latex/file-latex-items-exp.tex"), outFile);
    }

    @Test
    public void testLoadComments() throws Exception {
        String f = "test/data/filters/Latex/file-latex-comments.tex";
        IProject.FileInfo fi = loadSourceFiles(new LatexFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("LaTeX Comment example", null, null, "", "Comment", null);
        checkMulti("Comment", null, null, "LaTeX Comment example", "This is a text with inline comments.",
                null);
        checkMulti("This is a text with inline comments.", null, null, "Comment", "", null);
        checkMultiEnd();
    }

    @Test
    public void testArticle() throws Exception {
        translate(new LatexFilter(), "test/data/filters/Latex/test-article.tex");
        compareBinary(new File("test/data/filters/Latex/test-article-exp.tex"), outFile);
    }

    @Test
    public void testBugOverlap() throws Exception {
        String f = "test/data/filters/Latex/bug_overlap.tex";
        List<String> entries = parse(new LatexFilter(), f);
        assertEquals(1, entries.size());
        String target = entries.get(0);
        assertTrue("ends with unwanted string: " + target.substring(target.length() - 10),
                target.matches("We <u\\d> it, and <u\\d> is compressed. Then we <u\\d>. "));
    }
    /**
     * Verifies that verbatim environments (verbatim, minted, lstlisting, etc.)
     * are preserved as-is and their content is NOT extracted as translatable segments.
     */
    @Test
    public void testVerbatimPreserved() throws Exception {
        String f = "test/data/filters/Latex/latexverbatim.tex";
        List<String> segments = parse(new LatexFilter(), f);

        // Normal text should be extracted as segments
        assertTrue("Normal text before verbatim should be a segment",
                segments.stream().anyMatch(s -> s.contains("Hello World")));
        assertTrue("Normal text after verbatim should be a segment",
                segments.stream().anyMatch(s -> s.contains("More text after verbatim")));

        // Verbatim content must NOT appear as separate segments
        assertFalse("C include directive should NOT be a segment",
                segments.stream().anyMatch(s -> s.contains("#include")));
        assertFalse("C main function should NOT be a segment",
                segments.stream().anyMatch(s -> s.contains("int main")));
        assertFalse("printf call should NOT be a segment",
                segments.stream().anyMatch(s -> s.contains("printf")));
        assertFalse("Special chars should NOT be a segment",
                segments.stream().anyMatch(s -> s.contains("return 0")));
        assertFalse("minted content should NOT be a segment",
                segments.stream().anyMatch(s -> s.contains("public class Test")));
        assertFalse("minted code should NOT be a segment",
                segments.stream().anyMatch(s -> s.contains("test$pecial")));
    }
}
