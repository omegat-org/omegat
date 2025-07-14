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

import org.jetbrains.annotations.Nullable;
import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.RealProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXWriter2;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "pseudo")
public class PseudoTranslateCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    @Nullable LegacyParameters legacyParameters;

    @CommandLine.Mixin
    @Nullable Parameters params;

    @Override
    public Integer call() {
        if (params == null || legacyParameters == null) {
            return 1;
        }
        legacyParameters.initialize();
        params.initialize();
        try {
            return runCreatePseudoTranslateTMX();
        } catch (Exception ex) {
            Log.logErrorRB(ex, "CT_ERROR_CREATING_PSEUDO_TRANSLATE_TMX",
                    params.projectLocation == null ? "" : params.projectLocation);
            return 1;
        }
    }

    /**
     * Execute in console mode for translate.
     */
    int runCreatePseudoTranslateTMX() throws Exception {
        if (params == null || legacyParameters == null) {
            return 1;
        }
        Log.logInfoRB("CONSOLE_PSEUDO_TRANSLATION_MODE");

        Log.logInfoRB("CONSOLE_INITIALIZING");
        Core.initializeConsole();

        RealProject p = Common.selectProjectConsoleMode(true, params);

        Common.validateTagsConsoleMode(params);

        Log.logInfoRB("CONSOLE_CREATE_PSEUDOTMX");

        ProjectProperties config = p.getProjectProperties();
        List<SourceTextEntry> entries = p.getAllEntries();
        String pseudoTranslateTMXFilename = legacyParameters.pseudoTranslateTmxPath;
        String pseudoTranslateType = legacyParameters.pseudoTranslateTypeName;

        String fname;
        if (pseudoTranslateTMXFilename != null && !StringUtil.isEmpty(pseudoTranslateTMXFilename)) {
            if (!pseudoTranslateTMXFilename.endsWith(OConsts.TMX_EXTENSION)) {
                fname = pseudoTranslateTMXFilename + "." + OConsts.TMX_EXTENSION;
            } else {
                fname = pseudoTranslateTMXFilename;
            }
        } else {
            fname = "";
        }

        // Write OmegaT-project-compatible TMX:
        try (TMXWriter2 wr = new TMXWriter2(new File(fname), config.getSourceLanguage(),
                config.getTargetLanguage(), config.isSentenceSegmentingEnabled(), false, false)) {
            for (SourceTextEntry ste : entries) {
                if ("equal".equalsIgnoreCase(pseudoTranslateType)) {
                    wr.writeEntry(ste.getSrcText(), ste.getSrcText(), "", "", 0, "", 0, null);
                } else if ("empty".equalsIgnoreCase(pseudoTranslateType)) {
                    wr.writeEntry(ste.getSrcText(), "", "", "", 0, "", 0, null);
                }
            }
        } catch (IOException e) {
            Log.logErrorRB(e, "CT_ERROR_CREATING_TMX");
            throw new IOException(OStrings.getString("CT_ERROR_CREATING_TMX") + "\n" + e.getMessage());
        }
        p.closeProject();
        Log.logInfoRB("CONSOLE_FINISHED");
        return 0;
    }

}
