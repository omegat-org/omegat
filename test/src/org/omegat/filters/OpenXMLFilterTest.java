package org.omegat.filters;

import java.util.List;

import org.omegat.filters3.xml.openxml.OpenXMLFilter;

public class OpenXMLFilterTest extends TestFilterBase {
    public void testParse() throws Exception {
	List<String> entries = parse(new OpenXMLFilter(), "test/data/filters/openXML/file-OpenXMLFilter.docx");
	assertEquals(2, entries.size());
	assertEquals("This is first line.", entries.get(0));
	assertEquals("This is second line.", entries.get(1));
    }
}
