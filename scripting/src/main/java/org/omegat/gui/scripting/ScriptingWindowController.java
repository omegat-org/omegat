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
import javax.swing.JMenuItem;
import javax.swing.SwingWorker;
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
                monitor.stop();
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
        window.setText("");

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

            String scriptString = window.getText();
            if (scriptString.trim().isEmpty()) {
                try {
                    scriptString = window.currentScriptItem.getText();
                    window.setText(scriptString);
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
                executeScript(new ScriptItem(window.getText()));
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

    public void runQuickScript(int index) {

        String quickScript = quickScriptManager.getQuickScriptFilename(index);

        if (quickScript == null) {
            logResultRB("SCW_NO_SCRIPT_SELECTED");
            return;
        }

        logResultRB("SCW_QUICK_RUN", index + 1);
        ScriptItem scriptFile = new ScriptItem(new File(scriptsDirectory, quickScript));

        try {
            executeScript(scriptFile);
        } catch (ScriptExecutionException e) {
            logResultRB(e, "SCW_SCRIPT_LOAD_ERROR", quickScript);
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
                window.setText("");
            }
        });

        return new ScriptWorker(this, scriptString, scriptItem, bindings);
    }

    public static String getString(String key) {
        return BUNDLE.getString(key);
    }

    public void logResultRB(Throwable t, String key, Object... args) {
        window.logResultToWindow(MessageFormat.format(getString(key), args) + "\n" + t.getMessage(), true);
        LOGGER.atError().setCause(t).setMessageRB(key).addArgument(args).log();
    }

    public void logResultRB(String key, Object... args) {
        window.logResultToWindow(MessageFormat.format(getString(key), args), true);
        LOGGER.atError().setMessageRB(key).addArgument(args).log();
    }

    public void logResult(String s, Throwable t) {
        window.logResultToWindow(s + "\n" + t.getMessage(), true);
        LOGGER.atInfo().setCause(t).setMessage(s).log();
    }

    public void logResult(String s) {
        logResult(s, true);
    }

    public void logResult(String s, boolean newLine) {
        window.logResultToWindow(s, newLine);
        LOGGER.atInfo().setMessage(s).log();
    }

    public void logInfoRB(String key, Object... args) {
        LOGGER.atInfo().setMessageRB(key).addArgument(args).log();
    }

    public void logErrorRB(String key, Object... args) {
        LOGGER.atError().setMessageRB(key).addArgument(args).log();
    }

    public void logErrorRB(Throwable t, String key, Object... args) {
        LOGGER.atError().setCause(t).setMessageRB(key).addArgument(args).log();
    }

    public void setScriptsDirectory(String scriptsDir) {
        File dir;
        try {
            dir = new File(scriptsDir).getCanonicalFile();
        } catch (IOException ex) {
            dir = new File(scriptsDir);
        }

        if (!dir.isDirectory()) {
            // updateQuickScripts();
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

    private final ScriptsMonitor monitor;
    private File scriptsDirectory;
    private ScriptWorker currentScriptWorker;
    private final Queue<ScriptWorker> queuedWorkers = new LinkedList<>();
    private QuickScriptManager quickScriptManager;

    public void setScriptItems(ArrayList<ScriptItem> scriptsList) {
        window.setScriptItems(scriptsList);
    }

    public void initQuickScriptManager(JMenuItem[] quickMenuItems) {
        quickScriptManager = new QuickScriptManager(this, quickMenuItems);
        quickScriptManager.loadQuickScriptsFromPreferences();
    }

    /**
     * Loads a script set and applies it to the quick script buttons.
     * This method clears existing quick script associations and sets new ones
     * from the provided ScriptSet.
     *
     * @param set The script set to load
     */
    public void loadScriptSet(ScriptSet set) {
        if (set == null) {
            return;
        }

        // Unset all previous scripts
        for (int i = 0; i < QuickScriptManager.NUMBERS_OF_QUICK_SCRIPTS; i++) {
            int scriptKey = i + 1;
            Preferences.setPreference(Preferences.SCRIPTS_QUICK_PREFIX + scriptKey, "");
            unsetQuickScript(i);
        }

        // Set scripts from the loaded set
        for (int i = 0; i < QuickScriptManager.NUMBERS_OF_QUICK_SCRIPTS; i++) {
            int scriptKey = i + 1;
            ScriptItem scriptItem = set.getScriptItem(scriptKey);

            if (scriptItem != null) {
                // Update preference
                Preferences.setPreference(Preferences.SCRIPTS_QUICK_PREFIX + scriptKey,
                        scriptItem.getFileName());

                // Update quick script in model
                quickScriptManager.setQuickScript(scriptItem, i);

                // Update UI
                setQuickScript(scriptItem, i);

                // Log the action
                logInfoRB("SCW_QUICK_SCRIPT_LOADED", scriptItem.getFileName(), scriptKey);
            }
        }

        // Update UI components to reflect the new set
        // updateQuickScripts();

        // Log success message
        logResultRB("SCW_SET_LOADED", set.getTitle());
    }

    public File getScriptsDirectory() {
        return scriptsDirectory;
    }

    /**
     * Gets the array of quick script filenames.
     *
     * @return Array of quick script filenames
     */
    public String[] getQuickScriptFilenames() {
        return quickScriptManager.getAllQuickScriptFilenames();
    }

    public void setQuickScript(ScriptItem scriptItem, int index) {
        quickScriptManager.setQuickScript(scriptItem, index);
        quickScriptManager.saveQuickScriptToPreferences(index, scriptItem.getFileName());
    }

    public void unsetQuickScript(int index) {
        quickScriptManager.unsetQuickScript(index);
        quickScriptManager.saveQuickScriptToPreferences(index, "");
    }

}
