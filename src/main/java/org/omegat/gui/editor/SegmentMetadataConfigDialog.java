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

package org.omegat.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.format.DateTimeFormatter;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.openide.awt.Mnemonics;

import org.omegat.gui.editor.SegmentMetadataGutter.Column;
import org.omegat.gui.editor.SegmentMetadataGutter.ColumnOption;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.StaticUIUtils;

/**
 * Small dialog to configure the segment metadata gutter: the visibility
 * toggle on top, below it a table with one row per column whose order decides
 * the display order. Rows move by drag and drop, by the up and down buttons of
 * the second table column, and by U/H and D/R or Alt with the arrow keys;
 * Space toggles the visibility of the selected row. The last table column offers
 * per-column options: a validated regex whose match becomes the shown value,
 * and a date format with the standard pattern letters. Changes apply
 * immediately, so the effect is visible while the dialog is open.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
@SuppressWarnings("serial")
final class SegmentMetadataConfigDialog extends JDialog {

    private final JLabel totalWidthLabel = new JLabel();

    SegmentMetadataConfigDialog(Frame owner, Runnable externalOnChange,
            ToIntFunction<Column> columnWidthProvider, IntSupplier totalWidthProvider,
            IntSupplier fontSizeProvider) {
        super(owner, OStrings.getString("GUI_EDITORWINDOW_GUTTER_MENU"), false);
        setLayout(new BorderLayout());
        // Every change also refreshes the shown total width.
        Runnable onChange = () -> {
            externalOnChange.run();
            updateTotalWidthLabel(totalWidthProvider);
        };

        JCheckBox show = new JCheckBox(OStrings.getString("GUI_EDITORWINDOW_GUTTER_SHOW"),
                Preferences.isPreference(Preferences.EDITOR_METADATA_GUTTER));
        show.addActionListener(e -> {
            Preferences.setPreference(Preferences.EDITOR_METADATA_GUTTER, show.isSelected());
            onChange.run();
        });
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING));
        top.setBorder(new EmptyBorder(6, 4, 0, 4));
        top.add(show);
        add(top, BorderLayout.NORTH);

        ColumnTableModel model = new ColumnTableModel(onChange);
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new RowMoveHandler(table, model));
        // Tab leaves the table instead of cycling through its cells, so the
        // other controls stay reachable by keyboard.
        table.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        table.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
        installMoveButtons(table, model);
        installKeyBindings(table, model);
        table.getColumnModel().getColumn(3).setCellRenderer(new OptionCellRenderer());
        table.getColumnModel().getColumn(3).setCellEditor(new OptionCellEditor(model, onChange));
        table.getColumnModel().getColumn(4).setCellRenderer(new AlignmentRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new AlignmentEditor(model, onChange));
        table.getColumnModel().getColumn(5)
                .setCellRenderer(new WidthSliderRenderer(columnWidthProvider));
        table.getColumnModel().getColumn(5).setCellEditor(
                new WidthSliderEditor(model, onChange, columnWidthProvider, fontSizeProvider));
        table.setRowHeight(new OptionCellPanel(Column.ID, () -> {
        }).getPreferredSize().height + 2);
        table.getAccessibleContext()
                .setAccessibleName(OStrings.getString("GUI_EDITORWINDOW_GUTTER_TABLE_COLUMN"));
        table.getAccessibleContext()
                .setAccessibleDescription(OStrings.getString("GUI_EDITORWINDOW_GUTTER_TABLE_A11Y"));
        // The table headers only label the columns; they never reorder them.
        table.getTableHeader().setReorderingAllowed(false);
        sizeColumns(table);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setFillsViewportHeight(true);
        JScrollPane tablePane = new JScrollPane(table);

        // One box holds the table, the total width and the display toggles.
        JPanel columnsBox = new JPanel(new BorderLayout());
        columnsBox.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(4, 10, 0, 10),
                BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                        new EmptyBorder(4, 4, 4, 4))));
        columnsBox.add(tablePane, BorderLayout.CENTER);
        JPanel display = new JPanel(new BorderLayout());
        JPanel toggles = new JPanel(new FlowLayout(FlowLayout.LEADING));
        toggles.add(createGutterToggle("GUI_EDITORWINDOW_GUTTER_GRID",
                Preferences.EDITOR_METADATA_GUTTER_GRID, onChange));
        toggles.add(createGutterToggle("GUI_EDITORWINDOW_GUTTER_ZEBRA",
                Preferences.EDITOR_METADATA_GUTTER_ZEBRA, onChange));
        display.add(toggles, BorderLayout.WEST);
        // The total sits at the trailing edge, beneath the width column.
        updateTotalWidthLabel(totalWidthProvider);
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        totalPanel.add(totalWidthLabel);
        display.add(totalPanel, BorderLayout.EAST);
        columnsBox.add(display, BorderLayout.SOUTH);
        add(columnsBox, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        JButton close = new JButton();
        Mnemonics.setLocalizedText(close, OStrings.getString("BUTTON_CLOSE"));
        close.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        buttons.add(close);
        bottom.add(buttons, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(close);

        StaticUIUtils.setEscapeClosable(this);
        pack();
        // The packed size shows the table without cutting off any column.
        setMinimumSize(getSize());
        setLocationRelativeTo(owner);

        // The keyboard works right away: the table starts focused with the
        // first row selected.
        table.setRowSelectionInterval(0, 0);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                table.requestFocusInWindow();
            }
        });
    }

    private void updateTotalWidthLabel(IntSupplier totalWidthProvider) {
        totalWidthLabel.setText(OStrings.getString("GUI_EDITORWINDOW_GUTTER_TOTAL_WIDTH") + ": "
                + totalWidthProvider.getAsInt() + " px");
    }

    private JCheckBox createGutterToggle(String labelKey, String prefKey, Runnable onChange) {
        JCheckBox box = new JCheckBox(OStrings.getString(labelKey), Preferences.isPreference(prefKey));
        box.addActionListener(e -> {
            Preferences.setPreference(prefKey, box.isSelected());
            onChange.run();
        });
        return box;
    }

    /** Column widths from the actual cell contents, so nothing is cut off. */
    private void sizeColumns(JTable table) {
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = table.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(table, table.getColumnName(column), false,
                            false, -1, column)
                    .getPreferredSize().width;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component cell = table.prepareRenderer(renderer, row, column);
                width = Math.max(width, cell.getPreferredSize().width);
            }
            table.getColumnModel().getColumn(column).setPreferredWidth(width + 12);
            if (column == 0 || column == 1) {
                // The toggle and move button columns keep their size when the
                // dialog grows.
                table.getColumnModel().getColumn(column).setMinWidth(width + 12);
                table.getColumnModel().getColumn(column).setMaxWidth(width + 12);
            } else if (column == 2) {
                // The column names are never cut off.
                table.getColumnModel().getColumn(column).setMinWidth(width + 12);
            }
        }
    }

    private void installMoveButtons(JTable table, ColumnTableModel model) {
        table.getColumnModel().getColumn(1).setCellRenderer(new MoveButtonsRenderer());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());
                if (row < 0 || column != 1) {
                    return;
                }
                if (table.isEditing()) {
                    // Close the open option editor before the rows move.
                    table.getCellEditor().stopCellEditing();
                }
                Rectangle cell = table.getCellRect(row, column, false);
                boolean up = e.getX() < cell.x + cell.width / 2;
                int moved = model.move(row, up ? -1 : 1);
                if (moved >= 0) {
                    table.setRowSelectionInterval(moved, moved);
                }
            }
        });
    }

    private void installKeyBindings(JTable table, ColumnTableModel model) {
        // Single letters move the row: U(p)/H(och) up, D(own)/R(unter) down;
        // Alt with the arrow keys works everywhere.
        for (KeyStroke up : new KeyStroke[] {
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_U, 0), KeyStroke.getKeyStroke(KeyEvent.VK_H, 0) }) {
            table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(up, "gutterRowUp");
        }
        for (KeyStroke down : new KeyStroke[] {
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), KeyStroke.getKeyStroke(KeyEvent.VK_R, 0) }) {
            table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(down,
                    "gutterRowDown");
        }
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "gutterRowToggle");
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "gutterRowAlign");
        table.getActionMap().put("gutterRowAlign", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (table.isEditing()) {
                    return;
                }
                int row = table.getSelectedRow();
                if (row >= 0) {
                    model.cycleAlignment(row);
                }
            }
        });
        table.getActionMap().put("gutterRowUp", new MoveAction(table, model, -1));
        table.getActionMap().put("gutterRowDown", new MoveAction(table, model, 1));
        table.getActionMap().put("gutterRowToggle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (table.isEditing()) {
                    // The keystroke belongs to the option field being edited.
                    return;
                }
                int row = table.getSelectedRow();
                if (row >= 0 && model.isCellEditable(row, 0)) {
                    model.setValueAt(!Boolean.TRUE.equals(model.getValueAt(row, 0)), row, 0);
                }
            }
        });
    }

    /** Moves the selected row by Alt with the arrow keys. */
    private static final class MoveAction extends AbstractAction {

        private final JTable table;
        private final ColumnTableModel model;
        private final int delta;

        MoveAction(JTable table, ColumnTableModel model, int delta) {
            this.table = table;
            this.model = model;
            this.delta = delta;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (table.isEditing()) {
                // The keystroke belongs to the option field being edited.
                return;
            }
            int row = table.getSelectedRow();
            if (row < 0) {
                return;
            }
            int moved = model.move(row, delta);
            if (moved >= 0) {
                table.setRowSelectionInterval(moved, moved);
            }
        }
    }

    /** Paints the up and down buttons of the move column. */
    private static final class MoveButtonsRenderer extends JPanel implements TableCellRenderer {

        MoveButtonsRenderer() {
            super(new GridLayout(1, 2, 2, 0));
            JButton up = new JButton("↑");
            up.getAccessibleContext()
                    .setAccessibleName(OStrings.getString("GUI_EDITORWINDOW_GUTTER_MOVE_UP"));
            JButton down = new JButton("↓");
            down.getAccessibleContext()
                    .setAccessibleName(OStrings.getString("GUI_EDITORWINDOW_GUTTER_MOVE_DOWN"));
            for (JButton button : new JButton[] { up, down }) {
                button.setMargin(new Insets(0, 2, 0, 2));
                button.setFocusable(false);
                add(button);
            }
            setToolTipText(OStrings.getString("GUI_EDITORWINDOW_GUTTER_TABLE_A11Y"));
            setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    /**
     * The per-column option cell: a checkbox enabling the option and a text
     * field with the regex or date format. The field only persists values
     * that compile; broken input is marked red and ignored by the gutter.
     */
    static final class OptionCellPanel extends JPanel {

        // Strong contrast in both themes: dark red ground, white text.
        private static final Color INVALID_BACKGROUND = new Color(0xB3, 0x26, 0x26);
        private static final Color INVALID_FOREGROUND = Color.WHITE;

        private final Column column;
        private final Runnable onChange;
        private final JCheckBox toggle;
        private final JTextField valueField;
        private final Color normalBackground;
        private final Color normalForeground;
        private boolean loading;

        OptionCellPanel(Column column, Runnable onChange) {
            super(new FlowLayout(FlowLayout.LEADING, 4, 1));
            this.column = column;
            this.onChange = onChange;
            setOpaque(false);
            toggle = new JCheckBox(OStrings.getString(column.getOption() == ColumnOption.REGEX
                    ? "GUI_EDITORWINDOW_GUTTER_OPT_REGEX" : "GUI_EDITORWINDOW_GUTTER_OPT_DATE_FORMAT"));
            toggle.setOpaque(false);
            valueField = new JTextField(10);
            String tooltip = "<html>" + OStrings.getString(column.getOption() == ColumnOption.REGEX
                    ? "GUI_EDITORWINDOW_GUTTER_OPT_REGEX_TOOLTIP"
                    : "GUI_EDITORWINDOW_GUTTER_OPT_DATE_FORMAT_TOOLTIP") + "</html>";
            toggle.setToolTipText(tooltip);
            valueField.setToolTipText(tooltip);
            normalBackground = valueField.getBackground();
            normalForeground = valueField.getForeground();
            add(toggle);
            add(valueField);
            load();
            toggle.addActionListener(e -> {
                Preferences.setPreference(column.getOptionOnKey(), toggle.isSelected());
                valueField.setEnabled(toggle.isSelected());
                onChange.run();
            });
            valueField.addActionListener(e -> commitValue());
            valueField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    commitValue();
                }
            });
            // Valid values apply on every keystroke, so a changed format is
            // visible in the gutter while typing.
            valueField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    commitValue();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    commitValue();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    commitValue();
                }
            });
        }

        void load() {
            loading = true;
            try {
                boolean on = column.getOptionOnKey() != null
                        && Preferences.isPreference(column.getOptionOnKey());
                toggle.setSelected(on);
                valueField.setEnabled(on);
                valueField.setText(column.getOptionValueKey() == null ? ""
                        : Preferences.getPreferenceDefault(column.getOptionValueKey(), ""));
                valueField.setBackground(normalBackground);
                valueField.setForeground(normalForeground);
            } finally {
                loading = false;
            }
        }

        private void commitValue() {
            if (loading) {
                return;
            }
            String text = valueField.getText();
            if (!text.isEmpty() && !isValid(text)) {
                valueField.setBackground(INVALID_BACKGROUND);
                valueField.setForeground(INVALID_FOREGROUND);
                return;
            }
            valueField.setBackground(normalBackground);
            valueField.setForeground(normalForeground);
            Preferences.setPreference(column.getOptionValueKey(), text);
            onChange.run();
        }

        private boolean isValid(String text) {
            if (column.getOption() == ColumnOption.REGEX) {
                try {
                    Pattern.compile(text);
                    return true;
                } catch (PatternSyntaxException ex) {
                    return false;
                }
            }
            try {
                DateTimeFormatter.ofPattern(text);
                return true;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
    }

    /**
     * The per-row width control: a slider and a pixel spinner kept in sync,
     * preset with the effective column width and applied live while dragging
     * or typing; the spinner accepts no invalid input. Shown only for enabled
     * rows.
     */
    static final class WidthSliderPanel extends JPanel {

        private static final int MIN_WIDTH = 8;
        private static final int MAX_WIDTH = 300;

        WidthSliderPanel(Column column, Runnable onChange,
                ToIntFunction<Column> columnWidthProvider, IntSupplier fontSizeProvider) {
            super(new FlowLayout(FlowLayout.LEADING, 2, 1));
            setOpaque(false);
            int current = Math.max(MIN_WIDTH,
                    Math.min(MAX_WIDTH, columnWidthProvider.applyAsInt(column)));
            JSlider slider = new JSlider(MIN_WIDTH, MAX_WIDTH, current);
            slider.setPreferredSize(new Dimension(110, slider.getPreferredSize().height));
            slider.setOpaque(false);
            slider.getAccessibleContext().setAccessibleName(
                    OStrings.getString("GUI_EDITORWINDOW_GUTTER_TABLE_WIDTH") + " " + column.getLabel());
            JSpinner pixels = new JSpinner(new SpinnerNumberModel(current, MIN_WIDTH, MAX_WIDTH, 1));
            pixels.getAccessibleContext().setAccessibleName(
                    OStrings.getString("GUI_EDITORWINDOW_GUTTER_TABLE_WIDTH") + " " + column.getLabel());
            slider.addChangeListener(e -> {
                pixels.setValue(slider.getValue());
                Preferences.setPreference(column.getWidthKey(), slider.getValue());
                // The width scales with the font size it was chosen at.
                Preferences.setPreference(column.getWidthRefKey(), fontSizeProvider.getAsInt());
                onChange.run();
            });
            pixels.addChangeListener(e -> slider.setValue((Integer) pixels.getValue()));
            add(slider);
            add(pixels);
        }
    }

    /** The alignment toggle button of the row, cycling through the values. */
    private static JButton createAlignmentButton(Column column) {
        JButton button = new JButton(OStrings
                .getString("GUI_EDITORWINDOW_GUTTER_ALIGN_" + column.getAlignment().name()));
        button.setMargin(new Insets(0, 4, 0, 4));
        button.getAccessibleContext().setAccessibleName(
                OStrings.getString("GUI_EDITORWINDOW_GUTTER_TABLE_ALIGN") + " " + column.getLabel());
        button.setToolTipText(OStrings.getString("GUI_EDITORWINDOW_GUTTER_TABLE_A11Y"));
        return button;
    }

    /** Shows the alignment toggle of eligible rows. */
    private static final class AlignmentRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            ColumnTableModel model = (ColumnTableModel) table.getModel();
            if (!model.isCellEditable(row, column)) {
                return new JPanel();
            }
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 1));
            panel.setOpaque(false);
            panel.add(createAlignmentButton(model.columnAt(row)));
            return panel;
        }
    }

    /** Cycles the alignment in place. */
    private static final class AlignmentEditor extends AbstractCellEditor
            implements TableCellEditor {

        private final ColumnTableModel model;
        private final Runnable onChange;

        AlignmentEditor(ColumnTableModel model, Runnable onChange) {
            this.model = model;
            this.onChange = onChange;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 1));
            panel.setOpaque(false);
            JButton button = createAlignmentButton(model.columnAt(row));
            button.addActionListener(e -> {
                model.cycleAlignment(row);
                button.setText(OStrings.getString("GUI_EDITORWINDOW_GUTTER_ALIGN_"
                        + model.columnAt(row).getAlignment().name()));
            });
            panel.add(button);
            return panel;
        }
    }

    /** Shows the width slider of enabled rows. */
    private static final class WidthSliderRenderer implements TableCellRenderer {

        private final ToIntFunction<Column> columnWidthProvider;

        WidthSliderRenderer(ToIntFunction<Column> columnWidthProvider) {
            this.columnWidthProvider = columnWidthProvider;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Column gutterColumn = ((ColumnTableModel) table.getModel()).columnAt(row);
            if (!gutterColumn.isEnabled()) {
                return new JPanel();
            }
            return new WidthSliderPanel(gutterColumn, () -> {
            }, columnWidthProvider, () -> 0);
        }
    }

    /** Edits the width slider in place. */
    private static final class WidthSliderEditor extends AbstractCellEditor
            implements TableCellEditor {

        private final ColumnTableModel model;
        private final Runnable onChange;
        private final ToIntFunction<Column> columnWidthProvider;
        private final IntSupplier fontSizeProvider;

        WidthSliderEditor(ColumnTableModel model, Runnable onChange,
                ToIntFunction<Column> columnWidthProvider, IntSupplier fontSizeProvider) {
            this.model = model;
            this.onChange = onChange;
            this.columnWidthProvider = columnWidthProvider;
            this.fontSizeProvider = fontSizeProvider;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            return new WidthSliderPanel(model.columnAt(row), onChange, columnWidthProvider,
                    fontSizeProvider);
        }
    }

    /**
     * The option cell of the length columns: a trim toggle and a non-space
     * toggle, both applied live.
     */
    static final class LengthOptionPanel extends JPanel {

        LengthOptionPanel(Column column, Runnable onChange) {
            super(new FlowLayout(FlowLayout.LEADING, 4, 1));
            setOpaque(false);
            add(createToggle(column.getTrimKey(), "GUI_EDITORWINDOW_GUTTER_OPT_TRIM", onChange));
            add(createToggle(column.getNonSpaceKey(), "GUI_EDITORWINDOW_GUTTER_OPT_NONSPACE",
                    onChange));
        }

        private JCheckBox createToggle(String prefKey, String labelKey, Runnable onChange) {
            JCheckBox box = new JCheckBox(OStrings.getString(labelKey),
                    Preferences.isPreference(prefKey));
            box.setOpaque(false);
            box.addActionListener(e -> {
                Preferences.setPreference(prefKey, box.isSelected());
                onChange.run();
            });
            return box;
        }
    }

    private static Component createOptionCell(Column column, Runnable onChange) {
        switch (column.getOption()) {
        case REGEX:
        case DATE_FORMAT:
            return new OptionCellPanel(column, onChange);
        case LENGTH:
            return new LengthOptionPanel(column, onChange);
        default:
            return new JPanel();
        }
    }

    /** Shows the option cell of rows that offer one. */
    private static final class OptionCellRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return createOptionCell(((ColumnTableModel) table.getModel()).columnAt(row), () -> {
            });
        }
    }

    /** Edits the option cell in place. */
    private static final class OptionCellEditor extends AbstractCellEditor
            implements TableCellEditor {

        private final ColumnTableModel model;
        private final Runnable onChange;

        OptionCellEditor(ColumnTableModel model, Runnable onChange) {
            this.model = model;
            this.onChange = onChange;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            return createOptionCell(model.columnAt(row), onChange);
        }
    }

    /**
     * One row per gutter column in display order: a visibility checkbox, the
     * move buttons, the column name and the column options. Separate from the
     * dialog shell for headless tests.
     */
    static final class ColumnTableModel extends AbstractTableModel {

        private final java.util.List<Column> rows;
        private final Runnable onChange;

        ColumnTableModel(Runnable onChange) {
            this.rows = Column.inDisplayOrder();
            this.onChange = onChange;
        }

        Column columnAt(int row) {
            return rows.get(row);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 6;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
            case 0:
                return OStrings.getString("GUI_EDITORWINDOW_GUTTER_TABLE_SHOW");
            case 1:
                return OStrings.getString("GUI_EDITORWINDOW_GUTTER_TABLE_MOVE");
            case 2:
                return OStrings.getString("GUI_EDITORWINDOW_GUTTER_TABLE_COLUMN");
            case 3:
                return OStrings.getString("GUI_EDITORWINDOW_GUTTER_TABLE_OPTIONS");
            case 4:
                return OStrings.getString("GUI_EDITORWINDOW_GUTTER_TABLE_ALIGN");
            case 5:
                return OStrings.getString("GUI_EDITORWINDOW_GUTTER_TABLE_WIDTH");
            default:
                return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == 0) {
                return true;
            }
            if (column == 3) {
                return rows.get(row).getOption() != ColumnOption.NONE;
            }
            if (column == 4) {
                return rows.get(row).isEnabled() && rows.get(row) != Column.COLOR;
            }
            return column == 5 && rows.get(row).isEnabled();
        }

        @Override
        public Object getValueAt(int row, int column) {
            Column gutterColumn = rows.get(row);
            switch (column) {
            case 0:
                return gutterColumn.isEnabled();
            case 2:
                return gutterColumn.getLabel();
            default:
                return "";
            }
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            if (column == 0) {
                Preferences.setPreference(rows.get(row).getPrefKey(), Boolean.TRUE.equals(value));
                // The width slider of the row appears and disappears with it.
                fireTableRowsUpdated(row, row);
                onChange.run();
            }
        }

        /** Cycles the alignment of the row, also reachable by keyboard. */
        void cycleAlignment(int row) {
            Column column = rows.get(row);
            if (!column.isEnabled() || column == Column.COLOR) {
                return;
            }
            Preferences.setPreference(column.getAlignmentKey(),
                    column.getAlignment().next().name());
            fireTableRowsUpdated(row, row);
            onChange.run();
        }

        /**
         * Moves a row by the given delta and persists the new order. Returns
         * the new row index, negative when nothing moved.
         */
        int move(int row, int delta) {
            int to = row + delta;
            if (row < 0 || row >= rows.size() || to < 0 || to >= rows.size()) {
                return -1;
            }
            rows.add(to, rows.remove(row));
            Column.persistDisplayOrder(rows);
            fireTableDataChanged();
            onChange.run();
            return to;
        }

        /** Moves a row to the drop index and persists the new order. */
        void moveRow(int from, int dropIndex) {
            int to = dropIndex > from ? dropIndex - 1 : dropIndex;
            move(from, to - from);
        }
    }

    /** Moves table rows by drag and drop. */
    private static final class RowMoveHandler extends TransferHandler {

        private final JTable table;
        private final ColumnTableModel model;

        RowMoveHandler(JTable table, ColumnTableModel model) {
            this.table = table;
            this.model = model;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return new StringSelection(String.valueOf(table.getSelectedRow()));
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDrop() && support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            if (table.isEditing()) {
                // Close the open option editor before the rows move.
                table.getCellEditor().stopCellEditing();
            }
            try {
                int from = Integer.parseInt(
                        (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor));
                int dropIndex = ((JTable.DropLocation) support.getDropLocation()).getRow();
                model.moveRow(from, dropIndex);
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
    }
}
