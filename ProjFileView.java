//-------------------------------------------------------------------------
//  
//  ProjFileView.java - 
//  
//  Copyright (C) 2004, Keith Godfrey
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
//  Copyright (C) 2004, Keith Godfrey, et al
//  keithgodfrey@users.sourceforge.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------


import java.io.File;
import java.util.*;
import javax.swing.filechooser.*;
import javax.swing.*;

class ProjectFileView extends FileView
{
	ImageIcon omegatIcon = new ImageIcon("images" + File.separator 
				+ "OmegaT.gif");

	public String getName(File f)				{ return null;		}
	public String getDescription(File f)		{ return null;		}
	public Boolean isTraversable(File f)		{ return null;		}
	public String getTypeDescription(File f)
	{
		if (ProjectFileChooser.isProjectDir(f))
			return OStrings.PFC_OMEGAT_PROJECT;
		else
			return null;
	}
	public Icon getIcon(File f)
	{
		if (ProjectFileChooser.isProjectDir(f))
			return omegatIcon;
		else	
			return null;
	}
}
