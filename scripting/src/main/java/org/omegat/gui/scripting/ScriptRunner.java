/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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
import org.omegat.gui.scripting.runner.AbstractScriptRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Scripting window entry point.
 * @author Hiroshi Miura
 */
public final class ScriptRunner {

    private ScriptRunner() {
    }

    /**
     * Scripts that want to run on the Event Dispatch Thread should define a
     * top-level function with this name and NOT evaluate it.
     */
    public static final String SCRIPT_GUI_FUNCTION_NAME = "gui";

    public static final String DEFAULT_SCRIPT = "groovy";

    // definition of global variables available to scripts.
    public static final String VAR_CONSOLE = "console";
    public static final String VAR_MAINWINDOW = "mainWindow";
    public static final String VAR_GLOSSARY = "glossary";
    public static final String VAR_CORE = "Core";
    public static final String VAR_EDITOR = "editor";
    public static final String VAR_PROJECT = "project";
    public static final String VAR_RESOURCES = "res";

    /**
     * Execute as read from the file associated with the supplied
     * {@link ScriptItem}. This is a convenience method for
     * {@link AbstractScriptRunner#executeScript(String, ScriptItem, Map)}.
     *
     * @param item
     *            The associated {@link ScriptItem}. Must not be null. If
     *            <code>script</code> is null, the script content will be read
     *            from the associated file. The script engine will be resolved
     *            from the filename. The resource bundle associated with the
     *            <code>item</code> will be included in the bindings as
     *            {@link #VAR_RESOURCES}.
     * @param additionalBindings
     *            A map of bindings that will be included along with other
     *            bindings
     * @return result string.
     * @throws IOException when I/O error occurred.
     * @throws ScriptException when script engine raises error.
     */
    public static String executeScript(ScriptItem item, Map<String, Object> additionalBindings)
            throws IOException, ScriptException {
        return executeScript(null, item, additionalBindings);
    }

    /**
     * Execute a script either in string form or, if <code>script</code> is
     * null, as read from the file associated with the supplied
     * {@link ScriptItem}. The engine is resolved via the filename extension
     * associated with <code>item</code> (defaults to {@link #DEFAULT_SCRIPT}).
     * <p>
     * This is a convenience method for
     * {@link #executeScript(String, ScriptEngine, Map)}.
     *
     * @param script
     *            The script in string form. Can be null.
     * @param item
     *            The associated {@link ScriptItem}. Must not be null. If
     *            <code>script</code> is null, the script content will be read
     *            from the associated file. The script engine will be resolved
     *            from the filename. The resource bundle associated with the
     *            <code>item</code> will be included in the bindings as
     *            {@link #VAR_RESOURCES}.
     * @param additionalBindings
     *            A map of bindings that will be included along with other
     *            bindings
     * @return result string.
     * @throws IOException when I/O error occurred.
     * @throws ScriptException when script engine raises error.
     */
    public static String executeScript(@Nullable String script, ScriptItem item,
                                       Map<String, Object> additionalBindings) throws IOException, ScriptException {
        return AbstractScriptRunner.getActiveRunner().executeScript(script, item, additionalBindings);
    }

    public static ScriptEngineManager getManager() {
        return AbstractScriptRunner.getActiveRunner().getManager();
    }

    public static List<ScriptEngineFactory> getEngineFactories() {
        return AbstractScriptRunner.getActiveRunner().getManager().getEngineFactories();
    }

    /**
     * Execute a script with a given engine and bindings.
     *
     * @param script
     *            The script in string form
     * @param engine
     *            The engine
     * @param additionalBindings
     *            A map of bindings that will be included along with other
     *            bindings
     * @return The evaluation result
     * @throws ScriptException when script engine raises error.
     */
    @SuppressWarnings("unused")
    public static Object executeScript(String script, ScriptEngine engine,
                                       Map<String, Object> additionalBindings) throws ScriptException {
        return AbstractScriptRunner.getActiveRunner().executeScript(script, engine, additionalBindings);
    }

    public static List<String> getAvailableScriptExtensions() {
        return AbstractScriptRunner.getActiveRunner().getAvailableScriptExtensions();
    }
}
