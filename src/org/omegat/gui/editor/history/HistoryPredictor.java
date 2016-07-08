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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

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

public class HistoryPredictor extends AutoCompleterListView {

    private static final Logger LOGGER = Logger.getLogger(HistoryPredictor.class.getName());

    WordPredictor predictor = new WordPredictor();
    private SourceTextEntry currentEntry;
    private TMXEntry currentEntryTranslation;

    public HistoryPredictor() {
        super(OStrings.getString("AC_HISTORY_PREDICTIONS_VIEW"));
        
        CoreEvents.registerProjectChangeListener(new IProjectEventListener() {
            @Override
            public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
                if (eventType == PROJECT_CHANGE_TYPE.LOAD) {
                    predictor.setisLanguageSpaceDelimited(isLanguageSpaceDelimited());
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
            if (evt.getPropertyName().equals(Preferences.AC_HISTORY_PREDICTION_ENABLED)) {
                if ((Boolean) evt.getNewValue()) {
                    if (Core.getProject().isProjectLoaded()) {
                        train();
                    }
                } else {
                    predictor.reset();
                }
            }
        });
    }
    
    synchronized void train() {
        long start = System.currentTimeMillis();
        predictor.reset();
        Core.getProject().iterateByDefaultTranslations((source, trans) -> trainString(trans.translation));
        Core.getProject().iterateByMultipleTranslations((source, trans) -> trainString(trans.translation));
        LOGGER.finer(() -> String.join(" ", "Time to train History Predictor:",
                Long.toString(System.currentTimeMillis() - start), "ms"));
    }
    
    private void trainString(String text) {
        if (text == null) {
            return;
        }
        String[] tokens = getTokenizer().tokenizeWordsToStrings(text, StemmingMode.NONE);
        
        predictor.trainStringPrediction(text, tokens);
    }

    @Override
    public List<AutoCompleterItem> computeListData(String prevText, boolean contextualOnly) {
        if (prevText == null || prevText.isEmpty()) {
            return Collections.emptyList();
        }

        String[] tokens = getTokenizer().tokenizeVerbatimToStrings(prevText);

        List<AutoCompleterItem> predictions = predictor.predictWord(tokens);

        if (predictions.isEmpty()) {
            return predictions;
        }

        // We have a non-space-delimited language, so it's not possible to
        // distinguish between a new-word situation and a completion situation.
        if (!isLanguageSpaceDelimited()) {
            return predictions;
        }

        // We are starting a new word so all predictions are relevant
        if (tokens[tokens.length - 1].trim().isEmpty()) {
            return predictions;
        }

        // We have context to filter on
        String context = tokens[tokens.length - 1];
        List<AutoCompleterItem> result = new ArrayList<>();
        for (AutoCompleterItem item : predictions) {
            if (item.payload.startsWith(context) && !item.payload.equals(context)) {
                result.add(new AutoCompleterItem(item.payload, item.extras, context.length()));
            }
        }
        return result;
    }

    @Override
    public String itemToString(AutoCompleterItem item) {
        return "<html>" + item.payload + " <font color=\"gray\">(" + item.extras[0] + ")</font></html>";
    }

    @Override
    protected boolean isEnabled() {
        return Preferences.isPreference(Preferences.AC_HISTORY_PREDICTION_ENABLED);
    }

    private boolean isLanguageSpaceDelimited() {
        return getTargetLanguage().isSpaceDelimited();
    }
}
