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

import org.omegat.gui.main.MainWindow;

/**
 * class to pass messages
 *
 * @author Keith Godfrey
 */
class MMx implements Runnable
{
	private String	m_msg = "";	// NOI18N
	private int		m_cmdNum;
	private Throwable	m_throw;
	private MainWindow m_tf;

	public MMx(MainWindow tf, int cmd)
	{
		m_cmdNum = 0;
		m_tf = tf;

		// check for errors 
		if (cmd >= 1 && cmd <= 5 || cmd == 10 || cmd == 11)
			m_cmdNum = cmd;
	}

	public MMx(MainWindow tf, int cmd, String msg)
	{
		m_cmdNum = 0;
		m_tf = tf;

		// check for errors 
		if (cmd == 6 || cmd == 7)
		{
			m_cmdNum = cmd;
			m_msg = msg;
		}
	}

	public MMx(MainWindow tf, String msg, Throwable e)
	{
		m_cmdNum = 0;
		m_tf = tf;
        int cmd = 9;

		// check for errors
		m_cmdNum = cmd;
		m_msg = msg;
		m_throw = e;
	}

	public void run()
	{
		switch (m_cmdNum)
		{
			case 2:
				m_tf.doNextEntry();
				break;
			case 3:
				m_tf.doPrevEntry();
				break;
			case 4:
				m_tf.doRecycleTrans();
				break;
			case 5:
				m_tf.activateEntry();
				break;
			case 6:
				m_tf.doGotoEntry(m_msg);
				break;
			case 7:
				m_tf.setMessageText(m_msg);
				break;
			case 8:
				m_tf.displayWarning(m_msg, m_throw);
				break;
			case 9:
				m_tf.displayError(m_msg, m_throw);
				break;
			case 10:
				m_tf.updateFuzzyInfo(0);
				break;
			default:
				// do nothing
				break;
		}
    }
}

