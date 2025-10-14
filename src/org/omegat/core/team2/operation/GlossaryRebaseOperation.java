/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008-2016 Alex Buloichik
               2012 Martin Fleurke
               2013-2017 Aaron Madlon-Kay
               2023 Briac Pilpre
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

package org.omegat.core.team2.operation;

import org.omegat.core.data.ProjectProperties;
import org.omegat.gui.glossary.GlossaryEntry;
import org.omegat.gui.glossary.GlossaryReaderTSV;
import org.omegat.util.Log;
import org.omegat.util.Preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GlossaryRebaseOperation implements IRebaseOperation {
    private final ProjectProperties config;
    private List<GlossaryEntry> baseGlossaryEntries;
    private List<GlossaryEntry> headGlossaryEntries;

    public GlossaryRebaseOperation(ProjectProperties config) {
        this.config = config;
    }

    @Override
    public void parseBaseFile(File file) throws Exception {
        if (file.exists()) {
            baseGlossaryEntries = GlossaryReaderTSV.read(file, true);
            Log.logDebug("read {0} entries from local glossary.txt", baseGlossaryEntries.size());
        } else {
            baseGlossaryEntries = new ArrayList<>();
        }
    }

    @Override
    public void parseHeadFile(File file) throws Exception {
        if (file.exists()) {
            headGlossaryEntries = GlossaryReaderTSV.read(file, true);
            Log.logDebug("read {0} entries from remote glossaries", headGlossaryEntries.size());
        } else {
            headGlossaryEntries = new ArrayList<>();
        }
    }

    @Override
    public void rebaseAndSave(File out) throws Exception {
        File glossaryFile = config.getWritableGlossaryFile().getAsFile();
        final List<GlossaryEntry> currentGlossaryEntries;
        if (glossaryFile.exists()) {
            currentGlossaryEntries = GlossaryReaderTSV.read(glossaryFile, true);
            Log.logDebug("Read {0} current glossaries from {1}", currentGlossaryEntries.size(), glossaryFile);
        } else {
            currentGlossaryEntries = Collections.emptyList();
        }
        List<GlossaryEntry> deltaAddedGlossaryLocal = new ArrayList<>(
                currentGlossaryEntries);
        deltaAddedGlossaryLocal.removeAll(baseGlossaryEntries);
        List<GlossaryEntry> deltaRemovedGlossaryLocal = new ArrayList<>(
                baseGlossaryEntries);
        deltaRemovedGlossaryLocal.removeAll(currentGlossaryEntries);
        headGlossaryEntries.addAll(deltaAddedGlossaryLocal);
        headGlossaryEntries.removeAll(deltaRemovedGlossaryLocal);

        Log.logDebug("Update and write glossary.txt with {0} entries.", headGlossaryEntries.size());
        for (GlossaryEntry ge : headGlossaryEntries) {
            GlossaryReaderTSV.append(out, ge);
        }
    }

    @Override
    public void reload(final File file) {
        Log.logDebug("Reloading glossary file {0}", file);
        RebaseUtils.notifyGlossaryManagerFileChanged(file);
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

}
