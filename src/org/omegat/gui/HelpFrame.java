/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2004  Keith Godfrey et al
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

package org.omegat.gui;

import org.omegat.util.OConsts;
import org.omegat.util.OStrings;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Frame that displays help HTML files.
 * Singleton.
 *
 * @author Keith Godfrey
 * @author Sandra Jean Chua - sachachua at users.sourceforge.net
 * @autor Maxym Mykhalchuk
 */
public class HelpFrame extends JFrame
{
	/* 
	 * The Singleton design pattern allows us to have just one
	 * instance of the help frame at all times. In order to use
	 * this pattern, we need to prevent other classes from calling
	 * HelpFrame's constructor. To get a reference to the help frame,
	 * classes should call the static getInstance() method.
	 */
	private static HelpFrame singleton;

	/** Creates the Help Frame */
	private HelpFrame()
	{
		language = detectLanguage();
		
		m_historyList = new ArrayList();
		
		Container cp = getContentPane();
		m_helpPane = new JEditorPane();
		m_helpPane.setEditable(false);
		m_helpPane.setContentType("text/html"); // NOI18N
		JScrollPane scroller = new JScrollPane(m_helpPane);
		cp.add(scroller, "Center"); // NOI18N

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
		Box bbut = Box.createHorizontalBox();
		bbut.add(m_backButton);
		bbut.add(Box.createHorizontalStrut(10));
		bbut.add(m_homeButton);
		bbut.add(Box.createHorizontalGlue());
		bbut.add(m_closeButton);
		cp.add(bbut, "North"); // NOI18N

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
	
	/**
	 * Gets the only instance of Help Frame
	 */
	public static HelpFrame getInstance()
	{
		if (singleton == null)
		{
			singleton = new HelpFrame();
		}
		return singleton;
	}

	private void displayFile(String file)
	{
		String fullname = absolutePath(file);
		try
		{
			URL page = new URL(fullname);
			m_helpPane.setPage(page);
			m_filename = file;
		}
		catch (IOException e)
		{
			String s = errorHaiku() + "<p><p>" + OStrings.HF_CANT_FIND_HELP +  // NOI18N
				fullname;

			m_helpPane.setText(s);
		}
	}

	// immortalize the BeOS 404 messages (some modified a bit for context)
    private String errorHaiku()
	{
		String s;
		switch( (int) (Math.random() * 11) )
		{
			case 0:
				s=OStrings.HF_HAIKU_1;
				break;
			case 1:
				s=OStrings.HF_HAIKU_2;
				break;
			case 2:
				s=OStrings.HF_HAIKU_3;
				break;
			case 3:
				s=OStrings.HF_HAIKU_4;
				break;
			case 4:
				s=OStrings.HF_HAIKU_5;
				break;
			case 5:
				s=OStrings.HF_HAIKU_6;
				break;
			case 6:
				s=OStrings.HF_HAIKU_7;
				break;
			case 7:
				s=OStrings.HF_HAIKU_8;
				break;
			case 8:
				s=OStrings.HF_HAIKU_9;
				break;
			case 9:
				s=OStrings.HF_HAIKU_10;
				break;
			case 10:
			default:
				s=OStrings.HF_HAIKU_11;
				break;
		}

        return s;
	}
	
	private void updateUIText()
	{
		m_closeButton.setText(OStrings.HF_BUTTON_CLOSE);
		m_homeButton.setText(OStrings.HF_BUTTON_HOME);
		m_backButton.setText(OStrings.HF_BUTTON_BACK);
		setTitle(OStrings.HF_WINDOW_TITLE);
	}
	
	private String absolutePath(String file)
	{
		return "file:" + System.getProperty("user.dir")				// NOI18N
				+ File.separator + OConsts.HELP_DIR + File.separator 
				+ language + File.separator + file;
	}
	
	private String detectLanguage()
	{
		String lang = System.getProperty("user.language", "en");
		File docsFolder = new File(System.getProperty("user.dir")				// NOI18N
				+ File.separator + OConsts.HELP_DIR + File.separator + lang);
		if( docsFolder.exists() )
			return lang;
		else
			return "en";
	}

	private JEditorPane m_helpPane;
	private JButton		m_closeButton;
	private JButton		m_homeButton;
	private JButton		m_backButton;
	private ArrayList	m_historyList;

	private String	m_filename = ""; // NOI18N

	/** The language of the help files, English by default */
	private String language;
}

