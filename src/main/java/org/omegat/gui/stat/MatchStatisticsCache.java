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

    /**
     * One finished scan, wrapped with the cache-specific metadata to judge
     * its validity: the root folder of the project the scan belongs to and
     * the scan time. The statistics data itself is the already-copied
     * {@link MatchStatisticsResult}.
     */
    public record Entry(MatchStatisticsResult result, String projectRoot, Instant lastScan) {
    }

    private static volatile @Nullable Entry entry;

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

    public static void store(MatchStatisticsResult result, String projectRoot) {
        entry = new Entry(result, projectRoot, Instant.now());
    }

    public static Optional<Entry> get() {
        return Optional.ofNullable(entry);
    }

    public static void clear() {
        entry = null;
    }
}
