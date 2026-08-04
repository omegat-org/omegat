/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Tests for the command line pre-scan in Main.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class MainTest {

    @Test
    public void testExtractConfigDirSeparateValue() {
        assertEquals("/tmp/omegat-conf",
                Main.extractConfigDir(new String[] { "--config-dir", "/tmp/omegat-conf", "start" }));
    }

    @Test
    public void testExtractConfigDirEqualsForm() {
        assertEquals("/tmp/omegat-conf",
                Main.extractConfigDir(new String[] { "start", "--config-dir=/tmp/omegat-conf" }));
    }

    @Test
    public void testExtractConfigDirAbsent() {
        assertNull(Main.extractConfigDir(new String[] { "start", "project" }));
        assertNull(Main.extractConfigDir(new String[] { "--config-dir" }));
        assertNull(Main.extractConfigDir(new String[] { "--config-dir=" }));
        assertNull(Main.extractConfigDir(new String[0]));
        assertNull(Main.extractConfigDir(null));
    }
}
