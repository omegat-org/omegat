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
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
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
        // Give the running-number column 8px of horizontal padding so the
        // numbers are not cramped against the column edges, right-aligned.
        DefaultTableCellRenderer numberRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
        numberRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.colorStylesTable.getColumnModel().getColumn(ColorColumns.NUMBER.index)
                .setCellRenderer(numberRenderer);
        // Filter as the user types and let header clicks sort each column. The
        // colour column is special: repeated clicks rotate through several
        // colour orderings (see ColorRowSorter), so give its header a tooltip
        // that explains this. Other columns just toggle ascending/descending.
        JTableHeader tableHeader = new JTableHeader(panel.colorStylesTable.getColumnModel()) {
            @Override
            public String getToolTipText(MouseEvent event) {
                int viewColumn = columnAtPoint(event.getPoint());
                int modelColumn = viewColumn < 0 ? -1
                        : panel.colorStylesTable.convertColumnIndexToModel(viewColumn);
                if (modelColumn == ColorColumns.COLOR.index) {
                    return OStrings.getString("GUI_COLORS_COLUMN_COLOR_SORT_TOOLTIP");
                }
                return null;
            }
        };
        // Pin the columns so the running-number column stays first and cannot be
        // dragged out of place; resizing column widths stays enabled.
        tableHeader.setReorderingAllowed(false);
        panel.colorStylesTable.setTableHeader(tableHeader);
        sorter = new ColorRowSorter(model);
        panel.colorStylesTable.setRowSorter(sorter);
        // Start sorted by the running number, ascending, i.e. in the natural
        // EditorColor enum order.
        sorter.setSortKeys(Collections.singletonList(
                new RowSorter.SortKey(ColorColumns.NUMBER.index, SortOrder.ASCENDING)));
        // Force a minimum width on the colour column so its swatch is never
        // clipped, regardless of the header/content measurement.
        panel.colorStylesTable.getColumnModel().getColumn(ColorColumns.COLOR.index)
                .setMinWidth(rowHeight + 8);
        // The running-number column carries the default sort, so its header also
        // paints a sort arrow that the width measurement does not account for.
        // Reserve room for the title plus the arrow so "#" is never truncated.
        int numberColumnWidth = tableHeader.getFontMetrics(tableHeader.getFont())
                .stringWidth(ColorColumns.NUMBER.getTitle()) + rowHeight + 8;
        panel.colorStylesTable.getColumnModel().getColumn(ColorColumns.NUMBER.index)
                .setMinWidth(numberColumnWidth);
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

    /** Saturation at or below which a colour is treated as grey (achromatic). */
    private static final float ACHROMATIC_SATURATION = 0.05f;

    /**
     * Compares colours lexicographically by their {@code #rrggbb} hex value.
     * Unset colours (null, i.e. "follows the look and feel") sort first.
     */
    static Comparator<Color> hexComparator() {
        return Comparator.comparing(CustomColorSelectionController::colorSortKey);
    }

    /**
     * Compares colours the way a colour wheel groups them: unset colours first,
     * then greys ordered by brightness, then chromatic colours ordered by hue,
     * saturation and brightness. A trailing hex tie-break keeps the order total
     * and stable.
     */
    static Comparator<Color> hsbComparator() {
        return Comparator.comparingInt(CustomColorSelectionController::colorGroup)
                .thenComparingDouble(CustomColorSelectionController::hue)
                .thenComparingDouble(CustomColorSelectionController::saturation)
                .thenComparingDouble(CustomColorSelectionController::brightness)
                .thenComparing(CustomColorSelectionController::colorSortKey);
    }

    /**
     * Compares colours by perceived brightness (Rec. 709 relative luminance),
     * darkest first. Unset colours sort first, with a trailing hex tie-break.
     */
    static Comparator<Color> luminanceComparator() {
        return Comparator.comparingInt(CustomColorSelectionController::nullGroup)
                .thenComparingDouble(CustomColorSelectionController::luminance)
                .thenComparing(CustomColorSelectionController::colorSortKey);
    }

    private static int nullGroup(Color color) {
        return color == null ? 0 : 1;
    }

    private static int colorGroup(Color color) {
        if (color == null) {
            return 0;
        }
        return saturation(color) <= ACHROMATIC_SATURATION ? 1 : 2;
    }

    private static float hue(Color color) {
        return color == null ? 0f
                : Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[0];
    }

    private static float saturation(Color color) {
        return color == null ? 0f
                : Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[1];
    }

    private static float brightness(Color color) {
        return color == null ? 0f
                : Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[2];
    }

    private static double luminance(Color color) {
        return color == null ? 0d
                : 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
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
        NUMBER(0, Integer.class, "GUI_COLORS_COLUMN_NUMBER"),
        NAME(1, String.class, "GUI_COLORS_COLUMN_NAME"), COLOR(2, Color.class, "GUI_COLORS_COLUMN_COLOR"),
        INTERNAL(3, String.class, "GUI_COLORS_COLUMN_INTERNAL");

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

    /**
     * Row sorter whose colour column does not just toggle ascending/descending
     * but rotates through several colour orderings on repeated header clicks:
     * hex, HSB (colour wheel) and luminance, each ascending then descending.
     * Every other column keeps the usual single ascending/descending toggle.
     */
    @SuppressWarnings("serial")
    static class ColorRowSorter extends TableRowSorter<ColorTableModel> {

        /** The (comparator, direction) pairs the colour header cycles through. */
        private static final List<Map.Entry<Comparator<Color>, SortOrder>> COLOR_SORTS = List.of(
                Map.entry(hexComparator(), SortOrder.ASCENDING),
                Map.entry(hexComparator(), SortOrder.DESCENDING),
                Map.entry(hsbComparator(), SortOrder.ASCENDING),
                Map.entry(hsbComparator(), SortOrder.DESCENDING),
                Map.entry(luminanceComparator(), SortOrder.ASCENDING),
                Map.entry(luminanceComparator(), SortOrder.DESCENDING));

        /** Index into {@link #COLOR_SORTS}, or -1 when the colour column is not sorted. */
        private int colorSortState = -1;

        ColorRowSorter(ColorTableModel model) {
            super(model);
            setComparator(ColorColumns.COLOR.index, hexComparator());
        }

        @Override
        public void toggleSortOrder(int column) {
            if (column == ColorColumns.COLOR.index) {
                colorSortState = (colorSortState + 1) % COLOR_SORTS.size();
                Map.Entry<Comparator<Color>, SortOrder> sort = COLOR_SORTS.get(colorSortState);
                setComparator(ColorColumns.COLOR.index, sort.getKey());
                setSortKeys(Collections.singletonList(
                        new RowSorter.SortKey(ColorColumns.COLOR.index, sort.getValue())));
            } else {
                colorSortState = -1;
                super.toggleSortOrder(column);
            }
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
            case NUMBER:
                return rowIndex + 1;
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
