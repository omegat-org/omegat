package org.omegat.filters;

import java.util.List;

import org.omegat.filters3.xml.opendoc.OpenDocFilter;

public class OpenDocFilterTest extends TestFilterBase{
    public void testParse() throws Exception {
	List<String> entries=parse(new OpenDocFilter(), "test/data/filters/openDoc/file-OpenDocFilter.odt");
	assertEquals(2, entries.size());
        assertEquals("This is first line.",entries.get(0));
        assertEquals("This is second line.",entries.get(1));
    }
}
