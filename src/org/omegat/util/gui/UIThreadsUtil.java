/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.util.gui;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.omegat.util.Log;

/**
 * Utils for check UI threads and run specific code in UI threads only.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class UIThreadsUtil {

    private UIThreadsUtil() {
    }

    /**
     * Execute code in swing thread only.
     */
    public static void executeInSwingThread(final Runnable code) {
        if (SwingUtilities.isEventDispatchThread()) {
            code.run();
        } else {
            SwingUtilities.invokeLater(code);
        }
    }

    public static <T> T returnResultFromSwingThread(Supplier<T> s) {
        if (SwingUtilities.isEventDispatchThread()) {
            return s.get();
        } else {
            AtomicReference<T> reference = new AtomicReference<>();
            try {
                SwingUtilities.invokeAndWait(() -> reference.set(s.get()));
            } catch (InvocationTargetException | InterruptedException e) {
                Logger.getLogger(StaticUIUtils.class.getName()).log(Level.WARNING, e.getLocalizedMessage(),
                        e);
            }
            return reference.get();
        }
    }

    /**
     * Check if current thread is swing thread, and report error in log if it's
     * not true.
     */
    public static void mustBeSwingThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            Log.logErrorRB("LOG_ERROR_MUST_BE_SWING_THREAD");
            Log.log(new Exception());
        }
    }

    /**
     * Check if current thread is NOT swing thread, and report error in log if
     * it's not true.
     */
    public static void mustNotBeSwingThread() {
        if (SwingUtilities.isEventDispatchThread()) {
            Log.logErrorRB("LOG_ERROR_MUSTNOT_BE_SWING_THREAD");
            Log.log(new Exception());
        }
    }
}
