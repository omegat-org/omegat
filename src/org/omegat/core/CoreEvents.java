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

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

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
    private static final List<IProjectEventListener> projectEventListeners = new ArrayList<IProjectEventListener>();
    private static final List<IApplicationEventListener> applicationEventListeners = new ArrayList<IApplicationEventListener>();
    private static final List<IEntryEventListener> entryEventListeners = new ArrayList<IEntryEventListener>();
    private static final List<IFontChangedEventListener> fontChangedEventListeners = new ArrayList<IFontChangedEventListener>();
    private static final List<IEditorEventListener> editorEventListeners = new ArrayList<IEditorEventListener>();

    /** Register listener. */
    public static void registerProjectChangeListener(
            final IProjectEventListener listener) {
        synchronized (projectEventListeners) {
            projectEventListeners.add(listener);
        }
    }

    /** Unregister listener. */
    public static void unregisterProjectChangeListener(
            final IProjectEventListener listener) {
        synchronized (projectEventListeners) {
            projectEventListeners.remove(listener);
        }
    }

    /** Register listener. */
    public static void registerApplicationEventListener(
            final IApplicationEventListener listener) {
        synchronized (applicationEventListeners) {
            applicationEventListeners.add(listener);
        }
    }

    /** Unregister listener. */
    public static void unregisterApplicationEventListener(
            final IApplicationEventListener listener) {
        synchronized (applicationEventListeners) {
            applicationEventListeners.remove(listener);
        }
    }

    /** Register listener. */
    public static void registerEntryEventListener(
            final IEntryEventListener listener) {
        synchronized (entryEventListeners) {
            entryEventListeners.add(listener);
        }
    }

    /** Unregister listener. */
    public static void unregisterEntryEventListener(
            final IEntryEventListener listener) {
        synchronized (entryEventListeners) {
            entryEventListeners.remove(listener);
        }
    }

    /** Register listener. */
    public static void registerFontChangedEventListener(
            final IFontChangedEventListener listener) {
        synchronized (fontChangedEventListeners) {
            fontChangedEventListeners.add(listener);
        }
    }

    /** Unregister listener. */
    public static void unregisterFontChangedEventListener(
            final IFontChangedEventListener listener) {
        synchronized (fontChangedEventListeners) {
            fontChangedEventListeners.remove(listener);
        }
    }
    
    /** Register listener. */
    public static void registerEditorEventListener(
            final IEditorEventListener listener) {
        synchronized (editorEventListeners) {
            editorEventListeners.add(listener);
        }
    }

    /** Unregister listener. */
    public static void unregisterEditorEventListener(
            final IEditorEventListener listener) {
        synchronized (editorEventListeners) {
            editorEventListeners.remove(listener);
        }
    }


    /** Fire event. */
    public static void fireProjectChange(
            final IProjectEventListener.PROJECT_CHANGE_TYPE eventType) {
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
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Log.logInfoRB("LOG_INFO_EVENT_APPLICATION_SHUTDOWN");
                synchronized (applicationEventListeners) {
                    for (IApplicationEventListener listener : applicationEventListeners) {
                        listener.onApplicationShutdown();
                    }
                }
            }
        });
    }

    /** Fire event. */
    public static void fireEntryNewFile(final String activeFileName) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Log.logInfoRB("LOG_INFO_EVENT_ENTRY_NEWFILE", activeFileName);
                synchronized (entryEventListeners) {
                    for (IEntryEventListener listener : entryEventListeners) {
                        listener.onNewFile(activeFileName);
                    }
                }
            }
        });
    }

    /** Fire event. */
    public static void fireEntryActivated(final SourceTextEntry newEntry) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Log.logInfoRB("LOG_INFO_EVENT_ENTRY_ACTIVATED");
                synchronized (entryEventListeners) {
                    for (IEntryEventListener listener : entryEventListeners) {
                        listener.onEntryActivated(newEntry);
                    }
                }
            }
        });
    }
    
    /** Fire event. */
    public static void fireFontChanged(final Font newFont) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Log.logInfoRB("LOG_INFO_EVENT_FONT_CHANGED");
                synchronized (fontChangedEventListeners) {
                    for (IFontChangedEventListener listener : fontChangedEventListeners) {
                        listener.onFontChanged(newFont);
                    }
                }
            }
        });
    }
    

    /** Fire event. */
    public static void fireEditorNewWord(final String newWord) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (editorEventListeners) {
                    for (IEditorEventListener listener : editorEventListeners) {
                        listener.onNewWord(newWord);
                    }
                }
            }
        });
    }    
}
