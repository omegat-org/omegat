/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.util.gui;

import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * Basic File Chooser for OmegaT, showing the icon for OmegaT projects
 * and customizing the description for OmegaT project directories.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class OmegaTFileChooser extends JFileChooser
{
 
    /**
     * Constructs an <code>OmegaTFileChooser</code> pointing to OmegaT's
     * current directory.
     */
    public OmegaTFileChooser()
    {
        this(Preferences.getPreference(Preferences.CURRENT_FOLDER));
    }
    /**
     * Constructs an <code>OmegaTFileChooser</code> using the given path.
     */
    public OmegaTFileChooser(String path)
    {
        super(path);
        try
        {
            if( omegatIcon==null )
                omegatIcon = new ImageIcon( getClass().getResource(
                        "/org/omegat/gui/resources/OmegaT_small.gif") );        // NOI18N
        }
        catch( Exception e )
        {
            // do nothing
        }

        /* Handle Cmd+N key on Mac OSX to create new directories
         * Fix for bug 1556293
         * (Note: Windows and Linux already have functionality for this)
         *
         * @author Henry Pijffers (henry.pijffers@saxnot.com)
         */
        /*if (StaticUtils.onMacOSX()) {
            KeyStroke newDirKey = KeyStroke.getKeyStroke(KeyEvent.VK_N, java.awt.event.InputEvent.META_MASK);
            Action newDirAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    createNewDir();
                }
            };
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(newDirKey, "NEW DIRECTORY"); // NOI18N
            getActionMap().put("NEW DIRECTORY", newDirAction); // NOI18N
        }*/
    }

    /**
      * Creates a new directory/folder in the current directory/folder.
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    /*private void createNewDir() {
        // get the current directory (file chooser)
        File currentDirectory = getCurrentDirectory();

        File newFolder = null;
        try {
            // create a new directory in the current directory
            newFolder = getFileSystemView().createNewFolder(currentDirectory);

            // select (visually) the newly created directory
            if (isMultiSelectionEnabled()) {
                setSelectedFiles(new File[]{newFolder});
            } else {
                setSelectedFile(newFolder);
            }
        } catch (IOException exception) {
            // log the error
            StaticUtils.log(OStrings.getString("OFC_NEW_DIR_ERROR"));
            StaticUtils.log(exception.getLocalizedMessage());
            exception.printStackTrace(StaticUtils.getLogStream());

            // display the error
            CommandThread.core.displayErrorMessage(OStrings.getString("OFC_NEW_DIR_ERROR"), exception);

            // give up
            return;
        }

        // redisplay the current directory (so the new dir is displayed correctly)
        rescanCurrentDirectory();
    }*/

    /** OmegaT project icon */
    private static ImageIcon omegatIcon = null;

    /** Redefines the icon for OmegaT projects. */
    public Icon getIcon(File f)
    {
        if( isProjectDir(f) && omegatIcon!=null )
            return omegatIcon;
        else
            return super.getIcon(f);
    }

    /** Redefines the file type for OmegaT projects. */
    public String getTypeDescription(File f)
    {
        if (isProjectDir(f))
            return OStrings.getString("PFC_OMEGAT_PROJECT");
        else
            return super.getTypeDescription(f);
    }
    
	public static boolean isProjectDir(File f)
	{
		if( f==null || f.getName().length()==0 )
			return false;
		File projFile = new File(f.getAbsolutePath() + File.separator + 
		    OConsts.FILE_PROJECT);
		File internal = new File(f.getAbsolutePath() + File.separator + 
		    OConsts.DEFAULT_INTERNAL);
        return projFile.exists() && internal.exists() && internal.isDirectory();
	}
    
}
