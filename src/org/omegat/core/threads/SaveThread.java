/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Alex Buloichik
               2012 Didier Briel
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

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.omegat.core.Core;
import org.omegat.core.KnownException;
import org.omegat.core.data.IProject;
import org.omegat.util.Preferences;

/**
 * An independent stream to save project, created in order not to freese UI
 * while project is saved (may take a lot)
 * 
 * @author Keith Godfrey
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 */
public class SaveThread extends Thread implements IAutoSave {
    private static final Logger LOGGER = Logger.getLogger(SaveThread.class.getName());

    private static final int SAVE_DURATION = (new Integer(Preferences.getPreferenceDefault(
                        Preferences.AUTO_SAVE_INTERVAL,          // Preferences are in seconds,
                        Preferences.AUTO_SAVE_DEFAULT)) * 1000); // save duration is in milliseconds

    private boolean needToSaveNow;
    private boolean enabled;

    public SaveThread() {
        setName("Save thread");
    }

    public synchronized void disable() {
        LOGGER.fine("Disable autosave");
        enabled = false;
    }

    public synchronized void enable() {
        LOGGER.fine("Enable autosave");
        enabled = true;
        needToSaveNow = false;
        notify();
    }

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (this) {
                    // Set flag for saving. If somebody will reset time, he will
                    // clear this flag also.
                    needToSaveNow = true;
                    // sleep
                    wait(SAVE_DURATION);
                }
                if (needToSaveNow && enabled) {
                    // Wait finished by time and autosaving enabled.
                    IProject dataEngine = Core.getProject();
                    LOGGER.fine("Start project save from SaveThread");
                    try {
                        dataEngine.saveProject();
                        Core.getMainWindow().showStatusMessageRB("ST_PROJECT_AUTOSAVED",
                                DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date()));
                    } catch (KnownException ex) {
                        Core.getMainWindow().showStatusMessageRB(ex.getMessage(), ex.getParams());
                    }
                    LOGGER.fine("Finish project save from SaveThread");
                }
            }
        } catch (InterruptedException ex) {
            LOGGER.log(Level.WARNING, "Save thread interrupted", ex);
            return;
        }
    }
}
