//-------------------------------------------------------------------------
//  
//  HelpFrame.java - 
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import javax.swing.event.*;
import java.net.*;

class HelpFrame extends JFrame 
{
	public HelpFrame()
	{
		String str;

		m_historyList = new ArrayList();
		
		Container cp = getContentPane();
		m_helpPane = new JEditorPane();
		m_helpPane.setEditable(false);
		m_helpPane.setContentType("text/html");
		JScrollPane scroller = new JScrollPane(m_helpPane);
		cp.add(scroller, "Center");

		m_homeButton = new JButton();
		m_homeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				displayFile(OConsts.HELP_HOME);
			}
		});

		m_backButton = new JButton();
		m_backButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (m_historyList.size() > 0)
				{
					String s = (String) m_historyList.remove(
							m_historyList.size()-1);
					displayFile(s);
				}
				if (m_historyList.size() == 0)
				{
					m_backButton.setEnabled(false);
				}
			}
		});
		m_backButton.setEnabled(false);
		
		m_closeButton = new JButton();
		m_closeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		Container cont = new Container();
		Box bbut = Box.createHorizontalBox();
		bbut.add(m_backButton);
		bbut.add(Box.createHorizontalStrut(10));
		bbut.add(m_homeButton);
		bbut.add(Box.createHorizontalGlue());
		bbut.add(m_closeButton);
		cp.add(bbut, "North");

		setSize(600, 500);
		m_helpPane.addHyperlinkListener(new HyperlinkListener()
		{
			public void hyperlinkUpdate(HyperlinkEvent he)
			{
				if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
				{
					m_historyList.add(m_filename);
					displayFile(he.getDescription());
					m_backButton.setEnabled(true);
				}
			}
		});

		updateUIText();
		displayFile(OConsts.HELP_HOME);
	}

	public void displayFile(String file)
	{
		//m_helpPane.setText("");
		String fullname = "file:" + System.getProperty("user.dir") + 
			File.separator + OConsts.HELP_DIR + File.separator + file;
		try
		{
			URL page = new URL(fullname);
			m_helpPane.setPage(page);
			m_filename = file;
		}
		catch (IOException e)
		{
			String s = errorHaiku() + "<p><p>" + OStrings.HF_CANT_FIND_HELP +
				fullname;

			m_helpPane.setText(s);
		}
	}

	// immortalize the BeOS 404 messages (some modified a bit for context)
	protected String errorHaiku()
	{
		int i = (int ) (Math.random() * 10);
		String s = "";
		switch (i)
		{
			case 0:
				s+="Exciting help page<p>"+
					"Gossamer threads hold you back<p>" +
					"404 not found";
				break;
			case 1:
				s+="Wind catches lily<p>"+
					"Scatt'ring petals to the wind:<p>" +
					"Your page is not found";
				break;
			case 2:
				s+="Bartender yells loud:<p>"+
					"Your page cannot be found, boy<p>"+
					"Buy another drink.";
				break;
			case 3:
				s+="Page slips through fingers<p>" +
					"Pulse pounding hard and frantic<p>" +
					"Vanishing like mist.";
				break;
			case 4:
				s+="These three are certain:<p>"+
					"Death, taxes, and page not found.<p>"+
					"You, victim of one.";
				break;
			case 5:
				s+="The help page you seek<p>"+
					"Lies beyond our perception<p>" +
					"But others await";
				break;
			case 6:
				s+="Emptiness of soul<p>"+
					"Forever aching blackness<p>"+
					"Your help page not found";
				break;
			case 7:
				s+="Ephemeral page<p>"+
					"I am the Blue Screen of Death<p>" +
					"No one hears your screams.";
				break;
			case 8:
				s+="Rather than beep<p>"+
					"Or a rude error message<p>" +
					"These words: 'Page not found'";
				break;
			case 9:
				s+="Mourning and sorrow<p>"+
					"404 not with us now<p>"+
					"Lost to paradise";
				break;
			default:
				s += "Help you are seeking<p>" +
					"From your path it is fleeing<p>" +
					"Its winter has come";
				break;
		};

		return s;
	}
	
	public void updateUIText()
	{
		m_closeButton.setText(OStrings.HF_BUTTON_CLOSE);
		m_homeButton.setText(OStrings.HF_BUTTON_HOME);
		m_backButton.setText(OStrings.HF_BUTTON_BACK);
		setTitle(OStrings.HF_WINDOW_TITLE);
	}

	private JEditorPane m_helpPane;
	private JButton		m_closeButton;
	private JButton		m_homeButton;
	private JButton		m_backButton;
	private ArrayList	m_historyList;

	protected String	m_filename = "";

///////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		JFrame f = new HelpFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.show();
	}
}

