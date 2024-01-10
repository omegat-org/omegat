/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.omegat.core.data.TMXEntry.ExternalLinked;
import org.omegat.core.statistics.StatisticsInfo;
import org.omegat.filters2.TranslationException;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Language;

/**
 * Project implementation when project not really loaded.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 */
public class NotLoadedProject implements IProject {

    protected static final TMXEntry EMPTY_TRANSLATION;
    static {
        PrepareTMXEntry empty = new PrepareTMXEntry();
        empty.source = "";
        EMPTY_TRANSLATION = new TMXEntry(empty, true, null);
    }

    @Override
    public void compileProject(String sourcePattern) throws IOException, TranslationException {
    }

    @Override
    public boolean isRemoteProject() {
        return false;
    }

    @Override
    public void compileProjectAndCommit(String sourcePattern, boolean doPostProcessing, boolean commitTargetFiles)
            throws Exception {
    }

    @Override
    public void commitSourceFiles() throws Exception {
    }

    @Override
    public void closeProject() {
    }

    public void decreaseTranslated() {
    }

    public List<SourceTextEntry> getAllEntries() {
        return null;
    }

    public List<StringEntry> getUniqueEntries() {
        return null;
    }

    public TMXEntry getTranslationInfo(SourceTextEntry ste) {
        return null;
    }

    public AllTranslations getAllTranslations(SourceTextEntry ste) {
        return null;
    }

    public void iterateByDefaultTranslations(DefaultTranslationsIterator it) {
    }

    public void iterateByMultipleTranslations(MultipleTranslationsIterator it) {
    }

    public void setNote(SourceTextEntry entry, TMXEntry oldTrans, String note) {
    }

    public boolean isOrphaned(String source) {
        return false;
    }

    public boolean isOrphaned(EntryKey entry) {
        return false;
    }

    public Map<String, ExternalTMX> getTransMemories() {
        return null;
    }

    public Map<Language, ProjectTMX> getOtherTargetLanguageTMs() {
        return null;
    }

    public List<FileInfo> getProjectFiles() {
        return null;
    }

    public ProjectProperties getProjectProperties() {
        return null;
    }

    public StatisticsInfo getStatistics() {
        return null;
    }

    public void increaseTranslated() {
    }

    public boolean isProjectLoaded() {
        return false;
    }

    public boolean isProjectModified() {
        return false;
    }

    public void saveProject(boolean doTeamSync) {
    }

    public void saveProjectProperties() throws IOException {
    }

    public void setTranslation(SourceTextEntry entry, PrepareTMXEntry trans, boolean defaultTranslation,
            ExternalLinked externalLinked) {
    }

    public void setTranslation(SourceTextEntry entry, PrepareTMXEntry trans, boolean defaultTranslation,
            ExternalLinked externalLinked, AllTranslations previousTranslations) throws OptimisticLockingFail {
    }

    public ITokenizer getSourceTokenizer() {
        return null;
    }

    public ITokenizer getTargetTokenizer() {
        return null;
    }

    public void findNonUniqueSegments() {
    }

    public List<String> getSourceFilesOrder() {
        return null;
    }

    public void setSourceFilesOrder(List<String> filesList) {
    }

    @Override
    public String getTargetPathForSourceFile(String sourceFile) {
        return null;
    }

    @Override
    public boolean isTeamSyncPrepared() {
        return false;
    }

    @Override
    public void teamSync() {
    }

    @Override
    public void teamSyncPrepare() throws Exception {
    }
}
