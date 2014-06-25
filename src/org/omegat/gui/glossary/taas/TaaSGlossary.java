/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
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

package org.omegat.gui.glossary.taas;

import gen.taas.TaasExtractionResult;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.omegat.core.glossaries.IGlossary;
import org.omegat.gui.glossary.GlossaryEntry;
import org.omegat.gui.glossary.GlossaryReaderTBX;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.Preferences;

/**
 * TaaS glossary implementation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TaaSGlossary implements IGlossary {
    private static final Logger LOGGER = Logger.getLogger(TaaSGlossary.class.getName());

    @Override
    public List<GlossaryEntry> search(Language sLang, Language tLang, String srcText) throws Exception {
        if (!Preferences.isPreferenceDefault(Preferences.TAAS_LOOKUP, false)) {
            return Collections.emptyList();
        }

        TaasExtractionResult res = TaaSPlugin.client.termExtraction(sLang, tLang, srcText);
        String data = TaaSPlugin.filterTaasResult(res.getTerms());
        List<GlossaryEntry> entries = GlossaryReaderTBX.read(data, false);
        Log.logDebug(LOGGER, "termExtraction returns data");
        return entries;
    }
}
