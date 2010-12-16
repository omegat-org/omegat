/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2010 Didier Briel
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omegat.core.matching.ITokenizer;
import org.omegat.core.statistics.StatisticsInfo;
import org.omegat.filters2.TranslationException;

/**
 * Interface for access to loaded project. Each loaded project will be new
 * instance of IProject.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
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
    void compileProject(String sourcePattern) throws IOException, TranslationException;

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
     * Returns tokenizer for source language.
     */
    ITokenizer getSourceTokenizer();

    /**
     * Returns tokenizer for target language.
     */
    ITokenizer getTargetTokenizer();

    /**
     * Get all source segments. It's unmodifiable list, so, there is no need
     * synchronization to read it.
     */
    List<SourceTextEntry> getAllEntries();

    /**
     * Set translation for entry. Use when user has typed a new translation.
     * 
     * @param entry
     *            entry
     * @param trans
     *            translation
     */
    void setTranslation(SourceTextEntry entry, String trans);

    /**
     * Set author and translation for entry. Use when user has typed a new
     * translation.
     * 
     * @param author
     *            author
     * @param entry
     *            entry
     * @param trans
     *            translation
     */
    void setAuthorTranslation(String author, SourceTextEntry entry, String trans);

    /**
     * Get statistics for project.
     * 
     * @return
     */
    StatisticsInfo getStatistics();

    /**
     * Get all translations for current project.
     * 
     * @return all translations map
     */
    Set<Map.Entry<String, TransEntry>> getTranslationsSet();

    /**
     * Get translation for specified entry.
     * 
     * @param ste
     *            source entry
     * @return translation, or null if translation not exist
     */
    TransEntry getTranslation(SourceTextEntry ste);

    /**
     * Get all translation memories from /tm/ folder.
     * 
     * @return translation memories
     */
    Map<String, List<TransMemory>> getTransMemories();

    /**
     * Get orphaned segments.
     * 
     * @return orphaned segments
     */
    Map<String, TransEntry> getOrphanedSegments();

    /**
     * Get info about each source file in project. It's unmodifiable list, so,
     * there is no need synchronization to read it.
     */
    List<FileInfo> getProjectFiles();

    public static class FileInfo {
        public String filePath;

        public List<SourceTextEntry> entries = new ArrayList<SourceTextEntry>();
    }
}
