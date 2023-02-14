/*
 * *************************************************************************
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2021 Hiroshi Miura.
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

public class Google2TranslateTest extends TestCore {

    @Test
    public void testGetJsonResults() throws Exception {
        Preferences.setPreference(Preferences.ALLOW_GOOGLE2_TRANSLATE, true);
        Google2Translate google2Translate = new Google2Translate();
        String json = "{\n"
                + "  \"data\": {\n"
                + "    \"translations\": [\n"
                + "      {\n"
                + "        \"translatedText\": \"Hallo Welt\",\n"
                + "        \"detectedSourceLanguage\": \"en\"\n"
                + "      },\n"
                + "      {\n"
                + "        \"translatedText\": \"Mein Name ist Jeff\",\n"
                + "        \"detectedSourceLanguage\": \"en\"\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "}";
        String translation = google2Translate.getJsonResults(json);
        assertEquals("Hallo Welt", translation);
    }

}
