//-------------------------------------------------------------------------
//  
//  ProjectFileChooser.java - 
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
//  Build date:  23Feb2002
//  Copyright (C) 2002, Keith Godfrey
//  aurora@coastside.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------


import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.*;
import java.beans.*;
import java.lang.*;

class ProjectFileChooser extends JFileChooser
{
	public ProjectFileChooser()
	{
		setFileView(new ProjectFileView());
		setFileSelectionMode(DIRECTORIES_ONLY);
		setMultiSelectionEnabled(false);
		setFileHidingEnabled(true);
		addPropertyChangeListener(new DirectoryChangeListener());
	}

	protected void acceptedProjectDir()
	{
		approveSelection();
	}

	public void approveSelection()
	{
		// user hit 'open' button - redirect command to open project or
		//  recurse into lower directory
		if (isProjectDir(getSelectedFile()))
		{
			// ALERT - HACK
			// when double clicking the file, everything works fine,
			//  but when hitting 'OK' the parent directory is somehow
			//	selected.  Explicitly make sure the selected directory
			//	is targetted
			File d = getSelectedFile();
			if (d.isDirectory())
				setCurrentDirectory(d);
			// this is OK - continue
			super.approveSelection();
		}
		else
		{
			setCurrentDirectory(getSelectedFile());
		}
	}
	
	public static boolean isProjectDir(File f)
	{
		File projFile = new File(f.getAbsolutePath() + File.separator + 
		    OConsts.PROJ_FILENAME);
		File internal = new File(f.getAbsolutePath() + File.separator + 
		    OConsts.DEFAULT_INTERNAL);
		if (projFile.exists() && internal.exists() && 
					internal.isDirectory())
		{
			return true;
		}
		else
			return false;
	}

	class DirectoryChangeListener implements PropertyChangeListener
	{
		public void propertyChange(PropertyChangeEvent evt)
		{
			if (isProjectDir(getCurrentDirectory()))
			{
				acceptedProjectDir();
			}
		}
	}

	//////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		ProjectFileChooser pfc = new ProjectFileChooser();
		int retVal = pfc.showDialog(null, "select");
		if (retVal == JFileChooser.APPROVE_OPTION)
			System.out.println("accepted project directory '" +
					pfc.getCurrentDirectory() + "'");
		else
			System.out.println("user cancelled");
	}
}
