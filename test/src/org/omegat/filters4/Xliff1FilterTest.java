/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2022 Thomas Cordonnier
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.omegat.core.data.IProject;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.TranslationException;
import org.omegat.filters4.xml.xliff.Xliff1Filter;

public class Xliff1FilterTest extends org.omegat.filters.TestFilterBase {
    @Test
    public void testParse() throws Exception {
        List<String> entries = parse(new Xliff1Filter(), "test/data/filters/xliff/filters4-xliff1/en-xx.xlf");
        assertEquals(7, entries.size()); // the file contains 8 entries but the one with "translate=no" is ignored
        assertEquals("This is the source text.", entries.get(0));
        assertEquals("foo", entries.get(1));
    }
    
    @Test // checks that file wihout ID will fail (REQUIRED in the specification)
    public void testParseMissingId() throws Exception {
        Exception exception = assertThrows(TranslationException.class, () -> {
            // Try with a file from filters3 : they are not conform to specification so it should fail
            parse(new Xliff1Filter(), "test/data/filters/xliff/filters3/file-XLIFFFilter.xlf");        
        });
        System.err.println("Parsing file where trans-unit/@id is missing returns: "
            + exception.getClass() + ": " + exception.getMessage());
        // Exception says that attribute 'id' is missing in <trans-unit>
        // Since it is localized we check for 'id' and <trans-unit>
        // Note: hope translators will use '{0}' and <{1}>
        assertTrue(exception.getMessage().contains("'id'"));
        assertTrue(exception.getMessage().contains("<trans-unit>"));
    }

    @Test
    public void testBilingual() throws Exception {
        Map<String, String> result = new HashMap<>();
        Map<String, String> legacy = new HashMap<>();

        // Test that we correctly read translation
        parse2(new Xliff1Filter(), "test/data/filters/xliff/filters4-xliff1/en-xx.xlf", result, legacy);
        assertEquals("bar", result.get("foo"));
    }

    @Test
    public void testKey() throws Exception {
        List<ParsedEntry> entries = parse3(new Xliff1Filter(), "test/data/filters/xliff/filters4-xliff1/en-xx.xlf",
            Collections.emptyMap());
        ParsedEntry firstEntry = entries.get(0);
        assertEquals(firstEntry.id, "example_01");
        assertEquals(firstEntry.path, "//interface.po");  // path is built with file name
        ParsedEntry secondEntry = entries.get(1);
        assertEquals("foo", secondEntry.source);
        assertEquals("bar", secondEntry.translation);
        ParsedEntry lastEntry = entries.get(entries.size() - 1);
        assertEquals("Added_1", lastEntry.id);  // next entry Added_2 is ignored as translate=no
        assertEquals("//interface.po/Group 3", lastEntry.path);  // path is built with file name and groups
    }

    @Test
    public void testTranslation() throws Exception {
        Xliff1Filter filter = new Xliff1Filter();
        translate(filter, "test/data/filters/xliff/filters4-xliff1/en-xx.xlf");
        // Check that it correctly translates
        List<ParsedEntry> entries = parse3(filter, outFile.getCanonicalPath(),
            Collections.emptyMap());
        // entry translated in the source file, not in the Callback
        assertEquals("bar", entries.get(1).translation);
        // entry translated in the callback, not in the source file
        assertEquals("Devrait traduire dans le r\u00E9sultat.", entries.get(2).translation); 
    }
    
    @Test
    public void testKeepProperties() throws Exception {
        Xliff1Filter filter = new Xliff1Filter();
        String f = "test/data/filters/xliff/file-XLIFFFilter-state-final.xlf";
        org.omegat.core.data.IProject.FileInfo fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMulti("This is test", "1", "//text.txt", null, null, null);
        assertEquals(fi.entries.get(fiCount - 1).getRawProperties().length, 0);
        checkMultiProps("test2", "2", "//text.txt", null, null,"LOCKED", "xliff final");
        checkMultiEnd();
        
        translate(filter, new File(f).getPath());
        fi = loadSourceFiles(filter, outFile.toString());
        
        checkMultiStart(fi, outFile.toString());
        checkMulti("This is test", "1", "//text.txt", null, null, null);
        assertEquals(fi.entries.get(fiCount - 1).getRawProperties().length, 0);
        checkMultiProps("test2", "2", "//text.txt", null, null,"LOCKED", "xliff final");
        checkMultiEnd();
    }    
    
    
    /**
     * Test with live example of XLIFF version 1.2, as similar with exported
     * file from Crowdin service.
     */
    @Test
    public void testTranslationRFE1506() throws Exception {
        Xliff1Filter filter = new Xliff1Filter();
        org.omegat.filters.XLIFFFilterTest.checkXLiffTranslationRFE1506(filter, context, outFile, false);
        // Actually option "NeedsTranslate" is not yet implemented
        //org.omegat.filters.XLIFFFilterTest.checkXLiffTranslationRFE1506(filter, context, outFile, true);
    }

    @Override
    protected void translate(AbstractFilter filter, String filename) throws Exception {
        translate(filter, filename, Collections.emptyMap(),
                Collections.singletonMap("Should translate in result.", "Devrait traduire dans le r\u00E9sultat."));
    }

    @Test
    public void testBugs418() throws Exception {
        Xliff1Filter filter = new Xliff1Filter();
        translateXML(filter,
                "test/data/filters/xliff/filters3/file-XLIFFFilter-cdata-bugs418.xlf");
    }

    @Test
    public void testBugs1247() throws Exception {
        Xliff1Filter filter = new Xliff1Filter();
        translateXML(filter,
                "test/data/filters/xliff/filters4-xliff1/file-XLIFFFilter1-multiple-file-tag.xlf");
    }

    @Test
    public void testBugs1247_2() throws Exception {
        String f = "test/data/filters/xliff/filters4-xliff1/file-XLIFFFilter1-multiple-file-tag.xlf";
        Xliff1Filter filter = new Xliff1Filter();
        IProject.FileInfo fi = loadSourceFiles(filter, f);
        checkMultiStart(fi, f);
        checkMultiNoPrevNext("This is text3", "id1", "//text3.txt", null);
        checkMultiNoPrevNext("test2", "id2", "//text.txt", null);
        checkMultiEnd();
    }


}
