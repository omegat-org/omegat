/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.swing.ImageIcon;
import javax.swing.JDialog;

import org.apache.commons.io.IOUtils;
import org.omegat.help.Help;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.ResourcesUtil;
import org.omegat.util.gui.StaticUIUtils;

/**
 * About dialog, showing OmegaT version and information on contributors.
 *
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 */
@SuppressWarnings("serial")
public class AboutDialog extends JDialog {
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;

    /** Creates new form AboutDialog */
    public AboutDialog(Frame parent) {
        super(parent, true);

        StaticUIUtils.setEscapeClosable(this);

        initComponents();

        StaticUIUtils.setCaretUpdateEnabled(abouttext, false);
        abouttext.setText(StringUtil.format(OStrings.getString("ABOUTDIALOG_CONTRIBUTORS"),
                getContributors(), getLibraries()));

        versionLabel.setText(getVersionString());

        Object[] args = { StaticUtils.getMB(Runtime.getRuntime().totalMemory()),
                StaticUtils.getMB(Runtime.getRuntime().freeMemory()),
                StaticUtils.getMB(Runtime.getRuntime().maxMemory())};
        String memoryUsage = StringUtil.format(OStrings.getString("MEMORY_USAGE"), args);
        memoryusage.setText(memoryUsage);

        String javaVersion = StringUtil.format(OStrings.getString("JAVA_VERSION"),
                System.getProperty("java.version"), Platform.is64Bit() ? 64 : 32);
        javaversion.setText(javaVersion);

        invalidate();
        pack();

        StaticUIUtils.fitInScreen(this);
        setLocationRelativeTo(parent);
    }

    private static String getLibraries() {
        URI librariesUri = Help.getHelpFileURI(OConsts.LIBRARIES_FILE);
        String result = OStrings.getString("ABOUTDIALOG_LIBRARIES_UNAVAILABLE");
        if (librariesUri != null) {
            try {
                result = IOUtils.toString(librariesUri, StandardCharsets.UTF_8);
                result = StringUtil.wrap(result, 78).replaceAll("(?m)^", "  ");
            } catch (IOException ignored) {
                // ignore
            }
        }
        return result;
    }

    private static String getContributors() {
        URI contributorsUri = Help.getHelpFileURI(OConsts.CONTRIBUTORS_FILE);
        String result = OStrings.getString("ABOUTDIALOG_CONTRIBUTORS_UNAVAILABLE");
        if (contributorsUri != null) {
            try {
                result = IOUtils.toString(contributorsUri, StandardCharsets.UTF_8);
                result = StringUtil.wrap(result, 78).replaceAll("(?m)^", "  ");
            } catch (IOException e) {
                // Ignore
            }
        }
        return result;
    }


    private String getVersionString() {
        if (!StringUtil.isEmpty(OStrings.UPDATE) && !OStrings.UPDATE.equals("0")) {
            return StringUtil.format(OStrings.getString("ABOUTDIALOG_BRAND_VERSION_UPDATE_REVISION"),
                    OStrings.getApplicationDisplayName(),OStrings.VERSION, OStrings.UPDATE, OStrings.REVISION);
        } else {
            return StringUtil.format(OStrings.getString("ABOUTDIALOG_BRAND_VERSION_REVISION"),
                    OStrings.getApplicationDisplayName(), OStrings.VERSION, OStrings.REVISION);
        }
    }

    private void copySupportInfo() {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(StaticUtils.getSupportInfo()), null);
    }

    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        versionLabel = new javax.swing.JLabel();
        aboutpane = new javax.swing.JScrollPane();
        abouttext = new javax.swing.JTextArea();
        buttonPanel = new javax.swing.JPanel();
        javaversion = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        memoryusage = new javax.swing.JLabel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        jLabel2 = new javax.swing.JLabel();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        copySupportInfoButton = new javax.swing.JButton();
        licenseButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();

        setTitle(OStrings.getString("ABOUTDIALOG_TITLE")); // NOI18N
        setModal(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        getContentPane().setLayout(new java.awt.BorderLayout(5, 5));

        versionLabel.setIcon(new ImageIcon(ResourcesUtil.APP_ICON_32X32));
        org.openide.awt.Mnemonics.setLocalizedText(versionLabel, OStrings.getString("ABOUTDIALOG_BRAND_VERSION_REVISION")); // NOI18N
        versionLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        getContentPane().add(versionLabel, java.awt.BorderLayout.NORTH);

        aboutpane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        abouttext.setEditable(false);
        abouttext.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        abouttext.setFont(versionLabel.getFont());
        abouttext.setLineWrap(true);
        abouttext.setText(OStrings.getString("ABOUTDIALOG_CONTRIBUTORS")); // NOI18N
        abouttext.setWrapStyleWord(true);
        abouttext.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        abouttext.setCaretPosition(0);
        aboutpane.setViewportView(abouttext);

        getContentPane().add(aboutpane, java.awt.BorderLayout.CENTER);

        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.setLayout(new javax.swing.BoxLayout(buttonPanel, javax.swing.BoxLayout.PAGE_AXIS));

        org.openide.awt.Mnemonics.setLocalizedText(javaversion, OStrings.getString("JAVA_VERSION")); // NOI18N
        buttonPanel.add(javaversion);
        buttonPanel.add(filler1);

        org.openide.awt.Mnemonics.setLocalizedText(memoryusage, OStrings.getString("MEMORY_USAGE")); // NOI18N
        buttonPanel.add(memoryusage);
        buttonPanel.add(filler2);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, OStrings.getString("ABOUTDIALOG_COPYRIGHT")); // NOI18N
        buttonPanel.add(jLabel2);
        buttonPanel.add(filler3);

        jPanel3.setAlignmentX(0.0F);
        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel1.setAlignmentX(0.0F);
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        org.openide.awt.Mnemonics.setLocalizedText(copySupportInfoButton, OStrings.getString("ABOUTDIALOG_COPY_SUPPORT_INFO_BUTTON")); // NOI18N
        copySupportInfoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copySupportInfoButtonActionPerformed(evt);
            }
        });
        jPanel1.add(copySupportInfoButton);

        org.openide.awt.Mnemonics.setLocalizedText(licenseButton, OStrings.getString("ABOUTDIALOG_LICENSE_BUTTON")); // NOI18N
        licenseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                licenseButtonActionPerformed(evt);
            }
        });
        jPanel1.add(licenseButton);

        org.openide.awt.Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        jPanel1.add(okButton);

        jPanel3.add(jPanel1, java.awt.BorderLayout.EAST);

        buttonPanel.add(jPanel3);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void licenseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_licenseButtonActionPerformed
        new LicenseDialog(this).setVisible(true);
    }//GEN-LAST:event_licenseButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        doClose(RET_OK);
    }//GEN-LAST:event_okButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog

    private void copySupportInfoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copySupportInfoButtonActionPerformed
        copySupportInfo();
    }//GEN-LAST:event_copySupportInfoButtonActionPerformed

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane aboutpane;
    private javax.swing.JTextArea abouttext;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton copySupportInfoButton;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel javaversion;
    private javax.swing.JButton licenseButton;
    private javax.swing.JLabel memoryusage;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables

    private int returnStatus = RET_CANCEL;

}
