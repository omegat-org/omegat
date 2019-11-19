/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.editor.history;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.IProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.events.IProjectEventListener.PROJECT_CHANGE_TYPE;
import org.omegat.gui.editor.autocompleter.AutoCompleterItem;
import org.omegat.gui.editor.autocompleter.AutoCompleterListView;
import org.omegat.tokenizer.ITokenizer.StemmingMode;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

public class HistoryCompleter extends AutoCompleterListView {

    private static final Logger LOGGER = Logger.getLogger(HistoryCompleter.class.getName());

    WordCompleter wordCompleter = new WordCompleter();
    private SourceTextEntry currentEntry;
    private boolean isCurrentEntryTranslated;

    public HistoryCompleter() {
        super(OStrings.getString("AC_HISTORY_COMPLETIONS_VIEW"));

        CoreEvents.registerProjectChangeListener(eventType -> {
            if (isEnabled() && eventType == PROJECT_CHANGE_TYPE.LOAD) {
                train();
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
                IProject project = Core.getProject();
                if (!project.isProjectLoaded()) {
                    return;
                }
                SourceTextEntry lastEntry = currentEntry;
                boolean wasTranslated = isCurrentEntryTranslated;
                if (lastEntry != null && !wasTranslated) {
                    TMXEntry newTranslation = project.getTranslationInfo(lastEntry);
                    if (newTranslation.isTranslated()) {
                        trainString(newTranslation.translation);
                    }
                }
                currentEntry = newEntry;
                isCurrentEntryTranslated = project.getTranslationInfo(newEntry).isTranslated();
            }
        });
        Preferences.addPropertyChangeListener(Preferences.AC_HISTORY_COMPLETION_ENABLED, evt -> {
            if ((Boolean) evt.getNewValue()) {
                if (Core.getProject().isProjectLoaded()) {
                    train();
                }
            } else {
                wordCompleter.reset();
            }
        });
    }

    synchronized void train() {
        IProject project = Core.getProject();
        if (!project.isProjectLoaded()) {
            return;
        }
        long start = System.currentTimeMillis();
        wordCompleter.reset();
        project.iterateByDefaultTranslations((source, trans) -> trainString(trans.translation));
        project.iterateByMultipleTranslations((source, trans) -> trainString(trans.translation));
        long time = System.currentTimeMillis() - start;
        LOGGER.finer(() -> String.format("Time to train History Completer: %d ms", time));
    }

    private void trainString(String text) {
        if (text == null) {
            return;
        }
        String[] tokens = getTokenizer().tokenizeWordsToStrings(text, StemmingMode.NONE);

        wordCompleter.train(tokens);
    }

    @Override
    public List<AutoCompleterItem> computeListData(String prevText, boolean contextualOnly) {
        if (prevText == null || prevText.isEmpty()) {
            return Collections.emptyList();
        }
        String lastToken = getLastToken(prevText);
        if (lastToken.isEmpty()) {
            return Collections.emptyList();
        }
        return wordCompleter.completeWord(lastToken).stream().map(s -> new AutoCompleterItem(s, null, lastToken.length()))
                .collect(Collectors.toList());
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
