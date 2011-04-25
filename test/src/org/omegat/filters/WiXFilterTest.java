package org.omegat.filters;

import org.junit.Test;
import org.omegat.core.data.IProject;
import org.omegat.filters3.xml.wix.WiXFilter;

public class WiXFilterTest extends TestFilterBase {
    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/Wix/fr-fr.wxl";
        IProject.FileInfo fi = loadSourceFiles(new WiXFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("1036", "LANG", null, null, null, null);
        checkMulti("This installation requires XXX. Setup will now exit.", "XXXRequired", null, null, null,
                null);
        checkMultiEnd();
    }
}
