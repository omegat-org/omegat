/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.gui.messages;

import javax.swing.SwingUtilities;

import org.omegat.gui.main.MainWindow;

/**
 * Methods to send messages to UI objects
 * use indirect methods because Swing objects tend to lockup in
 * java 1.4 when accessed directly
 *
 * @author Keith Godfrey
 */
public class MessageRelay
{

    public static void uiMessageDisplayEntry(MainWindow tf)
	{
		MMx msg = new MMx(tf, MMx.CMD_ACTIVATE_ENTRY);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageDoGotoEntry(MainWindow tf, String str)
	{
		MMx msg = new MMx(tf, MMx.CMD_GOTO_ENTRY, str);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageSetMessageText(MainWindow tf, String str)
	{
		MMx msg = new MMx(tf, MMx.CMD_SET_STATUS, str);
		SwingUtilities.invokeLater(msg);
	}

    public static void uiMessageDisplayError(MainWindow tf, 
					String str, Throwable e)
	{
		MMx msg = new MMx(tf, MMx.CMD_ERROR_MESSAGE, str, e);
		SwingUtilities.invokeLater(msg);
	}
}

