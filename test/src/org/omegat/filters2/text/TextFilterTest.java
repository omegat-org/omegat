package org.omegat.filters2.text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.omegat.filters2.IParseCallback;

import junit.framework.TestCase;

public class TextFilterTest extends TestCase {
    public void testTextFilterParsing() throws Exception {
        final List<String> entries=new ArrayList<String>();
        TextFilter filter = new TextFilter();
        filter.setParseCallback(new IParseCallback() {
            public String processEntry(String entry) {
                entries.add(entry);
                return entry;
            }
        });
        filter.processFile(new File("test/data/filters/text/text1.txt"), null, null, null);
        assertEquals("First entry\r\n", entries.get(0));
    }
}
