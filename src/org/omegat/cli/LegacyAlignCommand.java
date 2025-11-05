/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

package org.omegat.cli;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.RealProject;
import org.omegat.util.FileUtil;
import org.omegat.util.Log;
import org.omegat.util.RuntimePreferences;
import org.omegat.util.TMXWriter2;

import java.io.File;

public class LegacyAlignCommand {

    LegacyParameters legacyParams;

    public LegacyAlignCommand(LegacyParameters legacyParams) {
        this.legacyParams = legacyParams;
    }

    int runConsoleAlign() throws Exception {
        if (legacyParams.alignDirPath == null) {
            Log.logErrorRB("CONSOLE_TRANSLATED_FILES_LOC_UNDEFINED");
            return 1;
        }

        CommandCommon.initializeApp();
        Core.initializeConsole();

        if (legacyParams.noTeam) {
            RuntimePreferences.setNoTeam();
        }
        if (legacyParams.disableProjectLocking) {
            RuntimePreferences.setProjectLockingEnabled(false);
        }
        if (legacyParams.disableLocationSave) {
            RuntimePreferences.setLocationSaveEnabled(false);
        }

        CommonParameters params = new CommonParameters();
        params.setProjectLocation(legacyParams.project);

        RealProject p = CommandCommon.selectProjectConsoleMode(true, params);

        Log.logInfoRB("CONSOLE_ALIGN_AGAINST", legacyParams.alignDirPath);

        String tmxFile = p.getProjectProperties().getProjectInternal() + "align.tmx";
        ProjectProperties config = p.getProjectProperties();
        boolean alt = !config.isSupportDefaultTranslations();
        try (TMXWriter2 wr = new TMXWriter2(new File(tmxFile), config.getSourceLanguage(),
                config.getTargetLanguage(), config.isSentenceSegmentingEnabled(), alt, alt)) {
            wr.writeEntries(p.align(config, new File(FileUtil.expandTildeHomeDir(legacyParams.alignDirPath))),
                    alt);
        }
        p.closeProject();
        Log.logInfoRB("CONSOLE_FINISHED");
        return 0;
    }

}
