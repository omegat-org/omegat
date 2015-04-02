/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2015 Didier Briel
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

import java.util.List;

import org.omegat.filters3.xml.xmlspreadsheet.XMLSpreadsheetFilter;

public class XMLSpreadsheetTest extends TestFilterBase {
    public void testParse() throws Exception {
        List<String> lines = parse(new XMLSpreadsheetFilter(),
                "test/data/filters/XMLSpreadsheet/XMLSpreadsheet2003.xml", null);
        assertTrue(lines.size()== 1);
        assertEquals("This is a test sentence with <b>HTML tags</b> inside.", lines.get(0));
    }

    public void testTranslate() throws Exception {
        translateXML(new XMLSpreadsheetFilter(), "test/data/filters/XMLSpreadsheet/XMLSpreadsheet2003.xml");
    }

}
