/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.core.data;

import java.io.IOException;
import java.util.List;

import org.omegat.core.data.stat.StatisticsInfo;
import org.omegat.filters2.TranslationException;

/**
 * Interface for access to loaded project. Each loaded project will be new
 * instance of IProject.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public interface IProject {

    /**
     * Save project properties only.
     */
    void saveProjectProperties() throws IOException;

    /**
     * Save project.
     */
    void saveProject();

    /**
     * Close project.
     */
    void closeProject();

    /**
     * Create translated documents.
     */
    void compileProject() throws IOException, TranslationException;

    /**
     * Get project properties.
     * 
     * @return project properties
     */
    ProjectProperties getProjectProperties();

    /**
     * Get project loaded status.
     * 
     * @return true if project loaded
     */
    boolean isProjectLoaded();

    /**
     * Is project modified ?
     */
    boolean isProjectModified();

    /**
     * Get all source segments. It's unmodifiable list, so, there is no need
     * synchronization to read it.
     */
    List<SourceTextEntry> getAllEntries();

    /**
     * Set translation for entry.
     * 
     * @param entry entry
     * @param trans translation
     */
    void setTranslation(SourceTextEntry entry, String trans);

    /**
     * Get statistics for project.
     * 
     * @return
     */
    StatisticsInfo getStatistics();

    /**
     * Get all unique segments.
     * 
     * @return read-only list of project entries, or null if project not loaded
     */
    List<StringEntry> getUniqueEntries();

    /**
     * Get TM files from /tm/*.tmx dir.
     * 
     * @return read-only list of translation memories, or null if project not
     *         loaded
     */
    List<LegacyTM> getMemory();

    /**
     * Entries from all /tm/*.tmx files and orphaned from project_save.tmx.
     * 
     * @return list of additional memories
     */
    List<TransMemory> getTransMemory();

    /**
     * Get info about each source file in project. It's unmodifiable list, so,
     * there is no need synchronization to read it.
     */
    List<FileInfo> getProjectFiles();

    public static class FileInfo {
        public String filePath;
        public int firstEntryIndex;
        public int size;
    }
}
