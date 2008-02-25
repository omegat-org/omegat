package org.omegat.filters;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.omegat.filters3.xml.opendoc.OpenDocFilter;

public class OpenDocFilterTest extends TestFilterBase {
    public void testParse() throws Exception {
        List<String> entries = parse(new OpenDocFilter(), "test/data/filters/openDoc/file-OpenDocFilter.odt");
        assertEquals(2, entries.size());
        assertEquals("This is first line.", entries.get(0));
        assertEquals("This is second line.", entries.get(1));
    }

    public void testTranslate() throws Exception {
        File in = new File("test/data/filters/openDoc/file-OpenDocFilter.odt");
        translate(new OpenDocFilter(), in.getPath());

        for (String f : new String[] { "content.xml", "styles.xml", "meta.xml" }) {
            compareXML(new URL("jar:file:/" + in.getAbsolutePath() + "!/" + f), new URL("jar:file:/"
                    + outFile.getAbsolutePath() + "!/" + f));
        }
    }
}
