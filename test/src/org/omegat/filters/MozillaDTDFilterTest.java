package org.omegat.filters;

import org.junit.Test;
import org.omegat.core.data.IProject;
import org.omegat.filters2.mozdtd.MozillaDTDFilter;

public class MozillaDTDFilterTest extends TestFilterBase {
    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/MozillaDTD/file.dtd";
        IProject.FileInfo fi = loadSourceFiles(new MozillaDTDFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("Title", "mainWindow.title", null, null, null, null);
        checkMulti("File", "fileMenu.label", null, null, null, null);
        checkMulti("Edit", "editMenu.label", null, null, null, null);
        checkMultiEnd();
    }
}
