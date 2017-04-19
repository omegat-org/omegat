/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Briac Pilpre
               2015 Aaron Madlon-Kay
               2016 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.gui.preferences.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.table.AbstractTableModel;

import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.gui.Styles.EditorColor;

/**
 * @author Briac Pilpre
 * @author Aaron Madlon-Kay
 */
public class CustomColorSelectionController extends BasePreferencesController {

    private static final int MAX_ROW_COUNT = 10;
    private final Map<EditorColor, Color> temporaryPreferences = new EnumMap<>(EditorColor.class);
    private ColorIcon icon;
    private CustomColorSelectionPanel panel;
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
        panel.colorStylesTable.setModel(new ColorTableModel());
        icon = new ColorIcon(panel.colorStylesTable.getRowHeight());
        panel.resetCurrentColorButton.addActionListener(e -> resetCurrentColor());
    }

    private Optional<EditorColor> getSelection() {
        int row = panel.colorStylesTable.getSelectedRow();
        if (row < 0) {
            return Optional.empty();
        } else {
            ColorTableModel model = ((ColorTableModel) panel.colorStylesTable.getModel());
            return Optional.of(model.getEditorColorAtRow(row));
        }
    }

    private void recordTemporaryPreference() {
        getSelection().ifPresent(style -> {
            temporaryPreferences.put(style, panel.colorChooser.getColor());
            setRestartRequired(hasChanges());
            updateSelectionIcon();
        });
    }

    private void updateSelectionIcon() {
        int row = panel.colorStylesTable.getSelectedRow();
        if (row >= 0) {
            ((ColorTableModel) panel.colorStylesTable.getModel()).fireTableRowsUpdated(row, row);
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
            setRestartRequired(hasChanges());
            updateSelectionIcon();
        });
    }

    private boolean hasChanges() {
        return !temporaryPreferences.entrySet().stream()
                .allMatch(e -> Objects.equals(e.getKey().getColor(), e.getValue()));
    }

    @Override
    public void restoreDefaults() {
        for (EditorColor style : EditorColor.values()) {
            temporaryPreferences.put(style, style.getDefault());
        }
        panel.colorStylesTable.repaint();
        panel.colorStylesTable.clearSelection();
        onSelectionChanged();
        setRestartRequired(hasChanges());
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
    }

    enum ColorColumns {
        NAME(String.class), ICON(Icon.class);

        private final Class<?> clss;

        private ColorColumns(Class<?> clss) {
            this.clss = clss;
        }

        static ColorColumns get(int index) {
            return values()[index];
        }
    }

    static class ColorIcon implements Icon {
        private final int size;
        private Color color;

        public ColorIcon(int size) {
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
            switch(ColorColumns.get(columnIndex)) {
            case NAME:
                return style.getDisplayName();
            case ICON:
                Color color = temporaryPreferences.getOrDefault(style, style.getColor());
                return icon.setColor(color);
            }
            throw new IllegalArgumentException();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return ColorColumns.get(columnIndex).clss;
        }

        public EditorColor getEditorColorAtRow(int row) {
            return EditorColor.values()[row];
        }
    }
}
