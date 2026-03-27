/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Hiroshi Miura.
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
package org.omegat.filters2.text.yaml;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.omegat.util.OStrings;

public class YamlOptionsDialog extends JDialog {
    private final YamlOptions yamlOptions;
    private final JTextArea ignoreKeysTextArea;
    private boolean confirmed = false;

    public YamlOptionsDialog(Window parent, YamlOptions yamlOptions) {
        super(parent);
        this.yamlOptions = yamlOptions;
        setTitle(OStrings.getString("YAML_FILTER_OPTIONS_TITLE"));
        setModal(true);
        setLayout(new BorderLayout());

        ignoreKeysTextArea = new JTextArea(10, 40);
        Set<String> ignoreKeys = yamlOptions.getIgnoreKeys();
        ignoreKeysTextArea.setText(String.join("\n", new TreeSet<>(ignoreKeys)));

        JPanel mainPanel = new JPanel(new BorderLayout(0, 5));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel labelPanel = new JPanel(new BorderLayout(0, 2));
        labelPanel.add(new JLabel(OStrings.getString("YAML_FILTER_OPTIONS_DESCRIPTION")), BorderLayout.NORTH);
        labelPanel.add(new JLabel(OStrings.getString("YAML_FILTER_OPTIONS_IGNORE_KEYS")), BorderLayout.SOUTH);

        mainPanel.add(labelPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(ignoreKeysTextArea), BorderLayout.CENTER);
        mainPanel.add(new JLabel(OStrings.getString("YAML_FILTER_OPTIONS_HINT")), BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton(OStrings.getString("BUTTON_OK"));
        okButton.addActionListener(e -> {
            confirmed = true;
            setVisible(false);
        });
        JButton cancelButton = new JButton(OStrings.getString("BUTTON_CANCEL"));
        cancelButton.addActionListener(e -> setVisible(false));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    public YamlOptions getOptions() {
        if (confirmed) {
            Set<String> ignoreKeys = Stream.of(ignoreKeysTextArea.getText().split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
            yamlOptions.setIgnoreKeys(ignoreKeys);
        }
        return yamlOptions;
    }
}
