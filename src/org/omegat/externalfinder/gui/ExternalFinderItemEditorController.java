/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.externalfinder.gui;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;

import org.omegat.externalfinder.item.ExternalFinderItem;
import org.omegat.externalfinder.item.ExternalFinderItem.ENCODING;
import org.omegat.externalfinder.item.ExternalFinderItem.SCOPE;
import org.omegat.externalfinder.item.ExternalFinderItem.TARGET;
import org.omegat.externalfinder.item.ExternalFinderItemCommand;
import org.omegat.externalfinder.item.ExternalFinderItemURL;
import org.omegat.gui.dialogs.KeyStrokeEditorDialog;
import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.TableColumnSizer;

/**
 * Editor GUI for a single {@link ExternalFinderItem}.
 *
 * @author Aaron Madlon-Kay
 */
public class ExternalFinderItemEditorController {

    private static final int MAX_ROW_COUNT = 5;

    private final ExternalFinderItemEditorPanel panel;
    private final ExternalFinderItem.Builder builder;
    private boolean userDidConfirm;

    public ExternalFinderItemEditorController(SCOPE scope) {
        this(new ExternalFinderItem.Builder().setScope(scope));
    }

    public ExternalFinderItemEditorController(ExternalFinderItem item) {
        this(ExternalFinderItem.Builder.from(item));
    }

    public ExternalFinderItemEditorController(ExternalFinderItem.Builder builder) {
        this.builder = builder;
        this.panel = new ExternalFinderItemEditorPanel();
    }

    public boolean show(Window parent) {
        JDialog dialog = new JDialog(parent, OStrings.getString("EXTERNALFINDER_EDITOR_TITLE"));
        dialog.setModal(true);
        dialog.getContentPane().add(panel);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        StaticUIUtils.setWindowIcon(dialog);
        StaticUIUtils.setEscapeClosable(dialog);

        panel.nameField.setText(builder.getName());
        panel.nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                builder.setName(panel.nameField.getText().trim());
                validate();
            }
        });

        panel.setKeystrokeButton.addActionListener(e -> editKeyStroke());
        updateKeyStroke();

        panel.popupCheckBox.setSelected(!builder.isNopopup());
        panel.popupCheckBox.addActionListener(e -> builder.setNopopup(!panel.popupCheckBox.isSelected()));

        // URLs table

        panel.addUrlButton.addActionListener(e -> addUrl());
        panel.removeUrlButton.addActionListener(e -> removeSelectedUrl());
        panel.editUrlButton.addActionListener(e -> editSelectedUrl());

        panel.urlsTable.setModel(new UrlsTableModel());
        panel.urlsTable.getSelectionModel().addListSelectionListener(e -> onUrlSelectionChanged());

        Dimension tableSize = panel.urlsTable.getPreferredSize();
        panel.urlsTable.setPreferredScrollableViewportSize(
                new Dimension(tableSize.width, panel.urlsTable.getRowHeight() * MAX_ROW_COUNT));

        TableColumnSizer.autoSize(panel.urlsTable, UrlColumn.URL.index, true);

        panel.urlsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    editSelectedUrl();
                }
            }
        });

        // Commands table

        panel.addCommandButton.addActionListener(e -> addCommand());
        panel.removeCommandButton.addActionListener(e -> removeSelectedCommand());
        panel.editCommandButton.addActionListener(e -> editSelectedCommand());

        panel.commandsTable.setModel(new CommandsTableModel());
        panel.commandsTable.getSelectionModel().addListSelectionListener(e -> onCommandSelectionChanged());

        tableSize = panel.commandsTable.getPreferredSize();
        panel.commandsTable.setPreferredScrollableViewportSize(
                new Dimension(tableSize.width, panel.commandsTable.getRowHeight() * MAX_ROW_COUNT));

        TableColumnSizer.autoSize(panel.commandsTable, CommandColumn.COMMAND.index, true);

        panel.commandsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    editSelectedCommand();
                }
            }
        });

        panel.okButton.addActionListener(e -> {
            if (validate()) {
                userDidConfirm = true;
                StaticUIUtils.closeWindowByEvent(dialog);
            }
        });
        dialog.getRootPane().setDefaultButton(panel.okButton);

        panel.cancelButton.addActionListener(e -> {
            userDidConfirm = false;
            StaticUIUtils.closeWindowByEvent(dialog);
        });

        onUrlSelectionChanged();
        onCommandSelectionChanged();
        validate();

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return userDidConfirm;
    }

    public ExternalFinderItem getResult() {
        return builder.build();
    }

    private boolean validate() {
        boolean isValid = true;
        try {
            builder.validate();
        } catch (Exception e) {
            isValid = false;
        }
        panel.okButton.setEnabled(isValid);
        return isValid;
    }

    private void editKeyStroke() {
        KeyStrokeEditorDialog dialog = new KeyStrokeEditorDialog(builder.getKeyStroke());
        if (dialog.show(SwingUtilities.windowForComponent(panel))) {
            builder.setKeyStroke(dialog.getResult());
            updateKeyStroke();
        }
    }

    private void updateKeyStroke() {
        KeyStroke ks = builder.getKeyStroke();
        String text = ks == null ? OStrings.getString("KEYSTROKE_EDITOR_NOT_SET")
                : StaticUIUtils.getKeyStrokeText(ks);
        panel.keystrokeLabel.setText(text);
    }

    private void addUrl() {
        ExternalFinderItemURLEditorController editor = new ExternalFinderItemURLEditorController();
        if (editor.show(SwingUtilities.windowForComponent(panel))) {
            int row = panel.urlsTable.getSelectedRow();
            int newRow = row >= 0 ? row + 1 : builder.getURLs().size();
            builder.getURLs().add(newRow, editor.getResult());
            panel.urlsTable.repaint();
            panel.urlsTable.setRowSelectionInterval(newRow, newRow);
            validate();
        }
    }

    private void removeSelectedUrl() {
        int row = panel.urlsTable.getSelectedRow();
        if (row >= 0) {
            builder.getURLs().remove(row);
            panel.urlsTable.repaint();
            validate();
        }
    }

    private void editSelectedUrl() {
        int row = panel.urlsTable.getSelectedRow();
        if (row >= 0) {
            ExternalFinderItemURL url = builder.getURLs().get(row);
            ExternalFinderItemURLEditorController editor = new ExternalFinderItemURLEditorController(url);
            if (editor.show(SwingUtilities.windowForComponent(panel))) {
                builder.getURLs().set(row, editor.getResult());
                panel.urlsTable.repaint();
                validate();
            }
        }
    }

    private void addCommand() {
        ExternalFinderItemCommandEditorController editor = new ExternalFinderItemCommandEditorController();
        if (editor.show(SwingUtilities.windowForComponent(panel))) {
            int row = panel.commandsTable.getSelectedRow();
            int newRow = row >= 0 ? row + 1 : builder.getCommands().size();
            builder.getCommands().add(newRow, editor.getResult());
            panel.commandsTable.repaint();
            panel.commandsTable.setRowSelectionInterval(newRow, newRow);
            validate();
        }
    }

    private void removeSelectedCommand() {
        int row = panel.commandsTable.getSelectedRow();
        if (row >= 0) {
            builder.getCommands().remove(row);
            panel.commandsTable.repaint();
            validate();
        }
    }

    private void editSelectedCommand() {
        int row = panel.commandsTable.getSelectedRow();
        if (row >= 0) {
            ExternalFinderItemCommand command = builder.getCommands().get(row);
            ExternalFinderItemCommandEditorController editor = new ExternalFinderItemCommandEditorController(
                    command);
            if (editor.show(SwingUtilities.windowForComponent(panel))) {
                builder.getCommands().set(row, editor.getResult());
                panel.commandsTable.repaint();
                validate();
            }
        }
    }

    private void onUrlSelectionChanged() {
        int row = panel.urlsTable.getSelectedRow();
        boolean enabled = row >= 0;
        panel.removeUrlButton.setEnabled(enabled);
        panel.editUrlButton.setEnabled(enabled);
    }

    private void onCommandSelectionChanged() {
        int row = panel.commandsTable.getSelectedRow();
        boolean enabled = row >= 0;
        panel.removeCommandButton.setEnabled(enabled);
        panel.editCommandButton.setEnabled(enabled);
    }

    enum UrlColumn {
        URL(0, OStrings.getString("EXTERNALFINDER_EDITOR_COLUMN_URL"), String.class),
        TARGET(1, OStrings.getString("EXTERNALFINDER_EDITOR_COLUMN_TARGET"), TARGET.class),
        ENCODING(2, OStrings.getString("EXTERNALFINDER_EDITOR_COLUMN_ENCODING"), ENCODING.class);

        final int index;
        final String label;
        final Class<?> clazz;

        UrlColumn(int index, String label, Class<?> clazz) {
            this.index = index;
            this.label = label;
            this.clazz = clazz;
        }

        static UrlColumn get(int index) {
            return values()[index];
        }
    }

    @SuppressWarnings("serial")
    class UrlsTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return builder.getURLs().size();
        }

        @Override
        public int getColumnCount() {
            return UrlColumn.values().length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (UrlColumn.get(columnIndex)) {
            case URL:
                return builder.getURLs().get(rowIndex).getURL();
            case TARGET:
                return builder.getURLs().get(rowIndex).getTarget();
            case ENCODING:
                return builder.getURLs().get(rowIndex).getEncoding();
            }
            throw new IllegalArgumentException();
        }

        @Override
        public String getColumnName(int column) {
            return UrlColumn.get(column).label;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return UrlColumn.get(columnIndex).clazz;
        }
    }

    enum CommandColumn {
        COMMAND(0, OStrings.getString("EXTERNALFINDER_EDITOR_COLUMN_COMMAND"), String.class),
        TARGET(1, OStrings.getString("EXTERNALFINDER_EDITOR_COLUMN_TARGET"), TARGET.class),
        ENCODING(2, OStrings.getString("EXTERNALFINDER_EDITOR_COLUMN_ENCODING"), ENCODING.class),
        DELIMITER(3, OStrings.getString("EXTERNALFINDER_EDITOR_COLUMN_DELIMITER"), String.class);

        final int index;
        final String label;
        final Class<?> clazz;

        CommandColumn(int index, String label, Class<?> clazz) {
            this.index = index;
            this.label = label;
            this.clazz = clazz;
        }

        static CommandColumn get(int index) {
            return values()[index];
        }
    }

    @SuppressWarnings("serial")
    class CommandsTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return builder.getCommands().size();
        }

        @Override
        public int getColumnCount() {
            return CommandColumn.values().length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (CommandColumn.get(columnIndex)) {
            case COMMAND:
                return builder.getCommands().get(rowIndex).getCommand();
            case TARGET:
                return builder.getCommands().get(rowIndex).getTarget();
            case ENCODING:
                return builder.getCommands().get(rowIndex).getEncoding();
            case DELIMITER:
                return builder.getCommands().get(rowIndex).getDelimiter();
            }
            throw new IllegalArgumentException();
        }

        @Override
        public String getColumnName(int column) {
            return CommandColumn.get(column).label;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return CommandColumn.get(columnIndex).clazz;
        }
    }
}
