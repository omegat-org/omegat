/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

package org.omegat.util.gui;

import static org.junit.Assert.fail;

import java.util.MissingResourceException;

import org.junit.Test;
import org.omegat.util.OStrings;
import org.omegat.util.TestPreferencesInitializer;

public class StylesTest {

    /**
     * All colors should have a localizable name in Bundle.properties.
     *
     * @throws Exception
     */
    @Test
    public void testColorStrings() throws Exception {
        TestPreferencesInitializer.init();
        for (Styles.EditorColor c : Styles.EditorColor.values()) {
            try {
                OStrings.getString(c.name());
            } catch (MissingResourceException t) {
                fail("Color " + c.name() + " does not have an entry in Bundle.properties");
            }
        }
    }
}
