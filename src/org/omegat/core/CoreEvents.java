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

package org.omegat.core;

import java.awt.Component;
import java.awt.Font;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IEditorEventListener;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.events.IFontChangedEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.main.IMainWindow;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

/**
 * Class for distribute main application events.
 *
 * All events can be fired in any threads, but will be delivered to listeners
 * only in the UI thread. It's required for better threads synchronization.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class CoreEvents {
    private static final List<IProjectEventListener> PROJECT_EVENT_LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<IApplicationEventListener> APPLICATION_EVENT_LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<IEntryEventListener> ENTRY_EVENT_LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<IFontChangedEventListener> FONT_CHANGED_EVENT_LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<IEditorEventListener> EDITOR_EVENT_LISTENERS = new CopyOnWriteArrayList<>();

    private CoreEvents() {
    }

    /** Register listener. */
    public static void registerProjectChangeListener(final IProjectEventListener listener) {
        PROJECT_EVENT_LISTENERS.add(listener);
    }

    /** Unregister listener. */
    public static void unregisterProjectChangeListener(final IProjectEventListener listener) {
        PROJECT_EVENT_LISTENERS.remove(listener);
    }

    /** Register listener. */
    public static void registerApplicationEventListener(final IApplicationEventListener listener) {
        APPLICATION_EVENT_LISTENERS.add(listener);
    }

    /** Unregister listener. */
    public static void unregisterApplicationEventListener(final IApplicationEventListener listener) {
        APPLICATION_EVENT_LISTENERS.remove(listener);
    }

    /** Register listener. */
    public static void registerEntryEventListener(final IEntryEventListener listener) {
        ENTRY_EVENT_LISTENERS.add(listener);
    }

    /** Unregister listener. */
    public static void unregisterEntryEventListener(final IEntryEventListener listener) {
        ENTRY_EVENT_LISTENERS.remove(listener);
    }

    /** Register listener. */
    public static void registerFontChangedEventListener(final IFontChangedEventListener listener) {
        FONT_CHANGED_EVENT_LISTENERS.add(listener);
    }

    /** Unregister listener. */
    public static void unregisterFontChangedEventListener(final IFontChangedEventListener listener) {
        FONT_CHANGED_EVENT_LISTENERS.remove(listener);
    }

    /** Register listener. */
    public static void registerEditorEventListener(final IEditorEventListener listener) {
        EDITOR_EVENT_LISTENERS.add(listener);
    }

    /** Unregister listener. */
    public static void unregisterEditorEventListener(final IEditorEventListener listener) {
        EDITOR_EVENT_LISTENERS.remove(listener);
    }

    /** Fire event. */
    public static void fireProjectChange(final IProjectEventListener.PROJECT_CHANGE_TYPE eventType) {
        SwingUtilities.invokeLater(() -> {
            Log.logInfoRB("LOG_INFO_EVENT_PROJECT_CHANGE", eventType);
            for (IProjectEventListener listener : PROJECT_EVENT_LISTENERS) {
                try {
                    listener.onProjectChanged(eventType);
                } catch (Throwable t) {
                    log("ERROR_EVENT_PROJECT_CHANGE", t);
                }
            }
        });
    }

    /** Fire event. */
    public static void fireApplicationStartup() {
        SwingUtilities.invokeLater(() -> {
            Log.logInfoRB("LOG_INFO_EVENT_APPLICATION_STARTUP");
            for (IApplicationEventListener listener : APPLICATION_EVENT_LISTENERS) {
                try {
                    listener.onApplicationStartup();
                } catch (Throwable t) {
                    log("ERROR_EVENT_APPLICATION_STARTUP", t);
                }
            }
        });
    }

    /** Fire event. */
    public static void fireApplicationShutdown() {
        // We shouldn't invoke it later, because need to shutdown immediately.
        Log.logInfoRB("LOG_INFO_EVENT_APPLICATION_SHUTDOWN");
        for (IApplicationEventListener listener : APPLICATION_EVENT_LISTENERS) {
            try {
                listener.onApplicationShutdown();
            } catch (Throwable t) {
                log("ERROR_EVENT_APPLICATION_SHUTDOWN", t);
            }
        }
    }

    /** Fire event. */
    public static void fireEntryNewFile(final String activeFileName) {
        SwingUtilities.invokeLater(() -> {
            Log.logInfoRB("LOG_INFO_EVENT_ENTRY_NEWFILE", activeFileName);
            for (IEntryEventListener listener : ENTRY_EVENT_LISTENERS) {
                try {
                    listener.onNewFile(activeFileName);
                } catch (Throwable t) {
                    log("ERROR_EVENT_ENTRY_NEWFILE", t);
                }
            }
        });
    }

    /** Fire event. */
    public static void fireEntryActivated(final SourceTextEntry newEntry) {
        SwingUtilities.invokeLater(() -> {
            Log.logInfoRB("LOG_INFO_EVENT_ENTRY_ACTIVATED");
            for (IEntryEventListener listener : ENTRY_EVENT_LISTENERS) {
                try {
                    listener.onEntryActivated(newEntry);
                } catch (Throwable t) {
                    log("ERROR_EVENT_ENTRY_ACTIVATED", t);
                }
            }
        });
    }

    /** Fire event. */
    public static void fireFontChanged(final Font newFont) {
        SwingUtilities.invokeLater(() -> {
            Log.logInfoRB("LOG_INFO_EVENT_FONT_CHANGED");
            for (IFontChangedEventListener listener : FONT_CHANGED_EVENT_LISTENERS) {
                try {
                    listener.onFontChanged(newFont);
                } catch (Throwable t) {
                    log("ERROR_EVENT_FONT_CHANGED", t);
                }
            }
        });
    }

    /** Fire event. */
    public static void fireEditorNewWord(final String newWord) {
        SwingUtilities.invokeLater(() -> {
            for (IEditorEventListener listener : EDITOR_EVENT_LISTENERS) {
                try {
                    listener.onNewWord(newWord);
                } catch (Throwable t) {
                    log("ERROR_EVENT_EDITOR_NEW_WORD", t);
                }
            }
        });
    }

    private static void log(String msgKey, Throwable t) {
        Log.log(t);
        // Main window might not yet be available
        Component parent = Optional.ofNullable(Core.getMainWindow()).map(IMainWindow::getApplicationFrame).orElse(null);
        JOptionPane.showMessageDialog(parent, OStrings.getString(msgKey, t), OStrings.getString("ERROR_TITLE"),
                JOptionPane.ERROR_MESSAGE);
    }
}
