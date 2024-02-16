/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Alex Buloichik
               2012 Thomas Cordonnier, Martin Fleurke
               2013 Aaron Madlon-Kay
               2024 Hiroshi Miura
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

package org.omegat.gui.matches;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.omegat.core.data.IProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IStopped;
import org.omegat.core.matching.NearString;
import org.omegat.core.statistics.FindMatches;
import org.omegat.gui.common.EntryInfoSearchThread;
import org.omegat.util.OConsts;

/**
 * Find matches in separate thread then show a result in the matches' pane.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Hiroshi Miura
 */
public class FindMatchesThread extends EntryInfoSearchThread<List<NearString>> {
    private static final Logger LOGGER = Logger.getLogger(FindMatchesThread.class.getName());

    /** Current project. */
    private final IProject project;

    /**
     * Entry which is processed currently.
     * <p>
     * If entry in controller was changed, it means the user has moved to
     * another entry, and there is no sense to continue.
     */
    private final SourceTextEntry processedEntry;

    public FindMatchesThread(final MatchesTextArea matcherPane, final IProject project,
            final SourceTextEntry entry) {
        super(matcherPane, entry);
        this.project = project;
        this.processedEntry = entry;
    }

    @Override
    protected List<NearString> search() throws Exception {
        if (!project.isProjectLoaded()) {
            // project is closed
            return Collections.emptyList();
        }

        if (project.getSourceTokenizer() == null) {
            return null;
        }

        long before = System.currentTimeMillis();

        try {
            List<NearString> result = finderSearch(project, processedEntry.getSrcText(), this::isEntryChanged);
            LOGGER.finer(() -> "Time for find matches: " + (System.currentTimeMillis() - before));
            return result;
        } catch (FindMatches.StoppedException ex) {
            throw new EntryChangedException();
        }
    }

    /**
     * Search matches (static for test purpose).
     * @param project OmegaT project.
     * @param srcText source text to look for.
     * @param isEntryChanged stop and raise StopException when it returns true.
     * @return result as a list of NearString.
     */
    protected static List<NearString> finderSearch(IProject project, String srcText, IStopped isEntryChanged) {
        FindMatches finder = new FindMatches(project, OConsts.MAX_NEAR_STRINGS, true, false);
        return finder.search(srcText, true, true, isEntryChanged);
    }
}
