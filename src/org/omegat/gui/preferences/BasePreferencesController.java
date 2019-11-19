/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.gui.preferences;

import java.util.concurrent.CopyOnWriteArrayList;

import org.omegat.util.Preferences;

/**
 * A base preferences controller implementation.
 *
 * @author Aaron Madlon-Kay
 */
public abstract class BasePreferencesController implements IPreferencesController {

    private CopyOnWriteArrayList<FurtherActionListener> listeners = new CopyOnWriteArrayList<>();
    private boolean restartRequired = false;
    private boolean reloadRequired = false;

    @Override
    public void addFurtherActionListener(FurtherActionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeFurtherActionListener(FurtherActionListener listener) {
        listeners.remove(listener);
    }

    protected boolean valueIsDifferent(String prefsKey, Object newValue) {
        String oldValue = Preferences.getPreference(prefsKey);
        return !oldValue.equals(newValue.toString());
    }

    protected void fireRestartRequired() {
        for (FurtherActionListener listener : listeners) {
            listener.setRestartRequired(restartRequired);
        }
    }

    protected void fireReloadRequired() {
        for (FurtherActionListener listener : listeners) {
            listener.setReloadRequired(reloadRequired);
        }
    }

    public void setRestartRequired(boolean restartRequired) {
        this.restartRequired = restartRequired;
        fireRestartRequired();
    }

    public void setReloadRequired(boolean reloadRequired) {
        this.reloadRequired = reloadRequired;
        fireReloadRequired();
    }

    @Override
    public boolean isRestartRequired() {
        return restartRequired;
    }

    @Override
    public boolean isReloadRequired() {
        return reloadRequired;
    }

    @Override
    public void undoChanges() {
        initFromPrefs();
        setReloadRequired(false);
        setRestartRequired(false);
    }

    /**
     * Apply current user preferences to the GUI.
     */
    protected abstract void initFromPrefs();

    // Re-declared as abstract here to ensure that subclasses override
    // with a meaningful return value
    @Override
    public abstract String toString();
}
