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
package org.omegat.core;

import org.junit.Rule;
import org.junit.Test;
import org.omegat.util.LocaleRule;
import org.omegat.util.OStrings;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class KnownExceptionTest {
    @Rule
    public final LocaleRule localeRule = new LocaleRule(new Locale("en"));

    @Test
    public void testExceptions() {
        // Call constructor with error code and parameters
        KnownException ex1 = new KnownException("TF_ERROR", "param1", "param2");
        assertEquals(2, ex1.getParams().length);
        assertEquals("param1", ex1.getParams()[0]);
        assertEquals("param2", ex1.getParams()[1]);
        // Usage of <code>OStrings.getString(ex1.getMessage(), ex1.getParams())</code>
        assertEquals("TF_ERROR", ex1.getMessage());
        assertEquals(OStrings.getString("TF_ERROR"), ex1.getLocalizedMessage());

        // Call constructor with throwable, error code, and parameters
        KnownException ex2 = new KnownException(new RuntimeException("Cause"), "TF_ERROR", "param1", "param2");
        assertEquals(2, ex2.getParams().length);
        assertEquals("param1", ex2.getParams()[0]);
        assertEquals("param2", ex2.getParams()[1]);
        assertEquals(OStrings.getString("TF_ERROR"), ex2.getLocalizedMessage());
        assertEquals("Cause", ex2.getCause().getMessage());
    }

}
