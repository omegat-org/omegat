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

package org.omegat.gui;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.omegat.gui.messages.MessageRelay;
import org.omegat.gui.main.MainWindow;

/**
 * A listener for old Hyperlink-like style
 *
 * @author Keith Godfrey
 */
class HListener implements HyperlinkListener
{
	public HListener(MainWindow t, boolean grabFocus)
	{
		m_transFrame = t;
		m_grabFocus = grabFocus;
	}

	public void hyperlinkUpdate(HyperlinkEvent e)
	{
		String s;
		if (e.getEventType() == 
		HyperlinkEvent.EventType.ACTIVATED)
		{
			s = e.getDescription();
			MessageRelay.uiMessageDoGotoEntry(m_transFrame, s);
			//m_transFrame.doGotoEntry(s);
			if (m_grabFocus)
			{
				m_transFrame.toFront();
			}
		}
	}

	private MainWindow	m_transFrame;
	private boolean	m_grabFocus;
}
