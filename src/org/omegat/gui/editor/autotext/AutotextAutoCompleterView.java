/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko, Aaron Madlon-Kay
               2014 Aaron Madlon-Kay
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

package org.omegat.gui.editor.autotext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.gui.editor.autocompleter.AutoCompleterItem;
import org.omegat.gui.editor.autocompleter.AutoCompleterListView;
import org.omegat.gui.editor.autotext.Autotext.AutotextItem;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * @author bartkoz
 * @author Aaron Madlon-Kay
 */
public class AutotextAutoCompleterView extends AutoCompleterListView {

    public AutotextAutoCompleterView() {
        super(OStrings.getString("AC_AUTOTEXT_VIEW"));
    }

    @Override
    public List<AutoCompleterItem> computeListData(String prevText, boolean contextualOnly) {
        List<AutotextItem> items = Autotext.getItems();
        List<AutoCompleterItem> result = new ArrayList<>();
        for (AutotextItem s : items) {
            if (prevText.endsWith(s.source)) {
                result.add(new AutoCompleterItem(s.target,
                    new String[] { s.source, s.comment }, s.source.length()));
            }
        }

        if (!Core.getProject().getProjectProperties().getTargetLanguage().isSpaceDelimited()
                && result.isEmpty() && !contextualOnly) {
            for (AutotextItem s : items) {
                result.add(new AutoCompleterItem(s.target, new String[] { s.source, s.comment }, 0));
            }
        }

        Collections.sort(result, new AutotextComparator());

        return result;
    }

    @Override
    public String itemToString(AutoCompleterItem item) {
        StringBuilder b = new StringBuilder();

        if (item.extras != null && item.extras.length > 0 && item.extras[0] != null && !item.extras[0].isEmpty()) {
            b.append(item.extras[0]).append(" \u2192 ");
        }
        if (item.payload != null) {
            b.append(item.payload);
        }
        if (item.extras != null && item.extras.length > 1 && item.extras[1] != null && !item.extras[1].isEmpty()) {
            b.append(" (").append(item.extras[1]).append(")");
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
                if (sortFullText) {
                    return o1.payload.compareTo(o2.payload);
                } else {
                    return itemToString(o1).compareTo(itemToString(o2));
                }
            }

            return 0;
        }
    }

    @Override
    public ITokenizer getTokenizer() {
        return new DefaultTokenizer();
    }

    @Override
    protected boolean isEnabled() {
        return Preferences.isPreferenceDefault(Preferences.AC_AUTOTEXT_ENABLED, true);
    }
}
