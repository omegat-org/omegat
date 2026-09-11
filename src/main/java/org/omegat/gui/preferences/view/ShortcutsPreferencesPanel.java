/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.gui.preferences.view;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.openide.awt.Mnemonics;

import org.omegat.util.OStrings;

/**
 * Panel of the shortcuts preferences view: a labeled filter field, the
 * sortable table of all shortcutable functions, the action buttons and the
 * effectiveness hint. Hand-built for full control over the accessible
 * names; all logic lives in {@link ShortcutsPreferencesController}.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
@SuppressWarnings("serial")
public class ShortcutsPreferencesPanel extends JPanel {

    final JTable table = new JTable();
    final JButton assignButton = new JButton();
    final JButton removeButton = new JButton();
    final JButton restoreButton = new JButton();
    final JButton exportButton = new JButton();
    final JButton importButton = new JButton();

    public ShortcutsPreferencesPanel() {
        super(new BorderLayout(0, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        table.getAccessibleContext().setAccessibleName(OStrings.getString("PREFS_SHORTCUTS_TABLE_NAME"));
        table.getAccessibleContext()
                .setAccessibleDescription(OStrings.getString("PREFS_SHORTCUTS_TABLE_DESC"));
        add(new JScrollPane(table), BorderLayout.CENTER);

        Mnemonics.setLocalizedText(assignButton, OStrings.getString("PREFS_SHORTCUTS_ASSIGN"));
        Mnemonics.setLocalizedText(removeButton, OStrings.getString("PREFS_SHORTCUTS_REMOVE"));
        Mnemonics.setLocalizedText(restoreButton, OStrings.getString("PREFS_SHORTCUTS_RESTORE_DEFAULT"));
        Mnemonics.setLocalizedText(exportButton, OStrings.getString("PREFS_SHORTCUTS_EXPORT"));
        Mnemonics.setLocalizedText(importButton, OStrings.getString("PREFS_SHORTCUTS_IMPORT"));
        Box buttons = Box.createHorizontalBox();
        buttons.add(assignButton);
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(removeButton);
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(restoreButton);
        buttons.add(Box.createHorizontalGlue());
        buttons.add(exportButton);
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(importButton);

        JLabel hint = new JLabel(OStrings.getString("PREFS_SHORTCUTS_EFFECT_HINT"));
        Box south = Box.createVerticalBox();
        buttons.setAlignmentX(LEFT_ALIGNMENT);
        hint.setAlignmentX(LEFT_ALIGNMENT);
        south.add(buttons);
        south.add(Box.createVerticalStrut(5));
        south.add(hint);
        add(south, BorderLayout.SOUTH);
    }
}
