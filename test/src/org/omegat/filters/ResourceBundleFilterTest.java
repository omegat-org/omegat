/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

import java.util.HashMap;
import java.util.Map;

import org.omegat.core.data.IProject;
import org.omegat.filters2.IAlignCallback;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.text.bundles.ResourceBundleFilter;

public class ResourceBundleFilterTest extends TestFilterBase {
    public void testParse() throws Exception {
        parse(new ResourceBundleFilter(),
                "test/data/filters/resourceBundle/file-ResourceBundleFilter.properties");
    }

    public void testTranslate() throws Exception {
        translateText(new ResourceBundleFilter(),
                "test/data/filters/resourceBundle/file-ResourceBundleFilter.properties");
    }

    public void testAlign() throws Exception {
        final AlignResult ar = new AlignResult();
        align(new ResourceBundleFilter(), "resourceBundle/file-ResourceBundleFilter.properties",
                "resourceBundle/file-ResourceBundleFilter_be.properties", new IAlignCallback() {
                    public void addTranslation(String id, String source, String translation, boolean isFuzzy,
                            String path, IFilter filter) {
                        ar.found = id.equals("ID") && source.equals("Value") && translation.equals("test");
                    }
                });
        assertTrue(ar.found);
    }

    public static class AlignResult {
        boolean found = false;
    }

    public void testLoad() throws Exception {
        String f = "test/data/filters/resourceBundle/file-ResourceBundleFilter.properties";
        ResourceBundleFilter filter = new ResourceBundleFilter();
        IProject.FileInfo fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMulti("Value", "ID", null, null, null, null);
        checkMulti("Value2", "ID2", null, null, null, null);
        checkMulti("Value3", "ID3", null, null, null, "# some comment");
        checkMulti("Value4", "ID4", null, null, null, "# multiple line\n# comment");
        checkMulti("Value5", "ID5", null, null, null, "! alternate comment style");
        checkMultiEnd();
        
        f = "test/data/filters/resourceBundle/file-ResourceBundleFilter-SMP.properties";
        fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMulti("\uD835\uDC00\uD835\uDC01\uD835\uDC02", "ID", null, null, null, null);
        checkMulti("\uD835\uDC03\uD835\uDC04\uD835\uDC05", "ID2", null, null, null, null);
        checkMultiEnd();
    }
    
    public void testDoNotEscapeUnicodeLiterals() throws Exception {
        String f = "test/data/filters/resourceBundle/file-ResourceBundleFilter-UnicodeLiterals.properties";
        ResourceBundleFilter filter = new ResourceBundleFilter();
        Map<String, String> options = new HashMap<String, String>();
        options.put(ResourceBundleFilter.OPTION_DONT_UNESCAPE_U_LITERALS, "true");
        IProject.FileInfo fi = loadSourceFiles(filter, f, options);

        checkMultiStart(fi, f);
        checkMulti("a\nb\\u0020\\ad", "MU", null, null, null, "# \\u00ad");
        checkMultiEnd();
        
        translateText(filter, f, options);
    }
}
