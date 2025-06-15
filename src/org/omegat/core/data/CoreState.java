/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

package org.omegat.core.data;

import org.jetbrains.annotations.VisibleForTesting;
import org.omegat.core.threads.IAutoSave;
import org.omegat.core.threads.SaveThread;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.main.IMainWindow;

import java.util.Collections;
import java.util.Map;

public class CoreState {

    protected static volatile CoreState instance = new CoreState();

    private IAutoSave saveThread;

    protected CoreState() {
        initializeSaveThread();
    }

    private void initializeSaveThread() {
        SaveThread th = new SaveThread();
        saveThread = th;
        th.start();
    }

    // For testing purposes
    @VisibleForTesting
    void setSaveThread(IAutoSave thread) {
        if (saveThread != null) {
            saveThread.fin();
        }
        saveThread = thread;
    }

    @VisibleForTesting
    static void setInstance(CoreState instance) {
        CoreState.instance = instance;
    }

    public static CoreState getInstance() {
        return instance;
    }

    private Map<String, String> cmdLineParams = Collections.emptyMap();
    private IProject project;
    private IMainWindow mainWindow;
    private IEditor editor;

    public boolean isProjectLoaded() {
        if (project == null) {
            return false;
        }
        return project.isProjectLoaded();
    }

    public Map<String, String> getCmdLineParams() {
        return cmdLineParams;
    }

    public void setCmdLineParams(Map<String, String> cmdLineParams) {
        this.cmdLineParams = cmdLineParams;
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    public IMainWindow getMainWindow() {
        return mainWindow;
    }

    public void setMainWindow(IMainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public IEditor getEditor() {
        return editor;
    }

    public void setEditor(IEditor editor) {
        this.editor = editor;
    }
}
