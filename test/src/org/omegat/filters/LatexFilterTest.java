package org.omegat.filters;

import org.omegat.core.data.IProject;
import org.omegat.filters2.latex.LatexFilter;

public class LatexFilterTest extends TestFilterBase {

    public void testLoad() throws Exception {
        String f = "test/data/filters/Latex/latexexample.tex";
        IProject.FileInfo fi = loadSourceFiles(new LatexFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("[11pt]{article}", null, null, "", "LaTeX Typesetting By Example", null);
        checkMulti("LaTeX Typesetting By Example", null, null, "[11pt]{article}",
                "Phil Farrell<br0> Stanford University School of Earth Sciences", null);
    }
}
