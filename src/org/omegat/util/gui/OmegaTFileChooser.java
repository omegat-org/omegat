/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/
               Support center: https://omegat.org/support

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

    /** OmegaT project icon */
    private static final ImageIcon OMEGAT_ICON = new ImageIcon(ResourcesUtil.APP_ICON_16X16);

    /**
     * Constructs an <code>OmegaTFileChooser</code> pointing to OmegaT's current
     * directory.
     */
    public OmegaTFileChooser() {
        super(Preferences.getPreference(Preferences.CURRENT_FOLDER));
    }

    /** Redefines the icon for OmegaT projects. */
    public Icon getIcon(File f) {
        if (StaticUtils.isProjectDir(f)) {
            return OMEGAT_ICON;
        } else {
            return super.getIcon(f);
        }
    }

    /** Redefines the file type for OmegaT projects. */
    public String getTypeDescription(File f) {
        if (StaticUtils.isProjectDir(f)) {
            return OStrings.getString("PFC_OMEGAT_PROJECT");
        } else {
            return super.getTypeDescription(f);
        }
    }
}
