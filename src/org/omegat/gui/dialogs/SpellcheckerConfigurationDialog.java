/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Zoltan Bartko
               2008-2011 Didier Briel
               2012 Martin Fleurke, Didier Briel
               2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

import java.awt.Cursor;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.omegat.core.spellchecker.DictionaryManager;
import org.omegat.util.Language;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.StaticUIUtils;

/**
 * @author Zoltan Bartko
 * @author Didier Briel
 * @author Martin Fleurke
 * @author Aaron Madlon-Kay
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
    private final Language currentLanguage;

    /**
     * The dictionary manager
     */
    private DictionaryManager dicMan;

    /**
     * the language list model
     */
    private final DefaultListModel languageListModel;

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

        StaticUIUtils.setEscapeClosable(this);

        initComponents();
        getRootPane().setDefaultButton(okButton);

        currentLanguage = current;

        languageListModel = new DefaultListModel();

        // initialize things from the preferences
        autoSpellcheckCheckBox.setSelected(Preferences.isPreference(Preferences.ALLOW_AUTO_SPELLCHECKING));
        updateDetailPanel();

        directoryTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLanguageList();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLanguageList();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLanguageList();
            }
        });
        directoryTextField.setText(Preferences.getPreference(Preferences.SPELLCHECKER_DICTIONARY_DIRECTORY));

        String dictionaryUrl = Preferences.getPreference(Preferences.SPELLCHECKER_DICTIONARY_URL);
        if (dictionaryUrl.isEmpty()
                || //string below was default prior to 2.5.0 update 5, but is not working. Override with new default.
                "http://ftp.services.openoffice.org/pub/OpenOffice.org/contrib/dictionaries/".equalsIgnoreCase(dictionaryUrl)) {
            dictionaryUrlTextField.setText(OConsts.REMOTE_SC_DICTIONARY_LIST_LOCATION);
        } else {
            dictionaryUrlTextField.setText(Preferences.getPreference(Preferences.SPELLCHECKER_DICTIONARY_URL));
        }
    }

    /**
     * Updates the language list based on the directory text field
     */
    public final void updateLanguageList() {
        // initialize the language list model
        languageListModel.clear();
        
        String dirName = directoryTextField.getText();

        // should we do anything?
        if (dirName == null || dirName.isEmpty()) {
            return;
        }
        
        File dir = new File(dirName);
        if (!dir.exists()) {
            return;
        }

        dicMan = new DictionaryManager(dir);

        List<String> aList = dicMan.getLocalDictionaryNameList();

        Collections.sort(aList);

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

        autoSpellcheckCheckBox = new javax.swing.JCheckBox();
        detailPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        directoryLabel = new javax.swing.JLabel();
        directoryTextField = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        directoryChooserButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        contentLabel = new javax.swing.JLabel();
        languageScrollPane = new javax.swing.JScrollPane();
        languageList = new javax.swing.JList();
        jPanel4 = new javax.swing.JPanel();
        uninstallButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        dictionaryUrlLabel = new javax.swing.JLabel();
        dictionaryUrlTextField = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        installButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(OStrings.getString("GUI_SPELLCHECKER_TITLE")); // NOI18N
        setPreferredSize(new java.awt.Dimension(600, 500));

        org.openide.awt.Mnemonics.setLocalizedText(autoSpellcheckCheckBox, OStrings.getString("GUI_SPELLCHECKER_AUTOSPELLCHECKCHECKBOX")); // NOI18N
        autoSpellcheckCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 5, 0));
        autoSpellcheckCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        autoSpellcheckCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoSpellcheckCheckBoxActionPerformed(evt);
            }
        });
        getContentPane().add(autoSpellcheckCheckBox, java.awt.BorderLayout.NORTH);

        detailPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(5, 20, 5, 20), javax.swing.BorderFactory.createEtchedBorder()));
        detailPanel.setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 5, 10));
        jPanel1.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(directoryLabel, OStrings.getString("GUI_SPELLCHECKER_DICTIONARYLABEL")); // NOI18N
        jPanel1.add(directoryLabel, java.awt.BorderLayout.NORTH);

        directoryTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directoryTextFieldActionPerformed(evt);
            }
        });
        jPanel1.add(directoryTextField, java.awt.BorderLayout.CENTER);

        jPanel8.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 0));
        jPanel8.setLayout(new javax.swing.BoxLayout(jPanel8, javax.swing.BoxLayout.LINE_AXIS));

        org.openide.awt.Mnemonics.setLocalizedText(directoryChooserButton, OStrings.getString("GUI_SPELLCHECKER_DIRECTORYCHOOSERBUTTON")); // NOI18N
        directoryChooserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directoryChooserButtonActionPerformed(evt);
            }
        });
        jPanel8.add(directoryChooserButton);

        jPanel1.add(jPanel8, java.awt.BorderLayout.EAST);

        detailPanel.add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));
        jPanel3.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(contentLabel, OStrings.getString("GUI_SPELLCHECKER_AVAILABLE_LABEL")); // NOI18N
        jPanel3.add(contentLabel, java.awt.BorderLayout.NORTH);

        languageList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                languageListMouseClicked(evt);
            }
        });
        languageScrollPane.setViewportView(languageList);

        jPanel3.add(languageScrollPane, java.awt.BorderLayout.CENTER);

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 0));
        jPanel4.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(uninstallButton, OStrings.getString("GUI_SPELLCHECKER_UNINSTALLBUTTON")); // NOI18N
        uninstallButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uninstallButtonActionPerformed(evt);
            }
        });
        jPanel4.add(uninstallButton, java.awt.BorderLayout.NORTH);

        jPanel3.add(jPanel4, java.awt.BorderLayout.EAST);

        detailPanel.add(jPanel3, java.awt.BorderLayout.CENTER);

        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 10, 10));
        jPanel5.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(dictionaryUrlLabel, OStrings.getString("GUI_SPELLCHECKER_URL_LABEL")); // NOI18N
        jPanel5.add(dictionaryUrlLabel, java.awt.BorderLayout.NORTH);
        jPanel5.add(dictionaryUrlTextField, java.awt.BorderLayout.CENTER);

        jPanel6.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 0, 0));
        jPanel6.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(installButton, OStrings.getString("GUI_SPELLCHECKER_INSTALLBUTTON")); // NOI18N
        installButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installButtonActionPerformed(evt);
            }
        });
        jPanel6.add(installButton, java.awt.BorderLayout.WEST);

        jPanel5.add(jPanel6, java.awt.BorderLayout.SOUTH);

        detailPanel.add(jPanel5, java.awt.BorderLayout.SOUTH);

        getContentPane().add(detailPanel, java.awt.BorderLayout.CENTER);

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 1, 20, 20));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel7.setLayout(new javax.swing.BoxLayout(jPanel7, javax.swing.BoxLayout.LINE_AXIS));

        org.openide.awt.Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        jPanel7.add(okButton);

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        jPanel7.add(cancelButton);

        jPanel2.add(jPanel7, java.awt.BorderLayout.EAST);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
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
    }// GEN-LAST:event_directoryChooserButtonActionPerformed

    private void installButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_installButtonActionPerformed
        if (dicMan == null) {
            JOptionPane.showMessageDialog(this, OStrings.getString("GUI_SPELLCHECKER_INSTALL_UNABLE"),
                    OStrings.getString("GUI_SPELLCHECKER_INSTALL_UNABLE_TITLE"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        Preferences.setPreference(Preferences.SPELLCHECKER_DICTIONARY_URL, dictionaryUrlTextField.getText());

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
        if (dicMan == null) {
            return; // this should never happen - just in case it does
        }
        if (currentLanguage != null) {
            Object[] selection = languageList.getSelectedValues();
            for (Object item : selection) {
                String selectedItem = (String) item;
                String selectedLocaleName = selectedItem.substring(0, selectedItem.indexOf(" "));

                if (selectedLocaleName.equals(currentLanguage.getLocaleCode())) {
                    if (JOptionPane.showConfirmDialog(this,
                            OStrings.getString("GUI_SPELLCHECKER_UNINSTALL_CURRENT"),
                            OStrings.getString("GUI_SPELLCHECKER_UNINSTALL_CURRENT_TITLE"),
                            JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
                if (!dicMan.uninstallDictionary(selectedLocaleName)) {
                    JOptionPane.showMessageDialog(this,
                            OStrings.getString("GUI_SPELLCHECKER_UNINSTALL_UNABLE"),
                            OStrings.getString("GUI_SPELLCHECKER_UNINSTALL_UNABLE_TITLE"),
                            JOptionPane.ERROR_MESSAGE);
                }
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

        Preferences.setPreference(Preferences.SPELLCHECKER_DICTIONARY_DIRECTORY, directoryTextField.getText());

        Preferences.setPreference(Preferences.SPELLCHECKER_DICTIONARY_URL, dictionaryUrlTextField.getText());

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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JList languageList;
    private javax.swing.JScrollPane languageScrollPane;
    private javax.swing.JButton okButton;
    private javax.swing.JButton uninstallButton;
    // End of variables declaration//GEN-END:variables

}
