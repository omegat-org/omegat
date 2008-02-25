package org.omegat.filters;

import org.omegat.filters3.xml.xhtml.XHTMLFilter;

public class XHTMLFilterTest extends TestFilterBase {
    public void testParse() throws Exception {
        parse(new XHTMLFilter(), "test/data/filters/xhtml/file-XHTMLFilter.html");
    }

    public void testTranslate() throws Exception {
        //translateXML(new XHTMLFilter(), "test/data/filters/xhtml/file-XHTMLFilter.html");
    }
}
