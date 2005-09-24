/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.openide.awt.Mnemonics;
import org.omegat.gui.main.MainInterface;
import org.omegat.util.OStrings;

/**
 * A frame for project,
 * showing all the files of the project.
 *
 * @author Keith Godfrey
 */
public class ProjectFrame extends JFrame
{
	public ProjectFrame(MainInterface parent)
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

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-500)/2, (screenSize.height-500)/2, 500, 400);
        
		m_editorPane.addHyperlinkListener(new HListener(m_parent, true));
        
        //  Handle escape key to close the window
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(escape, "ESCAPE");                                          // NOI18N
        getRootPane().getActionMap().put("ESCAPE", escapeAction);               // NOI18N

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
		Mnemonics.setLocalizedText(m_closeButton, OStrings.PF_BUTTON_CLOSE);
		setTitle(OStrings.PF_WINDOW_TITLE);
	}

    public void addFile(String name, int entryNum)
	{
		m_nameList.add(name);
		m_offsetList.add(new Integer(entryNum));
	}

    /**
     * Sets the number of translated segments to display.
     */
	public void setNumberofTranslatedSegments(int value)
	{
		numberofTranslatedSegments = value;
	}
    
    /**
     * Builds the table which lists all the project files.
     */
	public void buildDisplay()
	{
		if( m_nameList==null || m_offsetList==null || m_nameList.size()==0 )
			return;

		StringBuffer output = new StringBuffer();
        output.append("<table align=center width=95% border=0>\n");             // NOI18N
		output.append("<tr>\n");                                                // NOI18N
        output.append("<th width=80% align=center>");                           // NOI18N
        output.append(OStrings.PF_FILENAME);                                    // NOI18N
        output.append("</th>\n");                                               // NOI18N
        output.append("<th width=20% align=center>");                           // NOI18N
        output.append(OStrings.PF_NUM_SEGMENTS);                                // NOI18N
        output.append("</th>\n");                                               // NOI18N
        output.append("</tr>\n");                                               // NOI18N
        int firstEntry = 1;
        int entriesUpToNow = 0;
		for (int i=0; i<m_nameList.size(); i++)
		{
			String name = (String) m_nameList.get(i);
            entriesUpToNow = ((Integer)m_offsetList.get(i)).intValue();
            int size = 1+entriesUpToNow-firstEntry;

			output.append("<tr>\n");                                            // NOI18N
            output.append("<td width=80%>");                                    // NOI18N
            output.append("<a href=\""+firstEntry+"\">"+name+"</a>");           // NOI18N
            output.append("</td>\n");                                           // NOI18N
            output.append("<td width=20% align=center>");                       // NOI18N
            output.append(size);                                                // NOI18N
            output.append("</td>\n");                                           // NOI18N
            output.append("</tr>\n");                                           // NOI18N
            
			firstEntry = entriesUpToNow+1;
		}
        
        output.append("<tr>\n");                                                // NOI18N
        output.append("<td width=80%><b>");                                     // NOI18N
        output.append(OStrings.getString("GUI_PROJECT_Total_number_of_segments"));
        output.append("</b></td>\n");                                           // NOI18N
        output.append("<td width=20% align=center><b>");                        // NOI18N
        output.append(entriesUpToNow);                                          // NOI18N
        output.append("</b></td>\n");                                           // NOI18N
        output.append("</tr>\n");                                               // NOI18N
        output.append("<tr>\n");                                                // NOI18N
        output.append("<td width=80%><b>");                                     // NOI18N
        output.append(OStrings.getString("GUI_PROJECT_Of_which_translated_segments"));
        output.append("</b></td>\n");                                           // NOI18N
        output.append("<td width=20% align=center><b>");                        // NOI18N
        output.append(numberofTranslatedSegments);                              // NOI18N
        output.append("</b></td>\n");                                           // NOI18N
        output.append("</tr>\n");                                               // NOI18N
        
		output.append("</table>\n");                                            // NOI18N
        
		m_editorPane.setText(output.toString());
	}
	
	private JEditorPane m_editorPane;
	private JButton		m_closeButton;
	private ArrayList	m_nameList;
	private ArrayList	m_offsetList;

	private int			numberofTranslatedSegments;

	private MainInterface m_parent;

}

