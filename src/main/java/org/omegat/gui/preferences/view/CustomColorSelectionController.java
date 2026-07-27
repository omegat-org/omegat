/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Briac Pilpre
               2015 Aaron Madlon-Kay
               2016 Aaron Madlon-Kay
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.omegat.core.CoreEvents;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.gui.preferences.PreferencesWindowController;
import org.omegat.util.OStrings;
import org.omegat.util.gui.Styles.EditorColor;
import org.omegat.util.gui.TableColumnSizer;

/**
 * @author Briac Pilpre
 * @author Aaron Madlon-Kay
 */
public class CustomColorSelectionController extends BasePreferencesController {

    private static final int MAX_ROW_COUNT = 10;
    private final Map<EditorColor, Color> temporaryPreferences = new EnumMap<>(EditorColor.class);
    private CustomColorSelectionPanel panel;
    private TableRowSorter<ColorTableModel> sorter;
    private boolean listenerEnabled = true;

    @Override
    public JComponent getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    @Override
    public String toString() {
        return OStrings.getString("PREFS_TITLE_COLORS");
    }

    private void initGui() {
        panel = new CustomColorSelectionPanel();
        try {
            removeTransparencySlider(panel.colorChooser);
        } catch (Exception e) {
            // Ignore
        }
        panel.colorChooser.getSelectionModel().addChangeListener(e -> {
            if (listenerEnabled) {
                recordTemporaryPreference();
            }
        });
        panel.colorStylesTable.getSelectionModel().addListSelectionListener(e -> onSelectionChanged());
        Dimension tableSize = panel.colorStylesTable.getPreferredSize();
        panel.colorStylesTable.setPreferredScrollableViewportSize(
                new Dimension(tableSize.width, panel.colorStylesTable.getRowHeight() * MAX_ROW_COUNT));
        ColorTableModel model = new ColorTableModel();
        panel.colorStylesTable.setModel(model);
        int rowHeight = panel.colorStylesTable.getRowHeight();
        ColorCellRenderer textRenderer = new ColorCellRenderer(0);
        ColorCellRenderer swatchRenderer = new ColorCellRenderer(rowHeight);
        panel.colorStylesTable.getColumnModel().getColumn(ColorColumns.NAME.index)
                .setCellRenderer(textRenderer);
        panel.colorStylesTable.getColumnModel().getColumn(ColorColumns.COLOR.index)
                .setCellRenderer(swatchRenderer);
        panel.colorStylesTable.getColumnModel().getColumn(ColorColumns.INTERNAL.index)
                .setCellRenderer(textRenderer);
        // Filter as the user types and let header clicks sort each column; the
        // colour column sorts by its hex value.
        sorter = new TableRowSorter<>(model);
        sorter.setComparator(ColorColumns.COLOR.index,
                Comparator.comparing(CustomColorSelectionController::colorSortKey));
        panel.colorStylesTable.setRowSorter(sorter);
        // Start sorted by the internal identifier, ascending.
        sorter.setSortKeys(Collections.singletonList(
                new RowSorter.SortKey(ColorColumns.INTERNAL.index, SortOrder.ASCENDING)));
        // Force a minimum width on the colour column so its swatch is never
        // clipped, regardless of the header/content measurement.
        panel.colorStylesTable.getColumnModel().getColumn(ColorColumns.COLOR.index)
                .setMinWidth(rowHeight + 8);
        // Size every column to its own content (the item column ends up as wide
        // as its widest text cell) and turn off auto-resize, so the layout does
        // not depend on the viewport width - it renders correctly even with no
        // project open. The long internal-name column is the remainder, so it is
        // fully sized from the start (no leading "...") and extends past the
        // right edge, reachable via the horizontal scroll bar.
        TableColumnSizer.autoSize(panel.colorStylesTable, ColorColumns.INTERNAL.index, false);
        panel.searchTextField.getDocument().addDocumentListener(new DocumentListener() {
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
        panel.clearSearchButton.setToolTipText(OStrings.getString("KEYSTROKE_EDITOR_CLEAR_BUTTON"));
        panel.clearSearchButton.addActionListener(e -> panel.searchTextField.setText(""));
        applyFilter();
        panel.resetCurrentColorButton.addActionListener(e -> resetCurrentColor());
    }

    private void applyFilter() {
        String text = panel.searchTextField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            // A row matches when the query hits either the item name or the
            // internal identifier.
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text),
                    ColorColumns.NAME.index, ColorColumns.INTERNAL.index));
        }
        panel.clearSearchButton.setEnabled(!text.isEmpty());
        int shown = panel.colorStylesTable.getRowCount();
        int total = panel.colorStylesTable.getModel().getRowCount();
        panel.matchCountLabel.setText(OStrings.getString("GUI_COLORS_MATCH_COUNT", shown, total));
    }

    static String colorSortKey(Color color) {
        return color == null ? "" : String.format("#%02x%02x%02x", color.getRed(), color.getGreen(),
                color.getBlue());
    }

    private Optional<EditorColor> getSelection() {
        int row = panel.colorStylesTable.getSelectedRow();
        if (row < 0) {
            return Optional.empty();
        } else {
            int modelRow = panel.colorStylesTable.convertRowIndexToModel(row);
            ColorTableModel model = ((ColorTableModel) panel.colorStylesTable.getModel());
            return Optional.of(model.getEditorColorAtRow(modelRow));
        }
    }

    private void recordTemporaryPreference() {
        getSelection().ifPresent(style -> {
            temporaryPreferences.put(style, panel.colorChooser.getColor());
            updateSelectionIcon();
        });
    }

    private void updateSelectionIcon() {
        int row = panel.colorStylesTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = panel.colorStylesTable.convertRowIndexToModel(row);
            ((ColorTableModel) panel.colorStylesTable.getModel()).fireTableRowsUpdated(modelRow, modelRow);
        }
    }

    // Hide the Transparency Slider.
    // From: http://stackoverflow.com/a/22608885
    private static void removeTransparencySlider(JColorChooser jc) throws Exception {

        AbstractColorChooserPanel[] colorPanels = jc.getChooserPanels();
        for (int i = 1; i < colorPanels.length; i++) {
            AbstractColorChooserPanel cp = colorPanels[i];

            Field f = cp.getClass().getDeclaredField("panel");
            f.setAccessible(true);

            Object colorPanel = f.get(cp);
            Field f2 = colorPanel.getClass().getDeclaredField("spinners");
            f2.setAccessible(true);
            Object spinners = f2.get(colorPanel);

            Object transpSlispinner = Array.get(spinners, 3);
            if (i == colorPanels.length - 1) {
                transpSlispinner = Array.get(spinners, 4);
            }
            Field f3 = transpSlispinner.getClass().getDeclaredField("slider");
            f3.setAccessible(true);
            JSlider slider = (JSlider) f3.get(transpSlispinner);
            slider.setEnabled(false);
            slider.setVisible(false);
            Field f4 = transpSlispinner.getClass().getDeclaredField("spinner");
            f4.setAccessible(true);
            JSpinner spinner = (JSpinner) f4.get(transpSlispinner);
            spinner.setEnabled(false);
            spinner.setVisible(false);

            Field f5 = transpSlispinner.getClass().getDeclaredField("label");
            f5.setAccessible(true);
            JLabel label = (JLabel) f5.get(transpSlispinner);
            label.setVisible(false);
        }
    }

    private void setColorChooserWithoutNotifying(Color color) {
        listenerEnabled = false;
        panel.colorChooser.setColor(color == null ? Color.BLACK : color);
        listenerEnabled = true;
    }

    private void onSelectionChanged() {
        Optional<EditorColor> selection = getSelection();
        boolean enabled = selection.isPresent();
        panel.colorChooser.setEnabled(enabled);
        panel.resetCurrentColorButton.setEnabled(enabled);
        selection.ifPresent(style -> {
            Color color = temporaryPreferences.getOrDefault(style, style.getColor());
            setColorChooserWithoutNotifying(color);
        });
    }

    private void resetCurrentColor() {
        getSelection().ifPresent(style -> {
            Color defaultColor = style.getDefault();
            if (defaultColor == null) {
                setColorChooserWithoutNotifying(Color.BLACK);
                temporaryPreferences.put(style, null);
            } else {
                panel.colorChooser.setColor(defaultColor);
            }
            // resetting is an explicit action and takes effect immediately
            style.setColor(null);
            PreferencesWindowController.refreshEditorView();
            CoreEvents.fireColorsChanged();
            updateSelectionIcon();
        });
    }

    @Override
    public void restoreDefaults() {
        for (EditorColor style : EditorColor.values()) {
            temporaryPreferences.put(style, style.getDefault());
            // restoring the defaults is an explicit action and takes effect
            // immediately
            style.setColor(null);
        }
        panel.colorStylesTable.repaint();
        panel.colorStylesTable.clearSelection();
        onSelectionChanged();
        PreferencesWindowController.refreshEditorView();
        CoreEvents.fireColorsChanged();
    }

    @Override
    protected void initFromPrefs() {
        temporaryPreferences.clear();
        panel.colorStylesTable.repaint();
        panel.colorStylesTable.clearSelection();
        onSelectionChanged();
    }

    @Override
    public void persist() {
        temporaryPreferences.entrySet().forEach(e -> e.getKey().setColor(e.getValue()));
        CoreEvents.fireColorsChanged();
    }

    enum ColorColumns {
        NAME(0, String.class, "GUI_COLORS_COLUMN_NAME"), COLOR(1, Color.class, "GUI_COLORS_COLUMN_COLOR"),
        INTERNAL(2, String.class, "GUI_COLORS_COLUMN_INTERNAL");

        private final int index;
        private final Class<?> clss;
        private final String titleKey;

        ColorColumns(int index, Class<?> clss, String titleKey) {
            this.index = index;
            this.clss = clss;
            this.titleKey = titleKey;
        }

        String getTitle() {
            return OStrings.getString(titleKey);
        }

        static ColorColumns get(int index) {
            return values()[index];
        }
    }

    /**
     * Renders a cell as plain text or, in swatch mode, as a colour sample.
     */
    @SuppressWarnings("serial")
    static class ColorCellRenderer extends DefaultTableCellRenderer {
        private final ColorIcon swatch;

        ColorCellRenderer(int swatchSize) {
            this.swatch = swatchSize > 0 ? new ColorIcon(swatchSize) : null;
        }

        @Override
        protected void setValue(Object value) {
            // Swatch mode only applies to real colour values. Anything else
            // (e.g. the header text handed in while measuring column widths)
            // falls back to plain text rendering to avoid a class cast.
            if (swatch != null && (value == null || value instanceof Color)) {
                swatch.setColor((Color) value);
                setIcon(swatch);
                setText("");
            } else {
                setIcon(null);
                super.setValue(value);
            }
        }
    }

    static class ColorIcon implements Icon {
        private final int size;
        private Color color;

        ColorIcon(int size) {
            this.size = size;
        }

        public ColorIcon setColor(Color color) {
            this.color = color;
            return this;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (color != null) {
                g.setColor(color);
                g.fillRect(x, y, size, size);
            } else {
                g.setColor(Color.RED);
                g.drawLine(x, y, x + size, y + size);
            }
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

    }

    @SuppressWarnings("serial")
    class ColorTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return EditorColor.values().length;
        }

        @Override
        public int getColumnCount() {
            return ColorColumns.values().length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            EditorColor style = getEditorColorAtRow(rowIndex);
            switch (ColorColumns.get(columnIndex)) {
            case NAME:
                return style.getDisplayName();
            case COLOR:
                return temporaryPreferences.getOrDefault(style, style.getColor());
            case INTERNAL:
                return style.name();
            }
            throw new IllegalArgumentException();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return ColorColumns.get(columnIndex).clss;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return ColorColumns.get(columnIndex).getTitle();
        }

        public EditorColor getEditorColorAtRow(int row) {
            return EditorColor.values()[row];
        }
    }
}
