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
    private static boolean applicationStartupEventScriptsExecuted = false;

    private static final FilenameFilter FILTER;

    static {
        FILTER = (dir, name) -> ScriptRunner.getAvailableScriptExtensions().contains(FilenameUtils.getExtension(name)
                .toLowerCase(Locale.ENGLISH));
    }

    public ScriptsMonitor(final ScriptingWindow scriptingWindow) {
        this.m_scriptingWindow = scriptingWindow;

        if (SCRIPTING_EVENTS) {
            // Initialize the events script list for all the events
            for (EventType t : EventType.values()) {
                m_eventsScript.put(t, new ArrayList<>());
            }
        }

    }

    public void start(final File scriptDir) {
        this.m_scriptDir = scriptDir;
        m_monitor = new DirectoryMonitor(scriptDir, this, this);
        m_monitor.start();

        // Immediately execute APPLICATION_STARTUP event scripts
        if (!applicationStartupEventScriptsExecuted) { // first-time only
            applicationStartupEventScriptsExecuted = true;
            addEventScripts(EventType.APPLICATION_STARTUP);
            ArrayList<ScriptItem> scripts = m_eventsScript.get(EventType.APPLICATION_STARTUP);
            m_scriptingWindow.executeScripts(scripts, Collections.emptyMap());
            scripts.clear();
        }
    }

    public void stop() {
        if (m_monitor != null) {
            m_monitor.fin();
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
        if (!m_scriptDir.isDirectory()) {
            // No script directory.
            return;
        }

        // Plain Scripts
        // Only display files with an extension supported by the engines
        // currently installed.
        ArrayList<ScriptItem> scriptsList = new ArrayList<>();
        // Replace the script filename by its description, if available
        File[] aFile = m_scriptDir.listFiles(FILTER);
        if (aFile != null) {
            for (File script : aFile) {
                scriptsList.add(new ScriptItem(script));
            }
        }

        Collections.sort(scriptsList);
        SwingUtilities.invokeLater(() -> {
            m_scriptingWindow.setScriptItems(scriptsList);
        });

        if (SCRIPTING_EVENTS) {
            hookApplicationEvent();
            hookEntryEvent();
            hookProjectEvent();
            hookEditorEvent();
        }
    }

    private void hookEntryEvent() {
        if (m_entryEventListener != null) {
            CoreEvents.unregisterEntryEventListener(m_entryEventListener);
        }

        addEventScripts(EventType.ENTRY_ACTIVATED);
        addEventScripts(EventType.NEW_FILE);

        m_entryEventListener = new IEntryEventListener() {
            @Override
            public void onNewFile(String activeFileName) {
                HashMap<String, Object> binding = new HashMap<String, Object>();
                binding.put("activeFileName", activeFileName);

                m_scriptingWindow.executeScripts(m_eventsScript.get(EventType.NEW_FILE), binding);
            }

            @Override
            public void onEntryActivated(SourceTextEntry newEntry) {
                HashMap<String, Object> binding = new HashMap<String, Object>();
                binding.put("newEntry", newEntry);

                m_scriptingWindow.executeScripts(m_eventsScript.get(EventType.ENTRY_ACTIVATED), binding);
            }
        };

        CoreEvents.registerEntryEventListener(m_entryEventListener);
    }

    private void hookProjectEvent() {
        if (m_projectEventListener != null) {
            CoreEvents.unregisterProjectChangeListener(m_projectEventListener);
        }

        addEventScripts(EventType.PROJECT_CHANGED);

        m_projectEventListener = new IProjectEventListener() {

            @Override
            public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
                HashMap<String, Object> binding = new HashMap<String, Object>();
                binding.put("eventType", eventType);
                ArrayList<ScriptItem> scripts = m_eventsScript.get(EventType.PROJECT_CHANGED);
                m_scriptingWindow.executeScripts(scripts, binding);
           }
        };
        CoreEvents.registerProjectChangeListener(m_projectEventListener);
    }

    private void hookApplicationEvent() {
        if (m_applicationEventListener != null) {
            CoreEvents.unregisterApplicationEventListener(m_applicationEventListener);
        }

        addEventScripts(EventType.APPLICATION_SHUTDOWN);

        m_applicationEventListener = new IApplicationEventListener() {
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
                ArrayList<ScriptItem> scriptItems = m_eventsScript.get(EventType.APPLICATION_SHUTDOWN);
                m_scriptingWindow.executeScripts(scriptItems,
                        new HashMap<String, Object>());
            }
        };

        CoreEvents.registerApplicationEventListener(m_applicationEventListener);
    }

    private void hookEditorEvent() {
        if (m_editorEventListener != null) {
            CoreEvents.unregisterEditorEventListener(m_editorEventListener);
        }

        addEventScripts(EventType.NEW_WORD);

        m_editorEventListener = new IEditorEventListener() {
            @Override
            public void onNewWord(String newWord) {
                HashMap<String, Object> binding = new HashMap<String, Object>();
                binding.put("newWord", newWord);

                m_scriptingWindow.executeScripts(m_eventsScript.get(EventType.NEW_WORD), binding);
            }
        };

        CoreEvents.registerEditorEventListener(m_editorEventListener);
    }

    private void addEventScripts(EventType eventType) {
        String entryDirName = eventType.name().toLowerCase(Locale.ENGLISH);

        File entryActivatedDir = new File(m_scriptDir, entryDirName);
        if (!entryActivatedDir.isDirectory()) {
            return;
        }
        ArrayList<ScriptItem> eventScripts = m_eventsScript.get(eventType);
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

    private File m_scriptDir;
    protected DirectoryMonitor m_monitor;
    private ScriptingWindow m_scriptingWindow;

    // Event listeners.
    private IEntryEventListener m_entryEventListener;
    private IProjectEventListener m_projectEventListener;
    private IApplicationEventListener m_applicationEventListener;
    private IEditorEventListener m_editorEventListener;

    // Map holding the script fired for the different event listeners.
    private HashMap<EventType, ArrayList<ScriptItem>> m_eventsScript = new HashMap<EventType, ArrayList<ScriptItem>>();
}
