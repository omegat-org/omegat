/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2016 Didier Briel
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
package org.omegat.filters;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.omegat.filters3.xml.wordpress.WordpressFilter;

public class Wordpress extends TestFilterBase {
    @Test
    public void testParse() throws Exception {
        List<String> lines = parse(new WordpressFilter(),
                "test/data/filters/wordpress/Wordpress.xml", null);
        assertEquals(3, lines.size());
        assertEquals("This is a description", lines.get(0));
    }

}
