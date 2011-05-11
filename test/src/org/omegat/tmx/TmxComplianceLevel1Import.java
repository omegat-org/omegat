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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.tmx;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.Assert;

import org.junit.Test;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.TMXEntry;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.ITranslateCallback;
import org.omegat.filters2.text.TextFilter;

/**
 * TMX Compliance tests as described on http://www.lisa.org/tmx/comp.htm
 * 
 * The Level 1 Compliance verifies mostly TMX structure, white spaces handling
 * and how the application deals with non-ASCII characters and special characters
 * in XML such as '<', or '&', XML syntax, encodings, and so forth.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TmxComplianceLevel1Import extends TmxComplianceBase {
    /**
     * Test Import1A - Internal Classic White Spaces.
     */
    @Test
    public void testImport1A() throws Exception {
        ProjectProperties props = new TestProjectProperties();
        final ProjectTMX tmx = new ProjectTMX(props, new File(
                "test/data/tmx/TMXComplianceKit/ImportTest1A.tmx"), orphanedCallback);

        TextFilter f = new TextFilter();
        Map<String, String> c = new TreeMap<String, String>();
        c.put(TextFilter.OPTION_SEGMENT_ON, TextFilter.SEGMENT_BREAKS);
        FilterContext fc = new FilterContext(props);
        ITranslateCallback cb = new ITranslateCallback() {
            public void setPass(int pass) {
            }

            public void linkPrevNextSegments() {
            }

            public String getTranslation(String id, String source, String path) {
                TMXEntry e = tmx.getDefaultTranslation(source);
                Assert.assertNotNull(e);
                return e.translation;
            }
        };
        f.translateFile(new File("test/data/tmx/TMXComplianceKit/ImportTest1A.txt"), new File(
                "test/data/tmx/TMXComplianceKit/ImportTest1A.txt.out"), c, fc, cb);
        compareBinary(new File("test/data/tmx/TMXComplianceKit/ImportTest1A_fr-ca.txt"), new File(
                "test/data/tmx/TMXComplianceKit/ImportTest1A.txt.out"));
    }
}
