/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.scripting;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;
import org.omegat.core.Core;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

public final class ScriptRunner {

    private ScriptRunner() {
    }

    /**
     * Scripts that want to run on the Event Dispatch Thread should define a
     * top-level function with this name and NOT evaluate it.
     */
    public static final String SCRIPT_GUI_FUNCTION_NAME = "gui";

    public static final String DEFAULT_SCRIPT = "groovy";
    public static final String VAR_CONSOLE = "console";
    public static final String VAR_MAINWINDOW = "mainWindow";
    public static final String VAR_GLOSSARY = "glossary";
    public static final String VAR_CORE = "Core";
    public static final String VAR_EDITOR = "editor";
    public static final String VAR_PROJECT = "project";
    public static final String VAR_RESOURCES = "res";

    public static final ScriptEngineManager MANAGER = new ScriptEngineManager(ScriptRunner.class.getClassLoader());

    /**
     * Execute as read from the file associated with the supplied
     * {@link ScriptItem}. This is a convenience method for
     * {@link #executeScript(String, ScriptItem, Map)}.
     *
     * @param item
     * @param additionalBindings
     * @return
     * @throws ScriptException
     * @throws IOException
     * @throws Exception
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
     * @return
     * @throws IOException
     * @throws ScriptException
     */
    public static String executeScript(String script, ScriptItem item, Map<String, Object> additionalBindings)
            throws IOException, ScriptException {
        Map<String, Object> bindings = new HashMap<String, Object>();
        if (additionalBindings != null) {
            bindings.putAll(additionalBindings);
        }
        bindings.put(VAR_RESOURCES, item.getResourceBundle());
        String extension = DEFAULT_SCRIPT;
        if (item.getFile() != null) {
            extension = FilenameUtils.getExtension(item.getFile().getName());
        }
        ScriptEngine engine = MANAGER.getEngineByExtension(extension);
        if (engine == null) {
            engine = MANAGER.getEngineByName(DEFAULT_SCRIPT);
        }
        if (StringUtil.isEmpty(script)) {
            script = item.getText();
        }

        StringBuilder result = new StringBuilder();
        Object eval = executeScript(script, engine, bindings);
        if (eval != null) {
            result.append(OStrings.getString("SCW_SCRIPT_RESULT")).append('\n');
            result.append(eval.toString()).append('\n');
        }
        return result.toString();
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
     * @throws ScriptException
     */
    public static Object executeScript(String script, ScriptEngine engine, Map<String, Object> additionalBindings)
            throws ScriptException {
        // logResult(StaticUtils.format(OStrings.getString("SCW_SELECTED_LANGUAGE"),
        // engine.getFactory().getEngineName()));
        Bindings bindings = engine.createBindings();
        bindings.put(VAR_PROJECT, Core.getProject());
        bindings.put(VAR_EDITOR, Core.getEditor());
        bindings.put(VAR_GLOSSARY, Core.getGlossary());
        bindings.put(VAR_MAINWINDOW, Core.getMainWindow());
        bindings.put(VAR_CORE, Core.class);

        if (additionalBindings != null) {
            bindings.putAll(additionalBindings);
        }
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        Object result = engine.eval(script);
        if (engine instanceof Invocable) {
            invokeGuiScript((Invocable) engine);
        }
        return result;
    }

    private static void invokeGuiScript(Invocable engine) throws ScriptException {
        Runnable invoke = () -> {
            try {
                engine.invokeFunction(SCRIPT_GUI_FUNCTION_NAME);
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

    public static List<String> getAvailableScriptExtensions() {
        return MANAGER.getEngineFactories().stream().flatMap(factory -> factory.getExtensions().stream())
                .collect(Collectors.toList());
    }
}
