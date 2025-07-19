/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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
package org.omegat.cli;

import org.omegat.core.Core;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.RealProject;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.tagvalidation.ErrorReport;
import org.omegat.gui.scripting.ConsoleBindings;
import org.omegat.gui.scripting.ScriptItem;
import org.omegat.gui.scripting.ScriptRunner;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.ProjectFileStorage;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

final class Common {

    private Common() {
    }

    /**
     * Execute a script as PROJECT_CHANGE events. We can't use the regular
     * project listener because the SwingUtilities.invokeLater method used in
     * CoreEvents doesn't stop the project processing in console mode.
     */
    static void executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE eventType, Parameters params) {
        if (params.scriptName != null) {
            File script = new File(params.scriptName);
            Log.logInfoRB("CONSOLE_EXECUTE_SCRIPT", script, eventType);
            if (script.isFile()) {
                HashMap<String, Object> binding = new HashMap<>();
                binding.put("eventType", eventType);

                ConsoleBindings consoleBindigs = new ConsoleBindings();
                binding.put(ScriptRunner.VAR_CONSOLE, consoleBindigs);
                binding.put(ScriptRunner.VAR_GLOSSARY, consoleBindigs);
                binding.put(ScriptRunner.VAR_EDITOR, consoleBindigs);

                try {
                    String result = ScriptRunner.executeScript(new ScriptItem(script), binding);
                    Log.log(result);
                } catch (Exception ex) {
                    Log.log(ex);
                }
            } else {
                Log.logInfoRB("SCW_SCRIPT_LOAD_ERROR", "the script is not a file");
            }
        }
    }

    /**
     * Validates tags according to command line specs:
     * <code>--tag-validation=[abort|warn]</code>
     * <p>
     * On abort, the program is aborted when tag validation finds errors. On
     * warning, the errors are printed, but the program continues. In all other
     * cases, no tag validation is done.
     */
    static void validateTagsConsoleMode(Parameters params) {
        List<ErrorReport> stes;
        if ("abort".equalsIgnoreCase(params.tagValidation)) {
            System.out.println(OStrings.getString("CONSOLE_VALIDATING_TAGS"));
            stes = Core.getTagValidation().listInvalidTags();
            if (!stes.isEmpty()) {
                Core.getTagValidation().logTagValidationErrors(stes);
                System.out.println(OStrings.getString("CONSOLE_TAGVALIDATION_FAIL"));
                System.out.println(OStrings.getString("CONSOLE_TAGVALIDATION_ABORT"));
                System.exit(1);
            }
        } else if ("warn".equalsIgnoreCase(params.tagValidation)) {
            System.out.println(OStrings.getString("CONSOLE_VALIDATING_TAGS"));
            stes = Core.getTagValidation().listInvalidTags();
            if (!stes.isEmpty()) {
                Core.getTagValidation().logTagValidationErrors(stes);
                System.out.println(OStrings.getString("CONSOLE_TAGVALIDATION_FAIL"));
            }
        }
    }

    /**
     * creates the project class and adds it to the Core. Loads the project if
     * specified. An exit occurs on error loading the project. This method is
     * for the different console modes, to prevent code duplication.
     *
     * @param loadProject
     *            load the project or not
     * @return the project.
     */
    static RealProject selectProjectConsoleMode(boolean loadProject, Parameters params) {

        if (params.projectLocation == null) {
            Log.logErrorRB("PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
            System.exit(1);
        }

        // check if project okay
        ProjectProperties projectProperties = null;
        try {
            projectProperties = ProjectFileStorage
                    .loadProjectProperties(Paths.get(params.projectLocation).toFile());
            projectProperties.verifyProject();
        } catch (Exception ex) {
            Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
            System.exit(1);
        }

        RealProject p = new RealProject(projectProperties);
        Core.setProject(p);
        if (loadProject) {
            Log.logInfoRB("CONSOLE_LOADING_PROJECT");
            p.loadProject(true);
            if (!p.isProjectLoaded()) {
                Core.setProject(new NotLoadedProject());
            } else {
                executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD, params);
            }

        }
        return p;
    }

}
