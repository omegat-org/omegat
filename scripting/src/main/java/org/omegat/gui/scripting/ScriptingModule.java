/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Briac Pilpre (briacp@gmail.com)
               2014 Briac Pilpre (briacp@gmail.com), Yu Tang
               2015 Yu Tang, Aaron Madlon-Kay
               2023-2025 Hiroshi Miura
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

import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.util.Log;

import java.io.File;
import java.util.HashMap;

/**
 * The ScriptingModule class is responsible for managing scripting
 * functionality, including loading and unloading scripting-related plugins and
 * executing scripts.
 *
 * @author Hiroshi Miura
 */
@SuppressWarnings("unused")
public final class ScriptingModule {

    private static ScriptingStartupEventListener listener;

    public static void loadPlugins() {
        listener = new ScriptingStartupEventListener();
        CoreEvents.registerApplicationEventListener(listener);
    }

    public static void unloadPlugins() {
        CoreEvents.unregisterApplicationEventListener(listener);
    }

    /**
     * Private constructor for the class that contains only static methods.
     */
    private ScriptingModule() {
    }

    /**
     * Execute a script as PROJECT_CHANGE events.
     */
    @SuppressWarnings("unused")
    public static void executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE eventType,
            File script) {
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

    private static class ScriptingStartupEventListener implements IApplicationEventListener {
        private ScriptingWindow window;

        @Override
        public void onApplicationStartup() {
            window = new ScriptingWindow();
        }

        @Override
        public void onApplicationShutdown() {
            window.stop();
        }
    }
}
