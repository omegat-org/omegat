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
import org.omegat.core.team2.TeamTool;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.Log;
import org.omegat.util.Preferences;
import picocli.CommandLine;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 * @author Hiroshi Miura
 */
@CommandLine.Command(name = "team", resourceBundle = "org.omegat.cli.Parameters")
public class TeamCommand implements Callable<Integer> {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true)
    boolean usageHelpRequested = false;

    /**
     * define team init sub-subcommand.
     */
    @CommandLine.Command(name = "init")
    @SuppressWarnings("unused")
    int init(@CommandLine.Parameters(index = "0", paramLabel = "<source>") String sLang,
             @CommandLine.Parameters(index = "1", paramLabel = "<target>") String tLang,
             @CommandLine.Parameters(index = "2", paramLabel = "<dir>", arity = "0..1",
                     defaultValue = CommandLine.Option.NULL_VALUE)
             @Nullable String dir) {
        return executeInit(dir, sLang, tLang);
    }

    /**
     * Function method called from command entry.
     * 
     * @param sLang
     *            source language.
     * @param tLang
     *            target language.
     */
    private int executeInit(String dir, String sLang, String tLang) {
        CommandCommon.showStartUpLogInfo();
        Log.setLevel(Level.WARNING);

        try {
            Preferences.init();
            PluginUtils.loadPlugins(Collections.emptyMap());
            File targetDir;
            if (dir == null) {
                targetDir = new File("").getAbsoluteFile();
            } else {
                targetDir = new File(dir);
            }
            TeamTool.initTeamProject(targetDir, sLang, tLang);
            return 0;
        } catch (Exception ex) {
            Log.log(ex);
        }
        return 1;
    }

    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }
}
