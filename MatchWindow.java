//-------------------------------------------------------------------------
//  
//  MatchWindow.java - 
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

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
//import javax.swing.filechooser.*;
import java.io.*;
import java.lang.*;
import java.text.ParseException;

// creates a dialog where project properties are entered and/or modified
class MatchWindow extends JFrame
{
	public MatchWindow()
	{
		// KBG - set screen size based on saved values or guesses about
		//		screen size (18may04)
		initScreenLayout();

		Container cont = getContentPane();
		cont.setLayout(new GridLayout(2, 1, 3, 4));

		m_matchPane = new JTextPane();
		m_glosPane = new JTextPane();
		JScrollPane matchScroller = new JScrollPane(m_matchPane);
		JScrollPane glosScroller = new JScrollPane(m_glosPane);

		cont.add(matchScroller);
		cont.add(glosScroller);

		m_matchPane.setEditable(false);
		m_glosPane.setEditable(false);

		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				hide();
			}
		});
	}

	public JTextPane getMatchPane()		{ return m_matchPane;	}
	// copy match and glos buffers to display
	public void updateGlossaryText()
	{
		m_glosPane.setText(m_glosDisplay);
		m_glosDisplay = "";
	}

	public void formatNearText(String text, byte[] attrList, Color uniqColor)
	{
		if (text == null)
			return;

		int start;
		int end;
		int len = text.length();
		JTextPane pane = m_matchPane;

		// reset color of text to default value
		ArrayList tokenList = new ArrayList();
		StaticUtils.tokenizeText(text, tokenList);
		int numTokens = tokenList.size();
		for (int i=0; i<numTokens; i++)
		{
			if (i == (numTokens-1))
				end = m_hiliteStart + len + 4;
			else
				end = m_hiliteStart + ((Token) tokenList.get(i+1)).offset + 4;
			start = m_hiliteStart + ((Token) tokenList.get(i)).offset + 4;

			pane.select(start, end);
			SimpleAttributeSet mattr = new SimpleAttributeSet();
			if ((attrList[i] & StringData.UNIQ) != 0)
			{
				StyleConstants.setForeground(mattr, uniqColor);
			}
			else if ((attrList[i] & StringData.PAIR) != 0)
			{
				StyleConstants.setForeground(mattr, Color.green);
			}
			pane.setCharacterAttributes(mattr, false);
		}
		pane.select(0, 0);
		SimpleAttributeSet mattr = new SimpleAttributeSet();
		pane.setCharacterAttributes(mattr, false);
	}

	public void updateMatchText()
	{
		// Cancelling all the attributes
		m_matchPane.setCharacterAttributes(new SimpleAttributeSet(), true);
		
		m_matchPane.setText(m_matchDisplay);

		if (m_hiliteStart >= 0)
		{
			m_matchPane.select(m_hiliteStart, m_hiliteEnd);
			MutableAttributeSet mattr = null;
			mattr = new SimpleAttributeSet();
			StyleConstants.setBold(mattr, true);
			m_matchPane.setCharacterAttributes(mattr, false);

			m_matchPane.setCaretPosition(m_hiliteStart);
		}

		m_matchDisplay = "";
		m_matchCount = 0;
	}

	public void reset()
	{
		m_matchDisplay = "";
		m_glosDisplay = "";
		m_matchCount = 0;
		m_matchPane.setText("");
		m_glosPane.setText("");
	}

	public void setFont(Font f)
	{
		m_matchPane.setFont(f);
		m_glosPane.setFont(f);
	}

	public void addGlosTerm(String src, String loc, String comments)
	{
		String glos = "'" + src + "'  =  '" + loc + "'\n";
		if (comments.length() > 0)
			glos += comments + "\n\n";
		else
			glos += "\n";
		m_glosDisplay += glos;
	}

	// returns offset in display of the start of this term
	public int addMatchTerm(String src, String loc, int score, String proj)
	{
		String entry = ++m_matchCount + ")  " + src + "\n" + loc + "\n< " +
					score + "% " + proj + " >\n\n";
		int size = m_matchDisplay.length();
		m_matchDisplay += entry;
		return size;
	}

	public void hiliteRange(int start, int end)
	{
		int len = m_matchDisplay.length();
		if ((start < 0) || (start > len))
		{
			m_hiliteStart = -1;
			m_hiliteEnd = -1;
			return;
		}

		if (end < 0)
			end = len;

		m_hiliteStart = start;
		m_hiliteEnd = end;
	}

	public void storeScreenLayout()
	{
		int w = getWidth();
		int h = getHeight();
		int x = getX();
		int y = getY();
		PreferenceManager.pref.setPreference(OConsts.PREF_MATCH_W, "" + w);
		PreferenceManager.pref.setPreference(OConsts.PREF_MATCH_H, "" + h);
		PreferenceManager.pref.setPreference(OConsts.PREF_MATCH_X, "" + x);
		PreferenceManager.pref.setPreference(OConsts.PREF_MATCH_Y, "" + y);
	}
	
	protected void initScreenLayout()
	{
		// KBG - assume screen size is 800x600 if width less than 900, and
		//		1024x768 if larger.  assume task bar at bottom of screen.
		//		if screen size saved, recover that and use instead
		//	(18may04)
		String dw, dh, dx, dy;
		dw = PreferenceManager.pref.getPreference(OConsts.PREF_MATCH_W);
		dh = PreferenceManager.pref.getPreference(OConsts.PREF_MATCH_H);
		dx = PreferenceManager.pref.getPreference(OConsts.PREF_MATCH_X);
		dy = PreferenceManager.pref.getPreference(OConsts.PREF_MATCH_Y);
		int x=0;
		int y=0;
		int w=0;
		int h=0;
		boolean badSize = false;
		if ((dw == null) || (dw.equals(""))	|| (dh == null)			|| 
				(dh.equals(""))	|| (dx == null) || (dx.equals(""))	||
				(dy == null) || (dy.equals("")))
		{
			badSize = true;
		}
		else
		{
			try 
			{
				x = Integer.parseInt(dx);
				y = Integer.parseInt(dy);
				w = Integer.parseInt(dw);
				h = Integer.parseInt(dh);
			}
			catch (NumberFormatException nfe)
			{
				badSize = true;
			}
		}
		if (badSize)
		{
			// size info missing - put window in default position
			GraphicsEnvironment env = 
					GraphicsEnvironment.getLocalGraphicsEnvironment();
			Rectangle scrSize = env.getMaximumWindowBounds();
			Point center = env.getCenterPoint();
			if (scrSize.width < 900)
			{
				// assume 800x600
				setSize(200, 536);
				setLocation(590, 0);
			}
			else
			{
				// assume 1024x768 or larger
				setSize(320, 700);
				setLocation(680, 0);
			}
		}
		else
		{
			setSize(w, h);
			setLocation(x, y);
		}
	}

	protected String		m_matchDisplay = "";
	protected String		m_glosDisplay = "";
	protected JTextPane		m_matchPane;
	protected JTextPane		m_glosPane;
	protected int			m_matchCount = 0;
	protected int			m_hiliteStart = -1;
	protected int			m_hiliteEnd = -1;

//////////////////////////////////////////////////////////

	public static void main(String[] s)
	{
		MatchWindow mw = new MatchWindow();
		mw.addGlosTerm("OmegaT", "Marc Prior", "");
		mw.addGlosTerm("Trados", "Evil corporate tool", "not truly evil, " +
				"but a tool from a very greedy company");
		mw.addMatchTerm("For whom the bell tolls", "Whence the bell tolls",
				65, "unknown");
		mw.updateGlossaryText();
		mw.updateMatchText();
		mw.show();
	}
}
