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
import org.omegat.core.data.RealProject;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.util.Log;
import org.omegat.util.RuntimePreferences;
import picocli.CommandLine;

import java.util.Objects;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "translate", resourceBundle = "org.omegat.cli.Parameters")
public class TranslateCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    @Nullable LegacyParameters legacyParams;

    @CommandLine.Parameters(index = "0", paramLabel = "<project>", defaultValue = CommandLine.Option.NULL_VALUE)
    @Nullable String project;

    @CommandLine.Mixin
    @Nullable Parameters params;

    @Override
    public Integer call() {
        if (params == null || legacyParams == null) {
            return 1;
        }
        legacyParams.initialize();
        params.setProjectLocation(Objects.requireNonNullElse(project, "."));
        params.initialize();
        return runConsoleTranslate();
    }

    /**
     * Execute in console mode for translate.
     */
    int runConsoleTranslate() {
        if (params == null || legacyParams == null) {
            return 1;
        }
        Log.logInfoRB("STARTUP_CONSOLE_TRANSLATION_MODE");

        if (!params.team) {
            RuntimePreferences.setNoTeam();
        }

        Log.logInfoRB("CONSOLE_INITIALIZING");
        Core.initializeConsole();

        RealProject p = Common.selectProjectConsoleMode(true, params);

        Common.validateTagsConsoleMode(params);

        Log.logInfoRB("CONSOLE_TRANSLATING");

        try {
            String sourceMask = legacyParams.sourcePattern;
            p.compileProject(Objects.requireNonNullElse(sourceMask, ".*"), false);
        } catch (Exception ex) {
            Log.logErrorRB(ex, "CT_ERROR_COMPILING_PROJECT");
            return 1;
        }

        if (legacyParams.disableProjectLocking) {
            RuntimePreferences.setProjectLockingEnabled(false);
        }
        if (legacyParams.disableLocationSave) {
            RuntimePreferences.setLocationSaveEnabled(false);
        }

        // Called *after* executing post processing command (unlike the
        // regular PROJECT_CHANGE_TYPE.COMPILE)
        Common.executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE.COMPILE, params);

        p.closeProject();
        Common.executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE, params);
        Log.logInfoRB("CONSOLE_FINISHED");

        return 0;
    }
}
