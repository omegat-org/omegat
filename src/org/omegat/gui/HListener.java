/*
 * HListener.java
 *
 * Created on 8 Август 2004 г., 19:48
 */

package org.omegat.gui;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.omegat.gui.messages.MessageRelay;

/**
 * A listener for old Hyperlink-like style
 *
 * @author Keith Godfrey
 */
public class HListener implements HyperlinkListener
{
	public HListener(TransFrame t, boolean grabFocus)
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
			if (m_grabFocus == true)
			{
				m_transFrame.toFront();
			}
		}
	}

	private TransFrame	m_transFrame;
	protected boolean	m_grabFocus;
}
