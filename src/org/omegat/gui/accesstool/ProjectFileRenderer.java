/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.gui.accesstool;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

public class ProjectFileRenderer implements ListCellRenderer<ProjectFileInformation> {

    public ProjectFileRenderer() {
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ProjectFileInformation> list, ProjectFileInformation value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        JPanel panel = new JPanel();
        if (value != null) {
            panel.setBorder(BorderFactory.createEmptyBorder(5, 1, 5, 1));
            panel.setLayout(new BorderLayout());
            JLabel filePath = new JLabel(value.getFilePath());
            panel.add(filePath, BorderLayout.WEST);
            if (index < 0) {
                return panel;
            }
            JLabel segmentNumber = new JLabel(String.format("(%d)", value.getSegments()));
            panel.add(segmentNumber, BorderLayout.EAST);
            if (cellHasFocus) {
                panel.setBackground(UIManager.getColor("ComboBox.renderer[Selected].background"));
            }
        }
        return panel;
    }
}
