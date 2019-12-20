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

package org.omegat.gui.align;

import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

import org.omegat.util.Java8Compat;
import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

/**
 * Controller for a simple text editing dialog.
 *
 * @author Aaron Madlon-Kay
 */
public class EditingPanelController {

    private final String text;
    private String result;

    /**
     * Create the controller with the default text.
     *
     * @param text
     *            The text to be shown in the editing area
     */
    public EditingPanelController(String text) {
        this.text = text;
    }

    /**
     * Show the dialog. The dialog is modal, so this method will block until complete.
     *
     * @param parent
     *            The parent window of the dialog
     * @return The result of editing
     */
    public String show(Window parent) {
        final JDialog dialog = new JDialog(parent, OStrings.getString("ALIGNER_DIALOG_EDITOR"),
                ModalityType.DOCUMENT_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                doCancel(dialog);
            }
        });
        StaticUIUtils.setEscapeClosable(dialog);

        final EditingPanel panel = new EditingPanel();
        panel.editorPane.setText(text);

        StaticUIUtils.makeCaretAlwaysVisible(panel.editorPane);

        panel.okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = panel.editorPane.getText();
                dialog.dispose();
            }
        });
        panel.cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCancel(dialog);
            }
        });

        panel.editorPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER
                        && ((e.getModifiersEx() & Java8Compat.getMenuShortcutKeyMaskEx()) != 0)) {
                    panel.okButton.doClick();
                }
            }
        });

        dialog.add(panel);
        dialog.getRootPane().setDefaultButton(panel.okButton);
        dialog.setMinimumSize(new Dimension(450, 200));
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result;
    }

    private void doCancel(JDialog dialog) {
        result = text;
        dialog.dispose();
    }
}
