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

package org.omegat.filters2.master;

import junit.framework.TestCase;

/**
 * Tests for PluginUtils.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class PluginUtilsTest extends TestCase {
    public void testVersionAllowed() {
        assertFalse(PluginUtils.isVersionAllowed("zzz", "1.2.3"));

        assertFalse(PluginUtils.isVersionAllowed("1.1-2.2-3.3", "1.2.3"));

        assertFalse(PluginUtils.isVersionAllowed("1.1_2-1.20", "1.5"));
        assertFalse(PluginUtils.isVersionAllowed("1.1-1.20_2", "1.5"));
        assertFalse(PluginUtils.isVersionAllowed("1.1-1.20", "1.5_02"));

        assertTrue(PluginUtils.isVersionAllowed("1.1-2.2", "1.20"));
        assertTrue(PluginUtils.isVersionAllowed("1.1-2.2", "2.0"));
        assertTrue(PluginUtils.isVersionAllowed("1.1-2.2", "2.1"));
        assertTrue(PluginUtils.isVersionAllowed("1.1-1.5", "1.1"));
        assertTrue(PluginUtils.isVersionAllowed("1.1-1.5", "1.5"));
        assertTrue(PluginUtils.isVersionAllowed("1.1-2.2", "1.1"));
        assertTrue(PluginUtils.isVersionAllowed("1.1-2.2", "2.02"));

        assertFalse(PluginUtils.isVersionAllowed("1.1-2.2", "1.0"));
        assertFalse(PluginUtils.isVersionAllowed("1.1-2.2", "2.3"));
    }
}
