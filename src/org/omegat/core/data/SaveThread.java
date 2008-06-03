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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.core.data;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.omegat.core.Core;

/**
 * An independent stream to save project, created in order not to freese UI
 * while project is saved (may take a lot)
 * 
 * @author Keith Godfrey
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
class SaveThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(SaveThread.class
            .getName());

    private static final int SAVE_DURATION = 10 * 60 * 1000; // 10 minutes;

    private boolean needToSaveNow;

    public SaveThread() {
        setName("Save thread"); // NOI18N
    }

    /**
     * This method called from other thread. Somebody calls this method for
     * start autosave waiting time again. It happen after each manual project
     * saving or project loading.
     */
    public synchronized void resetTime() {
        LOGGER.fine("Reset time for SaveThread");
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
                if (needToSaveNow) {
                    // Nobody didn't clear save flag. Then save project.
                    IDataEngine dataEngine = Core.getDataEngine();
                    // need to synchronize arounf DataENgine against change
                    // project loaded state
                        if (dataEngine.isProjectLoaded()) {
                            LOGGER.fine("Start project save from SaveThread");
                            Core.getDataEngine().saveProject();
                            LOGGER.fine("Finish project save from SaveThread");
                            Core.getMainWindow().showStatusMessageRB(
                                    "ST_PROJECT_AUTOSAVED",
                                    DateFormat
                                            .getTimeInstance(DateFormat.SHORT)
                                            .format(new Date()));
                        }
                }
            }
        } catch (InterruptedException ex) {
            LOGGER.log(Level.WARNING, "Save thread interrupted", ex);
            return;
        }
    }
}
