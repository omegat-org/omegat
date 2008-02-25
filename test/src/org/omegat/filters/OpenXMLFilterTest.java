package org.omegat.filters;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.omegat.filters3.xml.openxml.OpenXMLFilter;

public class OpenXMLFilterTest extends TestFilterBase {
    public void testParse() throws Exception {
        List<String> entries = parse(new OpenXMLFilter(), "test/data/filters/openXML/file-OpenXMLFilter.docx");
        assertEquals(2, entries.size());
        assertEquals("This is first line.", entries.get(0));
        assertEquals("This is second line.", entries.get(1));
    }

    public void testTranslate() throws Exception {
        File in = new File("test/data/filters/openXML/file-OpenXMLFilter.docx");
        translate(new OpenXMLFilter(), in.getPath());

        for (String f : new String[] { "word/document.xml" }) {
            compareXML(new URL("jar:file:/" + in.getAbsolutePath() + "!/" + f), new URL("jar:file:/"
                    + outFile.getAbsolutePath() + "!/" + f));
        }
    }
}
