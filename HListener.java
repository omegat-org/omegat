//-------------------------------------------------------------------------
//  
//  HListener.java - 
//  
//  Copyright (C) 2002, Keith Godfrey
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
//  Build date:  21Dec2002
//  Copyright (C) 2002, Keith Godfrey
//  aurora@coastside.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.util.*;
import javax.swing.event.*;

class HListener implements HyperlinkListener
{
	public HListener(TransFrame t)
	{
		m_transFrame = t;
	}

	public void hyperlinkUpdate(HyperlinkEvent e)
	{
		String s;
		if (e.getEventType() == 
		HyperlinkEvent.EventType.ACTIVATED)
		{
			s = e.getDescription();
			CommandThread.uiMessageDoGotoEntry(m_transFrame, s);
			//m_transFrame.doGotoEntry(s);
		}
	}

	private TransFrame	m_transFrame;
}

