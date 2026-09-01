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

import java.awt.Component;
import java.awt.FlowLayout;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.omegat.gui.editor.CsvFormatOptions.CsvCharset;
import org.omegat.gui.editor.CsvFormatOptions.QuoteEscape;
import org.omegat.gui.editor.CsvFormatOptions.SeparatorChoice;
import org.omegat.util.OStrings;

/**
 * Options of the segment CSV file format: character set, field separator
 * with a free text field for a custom separator, quoting and escaping.
 * Reusable as-is by the planned CSV import; reads and writes the shared
 * format preferences via {@link CsvFormatOptions}.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
@SuppressWarnings("serial")
final class CsvFormatOptionsPanel extends JPanel {

    private final JComboBox<CsvCharset> charsetBox = new JComboBox<>(CsvCharset.values());
    private final Map<SeparatorChoice, JRadioButton> separatorButtons = new EnumMap<>(SeparatorChoice.class);
    private final JTextField customSeparatorField = new JTextField(2);
    private final JCheckBox quoteAll = new JCheckBox(OStrings.getString("GUI_EDITORWINDOW_CSV_QUOTE_ALL"));
    private final JCheckBox escapeNewlines = new JCheckBox(
            OStrings.getString("GUI_EDITORWINDOW_CSV_ESCAPE_NEWLINES"));
    private final Map<QuoteEscape, JRadioButton> quoteEscapeButtons = new EnumMap<>(QuoteEscape.class);

    CsvFormatOptionsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(labelled("GUI_EDITORWINDOW_CSV_ENCODING", charsetBox));

        ButtonGroup separatorGroup = new ButtonGroup();
        JPanel separators = row(new JLabel(OStrings.getString("GUI_EDITORWINDOW_CSV_SEPARATOR")));
        for (SeparatorChoice choice : SeparatorChoice.values()) {
            JRadioButton button = new JRadioButton(
                    OStrings.getString("GUI_EDITORWINDOW_CSV_SEPARATOR_" + choice.name()));
            button.addActionListener(e -> updateCustomFieldState());
            separatorGroup.add(button);
            separators.add(button);
            separatorButtons.put(choice, button);
        }
        separators.add(customSeparatorField);
        add(separators);

        add(leftAligned(quoteAll));
        add(leftAligned(escapeNewlines));

        ButtonGroup escapeGroup = new ButtonGroup();
        JPanel escapes = row(new JLabel(OStrings.getString("GUI_EDITORWINDOW_CSV_QUOTE_ESCAPE")));
        for (QuoteEscape escape : QuoteEscape.values()) {
            JRadioButton button = new JRadioButton(
                    OStrings.getString("GUI_EDITORWINDOW_CSV_QUOTE_ESCAPE_" + escape.name()));
            escapeGroup.add(button);
            escapes.add(button);
            quoteEscapeButtons.put(escape, button);
        }
        add(escapes);

        load(CsvFormatOptions.loadFromPreferences());
    }

    private static JPanel row(JLabel label) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        row.add(label);
        row.add(Box.createHorizontalStrut(5));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private static JPanel labelled(String labelKey, Component component) {
        JPanel row = row(new JLabel(OStrings.getString(labelKey)));
        row.add(component);
        return row;
    }

    private static Component leftAligned(javax.swing.JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        return component;
    }

    private void load(CsvFormatOptions options) {
        charsetBox.setSelectedItem(options.getCharset());
        Objects.requireNonNull(separatorButtons.get(options.getSeparatorChoice())).setSelected(true);
        customSeparatorField.setText(String.valueOf(options.getCustomSeparator()));
        quoteAll.setSelected(options.isQuoteAll());
        escapeNewlines.setSelected(options.isEscapeNewlines());
        Objects.requireNonNull(quoteEscapeButtons.get(options.getQuoteEscape())).setSelected(true);
        updateCustomFieldState();
    }

    private void updateCustomFieldState() {
        customSeparatorField
                .setEnabled(Objects.requireNonNull(separatorButtons.get(SeparatorChoice.OTHER)).isSelected());
    }

    /** The chosen format; an unusable custom separator falls back to the comma. */
    CsvFormatOptions getOptions() {
        SeparatorChoice choice = separatorButtons.entrySet().stream().filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey).findFirst().orElse(SeparatorChoice.COMMA);
        QuoteEscape escape = quoteEscapeButtons.entrySet().stream().filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey).findFirst().orElse(QuoteEscape.DOUBLED);
        return new CsvFormatOptions((CsvCharset) charsetBox.getSelectedItem(), choice,
                customSeparator(customSeparatorField.getText()), quoteAll.isSelected(),
                escapeNewlines.isSelected(), escape);
    }

    /** First character of the input; quote, line breaks and surrogates would break the format. */
    private static char customSeparator(String input) {
        if (input.isEmpty()) {
            return CsvFormatOptions.DEFAULT_CUSTOM_SEPARATOR;
        }
        char separator = input.charAt(0);
        if (separator == '"' || separator == '\r' || separator == '\n' || Character.isSurrogate(separator)) {
            return CsvFormatOptions.DEFAULT_CUSTOM_SEPARATOR;
        }
        return separator;
    }
}
