/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util.gui;

import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import org.omegat.gui.common.OmegaTIcons;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;

/**
 * Basic File Chooser for OmegaT, showing the icon for OmegaT projects and
 * customizing the description for OmegaT project directories.
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
@SuppressWarnings("serial")
public class OmegaTFileChooser extends JFileChooser {

    /**
     * Constructs an <code>OmegaTFileChooser</code> pointing to OmegaT's current
     * directory.
     */
    public OmegaTFileChooser() {
        this(Preferences.getPreference(Preferences.CURRENT_FOLDER));
    }

    /**
     * Constructs an <code>OmegaTFileChooser</code> using the given path.
     */
    public OmegaTFileChooser(String path) {
        super(path);
        try {
            if (omegatIcon == null)
                omegatIcon = new ImageIcon(OmegaTIcons.ICONS.get(0));
        } catch (Exception e) {
            // do nothing
        }
    }

    /** OmegaT project icon */
    private static ImageIcon omegatIcon = null;

    /** Redefines the icon for OmegaT projects. */
    public Icon getIcon(File f) {
        if (StaticUtils.isProjectDir(f) && omegatIcon != null)
            return omegatIcon;
        else
            return super.getIcon(f);
    }

    /** Redefines the file type for OmegaT projects. */
    public String getTypeDescription(File f) {
        if (StaticUtils.isProjectDir(f))
            return OStrings.getString("PFC_OMEGAT_PROJECT");
        else
            return super.getTypeDescription(f);
    }
}
