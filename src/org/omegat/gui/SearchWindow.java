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

import org.omegat.core.threads.CommandThread;
import org.omegat.core.threads.SearchThread;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * This is a window that appears when user'd like to search for something.
 * For each new user's request new window is created.
 * Actual search is done by SearchThread.
 *
 * @author Keith Godfrey
 */
public class SearchWindow extends JFrame
{
	public SearchWindow(TransFrame par, SearchThread th, String startText)
	{
		//super(par, false);
		setSize(650, 700);

		m_thread = th;
		m_searchLabel = new JLabel();
		m_searchField = new MFindField();
		if (startText != null)
			m_searchField.setText(startText);
		m_searchButton = new JButton();
		m_searchButton.setMnemonic(KeyEvent.VK_S);
		Box bSearch = Box.createHorizontalBox();
		bSearch.add(m_searchLabel);
		bSearch.add(m_searchField);
		bSearch.add(Box.createHorizontalStrut(10));
		bSearch.add(m_searchButton);

		m_exactSearchRB = new JRadioButton();
		m_exactSearchRB.setMnemonic(KeyEvent.VK_E);
		m_exactSearchRB.setSelected(true);

		m_keywordSearchRB = new JRadioButton();
		m_keywordSearchRB.setMnemonic(KeyEvent.VK_K);
		m_keywordSearchRB.setSelected(false);

		m_tmSearchCB = new JCheckBox();
		m_tmSearchCB.setMnemonic(KeyEvent.VK_T);
		m_tmSearchCB.setSelected(true);
		m_tmSearch = true;

		ButtonGroup bg = new ButtonGroup();
		bg.add(m_exactSearchRB);
		bg.add(m_keywordSearchRB);

		Box bRB = Box.createHorizontalBox();
		bRB.add(m_exactSearchRB);
		bRB.add(Box.createHorizontalStrut(10));
		bRB.add(m_keywordSearchRB);
		bRB.add(Box.createHorizontalStrut(10));
		bRB.add(m_tmSearchCB);

		m_viewer = new EntryListPane(par);
		JScrollPane viewerScroller = new JScrollPane(m_viewer);

		m_dirLabel = new JLabel();
		m_dirField = new JTextField();
		m_dirField.setEditable(false);
		m_dirButton = new JButton();
		Box bDir = Box.createHorizontalBox();
		bDir.add(m_dirLabel);
		bDir.add(m_dirField);
		bDir.add(Box.createHorizontalStrut(10));
		bDir.add(m_dirButton);

		m_dirCB = new JCheckBox();
		m_dirCB.setSelected(false);
		m_dirCB.setActionCommand(OConsts.SW_DIR_CB_CHECKED_CMD);
		m_dirCB.setMnemonic(KeyEvent.VK_F);
		m_recursiveCB = new JCheckBox();
		m_recursiveCB.setSelected(true);
		m_recursiveCB.setMnemonic(KeyEvent.VK_R);
		m_recursiveCB.setEnabled(false);

		m_dismissButton = new JButton();
		m_dismissButton.setMnemonic(KeyEvent.VK_W);

		Box bCB = Box.createHorizontalBox();
		bCB.add(m_dirCB);
		bCB.add(Box.createHorizontalStrut(10));
		bCB.add(m_recursiveCB);
		bCB.add(Box.createHorizontalGlue());
		bCB.add(m_dismissButton);

		//////////////////////////////////////
		// layout container
		Container cp = getContentPane();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		cp.setLayout(gridbag);

		c.insets = new Insets(3, 3, 3, 3);
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weighty = 0.0;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;

		// search controls
		gridbag.setConstraints(bSearch, c);
		cp.add(bSearch);

		// search type 
		gridbag.setConstraints(bRB, c);
		cp.add(bRB);

		// view pane
		c.weighty = 3.0;
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(viewerScroller, c);
		cp.add(viewerScroller);

		// directory controls
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.SOUTHWEST;
		gridbag.setConstraints(bDir, c);
		cp.add(bDir);

		// directory checkboxes
		gridbag.setConstraints(bCB, c);
		cp.add(bCB);

		/////////////////////////////////////
		// action listeners
		m_dismissButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				doCancel();
			}
		});

		m_searchButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				doSearch();
			}
		});

		m_dirButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				doBrowseDirectory();
			}
		});


		// need to control check boxes and radio buttons manually
		//
		// keyword search can only be used when searching current project
		// TM search only works with exact search on current project
		// file search only works with exact search
		//
		// keep track of settings and only show what are valid choices
		
		m_exactSearchRB.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (m_exactSearchRB.isSelected())
				{
					m_tmSearchCB.setEnabled(true);
					if (!m_dirCB.isSelected())
						m_tmSearchCB.setSelected(m_tmSearch);
				}
			}
		});

		m_tmSearchCB.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				m_tmSearch = m_tmSearchCB.isSelected();
			}
		});

		m_dirCB.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (m_dirCB.isSelected())
				{
					m_recursiveCB.setEnabled(true);
					m_dirField.setEditable(true);
					m_dirField.requestFocus();
					m_tmSearchCB.setEnabled(false);
					m_tmSearchCB.setSelected(false);
					m_keywordSearch = m_keywordSearchRB.isSelected();
					m_keywordSearchRB.setSelected(false);
					m_keywordSearchRB.setEnabled(false);
					m_exactSearchRB.setSelected(true);
				}
				else
				{
					m_recursiveCB.setEnabled(false);
					m_dirField.setEditable(false);
					m_tmSearchCB.setEnabled(true);
					m_tmSearchCB.setSelected(m_tmSearch);
					m_keywordSearchRB.setSelected(m_keywordSearch);
					m_keywordSearchRB.setEnabled(true);
					m_exactSearchRB.setSelected(!m_keywordSearch);
					m_exactSearchRB.setSelected(true);
				}
			}
		});

		String searchDir;													// NOI18N
		searchDir = CommandThread.core.getPreference(OConsts.PREF_SEARCH_DIR);
		if (!searchDir.equals(""))										// NOI18N
		{
			m_dirField.setText(searchDir);
		}

		updateUIText();

		m_viewer.setText(OStrings.SW_VIEWER_TEXT);

		if (!par.isProjectLoaded())
		{
			// restrict user to file only access
			m_dirCB.setSelected(true);
			m_dirCB.setEnabled(false);
			m_tmSearchCB.setSelected(false);
			m_tmSearchCB.setEnabled(false);
			m_keywordSearchRB.setEnabled(false);
			m_dirField.setEditable(true);
		}

		m_searchField.requestFocus();
	}

	////////////////////////////////////////////////////////////////
	// interface for displaying text in viewer
	
	public void displayResults()
	{
		m_viewer.finalize();
	}

	public void addEntry(int num, String preamble, String src, String tar)
	{
		m_viewer.addEntry(num, preamble, src, tar);
	}

	public void postMessage(String str)
	{
		m_viewer.addEntry(-1, str, null, null);
	}

	/////////////////////////////////////////////////////////////////
	// misc public functions
	
	// put keyboard focus on search field
	public void setSearchControlFocus()
	{
		m_searchField.requestFocus();
	}

	// called by controlling thread 
	public void threadDied()
	{
		m_thread = null;
	}

	///////////////////////////////////////////////////////////////
	// internal functions
	
	public void processWindowEvent(WindowEvent w)
	{
		int evt = w.getID();
		if (evt == WindowEvent.WINDOW_CLOSING || evt == WindowEvent.WINDOW_CLOSED)
		{
			if (m_thread != null)
				m_thread.haltThread();
		}
		super.processWindowEvent(w);
	}
	
	private void doBrowseDirectory()
	{
		JFileChooser browser = new JFileChooser();
		String str = OStrings.SW_BUTTON_SELECT;
		browser.setApproveButtonText(str);
		browser.setDialogTitle(OStrings.SW_TITLE);
		browser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		String curDir = m_dirField.getText();
		
		if (!curDir.equals(""))											// NOI18N
		{
			File dir = new File(curDir);
			if (dir.exists() && dir.isDirectory())
			{
				browser.setCurrentDirectory(dir);
			}
		}

		browser.showOpenDialog(this);
		File dir = browser.getSelectedFile();
		if (dir == null)
			return;

		str = dir.getAbsolutePath() + File.separator;
		m_dirField.setText(str);
	}

	private void doSearch()
	{
		if (m_thread == null)
			doCancel();
		else
		{
			m_viewer.reset();
			String root = null;
			if (m_dirCB.isSelected())
			{
				// make sure it's a valid directory name
				root = m_dirField.getText();
				if (!root.endsWith(File.separator))
					root += File.separator;
				File f = new File(root);
				if (!f.exists() || !f.isDirectory())
				{
					// TODO XXX alert user folder is invalid
System.out.println("ERROR - bad directory name  '"+m_dirField.getText()+"'");	// NOI18N
					return;
				}
				if (CommandThread.core != null && m_dirCB.isSelected())
				{
					CommandThread.core.setPreference(
								OConsts.PREF_SEARCH_DIR, root);
					// need to explicitly save preferences because project
					//	might not be open
					CommandThread.core.savePreferences();
				}
			}
			m_thread.requestSearch(m_searchField.getText(), root, 
					m_recursiveCB.isSelected(), 
					m_exactSearchRB.isSelected(), 
					m_tmSearchCB.isSelected(),
                    m_keywordSearchRB.isSelected());
		}
	}

	private void doCancel()
	{
		dispose();
	}

	private void updateUIText()
	{
		setTitle(OStrings.SW_TITLE);
		
		m_searchLabel.setText(OStrings.SW_SEARCH_TEXT);
		m_searchButton.setText(OStrings.SW_SEARCH);
		
		m_exactSearchRB.setText(OStrings.SW_EXACT_SEARCH);
		m_tmSearchCB.setText(OStrings.SW_SEARCH_TM);
		m_keywordSearchRB.setText(OStrings.SW_WORD_SEARCH);
		
		m_dirLabel.setText(OStrings.SW_LOCATION);
		m_dirCB.setText(OStrings.SW_DIR_SEARCH);
		m_recursiveCB.setText(OStrings.SW_DIR_RECURSIVE);
		m_dirButton.setText(OStrings.SW_BROWSE);
		
		m_dismissButton.setText(OStrings.CF_BUTTON_CLOSE);
	}

	class MFindField extends JTextField
	{
		protected void processKeyEvent(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_ENTER &&
                    e.getID() == KeyEvent.KEY_PRESSED)
			{
				if (!getText().equals(""))								// NOI18N
					doSearch();
			}
			else
			{
				super.processKeyEvent(e);
			}
		}
	}

	private JLabel		m_searchLabel;
	private JTextField	m_searchField;
	private JButton		m_searchButton;

    private JRadioButton	m_keywordSearchRB;
	private JRadioButton	m_exactSearchRB;
	private JCheckBox		m_tmSearchCB;

	private boolean		m_tmSearch = true;
	private boolean		m_keywordSearch;

	private JLabel		m_dirLabel;
	private JTextField	m_dirField;
	private JButton		m_dirButton;
	private JCheckBox	m_dirCB;
	private JCheckBox	m_recursiveCB;

	private JButton		m_dismissButton;

	private EntryListPane	m_viewer;

	private SearchThread	m_thread;

}
