/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.core;

import java.awt.Font;

import org.omegat.core.data.CoreState;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IEditorEventListener;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.events.IFontChangedEventListener;
import org.omegat.core.events.IProjectEventListener;

/**
 * Class for distribute main application events.
 * <p>
 * All events can be fired in any threads, but will be delivered to listeners
 * only in the UI thread. It's required for better threads synchronization.
 * <p>
 * Implemented the event action body in CoreState class.
 * It is a singleton class to synchronize and support test harness.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class CoreEvents {

    private CoreEvents() {
    }

    /** Register listener. */
    public static void registerProjectChangeListener(final IProjectEventListener listener) {
        CoreState.getInstance().registerProjectChangeListener(listener);
    }

    /** Unregister listener. */
    public static void unregisterProjectChangeListener(final IProjectEventListener listener) {
        CoreState.getInstance().unregisterProjectChangeListener(listener);
    }

    /** Register listener. */
    public static void registerApplicationEventListener(final IApplicationEventListener listener) {
        CoreState.getInstance().registerApplicationEventListener(listener);
    }

    /** Unregister listener. */
    public static void unregisterApplicationEventListener(final IApplicationEventListener listener) {
        CoreState.getInstance().unregisterApplicationEventListener(listener);
    }

    /** Register listener. */
    public static void registerEntryEventListener(final IEntryEventListener listener) {
        CoreState.getInstance().registerEntryEventListener(listener);
    }

    /** Unregister listener. */
    public static void unregisterEntryEventListener(final IEntryEventListener listener) {
        CoreState.getInstance().unregisterEntryEventListener(listener);
    }

    /** Register listener. */
    public static void registerFontChangedEventListener(final IFontChangedEventListener listener) {
        CoreState.getInstance().registerFontChangedEventListener(listener);
    }

    /** Unregister listener. */
    @SuppressWarnings("unused")
    public static void unregisterFontChangedEventListener(final IFontChangedEventListener listener) {
        CoreState.getInstance().unregisterFontChangedEventListener(listener);
    }

    /** Register listener. */
    public static void registerEditorEventListener(final IEditorEventListener listener) {
        CoreState.getInstance().registerEditorEventListener(listener);
    }

    /** Unregister listener. */
    public static void unregisterEditorEventListener(final IEditorEventListener listener) {
        CoreState.getInstance().unregisterEditorEventListener(listener);
    }

    /** Fire event. */
    public static void fireProjectChange(final IProjectEventListener.PROJECT_CHANGE_TYPE eventType) {
        CoreState.getInstance().fireProjectChange(eventType);
    }

    /** Fire event. */
    public static void fireApplicationStartup() {
        CoreState.getInstance().fireApplicationStartup();
    }

    /** Fire event. */
    public static void fireApplicationShutdown() {
        CoreState.getInstance().fireApplicationShutdown();
    }

    /** Fire event. */
    public static void fireEntryNewFile(final String activeFileName) {
        CoreState.getInstance().fireEntryNewFile(activeFileName);
    }

    /** Fire event. */
    public static void fireEntryActivated(final SourceTextEntry newEntry) {
        CoreState.getInstance().fireEntryActivated(newEntry);
    }

    /** Fire event. */
    public static void fireFontChanged(final Font newFont) {
        CoreState.getInstance().fireFontChanged(newFont);
    }

    /** Fire event. */
    public static void fireEditorNewWord(final String newWord) {
        CoreState.getInstance().fireEditorNewWord(newWord);
    }
}
