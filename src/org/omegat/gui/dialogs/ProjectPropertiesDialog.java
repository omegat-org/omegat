/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008-2009 Alex Buloichik
               2011 Martin Fleurke
               2012 Didier Briel
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.gui.dialogs;

import gen.core.filters.Filters;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.segmentation.SRX;
import org.omegat.gui.filters2.FiltersCustomizer;
import org.omegat.gui.segmentation.SegmentationCustomizer;
import org.omegat.util.Language;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.LanguageComboBoxRenderer;
import org.omegat.util.gui.OmegaTFileChooser;
import org.openide.awt.Mnemonics;

/**
 * The dialog for customizing the OmegaT project (where project properties are
 * entered and/or modified).
 * <p>
 * It is used:
 * <ul>
 * <li>During creation of a new project.
 * <li>If some directories are missing while opening a project.
 * <li>For editing project properties.
 * </ul>
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 * @author Didier Briel
 */
@SuppressWarnings("serial")
public class ProjectPropertiesDialog extends JDialog {
    private ProjectProperties projectProperties;

    /** This dialog is used to create a new project. */
    public static final int NEW_PROJECT = 1;
    /**
     * This dialog is used to resolve missing directories of existing project
     * (upon opening the project).
     */
    public static final int RESOLVE_DIRS = 2;
    /**
     * This dialog is used to edit project's properties: where directories
     * reside, languages, etc.
     */
    public static final int EDIT_PROJECT = 3;

    /**
     * The type of the dialog:
     * <ul>
     * <li>Creating project == {@link #NEW_PROJECT}
     * <li>Resolving the project's directories (existing project with some dirs
     * missing) == {@link #RESOLVE_DIRS}
     * <li>Editing project properties == {@link #EDIT_PROJECT}
     * </ul>
     */
    private int dialogType;

    /** Project SRX. */
    private SRX srx;

    /** Project filters. */
    private Filters filters;

    /**
     * Creates a dialog to create a new project / edit folders of existing one.
     * 
     * @param projectProperties
     *            properties of the project
     * @param projFileName
     *            project file name
     * @param dialogTypeValue
     *            type of the dialog ({@link #NEW_PROJECT},
     *            {@link #RESOLVE_DIRS} or {@link #EDIT_PROJECT}).
     */
    public ProjectPropertiesDialog(ProjectProperties projectProperties, String projFileName,
            int dialogTypeValue) {
        super(Core.getMainWindow().getApplicationFrame(), true);
        this.projectProperties = projectProperties;
        this.srx = projectProperties.getProjectSRX();
        this.dialogType = dialogTypeValue;

        Border emptyBorder = new EmptyBorder(2, 0, 2, 0);
        Box centerBox = Box.createVerticalBox();
        centerBox.setBorder(new EmptyBorder(5, 5, 5, 5));

        // hinting message
        JTextArea m_messageArea = new JTextArea();
        m_messageArea.setEditable(false);
        m_messageArea.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        m_messageArea.setFont(new Label().getFont());
        Box bMes = Box.createHorizontalBox();
        bMes.setBorder(emptyBorder);
        bMes.add(m_messageArea);
        bMes.add(Box.createHorizontalGlue());
        centerBox.add(bMes);

        // source and target languages
        Box localesBox = Box.createVerticalBox();
        localesBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), OStrings.getString("PP_LANGUAGES") ));

        JLabel m_sourceLocaleLabel = new JLabel();
        Mnemonics.setLocalizedText(m_sourceLocaleLabel, OStrings.getString("PP_SRC_LANG"));
        Box bSL = Box.createHorizontalBox();
        bSL.setBorder(emptyBorder);
        bSL.add(m_sourceLocaleLabel);
        bSL.add(Box.createHorizontalGlue());
        localesBox.add(bSL);

        final JComboBox m_sourceLocaleField = new JComboBox(Language.LANGUAGES);
        if (m_sourceLocaleField.getMaximumRowCount() < 20)
            m_sourceLocaleField.setMaximumRowCount(20);
        m_sourceLocaleField.setEditable(true);
        m_sourceLocaleField.setRenderer(new LanguageComboBoxRenderer());
        m_sourceLocaleField.setSelectedItem(projectProperties.getSourceLanguage());
        localesBox.add(m_sourceLocaleField);

        JLabel m_targetLocaleLabel = new JLabel();
        Mnemonics.setLocalizedText(m_targetLocaleLabel, OStrings.getString("PP_LOC_LANG"));
        Box bLL = Box.createHorizontalBox();
        bLL.setBorder(emptyBorder);
        bLL.add(m_targetLocaleLabel);
        bLL.add(Box.createHorizontalGlue());
        localesBox.add(bLL);

        final JComboBox m_targetLocaleField = new JComboBox(Language.LANGUAGES);
        if (m_targetLocaleField.getMaximumRowCount() < 20)
            m_targetLocaleField.setMaximumRowCount(20);
        m_targetLocaleField.setEditable(true);
        m_targetLocaleField.setRenderer(new LanguageComboBoxRenderer());
        m_targetLocaleField.setSelectedItem(projectProperties.getTargetLanguage());
        localesBox.add(m_targetLocaleField);

        centerBox.add(localesBox);

        // options
        centerBox.add(Box.createVerticalStrut(5));
        Box optionsBox = Box.createVerticalBox();
        optionsBox.setBorder(new EtchedBorder());
        optionsBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), OStrings.getString("PP_OPTIONS") ));
        
        // sentence-segmenting
        final JCheckBox m_sentenceSegmentingCheckBox = new JCheckBox();
        Mnemonics
                .setLocalizedText(m_sentenceSegmentingCheckBox, OStrings.getString("PP_SENTENCE_SEGMENTING"));

        JButton m_sentenceSegmentingButton = new JButton();
        Mnemonics.setLocalizedText(m_sentenceSegmentingButton, OStrings.getString("MW_OPTIONSMENU_SENTSEG"));

        Box bSent = Box.createHorizontalBox();
        bSent.add(m_sentenceSegmentingCheckBox);
        bSent.add(Box.createHorizontalGlue());
        bSent.add(m_sentenceSegmentingButton);
        optionsBox.add(bSent);
        
        //File Filters
        JButton m_fileFiltersButton = new JButton();
        Mnemonics.setLocalizedText(m_fileFiltersButton, OStrings.getString("WM_PROJECTMENU_FILEFILTERS"));

        Box bFF = Box.createHorizontalBox();
        bFF.add(Box.createHorizontalGlue());
        bFF.add(m_fileFiltersButton);
        optionsBox.add(bFF);

        //multiple translations
        final JCheckBox m_allowDefaultsCheckBox = new JCheckBox();
        Mnemonics.setLocalizedText(m_allowDefaultsCheckBox, OStrings.getString("PP_ALLOW_DEFAULTS"));
        Box bMT = Box.createHorizontalBox();
        bMT.setBorder(emptyBorder);
        bMT.add(m_allowDefaultsCheckBox);
        bMT.add(Box.createHorizontalGlue());
        optionsBox.add(bMT);

        centerBox.add(optionsBox, BorderLayout.WEST);

        // directories
        centerBox.add(Box.createVerticalStrut(5));

        Box dirsBox = Box.createVerticalBox();
        dirsBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), OStrings.getString("PP_DIRECTORIES") ));

        JLabel m_srcRootLabel = new JLabel();
        Mnemonics.setLocalizedText(m_srcRootLabel, OStrings.getString("PP_SRC_ROOT"));
        Box bSrc = Box.createHorizontalBox();
        bSrc.setBorder(emptyBorder);
        bSrc.add(m_srcRootLabel);
        bSrc.add(Box.createHorizontalGlue());
        JButton m_srcBrowse = new JButton();
        Mnemonics.setLocalizedText(m_srcBrowse, OStrings.getString("PP_BUTTON_BROWSE_SRC"));
        bSrc.add(m_srcBrowse);
        final JTextField m_srcRootField = new JTextField();
        dirsBox.add(bSrc);
        dirsBox.add(m_srcRootField);

        JLabel m_tmRootLabel = new JLabel();
        Mnemonics.setLocalizedText(m_tmRootLabel, OStrings.getString("PP_TM_ROOT"));
        Box bTM = Box.createHorizontalBox();
        bTM.setBorder(emptyBorder);
        bTM.add(m_tmRootLabel);
        bTM.add(Box.createHorizontalGlue());
        JButton m_tmBrowse = new JButton();
        Mnemonics.setLocalizedText(m_tmBrowse, OStrings.getString("PP_BUTTON_BROWSE_TM"));
        bTM.add(m_tmBrowse);
        final JTextField m_tmRootField = new JTextField();
        dirsBox.add(bTM);
        dirsBox.add(m_tmRootField);

        JLabel m_glosRootLabel = new JLabel();
        Mnemonics.setLocalizedText(m_glosRootLabel, OStrings.getString("PP_GLOS_ROOT"));
        Box bGlos = Box.createHorizontalBox();
        bGlos.setBorder(emptyBorder);
        bGlos.add(m_glosRootLabel);
        bGlos.add(Box.createHorizontalGlue());
        JButton m_glosBrowse = new JButton();
        Mnemonics.setLocalizedText(m_glosBrowse, OStrings.getString("PP_BUTTON_BROWSE_GL"));
        bGlos.add(m_glosBrowse);
        final JTextField m_glosRootField = new JTextField();
        dirsBox.add(bGlos);
        dirsBox.add(m_glosRootField);

        JLabel m_writeableGlosLabel = new JLabel();
        Mnemonics.setLocalizedText(m_writeableGlosLabel, OStrings.getString("PP_WRITEABLE_GLOS"));
        Box bwGlos = Box.createHorizontalBox();
        bwGlos.setBorder(emptyBorder);
        bwGlos.add(m_writeableGlosLabel);
        bwGlos.add(Box.createHorizontalGlue());
        JButton m_wGlosBrowse = new JButton();
        Mnemonics.setLocalizedText(m_wGlosBrowse, OStrings.getString("PP_BUTTON_BROWSE_WG"));
        bwGlos.add(m_wGlosBrowse);
        final JTextField m_writeableGlosField = new JTextField();
        dirsBox.add(bwGlos);
        dirsBox.add(m_writeableGlosField);

        JLabel m_locDictLabel = new JLabel();
        Mnemonics.setLocalizedText(m_locDictLabel, OStrings.getString("PP_DICT_ROOT"));
        Box bDict = Box.createHorizontalBox();
        bDict.setBorder(emptyBorder);
        bDict.add(m_locDictLabel);
        bDict.add(Box.createHorizontalGlue());
        JButton m_dictBrowse = new JButton();
        Mnemonics.setLocalizedText(m_dictBrowse, OStrings.getString("PP_BUTTON_BROWSE_DICT"));
        bDict.add(m_dictBrowse);
        final JTextField m_dictRootField = new JTextField();
        dirsBox.add(bDict);
        dirsBox.add(m_dictRootField);

        JLabel m_locRootLabel = new JLabel();
        Mnemonics.setLocalizedText(m_locRootLabel, OStrings.getString("PP_LOC_ROOT"));
        Box bLoc = Box.createHorizontalBox();
        bLoc.setBorder(emptyBorder);
        bLoc.add(m_locRootLabel);
        bLoc.add(Box.createHorizontalGlue());
        JButton m_locBrowse = new JButton();
        Mnemonics.setLocalizedText(m_locBrowse, OStrings.getString("PP_BUTTON_BROWSE_TAR"));
        bLoc.add(m_locBrowse);
        final JTextField m_locRootField = new JTextField();
        dirsBox.add(bLoc);
        dirsBox.add(m_locRootField);

        centerBox.add(dirsBox);

        getContentPane().add(centerBox, "Center");

        JButton m_okButton = new JButton();
        Mnemonics.setLocalizedText(m_okButton, OStrings.getString("BUTTON_OK"));
        JButton m_cancelButton = new JButton();
        Mnemonics.setLocalizedText(m_cancelButton, OStrings.getString("BUTTON_CANCEL"));

        Box southBox = Box.createHorizontalBox();
        southBox.setBorder(new EmptyBorder(5, 5, 5, 5));
        southBox.add(Box.createHorizontalGlue());
        southBox.add(m_okButton);
        southBox.add(Box.createHorizontalStrut(5));
        southBox.add(m_cancelButton);
        getContentPane().add(southBox, "South");

        setResizable(false);

        m_okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doOK(m_sourceLocaleField, m_targetLocaleField, m_sentenceSegmentingCheckBox, m_srcRootField,
                        m_locRootField, m_glosRootField, m_writeableGlosField, m_tmRootField, m_dictRootField,
                        m_allowDefaultsCheckBox);
            }
        });

        m_cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        });

        m_srcBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doBrowseDirectoy(1, m_srcRootField);
            }
        });

        m_locBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doBrowseDirectoy(2, m_locRootField);
            }
        });

        m_glosBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doBrowseDirectoy(3, m_glosRootField);
            }
        });

        m_wGlosBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doBrowseDirectoy(6, m_writeableGlosField);
            }
        });

        m_tmBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doBrowseDirectoy(4, m_tmRootField);
            }
        });

        m_dictBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doBrowseDirectoy(5, m_dictRootField);
            }
        });

        final JDialog self = this;
        m_sentenceSegmentingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SegmentationCustomizer segmentationCustomizer = new SegmentationCustomizer(self, true,
                        SRX.getDefault(), Preferences.getSRX(), srx);
                segmentationCustomizer.setVisible(true);
                if (segmentationCustomizer.getReturnStatus() == SegmentationCustomizer.RET_OK) {
                    srx = segmentationCustomizer.getSRX();
                }
            }
        });
        
        m_fileFiltersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame mainWindow = Core.getMainWindow().getApplicationFrame();
                FiltersCustomizer dlg = new FiltersCustomizer(mainWindow, true);
                dlg.setVisible(true);
                if (dlg.result != null) {
                    // saving config
                    filters = dlg.result;
                }
            }
        });

        // Handle escape key to close the window
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);

        m_srcRootField.setText(projectProperties.getSourceRoot());
        m_locRootField.setText(projectProperties.getTargetRoot());
        m_glosRootField.setText(projectProperties.getGlossaryRoot());
        m_writeableGlosField.setText(projectProperties.getWriteableGlossary());
        m_tmRootField.setText(projectProperties.getTMRoot());
        m_dictRootField.setText(projectProperties.getDictRoot());
        m_sourceLocaleField.setSelectedItem(projectProperties.getSourceLanguage());
        m_targetLocaleField.setSelectedItem(projectProperties.getTargetLanguage());
        m_sentenceSegmentingCheckBox.setSelected(projectProperties.isSentenceSegmentingEnabled());
        m_allowDefaultsCheckBox.setSelected(projectProperties.isSupportDefaultTranslations());

        switch (dialogType) {
        case RESOLVE_DIRS:
            // disabling some of the controls
            m_sourceLocaleField.setEnabled(false);
            m_targetLocaleField.setEnabled(false);
            m_sentenceSegmentingCheckBox.setEnabled(false);

            // marking missing folder RED
            File f = new File(m_srcRootField.getText());
            if (!f.exists() || !f.isDirectory())
                m_srcRootField.setForeground(Color.RED);

            f = new File(m_locRootField.getText());
            if (!f.exists() || !f.isDirectory())
                m_locRootField.setForeground(Color.RED);

            f = new File(m_glosRootField.getText());
            if (!f.exists() || !f.isDirectory())
                m_glosRootField.setForeground(Color.RED);

            f = new File(m_writeableGlosField.getText());
            String wGlos = f.getParent(); // Remove the file name
            if (!wGlos.endsWith(File.separator)) {
                wGlos += File.separator;
            }
            f = new File(wGlos);
            // The writeable glossary must be in in the /glossary folder
            if (!f.exists() || !f.isDirectory() || !wGlos.contains(m_glosRootField.getText()))
                m_writeableGlosField.setForeground(Color.RED);  

            f = new File(m_tmRootField.getText());
            if (!f.exists() || !f.isDirectory())
                m_tmRootField.setForeground(Color.RED);
            break;
        }

        updateUIText(m_messageArea);

        pack();

        setSize(9 * getWidth() / 8, getHeight() + 10);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dialogSize = getSize();
        setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);
    }

    /**
     * Browses for the directory.
     * 
     * @param browseTarget
     *            customizes the messages depening on what is browsed for
     * @param field
     *            text field to write browsed folder to
     */
    private void doBrowseDirectoy(int browseTarget, JTextField field) {
        String title;
        boolean fileMode = false;
        boolean glossaryFile = false;

        if (browseTarget == 6) {
            fileMode = true;
            glossaryFile = true;
        }

        switch (browseTarget) {
        case 1:
            title = OStrings.getString("PP_BROWSE_TITLE_SOURCE");
            break;

        case 2:
            title = OStrings.getString("PP_BROWSE_TITLE_TARGET");
            break;

        case 3:
            title = OStrings.getString("PP_BROWSE_TITLE_GLOS");
            break;

        case 4:
            title = OStrings.getString("PP_BROWSE_TITLE_TM");
            break;

        case 5:
            title = OStrings.getString("PP_BROWSE_TITLE_DICT");
            break;

        case 6:
            title = OStrings.getString("PP_BROWSE_W_GLOS");
            break;

            default:
            return;
        }

        OmegaTFileChooser browser = new OmegaTFileChooser();
        // String str = OStrings.getString("BUTTON_SELECT_NO_MNEMONIC");
        // browser.setApproveButtonText(str);
        browser.setDialogTitle(title);
        if (fileMode) {
            browser.setFileSelectionMode(OmegaTFileChooser.FILES_ONLY);
        } else {
            browser.setFileSelectionMode(OmegaTFileChooser.DIRECTORIES_ONLY);
        }

        // check if the current directory as specified by the field exists
        String curDir = (field != null) ? field.getText() : "";
        File curDirCheck = new File(curDir);
        if (fileMode && !StringUtil.isEmpty(curDirCheck.getName())) {
            String dirOnly = curDirCheck.getParent();
            dirOnly = (dirOnly != null) ? dirOnly : "";
            curDirCheck = new File(dirOnly);
        }

        // if the dir doesn't exist, use project dir and check if that exists
        if (!curDirCheck.exists() || !curDirCheck.isDirectory()) {
            curDir = projectProperties.getProjectRoot();
            curDirCheck = new File(curDir);
        }

        // if all fails, get last used dir from preferences
        if (!curDirCheck.exists() || !curDirCheck.isDirectory()) {
            switch (browseTarget) {
            case 1:
                curDir = Preferences.getPreference(Preferences.SOURCE_FOLDER);
                break;

            case 2:
                curDir = Preferences.getPreference(Preferences.TARGET_FOLDER);
                break;

            case 3:
                curDir = Preferences.getPreference(Preferences.GLOSSARY_FOLDER);
                break;

            case 4:
                curDir = Preferences.getPreference(Preferences.TM_FOLDER);
                break;

            case 5:
                curDir = Preferences.getPreference(Preferences.DICT_FOLDER);
                break;

            case 6:
                curDir = Preferences.getPreference(Preferences.GLOSSARY_FILE);
                break;

            }
        }

        if (fileMode) {
            File dirFile = new File(curDir);
            curDir = dirFile.getParent();
        }

        if (curDir.equals(""))
            curDir = Preferences.getPreference(Preferences.CURRENT_FOLDER);

        if (!curDir.equals("")) {
            File dir = new File(curDir);
            if (dir.exists() && dir.isDirectory()) {
                browser.setCurrentDirectory(dir);
            }
        }

        // show the browser
        int action = browser.showOpenDialog(this);

        // check if the selection has been approved
        if (action != javax.swing.JFileChooser.APPROVE_OPTION)
            return;

        // get the selected folder
        File dir = browser.getSelectedFile();
        if (dir == null)
            return;

        String str = dir.getAbsolutePath();
        if (!fileMode) {
            str+= File.separator; // Add file separator for directories
        }

        // The writeable glossary file must end with .txt or utf8
        if (glossaryFile && !str.endsWith(OConsts.EXT_TSV_TXT) &&!str.endsWith(OConsts.EXT_TSV_UTF8)) {
           str += OConsts.EXT_TSV_TXT; // Defaults to .txt
        }

        // reset appropriate path - store preferred directory
        switch (browseTarget) {
        case 1:
            Preferences.setPreference(Preferences.SOURCE_FOLDER, browser.getSelectedFile().getParent());
            projectProperties.setSourceRoot(str);
            field.setText(projectProperties.getSourceRoot());
            if (new File(projectProperties.getSourceRoot()).exists()
                    && new File(projectProperties.getSourceRoot()).isDirectory())
                field.setForeground(java.awt.SystemColor.textText);
            break;

        case 2:
            Preferences.setPreference(Preferences.TARGET_FOLDER, browser.getSelectedFile().getParent());
            projectProperties.setTargetRoot(str);
            field.setText(projectProperties.getTargetRoot());
            if (new File(projectProperties.getTargetRoot()).exists()
                    && new File(projectProperties.getTargetRoot()).isDirectory())
                field.setForeground(java.awt.SystemColor.textText);
            break;

        case 3:
            Preferences.setPreference(Preferences.GLOSSARY_FOLDER, browser.getSelectedFile().getParent());
            projectProperties.setGlossaryRoot(str);
            field.setText(projectProperties.getGlossaryRoot());
            if (new File(projectProperties.getGlossaryRoot()).exists()
                    && new File(projectProperties.getGlossaryRoot()).isDirectory())
                field.setForeground(java.awt.SystemColor.textText);
            break;

        case 4:
            Preferences.setPreference(Preferences.TM_FOLDER, browser.getSelectedFile().getParent());
            projectProperties.setTMRoot(str);
            field.setText(projectProperties.getTMRoot());
            if (new File(projectProperties.getTMRoot()).exists()
                    && new File(projectProperties.getTMRoot()).isDirectory())
                field.setForeground(java.awt.SystemColor.textText);
            break;

        case 5:
            Preferences.setPreference(Preferences.DICT_FOLDER, browser.getSelectedFile().getParent());
            projectProperties.setDictRoot(str);
            field.setText(projectProperties.getDictRoot());
            if (new File(projectProperties.getDictRoot()).exists()
                    && new File(projectProperties.getDictRoot()).isDirectory())
                field.setForeground(java.awt.SystemColor.textText);
            break;

        case 6:
            Preferences.setPreference(Preferences.GLOSSARY_FILE, browser.getSelectedFile().getPath());
            projectProperties.setWriteableGlossary(str);
            field.setText(projectProperties.getWriteableGlossary());
            // The writable glosssary file must be inside the glossary dir
            if (new File(projectProperties.getWriteableGlossaryDir()).exists()
                    && new File(projectProperties.getWriteableGlossaryDir()).isDirectory()
                    && projectProperties.getWriteableGlossaryDir().contains(projectProperties.getGlossaryRoot()))
                field.setForeground(java.awt.SystemColor.textText);
            break;

        }
    }

    private void doOK(JComboBox m_sourceLocaleField, JComboBox m_targetLocaleField,
            JCheckBox m_sentenceSegmentingCheckBox, JTextField m_srcRootField, JTextField m_locRootField,
            JTextField m_glosRootField, JTextField m_writeableGlosField, JTextField m_tmRootField, JTextField m_dictRootField,
            JCheckBox m_allowDefaultsCheckBox) {
        if (!ProjectProperties.verifySingleLangCode(m_sourceLocaleField.getSelectedItem().toString())) {
            JOptionPane.showMessageDialog(
                    this,
                    OStrings.getString("NP_INVALID_SOURCE_LOCALE")
                            + OStrings.getString("NP_LOCALE_SUGGESTION"), OStrings.getString("TF_ERROR"),
                    JOptionPane.ERROR_MESSAGE);
            m_sourceLocaleField.requestFocusInWindow();
            return;
        }
        projectProperties.setSourceLanguage(m_sourceLocaleField.getSelectedItem().toString());

        if (!ProjectProperties.verifySingleLangCode(m_targetLocaleField.getSelectedItem().toString())) {
            JOptionPane.showMessageDialog(
                    this,
                    OStrings.getString("NP_INVALID_TARGET_LOCALE")
                            + OStrings.getString("NP_LOCALE_SUGGESTION"), OStrings.getString("TF_ERROR"),
                    JOptionPane.ERROR_MESSAGE);
            m_targetLocaleField.requestFocusInWindow();
            return;
        }
        projectProperties.setTargetLanguage(m_targetLocaleField.getSelectedItem().toString());

        projectProperties.setSentenceSegmentingEnabled(m_sentenceSegmentingCheckBox.isSelected());
        
        projectProperties.setSupportDefaultTranslations(m_allowDefaultsCheckBox.isSelected());

        projectProperties.setSourceRoot(m_srcRootField.getText());
        if (!projectProperties.getSourceRoot().endsWith(File.separator))
            projectProperties.setSourceRoot(projectProperties.getSourceRoot() + File.separator);

        if (dialogType != NEW_PROJECT && !new File(projectProperties.getSourceRoot()).exists()) {
            JOptionPane.showMessageDialog(this, OStrings.getString("NP_SOURCEDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            m_srcRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setTargetRoot(m_locRootField.getText());
        if (!projectProperties.getTargetRoot().endsWith(File.separator))
            projectProperties.setTargetRoot(projectProperties.getTargetRoot() + File.separator);
        if (dialogType != NEW_PROJECT && !new File(projectProperties.getTargetRoot()).exists()) {
            JOptionPane.showMessageDialog(this, OStrings.getString("NP_TRANSDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            m_locRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setGlossaryRoot(m_glosRootField.getText());
        if (!projectProperties.getGlossaryRoot().endsWith(File.separator))
            projectProperties.setGlossaryRoot(projectProperties.getGlossaryRoot() + File.separator);
        if (dialogType != NEW_PROJECT && !new File(projectProperties.getGlossaryRoot()).exists()) {
            JOptionPane.showMessageDialog(this, OStrings.getString("NP_GLOSSDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            m_glosRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setWriteableGlossary(m_writeableGlosField.getText());
        if (dialogType != NEW_PROJECT && !new File(projectProperties.getWriteableGlossaryDir()).exists()) {
            JOptionPane.showMessageDialog(this, OStrings.getString("NP_W_GLOSSDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            m_writeableGlosField.requestFocusInWindow();
            return;
        }

        String glossaryDir = projectProperties.getWriteableGlossaryDir();
        if (!glossaryDir.endsWith(File.separator)) {
            glossaryDir += File.separator;
        }
        if (!glossaryDir.contains(projectProperties.getGlossaryRoot())) {
            JOptionPane.showMessageDialog(this, OStrings.getString("NP_W_GLOSDIR_NOT_INSIDE_GLOS"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            m_writeableGlosField.requestFocusInWindow();
            return;
        }

        projectProperties.setTMRoot(m_tmRootField.getText());
        if (!projectProperties.getTMRoot().endsWith(File.separator))
            projectProperties.setTMRoot(projectProperties.getTMRoot() + File.separator);
        if (dialogType != NEW_PROJECT && !new File(projectProperties.getTMRoot()).exists()) {
            JOptionPane.showMessageDialog(this, OStrings.getString("NP_TMDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            m_tmRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setDictRoot(m_dictRootField.getText());
        if (!projectProperties.getDictRoot().endsWith(File.separator))
            projectProperties.setDictRoot(projectProperties.getDictRoot() + File.separator);
        if (dialogType != NEW_PROJECT && !new File(projectProperties.getDictRoot()).exists()) {
            JOptionPane.showMessageDialog(this, OStrings.getString("NP_DICTDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            m_dictRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setProjectSRX(srx);
        projectProperties.setProjectFilters(filters);

        m_dialogCancelled = false;
        setVisible(false);
    }

    private void doCancel() {
        // delete project dir in case of a new project
        // to fix bug 1476591 the project root is created before everything else
        // and if the new project is cancelled, the project root still exists,
        // so it must be deleted
        if (dialogType == NEW_PROJECT)
            new File(projectProperties.getProjectRoot()).delete();

        m_dialogCancelled = true;
        setVisible(false);
    }

    private void updateUIText(JTextArea m_messageArea) {
        switch (dialogType) {
        case NEW_PROJECT:
            setTitle(OStrings.getString("PP_CREATE_PROJ"));
            m_messageArea.setText(OStrings.getString("PP_MESSAGE_CONFIGPROJ"));
            break;
        case RESOLVE_DIRS:
            setTitle(OStrings.getString("PP_OPEN_PROJ"));
            m_messageArea.setText(OStrings.getString("PP_MESSAGE_BADPROJ"));
            break;
        case EDIT_PROJECT:
            setTitle(OStrings.getString("PP_EDIT_PROJECT"));
            m_messageArea.setText(OStrings.getString("PP_MESSAGE_EDITPROJ"));
            break;
        }
    }

    /**
     * Whether the user cancelled the dialog.
     */
    private boolean m_dialogCancelled;

    /**
     * Return new properties or null if dialog cancelled.
     */
    public ProjectProperties getResult() {
        return m_dialogCancelled ? null : projectProperties;
    }
}
