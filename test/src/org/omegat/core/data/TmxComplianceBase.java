/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
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

package org.omegat.core.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Before;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.ITranslateCallback;
import org.omegat.filters2.text.TextFilter;

/**
 * TMX Compliance tests as described on http://www.lisa.org/tmx/comp.htm
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public abstract class TmxComplianceBase extends TestCase {

    protected File outFile;

    @Before
    public void setUp() throws Exception {
        outFile = new File("build/testdata/OmegaT_test-" + getClass().getName() + "-" + getName());
        outFile.getParentFile().mkdirs();
        
        Segmenter.srx = SRX.getSRX();
    }

    protected void compareTexts(File f1, String charset1, File f2, String charset2) throws Exception {
        BufferedReader rd1 = new BufferedReader(new InputStreamReader(new FileInputStream(f1), charset1));
        BufferedReader rd2 = new BufferedReader(new InputStreamReader(new FileInputStream(f2), charset2));

        int ch;

        // BOM (byte order mark) bugfix
        rd1.mark(1);
        ch = rd1.read();
        if (ch != 0xFEFF) {
            rd1.reset();
        }
        rd2.mark(1);
        ch = rd2.read();
        if (ch != 0xFEFF) {
            rd2.reset();
        }

        String s1;
        while ((s1 = rd1.readLine()) != null) {
            String s2 = rd2.readLine();
            Assert.assertNotNull(s2);
            if (!s1.equals(s2)) {
                Assert.assertEquals(s1, s2);
            }
        }
        Assert.assertNull(rd2.readLine());

        rd1.close();
        rd2.close();
    }

    protected void translateTextUsingTmx(String fileTextIn, String inCharset, String fileTMX,
            String fileTextOut, String outCharset, String sourceLang, String targetLang,
            Map<String, TMXEntry> tmxPatch) throws Exception {
        TextFilter f = new TextFilter();
        Map<String, String> c = new TreeMap<String, String>();
        c.put(TextFilter.OPTION_SEGMENT_ON, TextFilter.SEGMENT_BREAKS);

        ProjectProperties props = new TestProjectProperties(sourceLang, targetLang);
        translateUsingTmx(f, c, fileTextIn, inCharset, fileTMX, fileTextOut, outCharset, props, tmxPatch);
    }

    protected void translateUsingTmx(IFilter filter, Map<String, String> config, String fileTextIn,
            String inCharset, String fileTMX, String fileTextOut, String outCharset, ProjectProperties props,
            Map<String, TMXEntry> tmxPatch) throws Exception {
        final ProjectTMX tmx = new ProjectTMX(props, new File("test/data/tmx/TMXComplianceKit/" + fileTMX),
                orphanedCallback, new HashMap<EntryKey, TMXEntry>());
        if (tmxPatch != null) {
            tmx.translationDefault.putAll(tmxPatch);
        }

        FilterContext fc = new FilterContext(props);
        fc.setInEncoding(inCharset);
        fc.setOutEncoding(outCharset);
        ITranslateCallback cb = new TranslateEntry(props) {
            @Override
            protected String getSegmentTranslation(String id, int segmentIndex, String segmentSource,
                    String prevSegment, String nextSegment, String path) {
                TMXEntry e = tmx.getDefaultTranslation(segmentSource);
                Assert.assertNotNull(e);
                return e.translation;
            }
        };
        filter.translateFile(new File("test/data/tmx/TMXComplianceKit/" + fileTextIn), outFile, config, fc,
                cb);
        compareTexts(new File("test/data/tmx/TMXComplianceKit/" + fileTextOut), outCharset, outFile,
                outCharset);
    }

    protected ProjectTMX.CheckOrphanedCallback orphanedCallback = new ProjectTMX.CheckOrphanedCallback() {
        public boolean existSourceInProject(String src) {
            return true;
        }

        public boolean existEntryInProject(EntryKey key) {
            return true;
        }
    };

    protected static class TestProjectProperties extends ProjectProperties {
        public TestProjectProperties(String sourceLang, String targetLang) {
            setSupportDefaultTranslations(true);
            setSourceLanguage(sourceLang);
            setTargetLanguage(targetLang);
        }
    }
}
