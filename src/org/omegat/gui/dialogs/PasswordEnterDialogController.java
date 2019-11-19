/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.dialogs;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;

import javax.swing.JDialog;

import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

/**
 * A simple modal dialog for prompting the user to enter a password.
 * <p>
 * The password is required to be non-empty; any other validation is left to the caller.
 * <p>
 * The result will be empty when the user cancels the dialog. When the result is present, the caller is
 * responsible for wiping the <code>char[]</code> after use.
 *
 * @author Aaron Madlon-Kay
 */
public class PasswordEnterDialogController {

    private char[] password;

    public void show(Window parent, String message) {
        JDialog dialog = new JDialog(parent);
        dialog.setTitle(OStrings.getString("PASSWORD_DIALOG_TITLE"));
        dialog.setModal(true);
        StaticUIUtils.setWindowIcon(dialog);
        StaticUIUtils.setEscapeClosable(dialog);

        PasswordSetPanel panel = new PasswordSetPanel();
        dialog.getContentPane().add(panel);

        dialog.getRootPane().setDefaultButton(panel.okButton);

        panel.confirmLabel.setVisible(false);
        panel.confirmPasswordField.setVisible(false);
        panel.doNotSetButton.setVisible(false);

        panel.messageTextArea.setText(message);

        panel.okButton.addActionListener(e -> {
            char[] entered = panel.passwordField.getPassword();
            if (entered.length == 0) {
                panel.errorTextArea.setText(OStrings.getString("PASSWORD_ERROR_EMPTY"));
            } else {
                panel.errorTextArea.setText(null);
                password = entered;
                StaticUIUtils.closeWindowByEvent(dialog);
            }
        });
        panel.cancelButton.addActionListener(e -> StaticUIUtils.closeWindowByEvent(dialog));

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                // Pack again to ensure the height is correct for the now-wrapped message area
                dialog.pack();
                panel.passwordField.requestFocusInWindow();
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /**
     * When the result is present, the caller is responsible for wiping the <code>char[]</code> after use!
     */
    public Optional<char[]> getResult() {
        return Optional.ofNullable(password);
    }
}
