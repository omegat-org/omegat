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
import org.omegat.util.OStrings;

/**
 * File Chooser to open project.
 * Project is a directory, so it's a bit tricky, we need to react on both:
 * - changing directory
 * - and hitting OK. 
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
class ProjectFileChooser extends JFileChooser
{

	public ProjectFileChooser(String curdir)
	{
        super(curdir);
		setFileSelectionMode(DIRECTORIES_ONLY);
		setMultiSelectionEnabled(false);
	}
    
    public void approveSelection()
	{
		// user hit 'open' button - redirect command to open project or
		//  recurse into lower directory
		if (isProjectDir(getSelectedFile()))
			// The parent directory is made current,
            // and the project's directory is the selected 'file'.
			super.approveSelection();
        else
            setCurrentDirectory(getSelectedFile());
	}
	
	public static boolean isProjectDir(File f)
	{
		if( f==null || f.getName().length()==0 )
			return false;
		File projFile = new File(f.getAbsolutePath() + File.separator + 
		    OConsts.PROJ_FILENAME);
		File internal = new File(f.getAbsolutePath() + File.separator + 
		    OConsts.DEFAULT_INTERNAL);
        return projFile.exists() && internal.exists() && internal.isDirectory();
	}

    public void setCurrentDirectory(File dir)
    {
        if( isProjectDir(dir) )
        {
            setSelectedFile(dir);
            approveSelection();
        }
        else
            super.setCurrentDirectory(dir);
    }

    /** OmegaT project icon */
    private static ImageIcon omegatIcon = new ImageIcon("images" + File.separator + "OmegaT_small.gif");	// NOI18N

    /** Redefines the icon for OmegaT projects. */
    public Icon getIcon(File f)
    {
		if (ProjectFileChooser.isProjectDir(f))
			return omegatIcon;
		else	
			return super.getIcon(f);
    }

    /** Redefines the file type for OmegaT projects. */
    public String getTypeDescription(File f)
    {
		if (ProjectFileChooser.isProjectDir(f))
			return OStrings.PFC_OMEGAT_PROJECT;
		else
			return super.getTypeDescription(f);
    }
}
