//-------------------------------------------------------------------------
//  
//  MessageRelay.java - 
//  
//  Copyright (C) 2004, Keith Godfrey
//  
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//  
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//  
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//  
//  Copyright (C) 2004, Keith Godfrey, et al
//  keithgodfrey@users.sourceforge.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import javax.swing.*;

///////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////
// methods to send messages to UI objects
// use indirect methods because Swing objects tend to lockup in
//	java 1.4 when accessed directly

//static class MessageRelay implements Runnable
class MessageRelay //implements Runnable
{
	public static void uiMessageDoPseudoTrans(TransFrame tf)
	{
		MMx msg = new MMx(tf, 1);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageDoNextEntry(TransFrame tf)
	{
		MMx msg = new MMx(tf, 2);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageDoPrevEntry(TransFrame tf)
	{
		MMx msg = new MMx(tf, 3);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageDoRecycleTrans(TransFrame tf)
	{
		MMx msg = new MMx(tf, 4);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageDisplayEntry(TransFrame tf)
	{
		MMx msg = new MMx(tf, 5);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageFuzzyInfo(TransFrame tf)
	{
		MMx msg = new MMx(tf, 10);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageDoGotoEntry(TransFrame tf, String str)
	{
		MMx msg = new MMx(tf, 6, str);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageSetMessageText(TransFrame tf, String str)
	{
		MMx msg = new MMx(tf, 7, str);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageDisplayWarning(TransFrame tf, 
					String str, Throwable e)
	{
		MMx msg = new MMx(tf, 8, str, e);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageDisplayError(TransFrame tf, 
					String str, Throwable e)
	{
		MMx msg = new MMx(tf, 9, str, e);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageUnloadProject(TransFrame tf) 
	{
		MMx msg = new MMx(tf, 11);
		SwingUtilities.invokeLater(msg);
	}

	// command codes
	//		1		doPseudoTrans
	//		2		doNextEntry
	//		3		doPrevEntry
	//		4		doRecycleTrans
	//		5		displayEntry(bool)
	//		6		doGotoEntry(str)
	//		7		setMessageText(str)
	//		8		displayWarning(str, throwable)
	//		9		displayError(str, throwable)
	//		10		display fuzzy info
	//		11		unload project
}

// class to pass messages
class MMx implements Runnable
{
	protected String	m_msg = "";
	protected int		m_cmdNum = 0;
	protected Throwable	m_throw = null;
	protected TransFrame	m_tf = null;

	public MMx(TransFrame tf, int cmd)
	{
		m_cmdNum = 0;
		m_tf = tf;

		// check for errors 
		if (((cmd >= 1) && (cmd <= 5)) || (cmd == 10) || (cmd == 11))
			m_cmdNum = cmd;
	}

	public MMx(TransFrame tf, int cmd, String msg)
	{
		m_cmdNum = 0;
		m_tf = tf;

		// check for errors 
		if ((cmd == 6) || (cmd == 7))
		{
			m_cmdNum = cmd;
			m_msg = msg;
		}
	}

	public MMx(TransFrame tf, int cmd, String msg, Throwable e)
	{
		m_cmdNum = 0;
		m_tf = tf;

		// check for errors 
		if ((cmd == 8) || (cmd == 9))
		{
			m_cmdNum = cmd;
			m_msg = msg;
			m_throw = e;
		}
	}

	public void run()
	{
		switch (m_cmdNum)
		{
			case 1:
				m_tf.doPseudoTrans();
				break;
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
				m_tf.doCompareN(0);
				break;
			case 11:
				m_tf.doUnloadProject();
				break;
			default:
				// do nothing
				break;
		};
	}
};


