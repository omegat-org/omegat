package org.omegat.filters;

import java.util.List;

import org.omegat.filters2.html2.HTMLFilter2;

public class HTMLFilter2Test extends TestFilterBase{
    public void testParse() throws Exception {
	List<String> entries=parse(new HTMLFilter2(), "test/data/filters/html/file-HTMLFilter2.html");
	assertEquals(entries.size(), 2);
        assertEquals("This is first line.",entries.get(0));
        assertEquals("This is second line.",entries.get(1));
    }
}
