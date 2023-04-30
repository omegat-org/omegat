/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2010 Didier Briel
               2014-2015 Alex Buloichik
               2017 Didier Briel
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.omegat.core.statistics.StatisticsInfo;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Language;

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
    void saveProject(boolean doTeamSync);

    /**
     * Prepare for team synchronization from save thread.
     */
    void teamSyncPrepare() throws Exception;

    /**
     * Check if team synchronization prepared.
     */
    boolean isTeamSyncPrepared();

    /**
     * Execute synchronization.
     */
    void teamSync();

    /**
     * Close project.
     */
    void closeProject();

    /**
     * Create translated documents.
     */
    void compileProject(String sourcePattern) throws Exception;

    /**
     * Builds translated files corresponding to sourcePattern and creates fresh TM files.
     *
     * @param sourcePattern
     *            The regexp of files to create
     * @param doPostProcessing
     *            Whether or not we should perform external post-processing.
     * @param commitTargetFiles
     *            Whether or not we should commit target files
     * @throws Exception
     */
    void compileProjectAndCommit(String sourcePattern, boolean doPostProcessing, boolean commitTargetFiles)
            throws Exception;

    /**
     * Tells whether a project is a team project
     * @return whether the project is a team project
     */
    boolean isRemoteProject();

    /**
     * Commit source files in a team project.
     * @throws java.lang.Exception
     */
    void commitSourceFiles() throws Exception;

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
     * Set translation for entry.
     *
     * Optimistic locking will not be checked.
     *
     * @param entry
     *            entry
     * @param trans
     *            translation. It can't be null
     */
    void setTranslation(SourceTextEntry entry, PrepareTMXEntry trans, boolean defaultTranslation,
            TMXEntry.ExternalLinked externalLinked);

    /**
     * Set translation for entry with optimistic lock checking: if previous translation is not the same like
     * in storage, OptimisticLockingFail exception will be generated. Use when user has typed a new
     * translation.
     *
     * @param entry
     *            entry
     * @param trans
     *            translation. It can't be null
     */
    void setTranslation(SourceTextEntry entry, PrepareTMXEntry trans, boolean defaultTranslation,
            TMXEntry.ExternalLinked externalLinked, AllTranslations previousTranslations)
            throws OptimisticLockingFail;

    /**
     * Change note only for translation.
     *
     * @param entry
     *            entry
     * @param oldTrans
     *            old translation
     * @param note
     *            note text
     */
    void setNote(SourceTextEntry entry, TMXEntry oldTrans, String note);

    /**
     * Get statistics for project.
     *
     * @return
     */
    StatisticsInfo getStatistics();

    /**
     * Get translation info for specified entry. It looks first for multiple, then for default. This method
     * ALWAYS returns TMXEntry, because note can exist even for non-translated segment. Use
     * TMXEntry.isTranslated() for check if translation text really exist. Translation can be checked for
     * default/alternative by the TMXEntry.defaultTranslation.
     *
     * @param ste
     *            source entry
     * @return translation
     */
    TMXEntry getTranslationInfo(SourceTextEntry ste);

    /**
     * Get default and alternative translations for optimistic locking.
     */
    AllTranslations getAllTranslations(SourceTextEntry ste);

    /**
     * Iterate by all default translations in project.
     */
    void iterateByDefaultTranslations(DefaultTranslationsIterator it);

    /**
     * Iterate by all multiple translations in project.
     */
    void iterateByMultipleTranslations(MultipleTranslationsIterator it);

    /**
     * Check if orphaned.
     */
    boolean isOrphaned(String source);

    /**
     * Check if orphaned.
     */
    boolean isOrphaned(EntryKey entry);

    /**
     * Get all translation memories from /tm/ folder.
     *
     * @return translation memories
     */
    Map<String, ExternalTMX> getTransMemories();

    /**
     * Get all translation memories from /other_lang/ folder.
     *
     * @return translation memories
     */
    Map<Language, ProjectTMX> getOtherTargetLanguageTMs();

    /**
     * Get info about each source file in project. It's unmodifiable list, so, there is no need
     * synchronization to read it.
     */
    List<FileInfo> getProjectFiles();

    /**
     * For a given source file, calculate the path of the target file that would be created
     * by running Create Translated Documents (the file may not exist yet).
     * <p>
     * The target path must be calculated because it can depend on project properties such
     * as the target language, etc.
     *
     * @param sourceFile The relative path (under the <code>source</code> directory) of the
     * source file, e.g. <code>Bundle.properties</code>
     * @return The relative path (under the <code>target</code> directory) of the corresponding
     * target file, e.g. <code>Bundle_fr_FR.properties</code>
     */
    String getTargetPathForSourceFile(String sourceFile);

    /**
     * Get ordered list of source file names.
     */
    List<String> getSourceFilesOrder();

    /**
     * Set ordered list of source file names.
     */
    void setSourceFilesOrder(List<String> filesList);

    class FileInfo {
        public String filePath;
        /**
         * IFilter implementing Class that was used to parse the file
         */
        public Class<?> filterClass;
        /**
         * Human readable name of the file format as defined by the filter.
         */
        public String filterFileFormatName;
        /**
         * Characterset name used for parsing the source file.
         */
        public String fileEncoding;
        public List<SourceTextEntry> entries = new ArrayList<SourceTextEntry>();
    }

    public interface DefaultTranslationsIterator {
        void iterate(String source, TMXEntry trans);
    }

    public interface MultipleTranslationsIterator {
        void iterate(EntryKey source, TMXEntry trans);
    }

    /**
     * These translations can't be null. Only value or EMPTY_TRANSLATION.
     */
    class AllTranslations {
        protected TMXEntry defaultTranslation;
        protected TMXEntry alternativeTranslation;
        protected TMXEntry currentTranslation;

        public TMXEntry getDefaultTranslation() {
            return defaultTranslation;
        }

        public TMXEntry getAlternativeTranslation() {
            return alternativeTranslation;
        }

        public TMXEntry getCurrentTranslation() {
            return currentTranslation;
        }
    }

    /**
     * Exception for optimistic locking fail. Used when segment changed remotely or by automatic translation,
     * but user also changed data.
     */
    @SuppressWarnings("serial")
    class OptimisticLockingFail extends Exception {
        private final String oldTranslationText;
        private final String newTranslationText;
        private final AllTranslations previous;

        public OptimisticLockingFail(String oldTranslationText, String newTranslationText,
                AllTranslations previous) {
            this.oldTranslationText = oldTranslationText;
            this.newTranslationText = newTranslationText;
            this.previous = previous;
        }

        public String getOldTranslationText() {
            return oldTranslationText;
        }

        public String getNewTranslationText() {
            return newTranslationText;
        }

        public AllTranslations getPrevious() {
            return previous;
        }
    }
}
