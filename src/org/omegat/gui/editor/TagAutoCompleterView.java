/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Aaron Madlon-Kay, Zoltan Bartko
               2014 Aaron Madlon-Kay
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

package org.omegat.gui.editor;

import java.util.ArrayList;
import java.util.List;

import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.gui.editor.autocompleter.AutoCompleterItem;
import org.omegat.gui.editor.autocompleter.AutoCompleterListView;
import org.omegat.gui.editor.autocompleter.AutoCompleter;
import org.omegat.util.OStrings;
import org.omegat.util.TagUtil;

/**
 * An AutoCompleterView for inserting missing tags.
 * 
 * @author Aaron Madlon-Kay
 */
public class TagAutoCompleterView extends AutoCompleterListView {

    public TagAutoCompleterView(AutoCompleter completer) {
        super(OStrings.getString("AC_TAG_VIEW"), completer);
    }

    @Override
    public List<AutoCompleterItem> computeListData(String wordChunk) {
        
        List<String> missingGroups = TagUtil.getGroupedMissingTagsFromTarget();
        
        // If wordChunk is a tag, pretend we have a blank wordChunk.
        if (TagUtil.getAllTagsInSource().contains(wordChunk)) {
            completer.adjustInsertionPoint(wordChunk.length());
            wordChunk = "";
        }

        // Check for partial matches among missing tag groups.
        List<String> matchGroups = new ArrayList<String>();
        for (String g : missingGroups) {
            if (g.startsWith(wordChunk)) matchGroups.add(g);
        }
        
        // If there are no partial matches, show all missing tags as suggestions.
        if (matchGroups.isEmpty()) {
            completer.adjustInsertionPoint(wordChunk.length());
            return convertList(missingGroups);
        }
        
        return convertList(matchGroups);
    }

    private static List<AutoCompleterItem> convertList(List<String> list) {
        List<AutoCompleterItem> result = new ArrayList<AutoCompleterItem>();
        for (String s : list) {
            int sep = s.indexOf(TagUtil.TAG_SEPARATOR_SENTINEL);
            String cleaned = s;
            String display = s;
            int adjustment = 0;
            boolean keepSelection = false;
            if (sep > -1) {
                cleaned = s.replace(TagUtil.TAG_SEPARATOR_SENTINEL, "");
                display = s.replace(TagUtil.TAG_SEPARATOR_SENTINEL, "|");
                adjustment = - (s.length() - 1 - sep);
                keepSelection = true;
            }
            result.add(new AutoCompleterItem(cleaned, new String[] { display }, adjustment, keepSelection));
        }
        return result;
    }

    @Override
    public ITokenizer getTokenizer() {
        return new DefaultTokenizer();
    }

    @Override
    public String itemToString(AutoCompleterItem item) {
        return item.extras[0];
    }
}
