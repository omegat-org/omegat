/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.gui.editor.history;

import java.util.Collections;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.editor.autocompleter.AutoCompleterItem;
import org.omegat.gui.editor.autocompleter.AutoCompleterListView;
import org.omegat.tokenizer.ITokenizer.StemmingMode;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

public class HistoryCompleter extends AutoCompleterListView {


    WordCompleter completer = new WordCompleter();
    private SourceTextEntry currentEntry;
    private TMXEntry currentEntryTranslation;

    public HistoryCompleter() {
        super(OStrings.getString("AC_HISTORY_COMPLETIONS_VIEW"));
        
        CoreEvents.registerProjectChangeListener(new IProjectEventListener() {
            @Override
            public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
                if (isEnabled() && eventType == PROJECT_CHANGE_TYPE.LOAD) {
                    train();
                }
            }
        });
        CoreEvents.registerEntryEventListener(new IEntryEventListener() {            
            @Override
            public void onNewFile(String activeFileName) {
            }
            @Override
            public void onEntryActivated(SourceTextEntry newEntry) {
                if (!isEnabled()) {
                    return;
                }
                SourceTextEntry lastEntry = currentEntry;
                TMXEntry lastEntryTranslation = currentEntryTranslation;
                if (lastEntry != null && lastEntryTranslation != null && !lastEntryTranslation.isTranslated()) {
                    TMXEntry newTranslation = Core.getProject().getTranslationInfo(lastEntry);
                    trainString(newTranslation.translation);
                }
                currentEntry = newEntry;
                currentEntryTranslation = Core.getProject().getTranslationInfo(newEntry);
            }
        });
        Preferences.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals(Preferences.AC_HISTORY_COMPLETION_ENABLED)) {
                if ((Boolean) evt.getNewValue()) {
                    if (Core.getProject().isProjectLoaded()) {
                        train();
                    }
                } else {
                    completer.reset();
                }
            }
        });
    }
    
    synchronized void train() {
        completer.reset();
        Core.getProject().iterateByDefaultTranslations((source, trans) -> trainString(trans.translation));
        Core.getProject().iterateByMultipleTranslations((source, trans) -> trainString(trans.translation));
    }
    
    private void trainString(String text) {
        if (text == null) {
            return;
        }
        String[] tokens = getTokenizer().tokenizeWordsToStrings(text, StemmingMode.NONE);
        
        completer.train(text, tokens);
    }

    @Override
    public List<AutoCompleterItem> computeListData(String prevText, boolean contextualOnly) {
        if (prevText == null || prevText.isEmpty()) {
            return Collections.emptyList();
        }
        return completer.completeWord(getLastToken(prevText));
    }

    @Override
    public String itemToString(AutoCompleterItem item) {
        return item.payload;
    }

    @Override
    protected boolean isEnabled() {
        return Preferences.isPreference(Preferences.AC_HISTORY_COMPLETION_ENABLED);
    }
}
