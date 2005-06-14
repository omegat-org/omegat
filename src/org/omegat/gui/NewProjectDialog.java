/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**************************************************************************/

package org.omegat.gui;

import javax.swing.border.EmptyBorder;
import org.omegat.core.threads.CommandThread;
import org.omegat.util.Language;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import org.omegat.util.PreferenceManager;
import org.openide.awt.Mnemonics;

/**
 * The dialog for creation of a new OmegaT project
 *
 * @author Keith Godfrey
 */
class NewProjectDialog extends JDialog
{
    private ProjectProperties projectProperties;

    /**
     * Creates a dialog to create a new project / edit folders of existing one.
     * 
     * @param foldersMissing if this is an existing project with some folders missing
     */
    public NewProjectDialog(ProjectProperties projectProperties, JFrame par, String projFileName, 
            boolean foldersMissing)
    {
        super(par, true);
        this.projectProperties = projectProperties;
        this.foldersMissing = foldersMissing;
        projectProperties.m_dialogOK = false;

        if (projFileName == null)
            projectProperties.reset();

        if (projFileName == null)
        {
            String sourceLocale = PreferenceManager.pref.getPreference(OConsts.PREF_SOURCELOCALE);
            if( !sourceLocale.equals(""))						                            // NOI18N
                projectProperties.setSourceLanguage(sourceLocale);
            
            String targetLocale = PreferenceManager.pref.getPreference(OConsts.PREF_TARGETLOCALE);
            if( !targetLocale.equals("") )                                                // NOI18N
                projectProperties.setTargetLanguage(targetLocale);
        }

        m_browseTarget = 0;
        
        Border emptyBorder = new EmptyBorder(2,0,2,0);

        m_messageArea = new javax.swing.JTextArea();
        m_messageArea.setEditable(false);
        m_messageArea.setBackground(
                javax.swing.UIManager.getDefaults().getColor("Label.background")); // NOI18N
        m_messageArea.setFont(new Label().getFont());
        Box bMes = Box.createHorizontalBox();
        bMes.setBorder(emptyBorder);
        bMes.add(m_messageArea);
        bMes.add(Box.createHorizontalGlue());

        m_srcRootLabel = new JLabel();
        Box bSrc = Box.createHorizontalBox();
        bSrc.setBorder(emptyBorder);
        bSrc.add(m_srcRootLabel);
        bSrc.add(Box.createHorizontalGlue());
        m_srcBrowse = new JButton();
        bSrc.add(m_srcBrowse);
        m_srcRootField = new JTextField();

        m_locRootLabel = new JLabel();
        Box bLoc = Box.createHorizontalBox();
        bLoc.setBorder(emptyBorder);
        bLoc.add(m_locRootLabel);
        bLoc.add(Box.createHorizontalGlue());
        m_locBrowse = new JButton();
        bLoc.add(m_locBrowse);
        m_locRootField = new JTextField();

        m_glosRootLabel = new JLabel();
        Box bGlos = Box.createHorizontalBox();
        bGlos.setBorder(emptyBorder);
        bGlos.add(m_glosRootLabel);
        bGlos.add(Box.createHorizontalGlue());
        m_glosBrowse = new JButton();
        bGlos.add(m_glosBrowse);
        m_glosRootField = new JTextField();

        m_tmRootLabel = new JLabel();
        Box bTM = Box.createHorizontalBox();
        bTM.setBorder(emptyBorder);
        bTM.add(m_tmRootLabel);
        bTM.add(Box.createHorizontalGlue());
        m_tmBrowse = new JButton();
        bTM.add(m_tmBrowse);
        m_tmRootField = new JTextField();

        m_sourceLocaleLabel = new JLabel();
        Box bSL = Box.createHorizontalBox();
        bSL.setBorder(emptyBorder);
        bSL.add(m_sourceLocaleLabel);
        bSL.add(Box.createHorizontalGlue());
        
        m_sourceLocaleField = new JComboBox(Language.LANGUAGES);
        if( m_sourceLocaleField.getMaximumRowCount()<20 )
            m_sourceLocaleField.setMaximumRowCount(20);
        m_sourceLocaleField.setEditable(true);
        m_sourceLocaleField.setRenderer(new MyComboBoxRenderer());
        m_sourceLocaleField.setSelectedItem(projectProperties.getSourceLanguage());

        m_targetLocaleLabel = new JLabel();
        Box bLL = Box.createHorizontalBox();
        bLL.setBorder(emptyBorder);
        bLL.add(m_targetLocaleLabel);
        bLL.add(Box.createHorizontalGlue());
        
        m_targetLocaleField = new JComboBox(Language.LANGUAGES);
        if( m_targetLocaleField.getMaximumRowCount()<20 )
            m_targetLocaleField.setMaximumRowCount(20);
        m_targetLocaleField.setEditable(true);
        m_targetLocaleField.setRenderer(new MyComboBoxRenderer());
        m_targetLocaleField.setSelectedItem(projectProperties.getTargetLanguage());
        
        Box b = Box.createVerticalBox();
        b.setBorder(new EmptyBorder(5,5,5,5));
        b.add(bMes);
        b.add(bSrc);
        b.add(m_srcRootField);
        b.add(bLoc);
        b.add(m_locRootField);
        b.add(bGlos);
        b.add(m_glosRootField);
        b.add(bTM);
        b.add(m_tmRootField);
        b.add(bSL);
        b.add(m_sourceLocaleField);
        b.add(bLL);
        b.add(m_targetLocaleField);
        getContentPane().add(b, "North");										// NOI18N

        m_okButton = new JButton();
        m_cancelButton = new JButton();
        
        Box b2 = Box.createHorizontalBox();
        b2.setBorder(new EmptyBorder(5,5,5,5));
        b2.add(Box.createHorizontalGlue());
        b2.add(m_okButton);
        b2.add(Box.createHorizontalStrut(5));
        b2.add(m_cancelButton);
        getContentPane().add(b2, "South");										// NOI18N
        
        setResizable(false);
        
        m_okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doOK();
            }
        });

        m_cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doCancel();
            }
        });

        m_srcBrowse.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_browseTarget = 1;
                doBrowseDirectoy();
            }
        });

        m_locBrowse.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_browseTarget = 2;
                doBrowseDirectoy();
            }
        });

        m_glosBrowse.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_browseTarget = 3;
                doBrowseDirectoy();
            }
        });

        m_tmBrowse.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_browseTarget = 4;
                doBrowseDirectoy();
            }
        });

        // if no project file specified, ask user to create one
        if (projFileName == null)
        {
            // have user select the project name and where they
            //  want to put it.  use that information to derive the
            //  location for project, source and loc directories
            // open save dialog
            NewDirectoryChooser ndc = new NewDirectoryChooser();
            String label;
            label = OStrings.PP_SAVE_PROJECT_FILE;
            ndc.setDialogTitle(label);

            String curDir = PreferenceManager.pref.getPreference(OConsts.PREF_CUR_DIR);
            if (curDir != null)
            {
                File dir = new File(curDir);
                if (dir.exists() && dir.isDirectory())
                {
                    ndc.setCurrentDirectory(dir);
                }
            }

            int val = ndc.showSaveDialog(this);
            if (val != JFileChooser.APPROVE_OPTION)
            {
                m_dialogCancelled = true;
                return;
            }
            projectProperties.setProjectRoot(ndc.getSelectedFile().getAbsolutePath()
                        + File.separator);
            projectProperties.setProjectFile(projectProperties.getProjectRoot() + OConsts.PROJ_FILENAME);
            PreferenceManager.pref.setPreference(OConsts.PREF_CUR_DIR,
                        ndc.getSelectedFile().getParent());
            projectProperties.setProjectName(projectProperties.getProjectFile().substring(projectProperties.getProjectRoot().length()));
            projectProperties.setSourceRoot(projectProperties.getProjectRoot() + OConsts.DEFAULT_SRC
                + File.separator);
            projectProperties.setLocRoot(projectProperties.getProjectRoot() + OConsts.DEFAULT_LOC
                + File.separator);
            projectProperties.setGlossaryRoot(projectProperties.getProjectRoot() + OConsts.DEFAULT_GLOS
                + File.separator);
            projectProperties.setTMRoot(projectProperties.getProjectRoot() + OConsts.DEFAULT_TM
                + File.separator);
        }
        else
        {
            projectProperties.setProjectFile(projFileName);
            projectProperties.setProjectRoot(projectProperties.getProjectFile().substring(0,
                            projectProperties.getProjectFile().lastIndexOf(File.separator)+1));
        }

        projectProperties.setProjectInternal(projectProperties.getProjectRoot() + OConsts.DEFAULT_INTERNAL
                + File.separator);

        m_srcRootField.setText(projectProperties.getSourceRoot());
        m_locRootField.setText(projectProperties.getLocRoot());
        m_glosRootField.setText(projectProperties.getGlossaryRoot());
        m_tmRootField.setText(projectProperties.getTMRoot());
        m_sourceLocaleField.setSelectedItem(projectProperties.getSourceLanguage());
        m_targetLocaleField.setSelectedItem(projectProperties.getTargetLanguage());

        if( foldersMissing )
        {
            File f = new File(m_srcRootField.getText());
            if( !f.exists() || !f.isDirectory() ) 
                m_srcRootField.setForeground(Color.RED);
            
            f = new File(m_locRootField.getText());
            if( !f.exists() || !f.isDirectory() )
                m_locRootField.setForeground(Color.RED);
            
            f = new File(m_glosRootField.getText());
            if( !f.exists() || !f.isDirectory() )
                m_glosRootField.setForeground(Color.RED);
            
            f = new File(m_tmRootField.getText());
            if( !f.exists() || !f.isDirectory() )
                m_tmRootField.setForeground(Color.RED);
        }
        
        updateUIText();
        
        pack();
        
        setSize(6*getWidth()/5, getHeight());
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dialogSize = getSize();
        setLocation((screenSize.width-dialogSize.width)/2,(screenSize.height-dialogSize.height)/2);
    }

    private void doBrowseDirectoy()
    {
        String title;														// NOI18N
        switch (m_browseTarget)
        {
            case 1:
                title = OStrings.PP_BROWSE_TITLE_SOURCE;
                break;

            case 2:
                title = OStrings.PP_BROWSE_TITLE_TARGET;
                break;

            case 3:
                title = OStrings.PP_BROWSE_TITLE_GLOS;
                break;

            case 4:
                title = OStrings.PP_BROWSE_TITLE_TM;
                break;

            default:
                return;
        }

        JFileChooser browser = new JFileChooser();
        String str = OStrings.getString("BUTTON_SELECT_NO_MNEMONIC");
        // browser.setApproveButtonText(str);
        browser.setDialogTitle(title);
        browser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String curDir = "";														// NOI18N
        switch (m_browseTarget)
        {
            case 1:
                curDir = PreferenceManager.pref.getPreference(OConsts.PREF_SRC_DIR);
                break;

            case 2:
                curDir = PreferenceManager.pref.getPreference(OConsts.PREF_LOC_DIR);
                break;

            case 3:
                curDir = PreferenceManager.pref.getPreference(OConsts.PREF_GLOS_DIR);
                break;

            case 4:
                curDir = PreferenceManager.pref.getPreference(OConsts.PREF_TM_DIR);
                break;
        }

        if (curDir.equals(""))													// NOI18N
            curDir = PreferenceManager.pref.getPreference(OConsts.PREF_CUR_DIR);

        if (!curDir.equals(""))											// NOI18N
        {
            File dir = new File(curDir);
            if (dir.exists() && dir.isDirectory())
            {
                browser.setCurrentDirectory(dir);
            }
        }

        browser.showOpenDialog(this);
        File dir = browser.getSelectedFile();
        if (dir == null)
            return;

        str = dir.getAbsolutePath() + File.separator;
        // reset appropriate path - store preferred directory
        switch (m_browseTarget)
        {
            case 1:
                PreferenceManager.pref.setPreference(OConsts.PREF_SRC_DIR,
                        browser.getSelectedFile().getParent());
                projectProperties.setSourceRoot(str);
                m_srcRootField.setText(projectProperties.getSourceRoot());
                if( new File(projectProperties.getSourceRoot()).exists() &&
                        new File(projectProperties.getSourceRoot()).isDirectory() )
                    m_srcRootField.setForeground(java.awt.SystemColor.textText);
                break;

            case 2:
                PreferenceManager.pref.setPreference(OConsts.PREF_LOC_DIR,
                        browser.getSelectedFile().getParent());
                projectProperties.setLocRoot(str);
                m_locRootField.setText(projectProperties.getLocRoot());
                if( new File(projectProperties.getLocRoot()).exists() &&
                        new File(projectProperties.getLocRoot()).isDirectory() )
                m_locRootField.setForeground(java.awt.SystemColor.textText);
                break;

            case 3:
                PreferenceManager.pref.setPreference(OConsts.PREF_GLOS_DIR,
                        browser.getSelectedFile().getParent());
                projectProperties.setGlossaryRoot(str);
                m_glosRootField.setText(projectProperties.getGlossaryRoot());
                if( new File(projectProperties.getGlossaryRoot()).exists() &&
                        new File(projectProperties.getGlossaryRoot()).isDirectory() )
                m_glosRootField.setForeground(java.awt.SystemColor.textText);
                break;

            case 4:
                PreferenceManager.pref.setPreference(OConsts.PREF_TM_DIR,
                        browser.getSelectedFile().getParent());
                projectProperties.setTMRoot(str);
                m_tmRootField.setText(projectProperties.getTMRoot());
                if( new File(projectProperties.getTMRoot()).exists() &&
                        new File(projectProperties.getTMRoot()).isDirectory() )
                m_tmRootField.setForeground(java.awt.SystemColor.textText);
                break;

        }
    }

    private void doOK()
    {
        if( !projectProperties.verifySingleLangCode(m_sourceLocaleField.getSelectedItem().toString()) )
        {
            JOptionPane.showMessageDialog(this, 
                    OStrings.getString("NP_INVALID_SOURCE_LOCALE") +
                    OStrings.getString("NP_LOCALE_SUGGESTION"), 
                    OStrings.TF_ERROR, JOptionPane.ERROR_MESSAGE);
            m_sourceLocaleField.requestFocusInWindow();
            return;
        }
        if( !projectProperties.verifySingleLangCode(m_targetLocaleField.getSelectedItem().toString()) )
        {
            JOptionPane.showMessageDialog(this, 
                    OStrings.getString("NP_INVALID_TARGET_LOCALE") +
                    OStrings.getString("NP_LOCALE_SUGGESTION"), 
                    OStrings.TF_ERROR, JOptionPane.ERROR_MESSAGE);
            m_targetLocaleField.requestFocusInWindow();
            return;
        }
        
        projectProperties.setSourceRoot(m_srcRootField.getText());
        if (!projectProperties.getSourceRoot().endsWith(File.separator))
            projectProperties.setSourceRoot(projectProperties.getSourceRoot() + File.separator);
        
        if( foldersMissing && !new File(projectProperties.getSourceRoot()).exists() )
        {
            JOptionPane.showMessageDialog(this, 
                    OStrings.getString("NP_SOURCEDIR_DOESNT_EXIST"),
                    OStrings.TF_ERROR, JOptionPane.ERROR_MESSAGE);
            m_srcRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setLocRoot(m_locRootField.getText());
        if (!projectProperties.getLocRoot().endsWith(File.separator))
            projectProperties.setLocRoot(projectProperties.getLocRoot() + File.separator);
        if( foldersMissing && !new File(projectProperties.getLocRoot()).exists() )
        {
            JOptionPane.showMessageDialog(this, 
                    OStrings.getString("NP_TRANSDIR_DOESNT_EXIST"),
                    OStrings.TF_ERROR, JOptionPane.ERROR_MESSAGE);
            m_locRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setGlossaryRoot(m_glosRootField.getText());
        if (!projectProperties.getGlossaryRoot().endsWith(File.separator))
            projectProperties.setGlossaryRoot(projectProperties.getGlossaryRoot() + File.separator);
        if( foldersMissing && !new File(projectProperties.getGlossaryRoot()).exists() )
        {
            JOptionPane.showMessageDialog(this, 
                    OStrings.getString("NP_GLOSSDIR_DOESNT_EXIST"),
                    OStrings.TF_ERROR, JOptionPane.ERROR_MESSAGE);
            m_glosRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setTMRoot(m_tmRootField.getText());
        if (!projectProperties.getTMRoot().endsWith(File.separator))
            projectProperties.setTMRoot(projectProperties.getTMRoot() + File.separator);
        if( foldersMissing && !new File(projectProperties.getTMRoot()).exists() )
        {
            JOptionPane.showMessageDialog(this, 
                    OStrings.getString("NP_TMDIR_DOESNT_EXIST"),
                    OStrings.TF_ERROR, JOptionPane.ERROR_MESSAGE);
            m_tmRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setSourceLanguage(m_sourceLocaleField.getSelectedItem().toString());
        projectProperties.setTargetLanguage(m_targetLocaleField.getSelectedItem().toString());

        m_dialogCancelled = false;
        setVisible(false);
        m_browseTarget = 0;
    }

    private void doCancel()
    {
        m_dialogCancelled = true;
        setVisible(false);
    }

    private void updateUIText()
    {
        if( foldersMissing )
        {
            setTitle(OStrings.PP_OPEN_PROJ);
            m_messageArea.setText(OStrings.PP_MESSAGE_BADPROJ);
        }
        else
        {
            setTitle(OStrings.PP_CREATE_PROJ);
            m_messageArea.setText(OStrings.PP_MESSAGE_CONFIGPROJ);
        }

        Mnemonics.setLocalizedText(m_srcRootLabel, OStrings.PP_SRC_ROOT);
        Mnemonics.setLocalizedText(m_srcBrowse, OStrings.PP_BUTTON_BROWSE_SRC);
        
        Mnemonics.setLocalizedText(m_locRootLabel, OStrings.PP_LOC_ROOT);
        Mnemonics.setLocalizedText(m_locBrowse, OStrings.PP_BUTTON_BROWSE_TAR);

        Mnemonics.setLocalizedText(m_glosRootLabel, OStrings.PP_GLOS_ROOT);
        Mnemonics.setLocalizedText(m_glosBrowse, OStrings.PP_BUTTON_BROWSE_GL);
        
        Mnemonics.setLocalizedText(m_tmRootLabel, OStrings.PP_TM_ROOT);
        Mnemonics.setLocalizedText(m_tmBrowse, OStrings.PP_BUTTON_BROWSE_TM);

        Mnemonics.setLocalizedText(m_sourceLocaleLabel, OStrings.PP_SRC_LANG);
        Mnemonics.setLocalizedText(m_targetLocaleLabel, OStrings.PP_LOC_LANG);

        Mnemonics.setLocalizedText(m_okButton, OStrings.PP_BUTTON_OK);
        Mnemonics.setLocalizedText(m_cancelButton, OStrings.PP_BUTTON_CANCEL);
    }

    /**
     * Whether the user cancelled the dialog.
     */
    public boolean dialogCancelled()	{ return m_dialogCancelled;	}
    private boolean		m_dialogCancelled;

    private int				m_browseTarget;

    private JTextArea   m_messageArea;
    /** if this is an existing project with some folders missing */
    private boolean		foldersMissing;

    private JLabel		m_srcRootLabel;
    private JTextField	m_srcRootField;
    private JButton		m_srcBrowse;

    private JLabel		m_locRootLabel;
    private JTextField	m_locRootField;
    private JButton		m_locBrowse;

    private JLabel		m_glosRootLabel;
    private JTextField	m_glosRootField;
    private JButton		m_glosBrowse;

    private JLabel		m_tmRootLabel;
    private JTextField	m_tmRootField;
    private JButton		m_tmBrowse;

    private JLabel		m_sourceLocaleLabel;
    private JComboBox	m_sourceLocaleField;

    private JLabel		m_targetLocaleLabel;
    private JComboBox	m_targetLocaleField;

    private JButton		m_okButton;
    private JButton		m_cancelButton;
}


/**
 * My own class that renders a locale combo box smartly.
 *
 * @author Maxym Mykhalchuk
 */
class MyComboBoxRenderer extends BasicComboBoxRenderer
{
    public Component getListCellRendererComponent(
            JList list,
            Object value,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // the list and the cell have the focus
    {
        JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        Language lang = (Language)value;
        label.setText(lang + " - " + lang.getDisplayName()); // NOI18N
        return label;
    }
}

