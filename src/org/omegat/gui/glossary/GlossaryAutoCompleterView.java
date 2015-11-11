/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko, Aaron Madlon-Kay
               2015 Aaron Madlon-Kay
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.omegat.core.Core;
import org.omegat.gui.editor.autocompleter.AutoCompleter;
import org.omegat.gui.editor.autocompleter.AutoCompleterItem;
import org.omegat.gui.editor.autocompleter.AutoCompleterListView;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;

/**
 * The glossary auto-completer view.
 * 
 * @author Zoltan Bartko <bartkozoltan@bartkozoltan.com>
 * @author Aaron Madlon-Kay
 */
public class GlossaryAutoCompleterView extends AutoCompleterListView {

    public GlossaryAutoCompleterView() {
        super(OStrings.getString("AC_GLOSSARY_VIEW"));
    }

    /* Users with gigantic glossaries can get too many popups, so adjust the behavior here.
     * Only pop up if a) we have suggestions, and b) if there's more than one page of
     * suggestions then the user should have input at least 2 characters.
     */
    @Override
    public boolean shouldPopUp() {
        String leadingText = getLeadingText();
        List<AutoCompleterItem> entries = computeListData(leadingText, true);
        return !entries.isEmpty()
                && (leadingText.codePointCount(0, leadingText.length()) > 1
                        || entries.size() <= AutoCompleter.PAGE_ROW_COUNT);
    }
    
    @Override
    public List<AutoCompleterItem> computeListData(String prevText, boolean contextualOnly) {
        String wordChunk = getLastToken(prevText);
        String sortMatchTo = wordChunk;
        
        List<AutoCompleterItem> result = new ArrayList<AutoCompleterItem>();
        List<GlossaryEntry> entries = Core.getGlossary().getDisplayedEntries();
        
        // Get contextual results
        fillMatchingTerms(result, entries, wordChunk);
        
        if (result.isEmpty() && !contextualOnly) {
            // Get non-contextual results only if called for
            fillMatchingTerms(result, entries, null);
            sortMatchTo = null;
        }
        
        Collections.sort(result, new GlossaryComparator(entries, sortMatchTo));
        
        return result;
    }
    
    /**
     * Fill provided result list with AutCompleterItems matching the provided wordChunk.
     * If the wordChunk is null, all available items will be added. However if the wordChunk is
     * empty ("") then no items will be added.
     * @param result
     * @param glossary
     * @param context
     */
    private void fillMatchingTerms(List<AutoCompleterItem> result, List<GlossaryEntry> glossary, String context) {
        if ("".equals(context)) {
            // Context is present but empty--we consider no terms to match.
            return;
        }
        
        for (GlossaryEntry entry : glossary) {
            for (String term : entry.getLocTerms(true)) {
                if (!termMatchesChunk(term, context)) {
                    continue;
                }
                String payload = StringUtil.matchCapitalization(term, context, getTargetLocale());
                int length = context == null ? 0 : context.length();
                AutoCompleterItem item = new AutoCompleterItem(payload, new String[] { entry.getSrcText() }, length);
                if (!result.contains(item)) {
                    result.add(item);
                }
            }
        }
    }
    
    private boolean termMatchesChunk(String term, String context) {
        if (context == null) {
            // Consider null context to match everything
            return true;
        }
        Locale locale = getTargetLocale();
        String lowerTerm = term.toLowerCase(locale);
        String lowerContext = context.toLowerCase(locale);
        // Consider a term to NOT match if it is the same (modulo case) as the context (i.e. it is already present)
        return !lowerTerm.equals(lowerContext) && lowerTerm.startsWith(lowerContext);
    }
    
    private Locale getTargetLocale() {
        return Core.getProject().getProjectProperties().getTargetLanguage().getLocale();
    }

    @Override
    public String itemToString(AutoCompleterItem item) {
        if (Preferences.isPreference(Preferences.AC_GLOSSARY_SHOW_SOURCE) && item.extras != null) {
            if (Preferences.isPreference(Preferences.AC_GLOSSARY_SHOW_TARGET_BEFORE_SOURCE)) {
                return item.payload + " \u2190 " + item.extras[0];
            } else {
                return item.extras[0] + " \u2192 " + item.payload;
            }
        } else {
            return item.payload;
        }
    }

    static class GlossaryComparator implements Comparator<AutoCompleterItem> {
        
        private boolean bySource = Preferences.isPreference(Preferences.AC_GLOSSARY_SORT_BY_SOURCE);
        private boolean byLength = Preferences.isPreference(Preferences.AC_GLOSSARY_SORT_BY_LENGTH);
        private boolean alphabetically = Preferences.isPreference(Preferences.AC_GLOSSARY_SORT_ALPHABETICALLY);
        
        private final List<GlossaryEntry> entries;
        private final String matchTo;
        
        public GlossaryComparator(List<GlossaryEntry> entries, String matchTo) {
            this.entries = entries;
            this.matchTo = matchTo;
        }
        
        @Override
        public int compare(AutoCompleterItem o1, AutoCompleterItem o2) {
            
            // If one of the payloads starts with the exact matchTo string, prioritize that one.
            if (!StringUtil.isEmpty(matchTo)) {
                boolean o1Matches = o1.payload.startsWith(matchTo);
                boolean o2Matches = o2.payload.startsWith(matchTo);
                if (o1Matches && !o2Matches) {
                    return -1;
                }
                if (!o1Matches && o2Matches) {
                    return 1;
                }
            }
            
            // Sort alphabetically by source term
            if (bySource) {
                int result = o1.extras[0].compareTo(o2.extras[0]);
                if (result != 0) {
                    return result;
                }
            }
            
            // Sorting for same source with multiple targets
            if (o1.extras[0].equals(o2.extras[0])) {
                if (byLength) {
                    if (o1.payload.length() < o2.payload.length()) {
                        return 1;
                    } else if (o1.payload.length() > o2.payload.length()) {
                        return -1;
                    }
                }
                if (alphabetically) {
                    return o1.payload.compareTo(o2.payload);
                }
            }
            
            // If we make it here, we should ensure the sorting is the same
            // as in the original list of entries.
            int i1 = -1;
            int i2 = -1;
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).getSrcText().equals(o1.extras[0])) {
                    i1 = i;
                }
                if (entries.get(i).getSrcText().equals(o2.extras[0])) {
                    i2 = i;
                }
                if (i1 != -1 && i2 != -1) {
                    break;
                }
            }
            if (i1 < i2) {
                return -1;
            } else if (i1 > i2) {
                return 1;
            }
            return 0;
        }
        
    }
}
