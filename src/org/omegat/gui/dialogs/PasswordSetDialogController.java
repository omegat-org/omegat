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
import java.util.Arrays;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.omegat.util.CredentialsManager.PasswordSetResult;
import org.omegat.util.CredentialsManager.ResponseType;
import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

/**
 * A simple modal dialog for prompting the user to set a password. The user must confirm the password by
 * entering it again in a second field.
 * <p>
 * The password is required to be:
 * <ul>
 * <li>Not empty
 * <li>Equal to the confirmation password (which must also not be empty)
 * </ul>
 * Any other validation is left to the caller.
 * <p>
 * The result will be empty when the user cancels the dialog. When the result is present, the caller is
 * responsible for wiping the <code>char[]</code> after use.
 *
 * @author Aaron Madlon-Kay
 */
public class PasswordSetDialogController {

    private char[] password;
    private boolean shouldGenerate;

    public void show(Window parent) {
        JDialog dialog = new JDialog(parent);
        dialog.setTitle(OStrings.getString("PASSWORD_DIALOG_TITLE"));
        dialog.setModal(true);
        StaticUIUtils.setWindowIcon(dialog);
        StaticUIUtils.setEscapeClosable(dialog);

        PasswordSetPanel panel = new PasswordSetPanel();
        dialog.getContentPane().add(panel);

        dialog.getRootPane().setDefaultButton(panel.okButton);

        panel.okButton.addActionListener(e -> {
            char[] entered = panel.passwordField.getPassword();
            char[] confirmed = panel.confirmPasswordField.getPassword();
            if (entered.length == 0) {
                panel.errorTextArea.setText(OStrings.getString("PASSWORD_ERROR_EMPTY"));
            } else if (confirmed.length == 0) {
                panel.errorTextArea.setText(OStrings.getString("PASSWORD_ERROR_CONFIRMATION_EMPTY"));
            } else if (Arrays.equals(entered, confirmed)) {
                panel.errorTextArea.setText(null);
                password = entered;
                shouldGenerate = false;
                StaticUIUtils.closeWindowByEvent(dialog);
            } else {
                panel.errorTextArea.setText(OStrings.getString("PASSWORD_ERROR_NO_MATCH"));
            }
        });
        panel.cancelButton.addActionListener(e -> StaticUIUtils.closeWindowByEvent(dialog));
        panel.doNotSetButton.addActionListener(e -> {
            String cancel = OStrings.getString("PASSWORD_CONFIRM_NOT_SET_BUTTON_CANCEL");
            String confirm = OStrings.getString("PASSWORD_CONFIRM_NOT_SET_BUTTON_OK");
            String[] options = { cancel, confirm };
            if (1 == JOptionPane.showOptionDialog(dialog,
                    OStrings.getString("PASSWORD_CONFIRM_NOT_SET_MESSAGE"), OStrings.getString("TF_WARNING"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, cancel)) {
                password = null;
                shouldGenerate = true;
                StaticUIUtils.closeWindowByEvent(dialog);
            }
        });

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
    public PasswordSetResult getResult() {
        return new PasswordSetResult(getResponseType(), password);
    }

    private ResponseType getResponseType() {
        if (password != null) {
            return ResponseType.USE_INPUT;
        } else if (shouldGenerate) {
            return ResponseType.GENERATE_AND_STORE;
        } else {
            return ResponseType.CANCEL;
        }
    }
}
