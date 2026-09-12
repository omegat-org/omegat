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
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jspecify.annotations.Nullable;
import org.openide.awt.Mnemonics;

import org.omegat.gui.shortcuts.PropertiesShortcuts;
import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

/**
 * Modal editor for one shortcut with two equivalent input paths: a capture
 * field that records a pressed key combination, and a plain text field in
 * the documented properties syntax ("ctrl shift N") - the latter is fully
 * screen-reader operable, because a capture field swallows the modifier
 * keys screen readers depend on. Both fields mirror each other.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class ShortcutEditorDialog {

    private @Nullable KeyStroke keyStroke;
    private boolean userDidConfirm;
    private boolean updating;

    public ShortcutEditorDialog(@Nullable KeyStroke initial) {
        this.keyStroke = initial;
    }

    /** Shows the modal dialog; true when the user confirmed. */
    public boolean show(Window parent, String functionLabel) {
        JDialog dialog = new JDialog(parent, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setTitle(OStrings.getString("SHORTCUT_EDITOR_TITLE", functionLabel));
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        StaticUIUtils.setEscapeClosable(dialog);
        StaticUIUtils.setWindowIcon(dialog);

        JLabel currentValue = new JLabel();
        currentValue.setFont(currentValue.getFont().deriveFont(java.awt.Font.BOLD));

        JTextField captureField = new JTextField(18);
        captureField.setEditable(false);
        captureField.getAccessibleContext()
                .setAccessibleName(OStrings.getString("SHORTCUT_EDITOR_CAPTURE_LABEL"));
        captureField.getAccessibleContext()
                .setAccessibleDescription(OStrings.getString("SHORTCUT_EDITOR_CAPTURE_DESC"));
        JTextField syntaxField = new JTextField(18);
        syntaxField.getAccessibleContext()
                .setAccessibleName(OStrings.getString("SHORTCUT_EDITOR_SYNTAX_LABEL"));
        syntaxField.getAccessibleContext()
                .setAccessibleDescription(OStrings.getString("SHORTCUT_EDITOR_SYNTAX_DESC"));
        JLabel syntaxState = new JLabel(" ");
        JButton okButton = new JButton();

        Runnable refresh = () -> {
            currentValue.setText(keyStroke == null ? OStrings.getString("KEYSTROKE_EDITOR_NOT_SET")
                    : StaticUIUtils.getKeyStrokeText(keyStroke));
            currentValue.getAccessibleContext().setAccessibleName(currentValue.getText());
        };

        // capture path: focus traversal is off so modified Tab combinations
        // are recordable, but a PLAIN Tab still traverses and a plain Escape
        // still closes the dialog - the capture field must never become a
        // keyboard trap (a plain Tab shortcut is enterable in the syntax
        // field).
        captureField.setFocusTraversalKeysEnabled(false);
        captureField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB
                        && (e.getModifiersEx() & ~InputEvent.SHIFT_DOWN_MASK) == 0) {
                    if (e.isShiftDown()) {
                        captureField.transferFocusBackward();
                    } else {
                        captureField.transferFocus();
                    }
                    e.consume();
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && e.getModifiersEx() == 0) {
                    return;
                }
                keyStroke = KeyStrokeEditorDialog.capture(e, keyStroke);
                updating = true;
                syntaxField.setText(PropertiesShortcuts.toPropertyValue(keyStroke));
                updating = false;
                syntaxState.setText(" ");
                okButton.setEnabled(true);
                refresh.run();
                e.consume();
            }
        });

        // text path: the documented properties syntax with live validation
        syntaxField.setText(PropertiesShortcuts.toPropertyValue(keyStroke));
        syntaxField.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() {
                if (updating) {
                    return;
                }
                String text = syntaxField.getText().trim();
                if (text.isEmpty()) {
                    keyStroke = null;
                    syntaxState.setText(" ");
                } else {
                    KeyStroke parsed = KeyStroke.getKeyStroke(text);
                    if (parsed == null) {
                        // an unparseable field must not be confirmable with
                        // the stale previous keystroke
                        syntaxState.setText(OStrings.getString("SHORTCUT_EDITOR_SYNTAX_INVALID"));
                        okButton.setEnabled(false);
                        return;
                    }
                    keyStroke = parsed;
                    syntaxState.setText(" ");
                }
                okButton.setEnabled(true);
                refresh.run();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
        });

        // plain Tab and Escape stay navigation keys everywhere, so these
        // two get one-click insertion into the syntax field instead
        JButton insertTab = insertButton("TAB", "SHORTCUT_EDITOR_INSERT_TAB", syntaxField);
        JButton insertEscape = insertButton("ESCAPE", "SHORTCUT_EDITOR_INSERT_ESCAPE", syntaxField);

        JPanel content = buildContent(currentValue, captureField, syntaxField, syntaxState, insertTab,
                insertEscape);

        Box buttons = buildButtonBar(dialog, syntaxField, syntaxState, okButton, refresh);

        refresh.run();
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(content, BorderLayout.CENTER);
        dialog.getContentPane().add(buttons, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(okButton);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return userDidConfirm;
    }


    private Box buildButtonBar(JDialog dialog, JTextField syntaxField, JLabel syntaxState,
            JButton okButton, Runnable refresh) {
        JButton clearButton = new JButton();
        Mnemonics.setLocalizedText(clearButton, OStrings.getString("SHORTCUT_EDITOR_CLEAR"));
        clearButton.addActionListener(e -> {
            keyStroke = null;
            updating = true;
            syntaxField.setText("");
            updating = false;
            syntaxState.setText(" ");
            okButton.setEnabled(true);
            refresh.run();
        });
        Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK"));
        okButton.addActionListener(e -> {
            userDidConfirm = true;
            StaticUIUtils.closeWindowByEvent(dialog);
        });
        JButton cancelButton = new JButton();
        Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL"));
        cancelButton.addActionListener(e -> {
            userDidConfirm = false;
            StaticUIUtils.closeWindowByEvent(dialog);
        });
        Box buttons = Box.createHorizontalBox();
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttons.add(clearButton);
        buttons.add(Box.createHorizontalGlue());
        buttons.add(okButton);
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(cancelButton);
        return buttons;
    }

    /** Button inserting the literal key name into the syntax field. */
    private static JButton insertButton(String literal, String nameKey, JTextField syntaxField) {
        JButton button = new JButton(literal);
        button.getAccessibleContext().setAccessibleName(OStrings.getString(nameKey));
        button.setToolTipText(OStrings.getString(nameKey));
        button.addActionListener(e -> syntaxField.setText(literal));
        return button;
    }

    private static JPanel buildContent(JLabel currentValue, JTextField captureField,
            JTextField syntaxField, JLabel syntaxState, JButton insertTab, JButton insertEscape) {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel currentLabel = new JLabel();
        Mnemonics.setLocalizedText(currentLabel, OStrings.getString("KEYSTROKE_EDITOR_CURRENT_LABEL"));
        content.add(currentLabel, gbc);
        gbc.gridx = 1;
        content.add(currentValue, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel captureLabel = new JLabel();
        Mnemonics.setLocalizedText(captureLabel, OStrings.getString("SHORTCUT_EDITOR_CAPTURE_LABEL"));
        captureLabel.setLabelFor(captureField);
        content.add(captureLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        content.add(captureField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel syntaxLabel = new JLabel();
        Mnemonics.setLocalizedText(syntaxLabel, OStrings.getString("SHORTCUT_EDITOR_SYNTAX_LABEL"));
        syntaxLabel.setLabelFor(syntaxField);
        content.add(syntaxLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        content.add(syntaxField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        Box inserts = Box.createHorizontalBox();
        inserts.add(insertTab);
        inserts.add(Box.createHorizontalStrut(5));
        inserts.add(insertEscape);
        content.add(inserts, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        content.add(syntaxState, gbc);

        return content;
    }

    public @Nullable KeyStroke getResult() {
        return keyStroke;
    }
}
