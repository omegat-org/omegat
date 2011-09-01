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

import gen.core.filters.Filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.omegat.core.matching.ITokenizer;
import org.omegat.core.statistics.StatisticsInfo;
import org.omegat.filters2.master.FilterMaster;

/**
 * Interface for access to loaded project. Each loaded project will be new instance of IProject.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 */
public interface IProject {

    /**
     * Save project properties only.
     */
    void saveProjectProperties() throws Exception;

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
    void compileProject(String sourcePattern) throws Exception;

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
     * Get all source segments. It's unmodifiable list, so, there is no need synchronization to read it.
     */
    List<SourceTextEntry> getAllEntries();

    /**
     * Set translation for entry. Use when user has typed a new translation.
     * 
     * @param entry
     *            entry
     * @param trans
     *            translation
     * @param isDefault
     *            true if default translation should be changed
     */
    void setTranslation(SourceTextEntry entry, String trans, boolean isDefault);

    /**
     * Get statistics for project.
     * 
     * @return
     */
    StatisticsInfo getStatistics();

    /**
     * Get translation for specified entry. It looks first for multiple, then for default.
     * 
     * @param ste
     *            source entry
     * @return translation, or null if translation not exist
     */
    TMXEntry getTranslation(SourceTextEntry ste);

    /**
     * Get default translation for specified entry.
     * 
     * @param ste
     *            source entry
     * @return translation, or null if translation not exist
     */
    TMXEntry getDefaultTranslation(SourceTextEntry ste);
    
    /**
     * Get multiple translation for specified entry.
     * 
     * @param ste
     *            source entry
     * @return translation, or null if translation not exist
     */
    TMXEntry getMultipleTranslation(SourceTextEntry ste);

    /**
     * Get all translations for current project.
     * 
     * @return all translations map
     */
    Collection<TMXEntry> getAllTranslations();

    /**
     * Get orphaned translations.
     * 
     * @return orphaned translations
     */
    Collection<TMXEntry> getAllOrphanedTranslations();
    
    /**
     * Iterate by all default translations in project.
     */
    void iterateByDefaultTranslations(DefaultTranslationsIterator it);

    /**
     * Iterate by all multiple translations in project.
     */
    void iterateByMultipleTranslations(MultipleTranslationsIterator it);

    /**
     * Iterate by all orphaned default translations in project.
     */
    void iterateByOrphanedDefaultTranslations(DefaultTranslationsIterator it);

    /**
     * Iterate by all orphaned multiple translations in project.
     */
    void iterateByOrphanedMultipleTranslations(MultipleTranslationsIterator it);

    /**
     * Get all translation memories from /tm/ folder.
     * 
     * @return translation memories
     */
    Map<String, ExternalTMX> getTransMemories();

    /**
     * Get info about each source file in project. It's unmodifiable list, so, there is no need
     * synchronization to read it.
     */
    List<FileInfo> getProjectFiles();

    public static class FileInfo {
        public String filePath;

        public List<SourceTextEntry> entries = new ArrayList<SourceTextEntry>();
    }

    public interface DefaultTranslationsIterator {
        void iterate(String source, TMXEntry trans);
    }

    public interface MultipleTranslationsIterator {
        void iterate(EntryKey source, TMXEntry trans);
    }
    
    public FilterMaster getFilterMaster();

    public void setConfig(Filters filters);

}
