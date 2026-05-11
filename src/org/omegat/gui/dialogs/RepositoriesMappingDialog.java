/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
               2025 Hiroshi Miura
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

import gen.core.project.RepositoryDefinition;
import org.omegat.gui.repositoriesmapping.RepositoriesMappingController;
import org.omegat.gui.repositoriesmapping.RepositoriesMappingPanel;
import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;
import org.openide.awt.Mnemonics;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.List;

/**
 * Dialog for repository mapping.
 *
 * @author Hiroshi Miura
 */
@SuppressWarnings("serial")
public class RepositoriesMappingDialog extends JDialog {
    private JButton cancelButton;
    private JButton okButton;

    /**
     * Creates new form RepositoriesMappingDialog
     */
    public RepositoriesMappingDialog(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(parent);
    }

    /**
     * Show the dialog and return the resulting repositories definitions, or null if cancelled.
     */
    public List<RepositoryDefinition> show(List<RepositoryDefinition> input) {
        RepositoriesMappingPanel panel = new RepositoriesMappingPanel();
        add(panel, BorderLayout.CENTER);
        // Core controller binds logic to the panel
        RepositoriesMappingController controller = new RepositoriesMappingController(panel, input);
        panel.getRootPane().setDefaultButton(okButton);
        StaticUIUtils.setEscapeClosable(this);

        okButton.addActionListener(e -> {
            String err = controller.onOk();
            if (err != null) {
                JOptionPane.showMessageDialog(panel, err, OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            setVisible(false);
        });
        cancelButton.addActionListener(e -> {
            controller.onCancel();
            setVisible(false);
        });

        setVisible(true);
        dispose();
        return controller.getResult();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(OStrings.getString("RMD_TITLE")); // NOI18N
        setMinimumSize(new Dimension(600, 400));
        setPreferredSize(new Dimension(900, 500));

        JPanel buttonPanel = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK"));
        buttonPanel.add(okButton);
        Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL"));
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }
}
