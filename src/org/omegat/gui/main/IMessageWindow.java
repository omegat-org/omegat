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

package org.omegat.gui.main;

/**
 * Interface for access to message window functionality.
 * 
 * @author Martin Fleurke
 */
public interface IMessageWindow {

    /**
     * Display error.
     * 
     * @param ex
     *                exception to show
     * @param errorKey
     *                error message key in resource bundle
     * @param params
     *                error text parameters
     */
    void displayErrorRB(Throwable ex, String errorKey, Object... params);

    /**
     * Show message in status window / - bar from resource bundle by key.
     * 
     * @param messageKey
     *                message key in resource bundle
     * @param params
     *                message parameters for formatting
     */
    void showStatusMessageRB(String messageKey, Object... params);
    
    /**
     * Show message in an ErrorDialog
     * 
     * @param message 
     *                message key in resource bundle of message that is to be 
     *                displayed 
     * @param title 
     *                title of dialog. message key in resource bundle of title 
     *                that is to be displayed
     */
    void showErrorDialogRB(String message, String title);
}
