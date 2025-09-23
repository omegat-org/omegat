
/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Briac Pilpre (briacp@gmail.com)
               2013 Alex Buloichik
               2014 Briac Pilpre (briacp@gmail.com), Yu Tang
               2015 Yu Tang, Aaron Madlon-Kay
               2025 Hiroshi Miura
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.gui.scripting.ui;

import org.omegat.gui.scripting.ScriptItem;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collection;

/**
 * Panel containing the script list and related functionality
 */
@SuppressWarnings("serial")
public class ScriptListPanel extends JPanel {

    private final JList<ScriptItem> scriptList;

    public ScriptListPanel() {
        setLayout(new BorderLayout());

        scriptList = new JList<>();
        JScrollPane scrollPane = new JScrollPane(scriptList);

        scriptList.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                ListModel<ScriptItem> lm = scriptList.getModel();
                int index = scriptList.locationToIndex(e.getPoint());
                if (index > -1) {
                    scriptList.setToolTipText(lm.getElementAt(index).getFileName());
                }
            }
        });

        add(scrollPane, BorderLayout.CENTER);
    }

    public void setScriptItems(Collection<ScriptItem> items) {
        scriptList.setListData(items.toArray(new ScriptItem[0]));
    }

    public void addListSelectionListener(ListSelectionListener listener) {
        scriptList.addListSelectionListener(listener);
    }

    public ScriptItem getSelectedValue() {
        return scriptList.getSelectedValue();
    }

    public boolean isSelectionEmpty() {
        return scriptList.isSelectionEmpty();
    }

    public void clearSelection() {
        scriptList.clearSelection();
    }
}
