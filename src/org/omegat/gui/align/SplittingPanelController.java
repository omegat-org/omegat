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

import java.awt.BorderLayout;
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
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

/**
 * Controller for a simple text splitting dialog.
 *
 * @author Aaron Madlon-Kay
 */
public class SplittingPanelController {

    private final String text;
    private final String reference;
    private int splitOffset = -1;

    /**
     * Create the controller with the text to be split and an optional hint to the user.
     *
     * @param text
     *            The text to be split
     * @param reference
     *            Reference text to serve as a hint to the user
     */
    public SplittingPanelController(String text, String reference) {
        this.text = text;
        this.reference = reference;
    }

    /**
     * Show the dialog. The dialog is modal, so this method will block until complete.
     * <p>
     * If the user cancels, the result array will contain as its sole member the original string provided to
     * the constructor. Otherwise the array will contain the (trimmed) results of splitting the original
     * string.
     *
     * @param parent
     *            The parent window of the dialog
     * @return The result array
     */
    public String[] show(Window parent) {
        final JDialog dialog = new JDialog(parent, OStrings.getString("ALIGNER_DIALOG_SPLITTER"), ModalityType.DOCUMENT_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                doCancel(dialog);
            }
        });
        StaticUIUtils.setEscapeClosable(dialog);

        final EditingPanel panel = new EditingPanel();
        panel.editorPane.setEditable(false);
        panel.editorPane.setText(text);

        StaticUIUtils.makeCaretAlwaysVisible(panel.editorPane);

        panel.okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                splitOffset = panel.editorPane.getCaretPosition();
                dialog.dispose();
            }
        });
        panel.cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCancel(dialog);
            }
        });

        panel.okButton.setEnabled(false);

        panel.editorPane.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                panel.okButton.setEnabled(e.getDot() == e.getMark() && e.getDot() > 0 && e.getDot() < text.length());
            }
        });

        panel.editorPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    panel.okButton.doClick();
                }
            }
        });

        if (reference != null) {
            JPanel referencePanel = new JPanel(new BorderLayout());
            JTextArea textArea = new JTextArea(reference);
            textArea.setOpaque(false);
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            textArea.setEditable(false);
            textArea.setFocusable(false);
            referencePanel.add(textArea);
            referencePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            panel.add(referencePanel, BorderLayout.NORTH);
        }

        panel.helpText.setText(OStrings.getString("ALIGNER_DIALOG_SPLITTER_HELP"));

        dialog.add(panel);
        dialog.getRootPane().setDefaultButton(panel.okButton);
        dialog.setMinimumSize(new Dimension(450, 250));
        dialog.pack();

        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        if (splitOffset == -1) {
            return new String[] { text };
        } else {
            return new String[] { text.substring(0, splitOffset).trim(),
                    text.substring(splitOffset, text.length()).trim() };
        }
    }

    private void doCancel(JDialog dialog) {
        splitOffset = -1;
        dialog.dispose();
    }
}
