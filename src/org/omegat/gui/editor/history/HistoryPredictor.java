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

public class HistoryPredictor extends AutoCompleterListView {

    private static final Logger LOGGER = Logger.getLogger(HistoryPredictor.class.getName());

    WordPredictor predictor = new WordPredictor();
    private SourceTextEntry currentEntry;
    private boolean isCurrentEntryTranslated;

    public HistoryPredictor() {
        super(OStrings.getString("AC_HISTORY_PREDICTIONS_VIEW"));

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
        Preferences.addPropertyChangeListener(Preferences.AC_HISTORY_PREDICTION_ENABLED, evt -> {
            if ((Boolean) evt.getNewValue()) {
                if (Core.getProject().isProjectLoaded()) {
                    train();
                }
            } else {
                predictor.reset();
            }
        });
    }

    synchronized void train() {
        IProject project = Core.getProject();
        if (!project.isProjectLoaded()) {
            return;
        }
        long start = System.currentTimeMillis();
        predictor.reset();
        project.iterateByDefaultTranslations((source, trans) -> trainString(trans.translation));
        project.iterateByMultipleTranslations((source, trans) -> trainString(trans.translation));
        long time = System.currentTimeMillis() - start;
        LOGGER.finer(() -> String.format("Time to train History Predictor: %d ms", time));
    }

    private void trainString(String text) {
        if (text == null) {
            return;
        }
        String[] tokens = getTokenizer().tokenizeWordsToStrings(text, StemmingMode.NONE);

        predictor.train(tokens);
    }

    @Override
    public List<AutoCompleterItem> computeListData(String prevText, boolean contextualOnly) {
        if (prevText == null || prevText.isEmpty()) {
            return Collections.emptyList();
        }

        String[] tokens = getTokenizer().tokenizeVerbatimToStrings(prevText);

        String seed = lastFullWordToken(tokens);
        if (seed.isEmpty()) {
            return Collections.emptyList();
        }

        List<AutoCompleterItem> predictions = predictor.predictWord(seed).stream().map(p -> {
            return new AutoCompleterItem(p.getWord(),
                    new String[] { String.valueOf(Math.round(p.getFrequency())) + "%" }, 0);
        }).collect(Collectors.toList());

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
        return predictions.stream().filter(item -> item.payload.startsWith(context) && !item.payload.equals(context))
                .map(item -> new AutoCompleterItem(item.payload, item.extras, context.length()))
                .collect(Collectors.toList());
    }

    /**
     * Find the last <em>completed</em> word.
     * <p>
     * If the language is space-delimited, that means ignoring the last token
     * (which should be a partially input word) and then iterating backwards to
     * find the first non-whitespace token.
     * <p>
     * If the language is not space-delimited, use the last token, as we have no
     * way of distinguishing a completed word from an incomplete one.
     *
     * @param tokens
     * @return
     */
    private String lastFullWordToken(String[] tokens) {
        int startOffset = isLanguageSpaceDelimited() ? 2 : 1;
        for (int i = tokens.length - startOffset; i >= 0; i--) {
            String token = tokens[i];
            if (!token.trim().isEmpty()) {
                return token;
            }
        }
        return "";
    }

    @Override
    public String itemToString(AutoCompleterItem item) {
        StringBuilder sb = new StringBuilder("<html>").append(item.payload);
        if (item.extras != null && item.extras.length > 0) {
            sb.append(" <font color=\"gray\">(").append(item.extras[0]).append(")</font>");
        }
        sb.append("</html>");
        return sb.toString();
    }

    @Override
    protected boolean isEnabled() {
        return Preferences.isPreference(Preferences.AC_HISTORY_PREDICTION_ENABLED);
    }

    private boolean isLanguageSpaceDelimited() {
        return getTargetLanguage().isSpaceDelimited();
    }
}
