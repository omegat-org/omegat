/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Ibai Lakunza Velasco, Didier Briel
               2013 Martin Wunderlich, Didier Briel
               2015 Didier Briel
               2017 Briac Pilpre
               2021 Hiroshi Miura
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

package org.omegat.core.machinetranslators;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.text.StringEscapeUtils;

import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * @author Ibai Lakunza Velasco
 * @author Didier Briel
 * @author Martin Wunderlich
 * @author Briac Pilpre
 * @author Hiroshi Miura
 */
public final class MyMemoryMachineTranslate extends AbstractMyMemoryTranslate {

    public MyMemoryMachineTranslate(final String url) {
        super(url);
    }

    public MyMemoryMachineTranslate() {
    }

    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_MYMEMORY_MACHINE_TRANSLATE;
    }

    @Override
    public String getName() {
        return OStrings.getString("MT_ENGINE_MYMEMORY_MACHINE");
    }

    @Override
    protected boolean includeMT() {
        return true;
    }

    @Override
    protected String translate(final Language sLang, final Language tLang, final String text)
            throws Exception {
        try {
            // Get MyMemory response in JSON format
            JsonNode jsonResponse = getMyMemoryResponse(sLang, tLang, text);

            // Find the best Human translation if no MT translation is provided for
            // this text. If there is a MT translation, it will always take
            // precedence.
            double bestScore = 0d;
            JsonNode bestEntry = null;
            JsonNode mtEntry = null;

            JsonNode entries = jsonResponse.get("matches");
            for (JsonNode entry : entries) {
                if ("MT!".equals(entry.get("created-by").asText())) {
                    mtEntry = entry;
                } else if (entry.get("match").asDouble() > bestScore) {
                    bestEntry = entry;
                    bestScore = entry.get("match").asDouble();
                }
            }
            if (mtEntry != null) {
                bestEntry = mtEntry;
            }
            assert bestEntry != null;
            return StringEscapeUtils.unescapeHtml4(bestEntry.get("translation").asText());
        } catch (IOException e) {
            throw new MachineTranslateError(OStrings.getString("MT_ENGINE_MYMEMOROY_ERROR"), e);
        }
    }
}
