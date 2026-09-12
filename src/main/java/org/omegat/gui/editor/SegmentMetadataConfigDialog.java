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

import org.jspecify.annotations.Nullable;
import org.openide.awt.Mnemonics;

import org.omegat.core.Core;
import org.omegat.gui.editor.SegmentMetadataGutter.Column;
import org.omegat.gui.editor.SegmentMetadataGutter.ColumnAlignment;
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

    /** The open dialog, refreshed live while a gutter boundary is dragged. */
    private static @Nullable SegmentMetadataConfigDialog openDialog;

    // Heavily shortened project header, like the colour scheme export.
    private static final String[] EXPORT_HEADER = {
        "# OmegaT editor layout",
        "# OmegaT is free/open-source software (GPLv3, https://omegat.org).",
        "# This exported layout is yours: use, share and modify it freely.",
        "# Each line maps one layout preference to its value.",
    };

    private final JLabel totalWidthLabel = new JLabel();
    private final IntSupplier totalWidthProvider;
    private final ToIntFunction<Column> columnWidthProvider;
    private final Runnable onChange;
    private JTable table;
    private JCheckBox showToggle;
    private JCheckBox gridToggle;
    private JCheckBox zebraToggle;

    SegmentMetadataConfigDialog(Frame owner, Runnable externalOnChange,
            ToIntFunction<Column> columnWidthProvider, IntSupplier totalWidthProvider,
            IntSupplier fontSizeProvider) {
        super(owner, OStrings.getString("GUI_EDITORWINDOW_GUTTER_MENU"), false);
        this.totalWidthProvider = totalWidthProvider;
        this.columnWidthProvider = columnWidthProvider;
        setOpenDialog(this);
        setLayout(new BorderLayout());
        // Every change also refreshes the shown total width.
        onChange = () -> {
            externalOnChange.run();
            updateTotalWidthLabel(totalWidthProvider);
        };

        showToggle = new JCheckBox(OStrings.getString("GUI_EDITORWINDOW_GUTTER_SHOW"),
                Preferences.isPreference(Preferences.EDITOR_METADATA_GUTTER));
        showToggle.addActionListener(e -> {
            Preferences.setPreference(Preferences.EDITOR_METADATA_GUTTER,
                    showToggle.isSelected());
            onChange.run();
            // The master switch turns the whole customization on or off.
            realignEditorText();
            relayoutEditorText();
        });
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING));
        top.setBorder(new EmptyBorder(6, 4, 0, 4));
        top.add(showToggle);
        add(top, BorderLayout.NORTH);

        ColumnTableModel model = new ColumnTableModel(onChange);
        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component cell = super.prepareRenderer(renderer, row, column);
                // While the texts are stacked, the target row is mostly
                // inert; its alignment controls stay live and keep their look.
                setEnabledTree(cell,
                        column == 4 || column == 3 || !(model.columnAt(row) == Column.TARGET_TEXT
                                && ColumnTableModel.stacked()));
                return cell;
            }
        };
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
        gridToggle = createGutterToggle("GUI_EDITORWINDOW_GUTTER_GRID",
                Preferences.EDITOR_METADATA_GUTTER_GRID, onChange);
        toggles.add(gridToggle);
        zebraToggle = createGutterToggle("GUI_EDITORWINDOW_GUTTER_ZEBRA",
                Preferences.EDITOR_METADATA_GUTTER_ZEBRA, onChange);
        toggles.add(zebraToggle);
        display.add(toggles, BorderLayout.WEST);
        // The total sits at the trailing edge, beneath the width column.
        updateTotalWidthLabel(totalWidthProvider);
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        totalPanel.add(totalWidthLabel);
        display.add(totalPanel, BorderLayout.EAST);
        columnsBox.add(display, BorderLayout.SOUTH);
        add(columnsBox, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        JButton restore = new JButton();
        Mnemonics.setLocalizedText(restore, OStrings.getString("GUI_EDITORWINDOW_GUTTER_RESTORE"));
        restore.addActionListener(e -> restoreDefaults());
        JButton importButton = new JButton();
        Mnemonics.setLocalizedText(importButton,
                OStrings.getString("GUI_EDITORWINDOW_GUTTER_IMPORT"));
        importButton.addActionListener(e -> importLayout());
        JButton exportButton = new JButton();
        Mnemonics.setLocalizedText(exportButton,
                OStrings.getString("GUI_EDITORWINDOW_GUTTER_EXPORT"));
        exportButton.addActionListener(e -> exportLayout());
        JPanel restorePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        restorePanel.add(restore);
        restorePanel.add(importButton);
        restorePanel.add(exportButton);
        bottom.add(restorePanel, BorderLayout.WEST);
        JButton close = new JButton();
        Mnemonics.setLocalizedText(close, OStrings.getString("BUTTON_CLOSE"));
        close.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        buttons.add(close);
        bottom.add(buttons, BorderLayout.EAST);
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
        int total = totalWidthProvider.getAsInt();
        int columnSum = 0;
        for (Column column : Column.gutterColumns()) {
            if (column.isEnabled()) {
                columnSum += columnWidthProvider.applyAsInt(column);
            }
        }
        totalWidthLabel.setText(OStrings.getString("GUI_EDITORWINDOW_GUTTER_TOTAL_WIDTH")
                + ": ≈ " + total + " px");
        // The shown total exceeds the sum of the width fields a little: the
        // leading inset and the gaps between the columns count too.
        totalWidthLabel.setToolTipText(OStrings.getString(
                "GUI_EDITORWINDOW_GUTTER_TOTAL_WIDTH_TIP", columnSum, total - columnSum));
    }

    @Override
    public void dispose() {
        if (openDialog == this) {
            setOpenDialog(null);
        }
        super.dispose();
    }

    private static void setOpenDialog(@Nullable SegmentMetadataConfigDialog dialog) {
        openDialog = dialog;
    }

    /**
     * Reloads the open dialog from the preferences, e.g. while a column
     * boundary is dragged in the editor: the width cells then follow the
     * mouse. No-op without an open dialog.
     */
    static void refreshOpenDialog() {
        SegmentMetadataConfigDialog dialog = openDialog;
        if (dialog == null || !dialog.isDisplayable()) {
            return;
        }
        if (dialog.table.isEditing()) {
            dialog.table.getCellEditor().cancelCellEditing();
        }
        ((ColumnTableModel) dialog.table.getModel()).reloadRows();
        ((AbstractTableModel) dialog.table.getModel()).fireTableDataChanged();
        dialog.showToggle.setSelected(Preferences.isPreference(Preferences.EDITOR_METADATA_GUTTER));
        dialog.gridToggle
                .setSelected(Preferences.isPreference(Preferences.EDITOR_METADATA_GUTTER_GRID));
        dialog.zebraToggle
                .setSelected(Preferences.isPreference(Preferences.EDITOR_METADATA_GUTTER_ZEBRA));
        dialog.updateTotalWidthLabel(dialog.totalWidthProvider);
    }

    /**
     * All layout preferences with their default values: the classic view of
     * the previous versions, i.e. no metadata column, source above target,
     * leading alignments and filling widths.
     */
    private static java.util.Map<String, String> defaultPreferences() {
        java.util.Map<String, String> defaults = new java.util.LinkedHashMap<>();
        defaults.put(Preferences.EDITOR_METADATA_GUTTER, "false");
        defaults.put(Preferences.EDITOR_LAYOUT_STACKED, "true");
        defaults.put(Preferences.EDITOR_METADATA_GUTTER_ORDER, "");
        defaults.put(Preferences.EDITOR_METADATA_GUTTER_GRID, "false");
        defaults.put(Preferences.EDITOR_METADATA_GUTTER_ZEBRA, "false");
        for (Column column : Column.values()) {
            if (!column.isText()) {
                defaults.put(column.getPrefKey(), String.valueOf(column.isEnabledByDefault()));
            }
            defaults.put(column.getWidthKey(), "0");
            defaults.put(column.getWidthRefKey(), "0");
            defaults.put(column.getAlignmentKey(), column.defaultAlignment().name());
            if (column.isText()) {
                defaults.put(column.getFillWeightKey(), "50");
            }
            if (column.getOptionOnKey() != null && column.getOptionValueKey() != null) {
                defaults.put(column.getOptionOnKey(), "false");
                defaults.put(column.getOptionValueKey(), "");
            }
            if (column.getOption() == ColumnOption.LENGTH) {
                defaults.put(column.getTrimKey(), "false");
                defaults.put(column.getNonSpaceKey(), "false");
            }
        }
        return defaults;
    }

    /**
     * Restores the classic editor layout after a confirmation, which only
     * appears when the current settings actually differ.
     */
    private void restoreDefaults() {
        if (isLayoutApplying()) {
            return;
        }
        java.util.Map<String, String> defaults = defaultPreferences();
        boolean differs = defaults.entrySet().stream().anyMatch(entry -> !Preferences
                .getPreferenceDefault(entry.getKey(), entry.getValue()).equals(entry.getValue()));
        if (!differs) {
            return;
        }
        int answer = javax.swing.JOptionPane.showConfirmDialog(this,
                OStrings.getString("GUI_EDITORWINDOW_GUTTER_RESTORE_CONFIRM"),
                OStrings.getString("GUI_EDITORWINDOW_GUTTER_RESTORE"),
                javax.swing.JOptionPane.YES_NO_OPTION);
        if (answer != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        defaults.forEach(Preferences::setPreference);
        applyLoadedLayout();
    }

    /** Refreshes the dialog and the editor after a bulk settings change. */
    private void applyLoadedLayout() {
        refreshOpenDialog();
        onChange.run();
        realignEditorText();
        relayoutEditorText();
    }

    /** The file chooser of the export and import, in the last used folder. */
    private static javax.swing.JFileChooser layoutChooser(String title) {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                OStrings.getString("GUI_EDITORWINDOW_GUTTER_FILE_DESCRIPTION"), "properties"));
        String lastDirectory = Preferences.getPreference(Preferences.EDITOR_LAYOUT_DIRECTORY);
        if (lastDirectory != null && !lastDirectory.isEmpty()) {
            java.io.File directory = new java.io.File(lastDirectory);
            if (directory.isDirectory()) {
                chooser.setCurrentDirectory(directory);
            }
        }
        return chooser;
    }

    private static void rememberDirectory(java.io.File file) {
        java.io.File directory = file.getParentFile();
        if (directory != null) {
            Preferences.setPreference(Preferences.EDITOR_LAYOUT_DIRECTORY,
                    directory.getAbsolutePath());
        }
    }

    /** Writes all layout preferences to a properties file of choice. */
    private void exportLayout() {
        if (isLayoutApplying()) {
            return;
        }
        String title = Mnemonics
                .removeMnemonics(OStrings.getString("GUI_EDITORWINDOW_GUTTER_EXPORT"));
        javax.swing.JFileChooser chooser = layoutChooser(title);
        chooser.setSelectedFile(new java.io.File(chooser.getCurrentDirectory(),
                "omegat-editor-layout.properties"));
        if (chooser.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(java.util.Locale.ENGLISH).endsWith(".properties")) {
            file = new java.io.File(file.getParentFile(), file.getName() + ".properties");
        }
        if (file.exists() && javax.swing.JOptionPane.showConfirmDialog(this,
                OStrings.getString("GUI_EDITORWINDOW_GUTTER_OVERWRITE_CONFIRM", file.getName()),
                title,
                javax.swing.JOptionPane.YES_NO_OPTION) != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        rememberDirectory(file);
        // A plain, human-readable .properties file in a stable order,
        // loadable with java.util.Properties. Backslashes (e.g. of the
        // regex options) are the only characters that need escaping.
        try (java.io.BufferedWriter out = java.nio.file.Files.newBufferedWriter(file.toPath(),
                java.nio.charset.StandardCharsets.UTF_8)) {
            for (String line : EXPORT_HEADER) {
                out.write(line);
                out.newLine();
            }
            for (java.util.Map.Entry<String, String> entry : defaultPreferences().entrySet()) {
                String value = Preferences.getPreferenceDefault(entry.getKey(),
                        entry.getValue());
                out.write(entry.getKey() + " = " + value.replace("\\", "\\\\"));
                out.newLine();
            }
        } catch (java.io.IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), title,
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads layout preferences from a properties file of choice. Only the
     * known layout keys are applied, anything else in the file is ignored,
     * so the files stay forward-compatible.
     */
    private void importLayout() {
        if (isLayoutApplying()) {
            return;
        }
        String title = Mnemonics
                .removeMnemonics(OStrings.getString("GUI_EDITORWINDOW_GUTTER_IMPORT"));
        javax.swing.JFileChooser chooser = layoutChooser(title);
        if (chooser.showOpenDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File file = chooser.getSelectedFile();
        rememberDirectory(file);
        java.util.Properties properties = new java.util.Properties();
        try (java.io.BufferedReader in = java.nio.file.Files.newBufferedReader(file.toPath(),
                java.nio.charset.StandardCharsets.UTF_8)) {
            properties.load(in);
        } catch (java.io.IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), title,
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        java.util.Map<String, String> known = defaultPreferences();
        long found = known.keySet().stream().filter(properties::containsKey).count();
        if (found == 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    OStrings.getString("GUI_EDITORWINDOW_GUTTER_IMPORT_NONE", file.getName()),
                    title, javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (javax.swing.JOptionPane.showConfirmDialog(this,
                OStrings.getString("GUI_EDITORWINDOW_GUTTER_IMPORT_CONFIRM", file.getName()),
                title,
                javax.swing.JOptionPane.YES_NO_OPTION) != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        for (String key : known.keySet()) {
            String value = properties.getProperty(key);
            if (value != null) {
                Preferences.setPreference(key, sanitizeImportedValue(key, value));
            }
        }
        applyLoadedLayout();
    }

    /**
     * Keeps imported numbers in their working ranges: a hand-edited width
     * of some thousand pixels would grow the gutter beyond the window.
     * Non-numeric keys pass through, their readers fall back to defaults.
     */
    private static String sanitizeImportedValue(String key, String value) {
        for (Column column : Column.values()) {
            if (key.equals(column.getWidthKey())) {
                return String.valueOf(
                        clampInt(value, 0, WidthSliderPanel.MAX_WIDTH, 0));
            }
            if (column.isText() && key.equals(column.getFillWeightKey())) {
                return String.valueOf(clampInt(value, SegmentColumnsView.MIN_CELL_PERCENT,
                        SegmentColumnsView.MAX_CELL_PERCENT, 50));
            }
        }
        return value;
    }

    /** The value as an int within the range, or the fallback. */
    private static int clampInt(String value, int min, int max, int fallback) {
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(value.trim())));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    /** Repaints the table row of the given column in the open dialog. */
    static void refreshRowOf(Column column) {
        SegmentMetadataConfigDialog dialog = openDialog;
        if (dialog == null || !dialog.isDisplayable()) {
            return;
        }
        ColumnTableModel model = (ColumnTableModel) dialog.table.getModel();
        for (int row = 0; row < model.getRowCount(); row++) {
            if (model.columnAt(row) == column) {
                model.fireTableRowsUpdated(row, row);
                return;
            }
        }
    }

    /** Enables or disables a renderer component including its children. */
    private static void setEnabledTree(Component component, boolean enabled) {
        component.setEnabled(enabled);
        if (component instanceof java.awt.Container) {
            for (Component child : ((java.awt.Container) component).getComponents()) {
                setEnabledTree(child, enabled);
            }
        }
    }

    /** True from a layout change until its rendering finished. */
    private static boolean layoutApplying;

    /**
     * Changes arriving during an apply, run afterwards. Coalesced by kind:
     * repeated ticks of one control collapse into the newest, but a realign
     * cannot displace a still pending relayout or the other way round.
     */
    private static final java.util.Map<String, Runnable> pendingLayoutChanges =
            new java.util.LinkedHashMap<>();

    /**
     * Button-like layout controls drop their clicks while an apply still
     * renders, so an accidental second click cannot fire twice.
     */
    static boolean isLayoutApplying() {
        return layoutApplying;
    }

    /**
     * Runs a potentially long layout change busy-gated: the wait cursor
     * shows from the trigger until the pass rendered, the apply runs after
     * the cursor became visible, and the render is forced synchronously, so
     * the release really means done. A change arriving meanwhile (e.g. a
     * width slider tick) replaces any earlier pending one of the same kind
     * and runs once after the release: the last state wins, nothing piles
     * up.
     */
    static void applyLayoutChange(String kind, Runnable change) {
        if (!(Core.getEditor() instanceof EditorController)) {
            return;
        }
        if (layoutApplying) {
            pendingLayoutChanges.put(kind, change);
            return;
        }
        EditorController controller = (EditorController) Core.getEditor();
        layoutApplying = true;
        java.awt.Cursor wait = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR);
        controller.editor.setCursor(wait);
        SegmentMetadataConfigDialog dialog = openDialog;
        if (dialog != null) {
            dialog.setCursor(wait);
        }
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                change.run();
                // Render synchronously, so the release really means done.
                javax.swing.RepaintManager repaints = javax.swing.RepaintManager
                        .currentManager(controller.editor);
                repaints.validateInvalidComponents();
                repaints.paintDirtyRegions();
            } finally {
                controller.editor.setCursor(java.awt.Cursor
                        .getPredefinedCursor(java.awt.Cursor.TEXT_CURSOR));
                if (dialog != null) {
                    dialog.setCursor(java.awt.Cursor.getDefaultCursor());
                }
                layoutApplying = false;
                if (!pendingLayoutChanges.isEmpty()) {
                    String next = pendingLayoutChanges.keySet().iterator().next();
                    applyLayoutChange(next, pendingLayoutChanges.remove(next));
                }
            }
        });
    }

    /** Re-applies the text alignments to the built document, busy-gated. */
    static void realignEditorText() {
        applyLayoutChange("realign", () -> {
            if (Core.getEditor() instanceof EditorController
                    && Core.getProject().isProjectLoaded()) {
                ((EditorController) Core.getEditor()).realignTextParts();
            }
        });
    }

    /**
     * Lays the editor text out again, so changes the views read at layout
     * time (stacked vs. side by side, the text widths) become visible.
     */
    static void relayoutEditorText() {
        applyLayoutChange("relayout", () -> {
            if (!(Core.getEditor() instanceof EditorController)) {
                return;
            }
            EditorController controller = (EditorController) Core.getEditor();
            SegmentColumnsView view = controller.editor.columnsView();
            if (view != null) {
                // The box view caches its layout; a plain revalidate with an
                // unchanged size would keep the stale arrangement.
                view.relayout();
            }
            controller.editor.revalidate();
            controller.editor.repaint();
        });
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

        static final int MIN_WIDTH = 8;
        static final int MAX_WIDTH = 300;

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

    /**
     * The width cell of the text rows: one percent slider per row, coupled
     * with its partner row, so source and target always sum up to one
     * hundred percent of the text width.
     */
    static final class PercentSliderPanel extends JPanel {

        PercentSliderPanel(Column column, Runnable onChange) {
            super(new FlowLayout(FlowLayout.LEADING, 2, 1));
            setOpaque(false);
            int current = SegmentColumnsView.cellPercent(column);
            JSlider slider = new JSlider(SegmentColumnsView.MIN_CELL_PERCENT,
                    SegmentColumnsView.MAX_CELL_PERCENT, current);
            slider.setPreferredSize(new Dimension(110, slider.getPreferredSize().height));
            slider.setOpaque(false);
            slider.getAccessibleContext().setAccessibleName(
                    OStrings.getString("GUI_EDITORWINDOW_GUTTER_TABLE_WIDTH") + " "
                            + column.getLabel());
            JSpinner percent = new JSpinner(
                    new SpinnerNumberModel(current, SegmentColumnsView.MIN_CELL_PERCENT,
                            SegmentColumnsView.MAX_CELL_PERCENT, 1));
            percent.getAccessibleContext().setAccessibleName(
                    OStrings.getString("GUI_EDITORWINDOW_GUTTER_TABLE_WIDTH") + " "
                            + column.getLabel());
            Column partner = column == Column.SOURCE_TEXT ? Column.TARGET_TEXT
                    : Column.SOURCE_TEXT;
            slider.addChangeListener(e -> {
                percent.setValue(slider.getValue());
                Preferences.setPreference(column.getFillWeightKey(), slider.getValue());
                Preferences.setPreference(partner.getFillWeightKey(), 100 - slider.getValue());
                // The partner slider shows the coupled complement.
                refreshRowOf(partner);
                onChange.run();
                relayoutEditorText();
            });
            percent.addChangeListener(e -> slider.setValue((Integer) percent.getValue()));
            add(slider);
            add(percent);
            // The percent sign doubles as the equal-widths reset button.
            JButton equalWidths = new JButton("%");
            equalWidths.setMargin(new Insets(0, 4, 0, 4));
            equalWidths.setToolTipText(
                    OStrings.getString("GUI_EDITORWINDOW_GUTTER_EQUAL_WIDTHS"));
            equalWidths.getAccessibleContext().setAccessibleName(
                    OStrings.getString("GUI_EDITORWINDOW_GUTTER_EQUAL_WIDTHS"));
            // The slider listener persists both shares and relayouts.
            equalWidths.addActionListener(e -> slider.setValue(50));
            add(equalWidths);
        }

        @Override
        public @Nullable String getToolTipText(MouseEvent event) {
            // As a cell renderer this panel is asked for the tooltip; hand
            // the question to the control under the mouse (see
            // PairAlignmentPanel).
            if (!isValid()) {
                setSize(getPreferredSize());
                doLayout();
            }
            Component child = getComponentAt(event.getX(), event.getY());
            if (child instanceof JComponent && child != this) {
                String tip = ((JComponent) child).getToolTipText();
                if (tip != null) {
                    return tip;
                }
            }
            return super.getToolTipText(event);
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
            if (gutterColumn.isText()) {
                if (ColumnTableModel.stacked()) {
                    return new JPanel();
                }
                return new PercentSliderPanel(gutterColumn, () -> {
                });
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
            Column gutterColumn = model.columnAt(row);
            if (gutterColumn.isText()) {
                return new PercentSliderPanel(gutterColumn, onChange);
            }
            return new WidthSliderPanel(gutterColumn, onChange, columnWidthProvider,
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
        case STACKED:
            return new StackedOptionPanel(onChange);
        case PAIR_ALIGNMENT:
            return new PairAlignmentPanel(onChange);
        default:
            return new JPanel();
        }
    }

    /**
     * The option cell of the target text row: one-click alignment patterns
     * for both text rows at once. The schematic labels show the two texts
     * around their boundary; the first alignment goes to the first row of
     * the pair in the configured order, the second to its partner.
     */
    static final class PairAlignmentPanel extends JPanel {

        PairAlignmentPanel(Runnable onChange) {
            super(new FlowLayout(FlowLayout.LEADING, 4, 1));
            setOpaque(false);
            add(createButton("x..|a..", "GUI_EDITORWINDOW_GUTTER_ALIGN_PAIR_LEADING",
                    ColumnAlignment.LEADING, ColumnAlignment.LEADING, onChange));
            add(createButton("..x|..a", "GUI_EDITORWINDOW_GUTTER_ALIGN_PAIR_TRAILING",
                    ColumnAlignment.TRAILING, ColumnAlignment.TRAILING, onChange));
            add(createButton("..x|a..", "GUI_EDITORWINDOW_GUTTER_ALIGN_PAIR_INNER",
                    ColumnAlignment.TRAILING, ColumnAlignment.LEADING, onChange));
            add(createButton("x..|..a", "GUI_EDITORWINDOW_GUTTER_ALIGN_PAIR_OUTER",
                    ColumnAlignment.LEADING, ColumnAlignment.TRAILING, onChange));
            add(createButton(".x.|.a.", "GUI_EDITORWINDOW_GUTTER_ALIGN_PAIR_CENTER",
                    ColumnAlignment.CENTER, ColumnAlignment.CENTER, onChange));
        }

        @Override
        public @Nullable String getToolTipText(MouseEvent event) {
            // As a cell renderer this panel is asked for the tooltip; hand
            // the question to the button under the mouse. The renderer copy
            // is never laid out, so lay it out for the hit test first.
            if (!isValid()) {
                setSize(getPreferredSize());
                doLayout();
            }
            Component child = getComponentAt(event.getX(), event.getY());
            if (child instanceof JComponent && child != this) {
                String tip = ((JComponent) child).getToolTipText();
                if (tip != null) {
                    return tip;
                }
            }
            return super.getToolTipText(event);
        }

        private static JButton createButton(String pattern, String tooltipKey,
                ColumnAlignment first, ColumnAlignment second, Runnable onChange) {
            JButton button = new JButton(pattern);
            button.setMargin(new Insets(0, 4, 0, 4));
            button.setToolTipText(OStrings.getString(tooltipKey));
            button.getAccessibleContext().setAccessibleName(OStrings.getString(tooltipKey));
            button.addActionListener(e -> {
                if (isLayoutApplying()) {
                    return;
                }
                Column firstColumn = SegmentColumnsView.leftCell();
                Column secondColumn = firstColumn == Column.SOURCE_TEXT ? Column.TARGET_TEXT
                        : Column.SOURCE_TEXT;
                Preferences.setPreference(firstColumn.getAlignmentKey(), first.name());
                Preferences.setPreference(secondColumn.getAlignmentKey(), second.name());
                refreshOpenDialog();
                onChange.run();
                realignEditorText();
            });
            return button;
        }
    }

    /**
     * The option cell of the source text row: the classic layout with the
     * source above the translation. While it is on, the source row stands
     * for both texts and the target row is inert.
     */
    static final class StackedOptionPanel extends JPanel {

        StackedOptionPanel(Runnable onChange) {
            super(new FlowLayout(FlowLayout.LEADING, 4, 1));
            setOpaque(false);
            JCheckBox box = new JCheckBox(OStrings.getString("GUI_EDITORWINDOW_GUTTER_OPT_STACKED"),
                    ColumnTableModel.stacked());
            box.setOpaque(false);
            box.addActionListener(e -> {
                if (isLayoutApplying()) {
                    // Dropped like the other layout clicks while busy.
                    box.setSelected(ColumnTableModel.stacked());
                    return;
                }
                Preferences.setPreference(Preferences.EDITOR_LAYOUT_STACKED, box.isSelected());
                onChange.run();
                // The target row of the table greys out or comes back.
                refreshOpenDialog();
                relayoutEditorText();
            });
            add(box);
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

        /**
         * Reloads the row order from the preferences: after an import or a
         * restore the stored order may differ from the shown one, and the
         * next move would persist the stale rows otherwise.
         */
        void reloadRows() {
            rows.clear();
            rows.addAll(Column.inDisplayOrder());
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

        /** The classic layout: the source text above the translation. */
        static boolean stacked() {
            return Preferences.isPreferenceDefault(Preferences.EDITOR_LAYOUT_STACKED, true);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            Column gutterColumn = rows.get(row);
            if (column == 0) {
                // The text rows have no visibility of their own, but while
                // they sit side by side, their box folds the layout back.
                return !gutterColumn.isText() || !stacked();
            }
            if (gutterColumn == Column.TARGET_TEXT && stacked() && column != 4 && column != 3) {
                // Mostly inert while the texts are stacked; only the
                // alignment controls stay configurable, they apply there too.
                return false;
            }
            if (column == 3) {
                return gutterColumn.getOption() != ColumnOption.NONE;
            }
            if (column == 4) {
                return gutterColumn.isEnabled() && gutterColumn != Column.COLOR;
            }
            // The stacked texts fill the editor width, so no width control.
            return column == 5 && gutterColumn.isEnabled()
                    && !(gutterColumn.isText() && stacked());
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
            if (column != 0) {
                return;
            }
            Column gutterColumn = rows.get(row);
            if (gutterColumn.isText()) {
                if (isLayoutApplying()) {
                    return;
                }
                // Unchecking a text row folds the layout back to the classic
                // stacked view; the text rows themselves always stay on.
                Preferences.setPreference(Preferences.EDITOR_LAYOUT_STACKED, true);
                fireTableDataChanged();
                onChange.run();
                relayoutEditorText();
                return;
            }
            Preferences.setPreference(gutterColumn.getPrefKey(), Boolean.TRUE.equals(value));
            // The width slider of the row appears and disappears with it.
            fireTableRowsUpdated(row, row);
            onChange.run();
        }

        /** Cycles the alignment of the row, also reachable by keyboard. */
        void cycleAlignment(int row) {
            Column column = rows.get(row);
            if (!column.isEnabled() || column == Column.COLOR) {
                return;
            }
            // Debounced by the busy state: while the previous layout change
            // still applies and renders, another click must not cycle again.
            if (column.isText() && isLayoutApplying()) {
                return;
            }
            Preferences.setPreference(column.getAlignmentKey(),
                    column.getAlignment().next().name());
            fireTableRowsUpdated(row, row);
            onChange.run();
            if (column.isText()) {
                // The text alignments are baked into the paragraphs.
                realignEditorText();
            }
        }

        /**
         * Moves a row by the given delta and persists the new order. Returns
         * the new row index, negative when nothing moved. The two text rows
         * stay an adjacent pair at the start or the end of the table: they
         * swap with each other, a farther move carries the pair to the other
         * end, and the metadata rows never land between or beyond them.
         */
        int move(int row, int delta) {
            if (row < 0 || row >= rows.size() || delta == 0) {
                return -1;
            }
            boolean textMove = rows.get(row).isText();
            // The arrow buttons and keys drop their clicks while a previous
            // layout change still renders, like the alignment does.
            if (textMove && isLayoutApplying()) {
                return -1;
            }
            int moved = textMove ? moveTextRow(row, delta) : moveMetadataRow(row, delta);
            if (moved >= 0) {
                Column.persistDisplayOrder(rows);
                fireTableDataChanged();
                onChange.run();
                if (textMove) {
                    // The pair order is a layout property of the editor.
                    relayoutEditorText();
                }
            }
            return moved;
        }

        private int moveTextRow(int row, int delta) {
            int partner = rows.indexOf(rows.get(row) == Column.SOURCE_TEXT ? Column.TARGET_TEXT
                    : Column.SOURCE_TEXT);
            int to = row + delta;
            if (to == partner) {
                java.util.Collections.swap(rows, row, partner);
                return to;
            }
            // A farther move carries the pair to the other end of the table,
            // which flips the metadata columns to the other side of the text.
            boolean pairAtStart = rows.get(0).isText();
            if (pairAtStart != (delta > 0) || to < 0 || to >= rows.size()) {
                return -1;
            }
            Column movingColumn = rows.get(row);
            java.util.List<Column> pair = new java.util.ArrayList<>(
                    pairAtStart ? rows.subList(0, 2) : rows.subList(rows.size() - 2, rows.size()));
            rows.removeAll(pair);
            rows.addAll(pairAtStart ? rows.size() : 0, pair);
            return rows.indexOf(movingColumn);
        }

        private int moveMetadataRow(int row, int delta) {
            // Clamped to the metadata block on its side of the text pair.
            boolean pairAtStart = rows.get(0).isText();
            int lowest = pairAtStart ? 2 : 0;
            int highest = pairAtStart ? rows.size() - 1 : rows.size() - 3;
            int to = Math.max(lowest, Math.min(highest, row + delta));
            if (to == row) {
                return -1;
            }
            rows.add(to, rows.remove(row));
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
