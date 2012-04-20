/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.core.threads;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;

/**
 * An independent stream to save project, created in order not to freese UI
 * while project is saved (may take a lot)
 * 
 * @author Keith Godfrey
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SaveThread extends Thread implements IAutoSave {
    private static final Logger LOGGER = Logger.getLogger(SaveThread.class.getName());

    private static final int SAVE_DURATION = 3 * 60 * 1000; // 3 minutes;

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
                    dataEngine.saveProject();
                    LOGGER.fine("Finish project save from SaveThread");
                    Core.getMainWindow().showStatusMessageRB("ST_PROJECT_AUTOSAVED",
                            DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date()));
                }
            }
        } catch (InterruptedException ex) {
            LOGGER.log(Level.WARNING, "Save thread interrupted", ex);
            return;
        }
    }
}
