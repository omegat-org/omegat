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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

/**
 * A dialog for inputting a keystroke (e.g. "Shift+Ctrl+S")
 *
 * @author Aaron Madlon-Kay
 */
public class KeyStrokeEditorDialog {

    private KeyStroke keyStroke;
    private boolean userDidConfirm;

    public KeyStrokeEditorDialog(KeyStroke keyStroke) {
        this.keyStroke = keyStroke;
    }

    public boolean show(Window parent) {
        JDialog dialog = new JDialog(parent);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setModal(true);
        StaticUIUtils.setWindowIcon(dialog);

        KeyStrokeEditorPanel panel = new KeyStrokeEditorPanel();

        dialog.getContentPane().add(panel);

        panel.shortcutLabel.setText(keyStrokeToString(keyStroke));

        // Disable focus traversal keys to allow capturing Tab, Shift+Tab
        dialog.setFocusTraversalKeysEnabled(false);
        dialog.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                keyStroke = KeyStroke.getKeyStrokeForEvent(e);
                panel.shortcutLabel.setText(keyStrokeToString(keyStroke));
                e.consume();
            }
        });

        panel.clearButton.addActionListener(e -> {
            keyStroke = null;
            panel.shortcutLabel.setText(keyStrokeToString(keyStroke));
        });

        panel.okButton.addActionListener(e -> {
            userDidConfirm = true;
            StaticUIUtils.closeWindowByEvent(dialog);
        });
        panel.cancelButton.addActionListener(e -> {
            keyStroke = null;
            userDidConfirm = false;
            StaticUIUtils.closeWindowByEvent(dialog);
        });

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                // Repack to get the height right
                dialog.pack();
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return userDidConfirm;
    }

    String keyStrokeToString(KeyStroke ks) {
        if (ks == null) {
            return OStrings.getString("KEYSTROKE_EDITOR_NOT_SET");
        } else {
            return StaticUIUtils.getKeyStrokeText(ks);
        }
    }

    public KeyStroke getResult() {
        return keyStroke;
    }
}
