//-------------------------------------------------------------------------
//  
//  MatchWindow.java - 
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
//  Build date:  8Mar2003
//  Copyright (C) 2002, Keith Godfrey
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
//import javax.swing.filechooser.*;
import java.io.*;
import java.lang.*;
import java.text.ParseException;

// creates a dialog where project properties are entered and/or modified
class MatchWindow extends JFrame
{
	public MatchWindow()
	{
		//super(par, false);
		setSize(350, 720);
		setLocation(660, 0);

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
	}

	// copy match and glos buffers to display
	public void updateText()
	{
		m_matchPane.setText(m_matchDisplay);
		m_glosPane.setText(m_glosDisplay);
		m_matchDisplay = "";
		m_glosDisplay = "";
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


	protected String		m_matchDisplay = "";
	protected String		m_glosDisplay = "";
	protected JTextPane		m_matchPane;
	protected JTextPane		m_glosPane;
	protected int			m_matchCount = 0;

//////////////////////////////////////////////////////////

	public static void main(String[] s)
	{
		MatchWindow mw = new MatchWindow();
		mw.addGlosTerm("OmegaT", "Marc Prior", "");
		mw.addGlosTerm("Trados", "Evil corporate tool", "not truly evil, " +
				"but a tool from a very greedy company");
		mw.addMatchTerm("For whom the bell tolls", "Whence the bell tolls",
				65, "unknown");
		mw.updateText();
		mw.show();
	}
}
