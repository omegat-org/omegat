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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.omegat.util.Preferences;

/**
 * What the segment CSV export writes: segment scope, whether the editor's
 * current filter and display order apply, the columns with their order, and
 * the {@link CsvFormatOptions file format}.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
final class CsvExportOptions {

    enum Scope { PROJECT, CURRENT_FILE }

    private final Scope scope;
    private final boolean applyFilter;
    private final boolean applySort;
    /** All columns in export order, mapped to whether they are exported. */
    private final LinkedHashMap<CsvColumn, Boolean> columnOrder;
    private final CsvFormatOptions format;

    CsvExportOptions(Scope scope, boolean applyFilter, boolean applySort,
            Map<CsvColumn, Boolean> columnOrder, CsvFormatOptions format) {
        this.scope = scope;
        this.applyFilter = applyFilter;
        this.applySort = applySort;
        this.columnOrder = completedOrder(columnOrder);
        this.format = format;
    }

    /** Copies the given order and appends columns it does not mention, unselected. */
    private static LinkedHashMap<CsvColumn, Boolean> completedOrder(Map<CsvColumn, Boolean> order) {
        LinkedHashMap<CsvColumn, Boolean> completed = new LinkedHashMap<>(order);
        for (CsvColumn column : CsvColumn.values()) {
            completed.putIfAbsent(column, false);
        }
        return completed;
    }

    Scope getScope() {
        return scope;
    }

    boolean isApplyFilter() {
        return applyFilter;
    }

    boolean isApplySort() {
        return applySort;
    }

    /** All columns in export order, mapped to whether they are exported. */
    Map<CsvColumn, Boolean> getColumnOrder() {
        return new LinkedHashMap<>(columnOrder);
    }

    /** The exported columns in export order. */
    List<CsvColumn> getSelectedColumns() {
        List<CsvColumn> selected = new ArrayList<>();
        columnOrder.forEach((column, on) -> {
            if (on) {
                selected.add(column);
            }
        });
        return selected;
    }

    CsvFormatOptions getFormat() {
        return format;
    }

    static CsvExportOptions loadFromPreferences() {
        Scope scope = CsvFormatOptions.parseEnum(Scope.class,
                Preferences.getPreferenceDefault(Preferences.EDITOR_CSV_EXPORT_SCOPE, Scope.PROJECT.name()),
                Scope.PROJECT);
        boolean applyFilter = Preferences.isPreferenceDefault(Preferences.EDITOR_CSV_EXPORT_APPLY_FILTER, true);
        boolean applySort = Preferences.isPreferenceDefault(Preferences.EDITOR_CSV_EXPORT_APPLY_SORT, true);
        return new CsvExportOptions(scope, applyFilter, applySort, loadColumnOrder(),
                CsvFormatOptions.loadFromPreferences());
    }

    /**
     * Stored as comma-joined header ids in export order; a {@code -} prefix
     * marks a column that is not exported. Unknown ids are skipped, missing
     * columns appended. No stored value means all columns, declaration order.
     */
    private static LinkedHashMap<CsvColumn, Boolean> loadColumnOrder() {
        LinkedHashMap<CsvColumn, Boolean> order = new LinkedHashMap<>();
        String stored = Preferences.getPreferenceDefault(Preferences.EDITOR_CSV_EXPORT_COLUMNS, "");
        if (stored.isEmpty()) {
            for (CsvColumn column : CsvColumn.values()) {
                order.put(column, true);
            }
            return order;
        }
        for (String name : stored.split(",", -1)) {
            boolean on = !name.startsWith("-");
            String id = on ? name : name.substring(1);
            for (CsvColumn column : CsvColumn.values()) {
                if (column.getHeaderId().equals(id)) {
                    order.putIfAbsent(column, on);
                }
            }
        }
        return completedOrder(order);
    }

    void saveToPreferences() {
        Preferences.setPreference(Preferences.EDITOR_CSV_EXPORT_SCOPE, scope.name());
        Preferences.setPreference(Preferences.EDITOR_CSV_EXPORT_APPLY_FILTER, applyFilter);
        Preferences.setPreference(Preferences.EDITOR_CSV_EXPORT_APPLY_SORT, applySort);
        Preferences.setPreference(Preferences.EDITOR_CSV_EXPORT_COLUMNS, columnOrder.entrySet().stream()
                .map(e -> (e.getValue() ? "" : "-") + e.getKey().getHeaderId())
                .collect(Collectors.joining(",")));
        format.saveToPreferences();
    }
}
