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

public class NewDirectoryChooser extends JFileChooser
{
	public NewDirectoryChooser()
	{
		setFileView(new ProjectFileView());
		setMultiSelectionEnabled(false);
		setFileHidingEnabled(true);
	}
	
	public void approveSelection()
	{
		// user hit 'open' button - redirect command to open project or
		//  recurse into lower directory
		if (getSelectedFile().exists())
		{
			// must select non-existing name for project
			JOptionPane jop = new JOptionPane();
			jop.showMessageDialog(this, OStrings.NDC_SELECT_UNIQUE,
					OStrings.NDC_SELECT_UNIQUE_TITLE, 
					JOptionPane.ERROR_MESSAGE); 
		}
		else
		{
			// this is OK - continue
			super.approveSelection();
		}
	}
	
}
