/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Briac Pilpre
               2015 Aaron Madlon-Kay
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
package org.omegat.core.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.omegat.core.Core;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.gui.editor.IEditor;
import org.omegat.util.OConsts;

/**
 * @author Briac Pilpre
 * @author Aaron Madlon-Kay
 */
public final class LastSegmentManager {

    private LastSegmentManager() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LastSegmentManager.class);

    private static final String LAST_ENTRY_SRC = "LAST_ENTRY_SRC";
    private static final String LAST_ENTRY_FILE = "LAST_ENTRY_FILE";
    private static final String LAST_ENTRY_NUMBER = "LAST_ENTRY_NUMBER";

    private static File getLastEntryFile() {
        return new File(Core.getProject().getProjectProperties().getProjectInternal(),
                OConsts.LAST_ENTRY_NUMBER);
    }

    /**
     * Save current entry position for repositioning on reload (RFE#35). This
     * method is called from RealProject during saveProject(boolean)
     */
    public static void saveLastSegment() {
        Properties prop = new Properties();
        IEditor editor = Core.getEditor();

        int lastEntryNumber = editor.getCurrentEntryNumber();
        String currentFile = editor.getCurrentFile();

        if (currentFile == null) {
            // Project has no files, no need to save.
            return;
        }

        SourceTextEntry ste = editor.getCurrentEntry();
        if (ste == null) {
            return;
        }

        prop.put(LAST_ENTRY_SRC, ste.getSrcText());
        prop.put(LAST_ENTRY_NUMBER, Integer.toString(lastEntryNumber, 10));
        prop.put(LAST_ENTRY_FILE, currentFile);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(getLastEntryFile());
            prop.store(fos, null);
        } catch (Exception e) {
            LOGGER.atDebug().setMessage("Could not write the last entry number: {}")
                    .addArgument(e::getMessage).log();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    LOGGER.atWarn().log("", ex);
                }
            }
        }
    }

    /**
     * Read the user's last-visited segment number from persistent storage. The
     * segment number is the user-visible number that starts with 1.
     *
     * @return The segment number (starts from 1)
     */
    public static int getLastSegmentNumber() {
        File lastEntryFile = getLastEntryFile();

        if (!lastEntryFile.exists()) {
            return 1;
        }

        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(lastEntryFile)) {
            prop.load(fis);
        } catch (IOException e) {
            LOGGER.atDebug().setMessage("Could not load last segment info {}").addArgument(e::getMessage)
                    .log();
            return 1;
        }

        Core.getMainWindow().showStatusMessageRB("MW_JUMPING_LAST_ENTRY");
        int lastEntryNumber = 1;
        try {
            String lastEntry = prop.getProperty(LAST_ENTRY_NUMBER, "1");
            lastEntryNumber = Integer.parseInt(lastEntry, 10);
        } catch (Exception e) {
            LOGGER.atDebug().setMessage("Cannot jump to last entry #{}: {}").addArgument(lastEntryNumber)
                    .addArgument(e::getMessage).log();
        }
        LOGGER.atDebug().log("Jumping to last entry #{}.", lastEntryNumber);

        List<SourceTextEntry> allEntries = Core.getProject().getAllEntries();

        if (allEntries.size() < lastEntryNumber) {
            LOGGER.atDebug().log("Not enough segments to jump to {}", lastEntryNumber);
            Core.getMainWindow().showStatusMessageRB(null);
            return 1;
        }

        SourceTextEntry propEntry = allEntries.get(lastEntryNumber - 1);

        String lastFile = prop.getProperty(LAST_ENTRY_FILE, "");
        String lastSrc = prop.getProperty(LAST_ENTRY_SRC, "");

        // Best case scenario, segment matches src (if it doesn't match
        // filename, it's still okay)
        if (propEntry.getSrcText().equals(lastSrc)) {
            // gotoEntry(propEntry.entryNum(), editor);
            Core.getMainWindow().showStatusMessageRB(null);
            return propEntry.entryNum();
        }

        // Check to see if the source and file match
        LOGGER.atDebug().setMessage("Last entry #{} mismatch").addArgument(lastEntryNumber)
                .addKeyValue("file", lastFile).addKeyValue("src", lastSrc).log();

        int fileIndex = fileIndex(lastFile);

        if (fileIndex == -1) {
            LOGGER.atDebug().log("File \"{}\" is not in the project anymore.", lastFile);
            Core.getMainWindow().showStatusMessageRB(null);
            return 1;
        }

        // We landed in the right file, just not the right segment
        List<SourceTextEntry> fileEntries = Core.getProject().getProjectFiles().get(fileIndex).entries;
        for (SourceTextEntry entry : fileEntries) {
            if (entry.getSrcText().equals(lastSrc)) {
                LOGGER.atDebug().log("Found a matching entry in the right file.");
                return entry.entryNum();
            }
        }

        // Things look bad, nothing in the matching file. Look in all the
        // project or quit ?
        for (SourceTextEntry entry : allEntries) {
            if (entry.getSrcText().equals(lastSrc)) {
                LOGGER.atDebug().log("Found a matching entry in the wrong file.");
                return entry.entryNum();
            }
        }

        Core.getMainWindow().showStatusMessageRB(null);
        return 1;
    }

    /**
     * Get the project index given its name, returns -1 if the file is not found
     */
    private static int fileIndex(String filename) {

        int fileIndex = 0;
        for (FileInfo file : Core.getProject().getProjectFiles()) {
            if (file.filePath.equals(filename)) {
                return fileIndex;
            }
            fileIndex++;
        }

        return -1;
    }
}
