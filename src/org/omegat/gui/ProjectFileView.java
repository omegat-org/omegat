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
import javax.swing.filechooser.FileView;
import java.io.File;

/**
 * A special class to insert a custom OmegaT image into JFileChooser
 *
 * @author Keith Godfrey
 */
class ProjectFileView extends FileView
{
	private ImageIcon omegatIcon = new ImageIcon("images" + File.separator + "OmegaT.gif");	// NOI18N
	
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
