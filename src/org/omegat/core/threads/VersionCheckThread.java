/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.core.threads;

import java.awt.Window;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.FocusManager;
import javax.swing.SwingUtilities;

import org.omegat.core.Core;
import org.omegat.gui.dialogs.VersionCheckDialog;
import org.omegat.gui.main.IMainWindow;
import org.omegat.util.Preferences;
import org.omegat.util.VersionChecker;

public final class VersionCheckThread extends LongProcessThread {

    private static final Logger LOGGER = Logger.getLogger(VersionCheckThread.class.getName());
    private static final int CHECK_INTERVAL = 60 * 60 * 24 * 1000;

    private final int initialDelaySeconds;
    private boolean enabled = true;

    public VersionCheckThread(int initialDelaySeconds) {
        this.initialDelaySeconds = initialDelaySeconds;
        enabled = Preferences.isPreferenceDefault(Preferences.VERSION_CHECK_AUTOMATIC,
                Preferences.VERSION_CHECK_AUTOMATIC_DEFAULT);
        Preferences.addPropertyChangeListener(Preferences.VERSION_CHECK_AUTOMATIC,
                e -> enabled = (Boolean) e.getNewValue());
    }

    @Override
    public void run() {
        try {
            synchronized (this) {
                wait(initialDelaySeconds * 1000);
            }
            while (true) {
                synchronized (this) {
                    try {
                        if (enabled && !VersionChecker.getInstance().isUpToDate()) {
                            VersionCheckDialog dialog = new VersionCheckDialog(VersionChecker.getInstance().getRemoteVersion());
                            SwingUtilities.invokeAndWait(() -> dialog.show(getParentWindow()));
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
                    }
                    wait(CHECK_INTERVAL);
                }
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Version check thread interrupted", e);
        }
    }

    private Window getParentWindow() {
        Window window = FocusManager.getCurrentManager().getActiveWindow();
        if (window == null) {
            IMainWindow mw = Core.getMainWindow();
            if (mw != null) {
                window = mw.getApplicationFrame();
            }
        }
        return window;
    }
}
