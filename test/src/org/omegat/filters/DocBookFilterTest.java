package org.omegat.filters;

import org.omegat.filters3.xml.docbook.DocBookFilter;

public class DocBookFilterTest extends TestFilterBase {
    public void testParse() throws Exception {
	parse(new DocBookFilter(), "test/data/filters/docBook/file-DocBookFilter.xml");
    }
}
