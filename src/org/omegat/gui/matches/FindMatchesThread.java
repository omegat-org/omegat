/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Alex Buloichik
               2012 Thomas Cordonnier, Martin Fleurke
               2013 Aaron Madlon-Kay
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

package org.omegat.gui.matches;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.omegat.core.data.IProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IStopped;
import org.omegat.core.matching.NearString;
import org.omegat.core.statistics.FindMatches;
import org.omegat.gui.common.EntryInfoSearchThread;
import org.omegat.util.OConsts;

/**
 * Find matches in separate thread then show result in the matches pane.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class FindMatchesThread extends EntryInfoSearchThread<List<NearString>> {
    private static final Logger LOGGER = Logger.getLogger(FindMatchesThread.class.getName());

    /** Current project. */
    private final IProject project;

    /**
     * Entry which is processed currently.
     * 
     * If entry in controller was changed, it means user has moved to another entry, and there is no sense to
     * continue.
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
            return new ArrayList<NearString>();
        }

        if (project.getSourceTokenizer() == null) {
            return null;
        }

        long before = 0;
        if (LOGGER.isLoggable(Level.FINER)) {
            // only if need to be logged
            before = System.currentTimeMillis();
        }

        try {
            FindMatches finder = new FindMatches(project.getSourceTokenizer(), OConsts.MAX_NEAR_STRINGS, true, false);
            List<NearString> result = finder.search(project, processedEntry.getSrcText(), true, true,
                    new IStopped() {
                        public boolean isStopped() {
                            return isEntryChanged();
                        }
                    });

            if (LOGGER.isLoggable(Level.FINER)) {
                // only if need to be logged
                long after = System.currentTimeMillis();
                LOGGER.finer("Time for find matches: " + (after - before));
            }
            return result;
        } catch (FindMatches.StoppedException ex) {
            throw new EntryChangedException();
        }
    }
}
