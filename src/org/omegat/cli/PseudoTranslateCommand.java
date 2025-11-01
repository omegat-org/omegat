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
import org.omegat.util.RuntimePreferences;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXWriter2;
import picocli.CommandLine;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "pseudo", resourceBundle = "org.omegat.cli.Parameters")
public class PseudoTranslateCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    @Nullable LegacyParameters legacyParameters;

    @CommandLine.Mixin
    @Nullable CommonParameters params;

    @CommandLine.Option(names = { "--type" }, paramLabel = "<type>", defaultValue = "empty",
            descriptionKey = "PSEUDO_TRANSLATE_TYPE")
    @Nullable String type;

    @CommandLine.Option(names = { "--output-file" }, paramLabel = "<filename>", descriptionKey = "PSEUDO_TRANSLATE_TMX")
    @Nullable String filename;

    @CommandLine.Parameters(index = "0", paramLabel = "<project>", defaultValue = CommandLine.Option.NULL_VALUE)
    @Nullable String project;

    @Override
    public Integer call() {
        if (params == null || legacyParameters == null) {
            return 1;
        }
        legacyParameters.initialize();
        params.setProjectLocation(project);
        try {
            return runCreatePseudoTranslateTMX();
        } catch (Exception ex) {
            Log.logErrorRB(ex, "CT_ERROR_CREATING_PSEUDO_TRANSLATE_TMX",
                    params == null || params.projectLocation == null ? "" : params.projectLocation);
            return 1;
        }
    }

    /**
     * Execute in console mode for translate.
     */
    int runCreatePseudoTranslateTMX() {
        if (params == null || legacyParameters == null) {
            return 1;
        }
        CommandCommon.showStartUpLogInfo();
        CommandCommon.logLevelInitialize(params);
        Log.logInfoRB("CONSOLE_PSEUDO_TRANSLATION_MODE");

        if (!params.team || legacyParameters.noTeam) {
            RuntimePreferences.setNoTeam();
        }

        CommandCommon.initializeApp();
        Core.initializeConsole();

        if (legacyParameters.disableProjectLocking) {
            RuntimePreferences.setProjectLockingEnabled(false);
        }
        if (legacyParameters.disableLocationSave) {
            RuntimePreferences.setLocationSaveEnabled(false);
        }

        if (filename == null && legacyParameters.pseudoTranslateTmxPath != null) {
            filename = legacyParameters.pseudoTranslateTmxPath;
        }
        if (type == null && legacyParameters.pseudoTranslateTypeName != null) {
            type = legacyParameters.pseudoTranslateTypeName;
        }

        RealProject p = CommandCommon.selectProjectConsoleMode(true, params);

        CommandCommon.validateTagsConsoleMode(params);

        Log.logInfoRB("CONSOLE_CREATE_PSEUDOTMX");

        ProjectProperties config = p.getProjectProperties();
        List<SourceTextEntry> entries = p.getAllEntries();

        String fname;
        if (filename != null && !StringUtil.isEmpty(filename)) {
            if (!filename.endsWith(OConsts.TMX_EXTENSION)) {
                fname = filename + "." + OConsts.TMX_EXTENSION;
            } else {
                fname = filename;
            }
        } else {
            fname = "pseudotranslate" + OConsts.TMX_EXTENSION;
        }

        // Write OmegaT-project-compatible TMX:
        try (TMXWriter2 wr = new TMXWriter2(new File(fname), config.getSourceLanguage(),
                config.getTargetLanguage(), config.isSentenceSegmentingEnabled(), false, false)) {
            for (SourceTextEntry ste : entries) {
                if ("equal".equalsIgnoreCase(type)) {
                    wr.writeEntry(ste.getSrcText(), ste.getSrcText(), "", "", 0, "", 0, null);
                } else if ("empty".equalsIgnoreCase(type)) {
                    wr.writeEntry(ste.getSrcText(), "", "", "", 0, "", 0, null);
                }
            }
        } catch (Exception e) {
            Log.logErrorRB(e, "CT_ERROR_CREATING_PSEUDO_TRANSLATE_TMX", filename == null ? "" : filename);
            return 1;
        } finally {
            p.closeProject();
        }
        Log.logInfoRB("CONSOLE_FINISHED");
        return 0;
    }
}
