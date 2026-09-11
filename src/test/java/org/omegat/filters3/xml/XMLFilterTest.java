/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

package org.omegat.filters3.xml;

import org.jspecify.annotations.NullMarked;
import org.junit.Test;
import org.omegat.filters.TestFilterBase;
import org.omegat.filters2.Instance;

import java.io.File;
import java.util.Map;

@NullMarked
public class XMLFilterTest extends TestFilterBase {

    @Test
    public void testLoadCJKPath() throws Exception {
        XMLFilter filter = new TestFilter(new DefaultXMLDialect());
        String f = "src/test/resources/data/xml/文件/test.xml";
        File inputFile = new File(f);
        filter.processFile(inputFile, outFile, context);
    }

    public static class TestFilter extends XMLFilter {
        public TestFilter(XMLDialect dialect) {
            super(dialect, Map.of("http://apache.org/xml/features/disallow-doctype-decl", false));
        }

        @Override
        public String getFileFormatName() {
            return "";
        }

        @Override
        public Instance[] getDefaultInstances() {
            return new Instance[] { new Instance("*.xml", null, null), new Instance("*.dbk", null, null), };
        }
    }
}
