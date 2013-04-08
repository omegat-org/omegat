/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008      Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.data;

import gen.core.filters.Filters;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.omegat.core.segmentation.SRX;
import org.omegat.core.statistics.StatisticsInfo;
import org.omegat.core.team.IRemoteRepository;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Language;

/**
 * Project implementation when project not really loaded.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class NotLoadedProject implements IProject {

    public void compileProject(String sourcePattern) throws IOException, TranslationException {
    }

    public void closeProject() {
    }
    
    public IRemoteRepository getRepository() {
        return null;
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

    public void iterateByDefaultTranslations(DefaultTranslationsIterator it) {
    }

    public void iterateByMultipleTranslations(MultipleTranslationsIterator it) {
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

    public void saveProject() {
    }

    public void saveProjectProperties() throws IOException {
    }

    public void setTranslation(SourceTextEntry entry, String trans, String note, boolean isDefault) {
    }

    public ITokenizer getSourceTokenizer() {
        return null;
    }

    public ITokenizer getTargetTokenizer() {
        return null;
    }

    public void findNonUniqueSegments() {
    }
}
