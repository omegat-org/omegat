/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Briac Pilpre
               2015 Aaron Madlon-Kay
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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;

import org.jspecify.annotations.Nullable;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IEditorEventListener;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.util.DirectoryMonitor;

/**
 * Monitor to check changes in the script directory.
 *
 * @author Briac Pilpre
 */
public class ScriptsMonitor implements DirectoryMonitor.DirectoryCallback, DirectoryMonitor.Callback {
    private static final boolean SCRIPTING_EVENTS = true;
    private boolean applicationStartupEventScriptsExecuted = false;

    private static final FilenameFilter FILTER;

    static {
        FILTER = (dir, name) -> ScriptRunner.getAvailableScriptExtensions().contains(FilenameUtils
                .getExtension(name).toLowerCase(Locale.ENGLISH));
    }

    public ScriptsMonitor(final ScriptingWindow scriptingWindow) {
        this.scriptingWindow = scriptingWindow;

        if (SCRIPTING_EVENTS) {
            // Initialize the events script list for all the events
            for (EventType t : EventType.values()) {
                eventScriptsMap.put(t, new ArrayList<>());
            }
        }

    }

    public void start(final File scriptDir) {
        this.scriptDirectory = scriptDir;
        monitor = new DirectoryMonitor(scriptDir, this, this);
        monitor.start();

        // Immediately execute APPLICATION_STARTUP event scripts
        if (!applicationStartupEventScriptsExecuted) { // first-time only
            applicationStartupEventScriptsExecuted = true;
            addEventScripts(EventType.APPLICATION_STARTUP);
            ArrayList<ScriptItem> scripts = eventScriptsMap.get(EventType.APPLICATION_STARTUP);
            scriptingWindow.executeScripts(scripts, Collections.emptyMap());
            scripts.clear();
        }
    }

    public void stop() {
        if (monitor != null) {
            monitor.fin();
        }
    }

    /**
     * Executed on file changed.
     */
    @Override
    public void fileChanged(File file) {

    }

    @Override
    public void directoryChanged(File file) {
        if (!scriptDirectory.isDirectory()) {
            // No script directory.
            return;
        }

        // Plain Scripts
        // Only display files with an extension supported by the engines
        // currently installed.
        ArrayList<ScriptItem> scriptsList = new ArrayList<>();
        // Replace the script filename by its description, if available
        File[] aFile = scriptDirectory.listFiles(FILTER);
        if (aFile != null) {
            for (File script : aFile) {
                scriptsList.add(new ScriptItem(script));
            }
        }

        Collections.sort(scriptsList);
        SwingUtilities.invokeLater(() -> {
            scriptingWindow.setScriptItems(scriptsList);
        });

        if (SCRIPTING_EVENTS) {
            hookApplicationEvent();
            hookEntryEvent();
            hookProjectEvent();
            hookEditorEvent();
        }
    }

    private void hookEntryEvent() {
        if (fileEventListener != null) {
            CoreEvents.unregisterEntryEventListener(fileEventListener);
        }

        addEventScripts(EventType.ENTRY_ACTIVATED);
        addEventScripts(EventType.NEW_FILE);

        fileEventListener = new IEntryEventListener() {
            @Override
            public void onNewFile(String activeFileName) {
                HashMap<String, Object> binding = new HashMap<>();
                binding.put("activeFileName", activeFileName);

                scriptingWindow.executeScripts(eventScriptsMap.get(EventType.NEW_FILE), binding);
            }

            @Override
            public void onEntryActivated(SourceTextEntry newEntry) {
                HashMap<String, Object> binding = new HashMap<>();
                binding.put("newEntry", newEntry);

                scriptingWindow.executeScripts(eventScriptsMap.get(EventType.ENTRY_ACTIVATED), binding);
            }
        };

        CoreEvents.registerEntryEventListener(fileEventListener);
    }

    private void hookProjectEvent() {
        if (projectChangeListener != null) {
            CoreEvents.unregisterProjectChangeListener(projectChangeListener);
        }

        addEventScripts(EventType.PROJECT_CHANGED);

        projectChangeListener = new IProjectEventListener() {

            @Override
            public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
                HashMap<String, Object> binding = new HashMap<>();
                binding.put("eventType", eventType);
                ArrayList<ScriptItem> scripts = eventScriptsMap.get(EventType.PROJECT_CHANGED);
                scriptingWindow.executeScripts(scripts, binding);
            }
        };
        CoreEvents.registerProjectChangeListener(projectChangeListener);
    }

    private void hookApplicationEvent() {
        if (applicationEventListener != null) {
            CoreEvents.unregisterApplicationEventListener(applicationEventListener);
        }

        addEventScripts(EventType.APPLICATION_SHUTDOWN);

        applicationEventListener = new IApplicationEventListener() {
            @Override
            public void onApplicationStartup() {
                // APPLICATION_STARTUP is not working because it is registered too late.
                // The scripts are launched during
                // org.omegat.gui.scripting.ScriptsMonitor.start(File)
            }

            @Override
            public void onApplicationShutdown() {
                // FIXME APPLICATION_SHUTDOWN scripts are not reliably
                // executed, as the application may exit before they are
                // finished executing.
                ArrayList<ScriptItem> scriptItems = eventScriptsMap.get(EventType.APPLICATION_SHUTDOWN);
                scriptingWindow.executeScripts(scriptItems, new HashMap<>());
            }
        };

        CoreEvents.registerApplicationEventListener(applicationEventListener);
    }

    private void hookEditorEvent() {
        if (editorEventListener != null) {
            CoreEvents.unregisterEditorEventListener(editorEventListener);
        }

        addEventScripts(EventType.NEW_WORD);

        editorEventListener = new IEditorEventListener() {
            @Override
            public void onNewWord(String newWord) {
                HashMap<String, Object> binding = new HashMap<>();
                binding.put("newWord", newWord);

                scriptingWindow.executeScripts(eventScriptsMap.get(EventType.NEW_WORD), binding);
            }
        };

        CoreEvents.registerEditorEventListener(editorEventListener);
    }

    private void addEventScripts(EventType eventType) {
        String entryDirName = eventType.name().toLowerCase(Locale.ENGLISH);

        File entryActivatedDir = new File(scriptDirectory, entryDirName);
        if (!entryActivatedDir.isDirectory()) {
            return;
        }
        ArrayList<ScriptItem> eventScripts = eventScriptsMap.get(eventType);
        // Avoid executing scripts that may be deleted during the directory change.
        eventScripts.clear();

        File[] listFiles = entryActivatedDir.listFiles(FILTER);
        // Sort the script files to guarantee the same execution order
        Arrays.sort(listFiles);
        for (File script : listFiles) {
            ScriptItem scriptItem = new ScriptItem(script);
            if (!eventScripts.contains(scriptItem)) {
                eventScripts.add(scriptItem);
            }
        }
    }

    private enum EventType {
        // ApplicationEvent
        APPLICATION_STARTUP,
        APPLICATION_SHUTDOWN,

        // EditorEvent
        NEW_WORD,

        // FontEvent
        //FONT_CHANGED,

        // ProjectEvent
        PROJECT_CHANGED,

        // EntryEvent
        ENTRY_ACTIVATED,
        NEW_FILE
    }

    private @Nullable File scriptDirectory;
    protected @Nullable DirectoryMonitor monitor;
    private final ScriptingWindow scriptingWindow;

    // Event listeners.
    private @Nullable IEntryEventListener fileEventListener;
    private @Nullable IProjectEventListener projectChangeListener;
    private @Nullable IApplicationEventListener applicationEventListener;
    private @Nullable IEditorEventListener editorEventListener;

    // Map holding the script fired for the different event listeners.
    private final HashMap<EventType, ArrayList<ScriptItem>> eventScriptsMap = new HashMap<>();
}
