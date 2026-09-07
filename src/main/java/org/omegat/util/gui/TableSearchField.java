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

package org.omegat.util.gui;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.openide.awt.Mnemonics;

import org.omegat.util.OStrings;

/**
 * Generic live search over a sorted table: a labeled text field driving the
 * sorter's row filter through the {@link TableSearchQuery} language, plus a
 * hit counter ("12/160") that a screen reader also gets through the field's
 * accessible description. Reusable by any table with a
 * {@link TableRowSorter}.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
@SuppressWarnings("serial")
public class TableSearchField extends Box {

    private final JTextField field = new JTextField(24);
    private final javax.swing.JButton clearButton = new javax.swing.JButton();
    private final JLabel counter = new JLabel();
    private final JTable table;
    private final TableRowSorter<? extends TableModel> sorter;

    public TableSearchField(JTable table, TableRowSorter<? extends TableModel> sorter) {
        super(javax.swing.BoxLayout.LINE_AXIS);
        this.table = table;
        this.sorter = sorter;

        JLabel label = new JLabel();
        Mnemonics.setLocalizedText(label, OStrings.getString("TABLE_SEARCH_LABEL"));
        label.setLabelFor(field);
        add(label);
        add(Box.createHorizontalStrut(5));
        add(field);
        add(Box.createHorizontalStrut(2));
        // same affordance as the color table's search: icon-only reset
        // button, active only while a query is set
        clearButton.setIcon(new javax.swing.ImageIcon(
                TableSearchField.class.getResource("/org/omegat/gui/resources/clear-button.png")));
        clearButton.setDisabledIcon(new javax.swing.ImageIcon(TableSearchField.class
                .getResource("/org/omegat/gui/resources/clear-button-disabled.png")));
        clearButton.setPressedIcon(new javax.swing.ImageIcon(TableSearchField.class
                .getResource("/org/omegat/gui/resources/clear-button-pressed.png")));
        clearButton.setBorderPainted(false);
        clearButton.setContentAreaFilled(false);
        clearButton.setToolTipText(OStrings.getString("KEYSTROKE_EDITOR_CLEAR_BUTTON"));
        clearButton.getAccessibleContext()
                .setAccessibleName(OStrings.getString("KEYSTROKE_EDITOR_CLEAR_BUTTON"));
        clearButton.setEnabled(false);
        clearButton.addActionListener(e -> field.setText(""));
        add(clearButton);
        add(Box.createHorizontalStrut(10));
        add(counter);

        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
        sorter.addRowSorterListener(e -> updateCounter());
        updateCounter();
    }

    private void applyFilter() {
        sorter.setRowFilter(TableSearchQuery.parse(field.getText()));
        clearButton.setEnabled(!field.getText().isEmpty());
        updateCounter();
    }

    private void updateCounter() {
        String hits = OStrings.getString("TABLE_SEARCH_HITS", String.valueOf(table.getRowCount()),
                String.valueOf(table.getModel().getRowCount()));
        counter.setText(hits);
        // the current hit count travels with the field, so screen readers
        // hear it without leaving the search box
        field.getAccessibleContext()
                .setAccessibleDescription(OStrings.getString("TABLE_SEARCH_DESC") + " " + hits);
    }

    /** The text field, for focus requests and tests. */
    public JTextField getField() {
        return field;
    }
}
