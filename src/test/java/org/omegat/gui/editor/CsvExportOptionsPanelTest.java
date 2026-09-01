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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.junit.Before;
import org.junit.Test;

import org.omegat.gui.editor.CsvExportOptions.Scope;
import org.omegat.gui.editor.CsvFormatOptions.CsvCharset;
import org.omegat.gui.editor.CsvFormatOptions.QuoteEscape;
import org.omegat.gui.editor.CsvFormatOptions.SeparatorChoice;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;

/**
 * Tests for the CSV export accessory panel: preferences round trip, column
 * order editing and the enablement rules of the filter and sort checkboxes.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class CsvExportOptionsPanelTest {

    @Before
    public void setUp() throws Exception {
        TestPreferencesInitializer.init();
    }

    private static Map<CsvColumn, Boolean> allSelected(CsvColumn... order) {
        Map<CsvColumn, Boolean> columns = new LinkedHashMap<>();
        for (CsvColumn column : order) {
            columns.put(column, true);
        }
        return columns;
    }

    private static CsvFormatOptions format(CsvCharset charset, SeparatorChoice separator, char custom) {
        return new CsvFormatOptions(charset, separator, custom, false, false, QuoteEscape.DOUBLED);
    }

    private static AbstractButton requireButton(Container root, String labelKey) {
        String label = OStrings.getString(labelKey);
        AbstractButton button = searchButton(root, label);
        if (button == null) {
            throw new AssertionError("button not found: " + labelKey);
        }
        return button;
    }

    private static AbstractButton searchButton(Container root, String label) {
        for (Component component : root.getComponents()) {
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                if (label.equals(button.getText()) || label.equals(button.getToolTipText())) {
                    return button;
                }
            }
            if (component instanceof Container) {
                AbstractButton nested = searchButton((Container) component, label);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static <T extends Component> T requireComponent(Container root, Class<T> type) {
        T found = searchComponent(root, type);
        if (found == null) {
            throw new AssertionError("component not found: " + type.getSimpleName());
        }
        return found;
    }

    private static <T extends Component> T searchComponent(Container root, Class<T> type) {
        for (Component component : root.getComponents()) {
            if (type.isInstance(component)) {
                return type.cast(component);
            }
            if (component instanceof Container) {
                T nested = searchComponent((Container) component, type);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    @Test
    public void testPreferencesRoundTrip() {
        new CsvExportOptions(Scope.CURRENT_FILE, false, false,
                allSelected(CsvColumn.SOURCE, CsvColumn.TARGET),
                new CsvFormatOptions(CsvCharset.UTF_16LE_BOM, SeparatorChoice.OTHER, '|', true, true,
                        QuoteEscape.BACKSLASH)).saveToPreferences();

        CsvExportOptionsPanel panel = new CsvExportOptionsPanel(true);
        CsvExportOptions options = panel.getOptions();

        assertEquals(Scope.CURRENT_FILE, options.getScope());
        assertFalse(options.isApplyFilter());
        assertFalse(options.isApplySort());
        assertEquals(List.of(CsvColumn.SOURCE, CsvColumn.TARGET), options.getSelectedColumns());
        assertEquals(CsvCharset.UTF_16LE_BOM, options.getFormat().getCharset());
        assertEquals(SeparatorChoice.OTHER, options.getFormat().getSeparatorChoice());
        assertEquals('|', options.getFormat().getSeparator());
        assertTrue(options.getFormat().isQuoteAll());
        assertTrue(options.getFormat().isEscapeNewlines());
        assertEquals(QuoteEscape.BACKSLASH, options.getFormat().getQuoteEscape());
    }

    @Test
    public void testFirstRunDefaults() {
        CsvExportOptions options = new CsvExportOptionsPanel(false).getOptions();
        assertEquals(Scope.PROJECT, options.getScope());
        assertEquals(Arrays.asList(CsvColumn.values()), options.getSelectedColumns());
        assertEquals(CsvCharset.UTF_8, options.getFormat().getCharset());
        assertEquals(',', options.getFormat().getSeparator());
        assertFalse(options.getFormat().isQuoteAll());
        assertFalse(options.getFormat().isEscapeNewlines());
        assertEquals(QuoteEscape.DOUBLED, options.getFormat().getQuoteEscape());
    }

    @Test
    public void testColumnOrderRoundTrip() {
        Map<CsvColumn, Boolean> order = new LinkedHashMap<>();
        order.put(CsvColumn.TARGET, true);
        order.put(CsvColumn.SEG_NUM, false);
        order.put(CsvColumn.SOURCE, true);
        new CsvExportOptions(Scope.PROJECT, false, false, order,
                format(CsvCharset.UTF_8, SeparatorChoice.COMMA, ',')).saveToPreferences();

        CsvExportOptions reloaded = CsvExportOptions.loadFromPreferences();
        assertEquals(List.of(CsvColumn.TARGET, CsvColumn.SOURCE), reloaded.getSelectedColumns());
        // Order keeps the unselected column and appends the unmentioned rest.
        List<CsvColumn> fullOrder = List.copyOf(reloaded.getColumnOrder().keySet());
        assertEquals(List.of(CsvColumn.TARGET, CsvColumn.SEG_NUM, CsvColumn.SOURCE), fullOrder.subList(0, 3));
        assertEquals(CsvColumn.values().length, fullOrder.size());
        assertFalse(reloaded.getColumnOrder().get(CsvColumn.SEG_NUM));
        assertFalse(reloaded.getColumnOrder().get(CsvColumn.NOTE));
    }

    @Test
    public void testEmptyColumnSelectionSurvivesRoundTrip() {
        Map<CsvColumn, Boolean> none = new LinkedHashMap<>();
        for (CsvColumn column : CsvColumn.values()) {
            none.put(column, false);
        }
        new CsvExportOptions(Scope.PROJECT, false, false, none,
                format(CsvCharset.UTF_8, SeparatorChoice.COMMA, ',')).saveToPreferences();
        assertEquals(List.of(), CsvExportOptions.loadFromPreferences().getSelectedColumns());
    }

    @Test
    public void testMoveButtonsReorderColumns() {
        CsvExportOptionsPanel panel = new CsvExportOptionsPanel(false);
        JTable table = requireComponent(panel, JTable.class);
        List<CsvColumn> initial = List.copyOf(panel.getOptions().getColumnOrder().keySet());

        table.setRowSelectionInterval(1, 1);
        requireButton(panel, "GUI_EDITORWINDOW_EXPORT_CSV_COL_UP").doClick();
        List<CsvColumn> moved = List.copyOf(panel.getOptions().getColumnOrder().keySet());
        assertEquals(initial.get(1), moved.get(0));
        assertEquals(initial.get(0), moved.get(1));
        assertEquals(0, table.getSelectedRow());

        // Top row cannot move further up.
        requireButton(panel, "GUI_EDITORWINDOW_EXPORT_CSV_COL_UP").doClick();
        assertEquals(moved, List.copyOf(panel.getOptions().getColumnOrder().keySet()));

        requireButton(panel, "GUI_EDITORWINDOW_EXPORT_CSV_COL_DOWN").doClick();
        assertEquals(initial, List.copyOf(panel.getOptions().getColumnOrder().keySet()));
    }

    @Test
    public void testFilterCheckboxDisabledWithoutActiveFilter() {
        CsvExportOptionsPanel panel = new CsvExportOptionsPanel(false);
        AbstractButton applyFilter = requireButton(panel, "GUI_EDITORWINDOW_EXPORT_CSV_APPLY_FILTER");
        assertFalse(applyFilter.isEnabled());
        // A remembered filter preference must not apply without a filter.
        Preferences.setPreference(Preferences.EDITOR_CSV_EXPORT_APPLY_FILTER, true);
        assertFalse(new CsvExportOptionsPanel(false).getOptions().isApplyFilter());
    }

    @Test
    public void testSortCheckboxEnablement() {
        Preferences.setPreference(Preferences.EDITOR_CSV_EXPORT_SCOPE, Scope.CURRENT_FILE.name());
        Preferences.setPreference(Preferences.EDITOR_CSV_EXPORT_APPLY_FILTER, true);
        Preferences.setPreference(Preferences.EDITOR_CSV_EXPORT_APPLY_SORT, true);

        CsvExportOptionsPanel panel = new CsvExportOptionsPanel(true);
        AbstractButton applyFilter = requireButton(panel, "GUI_EDITORWINDOW_EXPORT_CSV_APPLY_FILTER");
        AbstractButton applySort = requireButton(panel, "GUI_EDITORWINDOW_EXPORT_CSV_APPLY_SORT");
        AbstractButton scopeProject = requireButton(panel, "GUI_EDITORWINDOW_EXPORT_CSV_SCOPE_PROJECT");
        AbstractButton scopeCurrent = requireButton(panel, "GUI_EDITORWINDOW_EXPORT_CSV_SCOPE_CURRENT_FILE");

        // Current document, filter applied: sort available.
        assertTrue(applySort.isEnabled());
        assertTrue(panel.getOptions().isApplySort());

        // Deselecting the filter while it is active leaves the order of the
        // filtered-out segments undefined: sort unavailable and not applied.
        applyFilter.doClick();
        assertFalse(applySort.isEnabled());
        assertFalse(panel.getOptions().isApplySort());
        applyFilter.doClick();
        assertTrue(applySort.isEnabled());

        // Whole project: sort unavailable.
        scopeProject.doClick();
        assertFalse(applySort.isEnabled());
        assertFalse(panel.getOptions().isApplySort());
        scopeCurrent.doClick();
        assertTrue(applySort.isEnabled());

        // Without an active filter the sort no longer depends on the filter box.
        CsvExportOptionsPanel noFilter = new CsvExportOptionsPanel(false);
        assertTrue(requireButton(noFilter, "GUI_EDITORWINDOW_EXPORT_CSV_APPLY_SORT").isEnabled());
    }

    @Test
    public void testCustomSeparatorFieldEnablement() {
        CsvFormatOptionsPanel panel = new CsvFormatOptionsPanel();
        JTextField field = requireComponent(panel, JTextField.class);
        AbstractButton comma = requireButton(panel, "GUI_EDITORWINDOW_CSV_SEPARATOR_COMMA");
        AbstractButton other = requireButton(panel, "GUI_EDITORWINDOW_CSV_SEPARATOR_OTHER");

        comma.doClick();
        assertFalse(field.isEnabled());
        other.doClick();
        assertTrue(field.isEnabled());

        field.setText("");
        assertEquals(CsvFormatOptions.DEFAULT_CUSTOM_SEPARATOR, panel.getOptions().getSeparator());
        field.setText("|");
        assertEquals('|', panel.getOptions().getSeparator());

        // Quote and surrogates would break the format: comma fallback. Line
        // breaks never reach the validation, the text field turns them into
        // spaces.
        for (String broken : new String[] {"\"", "😀"}) {
            field.setText(broken);
            assertEquals(CsvFormatOptions.DEFAULT_CUSTOM_SEPARATOR, panel.getOptions().getSeparator());
        }
    }
}
