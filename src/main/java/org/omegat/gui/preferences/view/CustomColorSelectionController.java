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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

import org.openide.awt.Mnemonics;

import org.omegat.core.CoreEvents;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.Styles.EditorColor;

/**
 * @author Briac Pilpre
 * @author Aaron Madlon-Kay
 */
public class CustomColorSelectionController extends BasePreferencesController {

    private static final int MAX_ROW_COUNT = 10;

    // Heavily shortened project header, plus the note that colours without a
    // fixed value follow the active look and feel and are therefore omitted.
    private static final String[] EXPORT_HEADER = {
        "# OmegaT colour scheme",
        "# OmegaT is free/open-source software (GPLv3, https://omegat.org).",
        "# This exported scheme is yours: use, share and modify it freely.",
        "# Colours without a fixed value follow the active look and feel and",
        "# are not listed. Each remaining line maps a colour to a #rrggbb value.",
    };

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
        addImportExportButtons();
    }

    /**
     * Add the theme import/export buttons to the same button column as the
     * reset button, without touching the generated NetBeans form.
     */
    private void addImportExportButtons() {
        Container buttonColumn = panel.resetCurrentColorButton.getParent();
        JButton exportButton = new JButton();
        Mnemonics.setLocalizedText(exportButton, OStrings.getString("GUI_COLORS_EXPORT"));
        exportButton.addActionListener(e -> exportColors());
        JButton importButton = new JButton();
        Mnemonics.setLocalizedText(importButton, OStrings.getString("GUI_COLORS_IMPORT"));
        importButton.addActionListener(e -> importColors());
        buttonColumn.add(exportButton);
        buttonColumn.add(importButton);
    }

    private JFileChooser colorSchemeChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setFileFilter(new FileNameExtensionFilter(
                OStrings.getString("GUI_COLORS_FILE_DESCRIPTION"), "properties"));
        // Reopen in the folder used last time.
        String lastDir = Preferences.getPreference(Preferences.COLOR_SCHEME_DIRECTORY);
        if (!StringUtil.isEmpty(lastDir)) {
            File dir = new File(lastDir);
            if (dir.isDirectory()) {
                chooser.setCurrentDirectory(dir);
            }
        }
        return chooser;
    }

    private void rememberDirectory(File file) {
        File dir = file.getParentFile();
        if (dir != null) {
            Preferences.setPreference(Preferences.COLOR_SCHEME_DIRECTORY, dir.getAbsolutePath());
        }
    }

    private void exportColors() {
        String title = Mnemonics.removeMnemonics(OStrings.getString("GUI_COLORS_EXPORT"));
        JFileChooser chooser = colorSchemeChooser();
        chooser.setDialogTitle(title);
        chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), "omegat-colours.properties"));
        if (chooser.showSaveDialog(panel) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (file.exists() && JOptionPane.showConfirmDialog(panel,
                OStrings.getString("GUI_COLORS_OVERWRITE_CONFIRM", file.getName()), title,
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        rememberDirectory(file);
        // A plain, human-readable .properties file: one "EditorColor = #rrggbb"
        // line per colour, in enum order. Same shape as the bundled
        // ColorScheme_*.properties, and loadable with java.util.Properties.
        int count = 0;
        try (BufferedWriter w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            for (String line : EXPORT_HEADER) {
                w.write(line);
                w.newLine();
            }
            for (EditorColor style : EditorColor.values()) {
                Color color = temporaryPreferences.getOrDefault(style, style.getColor());
                if (color != null) {
                    w.write(style.name() + " = " + toHex(color));
                    w.newLine();
                    count++;
                }
            }
            fireTransientMessage(OStrings.getString("GUI_COLORS_EXPORTED", count));
        } catch (IOException ex) {
            Log.log(ex);
            JOptionPane.showMessageDialog(panel, ex.getLocalizedMessage(), title,
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importColors() {
        String title = Mnemonics.removeMnemonics(OStrings.getString("GUI_COLORS_IMPORT"));
        JFileChooser chooser = colorSchemeChooser();
        chooser.setDialogTitle(title);
        if (chooser.showOpenDialog(panel) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        rememberDirectory(chooser.getSelectedFile());
        Properties props = new Properties();
        try (BufferedReader r = Files.newBufferedReader(chooser.getSelectedFile().toPath(),
                StandardCharsets.UTF_8)) {
            props.load(r);
        } catch (IOException ex) {
            Log.log(ex);
            JOptionPane.showMessageDialog(panel, ex.getLocalizedMessage(), title,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int count = 0;
        for (String key : props.stringPropertyNames()) {
            EditorColor style = editorColorForName(key);
            if (style == null) {
                // Unknown key: ignore, so schemes stay forward-compatible.
                continue;
            }
            try {
                // Stage the colour only: it shows in the table at once but is
                // applied by persist() on "Apply"/"OK", so "Cancel" discards it.
                temporaryPreferences.put(style, Color.decode(props.getProperty(key).trim()));
                count++;
            } catch (NumberFormatException ex) {
                Log.log(ex);
            }
        }
        panel.colorStylesTable.repaint();
        onSelectionChanged();
        fireTransientMessage(OStrings.getString("GUI_COLORS_IMPORTED", count));
    }

    private static EditorColor editorColorForName(String name) {
        try {
            return EditorColor.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
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
            // resetting is an explicit action and takes effect immediately;
            // bound spans repaint with the new palette, no rebuild needed
            style.setColor(null);
            CoreEvents.fireColorsChanged();
            updateSelectionIcon();
        });
    }

    @Override
    public void restoreDefaults() {
        // Nothing to reset if the colours already match their defaults; only
        // ask for confirmation when there is something to lose.
        if (!differsFromDefaults()) {
            return;
        }
        if (JOptionPane.showConfirmDialog(panel, OStrings.getString("GUI_COLORS_RESTORE_CONFIRM"),
                Mnemonics.removeMnemonics(OStrings.getString("PREFERENCES_BUTTON_RESET")),
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        for (EditorColor style : EditorColor.values()) {
            temporaryPreferences.put(style, style.getDefault());
            // restoring the defaults is an explicit action and takes effect
            // immediately
            style.setColor(null);
        }
        panel.colorStylesTable.repaint();
        panel.colorStylesTable.clearSelection();
        onSelectionChanged();
        CoreEvents.fireColorsChanged();
        fireTransientMessage(OStrings.getString("GUI_COLORS_RESTORED"));
    }

    /**
     * Color changes broadcast a colors-changed event and repaint; the editor
     * document does not need to be rebuilt for them.
     */
    @Override
    public boolean requiresEditorRefresh() {
        return false;
    }

    private boolean differsFromDefaults() {
        for (EditorColor style : EditorColor.values()) {
            Color current = temporaryPreferences.getOrDefault(style, style.getColor());
            if (!Objects.equals(current, style.getDefault())) {
                return true;
            }
        }
        return false;
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
        NAME(String.class), ICON(Icon.class);

        private final Class<?> clss;

        ColorColumns(Class<?> clss) {
            this.clss = clss;
        }

        static ColorColumns get(int index) {
            return values()[index];
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
