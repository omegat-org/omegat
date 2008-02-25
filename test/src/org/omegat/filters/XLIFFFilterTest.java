package org.omegat.filters;

import org.omegat.filters3.xml.xliff.XLIFFFilter;

public class XLIFFFilterTest extends TestFilterBase {
    public void testParse() throws Exception {
        parse(new XLIFFFilter(), "test/data/filters/xliff/file-XLIFFFilter.xlf");
    }
    public void testTranslate() throws Exception {
        translateXML(new XLIFFFilter(), "test/data/filters/xliff/file-XLIFFFilter.xlf");
    }
}
