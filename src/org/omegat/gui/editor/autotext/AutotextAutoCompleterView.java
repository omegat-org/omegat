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

package org.omegat.gui.editor.autotext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.gui.editor.autocompleter.AutoCompleterItem;
import org.omegat.gui.editor.autocompleter.AutoCompleterListView;
import org.omegat.gui.editor.autocompleter.AutoCompleter;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * @author bartkoz
 * @author Aaron Madlon-Kay
 */
public class AutotextAutoCompleterView extends AutoCompleterListView {

    public AutotextAutoCompleterView(AutoCompleter completer) {
        super(OStrings.getString("AC_AUTOTEXT_VIEW"), completer);
    }
            
    @Override
    public List<AutoCompleterItem> computeListData(String wordChunk) {
        boolean matchFullText = Preferences.isPreference(Preferences.AC_AUTOTEXT_SORT_FULL_TEXT);
        
        List<AutoCompleterItem> result = new ArrayList<AutoCompleterItem>();
        for (AutotextPair s : Core.getAutoText().getList()) {
            String toCompare = matchFullText ? s.target : s.source;
            if (toCompare.toLowerCase().startsWith(wordChunk.toLowerCase())) {
                result.add(new AutoCompleterItem(s.target,
                    new String[] { s.source, s.comment }));
            }
        }
        
        if (!Core.getProject().getProjectProperties().getTargetLanguage().isSpaceDelimited()
                && result.size() == 0) {
            for (AutotextPair s : Core.getAutoText().getList()) {
                result.add(new AutoCompleterItem(s.target, new String[] { s.source, s.comment }));
            }
            completer.adjustInsertionPoint(wordChunk.length());
        }
        
        Collections.sort(result, new AutotextComparator());
        
        return result;
    }

    @Override
    public String itemToString(AutoCompleterItem item) {
        StringBuilder b = new StringBuilder();
        
        if (item.extras != null && item.extras[0] != null && !item.extras[0].isEmpty()) {
            b.append(item.extras[0]);
            b.append(" \u2192 ");
        }
        if (item.payload != null) b.append(item.payload);
        if (item.extras != null && item.extras[1] != null && !item.extras[1].isEmpty()) {
            b.append(" (");
            b.append(item.extras[1]);
            b.append(")");
        }
        return b.toString();
    }
    
    class AutotextComparator implements Comparator<AutoCompleterItem> {

        private boolean byLength = Preferences.isPreference(Preferences.AC_AUTOTEXT_SORT_BY_LENGTH);
        private boolean alphabetically = Preferences.isPreference(Preferences.AC_AUTOTEXT_SORT_ALPHABETICALLY);
        private boolean sortFullText = Preferences.isPreference(Preferences.AC_AUTOTEXT_SORT_FULL_TEXT);
        
        @Override
        public int compare(AutoCompleterItem o1, AutoCompleterItem o2) {
            if (byLength) {
                if (o1.payload.length() < o2.payload.length()) {
                    return 1;
                } else if (o1.payload.length() > o2.payload.length()) {
                    return -1;
                }
            }
            
            if (alphabetically) {
                if (sortFullText)
                    return o1.payload.compareTo(o2.payload);
                else
                    return itemToString(o1).compareTo(itemToString(o2));
            }
            
            return 0;
        }
    }
    
    @Override
    public ITokenizer getTokenizer() {
        return new DefaultTokenizer();
    }
}
