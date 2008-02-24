package org.omegat.filters;

import org.omegat.filters2.po.PoFilter;

public class POFilterTest extends TestFilterBase {
    public void testParse() throws Exception {
	parse(new PoFilter(), "test/data/filters/po/file-POFilter-be.po");
    }
    public void testTranslate() throws Exception {
	//translateText(new PoFilter(), "test/data/filters/po/file-POFilter-be.po");
    }
}
