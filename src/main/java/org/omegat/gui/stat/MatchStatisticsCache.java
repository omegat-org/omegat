/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.gui.stat;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IProjectEventListener;

/**
 * Session-scoped cache of the last total match statistics scan. Keeps the
 * result available while the program runs, even after the statistics window has
 * been closed. Cleared whenever the project changes, because entry numbers are
 * only stable within one loaded project.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public final class MatchStatisticsCache {

    /** Immutable result of one finished match statistics scan. */
    public static final class Snapshot {
        private final String[] headers;
        private final String[][] data;
        private final Map<Integer, Integer> entryRowIndexes;
        private final @Nullable String textData;
        private final String projectRoot;
        private final Instant lastScan;

        Snapshot(String[] headers, String[][] data, Map<Integer, Integer> entryRowIndexes,
                @Nullable String textData, String projectRoot, Instant lastScan) {
            this.headers = headers.clone();
            this.data = new String[data.length][];
            for (int i = 0; i < data.length; i++) {
                this.data[i] = data[i].clone();
            }
            this.entryRowIndexes = entryRowIndexes;
            this.textData = textData;
            this.projectRoot = projectRoot;
            this.lastScan = lastScan;
        }

        public String[] getHeaders() {
            return headers;
        }

        public String[][] getData() {
            return data;
        }

        /** Root folder of the project the scan belongs to. */
        public String getProjectRoot() {
            return projectRoot;
        }

        /**
         * Map of entry number to category row index, see
         * {@link org.omegat.core.statistics.dso.MatchStatCounts}.
         */
        public Map<Integer, Integer> getEntryRowIndexes() {
            return entryRowIndexes;
        }

        public @Nullable String getTextData() {
            return textData;
        }

        public Instant getLastScan() {
            return lastScan;
        }
    }

    private static volatile @Nullable Snapshot snapshot;

    static {
        CoreEvents.registerProjectChangeListener(MatchStatisticsCache::onProjectChanged);
    }

    private MatchStatisticsCache() {
    }

    static void onProjectChanged(IProjectEventListener.PROJECT_CHANGE_TYPE eventType) {
        switch (eventType) {
        case CLOSE, LOAD, CREATE -> clear();
        default -> {
        }
        }
    }

    public static void store(String[] headers, String[][] data, Map<Integer, Integer> entryRowIndexes,
            @Nullable String textData, String projectRoot) {
        snapshot = new Snapshot(headers, data, entryRowIndexes, textData, projectRoot, Instant.now());
    }

    public static Optional<Snapshot> get() {
        return Optional.ofNullable(snapshot);
    }

    public static void clear() {
        snapshot = null;
    }
}
