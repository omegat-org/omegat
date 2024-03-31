/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2022 Hiroshi Miura
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

package org.omegat.gui.main;

import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * @author Hiroshi Miura
 */
public class MainDockingDesktop extends DockingDesktop {
    private static final long serialVersionUID = 1L;

    public void requestExpand(Dockable dockable) {
        if (dockable != null && dockable.getDockKey().isAutoHideEnabled()) {
            if (dockable.getDockKey().getLocation() == DockableState.Location.HIDDEN) {
                expandPanel.expand();
                scopeOutOfExpandedPanelTimer.restart();
            }
        }
    }

    // timer used to hide the expanded panel when scope is out too long
    private final javax.swing.Timer scopeOutOfExpandedPanelTimer = new javax.swing.Timer(
        2000, actionEvent -> {
            if (!expandPanel.isActive() && expandPanel.shouldCollapse()) {
                // do not hide it if it has got the focus
                // or if a non-collapsible operation is occurring
                expandPanel.collapse();
            }
        });
}
