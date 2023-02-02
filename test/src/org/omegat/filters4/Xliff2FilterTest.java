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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.omegat.filters2.ITranslateCallback;
import org.omegat.filters4.xml.xliff.Xliff2Filter;

public class Xliff2FilterTest extends org.omegat.filters.TestFilterBase {
    @Test
    public void testParse() throws Exception {
        List<String> entries = parse(new Xliff2Filter(), "test/data/filters/xliff/filters4-xliff2/ex.9.5.xlf");
        assertEquals(7, entries.size()); // the file contains 8 entries but the one with "translate=no" is ignored
        assertEquals("Birds in Oregon", entries.get(0));
        assertEquals("Oregon is a mostly temperate state. There are\n"
            + "            many different kinds of birds that thrive", entries.get(1));
    }

    @Test
    public void testBilingual() throws Exception {
        Map<String,String> result = new HashMap<String,String>();
        Map<String,String> legacy = new HashMap<String,String>();

        parse2(new Xliff2Filter(), "test/data/filters/xliff/filters4-xliff2/ex.9.5.xlf", result, legacy);
        assertEquals("<t0>Oiseaux de haute altitude", result.get("<t0>High Altitude Birds"));
    }
    
    @Test
    public void testKey() throws Exception {
        List<ParsedEntry> entries = parse3(new Xliff2Filter(), "test/data/filters/xliff/filters4-xliff2/ex.9.5.xlf",
            java.util.Collections.emptyMap());
        ParsedEntry firstEntry = entries.get(0);
        assertEquals("1", firstEntry.id); // <segment> has no id, not mandatory
        assertEquals("//groups/N65541xdocument/N65541bxmarksection-1/title-2", firstEntry.path);
        ParsedEntry secondEntry = entries.get(2);
        assertEquals("<t0>High Altitude Birds", secondEntry.source);
        assertEquals("<t0>Oiseaux de haute altitude", secondEntry.translation);            
    }

    @Test
    public void testTranslation() throws Exception {
        Xliff2Filter filter = new Xliff2Filter();
        filter.translateFile(new File("test/data/filters/xliff/filters4-xliff2/ex.9.5.xlf"), 
            outFile, java.util.Collections.emptyMap(), context,
                new ITranslateCallback() {
                    public String getTranslation(String id, String source, String path) {
                        if ("Birds in Oregon".equals(source)) {
                            return "Oiseaux en Oregon";
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
        assertEquals("<t0>Oiseaux de haute altitude", entries.get(2).translation);
        // entry translated in the callback, not in the source file
        assertEquals("Oiseaux en Oregon", entries.get(0).translation); 
    }
    
}
