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

package org.omegat.gui.scripting;

import org.jspecify.annotations.Nullable;
import org.omegat.cli.BaseSubCommand;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.util.Log;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * The ScriptingModule class is responsible for managing scripting
 * functionality, including loading and unloading scripting-related plugins and
 * executing scripts.
 */
@SuppressWarnings("unused")
public final class ScriptingModule {

    public static final String DEFAULT_SCRIPTS_DIR = "scripts";

    private ScriptingModule() {
    }

    private static final ScriptingStartupEventListener listener = new ScriptingStartupEventListener();

    public static void loadPlugins() {
        Core.registerConsoleCommand("ExecScriptForLoad", LoadScriptCommand.class);
        Core.registerConsoleCommand("ExecScriptForCompile", CompileScriptCommand.class);
        Core.registerConsoleCommand("ExecScriptForClose", CloseScriptCommand.class);
        CoreEvents.registerApplicationEventListener(listener);
    }

    public static void unloadPlugins() {
        CoreEvents.unregisterApplicationEventListener(listener);
    }

    public static class LoadScriptCommand extends BaseSubCommand {
        @Override
        public Integer call() {
            return executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD, getParam("script"));
        }
    }

    public static class CompileScriptCommand extends BaseSubCommand {
        @Override
        public Integer call() {
            return executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE.COMPILE, getParam("script"));
        }
    }

    public static class CloseScriptCommand extends BaseSubCommand {
        @Override
        public Integer call() {
            return executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE, getParam("script"));
        }
    }

    /**
     * Execute a script as PROJECT_CHANGE events.
     */
    @SuppressWarnings("unused")
    public static int executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE eventType,
                                           @Nullable String script) {
        Log.logInfoRB("CONSOLE_EXECUTE_SCRIPT", script, eventType);
        if (script == null) {
            Log.logInfoRB("SCW_SCRIPT_LOAD_ERROR", "the script is not a file");
            return 1;
        }
        File scriptFile = Paths.get(script).toFile();
        if (!scriptFile.isFile()) {
            Log.logInfoRB("SCW_SCRIPT_LOAD_ERROR", "the script is not a file");
            return 1;
        }

        HashMap<String, Object> binding = new HashMap<>();
        binding.put("eventType", eventType);

        ConsoleBindings consoleBindings = new ConsoleBindings();
        binding.put(ScriptRunner.VAR_CONSOLE, consoleBindings);
        binding.put(ScriptRunner.VAR_GLOSSARY, consoleBindings);
        binding.put(ScriptRunner.VAR_EDITOR, consoleBindings);

        try {
            String result = ScriptRunner.executeScript(new ScriptItem(script), binding);
            Log.log(result);
        } catch (Exception ex) {
            Log.log(ex);
            return 1;
        }
        return 0;
    }

    private static final class ScriptingStartupEventListener implements IApplicationEventListener {
        private @Nullable ScriptingWindow window;

        @Override
        public void onApplicationStartup() {
            window = new ScriptingWindow();
        }

        @Override
        public void onApplicationShutdown() {
            if (window != null) {
                window.dispose();
            }
        }
    }
}
