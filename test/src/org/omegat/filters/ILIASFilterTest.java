/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2011-2014 Michael Zakharov
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
import org.junit.Test;
import org.omegat.filters2.IAlignCallback;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.text.ilias.ILIASFilter;

/**
 *
 * @author Michael Zakharov <trapman.hunt@gmail.com>
 */
public class ILIASFilterTest extends TestFilterBase {

    @Test
    public void testParse() throws Exception {
	List<String> entries = parse(new ILIASFilter(), "test/data/filters/ilias/ILIASFilter.lang");
        assertEquals(7, entries.size());
        int i = 0;
        assertEquals("Good line", entries.get(i++));
        assertEquals("Another good line with HTML <br/> entity", entries.get(i++));
        assertEquals("The text may include : too", entries.get(i++));
        assertEquals("The text may include # as well", entries.get(i++));
        assertEquals("The text may include  #:# or ", entries.get(i++)); // note the space after "or"
        assertEquals("have it at the end #:#", entries.get(i++));
        assertEquals("#:#", entries.get(i++));
    }

    @Test
    public void testTranslate() throws Exception {
        translateText(new ILIASFilter(), "test/data/filters/ilias/ILIASFilter.lang");
    }

    @Test
   public void testAlign() throws Exception {
        final AlignResultHolder alignResult = new AlignResultHolder();
        
        align(new ILIASFilter(), "ilias/ILIASFilterAlign.lang",
                "ilias/ILIASFilterAlign-tr.lang", new IAlignCallback() {
                    @Override
                    public void addTranslation(String id, String source, String translation, boolean isFuzzy, String comment, IFilter filter) {
                        alignResult.aligned = id.equals("module_name#:#variable_name") &&
                                              source.equals("original") && 
                                              translation.equals("translated");
                    }
                });
        
        assertTrue(alignResult.aligned);
    }

    public static class AlignResultHolder {
        boolean aligned = false;
    }

}
