/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.core;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.util.Log;

/**
 * Class for distribute main application events.
 * 
 * All events can be fired in any threads, but will be delivered to listeners
 * only in the UI thread.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class CoreEvents {
    private static final List<IProjectEventListener> projectEventListeners = new ArrayList<IProjectEventListener>();
    private static final List<IApplicationEventListener> applicationEventListeners = new ArrayList<IApplicationEventListener>();

    /** Register listener. */
    public static void registerProjectChangeListener(final IProjectEventListener listener) {
        synchronized (projectEventListeners) {
            projectEventListeners.add(listener);
        }
    }

    /** Unregister listener. */
    public static void unregisterProjectChangeListener(final IProjectEventListener listener) {
        synchronized (projectEventListeners) {
            projectEventListeners.remove(listener);
        }
    }

    /** Register listener. */
    public static void registerApplicationEventListener(final IApplicationEventListener listener) {
        synchronized (applicationEventListeners) {
            applicationEventListeners.add(listener);
        }
    }

    /** Unregister listener. */
    public static void unregisterApplicationEventListener(final IApplicationEventListener listener) {
        synchronized (applicationEventListeners) {
            applicationEventListeners.remove(listener);
        }
    }

    /** Fire event. */
    public static void fireProjectChange() {
        Log.logInfoRB("LOG_INFO_EVENT_PROJECT_CHANGE");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (projectEventListeners) {
                    for (IProjectEventListener listener : projectEventListeners) {
                        listener.onProjectChanged();
                    }
                }
            }
        });
    }

    /** Fire event. */
    public static void fireApplicationStartup() {
        Log.logInfoRB("LOG_INFO_EVENT_APPLICATION_STARTUP");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (applicationEventListeners) {
                    for (IApplicationEventListener listener : applicationEventListeners) {
                        listener.onApplicationStartup();
                    }
                }
            }
        });
    }

    /** Fire event. */
    public static void fireApplicationShutdown() {
        Log.logInfoRB("LOG_INFO_EVENT_APPLICATION_SHUTDOWN");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (applicationEventListeners) {
                    for (IApplicationEventListener listener : applicationEventListeners) {
                        listener.onApplicationShutdown();
                    }
                }
            }
        });
    }
}
