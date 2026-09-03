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

package org.omegat.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jspecify.annotations.Nullable;
import org.openide.awt.Mnemonics;

import org.omegat.core.matching.MatchEquivalence;
import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

/**
 * Dialog listing the character equivalence classes used by fuzzy matching
 * (feature request #1681). Every class shows a checkbox (all active by
 * default) and its complete character inventory; a shared test area folds two
 * sample texts with the currently checked classes and reports whether they
 * compare as equal.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
@SuppressWarnings("serial")
public final class MatchEquivalenceDialog extends JDialog {

    /**
     * Prefill of the test area: the same mixed-language sentence in the
     * typographic and the plain-keyboard writing, exercising every class
     * (curly and CJK quotes, guillemets with narrow no-break space,
     * apostrophes, dash, no-break space, soft hyphen).
     */
    static final String TEST_SAMPLE_TYPOGRAPHIC = "He said \u201cl\u2019\u00e9t\u00e9\u201d, "
            + "\u00ab\u202foui\u202f\u00bb and \u300cはい\u300d \u2014 \u2019s morgens "
            + "10\u00a0% dis\u00adcount on pages 3\u20134";
    static final String TEST_SAMPLE_PLAIN = "He said \"l'\u00e9t\u00e9\", \" oui \" and \"はい\" - "
            + "'s morgens 10 % discount on pages 3-4";

    private final Map<MatchEquivalence, JCheckBox> checkboxes = new EnumMap<>(MatchEquivalence.class);
    private final JTextField testFieldA = new JTextField(30);
    private final JTextField testFieldB = new JTextField(30);
    private final JLabel testResult = new JLabel(" ");
    private @Nullable Set<MatchEquivalence> result;

    private MatchEquivalenceDialog(Window parent, Set<MatchEquivalence> disabled) {
        super(parent, OStrings.getString("MATCH_EQUIVALENCE_DIALOG_TITLE"),
                Dialog.ModalityType.APPLICATION_MODAL);
        StaticUIUtils.setEscapeClosable(this);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel classesPanel = new JPanel();
        classesPanel.setLayout(new BoxLayout(classesPanel, BoxLayout.Y_AXIS));
        classesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        JLabel nfcNote = new JLabel(OStrings.getString("MATCH_EQUIVALENCE_NFC_NOTE"));
        nfcNote.setAlignmentX(LEFT_ALIGNMENT);
        classesPanel.add(nfcNote);
        for (MatchEquivalence eq : MatchEquivalence.values()) {
            JCheckBox checkbox = new JCheckBox(eq.getLocalizedName(), !disabled.contains(eq));
            checkbox.setName("match_equivalence_" + eq.getId() + "_cb");
            checkbox.addActionListener(e -> updateTestResult());
            checkboxes.put(eq, checkbox);

            JPanel classPanel = new JPanel(new BorderLayout(0, 2));
            classPanel.setAlignmentX(LEFT_ALIGNMENT);
            classPanel.add(checkbox, BorderLayout.NORTH);
            JList<String> members = new JList<>(describeMembers(eq));
            members.setEnabled(false);
            members.setVisibleRowCount(Math.min(4, eq.getMembers().size()));
            members.setFont(new Font(Font.MONOSPACED, Font.PLAIN, members.getFont().getSize()));
            JScrollPane scroll = new JScrollPane(members);
            scroll.setBorder(BorderFactory.createEmptyBorder(0, 24, 6, 0));
            classPanel.add(scroll, BorderLayout.CENTER);
            classesPanel.add(classPanel);
        }

        JPanel testPanel = new JPanel(new GridBagLayout());
        testPanel.setBorder(BorderFactory.createTitledBorder(
                OStrings.getString("MATCH_EQUIVALENCE_TEST_TITLE")));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 6, 2, 6);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 0;
        testPanel.add(new JLabel(OStrings.getString("MATCH_EQUIVALENCE_TEST_TEXT_A")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        testPanel.add(testFieldA, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        testPanel.add(new JLabel(OStrings.getString("MATCH_EQUIVALENCE_TEST_TEXT_B")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        testPanel.add(testFieldB, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        testPanel.add(testResult, gbc);
        DocumentListener testListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateTestResult();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateTestResult();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateTestResult();
            }
        };
        testFieldA.setText(TEST_SAMPLE_TYPOGRAPHIC);
        testFieldB.setText(TEST_SAMPLE_PLAIN);
        testFieldA.getDocument().addDocumentListener(testListener);
        testFieldB.getDocument().addDocumentListener(testListener);

        JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK"));
        okButton.setName("match_equivalence_ok_button");
        okButton.addActionListener(e -> {
            result = getDisabledFromCheckboxes();
            dispose();
        });
        JButton cancelButton = new JButton();
        Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL"));
        cancelButton.setName("match_equivalence_cancel_button");
        cancelButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        getRootPane().setDefaultButton(okButton);

        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.add(testPanel);
        south.add(buttonPanel);

        getContentPane().add(new JScrollPane(classesPanel), BorderLayout.CENTER);
        getContentPane().add(south, BorderLayout.SOUTH);
        classesPanel.add(Box.createVerticalGlue());
        updateTestResult();
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Shows the dialog; returns the new set of disabled classes, or null when
     * cancelled.
     */
    public static @Nullable Set<MatchEquivalence> show(Window parent, Set<MatchEquivalence> disabled) {
        MatchEquivalenceDialog dialog = new MatchEquivalenceDialog(parent, disabled);
        dialog.setVisible(true);
        return dialog.result == null ? null : EnumSet.copyOf(dialog.result);
    }

    private EnumSet<MatchEquivalence> getDisabledFromCheckboxes() {
        EnumSet<MatchEquivalence> disabled = EnumSet.noneOf(MatchEquivalence.class);
        checkboxes.forEach((eq, checkbox) -> {
            if (!checkbox.isSelected()) {
                disabled.add(eq);
            }
        });
        return disabled;
    }

    private void updateTestResult() {
        Set<MatchEquivalence> active = EnumSet.complementOf(getDisabledFromCheckboxes());
        Map<Integer, String> foldMap = MatchEquivalence.buildFoldMap(active);
        boolean equal = MatchEquivalence.fold(testFieldA.getText(), foldMap)
                .equals(MatchEquivalence.fold(testFieldB.getText(), foldMap));
        testResult.setText(OStrings.getString(
                equal ? "MATCH_EQUIVALENCE_TEST_EQUAL" : "MATCH_EQUIVALENCE_TEST_DIFFERENT"));
    }

    /** One display line per member: glyph, code point, name, replacement. */
    private static String[] describeMembers(MatchEquivalence eq) {
        return eq.getMembers().entrySet().stream().map(entry -> {
            int cp = entry.getKey();
            String glyph = new String(Character.toChars(cp));
            String name = Character.getName(cp);
            if (name == null) {
                name = "?";
            }
            String replacement = entry.getValue().isEmpty()
                    ? OStrings.getString("MATCH_EQUIVALENCE_REMOVED")
                    : "→ " + entry.getValue();
            return String.format(Locale.ROOT, "%s  U+%04X  %s  %s", glyph, cp, name, replacement);
        }).toArray(String[]::new);
    }
}
