/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
               2015 Aaron Madlon-Kay
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

package org.omegat.gui.main;

import java.awt.Component;

import javax.swing.JPanel;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingConstants;

/**
 * Dockable JPanel for a docking library.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public class DockablePanel extends JPanel implements Dockable {
    private final DockKey dockKey;

    public DockablePanel(String key, String name, boolean detouchable) {
        dockKey = new DockKey(key, name, null, null, DockingConstants.HIDE_BOTTOM);
        dockKey.setFloatEnabled(detouchable);
        dockKey.setCloseEnabled(false);
    }

    @Override
    public DockKey getDockKey() {
        return dockKey;
    }

    @Override
    public Component getComponent() {
        return this;
    }

    /** Updates the tool tip text of the docking pane. */
    @Override
    public void setToolTipText(String text) {
        dockKey.setTooltip(text);
    }

    /** Updates the name of the docking pane. */
    @Override
    public void setName(String name) {
        dockKey.setName(name);
    }
}
