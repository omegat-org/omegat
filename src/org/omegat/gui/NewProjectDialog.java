/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2004  Keith Godfrey et al
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

import org.omegat.gui.threads.CommandThread;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class NewProjectDialog extends JDialog
{
    private ProjectProperties projectProperties;

    // msg: 0 for no message, 1 for bad project message
    // 0=config; 1=badlang; 2=badproj
    public NewProjectDialog(ProjectProperties projectProperties, JFrame par, String projFileName, int msg)
    {
        super(par, true);
        this.projectProperties = projectProperties;
        projectProperties.m_dialogOK = false;
        projectProperties.setSize(650, 500);

        if (projFileName == null)
            projectProperties.reset();
        m_message = msg;

        if (projFileName == null)
        {
            projectProperties.setSrcLang(
					CommandThread.core.getPreference(OConsts.PREF_SRCLANG) );
            projectProperties.setLocLang(
					CommandThread.core.getPreference(OConsts.PREF_LOCLANG));
            if (projectProperties.getSrcLang().equals(""))						// NOI18N
                projectProperties.setSrcLang("EN-US");							// NOI18N
            if (projectProperties.getLocLang().equals(""))						// NOI18N
                projectProperties.setLocLang("EN-UK");							// NOI18N
        }

        m_browseTarget = 0;

        m_messageLabel = new JLabel();
        Box bMes = Box.createHorizontalBox();
        bMes.add(m_messageLabel);
        bMes.add(Box.createHorizontalGlue());

        m_srcRootLabel = new JLabel();
        Box bSrc = Box.createHorizontalBox();
        bSrc.add(m_srcRootLabel);
        bSrc.add(Box.createHorizontalGlue());
        m_srcBrowse = new JButton();
        bSrc.add(m_srcBrowse);
        m_srcRootField = new JTextField();
        m_srcRootField.setEditable(false);

        m_locRootLabel = new JLabel();
        Box bLoc = Box.createHorizontalBox();
        bLoc.add(m_locRootLabel);
        bLoc.add(Box.createHorizontalGlue());
        m_locBrowse = new JButton();
        bLoc.add(m_locBrowse);
        m_locRootField = new JTextField();
        m_locRootField.setEditable(false);

        m_glosRootLabel = new JLabel();
        Box bGlos = Box.createHorizontalBox();
        bGlos.add(m_glosRootLabel);
        bGlos.add(Box.createHorizontalGlue());
        m_glosBrowse = new JButton();
        bGlos.add(m_glosBrowse);
        m_glosRootField = new JTextField();
        m_glosRootField.setEditable(false);

        m_tmRootLabel = new JLabel();
        Box bTM = Box.createHorizontalBox();
        bTM.add(m_tmRootLabel);
        bTM.add(Box.createHorizontalGlue());
        m_tmBrowse = new JButton();
        bTM.add(m_tmBrowse);
        m_tmRootField = new JTextField();
        m_tmRootField.setEditable(false);

        m_srcLangLabel = new JLabel();
        Box bSL = Box.createHorizontalBox();
        bSL.add(m_srcLangLabel);
        bSL.add(Box.createHorizontalGlue());
        m_srcLangField = new JTextField();
        m_srcLangField.setText(projectProperties.getSrcLang());

        m_locLangLabel = new JLabel();
        Box bLL = Box.createHorizontalBox();
        bLL.add(m_locLangLabel);
        bLL.add(Box.createHorizontalGlue());
        m_locLangField = new JTextField();
        m_locLangField.setText(projectProperties.getLocLang());

        m_okButton = new JButton();
        m_cancelButton = new JButton();

        Box b = Box.createVerticalBox();
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
        b.add(m_srcLangField);
        b.add(bLL);
        b.add(m_locLangField);
        getContentPane().add(b, "North");										// NOI18N
        Box b2 = Box.createHorizontalBox();
        b2.add(Box.createHorizontalGlue());
        b2.add(m_cancelButton);
        b2.add(Box.createHorizontalStrut(5));
        b2.add(m_okButton);
        getContentPane().add(b2, "South");										// NOI18N

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

            String curDir = CommandThread.core.getPreference(
                        OConsts.PREF_CUR_DIR);
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
            CommandThread.core.setPreference(OConsts.PREF_CUR_DIR,
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
                            projectProperties.getProjectFile().lastIndexOf(File.separator)));
        }

        projectProperties.setProjectInternal(projectProperties.getProjectRoot() + OConsts.DEFAULT_INTERNAL
                + File.separator);

        m_srcRootField.setText(projectProperties.getSourceRoot());
        m_locRootField.setText(projectProperties.getLocRoot());
        m_glosRootField.setText(projectProperties.getGlossaryRoot());
        m_tmRootField.setText(projectProperties.getTMRoot());
        m_srcLangField.setText(projectProperties.getSrcLang());
        m_locLangField.setText(projectProperties.getLocLang());

        updateUIText();
    }

    private void doBrowseDirectoy()
    {
        String title = "";														// NOI18N
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
        };

        JFileChooser browser = new JFileChooser();
        String str = OStrings.PP_BUTTON_SELECT;
        browser.setApproveButtonText(str);
        browser.setDialogTitle(title);
        browser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String curDir = "";														// NOI18N
        switch (m_browseTarget)
        {
            case 1:
                curDir = CommandThread.core.getPreference(
                        OConsts.PREF_SRC_DIR);
                break;

            case 2:
                curDir = CommandThread.core.getPreference(
                        OConsts.PREF_LOC_DIR);
                break;

            case 3:
                curDir = CommandThread.core.getPreference(
                        OConsts.PREF_GLOS_DIR);
                break;

            case 4:
                curDir = CommandThread.core.getPreference(
                        OConsts.PREF_TM_DIR);
                break;

        };

        if (curDir.equals(""))													// NOI18N
            curDir = CommandThread.core.getPreference(OConsts.PREF_CUR_DIR);

        if (curDir.equals("") == false)											// NOI18N
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
                CommandThread.core.setPreference(OConsts.PREF_SRC_DIR,
                        browser.getSelectedFile().getParent());
                projectProperties.setSourceRoot(str);
                m_srcRootField.setText(projectProperties.getSourceRoot());
                break;

            case 2:
                CommandThread.core.setPreference(OConsts.PREF_LOC_DIR,
                        browser.getSelectedFile().getParent());
                projectProperties.setLocRoot(str);
                m_locRootField.setText(projectProperties.getLocRoot());
                break;

            case 3:
                CommandThread.core.setPreference(OConsts.PREF_GLOS_DIR,
                        browser.getSelectedFile().getParent());
                projectProperties.setGlossaryRoot(str);
                m_glosRootField.setText(projectProperties.getGlossaryRoot());
                break;

            case 4:
                CommandThread.core.setPreference(OConsts.PREF_TM_DIR,
                        browser.getSelectedFile().getParent());
                projectProperties.setTMRoot(str);
                m_tmRootField.setText(projectProperties.getTMRoot());
                break;

        };
    }

    private void doOK()
    {
        projectProperties.setSourceRoot(m_srcRootField.getText());
        if (projectProperties.getSourceRoot().endsWith(File.separator) == false)
            projectProperties.setSourceRoot(projectProperties.getSourceRoot() + File.separator);

        projectProperties.setLocRoot(m_locRootField.getText());
        if (projectProperties.getLocRoot().endsWith(File.separator) == false)
            projectProperties.setLocRoot(projectProperties.getLocRoot() + File.separator);

        projectProperties.setGlossaryRoot(m_glosRootField.getText());
        if (projectProperties.getGlossaryRoot().endsWith(File.separator) == false)
            projectProperties.setGlossaryRoot(projectProperties.getGlossaryRoot() + File.separator);

        projectProperties.setTMRoot(m_tmRootField.getText());
        if (projectProperties.getTMRoot().endsWith(File.separator) == false)
            projectProperties.setTMRoot(projectProperties.getTMRoot() + File.separator);

        projectProperties.setSrcLang(m_srcLangField.getText());
        projectProperties.setLocLang(m_locLangField.getText());
        if (projectProperties.verifyLangCodes() == false)
        {
            // TODO display dialog describing how to fix
            setMessageCode(1);
            return;
        }

        projectProperties.m_dialogOK = true;
        hide();

        m_browseTarget = 0;
    }

    private void doCancel()
    {
        projectProperties.m_dialogOK = false;
        hide();
    }

    public void setMessageCode(int n)
    {
        m_message = n;
    }

    public void updateUIText()
    {
        String str;

        str = OStrings.PP_CREATE_PROJ;
        setTitle(str);

        if (m_message == 0)
        {
            str = OStrings.PP_MESSAGE_CONFIGPROJ;
            m_messageLabel.setText(str);
        }
        else if (m_message == 1)
        {
            str = OStrings.PP_MESSAGE_BADLANG;
            m_messageLabel.setText(str);
        }
        else if (m_message == 2)
        {
            str = OStrings.PP_MESSAGE_BADPROJ;
            m_messageLabel.setText(str);
        }

        str = OStrings.PP_SRC_ROOT;
        m_srcRootLabel.setText(str);

        str = OStrings.PP_BUTTON_BROWSE_SRC;
        m_srcBrowse.setText(str);

        str = OStrings.PP_LOC_ROOT;
        m_locRootLabel.setText(str);
        str = OStrings.PP_BUTTON_BROWSE_TAR;
        m_locBrowse.setText(str);

        str = OStrings.PP_GLOS_ROOT;
        m_glosRootLabel.setText(str);
        str = OStrings.PP_BUTTON_BROWSE_GL;
        m_glosBrowse.setText(str);

        str = OStrings.PP_TM_ROOT;
        m_tmRootLabel.setText(str);
        str = OStrings.PP_BUTTON_BROWSE_TM;
        m_tmBrowse.setText(str);

        str = OStrings.PP_SRC_LANG;
        m_srcLangLabel.setText(str);

        str = OStrings.PP_LOC_LANG;
        m_locLangLabel.setText(str);

        Dimension orig = m_srcBrowse.getPreferredSize();
        Dimension tmp = m_locBrowse.getPreferredSize();
        orig.width = Math.max(orig.width, tmp.width);
        orig.height = Math.max(orig.height, tmp.height);
        tmp = m_glosBrowse.getPreferredSize();
        orig.width = Math.max(orig.width, tmp.width);
        orig.height = Math.max(orig.height, tmp.height);
        tmp = m_tmBrowse.getPreferredSize();
        orig.width = Math.max(orig.width, tmp.width);
        orig.height = Math.max(orig.height, tmp.height);

        m_srcBrowse.setPreferredSize(orig);
        m_locBrowse.setPreferredSize(orig);
        m_glosBrowse.setPreferredSize(orig);
        m_tmBrowse.setPreferredSize(orig);

        m_okButton.setText(OStrings.PP_BUTTON_OK);

        m_cancelButton.setText(OStrings.PP_BUTTON_CANCEL);
    }

    public boolean dialogCancelled()	{ return m_dialogCancelled;	}
    private boolean		m_dialogCancelled;

    private int				m_browseTarget;

    public JLabel		m_messageLabel;
    public int			m_message;

    public JLabel		m_srcRootLabel;
    public JTextField	m_srcRootField;
    public JButton		m_srcBrowse;

    public JLabel		m_locRootLabel;
    public JTextField	m_locRootField;
    public JButton		m_locBrowse;

    public JLabel		m_glosRootLabel;
    public JTextField	m_glosRootField;
    public JButton		m_glosBrowse;

    public JLabel		m_tmRootLabel;
    public JTextField	m_tmRootField;
    public JButton		m_tmBrowse;

    public JLabel		m_srcLangLabel;
    public JTextField	m_srcLangField;

    public JLabel		m_locLangLabel;
    public JTextField	m_locLangField;

    public JButton		m_okButton;
    public JButton		m_cancelButton;
}
