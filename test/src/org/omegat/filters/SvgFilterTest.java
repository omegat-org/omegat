package org.omegat.filters;

import org.omegat.core.data.IProject;
import org.omegat.filters3.xml.svg.SvgFilter;

public class SvgFilterTest extends TestFilterBase {

    public void testLoad() throws Exception {
        String f = "test/data/filters/SVG/Neural_network_example.svg";
        IProject.FileInfo fi = loadSourceFiles(new SvgFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("image/svg+xml", null, null, "", "<t0><t1>input</t1></t0><t2><t3>layer</t3></t2>", null);
        checkMulti("<t0><t1>input</t1></t0><t2><t3>layer</t3></t2>", null, null, "image/svg+xml",
                "<t0><t1>hidden</t1></t0><t2><t3>layer</t3></t2>", null);
        checkMulti("<t0><t1>hidden</t1></t0><t2><t3>layer</t3></t2>", null, null,
                "<t0><t1>input</t1></t0><t2><t3>layer</t3></t2>",
                "<t0><t1>output</t1></t0><t2><t3>layer</t3></t2>", null);
    }
}
