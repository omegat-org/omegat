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

package org.omegat.gui.dialogs;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.omegat.core.threads.CommandThread;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;

/**
 * The dialog to change the font of OmegaT windows.
 *
 * @author  Maxym Mykhalchuk
 */
public class FontSelectionDialog extends JDialog 
{
	private String m_font, m_fontSize;
	
	/** creates a new Font Selection Dialog */
	public FontSelectionDialog(JFrame par, String font, String fontSize)
	{
		super(par, true);
		
		m_font = font;
		m_fontSize = fontSize;
		
		setSize(300, 100);
		setLocation(200, 200);
		Container cont = getContentPane();
		cont.setLayout(new GridLayout(3, 2, 8, 3));

		// create UI objects
		JLabel fontLabel = new JLabel();
		m_fontCB = new JComboBox(StaticUtils.getFontNames());
		m_fontCB.setEditable(true);
		if (!m_font.equals(""))										// NOI18N
			m_fontCB.setSelectedItem(m_font);
		cont.add("font label", fontLabel);									// NOI18N
		cont.add("font box", m_fontCB);										// NOI18N

		String[] fontSizes = new String[] 
			{	"8",	"9",	"10",	"11",								// NOI18N
				"12",	"14",	"16",	"18" };								// NOI18N
		JLabel fontSizeLabel = new JLabel();
		m_fontSizeCB = new JComboBox(fontSizes);
		m_fontSizeCB.setEditable(true);
		if (!m_fontSize.equals(""))									// NOI18N
			m_fontSizeCB.setSelectedItem(m_fontSize);
		cont.add("size label", fontSizeLabel);								// NOI18N
		cont.add("size box", m_fontSizeCB);									// NOI18N
		cont.add("spacer", new Container());								// NOI18N

		Box buttonBox = Box.createHorizontalBox();
		JButton okButton = new JButton();
		JButton cancelButton = new JButton();
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(okButton);
		buttonBox.add(Box.createHorizontalStrut(5));
		buttonBox.add(cancelButton);
		cont.add(buttonBox);

		// add text
		okButton.setText(OStrings.PP_BUTTON_OK);
		cancelButton.setText(OStrings.PP_BUTTON_CANCEL);

		fontLabel.setText(OStrings.TF_SELECT_SOURCE_FONT);
		fontSizeLabel.setText(OStrings.TF_SELECT_FONTSIZE);

		setTitle(OStrings.TF_SELECT_FONTS_TITLE);

		// arrange action listeners
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				doOK();
			}
		});

		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				doCancel();
			}
		});
	}

	/** @return if the user has changed the font and/or font size */
	public boolean isChanged()		
	{ 
		return m_isChanged;	
	}
	
	private void doOK()
	{
		String str;
		CommandThread core = CommandThread.core;
		str = m_fontCB.getSelectedItem().toString();
		if (!str.equals(m_font))
		{
			m_isChanged = true;
			m_font = str;
			core.setPreference(OConsts.TF_SRC_FONT_NAME, m_font);
		}

		str = m_fontSizeCB.getSelectedItem().toString();
		if (!str.equals(m_fontSize))
		{
			m_isChanged = true;
			m_fontSize = str;
			core.setPreference(OConsts.TF_SRC_FONT_SIZE, m_fontSize);
		}

		dispose();
	}

	private void		doCancel()		{ dispose();			}

	private JComboBox	m_fontCB;
	private JComboBox	m_fontSizeCB;

	private boolean	m_isChanged;
}

