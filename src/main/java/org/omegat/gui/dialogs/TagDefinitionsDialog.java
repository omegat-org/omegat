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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.text.MessageFormat;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.jspecify.annotations.Nullable;
import org.openide.awt.Mnemonics;

import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

/**
 * Editor for the project-specific custom tag and removed-text expressions,
 * opened from the "Local Tag Definitions..." button of the project
 * properties dialog. A {@code null} expression means the project follows the
 * global preference; an empty one switches the expression off.
 *
 * @author Stephan Pakebusch
 */
public class TagDefinitionsDialog {

    public static final String USE_LOCAL_CB_NAME = "tag_definitions_use_local_cb";
    public static final String CUSTOM_TAG_PATTERN_FIELD_NAME = "tag_definitions_custom_tag_pattern_field";
    public static final String REMOVE_TEXT_PATTERN_FIELD_NAME = "tag_definitions_remove_text_pattern_field";

    private final String globalCustomTagPattern;
    private final String globalRemoveTextPattern;
    private final boolean storedFileUnreadable;
    private @Nullable String customTagPattern;
    private @Nullable String removeTextPattern;
    private boolean userDidConfirm;
    /** The prefill runs at most once, so emptied fields stay empty on a re-tick. */
    private boolean prefilled;

    /**
     * @param customTagPattern
     *            the project's custom-tag expression, or {@code null} when
     *            the project follows the global preference
     * @param removeTextPattern
     *            the project's removed-text expression, or {@code null} when
     *            the project follows the global preference
     * @param globalCustomTagPattern
     *            the global custom-tag expression, used to prefill the field
     * @param globalRemoveTextPattern
     *            the global removed-text expression, used to prefill the
     *            field
     * @param storedFileUnreadable
     *            whether the project carries a tag_patterns.xml that could
     *            not be read; the editor then warns that confirming it
     *            replaces the file
     */
    public TagDefinitionsDialog(@Nullable String customTagPattern, @Nullable String removeTextPattern,
            String globalCustomTagPattern, String globalRemoveTextPattern,
            boolean storedFileUnreadable) {
        this.customTagPattern = customTagPattern;
        this.removeTextPattern = removeTextPattern;
        this.globalCustomTagPattern = globalCustomTagPattern;
        this.globalRemoveTextPattern = globalRemoveTextPattern;
        this.storedFileUnreadable = storedFileUnreadable;
    }

    /**
     * Shows the modal editor. Returns true when the user confirmed it; the
     * edited expressions are then available from {@link #getCustomTagPattern()}
     * and {@link #getRemoveTextPattern()}.
     */
    public boolean show(Window parent) {
        JDialog dialog = new JDialog(parent, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setTitle(OStrings.getString("PP_TAG_PATTERNS"));
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        StaticUIUtils.setEscapeClosable(dialog);
        StaticUIUtils.setWindowIcon(dialog);

        boolean overriddenAtOpen = customTagPattern != null || removeTextPattern != null;

        JCheckBox useLocalCheckBox = new JCheckBox();
        Mnemonics.setLocalizedText(useLocalCheckBox, OStrings.getString("PP_TAG_PATTERNS_PROJECT_SPECIFIC"));
        useLocalCheckBox.setName(USE_LOCAL_CB_NAME);
        useLocalCheckBox.setSelected(overriddenAtOpen);

        // Show the effective expressions: a pattern the project does not
        // override is displayed (and on OK pinned) at its global value, so
        // merely confirming the editor never changes behavior.
        JTextField customTagPatternField = new JTextField(30);
        customTagPatternField.setName(CUSTOM_TAG_PATTERN_FIELD_NAME);
        JTextField removeTextPatternField = new JTextField(30);
        removeTextPatternField.setName(REMOVE_TEXT_PATTERN_FIELD_NAME);
        if (overriddenAtOpen) {
            customTagPatternField.setText(customTagPattern != null ? customTagPattern
                    : globalCustomTagPattern);
            removeTextPatternField.setText(removeTextPattern != null ? removeTextPattern
                    : globalRemoveTextPattern);
        }
        customTagPatternField.setEnabled(overriddenAtOpen);
        removeTextPatternField.setEnabled(overriddenAtOpen);

        // Ticking the override for the first time starts from the global
        // expressions, so the usual case is editing them, not retyping them.
        // A project that already overrides the expressions keeps its values,
        // even when they are deliberately empty, so the prefill stays out of
        // the way after an accidental toggle.
        useLocalCheckBox.addActionListener(e -> {
            if (!overriddenAtOpen && !prefilled && useLocalCheckBox.isSelected()
                    && customTagPatternField.getText().isEmpty()
                    && removeTextPatternField.getText().isEmpty()) {
                prefilled = true;
                customTagPatternField.setText(globalCustomTagPattern);
                removeTextPatternField.setText(globalRemoveTextPattern);
            }
            customTagPatternField.setEnabled(useLocalCheckBox.isSelected());
            removeTextPatternField.setEnabled(useLocalCheckBox.isSelected());
        });

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.LINE_START;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(useLocalCheckBox, gbc);

        JLabel customTagPatternLabel = new JLabel();
        Mnemonics.setLocalizedText(customTagPatternLabel, OStrings.getString("TV_OPTION_CUSTOMPATTERN"));
        customTagPatternLabel.setLabelFor(customTagPatternField);
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        contentPanel.add(customTagPatternLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(customTagPatternField, gbc);

        JLabel removeTextPatternLabel = new JLabel();
        Mnemonics.setLocalizedText(removeTextPatternLabel, OStrings.getString("TV_OPTION_REMOVEPATTERN"));
        removeTextPatternLabel.setLabelFor(removeTextPatternField);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        contentPanel.add(removeTextPatternLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(removeTextPatternField, gbc);

        if (storedFileUnreadable) {
            // Deleting or replacing the still repairable file must be an
            // informed decision, not a side effect of a curiosity OK.
            JLabel warningLabel = new JLabel(OStrings.getString("PP_TAG_PATTERNS_LOAD_FAILED"));
            warningLabel.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            contentPanel.add(warningLabel, gbc);
        }

        JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK"));
        JButton cancelButton = new JButton();
        Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL"));
        Box buttonBox = Box.createHorizontalBox();
        buttonBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(okButton);
        buttonBox.add(Box.createHorizontalStrut(5));
        buttonBox.add(cancelButton);

        okButton.addActionListener(e -> {
            if (!useLocalCheckBox.isSelected()) {
                customTagPattern = null;
                removeTextPattern = null;
            } else {
                for (JTextField field : new JTextField[] { customTagPatternField,
                        removeTextPatternField }) {
                    try {
                        Pattern.compile(field.getText());
                    } catch (PatternSyntaxException ex) {
                        JOptionPane.showMessageDialog(dialog,
                                MessageFormat.format(OStrings.getString("PP_TAG_PATTERN_INVALID"),
                                        ex.getLocalizedMessage()),
                                OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
                        field.requestFocusInWindow();
                        return;
                    }
                }
                customTagPattern = customTagPatternField.getText();
                removeTextPattern = removeTextPatternField.getText();
            }
            userDidConfirm = true;
            StaticUIUtils.closeWindowByEvent(dialog);
        });
        cancelButton.addActionListener(e -> StaticUIUtils.closeWindowByEvent(dialog));

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(contentPanel, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonBox, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(okButton);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return userDidConfirm;
    }

    /**
     * The confirmed custom-tag expression, or {@code null} when the project
     * follows the global preference.
     */
    public @Nullable String getCustomTagPattern() {
        return customTagPattern;
    }

    /**
     * The confirmed removed-text expression, or {@code null} when the project
     * follows the global preference.
     */
    public @Nullable String getRemoveTextPattern() {
        return removeTextPattern;
    }
}
