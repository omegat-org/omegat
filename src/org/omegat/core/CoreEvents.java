/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.core;

import java.awt.Font;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.SwingUtilities;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IEditorEventListener;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.events.IFontChangedEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.util.Log;

/**
 * Class for distribute main application events.
 * 
 * All events can be fired in any threads, but will be delivered to listeners
 * only in the UI thread. It's required for better threads synchronization.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class CoreEvents {
    private static final List<IProjectEventListener> projectEventListeners = new CopyOnWriteArrayList<IProjectEventListener>();
    private static final List<IApplicationEventListener> applicationEventListeners = new CopyOnWriteArrayList<IApplicationEventListener>();
    private static final List<IEntryEventListener> entryEventListeners = new CopyOnWriteArrayList<IEntryEventListener>();
    private static final List<IFontChangedEventListener> fontChangedEventListeners = new CopyOnWriteArrayList<IFontChangedEventListener>();
    private static final List<IEditorEventListener> editorEventListeners = new CopyOnWriteArrayList<IEditorEventListener>();

    /** Register listener. */
    public static void registerProjectChangeListener(final IProjectEventListener listener) {
        projectEventListeners.add(listener);
    }

    /** Unregister listener. */
    public static void unregisterProjectChangeListener(final IProjectEventListener listener) {
        projectEventListeners.remove(listener);
    }

    /** Register listener. */
    public static void registerApplicationEventListener(final IApplicationEventListener listener) {
        applicationEventListeners.add(listener);
    }

    /** Unregister listener. */
    public static void unregisterApplicationEventListener(final IApplicationEventListener listener) {
        applicationEventListeners.remove(listener);
    }

    /** Register listener. */
    public static void registerEntryEventListener(final IEntryEventListener listener) {
        entryEventListeners.add(listener);
    }

    /** Unregister listener. */
    public static void unregisterEntryEventListener(final IEntryEventListener listener) {
        entryEventListeners.remove(listener);
    }

    /** Register listener. */
    public static void registerFontChangedEventListener(final IFontChangedEventListener listener) {
        fontChangedEventListeners.add(listener);
    }

    /** Unregister listener. */
    public static void unregisterFontChangedEventListener(final IFontChangedEventListener listener) {
        fontChangedEventListeners.remove(listener);
    }

    /** Register listener. */
    public static void registerEditorEventListener(final IEditorEventListener listener) {
        editorEventListeners.add(listener);
    }

    /** Unregister listener. */
    public static void unregisterEditorEventListener(final IEditorEventListener listener) {
        editorEventListeners.remove(listener);
    }

    /** Fire event. */
    public static void fireProjectChange(final IProjectEventListener.PROJECT_CHANGE_TYPE eventType) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Log.logInfoRB("LOG_INFO_EVENT_PROJECT_CHANGE", eventType);
                synchronized (projectEventListeners) {
                    for (IProjectEventListener listener : projectEventListeners) {
                        listener.onProjectChanged(eventType);
                    }
                }
            }
        });
    }

    /** Fire event. */
    public static void fireApplicationStartup() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Log.logInfoRB("LOG_INFO_EVENT_APPLICATION_STARTUP");
                for (IApplicationEventListener listener : applicationEventListeners) {
                    listener.onApplicationStartup();
                }
            }
        });
    }

    /** Fire event. */
    public static void fireApplicationShutdown() {
        // We shouldn't invoke it later, because need to shutdown immediately.
        Log.logInfoRB("LOG_INFO_EVENT_APPLICATION_SHUTDOWN");
        for (IApplicationEventListener listener : applicationEventListeners) {
            listener.onApplicationShutdown();
        }
    }

    /** Fire event. */
    public static void fireEntryNewFile(final String activeFileName) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Log.logInfoRB("LOG_INFO_EVENT_ENTRY_NEWFILE", activeFileName);
                for (IEntryEventListener listener : entryEventListeners) {
                    listener.onNewFile(activeFileName);
                }
            }
        });
    }

    /** Fire event. */
    public static void fireEntryActivated(final SourceTextEntry newEntry) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Log.logInfoRB("LOG_INFO_EVENT_ENTRY_ACTIVATED");
                for (IEntryEventListener listener : entryEventListeners) {
                    listener.onEntryActivated(newEntry);
                }
            }
        });
    }

    /** Fire event. */
    public static void fireFontChanged(final Font newFont) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Log.logInfoRB("LOG_INFO_EVENT_FONT_CHANGED");
                for (IFontChangedEventListener listener : fontChangedEventListeners) {
                    listener.onFontChanged(newFont);
                }
            }
        });
    }

    /** Fire event. */
    public static void fireEditorNewWord(final String newWord) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (IEditorEventListener listener : editorEventListeners) {
                    listener.onNewWord(newWord);
                }
            }
        });
    }
}
