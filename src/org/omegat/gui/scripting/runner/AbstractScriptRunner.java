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

package org.omegat.gui.scripting.runner;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.omegat.core.Core;
import org.omegat.gui.scripting.ScriptItem;
import org.omegat.gui.scripting.ScriptRunner;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract base class for script runners.
 *
 * @author Hiroshi Miura
 */
public abstract class AbstractScriptRunner {

    private ScriptEngineManager manager;

    /**
     * Execute a script either in string form or, if <code>script</code> is
     * null, as read from the file associated with the supplied
     * {@link ScriptItem}. The engine is resolved via the filename extension
     * associated with <code>item</code> (defaults to {@link ScriptRunner#DEFAULT_SCRIPT}).
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
     *            {@link ScriptRunner#VAR_RESOURCES}.
     * @param additionalBindings
     *            A map of bindings that will be included along with other
     *            bindings
     * @return result string.
     * @throws IOException when I/O error occurred.
     * @throws ScriptException when script engine raises error.
     */
    public String executeScript(String script, ScriptItem item,
                                       Map<String, Object> additionalBindings) throws IOException, ScriptException {

        Map<String, Object> bindings = new HashMap<>();
        if (additionalBindings != null) {
            bindings.putAll(additionalBindings);
        }
        bindings.put(ScriptRunner.VAR_RESOURCES, item.getResourceBundle());
        ScriptEngine engine = getEngine(item);
        if (StringUtil.isEmpty(script)) {
            script = item.getText();
        }

        StringBuilder result = new StringBuilder();
        Object eval = executeScript(script, engine, bindings);
        if (eval != null) {
            result.append(OStrings.getString("SCW_SCRIPT_RESULT")).append('\n');
            result.append(eval).append('\n');
        }
        return result.toString();
    }

    /**
     * Retrieves a {@link ScriptEngine} to execute scripts based on the file extension
     * associated with the provided {@link ScriptItem}. If the file is not specified
     * or the appropriate engine is not available, it falls back to a default scripting
     * engine.
     *
     * @param item the {@link ScriptItem} containing the script or script file information.
     *             Must not be null.
     * @return the {@link ScriptEngine} instance suitable for executing the script. Will
     *         default to the engine configured by {@code ScriptRunner.DEFAULT_SCRIPT} if
     *         no specific engine is found.
     */
     ScriptEngine getEngine(ScriptItem item) {
        String extension = ScriptRunner.DEFAULT_SCRIPT;
        if (item.getFile() != null) {
            extension = FilenameUtils.getExtension(item.getFileName());
        }
        ScriptEngine engine = getManager().getEngineByExtension(extension);
        if (engine == null) {
            engine = getManager().getEngineByName(ScriptRunner.DEFAULT_SCRIPT);
        }
        // implement me: placeholder a logic to return instrumented scripting engine if omegat.debug.scripts is set.
        return engine;
    }

    public ScriptEngineManager getManager() {
        if (manager == null) {
            manager = new ScriptEngineManager(AbstractScriptRunner.class.getClassLoader());
        }
        return manager;
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
    public Object executeScript(String script, ScriptEngine engine,
                                       Map<String, Object> additionalBindings) throws ScriptException {
        AbstractScriptRunner runner = getActiveRunner();
        return runner.doExecuteScript(script, engine, additionalBindings);
    }

    /**
     * Template method for actual script execution.
     * Subclasses override this for custom behavior
     */
    protected abstract Object doExecuteScript(String script, ScriptEngine engine,
                                              Map<String, Object> additionalBindings) throws ScriptException;

    /**
     * Common binding setup - used by both normal and debug runners.
     */
    protected Bindings setupBindings(ScriptEngine engine, Map<String, Object> additionalBindings) {
        Bindings bindings = engine.createBindings();
        bindings.put(ScriptRunner.VAR_PROJECT, Core.getProject());
        bindings.put(ScriptRunner.VAR_EDITOR, Core.getEditor());
        bindings.put(ScriptRunner.VAR_GLOSSARY, Core.getGlossary());
        bindings.put(ScriptRunner.VAR_MAINWINDOW, Core.getMainWindow());
        bindings.put(ScriptRunner.VAR_CORE, Core.class);

        if (additionalBindings != null) {
            bindings.putAll(additionalBindings);
        }

        return bindings;
    }

    /**
     * Common GUI script invocation
     */
    protected void invokeGuiScript(Invocable engine) throws ScriptException {
        Runnable invoke = () -> {
            try {
                engine.invokeFunction(ScriptRunner.SCRIPT_GUI_FUNCTION_NAME);
            } catch (NoSuchMethodException e) {
                // No GUI invocation defined
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            invoke.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(invoke);
            } catch (InvocationTargetException e) {
                // The original cause is double-wrapped at this point
                if (e.getCause().getCause() instanceof ScriptException) {
                    throw (ScriptException) e.getCause().getCause();
                } else {
                    Log.log(e);
                }
            } catch (InterruptedException e) {
                Log.log(e);
            }
        }
    }

    /**
     * Retrieves a list of file extensions that are supported by the available script engines.
     * This method collects all extensions from the script engine factories
     * managed by the {@link ScriptEngineManager}.
     *
     * @return a list of supported script file extensions as strings. The list may be empty
     *         if no engines are available or no extensions are defined.
     */
    public List<String> getAvailableScriptExtensions() {
        return getManager().getEngineFactories().stream()
                .flatMap(factory -> factory.getExtensions().stream())
                .collect(Collectors.toList());
    }

    // Runner selection logic
    private static volatile AbstractScriptRunner activeRunner;

    /**
     * Retrieves the active instance of {@link AbstractScriptRunner}. If no active
     * runner has been set, this method will initialize a new instance of
     * {@link StandardScriptRunner} as the default active runner.
     * <p>
     * Note: This method returns a shared singleton instance. The returned runner
     * should not be modified or stored beyond its immediate use for script execution.
     *
     * @return the current active {@link AbstractScriptRunner} instance. If no
     *         instance exists, a new {@link StandardScriptRunner} is created
     *         and returned. Never returns null.
     */
    public static AbstractScriptRunner getActiveRunner() {
        AbstractScriptRunner result = activeRunner;
        if (result == null) {
            synchronized (AbstractScriptRunner.class) {
                result = activeRunner;
                if (result == null) {
                    // implement me: set Debug Runner if omegat.debug.scripts is set.
                    activeRunner = result = new StandardScriptRunner();
                }
            }
        }
        return result;
    }

    /**
     * Sets the active runner instance. This method is intended for testing purposes
     * and for switching between standard and debug runners.
     * <p>
     * Package-private to limit external modification.
     *
     * @param runner the runner instance to set as active. Must not be null.
     */
    @VisibleForTesting
    static synchronized void setActiveRunner(AbstractScriptRunner runner) {
        if (runner == null) {
            throw new IllegalArgumentException("Runner cannot be null");
        }
        activeRunner = runner;
    }

    /**
     * Resets the active runner to null, forcing reinitialization on next access.
     * This method is intended for testing purposes.
     * <p>
     * Package-private to limit external modification.
     */
    @VisibleForTesting
    static synchronized void resetActiveRunner() {
        activeRunner = null;
    }
}

