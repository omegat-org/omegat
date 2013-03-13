/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008-2013 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.filters;

import java.io.File;
import java.util.TreeMap;

import org.junit.Test;
import org.omegat.core.data.IProject;
import org.omegat.filters2.ITranslateCallback;
import org.omegat.filters3.xml.xliff.XLIFFFilter;
import org.omegat.util.FileUtil;

public class XLIFFFilterTest extends TestFilterBase {
    public void testParse() throws Exception {
        parse(new XLIFFFilter(), "test/data/filters/xliff/file-XLIFFFilter.xlf");
    }

    public void testTranslate() throws Exception {
        translateXML(new XLIFFFilter(), "test/data/filters/xliff/file-XLIFFFilter.xlf");
    }

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/xliff/file-XLIFFFilter.xlf";
        IProject.FileInfo fi = loadSourceFiles(new XLIFFFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("tr1=This is test", null, null, "", "tr2=test2", null);
        checkMulti("tr2=test2", null, null, "tr1=This is test", "", null);
        checkMultiEnd();
    }

    @Test
    public void testTags() throws Exception {
        String f = "test/data/filters/xliff/file-XLIFFFilter-tags.xlf";
        IProject.FileInfo fi = loadSourceFiles(new XLIFFFilter(), f);

        checkMultiStart(fi, f);
        checkMultiNoPrevNext("Link to <m0>http://localhost</m0>.", null, null, null); // #1988732
        checkMultiNoPrevNext("About <b0>Gandalf</b0>", null, null, null); // #1988732
        checkMultiNoPrevNext("<i0>Tags</i0> translation zz<i1>2</i1>z <b2>-NONTRANSLATED", null, null, null);
        checkMultiNoPrevNext("one <a0> two </b1> three <c2> four </d3> five", null, null, null);
        checkMultiEnd();

        File inFile = new File("test/data/filters/xliff/file-XLIFFFilter-tags.xlf");
        new XLIFFFilter().translateFile(inFile, outFile, new TreeMap<String, String>(), context,
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
}
