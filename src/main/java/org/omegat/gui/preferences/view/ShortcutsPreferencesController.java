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

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;

import org.jspecify.annotations.Nullable;

import org.omegat.core.Core;
import org.omegat.gui.dialogs.ShortcutEditorDialog;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.gui.shortcuts.PropertiesShortcuts;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.TableColumnSizer;
import org.omegat.util.gui.TableSearchField;

/**
 * Preferences view listing every shortcutable function of the main menu, the
 * editor and the autocompleter in one searchable, sortable table, with full
 * keyboard-only management: assign, remove, restore defaults, export and
 * import of the documented shortcut files. Edits are staged in the table
 * model and applied on OK; consumers rebind live.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class ShortcutsPreferencesController extends BasePreferencesController {

    private @Nullable ShortcutsPreferencesPanel panel;
    private @Nullable ShortcutsTableModel model;
    private @Nullable TableRowSorter<ShortcutsTableModel> sorter;

    @Override
    public JComponent getGui() {
        if (panel == null) {
            initGui();
        }
        return panel;
    }

    @Override
    public String toString() {
        return OStrings.getString("PREFS_TITLE_SHORTCUTS");
    }

    private void initGui() {
        panel = new ShortcutsPreferencesPanel();
        model = new ShortcutsTableModel(PropertiesShortcuts.getMainMenuShortcuts(),
                PropertiesShortcuts.getEditorShortcuts(), harvestMenuLabels());
        panel.table.setModel(model);
        panel.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(model);
        panel.table.setRowSorter(sorter);
        // fitTableToWidth=false switches the table to AUTO_RESIZE_OFF, so a
        // narrow viewport gets a horizontal scrollbar instead of squeezing
        // the columns (same as the color table)
        TableColumnSizer colSizer = TableColumnSizer.autoSize(panel.table,
                ShortcutsTableModel.COLUMN_FUNCTION, false);
        // the Scope column never outgrows its content, and no column shrinks
        // while a search narrows the table
        colSizer.capWidthToContent(ShortcutsTableModel.COLUMN_SCOPE);
        colSizer.freezeCurrentWidthsAsMinimum();
        panel.table.setDefaultRenderer(Object.class, new ShortcutStateRenderer());

        TableSearchField search = new TableSearchField(panel.table, sorter);
        search.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 5, 0));
        panel.add(search, java.awt.BorderLayout.NORTH);

        panel.table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                    assignSelected();
                }
            }
        });
        panel.assignButton.addActionListener(e -> assignSelected());
        panel.removeButton.addActionListener(e -> withSelectedRow(row -> model.setShortcut(row, null)));
        panel.restoreButton.addActionListener(e -> withSelectedRow(model::restoreDefault));
        panel.exportButton.addActionListener(e -> exportShortcuts());
        panel.importButton.addActionListener(e -> importShortcuts());

        // Enter on a row = assign, the keyboard-first editing path
        panel.table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("ENTER"), "assignShortcut");
        panel.table.getActionMap().put("assignShortcut", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                assignSelected();
            }
        });
    }

    /**
     * Labels and menu paths of all menu functions, from the live menu bar;
     * empty when no main window exists (console use, tests) - the table then
     * shows raw keys.
     */
    private static Map<String, ShortcutsTableModel.MenuEntry> harvestMenuLabels() {
        Map<String, ShortcutsTableModel.MenuEntry> result = new HashMap<>();
        if (Core.getMainWindow() == null || Core.getMainWindow().getMainMenu() == null) {
            return result;
        }
        JMenuBar menuBar = Core.getMainWindow().getMainMenu().getMenuBar();
        if (menuBar == null) {
            return result;
        }
        for (Component top : menuBar.getComponents()) {
            if (top instanceof JMenu) {
                harvest((JMenu) top, ((JMenu) top).getText(), result);
            }
        }
        return result;
    }

    private static void harvest(JMenu menu, String path,
            Map<String, ShortcutsTableModel.MenuEntry> result) {
        for (Component c : menu.getMenuComponents()) {
            if (c instanceof JMenu) {
                harvest((JMenu) c, path + " > " + ((JMenu) c).getText(), result);
            } else if (c instanceof JMenuItem) {
                JMenuItem item = (JMenuItem) c;
                String key = item.getActionCommand();
                if (!StringUtil.isEmpty(key)) {
                    result.put(key, new ShortcutsTableModel.MenuEntry(item.getText(), path));
                }
            }
        }
    }

    private void withSelectedRow(java.util.function.IntConsumer action) {
        int viewRow = panel.table.getSelectedRow();
        if (viewRow >= 0) {
            action.accept(panel.table.convertRowIndexToModel(viewRow));
        }
    }

    private void assignSelected() {
        withSelectedRow(row -> {
            ShortcutEditorDialog dialog = new ShortcutEditorDialog(model.getStaged(row));
            if (dialog.show(windowOf(panel), model.getFunctionLabel(row))) {
                model.setShortcut(row, dialog.getResult());
                warnOnConflict(row);
            }
        });
    }

    private void warnOnConflict(int row) {
        if (model.getConflictKind(row) == ShortcutsTableModel.ConflictKind.CONFLICT) {
            JOptionPane.showMessageDialog(panel,
                    OStrings.getString("PREFS_SHORTCUTS_CONFLICT_MESSAGE", model.getFunctionLabel(row),
                            model.conflictText(row)),
                    OStrings.getString("PREFS_TITLE_SHORTCUTS"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private void exportShortcuts() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(OStrings.getString("PREFS_SHORTCUTS_EXPORT_TITLE"));
        if (chooser.showSaveDialog(panel) == JFileChooser.APPROVE_OPTION) {
            try {
                model.exportTo(chooser.getSelectedFile());
            } catch (IOException ex) {
                Log.logErrorRB(ex, "PREFS_SHORTCUTS_EXPORT_ERROR");
                JOptionPane.showMessageDialog(panel, OStrings.getString("PREFS_SHORTCUTS_EXPORT_ERROR"),
                        OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importShortcuts() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(OStrings.getString("PREFS_SHORTCUTS_IMPORT_TITLE"));
        if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
            try {
                List<String> rejected = model.importFrom(chooser.getSelectedFile());
                if (!rejected.isEmpty()) {
                    JOptionPane.showMessageDialog(panel,
                            OStrings.getString("PREFS_SHORTCUTS_IMPORT_REJECTED",
                                    String.join(", ", rejected)),
                            OStrings.getString("PREFS_TITLE_SHORTCUTS"), JOptionPane.WARNING_MESSAGE);
                }
            } catch (IOException ex) {
                Log.logErrorRB(ex, "PREFS_SHORTCUTS_IMPORT_ERROR");
                JOptionPane.showMessageDialog(panel, OStrings.getString("PREFS_SHORTCUTS_IMPORT_ERROR"),
                        OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static @Nullable Window windowOf(@Nullable Component c) {
        if (c == null) {
            return KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        }
        return javax.swing.SwingUtilities.getWindowAncestor(c);
    }

    /**
     * Grays out unassigned functions and tints hard conflicts; the states
     * stay readable as text (Shortcut and Conflict columns), the color only
     * reinforces them.
     */
    private class ShortcutStateRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);
            if (model == null || isSelected) {
                return c;
            }
            int modelRow = table.convertRowIndexToModel(row);
            c.setForeground(table.getForeground());
            c.setBackground(table.getBackground());
            if (model.getStaged(modelRow) == null) {
                c.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
            }
            if (model.getConflictKind(modelRow) == ShortcutsTableModel.ConflictKind.CONFLICT) {
                c.setBackground(mix(table.getBackground(), java.awt.Color.RED, 0.15f));
            }
            return c;
        }
    }

    /** Theme-safe tint: blends the accent into the table background. */
    private static java.awt.Color mix(java.awt.Color base, java.awt.Color accent, float ratio) {
        return new java.awt.Color(
                Math.round(base.getRed() * (1 - ratio) + accent.getRed() * ratio),
                Math.round(base.getGreen() * (1 - ratio) + accent.getGreen() * ratio),
                Math.round(base.getBlue() * (1 - ratio) + accent.getBlue() * ratio));
    }

    @Override
    public boolean requiresEditorRefresh() {
        // shortcut edits never change how the editor renders its document;
        // the live rebind handles menus and editor keys itself
        return false;
    }

    @Override
    protected void initFromPrefs() {
        // The model is built from the live shortcut sets in initGui.
    }

    @Override
    public void undoChanges() {
        // The dialog may stay open after Undo, so the staged edits must go.
        if (model != null) {
            model.resetToCurrent();
        }
    }

    @Override
    public void persist() {
        ShortcutsTableModel m = model;
        if (m == null) {
            return;
        }
        // persist runs on the preferences dialog's worker thread; every
        // Swing interaction here has to go through the EDT.
        List<String> hardConflicts = m.hardConflictsInvolvingEdits();
        if (!hardConflicts.isEmpty() && !confirmOnEdt(
                OStrings.getString("PREFS_SHORTCUTS_APPLY_CONFLICTS", String.join(", ", hardConflicts)))) {
            m.resetToCurrent();
            return;
        }
        try {
            m.apply();
        } catch (IOException ex) {
            Log.logErrorRB(ex, "PREFS_SHORTCUTS_SAVE_ERROR");
            onEdt(() -> JOptionPane.showMessageDialog(panel,
                    OStrings.getString("PREFS_SHORTCUTS_SAVE_ERROR"), OStrings.getString("TF_ERROR"),
                    JOptionPane.ERROR_MESSAGE));
            return;
        }
        // The menu rebinds through its change listener; the autocompleter
        // caches its keys per instance and gets a nudge.
        onEdt(() -> {
            if (Core.getEditor() != null && Core.getEditor().getAutoCompleter() != null) {
                Core.getEditor().getAutoCompleter().resetKeys();
            }
        });
    }

    private boolean confirmOnEdt(String message) {
        final boolean[] confirmed = { false };
        onEdtAndWait(() -> confirmed[0] = JOptionPane.showConfirmDialog(panel, message,
                OStrings.getString("PREFS_TITLE_SHORTCUTS"),
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION);
        return confirmed[0];
    }

    private static void onEdt(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    private static void onEdtAndWait(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(r);
        } catch (Exception ex) {
            Log.log(ex);
        }
    }

    @Override
    public void restoreDefaults() {
        // own assignments are about to vanish from the table (persistent
        // only after OK), so this asks first
        if (model != null && (!model.hasModifiedRows()
                || confirmOnEdt(OStrings.getString("PREFS_SHORTCUTS_RESTORE_CONFIRM")))) {
            model.restoreAllDefaults();
        }
    }
}
