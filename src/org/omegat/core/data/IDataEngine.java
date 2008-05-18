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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.omegat.core.matching.SourceTextEntry;
import org.omegat.filters2.TranslationException;

/**
 * Interface for access to data engine funtionality.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public interface IDataEngine {
    /**
     * Create new project.
     * 
     * This method should be called in UI thread, because DataEngine will show
     * project settings dialog. It's not good behavior and it should be chanegd
     * in future.
     * 
     * TODO: rewrite for display dialog before really create project
     */
    void createProject(File newProjectDir);

    /**
     * Loads project in a "big" sense -- loads project's properties, glossaryes,
     * tms, source files etc.
     * 
     * @param projectRoot
     *                The folder where the project resides.
     */
    void loadProject(String projectDir) throws Exception;

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
     * Mark project as dirty, i.e. translated, then project should be saved.
     */
    void markAsDirty();
    
    void increaseTranslated();
    
    void decreaseTranslated();
    
    /**
     * Get all source segments.
     */
    List<SourceTextEntry> getAllEntries();
    
    /**
     * Get statistics for project.
     * @return
     */
    StatisticsInfo getStatistics();
    
    /**
     * Get all unique segments.
     * 
     * @return read-only list of project entries, or null if project not loaded
     */
    List<StringEntry> getAllTranslations();

    /**
     * Get all translation memory objects.
     * 
     * @return read-only list of translation memories, or null if project not
     *         loaded
     */
    List<LegacyTM> getMemory();

    List<FileInfo> getProjectFiles();

    public static class FileInfo {
        public String filePath;
        public int firstEntryIndex;
        public int size;
    }
}
