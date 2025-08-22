/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Alex Buloichik
               2012 Didier Briel
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

package org.omegat.core.threads;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import org.omegat.core.Core;
import org.omegat.core.KnownException;
import org.omegat.core.data.IProject;
import org.omegat.core.team2.IRemoteRepository2;
import org.omegat.util.Log;
import org.omegat.util.Preferences;

/**
 * An independent stream to save project, created in order not to freese UI
 * while project is saved (may take a lot)
 *
 * @author Keith Godfrey
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 */
public class SaveThread extends Thread implements IAutoSave {
    private final Object lock = new Object();

    private volatile boolean running;

    /** The length the thread should wait in milliseconds */
    private int waitDuration;
    private boolean needToSaveNow;
    private boolean enabled;

    public SaveThread() {
        setName("Save thread");
        setWaitDuration(Preferences.getPreferenceDefault(Preferences.AUTO_SAVE_INTERVAL,
                Preferences.AUTO_SAVE_DEFAULT));
        running = true;
        Preferences.addPropertyChangeListener(Preferences.AUTO_SAVE_INTERVAL, evt -> {
            synchronized (lock) {
                setWaitDuration((Integer) evt.getNewValue());
                lock.notifyAll();
            }
        });
    }

    private void setWaitDuration(int seconds) {
        waitDuration = seconds * 1000;
    }

    public void disable() {
        Log.logDebug("Disable autosave");
        synchronized (lock) {
            enabled = false;
        }
    }

    public void enable() {
        Log.logDebug("Enable autosave");
        synchronized (lock) {
            enabled = true;
            needToSaveNow = false;
            lock.notifyAll();
        }
    }

    @Override
    public void run() {
        try {
            while (running) {
                synchronized (lock) {
                    // Set the flag for saving. Clear the flag if the timer is
                    // reset.
                    needToSaveNow = true;
                    // sleep
                    lock.wait(waitDuration);
                }

                boolean execute;
                synchronized (lock) {
                    // Wait finished by time and autosaving enabled.
                    execute = needToSaveNow && enabled;
                }

                if (execute) {
                    Log.logDebug("Start project save from SaveThread");
                    executeSave();
                    Log.logDebug("Finish project save from SaveThread");
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            Log.logDebug("Save thread interrupted:", ex);
        }
    }

    private static void executeSave() {
        IProject dataEngine = Core.getProject();
        try {
            Core.executeExclusively(false, () -> {
                dataEngine.saveProject(false);
                dataEngine.teamSyncPrepare();
            });
            Core.getMainWindow().showStatusMessageRB("ST_PROJECT_AUTOSAVED",
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date()));
        } catch (TimeoutException ex) {
            Log.logWarningRB("AUTOSAVE_LOCK_ACQUISITION_TIMEOUT");
        } catch (KnownException ex) {
            Core.getMainWindow().showStatusMessageRB(ex.getMessage(), ex.getParams());
        } catch (IRemoteRepository2.NetworkException ex) {
            Log.logWarningRB("TEAM_NETWORK_ERROR", ex.getMessage());
        } catch (OutOfMemoryError oome) {
            // inform the user
            long memory = Runtime.getRuntime().maxMemory() / 1024 / 1024;
            Log.logErrorRB(oome, "OUT_OF_MEMORY", memory);
            Core.getMainWindow().showErrorDialogRB("TF_ERROR", "OUT_OF_MEMORY", memory);
            // Just quit, we can't help it anyway
            System.exit(1);
        } catch (Exception ex) {
            Log.logWarningRB("AUTOSAVE_GENERIC_ERROR", ex.getMessage());
        }
    }

    @Override
    public void fin() {
        synchronized (lock) {
            running = false;
            lock.notifyAll();
        }
        try {
            join((long) waitDuration * 2);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
