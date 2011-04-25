package org.omegat.filters;

import org.junit.Test;
import org.omegat.core.data.IProject;
import org.omegat.filters2.rc.RcFilter;

public class RcFilterTest extends TestFilterBase {
    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/Rc/prog.rc";
        IProject.FileInfo fi = loadSourceFiles(new RcFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("&File", "103/512", null, null, null, null);
        checkMulti("&Import...", "103/601", null, null, null, null);
    }
}
