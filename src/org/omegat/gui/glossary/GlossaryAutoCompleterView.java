/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko, Aaron Madlon-Kay
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

package org.omegat.gui.glossary;

import java.util.ArrayList;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.gui.editor.autocompleter.AutoCompleter;
import org.omegat.gui.editor.autocompleter.AutoCompleterView;
import org.omegat.util.OStrings;

/**
 * The glossary auto-completer view.
 * 
 * @author Zoltan Bartko <bartkozoltan@bartkozoltan.com>
 * @author Aaron Madlon-Kay
 */
public class GlossaryAutoCompleterView extends AutoCompleterView {
    
    public GlossaryAutoCompleterView(AutoCompleter completer) {
        super(OStrings.getString("AC_GLOSSARY_VIEW"), completer);
    }

    @Override
    public List<String> computeListData(String wordChunk) {
        List<String> entryList = new ArrayList<String>();
        
        for (GlossaryEntry entry : Core.getGlossary().getDisplayedEntries()) {
            for (String s : entry.getLocTerms(true)) {
                if (s.toLowerCase().startsWith(wordChunk.toLowerCase())) {
                    entryList.add(matchCapitalization(s, wordChunk));
                }
            }
        }
        
        if (!Core.getProject().getProjectProperties().getTargetLanguage().isSpaceDelimited()
                && entryList.size() == 0) {
            for (GlossaryEntry entry : Core.getGlossary().getDisplayedEntries()) {
                for (String s : entry.getLocTerms(true)) {
                    entryList.add(matchCapitalization(s, wordChunk));
                }
            }
            completer.adjustInsertionPoint(wordChunk.length());
        }
        
        return entryList;
    }

    private static String matchCapitalization(String text, String compare) {
        if (compare.length() > 0
                && Character.isUpperCase(compare.charAt(0))
                && !Character.isUpperCase(text.charAt(0))) {
            return Character.toUpperCase(text.charAt(0)) + (text.length() > 1 ? text.substring(1) : "");
        }
        return text;
    }
}
