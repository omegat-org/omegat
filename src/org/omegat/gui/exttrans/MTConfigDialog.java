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

package org.omegat.gui.exttrans;

import java.awt.Window;

import javax.swing.JDialog;

import org.omegat.util.gui.StaticUIUtils;

/**
 * A base dialog controller providing a UI for configuring an MT connector. Implementers should customize the
 * labels, etc., of the {@link #dialog} member and override {@link #onConfirm()} to implement persistence
 * logic.
 *
 * @author Aaron Madlon-Kay
 */
public abstract class MTConfigDialog {

    public final MTConfigPanel panel;
    private final JDialog dialog;

    public MTConfigDialog(Window parent, String title) {
        panel = new MTConfigPanel();
        dialog = new JDialog(parent);
        dialog.setTitle(title);
        dialog.setModal(true);
        StaticUIUtils.setWindowIcon(dialog);
        dialog.getContentPane().add(panel);
        panel.cancelButton.addActionListener(e -> dialog.dispose());
        panel.okButton.addActionListener(e -> {
            onConfirm();
            dialog.dispose();
        });
        StaticUIUtils.setEscapeClosable(dialog);
        dialog.getRootPane().setDefaultButton(panel.okButton);
    }

    public void show() {
        dialog.pack();
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setVisible(true);
    }

    protected abstract void onConfirm();
}
