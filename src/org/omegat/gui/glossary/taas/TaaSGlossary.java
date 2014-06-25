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

import gen.taas.TaasTerm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.omegat.core.Core;
import org.omegat.core.glossaries.IGlossary;
import org.omegat.gui.glossary.GlossaryEntry;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Language;
import org.omegat.util.Preferences;
import org.omegat.util.Token;

/**
 * TaaS glossary implementation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TaaSGlossary implements IGlossary {

    @Override
    public List<GlossaryEntry> search(Language sLang, Language tLang, String srcText) throws Exception {
        if (!Preferences.isPreferenceDefault(Preferences.TAAS_LOOKUP, false)) {
            return Collections.emptyList();
        }
        ITokenizer tok = Core.getProject().getSourceTokenizer();
        if (tok == null) {
            return Collections.emptyList();
        }
        Token[] strTokens = tok.tokenizeWords(srcText, ITokenizer.StemmingMode.GLOSSARY);
        Set<String> terms = new TreeSet<String>();
        for (Token t : strTokens) {
            terms.add(t.getTextFromString(srcText));
        }
        List<GlossaryEntry> result = new ArrayList<GlossaryEntry>();
        for (String term : terms) {
            List<TaasTerm> r = TaaSPlugin.client.termLookup(sLang, tLang, term);
            for (TaasTerm tt : r) {
                result.add(new GlossaryEntry(term, tt.getTerm(), tt.getDomainName(), false));
            }
        }
        return result;
    }
}
