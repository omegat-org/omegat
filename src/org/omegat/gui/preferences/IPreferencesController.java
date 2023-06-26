/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.gui.preferences;

import java.awt.Component;

/**
 * An interface implemented by views shown in the Preferences window. See
 * <code>BasePreferencesController</code> for a base implementation.
 *
 * @author Aaron Madlon-Kay
 *
 */
public interface IPreferencesController {

    /**
     * An interface used by observers interested in knowing when a preference
     * has been altered that requires the application to be restarted or the
     * project to be reloaded.
     */
    interface FurtherActionListener {
        void setReloadRequired(boolean reloadRequired);

        void setRestartRequired(boolean restartRequired);
    }

    /**
     * Add a listener
     */
    void addFurtherActionListener(FurtherActionListener listener);

    /**
     * Remove a listener
     */
    void removeFurtherActionListener(FurtherActionListener listener);

    /**
     * Returns whether a preference has been altered such as to require the
     * application to be restarted.
     */
    boolean isRestartRequired();

    /**
     * Returns whether a preference has been altered such as to require the
     * project to be reloaded.
     */
    boolean isReloadRequired();

    /**
     * Implementors should override this to return the name of the view as shown in the view tree.
     */
    String toString();

    /**
     * Get the GUI (the "view") controlled by this controller. This should not
     * be a window (e.g. JDialog, JFrame) but rather a component embeddable in a
     * window (e.g. JPanel).
     */
    Component getGui();

    /**
     * Get the parent view in the view tree. Implementors should override this
     * to return the class of the desired parent; by default this is the Plugins
     * view.
     */
    default Class<? extends IPreferencesController> getParentViewClass() {
        return null;
    }

    /**
     * Commit changes.
     */
    void persist();

    /**
     * Validate the current preferences. Implementors should override to
     * implement validation logic as necessary.
     * <p>
     * When validation fails, implementors should <i>not</i> raise dialogs;
     * instead they should offer feedback within the view GUI.
     *
     * @return True if the settings are valid and OK to be persisted; false if
     *         not
     */
    default boolean validate() {
        return true;
    }

    /**
     * Restore preferences controlled by this view to their current persisted
     * state.
     */
    void undoChanges();

    /**
     * Restore preferences controlled by this view to their default state.
     */
    void restoreDefaults();

    /**
     * Whether supporting `restoreDefaults` feature.
     */
    default boolean canRestoreDefaults() {
        return true;
    }
}
