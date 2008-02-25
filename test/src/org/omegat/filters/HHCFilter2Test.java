package org.omegat.filters;

import org.omegat.filters2.hhc.HHCFilter2;

public class HHCFilter2Test extends TestFilterBase {
    public void testParse() throws Exception {
        parse(new HHCFilter2(), "test/data/filters/hhc/file-HHCFilter2.hhc");
        parse(new HHCFilter2(), "test/data/filters/hhc/file-HHCFilter2-Contents file.hhc");
        parse(new HHCFilter2(), "test/data/filters/hhc/file-HHCFilter2-Index file.hhk");
    }

    public void testTranslate() throws Exception {
        translateText(new HHCFilter2(), "test/data/filters/hhc/file-HHCFilter2.hhc");
        translateText(new HHCFilter2(), "test/data/filters/hhc/file-HHCFilter2-Contents file.hhc");
        translateText(new HHCFilter2(), "test/data/filters/hhc/file-HHCFilter2-Index file.hhk");
    }
}
