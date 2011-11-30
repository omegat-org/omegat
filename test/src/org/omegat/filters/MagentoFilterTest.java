/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2011 Michael Zakharov
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

import java.util.List;
import org.junit.Test;
import org.omegat.filters2.IAlignCallback;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.text.magento.MagentoFilter;

/**
 *
 * @author Michael Zakharov <trapman.hunt@gmail.com>
 */
public class MagentoFilterTest extends TestFilterBase {

    @Test
    public void testParse() throws Exception {
	List<String> entries = parse(new MagentoFilter(), "test/data/filters/magento/MagentoFilter.csv");
        assertEquals(5, entries.size());
        int i = 0;
        assertEquals("Tr: %s", entries.get(i++));
        assertEquals("Tr: There are <a href=\"\"%s\"\">%s items</a> in your cart.", entries.get(i++));
        assertEquals("Tr: Separate by \"\",\"\".", entries.get(i++));
        assertEquals("Tr: After selecting a new media storage location, press the Synchronize button", entries.get(i++));
        assertEquals("Tr: The reader repaired", entries.get(i++));
    }

   @Test
    public void testTranslate() throws Exception {
	translateText(new MagentoFilter(), "test/data/filters/magento/MagentoFilter.csv");
    }   
   
   @Test
   public void testAlign() throws Exception {
        final AlignResultHolder alignResult = new AlignResultHolder();
        
        align(new MagentoFilter(), "magento/MagentoFilterAlign.csv",
                "magento/MagentoFilterAlign-tr.csv", new IAlignCallback() {
                    public void addTranslation(String id, String source, String translation, boolean isFuzzy, String comment, IFilter filter) {
                        alignResult.aligned = id.equals("code") && 
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
