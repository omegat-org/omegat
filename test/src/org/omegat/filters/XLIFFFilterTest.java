/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008-2013 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.filters;

import java.io.File;
import java.util.TreeMap;

import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.filters2.ITranslateCallback;
import org.omegat.filters3.xml.xliff.XLIFFDialect;
import org.omegat.filters3.xml.xliff.XLIFFFilter;
import org.omegat.filters3.xml.xliff.XLIFFOptions;
import org.omegat.util.FileUtil;

public class XLIFFFilterTest extends TestFilterBase {
    XLIFFFilter filter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        filter = new XLIFFFilter();
        XLIFFDialect dialect = (XLIFFDialect) filter.getDialect();
        dialect.defineDialect(new XLIFFOptions(new TreeMap<String, String>()));
    }

    @Test
    public void testParse() throws Exception {
        parse(filter, "test/data/filters/xliff/file-XLIFFFilter.xlf");
    }

    @Test
    public void testTranslate() throws Exception {
        translateXML(filter, "test/data/filters/xliff/file-XLIFFFilter.xlf");
    }

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/xliff/file-XLIFFFilter.xlf";
        IProject.FileInfo fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMulti("tr1=This is test", null, null, "", "tr2=test2", null);
        checkMulti("tr2=test2", null, null, "tr1=This is test", "", null);
        checkMultiEnd();
    }

    @Test
    public void testTags() throws Exception {
        String f = "test/data/filters/xliff/file-XLIFFFilter-tags.xlf";
        IProject.FileInfo fi = loadSourceFiles(filter, f);

        SourceTextEntry ste;
        checkMultiStart(fi, f);
        checkMultiNoPrevNext("Link to <m0>http://localhost</m0>.", null, null, null); // #1988732
        checkMultiNoPrevNext("About <b0>Gandalf</b0>", null, null, null); // #1988732
        checkMultiNoPrevNext("<i0>Tags</i0> translation zz<i1>2</i1>z <b2>-NONTRANSLATED", null, null, null);
        checkMultiNoPrevNext("one <a0> two </b1> three <c2> four </d3> five", null, null, null);
        ste = checkMultiNoPrevNext("About <m0>Gandalf</m0> and <m1>other</m1>.", null, null, null);
        assertEquals(3, ste.getProtectedParts().getParts().length);
        assertEquals("<mrk mtype=\"other\">", ste.getProtectedParts().getDetail("<m1>"));
        assertEquals("</mrk>", ste.getProtectedParts().getDetail("</m1>"));
        assertEquals("<mrk mtype=\"protected\">Gandalf</mrk>", ste.getProtectedParts().getDetail("<m0>Gandalf</m0>"));
        checkMultiNoPrevNext("one <o0>two</o0> three", null, null, null);
        checkMultiNoPrevNext("one <t0/> three", null, null, null);
        checkMultiNoPrevNext("one <w0/> three", null, null, null);
        checkMultiNoPrevNext("Nested tags: before <g0><g1><x2/></g1></g0> after", null, null, null);
        checkMultiEnd();

        File inFile = new File("test/data/filters/xliff/file-XLIFFFilter-tags.xlf");
        filter.translateFile(inFile, outFile, new TreeMap<String, String>(), context,
                new ITranslateCallback() {
                    public String getTranslation(String id, String source, String path) {
                        return source.replace("NONTRANSLATED", "TRANSLATED");
                    }

                    public String getTranslation(String id, String source) {
                        return source.replace("NONTRANSLATED", "TRANSLATED");
                    }

                    public void linkPrevNextSegments() {
                    }

                    public void setPass(int pass) {
                    }
                });
        File trFile = new File(outFile.getPath() + "-translated");
        String text = FileUtil.readTextFile(inFile);
        text = text.replace("NONTRANSLATED", "TRANSLATED");
        FileUtil.writeTextFile(trFile, text);
        compareXML(trFile, outFile);
    }

    @Test
    public void testTagOptimization() throws Exception {
        String f = "test/data/filters/xliff/file-XLIFFFilter-tags-optimization.xlf";

        Core.getFilterMaster().getConfig().setRemoveTags(false);
        IProject.FileInfo fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMultiNoPrevNext("<b0>The text of a segment<b1>.<b2>", null, null, null);
        checkMultiNoPrevNext("<b0>The text of a segment<b1>.<b2><b3><b4>", null, null, null);
        checkMultiNoPrevNext("<b0>Link to a <a1>reference</a1></b0>", null, null, null);
        checkMultiEnd();
        translateXML(filter, f);

        Core.getFilterMaster().getConfig().setRemoveTags(true);
        fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMultiNoPrevNext("The text of a segment<b0>.", null, null, null);
        checkMultiNoPrevNext("The text of a segment<b0>.", null, null, null);
        checkMultiNoPrevNext("Link to a <a0>reference</a0>", null, null, null);
        checkMultiEnd();
        translateXML(filter, f);
    }
}
