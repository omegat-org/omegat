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

import org.omegat.util.OStrings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * A frame for project
 *
 * @author Keith Godfrey
 */
public class ProjectFrame extends JFrame
{
	public ProjectFrame(TransFrame parent)
	{
		m_parent = parent;

		m_nameList = new ArrayList(256);
		m_offsetList = new ArrayList(256);
		
		Container cp = getContentPane();
		m_editorPane = new JEditorPane();
		m_editorPane.setEditable(false);
		m_editorPane.setContentType("text/html");								// NOI18N
		JScrollPane scroller = new JScrollPane(m_editorPane);
		cp.add(scroller, "Center");												// NOI18N

		m_closeButton = new JButton();
		m_closeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}
		});

		Box bbut = Box.createHorizontalBox();
		bbut.add(Box.createHorizontalGlue());
		bbut.add(m_closeButton);
		bbut.add(Box.createHorizontalGlue());
		cp.add(bbut, "South");													// NOI18N

		setSize(500, 400);
		m_editorPane.addHyperlinkListener(new HListener(m_parent, true));

		updateUIText();
	}

	public void reset()
	{
		m_nameList.clear();
		m_offsetList.clear();
		m_editorPane.setText("");												// NOI18N
	}
	
	private void updateUIText()
	{
		m_closeButton.setText(OStrings.PF_BUTTON_CLOSE);
		setTitle(OStrings.PF_WINDOW_TITLE);

		buildDisplay();
	}

    public void addFile(String name, int entryNum)
	{
		m_nameList.add(name);
		m_offsetList.add(new Integer(entryNum));
	}

	public void setNumEntries(int num)
	{
		m_maxEntries = num;
		m_ready = true;
	}

	public /*synchronized*/ void buildDisplay()
	{
		if (!m_ready)
			return;

		int i;
		Integer inum;
		int num = 0;
		int next;
		String name;
		if (m_nameList.size() <= 0)
			return;

		String output = "<table BORDER COLS=2 WIDTH=\"100%\" NOSAVE>";			// NOI18N
		output += "<tr><td>" + OStrings.PF_FILENAME + "</td><td>" +				// NOI18N
					OStrings.PF_NUM_SEGMENTS + "</td></tr>";					// NOI18N
		for (i=0; i<m_nameList.size(); i++)
		{
			name = (String) m_nameList.get(i);
			if (i < m_nameList.size() - 1)
			{
				inum = (Integer) m_offsetList.get(i + 1);
				next = inum.intValue();
			}
			else
				next = m_maxEntries;

			output += "<tr><td><a href=\"" + (num+1) + "\">" + name +			// NOI18N
						"</a></td><td>" + (next - num) + "</td></tr>";			// NOI18N
			num = next;
		}
		output += "</table>";													// NOI18N
		m_editorPane.setText(output);
	}
	
	private JEditorPane m_editorPane;
	private JButton		m_closeButton;
	private ArrayList	m_nameList;
	private ArrayList	m_offsetList;

	private int			m_maxEntries;
	private boolean		m_ready;

	private TransFrame m_parent;

}

