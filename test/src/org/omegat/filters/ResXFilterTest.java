package org.omegat.filters;

import org.omegat.core.data.IProject;
import org.omegat.filters3.xml.resx.ResXFilter;

public class ResXFilterTest extends TestFilterBase {

    public void testLoad() throws Exception {
        String f = "test/data/filters/ResX/Resources.resx";
        IProject.FileInfo fi = loadSourceFiles(new ResXFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("This is a text displayed in the UI.", "InfoExperimentStoppingMessage", null, null, null,
                "This is a comment. It should not be displayed to the translator.");
        checkMulti("One more text", "InfoExperimentStoppingMessage2", null, null, null, "Second comment");
        checkMultiEnd();
    }
}
