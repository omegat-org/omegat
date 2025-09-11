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

import org.madlonkay.supertmxmerge.StmProperties;
import org.madlonkay.supertmxmerge.SuperTmxMerge;
import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.SyncTMX;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.TMXReader2;

import java.io.File;

public class TMXRebaseOperation implements IRebaseOperation {
    private final ProjectTMX projectTMX;
    private final ProjectProperties config;
    private final StringBuilder commitDetails;
    private ProjectTMX baseTMX;
    private ProjectTMX headTMX;

    public TMXRebaseOperation(ProjectTMX projectTMX, ProjectProperties config) {
        this.projectTMX = projectTMX;
        this.config = config;
        this.commitDetails = new StringBuilder("Translated by ").append(
                Preferences.getPreferenceDefault(Preferences.TEAM_AUTHOR, System.getProperty("user.name")));
    }

    @Override
    public void parseBaseFile(File file) throws Exception {
        baseTMX = new ProjectTMX();
        baseTMX.load(config.getSourceLanguage(), config.getTargetLanguage(),
                config.isSentenceSegmentingEnabled(), file, Core.getSegmenter());
    }

    @Override
    public void parseHeadFile(File file) throws Exception {
        headTMX = new ProjectTMX();
        headTMX.load(config.getSourceLanguage(), config.getTargetLanguage(),
                config.isSentenceSegmentingEnabled(), file, Core.getSegmenter());
    }

    @Override
    public void rebaseAndSave(File tempOut) throws Exception {
        // Rebase-merge and immediately save mergedTMX
        // to tempOut.
        // It shall not save it as the projectTMX here.
        ProjectTMX mergedTMX = mergeTMX(baseTMX, headTMX, commitDetails);
        mergedTMX.exportTMX(config, tempOut, false, false, true);
    }

    @Override
    public void reload(File file) throws Exception {
        ProjectTMX newTMX = new ProjectTMX();
        newTMX.load(config.getSourceLanguage(), config.getTargetLanguage(),
                config.isSentenceSegmentingEnabled(), file, Core.getSegmenter());
        projectTMX.replaceContent(newTMX);
    }

    @Override
    public String getCommentForCommit() {
        return commitDetails.toString();
    }

    @Override
    public String getFileCharset(File file) throws Exception {
        return TMXReader2.detectCharset(file);
    }

    /**
     * Do 3-way merge of:
     * <dl>
     * <dt>Base:</dt><dd>baseTMX</dd>
     * <dt>File 1:</dt><dd>projectTMX (mine)</dd>
     * <dt>File 2:</dt><dd>headTMX (theirs)</dd>
     * </dl>
     */
    protected ProjectTMX mergeTMX(ProjectTMX baseTMX, ProjectTMX headTMX, StringBuilder commitDetails) {
        ProjectTMX mergedTMX;
        StmProperties props = new StmProperties().setLanguageResource(OStrings.getResourceBundle())
                .setParentWindow(Core.getMainWindow().getApplicationFrame())
                // More than this number of conflicts will trigger List View by
                // default.
                .setListViewThreshold(5);
        String srcLang = config.getSourceLanguage().getLanguage();
        String trgLang = config.getTargetLanguage().getLanguage();
        mergedTMX = SuperTmxMerge.merge(
                new SyncTMX(baseTMX, OStrings.getString("TMX_MERGE_BASE"), srcLang, trgLang),
                new SyncTMX(projectTMX, OStrings.getString("TMX_MERGE_MINE"), srcLang, trgLang),
                new SyncTMX(headTMX, OStrings.getString("TMX_MERGE_THEIRS"), srcLang, trgLang), props);
        if (Log.isDebugEnabled()) {
            Log.logDebug("Merge report: {0}", props.getReport());
        }
        commitDetails.append('\n');
        commitDetails.append(props.getReport().toString());
        return mergedTMX;
    }
}
