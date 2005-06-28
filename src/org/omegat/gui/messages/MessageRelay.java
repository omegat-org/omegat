/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039

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

import org.omegat.gui.main.MainInterface;

/**
 * Methods to send messages to UI objects
 * use indirect methods because Swing objects tend to lockup in
 * java 1.4 when accessed directly
 *
 * @author Keith Godfrey
 */
public class MessageRelay
{

    public static void uiMessageDisplayEntry(MainInterface tf)
	{
		MMx msg = new MMx(tf, 5);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageFuzzyInfo(MainInterface tf)
	{
		MMx msg = new MMx(tf, 10);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageDoGotoEntry(MainInterface tf, String str)
	{
		MMx msg = new MMx(tf, 6, str);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageSetMessageText(MainInterface tf, String str)
	{
		MMx msg = new MMx(tf, 7, str);
		SwingUtilities.invokeLater(msg);
	}

    public static void uiMessageDisplayError(MainInterface tf, 
					String str, Throwable e)
	{
		MMx msg = new MMx(tf, str, e);
		SwingUtilities.invokeLater(msg);
	}

	// command codes
	//		2		doNextEntry
	//		3		doPrevEntry
	//		4		doRecycleTrans
	//		5		displayEntry(bool)
	//		6		doGotoEntry(str)
	//		7		setMessageText(str)
	//		8		displayWarning(str, throwable)
	//		9		displayError(str, throwable)
	//		10		display fuzzy info
}

