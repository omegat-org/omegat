/*
 * *************************************************************************
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2021.
 *                Home page: http://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *************************************************************************
 *
 */

package org.omegat.core.machinetranslators;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.omegat.core.TestCore;
import org.omegat.util.Preferences;

public class IBMWatsonTranslateTest extends TestCore {

    @Test
    public void getJsonResults() {
        Preferences.setPreference(Preferences.ALLOW_IBMWATSON_TRANSLATE, true);
        IBMWatsonTranslate ibmWatsonTranslate = new IBMWatsonTranslate();
        String json = "{\"translations\": ["
                + "  {\"translation\": \"translated text goes here.\" }"
                + "],"
                + " \"word_count\": 4, \"character_count\": 53 }";
        String translation = ibmWatsonTranslate.getJsonResults(json);
        assertEquals("translated text goes here.", translation);
    }
}