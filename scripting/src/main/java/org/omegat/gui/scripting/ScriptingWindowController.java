/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Briac Pilpre (briacp@gmail.com)
               2013 Alex Buloichik
               2014 Briac Pilpre (briacp@gmail.com), Yu Tang
               2015 Yu Tang, Aaron Madlon-Kay
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


import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.OSXIntegration;
import tokyo.northside.logging.ILogger;
import tokyo.northside.logging.LoggerFactory;

import javax.script.ScriptEngineFactory;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.ResourceBundle;

import static org.omegat.gui.scripting.ScriptingWindow.NUMBERS_OF_QUICK_SCRIPTS;

public class ScriptingWindowController {

    public static final String DEFAULT_SCRIPTS_DIR = "scripts";
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org.omegat.gui.scripting.Bundle");
    private static final ILogger LOGGER = LoggerFactory.getLogger(ScriptingWindow.class, BUNDLE);
    private final ScriptingWindow window;

    public ScriptingWindowController() {
        window = new ScriptingWindow(this);
        setScriptsDirectory(
                Preferences.getPreferenceDefault(Preferences.SCRIPTS_DIRECTORY, DEFAULT_SCRIPTS_DIR));
        window.btnRunScript.addActionListener(a -> runScript());
        window.btnCancelScript.addActionListener(e -> cancelCurrentScript());
        monitor = new ScriptsMonitor(this);
        if (scriptsDirectory != null) {
            monitor.start(scriptsDirectory);
        }
        window.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (monitor != null) {
                    monitor.stop();
                }
            }
        });
        logResult(listScriptEngines());
    }

    private String listScriptEngines() {
        StringBuilder sb = new StringBuilder(getString("SCW_LIST_ENGINES") + "\n");
        for (ScriptEngineFactory engine : ScriptRunner.getManager().getEngineFactories()) {
            sb.append(" - ");
            sb.append(engine.getEngineName());
            sb.append(" ");
            sb.append(engine.getLanguageName());
            sb.append(" v.");
            sb.append(engine.getLanguageVersion());
            sb.append(" (").append(getString("SCW_EXTENSIONS")).append(" ");
            boolean hasMore = false;
            for (String ext : engine.getExtensions()) {
                if (hasMore) {
                    sb.append(", ");
                }
                sb.append(ext);
                hasMore = true;
            }
            sb.append(")");
            sb.append("\n");
        }

        return sb.toString();
    }

    public void stop() {
        if (window.frame != null) {
            window.frame.dispose();
        }
    }

    /**
     * Execute the currently selected script.
     */
    void runScript() {
        window.txtResult.setText("");

        // Execute a script with a corresponding source file. If the source has
        // changed, we use the text area as source code, but still consider the
        // script file as the "base" (for determining the engine to use and to
        // fetch the localized resources).
        if (window.currentScriptItem != null && window.currentScriptItem.getFile() != null) {
            if (!window.currentScriptItem.getFile().canRead()) {
                logResultRB("SCW_CANNOT_READ_SCRIPT");
                return;
            }

            logResultRB("SCW_RUNNING_SCRIPT", window.currentScriptItem.getFile().getAbsolutePath());

            String scriptString = window.txtScriptEditor.getTextArea().getText();
            if (scriptString.trim().isEmpty()) {
                try {
                    scriptString = window.currentScriptItem.getText();
                    window.txtScriptEditor.getTextArea().setText(scriptString);
                } catch (IOException e) {
                    logErrorRB("SCW_CANNOT_READ_SCRIPT_FILE", window.currentScriptItem.getFileName());
                }
            }

            try {
                executeScript(window.currentScriptItem);
            } catch (ScriptExecutionException e) {
                logResultRB(e, "SCW_SCRIPT_LOAD_ERROR", window.currentScriptItem.getFileName());
            }
        } else {
            // No file is found for this script, it is executed as standalone.
            logResult(StringUtil.format(ScriptingWindowController.getString("SCW_RUNNING_SCRIPT"), ScriptItem.EDITOR_SCRIPT));
            try {
                executeScript(new ScriptItem(window.txtScriptEditor.getTextArea().getText()));
            } catch (ScriptExecutionException e) {
                logResultRB(e, "SCW_SCRIPT_LOAD_ERROR", ScriptItem.EDITOR_SCRIPT);
            }
        }
    }

    public void executeScript(ScriptItem scriptItem) throws ScriptExecutionException {
        executeScript(scriptItem, Collections.emptyMap());
    }

    public void executeScript(ScriptItem scriptItem, Map<String, Object> bindings) throws ScriptExecutionException {
        executeScript(scriptItem, bindings, true);
    }

    public void executeScript(ScriptItem scriptItem, Map<String, Object> bindings, boolean cancelQueue)
            throws ScriptExecutionException {
        executeScripts(Collections.singletonList(scriptItem), bindings, cancelQueue);
    }

    /**
     * Execute scripts sequentially to make sure they don't interrupt each
     * other.
     * <p>
     * Note: This method can be called in instances when the
     * Scripting Window is not visible, so it might make more
     * sense to let the caller handle the exception.
     *
     * @param scriptItems
     *            List of script to execute.
     * @param bindings
     *            Additional bindings to pass to the executed script.
     * @param cancelQueue
     *            If true, the run queue is cleared before running the scripts.
     */
    public void executeScripts(final List<ScriptItem> scriptItems, final Map<String, Object> bindings,
                               boolean cancelQueue) throws ScriptExecutionException {
        if (cancelQueue) {
            cancelScriptQueue();
        }

        List<ScriptExecutionException.ScriptError> errors = new ArrayList<>();

        for (ScriptItem scriptItem : scriptItems) {
            try {
                String scriptString = scriptItem.getText();
                queuedWorkers.add(createScriptWorker(scriptString, scriptItem, bindings));
            } catch (IOException e) {
                errors.add(new ScriptExecutionException.ScriptError(scriptItem, e));
                logResultRB(e, "SCW_SCRIPT_LOAD_ERROR", scriptItem.getFileName());
            }
        }

        executeScriptWorkers();

        if (!errors.isEmpty()) {
            throw new ScriptExecutionException("Failed to execute one or more scripts", errors);
        }
    }

    public void executeScripts(final List<ScriptItem> scriptItems, final Map<String, Object> bindings)
            throws ScriptExecutionException {
        executeScripts(scriptItems, bindings, false);
    }

    private void executeScriptWorkers() {
        final ScriptWorker scriptWorker = queuedWorkers.poll();

        if (scriptWorker == null) {
            return;
        }

        PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (!"state".equals(event.getPropertyName())) {
                    return;
                }

                if (SwingWorker.StateValue.DONE == event.getNewValue()) {
                    scriptWorker.removePropertyChangeListener(this);
                    executeScriptWorkers();
                }
            }
        };
        scriptWorker.addPropertyChangeListener(propertyChangeListener);
        currentScriptWorker = scriptWorker;
        scriptWorker.execute();
    }

    void runQuickScript(int index) {

        if (quickScripts[index] == null) {
            logResultRB("SCW_NO_SCRIPT_SELECTED");
            return;
        }

        logResultRB("SCW_QUICK_RUN", index + 1);
        ScriptItem scriptFile = new ScriptItem(new File(scriptsDirectory, quickScripts[index]));

        try {
            executeScript(scriptFile);
        } catch (ScriptExecutionException e) {
            logResultRB(e, "SCW_SCRIPT_LOAD_ERROR", quickScripts[index]);
        }
    }

    /**
     * Cancel the currently running script, if any.
     * <p>
     * <b>Note!</b> Canceling the worker does not do anything in and of itself.
     * The running script must poll for interruption with e.g.
     * {@link java.lang.Thread#interrupted()}.
     *
     * @see <a href="http://stackoverflow.com/a/24875881/448068">StackOverflow
     *      answer about interrupting scripts</a>
     */
    public void cancelCurrentScript() {
        if (currentScriptWorker != null) {
            currentScriptWorker.cancel(true);
        }
    }

    public void cancelScriptQueue() {
        cancelCurrentScript();
        queuedWorkers.clear();
    }

    public ScriptWorker createScriptWorker(String scriptString, ScriptItem scriptItem,
                                           Map<String, Object> additionalBindings) {

        if (!scriptString.endsWith("\n")) {
            scriptString += "\n";
        }

        Map<String, Object> bindings = new HashMap<>(additionalBindings);
        bindings.put(ScriptRunner.VAR_CONSOLE, new IScriptLogger() {
            @Override
            public void print(Object o) {
                logResult(o.toString(), false);
            }

            @Override
            public void println(Object o) {
                logResult(o.toString(), true);
            }

            @Override
            public void clear() {
                window.txtResult.setText("");
            }
        });

        return new ScriptWorker(this, scriptString, scriptItem, bindings);
    }

    public static String getString(String key) {
        return BUNDLE.getString(key);
    }

    void logResultRB(Throwable t, String key, Object... args) {
        logResultToWindow(MessageFormat.format(getString(key), args) + "\n" + t.getMessage(), true);
        LOGGER.atError().setCause(t).setMessageRB(key).addArgument(args).log();
    }

    void logResultRB(String key, Object... args) {
        logResultToWindow(MessageFormat.format(getString(key), args), true);
        LOGGER.atError().setMessageRB(key).addArgument(args).log();
    }

    void logResult(String s, Throwable t) {
        logResultToWindow(s + "\n" + t.getMessage(), true);
        LOGGER.atInfo().setCause(t).setMessage(s).log();
    }

    void logResult(String s) {
        logResult(s, true);
    }

    void logResult(String s, boolean newLine) {
        logResultToWindow(s, newLine);
        LOGGER.atInfo().setMessage(s).log();
    }

    /**
     * Print log text to the Scripting Window's console area. A trailing line
     * break will be added if the parameter newLine is true.
     */
    void logResultToWindow(String s, boolean newLine) {
        Document doc = window.txtResult.getDocument();
        try {
            doc.insertString(doc.getLength(), s + (newLine ? "\n" : ""), null);
        } catch (BadLocationException e1) {
            /* empty */
        }
    }

    void logInfoRB(String key, Object... args) {
        LOGGER.atInfo().setMessageRB(key).addArgument(args).log();
    }

    void logErrorRB(String key, Object... args) {
        LOGGER.atError().setMessageRB(key).addArgument(args).log();
    }

    void logErrorRB(Throwable t, String key, Object... args) {
        LOGGER.atError().setCause(t).setMessageRB(key).addArgument(args).log();
    }

    void setScriptsDirectory(String scriptsDir) {
        File dir;
        try {
            dir = new File(scriptsDir).getCanonicalFile();
        } catch (IOException ex) {
            dir = new File(scriptsDir);
        }

        if (!dir.isDirectory()) {
            window.updateQuickScripts();
            return;
        }
        scriptsDirectory = dir;
        Preferences.setPreference(Preferences.SCRIPTS_DIRECTORY, scriptsDir);
        OSXIntegration.setProxyIcon(window.frame.getRootPane(), scriptsDirectory);

        if (monitor != null) {
            monitor.stop();
            monitor.start(scriptsDirectory);
        }
    }

    ScriptsMonitor monitor;
    File scriptsDirectory;
    ScriptWorker currentScriptWorker;
    final Queue<ScriptWorker> queuedWorkers = new LinkedList<>();
    final String[] quickScripts = new String[NUMBERS_OF_QUICK_SCRIPTS];

    public void setScriptItems(ArrayList<ScriptItem> scriptsList) {
        window.setScriptItems(scriptsList);
    }
}
