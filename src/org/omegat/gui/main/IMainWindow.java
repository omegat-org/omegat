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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.gui.main;

import java.awt.Cursor;
import java.awt.Font;

import javax.swing.JFrame;

import com.vlsolutions.swing.docking.Dockable;

/**
 * Interface for access to main window functionality.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public interface IMainWindow {
    /**
     * Get application frame.
     */
    JFrame getApplicationFrame();

    /**
     * Lock UI for long-term operations.
     */
    void lockUI();

    /**
     * Unlock UI after locking.
     */
    void unlockUI();

    /**
     * Get main application font.
     */
    Font getApplicationFont();

    /**
     * Show message in status bar from resource bundle by key.
     * 
     * @param messageKey
     *            message key in resource bundle
     * @param params
     *            message parameters for formatting
     */
    void showStatusMessageRB(String messageKey, Object... params);

    /**
     * Show message in progress bar. Progress bar shows the translation progress: nr. of segments/words etc translated/to do.
     * 
     * @param messageText
     *            message text
     */
    void showProgressMessage(String messageText);

    /**
     * Show message in length label. Length label shows length (in nr of characters) of current segment
     * 
     * @param messageText
     *            message text
     */
    void showLengthMessage(String messageText);

    /**
     * Display error.
     * 
     * @param ex
     *            exception to show
     * @param errorKey
     *            error message key in resource bundle
     * @param params
     *            error text parameters
     */
    void displayErrorRB(Throwable ex, String errorKey, Object... params);

    /**
     * Show message in an ErrorDialog
     * 
     * @param message
     *            message key in resource bundle of message that is to be
     *            displayed
     * @param args
     *            arguments of the resource bundle message
     * @param title
     *            title of dialog. message key in resource bundle of title that
     *            is to be displayed
     */
    void showErrorDialogRB(String message, Object[] args, String title);

    /**
     * Add new dockable pane into application frame. This method called on
     * application startup.
     * 
     * @param pane
     *            dockable pane
     */
    void addDockable(Dockable pane);

    /**
     * Sets cursor of window
     * @param cursor the new cursor
     */
    void setCursor(Cursor cursor);

    /**
     * Retrieves current cursor of window
     * @return the current cursor
     */
    Cursor getCursor();

    /**
     * Retrieve main manu instance.
     */
    IMainMenu getMainMenu();
}
