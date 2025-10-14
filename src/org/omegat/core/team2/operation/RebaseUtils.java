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
import org.omegat.gui.glossary.GlossaryManager;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

import java.io.File;

public final class RebaseUtils {
    private RebaseUtils() {
    }

    public static void notifyGlossaryManagerFileChanged(File file) {
        GlossaryManager gm = Core.getGlossaryManager();
        if (gm != null) {
            gm.fileChanged(file);
        }
    }

    /**
     * Do 3-way merge of:
     * <dl>
     * <dt>Base:</dt><dd>baseTMX</dd>
     * <dt>File 1:</dt><dd>projectTMX (mine)</dd>
     * <dt>File 2:</dt><dd>headTMX (theirs)</dd>
     * </dl>
     */
    public static ProjectTMX mergeTMX(ProjectTMX projectTMX, ProjectTMX baseTMX, ProjectTMX headTMX, ProjectProperties config,
                               StringBuilder commitDetails) {
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
