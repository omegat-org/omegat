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

import org.omegat.core.threads.IAutoSave;
import org.omegat.gui.main.IMainWindow;
import org.omegat.util.Log;

import javax.swing.SwingUtilities;
import java.awt.Window;
import java.util.Collections;

public class TestCoreState extends CoreState {

    /**
     * Smart reset that handles both unit tests and GUI tests appropriately.
     * - For unit tests (no GUI): Creates a new instance for complete isolation
     * - For GUI tests (with GUI): Resets internal state while preserving singleton reference
     */
    public static void resetState() {
        CoreState currentInstance = CoreState.getInstance();

        if (isGUIContext()) {
            // GUI context: Reset internal state but keep same instance
            resetInternalStateForGUI(currentInstance);
        } else {
            // Unit test context: Create fresh instance for complete isolation
            currentInstance.setMainWindow(null);
            currentInstance.setSaveThread(null);
            setTestInstance(new TestCoreState());
        }
    }

    /**
     * Detect if we're in a GUI context by checking for active Swing components.
     * This is more reliable than just checking mainWindow != null because
     * GUI components might exist even after mainWindow is cleared.
     */
    private static boolean isGUIContext() {
        // Check if we're running on EDT
        if (SwingUtilities.isEventDispatchThread()) {
            return true;
        }

        // Check if there are any active Swing windows
        try {
            Window[] windows = Window.getWindows();
            for (Window window : windows) {
                if (window.isDisplayable() && !window.getClass().getName().contains("Popup")) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Ignore exceptions during detection
        }

        // Check if main window exists and has a frame
        IMainWindow mainWindow = CoreState.getInstance().getMainWindow();
        if (mainWindow != null && mainWindow.getApplicationFrame() != null) {
            return true;
        }

        // Check if any GUI components are registered in CoreState
        return hasGUIComponentsRegistered(CoreState.getInstance());
    }

    /**
     * Check if any GUI components are registered in the CoreState.
     */
    private static boolean hasGUIComponentsRegistered(CoreState state) {
        return state.getEditor() != null
                || state.getGlossaries() != null
                || state.getNotes() != null
                || state.getMatcher() != null
                || state.getProjWin() != null
                || state.getComments() != null
                || state.getMachineTranslatePane() != null
                || state.getDictionaries() != null
                || state.getSegmentPropertiesArea() != null;
    }
    /**
     * Reset internal state for GUI context while preserving singleton instance.
     * This ensures proper initialization order and prevents NPEs during project load events.
     */
    private static void resetInternalStateForGUI(CoreState state) {
        // 1. First, clear project-related state to prevent event listeners from accessing stale data
        state.setProject(new NotLoadedProject());

        // 2. Stop any running threads safely
        if (state.getAutoSave() != null) {
            state.getAutoSave().disable();
        }
        state.setSaveThread(null);

        // 3. Clear GUI component references (but keep mainWindow for last)
        IMainWindow mainWindow = state.getMainWindow(); // Preserve reference temporarily
        if (mainWindow.getApplicationFrame() != null) {
            mainWindow.lockUI();
        }
        state.setEditor(null);
        state.setGlossaries(null);
        state.setNotes(null);
        state.setMatcher(null);
        state.setProjWin(null);
        state.setComments(null);
        state.setMachineTranslatePane(null);
        state.setDictionaries(null);
        state.setSegmentPropertiesArea(null);
        state.setSpellCheckerManager(null);

        // 4. Clear core components
        state.setSegmenter(null);
        state.setFilterMaster(null);
        state.setGlossaryManager(null);
        state.setTagValidation(null);
        state.setIssuesWindow(null);

        // 5. Reset other state
        state.setCmdLineParams(Collections.emptyMap());

        // 6. Clear main window last to ensure event listeners still have access during cleanup
        state.setMainWindow(null);
        if (mainWindow.getApplicationFrame() != null) {
            mainWindow.unlockUI();
        }
    }

    /**
     * Force a complete reset even in GUI context.
     * Use this only when you need complete isolation and are willing to
     * handle the consequences of changing the singleton reference.
     */
    public static void forceCompleteReset() {
        CoreState.getInstance().setMainWindow(null);
        CoreState.getInstance().setSaveThread(null);
        setTestInstance(new TestCoreState());
    }

    /**
     * Reset for GUI tests - ensures internal state is cleared but keeps same instance.
     * This is an explicit method for GUI test classes to use.
     */
    public static void resetStateForGUI() {
        resetInternalStateForGUI(CoreState.getInstance());
    }

    private static void setTestInstance(TestCoreState instance) {
        CoreState.setInstance(instance);
    }

    public static void initAutoSave(IAutoSave autoSave) {
        CoreState.getInstance().setSaveThread(autoSave);
    }
}
