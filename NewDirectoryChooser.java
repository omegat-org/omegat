//-------------------------------------------------------------------------
//  
//  NewDirectoryChooser.java - 
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


import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.lang.*;
import java.awt.event.*;

class NewDirectoryChooser extends JFileChooser
{
	public NewDirectoryChooser()
	{
		setFileView(new ProjectFileView());
		setMultiSelectionEnabled(false);
		setFileHidingEnabled(true);
//		addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent e)
//			{
//				System.out.println("event: " +e.getActionCommand());
//			}
//		});
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
	
	
	//////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		JOptionPane jop = new JOptionPane();
		jop.showMessageDialog(null, OStrings.NDC_SELECT_UNIQUE, 
					OStrings.NDC_SELECT_UNIQUE_TITLE, 
					JOptionPane.ERROR_MESSAGE); 
	}
}
