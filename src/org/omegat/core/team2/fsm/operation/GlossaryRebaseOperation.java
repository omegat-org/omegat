/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Zoltan Bartko
               2008-2016 Alex Buloichik
               2009-2010 Didier Briel
               2012 Guido Leenders, Didier Briel, Martin Fleurke
               2013 Aaron Madlon-Kay, Didier Briel
               2014 Aaron Madlon-Kay, Didier Briel
               2015 Aaron Madlon-Kay
               2017-2018 Didier Briel
               2018 Enrique Estevez Fernandez
               2019 Thomas Cordonnier
               2020 Briac Pilpre
               2025 Hiroshi Miura
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

package org.omegat.core.team2.fsm.operation;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.gui.glossary.GlossaryEntry;
import org.omegat.gui.glossary.GlossaryManager;
import org.omegat.gui.glossary.GlossaryReaderTSV;
import org.omegat.util.Log;
import org.omegat.util.Preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Glossary file rebase operation that handles glossary entry merging.
 */
public class GlossaryRebaseOperation implements IRebaseOperation {
    private final ProjectProperties config;
    private List<GlossaryEntry> baseGlossaryEntries;
    private List<GlossaryEntry> headGlossaryEntries;
    private List<GlossaryEntry> currentGlossaryEntries;

    public GlossaryRebaseOperation(ProjectProperties config) {
        this.config = config;
    }

    @Override
    public void parseBaseFile(File file) throws Exception {
        if (file.exists()) {
            baseGlossaryEntries = GlossaryReaderTSV.read(file, true);
            Log.logDebug("read {0} entries from base glossary", baseGlossaryEntries.size());
        } else {
            baseGlossaryEntries = new ArrayList<>();
        }
    }

    @Override
    public void parseHeadFile(File file) throws Exception {
        if (file.exists()) {
            headGlossaryEntries = GlossaryReaderTSV.read(file, true);
            Log.logDebug("read {0} entries from head glossary", headGlossaryEntries.size());
        } else {
            headGlossaryEntries = new ArrayList<>();
        }
    }

    @Override
    public void rebaseAndSave(File out) throws Exception {
        // Load current glossary entries
        File glossaryFile = config.getWritableGlossaryFile().getAsFile();
        if (glossaryFile.exists()) {
            currentGlossaryEntries = GlossaryReaderTSV.read(glossaryFile, true);
            Log.logDebug("Read {0} current glossaries from {1}", currentGlossaryEntries.size(), glossaryFile);
        } else {
            currentGlossaryEntries = Collections.emptyList();
        }

        // Calculate local changes
        List<GlossaryEntry> deltaAddedLocal = new ArrayList<>(currentGlossaryEntries);
        deltaAddedLocal.removeAll(baseGlossaryEntries);

        List<GlossaryEntry> deltaRemovedLocal = new ArrayList<>(baseGlossaryEntries);
        deltaRemovedLocal.removeAll(currentGlossaryEntries);

        // Apply changes to head version
        headGlossaryEntries.addAll(deltaAddedLocal);
        headGlossaryEntries.removeAll(deltaRemovedLocal);

        // Write merged glossary
        Log.logDebug("Update and write glossary with {0} entries.", headGlossaryEntries.size());
        for (GlossaryEntry ge : headGlossaryEntries) {
            GlossaryReaderTSV.append(out, ge);
        }
    }

    @Override
    public void reload(File file) {
        Log.logDebug("Reloading glossary file {0}", file);
        notifyGlossaryManagerFileChanged(file);
    }

    @Override
    public String getCommentForCommit() {
        final String author = Preferences.getPreferenceDefault(
                Preferences.TEAM_AUTHOR, System.getProperty("user.name"));
        return "Glossary changes by " + author;
    }

    @Override
    public String getFileCharset(File file) throws Exception {
        return GlossaryReaderTSV.getFileEncoding(file);
    }

    private void notifyGlossaryManagerFileChanged(File file) {
        GlossaryManager gm = Core.getGlossaryManager();
        if (gm != null) {
            gm.fileChanged(file);
        }
    }
}
