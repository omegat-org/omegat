/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

package org.omegat.cms.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Locale;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.omegat.cms.dto.CmsResource;
import org.omegat.cms.spi.CmsConnector;
import org.omegat.core.data.CoreState;
import org.omegat.cms.dto.CmsTarget;
import org.omegat.cms.config.CmsXmlStore;
import org.omegat.util.Preferences;

/**
 * Modal panel for External CMS import. Updated to use configured targets and
 * provide a page search UI.
 */
public class CmsPanel extends JPanel {

    private final JComboBox<CmsTarget> targetCombo;
    private final JTextField pageField;
    private final JTextField urlField;
    private final JButton launchButton;

    public CmsPanel() {
        super(new BorderLayout());
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0;

        targetCombo = new JComboBox<>();
        pageField = new JTextField(20);
        JButton searchPageButton = new JButton("Search...");
        urlField = new JTextField(30);
        launchButton = new JButton("Launch");

        int row = 0;
        // Title
        add(new JLabel("External CMS import"), BorderLayout.NORTH);

        // Target row
        gc.gridx = 0;
        gc.gridy = row;
        form.add(new JLabel("Target"), gc);
        gc.gridx = 1;
        gc.gridy = row;
        gc.weightx = 1;
        form.add(targetCombo, gc);
        gc.weightx = 0;
        row++;
        // Page row
        gc.gridx = 0;
        gc.gridy = row;
        form.add(new JLabel("Page"), gc);
        gc.gridx = 1;
        gc.gridy = row;
        gc.weightx = 1;
        form.add(pageField, gc);
        gc.gridx = 2;
        gc.gridy = row;
        form.add(searchPageButton, gc);
        gc.weightx = 0;
        row++;
        // URL row
        gc.gridx = 0;
        gc.gridy = row;
        form.add(new JLabel("Custom URL"), gc);
        gc.gridx = 1;
        gc.gridy = row;
        gc.weightx = 1;
        form.add(urlField, gc);
        gc.weightx = 0;
        row++;
        // Button row
        gc.gridx = 1;
        gc.gridy = row;
        form.add(launchButton, gc);

        add(form, BorderLayout.CENTER);
        loadTargetsFromPrefs();
        loadDefaults();

        searchPageButton.addActionListener(e -> openSearchDialog());
    }

    private void loadTargetsFromPrefs() {
        List<CmsTarget> targets = CmsXmlStore.loadTargets();
        targetCombo.setModel(new DefaultComboBoxModel<>(targets.toArray(new CmsTarget[0])));
        targetCombo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                java.awt.Component c = super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                if (value instanceof CmsTarget) {
                    CmsTarget t = (CmsTarget) value;
                    setText(t.getProjectId() + " (" + t.getConnectorId() + ")");
                }
                return c;
            }
        });
        if (targetCombo.getItemCount() > 0) {
            targetCombo.setSelectedIndex(0);
        }
    }

    private void openSearchDialog() {
        CmsTarget target = (CmsTarget) targetCombo.getSelectedItem();
        if (target == null) {
            return;
        }
        CmsConnector connector = CoreState.getInstance().getCmsConnectors().get(target.getConnectorId());
        if (connector == null) {
            return;
        }
        try {
            List<CmsResource> resources = connector.listResources(target.getProjectId());
            JDialog dlg = new JDialog((java.awt.Frame) null, "Select Page", true);
            JTextField filter = new JTextField(20);
            javax.swing.DefaultListModel<CmsResource> listModel = new javax.swing.DefaultListModel<>();
            for (CmsResource r : resources) {
                listModel.addElement(r);
            }
            JList<CmsResource> list = new JList<>(listModel);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JPanel top = new JPanel(new BorderLayout());
            top.add(new JLabel("Filter:"), BorderLayout.WEST);
            top.add(filter, BorderLayout.CENTER);
            JPanel root = new JPanel(new BorderLayout());
            root.add(top, BorderLayout.NORTH);
            root.add(new JScrollPane(list), BorderLayout.CENTER);
            JPanel buttons = new JPanel();
            JButton ok = new JButton("OK");
            JButton cancel = new JButton("Cancel");
            buttons.add(ok);
            buttons.add(cancel);
            root.add(buttons, BorderLayout.SOUTH);
            dlg.getContentPane().add(root);
            dlg.setSize(400, 400);
            dlg.setLocationRelativeTo(this);

            filter.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                private void refilter() {
                    String q = filter.getText().toLowerCase(Locale.ROOT);
                    listModel.clear();
                    for (CmsResource r : resources) {
                        String nm = r.getName() != null ? r.getName() : r.getId();
                        if (nm.toLowerCase(Locale.ROOT).contains(q)) {
                            listModel.addElement(r);
                        }
                    }
                }

                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    refilter();
                }

                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    refilter();
                }

                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    refilter();
                }
            });

            ok.addActionListener(e -> {
                CmsResource sel = list.getSelectedValue();
                if (sel != null) {
                    pageField.setText(sel.getId());
                }
                dlg.dispose();
            });
            cancel.addActionListener(e -> dlg.dispose());

            dlg.setVisible(true);
        } catch (Exception ex) {
            org.omegat.util.Log.log(ex);
        }
    }

    public void loadDefaults() {
        // Pre-fill custom URL if any
        urlField.setText(Preferences.getPreferenceDefault("cms.default.url", ""));
    }

    public JButton getLaunchButton() {
        return launchButton;
    }

    public CmsConnector getSelectedConnector() {
        CmsTarget target = (CmsTarget) targetCombo.getSelectedItem();
        if (target == null)
            return null;
        return CoreState.getInstance().getCmsConnectors().get(target.getConnectorId());
    }

    public String getProjectId() {
        CmsTarget target = (CmsTarget) targetCombo.getSelectedItem();
        return target != null ? target.getProjectId() : "";
    }

    public String getResourceId() {
        return pageField.getText();
    }

    public String getCustomUrl() {
        return urlField.getText();
    }
}
