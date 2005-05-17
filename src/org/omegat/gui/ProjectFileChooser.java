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

import org.omegat.util.OConsts;

import javax.swing.*;
import java.io.File;

/**
 * File Chooser to open project.
 *
 * @author Keith Godfrey
 */
class ProjectFileChooser extends JFileChooser
{

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
		if (f == null)
			return false;
		File projFile = new File(f.getAbsolutePath() + File.separator + 
		    OConsts.PROJ_FILENAME);
		File internal = new File(f.getAbsolutePath() + File.separator + 
		    OConsts.DEFAULT_INTERNAL);
        return projFile.exists() && internal.exists() && internal.isDirectory();
	}

}
