/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Zoltan Bartko
               2008-2011 Didier Briel
               2012 Martin Fleurke, Didier Briel
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.dialogs;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.omegat.core.spellchecker.DictionaryManager;
import org.omegat.util.Language;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * @author Zoltan Bartko
 * @author Didier Briel
 * @author Martin Fleurke
 */
@SuppressWarnings("serial")
public class SpellcheckerConfigurationDialog extends javax.swing.JDialog {

    private final JFileChooser fileChooser = new JFileChooser();

    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;

    private int returnStatus = RET_CANCEL;

    /**
     * the project's current language
     */
    private Language currentLanguage;

    /**
     * The dictionary manager
     */
    private DictionaryManager dicMan;

    /**
     * the language list model
     */
    private DefaultListModel languageListModel;

    public int getReturnStatus() {
        return returnStatus;
    }

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    /**
     * Creates new form SpellcheckerConfigurationDialog
     */
    public SpellcheckerConfigurationDialog(Frame parent, Language current) {
        super(parent, true);

        // HP
        // Handle escape key to close the window
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
        // END HP

        initComponents();
        this.pack();

        currentLanguage = current;

        languageListModel = new DefaultListModel();

        // initialize things from the preferences
        autoSpellcheckCheckBox.setSelected(Preferences.isPreference(Preferences.ALLOW_AUTO_SPELLCHECKING));
        updateDetailPanel();

        directoryTextField.setText(Preferences.getPreference(Preferences.SPELLCHECKER_DICTIONARY_DIRECTORY));
        updateLanguageList();

        String dictionaryUrl = Preferences.getPreference(Preferences.SPELLCHECKER_DICTIONARY_URL);
        if ("".equalsIgnoreCase(dictionaryUrl) ||
            //string below was default prior to 2.5.0 update 5, but is not working. Override with new default.
            "http://ftp.services.openoffice.org/pub/OpenOffice.org/contrib/dictionaries/".equalsIgnoreCase(dictionaryUrl)
           ) {
               dictionaryUrlTextField.setText(OConsts.REMOTE_SC_DICTIONARY_LIST_LOCATION);
        } else {
            dictionaryUrlTextField.setText(Preferences.getPreference(Preferences.SPELLCHECKER_DICTIONARY_URL));
        }

    }

    /**
     * Updates the language list based on the directory text field
     */
    public void updateLanguageList() {
        String dirName = directoryTextField.getText();

        // should we do anything?
        if (dirName == null || dirName.equals(""))
            return;

        dicMan = new DictionaryManager(dirName);

        List<String> aList = dicMan.getLocalDictionaryNameList();

        Collections.sort(aList);

        // initialize the language list model
        languageListModel.clear();

        for (String str : aList) {
            languageListModel.addElement(str);
        }

        languageList.setModel(languageListModel);
    }

    /**
     * Updates the state of the detail panel based on the check box state
     */
    private void updateDetailPanel() {
        boolean enabled = autoSpellcheckCheckBox.isSelected();
        detailPanel.setEnabled(enabled);
        contentLabel.setEnabled(enabled);
        directoryChooserButton.setEnabled(enabled);
        directoryLabel.setEnabled(enabled);
        directoryTextField.setEnabled(enabled);
        installButton.setEnabled(enabled);
        setUninstalButtonStatus();  // Depends on whether something is selected in the dictionary list
        languageScrollPane.setEnabled(enabled);
        languageList.setEnabled(enabled);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        autoSpellcheckCheckBox = new javax.swing.JCheckBox();
        detailPanel = new javax.swing.JPanel();
        directoryTextField = new javax.swing.JTextField();
        languageScrollPane = new javax.swing.JScrollPane();
        languageList = new javax.swing.JList();
        contentLabel = new javax.swing.JLabel();
        directoryLabel = new javax.swing.JLabel();
        directoryChooserButton = new javax.swing.JButton();
        dictionaryUrlLabel = new javax.swing.JLabel();
        dictionaryUrlTextField = new javax.swing.JTextField();
        installButton = new javax.swing.JButton();
        uninstallButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(OStrings.getString("GUI_SPELLCHECKER_TITLE")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(420, Short.MAX_VALUE)
                .add(okButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cancelButton))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(cancelButton)
                .add(okButton))
        );

        org.openide.awt.Mnemonics.setLocalizedText(autoSpellcheckCheckBox, OStrings.getString("GUI_SPELLCHECKER_AUTOSPELLCHECKCHECKBOX")); // NOI18N
        autoSpellcheckCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        autoSpellcheckCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        autoSpellcheckCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoSpellcheckCheckBoxActionPerformed(evt);
            }
        });

        detailPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        directoryTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directoryTextFieldActionPerformed(evt);
            }
        });

        languageList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                languageListMouseClicked(evt);
            }
        });
        languageScrollPane.setViewportView(languageList);

        org.openide.awt.Mnemonics.setLocalizedText(contentLabel, OStrings.getString("GUI_SPELLCHECKER_AVAILABLE_LABEL")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(directoryLabel, OStrings.getString("GUI_SPELLCHECKER_DICTIONARYLABEL")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(directoryChooserButton, OStrings.getString("GUI_SPELLCHECKER_DIRECTORYCHOOSERBUTTON")); // NOI18N
        directoryChooserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directoryChooserButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(dictionaryUrlLabel, OStrings.getString("GUI_SPELLCHECKER_URL_LABEL")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(installButton, OStrings.getString("GUI_SPELLCHECKER_INSTALLBUTTON")); // NOI18N
        installButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(uninstallButton, OStrings.getString("GUI_SPELLCHECKER_UNINSTALLBUTTON")); // NOI18N
        uninstallButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uninstallButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout detailPanelLayout = new org.jdesktop.layout.GroupLayout(detailPanel);
        detailPanel.setLayout(detailPanelLayout);
        detailPanelLayout.setHorizontalGroup(
            detailPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(detailPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(detailPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(detailPanelLayout.createSequentialGroup()
                        .add(dictionaryUrlTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(dictionaryUrlLabel)
                    .add(detailPanelLayout.createSequentialGroup()
                        .add(detailPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(contentLabel)
                            .add(directoryLabel)
                            .add(detailPanelLayout.createSequentialGroup()
                                .add(detailPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(languageScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, directoryTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(detailPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(uninstallButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(directoryChooserButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addContainerGap())
                    .add(detailPanelLayout.createSequentialGroup()
                        .add(installButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(377, 377, 377))))
        );
        detailPanelLayout.setVerticalGroup(
            detailPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, detailPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(directoryLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(directoryChooserButton)
                    .add(directoryTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(contentLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(detailPanelLayout.createSequentialGroup()
                        .add(languageScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(dictionaryUrlLabel))
                    .add(uninstallButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dictionaryUrlTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(9, 9, 9)
                .add(installButton))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(autoSpellcheckCheckBox)
                    .add(detailPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(autoSpellcheckCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void languageListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_languageListMouseClicked
        setUninstalButtonStatus();
    }//GEN-LAST:event_languageListMouseClicked

    /**
     * Sets the enabled/disabled status of the Uninstall (Remove) button
     * To be enabled, at list one dictionary must be selected, and the Spell checking box must be selected
     */
    private void setUninstalButtonStatus() {
        Object[] selection = languageList.getSelectedValues();
        uninstallButton.setEnabled(selection.length > 0 && autoSpellcheckCheckBox.isSelected());
    }

    private void directoryTextFieldActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_directoryTextFieldActionPerformed
        updateLanguageList();
    }// GEN-LAST:event_directoryTextFieldActionPerformed

    private void directoryChooserButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_directoryChooserButtonActionPerformed
    // open a dialog box to choose the directory
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle(OStrings.getString("GUI_SPELLCHECKER_FILE_CHOOSER_TITLE"));
        int result = fileChooser.showOpenDialog(SpellcheckerConfigurationDialog.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            // we should write the result into the directory text field
            File file = fileChooser.getSelectedFile();
            directoryTextField.setText(file.getAbsolutePath());
        }
        updateLanguageList();
    }// GEN-LAST:event_directoryChooserButtonActionPerformed

    private void installButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_installButtonActionPerformed
        if (dicMan == null) {
            JOptionPane.showMessageDialog(this, OStrings.getString("GUI_SPELLCHECKER_INSTALL_UNABLE"),
                    OStrings.getString("GUI_SPELLCHECKER_INSTALL_UNABLE_TITLE"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        Preferences
                .setPreference(Preferences.SPELLCHECKER_DICTIONARY_URL, dictionaryUrlTextField.getText());

        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        Cursor oldCursor = getCursor();
        setCursor(hourglassCursor);

        DictionaryInstallerDialog installerDialog;
        try {
            installerDialog = new DictionaryInstallerDialog(this, dicMan);
            setCursor(oldCursor);
            installerDialog.setVisible(true);
            updateLanguageList();
        } catch (IOException ex) {
        	setCursor(oldCursor);
            JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
        }
        setUninstalButtonStatus();
    }// GEN-LAST:event_installButtonActionPerformed

    private void uninstallButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_uninstallButtonActionPerformed
    // any dictionary manager available
        if (dicMan == null)
            return; // this should never happen - just in case it does

        if (currentLanguage != null) {
            Object[] selection = languageList.getSelectedValues();
            for (int i = 0; i < selection.length; i++) {
                String selectedItem = (String) selection[i];
                String selectedLocaleName = selectedItem.substring(0, selectedItem.indexOf(" "));

                if (selectedLocaleName.equals(currentLanguage.getLocaleCode())) {
                    if (JOptionPane.showConfirmDialog(this,
                            OStrings.getString("GUI_SPELLCHECKER_UNINSTALL_CURRENT"),
                            OStrings.getString("GUI_SPELLCHECKER_UNINSTALL_CURRENT_TITLE"),
                            JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
                        return;
                }

                if (!dicMan.uninstallDictionary(selectedLocaleName))
                    JOptionPane.showMessageDialog(this,
                            OStrings.getString("GUI_SPELLCHECKER_UNINSTALL_UNABLE"),
                            OStrings.getString("GUI_SPELLCHECKER_UNINSTALL_UNABLE_TITLE"),
                            JOptionPane.ERROR_MESSAGE);

                languageListModel.remove(languageList.getSelectedIndex());
            }
            setUninstalButtonStatus();
        }
    }// GEN-LAST:event_uninstallButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cancelButtonActionPerformed
        doClose(RET_CANCEL);
    }// GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_okButtonActionPerformed
    // save preferences
        Preferences.setPreference(Preferences.ALLOW_AUTO_SPELLCHECKING, autoSpellcheckCheckBox.isSelected());

        Preferences
                .setPreference(Preferences.SPELLCHECKER_DICTIONARY_DIRECTORY, directoryTextField.getText());

        Preferences
                .setPreference(Preferences.SPELLCHECKER_DICTIONARY_URL, dictionaryUrlTextField.getText());

        doClose(RET_OK);
    }// GEN-LAST:event_okButtonActionPerformed

    private void autoSpellcheckCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_autoSpellcheckCheckBoxActionPerformed
        updateDetailPanel();
    }// GEN-LAST:event_autoSpellcheckCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoSpellcheckCheckBox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel contentLabel;
    private javax.swing.JPanel detailPanel;
    private javax.swing.JLabel dictionaryUrlLabel;
    private javax.swing.JTextField dictionaryUrlTextField;
    private javax.swing.JButton directoryChooserButton;
    private javax.swing.JLabel directoryLabel;
    private javax.swing.JTextField directoryTextField;
    private javax.swing.JButton installButton;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JList languageList;
    private javax.swing.JScrollPane languageScrollPane;
    private javax.swing.JButton okButton;
    private javax.swing.JButton uninstallButton;
    // End of variables declaration//GEN-END:variables

}
