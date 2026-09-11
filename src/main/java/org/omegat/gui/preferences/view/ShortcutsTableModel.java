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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;

import org.jspecify.annotations.Nullable;

import org.omegat.gui.shortcuts.PropertiesShortcuts;
import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

/**
 * Working copy of all shortcutable functions for the shortcuts preferences
 * panel. Edits are staged and only reach the shortcut sets (and their user
 * files) in {@link #apply()}. Conflicts are computed over the staged state:
 * two functions of the same shortcut set sharing a keystroke conflict,
 * across sets (menu accelerators work window-wide) it is a warning.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
@SuppressWarnings("serial")
public class ShortcutsTableModel extends AbstractTableModel {

    /** Where a function lives; also the source shortcut set of its row. */
    public enum Scope {
        MENU("PREFS_SHORTCUTS_SCOPE_MENU"),
        EDITOR("PREFS_SHORTCUTS_SCOPE_EDITOR"),
        AUTOCOMPLETER("PREFS_SHORTCUTS_SCOPE_AUTOCOMPLETER");

        private final String nameKey;

        Scope(String nameKey) {
            this.nameKey = nameKey;
        }

        public String getDisplayName() {
            return OStrings.getString(nameKey);
        }
    }

    /** How a staged keystroke collides with other rows. */
    public enum ConflictKind {
        NONE, WARNING, CONFLICT
    }

    /** Menu label and menu path of one menu function, harvested at runtime. */
    public static final class MenuEntry {
        private final String label;
        private final String path;

        public MenuEntry(String label, String path) {
            this.label = label;
            this.path = path;
        }
    }

    static final class Row {
        private final String key;
        private final Scope scope;
        private final String label;
        private final String location;
        private final PropertiesShortcuts set;
        /** Value at panel-open time, so shipped conflicts stay attributable. */
        private final @Nullable KeyStroke original;
        private @Nullable KeyStroke staged;

        private Row(String key, Scope scope, String label, String location, PropertiesShortcuts set,
                @Nullable KeyStroke staged) {
            this.key = key;
            this.scope = scope;
            this.label = label;
            this.location = location;
            this.set = set;
            this.original = staged;
            this.staged = staged;
        }

        private boolean isTouched() {
            return !Objects.equals(staged, original);
        }
    }

    public static final int COLUMN_SCOPE = 0;
    public static final int COLUMN_FUNCTION = 1;
    public static final int COLUMN_LOCATION = 2;
    public static final int COLUMN_SHORTCUT = 3;
    public static final int COLUMN_MODIFIED = 4;
    public static final int COLUMN_CONFLICT = 5;

    private static final String[] COLUMN_KEYS = { "PREFS_SHORTCUTS_COL_SCOPE",
            "PREFS_SHORTCUTS_COL_FUNCTION", "PREFS_SHORTCUTS_COL_LOCATION", "PREFS_SHORTCUTS_COL_SHORTCUT",
            "PREFS_SHORTCUTS_COL_MODIFIED", "PREFS_SHORTCUTS_COL_CONFLICT" };

    private final PropertiesShortcuts menuSet;
    private final PropertiesShortcuts editorSet;
    private final List<Row> rows = new ArrayList<>();

    /**
     * @param menuLabels
     *            menu function labels and paths keyed by action command,
     *            harvested from the live menu bar; functions without an
     *            entry display their raw key
     */
    public ShortcutsTableModel(PropertiesShortcuts menuSet, PropertiesShortcuts editorSet,
            Map<String, MenuEntry> menuLabels) {
        this.menuSet = menuSet;
        this.editorSet = editorSet;
        for (String key : menuSet.getKeys()) {
            MenuEntry entry = menuLabels.get(key);
            String label = entry != null ? entry.label : fallbackLabel(key);
            String location = entry != null ? entry.path : "";
            rows.add(new Row(key, Scope.MENU, label, location, menuSet, parse(menuSet, key)));
        }
        for (String key : editorSet.getKeys()) {
            Scope scope = key.startsWith("autocompleter") ? Scope.AUTOCOMPLETER : Scope.EDITOR;
            rows.add(new Row(key, scope, fallbackLabel(key), "", editorSet, parse(editorSet, key)));
        }
        rows.sort((a, b) -> {
            int c = a.scope.compareTo(b.scope);
            return c != 0 ? c : a.label.compareToIgnoreCase(b.label);
        });
    }

    /**
     * Localized function name of a key without a menu label: dedicated
     * bundle string when present, otherwise the raw key - a user file or a
     * newer version may carry keys this version has no name for.
     */
    private static String fallbackLabel(String key) {
        try {
            return OStrings.getString("SHORTCUT_KEY_" + key);
        } catch (MissingResourceException ex) {
            return key;
        }
    }

    private static @Nullable KeyStroke parse(PropertiesShortcuts set, String key) {
        String value = set.getShortcutValue(key);
        return value == null || value.isEmpty() ? null : KeyStroke.getKeyStroke(value);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_KEYS.length;
    }

    @Override
    public String getColumnName(int column) {
        return OStrings.getString(COLUMN_KEYS[column]);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Row row = rows.get(rowIndex);
        switch (columnIndex) {
        case COLUMN_SCOPE:
            return row.scope.getDisplayName();
        case COLUMN_FUNCTION:
            return row.label;
        case COLUMN_LOCATION:
            return row.location;
        case COLUMN_SHORTCUT:
            return row.staged == null ? OStrings.getString("PREFS_SHORTCUTS_NOT_ASSIGNED")
                    : StaticUIUtils.getKeyStrokeText(row.staged);
        case COLUMN_MODIFIED:
            return isModified(rowIndex) ? OStrings.getString("PREFS_SHORTCUTS_MODIFIED") : "";
        case COLUMN_CONFLICT:
            return conflictText(rowIndex);
        default:
            throw new IllegalArgumentException("Unknown column: " + columnIndex);
        }
    }

    public String getKey(int rowIndex) {
        return rows.get(rowIndex).key;
    }

    public String getFunctionLabel(int rowIndex) {
        return rows.get(rowIndex).label;
    }

    public @Nullable KeyStroke getStaged(int rowIndex) {
        return rows.get(rowIndex).staged;
    }

    /** Stages a keystroke for the row; null unbinds the function. */
    public void setShortcut(int rowIndex, @Nullable KeyStroke ks) {
        rows.get(rowIndex).staged = ks;
        fireRowsUpdated();
    }

    /**
     * Conflicts affect every row, but a full table-changed event would drop
     * the selection - deadly for keyboard-only editing.
     */
    private void fireRowsUpdated() {
        if (!rows.isEmpty()) {
            fireTableRowsUpdated(0, rows.size() - 1);
        }
    }

    /** Discards the staged edits and re-reads the sets' current values. */
    public void resetToCurrent() {
        for (Row row : rows) {
            row.staged = parse(row.set, row.key);
        }
        fireRowsUpdated();
    }

    /**
     * Function labels of hard-conflicting rows whose conflict involves at
     * least one row the user edited in this session. Conflicts the defaults
     * shipped with stay visible in the table but never nag on apply.
     */
    public List<String> hardConflictsInvolvingEdits() {
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (getConflictKind(i) != ConflictKind.CONFLICT) {
                continue;
            }
            boolean touched = row.isTouched();
            if (!touched) {
                for (Row other : rows) {
                    if (other != row && other.scope == row.scope && other.isTouched()
                            && Objects.equals(other.staged, row.staged)) {
                        touched = true;
                        break;
                    }
                }
            }
            if (touched) {
                labels.add(row.label);
            }
        }
        return labels;
    }

    /** Stages the bundled default of the row. */
    public void restoreDefault(int rowIndex) {
        Row row = rows.get(rowIndex);
        String value = row.set.getDefaultValue(row.key);
        row.staged = value == null || value.isEmpty() ? null : KeyStroke.getKeyStroke(value);
        fireRowsUpdated();
    }

    /** Stages the bundled defaults of all rows. */
    public void restoreAllDefaults() {
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            String value = row.set.getDefaultValue(row.key);
            row.staged = value == null || value.isEmpty() ? null : KeyStroke.getKeyStroke(value);
        }
        fireRowsUpdated();
    }

    /** Whether any row currently differs from its bundled default. */
    public boolean hasModifiedRows() {
        for (int i = 0; i < rows.size(); i++) {
            if (isModified(i)) {
                return true;
            }
        }
        return false;
    }

    /** Whether the staged keystroke differs from the bundled default. */
    public boolean isModified(int rowIndex) {
        Row row = rows.get(rowIndex);
        String value = row.set.getDefaultValue(row.key);
        KeyStroke def = value == null || value.isEmpty() ? null : KeyStroke.getKeyStroke(value);
        return !Objects.equals(row.staged, def);
    }

    public ConflictKind getConflictKind(int rowIndex) {
        Row row = rows.get(rowIndex);
        if (row.staged == null) {
            return ConflictKind.NONE;
        }
        ConflictKind kind = ConflictKind.NONE;
        for (Row other : rows) {
            if (other != row && row.staged.equals(other.staged)) {
                // Hardness follows the Scope column, not the backing file:
                // editor and autocompleter keys share a file but never the
                // same context (the bundled defaults reuse ENTER, INSERT and
                // the arrow keys across those scopes on purpose).
                if (other.scope == row.scope) {
                    return ConflictKind.CONFLICT;
                }
                kind = ConflictKind.WARNING;
            }
        }
        return kind;
    }

    /**
     * Names the other functions sharing the row's staged keystroke, or ""
     * when there are none.
     */
    public String conflictText(int rowIndex) {
        Row row = rows.get(rowIndex);
        if (row.staged == null) {
            return "";
        }
        StringBuilder others = new StringBuilder();
        for (Row other : rows) {
            if (other != row && row.staged.equals(other.staged)) {
                if (others.length() > 0) {
                    others.append(", ");
                }
                others.append(other.label);
            }
        }
        if (others.length() == 0) {
            return "";
        }
        String key = getConflictKind(rowIndex) == ConflictKind.CONFLICT ? "PREFS_SHORTCUTS_CONFLICT_WITH"
                : "PREFS_SHORTCUTS_SHARED_WITH";
        return OStrings.getString(key, others.toString());
    }

    /**
     * Writes the staged state into the shortcut sets and persists their user
     * files; consumers rebind through the sets' change listeners.
     */
    public void apply() throws IOException {
        boolean menuTouched = false;
        boolean editorTouched = false;
        for (Row row : rows) {
            if (!Objects.equals(row.staged, parse(row.set, row.key))) {
                row.set.setShortcut(row.key, row.staged);
                if (row.set == menuSet) {
                    menuTouched = true;
                } else {
                    editorTouched = true;
                }
            }
        }
        if (menuTouched) {
            menuSet.save();
        }
        if (editorTouched) {
            editorSet.save();
        }
    }

    /**
     * Exports the staged state of both sets as the documented user files
     * into the given directory, complete and thus usable standalone.
     */
    public void exportTo(File directory) throws IOException {
        writeScope(new File(directory, "MainMenuShortcuts.properties"), menuSet);
        writeScope(new File(directory, "EditorShortcuts.properties"), editorSet);
    }

    private void writeScope(File file, PropertiesShortcuts set) throws IOException {
        Map<String, String> values = new TreeMap<>();
        for (Row row : rows) {
            if (row.set == set) {
                values.put(row.key, PropertiesShortcuts.toPropertyValue(row.staged));
            }
        }
        try (BufferedWriter out = Files.newBufferedWriter(file.toPath(), StandardCharsets.ISO_8859_1)) {
            out.write("# OmegaT shortcuts, exported from the preferences dialog.");
            out.newLine();
            for (Map.Entry<String, String> entry : values.entrySet()) {
                out.write(entry.getKey() + "=" + entry.getValue());
                out.newLine();
            }
        }
    }

    /**
     * Stages the entries of a shortcut properties file (either documented
     * format); keys are matched to their scope by the catalogs.
     *
     * @return the keys the file carried but no catalog knows, plus the keys
     *         with unparseable values - for the user's report
     */
    public List<String> importFrom(File file) throws IOException {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(file)) {
            props.load(in);
        }
        List<String> rejected = new ArrayList<>();
        for (String key : new TreeMap<>(props).keySet().stream().map(Object::toString).toList()) {
            String value = props.getProperty(key);
            KeyStroke ks = value.isEmpty() ? null : KeyStroke.getKeyStroke(value);
            if (!value.isEmpty() && ks == null) {
                rejected.add(key + "=" + value);
                continue;
            }
            Row target = null;
            for (Row row : rows) {
                if (row.key.equals(key)) {
                    target = row;
                    break;
                }
            }
            if (target == null) {
                rejected.add(key);
            } else {
                target.staged = ks;
            }
        }
        fireRowsUpdated();
        return rejected;
    }
}
