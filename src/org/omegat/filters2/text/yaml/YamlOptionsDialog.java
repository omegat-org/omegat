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
import java.util.List;
import java.util.Map;
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
import org.openide.awt.Mnemonics;

public class YamlOptionsDialog extends JDialog {
    private final YamlOptions yamlOptions;
    private final JTextArea includeKeysTextArea;
    private final JTextArea excludeKeysTextArea;
    private boolean confirmed = false;

    public YamlOptionsDialog(Window parent, Map<String, String> options) {
        super(parent);
        this.yamlOptions = new YamlOptions(options);
        setTitle(OStrings.getString("YAML_FILTER_OPTIONS_TITLE"));
        setModal(true);
        setLayout(new BorderLayout());

        includeKeysTextArea = new JTextArea(5, 40);
        List<String> includeKeys = yamlOptions.getIncludeKeys();
        includeKeysTextArea.setText(String.join("\n", includeKeys));

        excludeKeysTextArea = new JTextArea(5, 40);
        List<String> excludeKeys = yamlOptions.getExcludeKeys();
        excludeKeysTextArea.setText(String.join("\n", excludeKeys));

        JPanel mainPanel = new JPanel(new BorderLayout(0, 5));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel labelPanel = new JPanel(new BorderLayout(0, 2));
        labelPanel.add(new JLabel(OStrings.getString("YAML_FILTER_OPTIONS_DESCRIPTION")), BorderLayout.NORTH);
        mainPanel.add(labelPanel, BorderLayout.NORTH);

        JPanel areasPanel = new JPanel(new BorderLayout(0, 5));
        
        JPanel includePanel = new JPanel(new BorderLayout(0, 2));
        includePanel.add(new JLabel(OStrings.getString("YAML_FILTER_OPTIONS_INCLUDE_KEYS")), BorderLayout.NORTH);
        includePanel.add(new JScrollPane(includeKeysTextArea), BorderLayout.CENTER);
        
        JPanel excludePanel = new JPanel(new BorderLayout(0, 2));
        excludePanel.add(new JLabel(OStrings.getString("YAML_FILTER_OPTIONS_EXCLUDE_KEYS")), BorderLayout.NORTH);
        excludePanel.add(new JScrollPane(excludeKeysTextArea), BorderLayout.CENTER);
        
        areasPanel.add(includePanel, BorderLayout.NORTH);
        areasPanel.add(excludePanel, BorderLayout.CENTER);
        
        mainPanel.add(areasPanel, BorderLayout.CENTER);

        JLabel hint = new JLabel();
        Mnemonics.setLocalizedText(hint, OStrings.getString("YAML_FILTER_OPTIONS_HINT"));
        mainPanel.add(hint, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK"));
        okButton.addActionListener(e -> {
            confirmed = true;
            setVisible(false);
        });
        JButton cancelButton = new JButton();
        Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL"));
        cancelButton.addActionListener(e -> setVisible(false));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    public Map<String, String> getOptions() {
        if (confirmed) {
            List<String> includeKeys = Stream.of(includeKeysTextArea.getText().split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            yamlOptions.setIncludeKeys(includeKeys);
            List<String> excludeKeys = Stream.of(excludeKeysTextArea.getText().split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            yamlOptions.setExcludeKeys(excludeKeys);
        }
        return yamlOptions.getOptionsMap();
    }
}
