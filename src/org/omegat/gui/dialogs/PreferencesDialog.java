/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

import org.omegat.core.Core;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.gui.preferences.IPreferencesController;
import org.omegat.gui.preferences.IPreferencesController.FurtherActionListener;
import org.omegat.gui.preferences.PreferencePanel;
import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

/**
 * A modal dialog for showing a single preferences "view"
 * ({@link IPreferencesController}).
 *
 * @author Aaron Madlon-Kay
 */
public class PreferencesDialog {

    private final IPreferencesController view;
    private PreferencePanel panel;
    private boolean userDidConfirm;

    public PreferencesDialog(IPreferencesController view) {
        this.view = view;
    }

    public boolean show(Window parent) {
        JDialog dialog = new JDialog();
        dialog.setTitle(view.toString());
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setModal(true);
        StaticUIUtils.setEscapeClosable(dialog);
        StaticUIUtils.setWindowIcon(dialog);

        panel = new PreferencePanel();
        dialog.getContentPane().add(panel);

        panel.prefsViewPanel.add(view.getGui(), BorderLayout.CENTER);

        view.addFurtherActionListener(new FurtherActionListener() {
            @Override
            public void setRestartRequired(boolean restartRequired) {
                updateMessage();
            }

            @Override
            public void setReloadRequired(boolean reloadRequired) {
                updateMessage();
            }
        });

        panel.okButton.addActionListener(e -> {
            view.persist();
            userDidConfirm = true;
            StaticUIUtils.closeWindowByEvent(dialog);
        });
        panel.cancelButton.addActionListener(e -> StaticUIUtils.closeWindowByEvent(dialog));
        panel.undoButton.addActionListener(e -> undoCurrentView());
        panel.resetButton.setEnabled(view.canRestoreDefaults());
        panel.resetButton.addActionListener(e -> resetCurrentView());

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (view.isReloadRequired()) {
                    ProjectUICommands.promptReload();
                }
            }
        });

        dialog.getRootPane().setDefaultButton(panel.okButton);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return userDidConfirm;
    }

    private void undoCurrentView() {
        view.undoChanges();
        updateMessage();
    }

    private void resetCurrentView() {
        view.restoreDefaults();
        updateMessage();
    }

    private void updateMessage() {
        String message = null;
        if (view.isRestartRequired()) {
            message = OStrings.getString("PREFERENCES_WARNING_NEEDS_RESTART");
        } else if (view.isReloadRequired() && Core.getProject().isProjectLoaded()) {
            message = OStrings.getString("PREFERENCES_WARNING_NEEDS_RELOAD");
        }
        panel.messageTextArea.setText(message);
    }
}
