/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/omegat/omegat.html
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

import org.omegat.core.Core;
import org.omegat.gui.main.MainWindow;

/**
 * class to pass messages
 *
 * @author Keith Godfrey
 */
class MMx implements Runnable
{
    /** Tells OmegaT to activate the entry -- project is loaded OK. */
    public static final int CMD_ACTIVATE_ENTRY = 5;
    /** Tells OmegaT to display a certain entry. */
    public static final int CMD_GOTO_ENTRY = 6;
    /** Allows to set status. */
    public static final int CMD_SET_STATUS = 7;
    /** Displays an error message. */
    public static final int CMD_ERROR_MESSAGE = 9;
    
    
	private String      m_msg;
	private int         m_cmdNum;
	private Throwable   m_throw;
	private MainWindow  m_tf;

	public MMx(MainWindow tf, int cmd)
	{
		m_tf = tf;
        m_cmdNum = cmd;
	}

	public MMx(MainWindow tf, int cmd, String msg)
	{
		m_tf = tf;
        m_cmdNum = cmd;
        m_msg = msg;
	}

	public MMx(MainWindow tf, int cmd, String msg, Throwable e)
	{
		m_tf = tf;
		m_cmdNum = cmd;
		m_msg = msg;
		m_throw = e;
	}

	public void run()
	{
		switch (m_cmdNum)
		{
			case CMD_ACTIVATE_ENTRY:
				m_tf.activateEntry();
				break;
			case CMD_GOTO_ENTRY:
				m_tf.doGotoEntry(m_msg);
				break;
			case CMD_SET_STATUS:
			        Core.getMainWindow().showStatusMessage(m_msg);
				break;
			case CMD_ERROR_MESSAGE:
				m_tf.displayError(m_msg, m_throw);
				break;
		}
    }
}

