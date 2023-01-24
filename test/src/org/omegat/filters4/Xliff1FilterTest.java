/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2022 Thomas Cordonnier
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.ITranslateCallback;
import org.omegat.filters4.xml.xliff.Xliff1Filter;
import org.omegat.util.Language;

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
        assertTrue(exception.getMessage().contains("Missing attribute"));
    }

    @Test
    public void testBilingual() throws Exception {
        Map<String,String> result = new HashMap<>();
        Map<String,String> legacy = new HashMap<>();

        // Test that we correctly read translation
        parse2(new Xliff1Filter(), "test/data/filters/xliff/filters4-xliff1/en-xx.xlf", result, legacy);
        assertEquals("bar", result.get("foo"));
    }

    @Test
    public void testKey() throws Exception {
        List<ParsedEntry> entries = parse3(new Xliff1Filter(), "test/data/filters/xliff/filters4-xliff1/en-xx.xlf",
            java.util.Collections.emptyMap());
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
        filter.translateFile(new File("test/data/filters/xliff/filters4-xliff1/en-xx.xlf"), 
            outFile, java.util.Collections.emptyMap(), context,
                new ITranslateCallback() {
                    public String getTranslation(String id, String source, String path) {
                        if ("Should translate in result.".equals(source)) {
                            return "Devrait traduire dans le r\u00E9sultat.";
                        }
                        return null; // not translated
                    }

                    public String getTranslation(String id, String source) {
                        return getTranslation(id,source,"");
                    }

                    public void linkPrevNextSegments() {
                    }

                    public void setPass(int pass) {
                    }
                });
        // Check that it correctly translates
        List<ParsedEntry> entries = parse3(filter, outFile.getCanonicalPath(),
            java.util.Collections.emptyMap());
        // entry translated in the source file, not in the Callback
        assertEquals("bar", entries.get(1).translation);
        // entry translated in the callback, not in the source file
        assertEquals("Devrait traduire dans le r\u00E9sultat.", entries.get(2).translation); 
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
}
