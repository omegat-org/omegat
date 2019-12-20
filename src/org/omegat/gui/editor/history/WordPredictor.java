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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

public class WordPredictor {
    static final double MIN_FREQUENCY = 10d;
    private static final Comparator<Prediction> RESULT_SORTER = Comparator.comparing(Prediction::getFrequency)
            .reversed().thenComparing(Prediction::getWord);

    private Map<String, FrequencyStrings> data = new HashMap<>();

    public void reset() {
        data.clear();
    }

    public void train(String[] tokens) {
        if (tokens.length == 0) {
            return;
        }
        for (int i = 0; i < tokens.length - 1; i++) {
            String token = tokens[i];
            FrequencyStrings strings = data.get(token);
            if (strings == null) {
                strings = new FrequencyStrings();
                data.put(token, strings);
            }
            strings.encounter(tokens[i + 1]);
        }
    }

    public List<Prediction> predictWord(String seed) {
        if (seed == null) {
            throw new NullPointerException("Prediction seed can't be null");
        }
        if (data.isEmpty() || seed.isEmpty()) {
            return Collections.emptyList();
        }

        FrequencyStrings candidates = data.get(seed);
        if (candidates == null) {
            return Collections.emptyList();
        }
        // Only consider candidates that have appeared more than once.
        List<Entry<String, Integer>> entries = candidates.getEntries().stream().filter(e -> e.getValue() > 1)
                .collect(Collectors.toList());
        int total = entries.stream().mapToInt(Entry::getValue).sum();
        return entries.stream().map(e -> {
            double percent = ((double) e.getValue() / total) * 100;
            // Only retain predictions meeting the minimum frequency.
            return percent >= MIN_FREQUENCY ? new Prediction(e.getKey(), percent) : null;
        }).filter(Objects::nonNull).sorted(RESULT_SORTER).collect(Collectors.toList());
    }

    private static class FrequencyStrings {
        private final Map<String, Integer> map = new HashMap<>();

        public void encounter(String string) {
            Integer count = map.get(string);
            map.put(string, count == null ? 1 : count + 1);
        }

        public List<Entry<String, Integer>> getEntries() {
            return new ArrayList<>(map.entrySet());
        }
    }

    public static class Prediction {
        private final String word;
        private final double frequency;

        public Prediction(String word, double frequency) {
            this.word = word;
            this.frequency = frequency;
        }

        public String getWord() {
            return word;
        }

        public double getFrequency() {
            return frequency;
        }
    }
}
