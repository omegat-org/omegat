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

import java.awt.Font;

import javax.swing.JFrame;

import org.omegat.util.OStrings;
import org.omegat.util.RuntimePreferences;
import org.omegat.util.StaticUtils;

import com.vlsolutions.swing.docking.Dockable;

/**
 * The main window of OmegaT application, if the program is started in 
 * consoleMode. 
 *
 * @author Martin Fleurke
 */
public class ConsoleWindow implements IMainWindow {
    /**
     * {@inheritDoc}
     */
    public void displayErrorRB(Throwable ex, String errorKey, Object... params) {
        String msg;
        if (params != null) {
            msg = StaticUtils.format(OStrings.getString(errorKey),
                    params);
        } else {
            msg = OStrings.getString(errorKey);
        }

        System.err.println(msg);
        String fulltext = msg;
        if (ex != null)
            fulltext += "\n" + ex.toString(); // NOI18N
        System.err.println(OStrings.getString("TF_ERROR"));
        System.err.println(fulltext);

    }
    /**
     * {@inheritDoc}
     * Nothing is shown in quiet mode.
     */
    public void showStatusMessageRB(String messageKey, Object... params) {
        if (RuntimePreferences.isQuietMode()) return;

        final String msg;
        if (messageKey == null) {
            msg = new String() + ' ';
        } else {
            if (params != null) {
                msg = StaticUtils
                        .format(OStrings.getString(messageKey), params);
            } else {
                msg = OStrings.getString(messageKey);
            }
        }
        System.out.println(msg);
    }
    /**
     * {@inheritDoc}
     */
    public void showErrorDialogRB(String message, Object[] args, String title) {
        System.err.println(StaticUtils.format(OStrings.getString(message), args));
    }
    
    public void addDockable(Dockable pane) {
        throw new NoSuchMethodError("Invalid call of ConsoleWindow");
    }
    public Font getApplicationFont() {
        throw new NoSuchMethodError("Invalid call of ConsoleWindow");
    }
    public JFrame getApplicationFrame() {
        throw new NoSuchMethodError("Invalid call of ConsoleWindow");
    }
    public void lockUI() {
        throw new NoSuchMethodError("Invalid call of ConsoleWindow");
    }
    public void showLengthMessage(String messageText) {
        throw new NoSuchMethodError("Invalid call of ConsoleWindow");
    }
    public void showProgressMessage(String messageText) {
        throw new NoSuchMethodError("Invalid call of ConsoleWindow");
    }
    public void unlockUI() {
        throw new NoSuchMethodError("Invalid call of ConsoleWindow");
    }
    public IMainMenu getMainMenu() {
        throw new NoSuchMethodError("Invalid call of ConsoleWindow");
    }
}
