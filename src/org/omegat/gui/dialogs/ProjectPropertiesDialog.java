/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008-2009 Alex Buloichik
               2011 Martin Fleurke
               2012 Didier Briel, Aaron Madlon-Kay
               2013 Aaron Madlon-Kay, Yu Tang
               2014-2015 Aaron Madlon-Kay
               2024 Hiroshi Miura
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.openide.awt.Mnemonics;

import org.omegat.CLIParameters;
import org.omegat.core.Core;
import org.omegat.core.data.CommandVarExpansion;
import org.omegat.core.data.ProjectProperties;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.LanguageComboBoxRenderer;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.TokenizerComboBoxRenderer;

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
 * @author Aaron Madlon-Kay
 * @author Yu Tang
 */
@SuppressWarnings("serial")
public class ProjectPropertiesDialog extends JDialog {

    public enum Mode {
        /** This dialog is used to create a new project. */
        NEW_PROJECT,
        /**
         * This dialog is used to resolve missing directories of existing
         * project (upon opening the project).
         */
        RESOLVE_DIRS,
        /**
         * This dialog is used to edit project's properties: where directories
         * reside, languages, etc.
         */
        EDIT_PROJECT
    }

    /**
     * The type of the dialog:
     * <ul>
     * <li>Creating project ==
     * {@link ProjectPropertiesDialog.Mode#NEW_PROJECT}
     * <li>Resolving the project's directories (existing project with some dirs
     * missing) == {@link ProjectPropertiesDialog.Mode#RESOLVE_DIRS}
     * <li>Editing project properties ==
     * {@link ProjectPropertiesDialog.Mode#EDIT_PROJECT}
     * </ul>
     */
    private final Mode dialogType;

    private final transient ProjectPropertiesDialogController controller;

    /**
     * Creates a dialog to create a new project / edit folders of existing one.
     *
     * @param projectProperties
     *            properties of the project
     * @param projFileName
     *            project file name
     * @param dialogTypeValue
     *            type of the dialog
     *            ({@link ProjectPropertiesDialog.Mode#NEW_PROJECT},
     *            {@link ProjectPropertiesDialog.Mode#RESOLVE_DIRS} or
     *            {@link ProjectPropertiesDialog.Mode#EDIT_PROJECT}).
     */
    public ProjectPropertiesDialog(Frame parent, final ProjectProperties projectProperties, String projFileName,
                                   Mode dialogTypeValue) {
        super(parent, true);
        this.dialogType = dialogTypeValue;
        initializeComponents();
        controller = new ProjectPropertiesDialogController(parent, this, dialogTypeValue, projectProperties);
        if (dialogType == Mode.RESOLVE_DIRS) {
            setResolveDirsDefaults();
        }
        setName(DIALOG_NAME);
        setLocationRelativeTo(parent);

        // Pack once to get the width...
        pack();
        updateUIText();
        // Then again to expand the height to accomodate the message.
        // This is needed because the height isn't known until the
        // amount of linewrapping is known.
        pack();
        // The result is still slightly too small on some LAFs, so enlarge
        // slightly with magic numbers.
        setSize(9 * getWidth() / 8, getHeight() + 10);
        setResizable(true);

        StaticUIUtils.fitInScreen(this);
    }

    private void initializeComponents() {
        Box centerBox = new ScrollableBox(BoxLayout.Y_AXIS);
        // Have to set background and opacity on OS X or else the entire dialog
        // is white.
        centerBox.setBackground(getBackground());
        centerBox.setOpaque(true);
        centerBox.setBorder(new EmptyBorder(5, 5, 5, 5));
        Box localesBox = createLocalesBox();
        centerBox.add(localesBox);

        // options
        centerBox.add(Box.createVerticalStrut(5));
        JPanel optionsBox = createOptionsBox();
        centerBox.add(optionsBox, BorderLayout.WEST);

        // directories
        centerBox.add(Box.createVerticalStrut(5));
        Box dirsBox = createDirsBox();
        centerBox.add(dirsBox);

        JScrollPane scrollPane = new JScrollPane(centerBox);
        // Prevent an ugly white viewport background with GTK LAF
        scrollPane.setBackground(getBackground());
        scrollPane.getViewport().setOpaque(false);
        getContentPane().add(scrollPane, "Center");

        Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK"));
        okButton.setName(OK_BUTTON_NAME);
        getRootPane().setDefaultButton(okButton);
        Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL"));
        cancelButton.setName(CANCEL_BUTTON_NAME);

        Box southBox = Box.createHorizontalBox();
        southBox.setBorder(new EmptyBorder(5, 5, 5, 5));
        southBox.add(Box.createHorizontalGlue());
        southBox.add(okButton);
        southBox.add(Box.createHorizontalStrut(5));
        southBox.add(cancelButton);
        getContentPane().add(southBox, "South");

        setResizable(false);
    }

    private Box createLocalesBox() {
        Border emptyBorder = new EmptyBorder(2, 0, 2, 0);
        // Source and target languages and tokenizers
        Box localesBox = Box.createHorizontalBox();
        localesBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                OStrings.getString("PP_LANGUAGES")));

        // Languages box
        Box bL = Box.createVerticalBox();
        localesBox.add(bL);

        // Source language label
        JLabel sourceLocaleLabel = new JLabel();
        Mnemonics.setLocalizedText(sourceLocaleLabel, OStrings.getString("PP_SRC_LANG"));
        Box bSL = Box.createHorizontalBox();
        bSL.setBorder(emptyBorder);
        bSL.add(sourceLocaleLabel);
        bSL.add(Box.createHorizontalGlue());
        bL.add(bSL);

        // Source language field
        sourceLocaleField = new JComboBox<>(new Vector<>(Language.getLanguages()));
        sourceLocaleField.setName(SOURCE_LOCALE_CB_NAME);
        if (sourceLocaleField.getMaximumRowCount() < 20) {
            sourceLocaleField.setMaximumRowCount(20);
        }
        sourceLocaleField.setEditable(true);
        sourceLocaleField.setRenderer(new LanguageComboBoxRenderer());
        bL.add(sourceLocaleField);

        // Target language label
        JLabel targetLocaleLabel = new JLabel();
        Mnemonics.setLocalizedText(targetLocaleLabel, OStrings.getString("PP_LOC_LANG"));
        Box bLL = Box.createHorizontalBox();
        bLL.setBorder(emptyBorder);
        bLL.add(targetLocaleLabel);
        bLL.add(Box.createHorizontalGlue());
        bL.add(bLL);

        // Target language field
        targetLocaleField = new JComboBox<>(new Vector<>(Language.getLanguages()));
        targetLocaleField.setName(TARGET_LOCALE_CB_NAME);
        if (targetLocaleField.getMaximumRowCount() < 20) {
            targetLocaleField.setMaximumRowCount(20);
        }
        targetLocaleField.setEditable(true);
        targetLocaleField.setRenderer(new LanguageComboBoxRenderer());
        bL.add(targetLocaleField);

        // Tokenizers box
        Box bT = Box.createVerticalBox();
        localesBox.add(bT);
        Class<?>[] tokenizers = PluginUtils.getTokenizerClasses().stream()
                .sorted(Comparator.comparing(Class::getName)).toArray(Class[]::new);

        // Source tokenizer label
        JLabel sourceTokenizerLabel = new JLabel();
        Mnemonics.setLocalizedText(sourceTokenizerLabel, OStrings.getString("PP_SRC_TOK"));
        Box bST = Box.createHorizontalBox();
        bST.setBorder(emptyBorder);
        bST.add(sourceTokenizerLabel);
        bST.add(Box.createHorizontalGlue());
        bT.add(bST);

        // Source tokenizer field
        sourceTokenizerField = new JComboBox<>(tokenizers);
        sourceTokenizerField.setName(SOURCE_TOKENIZER_FIELD_NAME);
        if (sourceTokenizerField.getMaximumRowCount() < 20) {
            sourceTokenizerField.setMaximumRowCount(20);
        }
        sourceTokenizerField.setEditable(false);
        sourceTokenizerField.setRenderer(new TokenizerComboBoxRenderer());
        bT.add(sourceTokenizerField);

        String cliTokSrc = Core.getParams().get(CLIParameters.TOKENIZER_SOURCE);
        if (cliTokSrc != null) {
            try {
                Class<?> srcTokClass = Class.forName(cliTokSrc);
                sourceTokenizerField.setEnabled(false);
                sourceTokenizerField.addItem(srcTokClass);
                sourceTokenizerField.setSelectedItem(cliTokSrc);
            } catch (ClassNotFoundException | LinkageError ex) {
                Log.log(ex);
            }
        }

        // Target tokenizer label
        JLabel targetTokenizerLabel = new JLabel();
        Mnemonics.setLocalizedText(targetTokenizerLabel, OStrings.getString("PP_LOC_TOK"));
        Box bTT = Box.createHorizontalBox();
        bTT.setBorder(emptyBorder);
        bTT.add(targetTokenizerLabel);
        bTT.add(Box.createHorizontalGlue());
        bT.add(bTT);

        // Target tokenizer field
        targetTokenizerField = new JComboBox<>(tokenizers);
        targetTokenizerField.setName(TARGET_TOKENIZER_FIELD_NAME);
        if (targetTokenizerField.getMaximumRowCount() < 20) {
            targetTokenizerField.setMaximumRowCount(20);
        }
        targetTokenizerField.setEditable(false);
        targetTokenizerField.setRenderer(new TokenizerComboBoxRenderer());
        bT.add(targetTokenizerField);

        String cliTokTrg = Core.getParams().get(CLIParameters.TOKENIZER_TARGET);
        if (cliTokTrg != null) {
            try {
                Class<?> trgTokClass = Class.forName(cliTokTrg);
                targetTokenizerField.setEnabled(false);
                targetTokenizerField.addItem(trgTokClass);
                targetTokenizerField.setSelectedItem(cliTokTrg);
            } catch (ClassNotFoundException | LinkageError ex) {
                Log.log(ex);
            }

        }
        return localesBox;
    }

    private JPanel createOptionsBox() {
        JPanel optionsBox = new JPanel(new GridBagLayout());
        optionsBox.setBorder(new EtchedBorder());
        optionsBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                OStrings.getString("PP_OPTIONS")));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);

        // sentence-segmenting
        Mnemonics.setLocalizedText(sentenceSegmentingCheckBox, OStrings.getString("PP_SENTENCE_SEGMENTING"));
        sentenceSegmentingCheckBox.setName(SENTENCE_SEGMENTING_CB_NAME);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        optionsBox.add(sentenceSegmentingCheckBox, gbc);

        Mnemonics.setLocalizedText(sentenceSegmentingButton, OStrings.getString("MW_OPTIONSMENU_LOCAL_SENTSEG"));
        sentenceSegmentingButton.setName(SENTENCE_SEGMENTING_BUTTON_NAME);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        optionsBox.add(sentenceSegmentingButton, gbc);

        // File Filters
        Mnemonics.setLocalizedText(fileFiltersButton, OStrings.getString("TF_MENU_DISPLAY_LOCAL_FILTERS"));
        fileFiltersButton.setName(FILE_FILTER_BUTTON_NAME);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        optionsBox.add(fileFiltersButton, gbc);

        // Repositories mapping
        Mnemonics.setLocalizedText(repositoriesButton, OStrings.getString("PP_REPOSITORIES"));
        repositoriesButton.setName(REPOSITORIES_BUTTON_NAME);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        optionsBox.add(repositoriesButton, gbc);

        // Repositories mapping
        Mnemonics.setLocalizedText(externalFinderButton, OStrings.getString("PP_EXTERNALFINDER"));
        externalFinderButton.setName(EXTERNAL_FINDER_BUTTON_NAME);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        optionsBox.add(externalFinderButton, gbc);

        // multiple translations
        Mnemonics.setLocalizedText(allowDefaultsCheckBox, OStrings.getString("PP_ALLOW_DEFAULTS"));
        allowDefaultsCheckBox.setName(ALLOW_DEFAULTS_CB_NAME);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        optionsBox.add(allowDefaultsCheckBox, gbc);

        // Remove Tags
        Mnemonics.setLocalizedText(removeTagsCheckBox, OStrings.getString("PP_REMOVE_TAGS"));
        removeTagsCheckBox.setName(REMOVE_TAGS_CB_NAME);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        optionsBox.add(removeTagsCheckBox, gbc);

        // Post-processing
        JLabel externalCommandLabel = new JLabel();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        optionsBox.add(externalCommandLabel, gbc);
        externalCommandTextArea.setRows(2);
        externalCommandTextArea.setLineWrap(true);
        if (Preferences.isPreference(Preferences.ALLOW_PROJECT_EXTERN_CMD)) {
            Mnemonics.setLocalizedText(externalCommandLabel, OStrings.getString("PP_EXTERNAL_COMMAND"));
        } else {
            Mnemonics.setLocalizedText(externalCommandLabel, OStrings.getString("PP_EXTERN_CMD_DISABLED"));
            externalCommandTextArea.setEditable(false);
            externalCommandTextArea.setToolTipText(OStrings.getString("PP_EXTERN_CMD_DISABLED_TOOLTIP"));
            externalCommandLabel.setToolTipText(OStrings.getString("PP_EXTERN_CMD_DISABLED_TOOLTIP"));
            externalCommandTextArea.setBackground(getBackground());
        }
        externalCommandTextArea.setName(EXTERNAL_COMMAND_TEXTAREA_NAME);
        final JScrollPane externalCommandScrollPane = new JScrollPane();
        externalCommandScrollPane.setViewportView(externalCommandTextArea);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        optionsBox.add(externalCommandScrollPane, gbc);
        variablesList = new JComboBox<>(new Vector<>(CommandVarExpansion.getCommandVariables()));
        variablesList.setName(VARIABLE_LIST_NAME);
        // Add variable insertion controls only if project external commands are
        // enabled.
        if (Preferences.isPreference(Preferences.ALLOW_PROJECT_EXTERN_CMD)) {
            Border emptyBorder = new EmptyBorder(2, 0, 2, 0);
            Box bIC = Box.createHorizontalBox();
            bIC.setBorder(emptyBorder);
            Mnemonics.setLocalizedText(variablesLabel,
                    OStrings.getString("EXT_TMX_MATCHES_TEMPLATE_VARIABLES"));
            bIC.add(variablesLabel);
            bIC.add(variablesList);
            Mnemonics.setLocalizedText(insertButton, OStrings.getString("BUTTON_INSERT"));
            insertButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    externalCommandTextArea.replaceSelection(variablesList.getSelectedItem().toString());
                }
            });
            bIC.add(insertButton);
            bIC.add(Box.createHorizontalGlue());
            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.weightx = 1;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            optionsBox.add(bIC, gbc);
        }
        return optionsBox;
    }

    private Box createDirsBox() {
        Border emptyBorder = new EmptyBorder(2, 0, 2, 0);
        Box dirsBox = Box.createVerticalBox();
        dirsBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                OStrings.getString("PP_DIRECTORIES")));

        JLabel srcRootLabel = new JLabel();
        Mnemonics.setLocalizedText(srcRootLabel, OStrings.getString("PP_SRC_ROOT"));
        Box bSrc = Box.createHorizontalBox();
        bSrc.setBorder(emptyBorder);
        bSrc.add(srcRootLabel);
        bSrc.add(Box.createHorizontalGlue());
        Mnemonics.setLocalizedText(srcExcludesBtn, OStrings.getString("PP_BUTTON_BROWSE_SRC_EXCLUDES"));
        srcExcludesBtn.setName(SRC_EXCLUDES_BUTTON_NAME);
        bSrc.add(srcExcludesBtn);
        Mnemonics.setLocalizedText(srcBrowse, OStrings.getString("PP_BUTTON_BROWSE_SRC"));
        bSrc.add(srcBrowse);
        dirsBox.add(bSrc);
        dirsBox.add(srcRootField);

        JLabel tmRootLabel = new JLabel();
        Mnemonics.setLocalizedText(tmRootLabel, OStrings.getString("PP_TM_ROOT"));
        Box bTM = Box.createHorizontalBox();
        bTM.setBorder(emptyBorder);
        bTM.add(tmRootLabel);
        bTM.add(Box.createHorizontalGlue());
        Mnemonics.setLocalizedText(tmBrowse, OStrings.getString("PP_BUTTON_BROWSE_TM"));
        tmBrowse.setName(TM_BROWSE_BUTTON_NAME);
        bTM.add(tmBrowse);
        dirsBox.add(bTM);
        dirsBox.add(tmRootField);

        JLabel glosRootLabel = new JLabel();
        Mnemonics.setLocalizedText(glosRootLabel, OStrings.getString("PP_GLOS_ROOT"));
        Box bGlos = Box.createHorizontalBox();
        bGlos.setBorder(emptyBorder);
        bGlos.add(glosRootLabel);
        bGlos.add(Box.createHorizontalGlue());
        Mnemonics.setLocalizedText(glosBrowse, OStrings.getString("PP_BUTTON_BROWSE_GL"));
        glosBrowse.setName(GLOSSARY_BROWSE_BUTTON_NAME);
        bGlos.add(glosBrowse);
        dirsBox.add(bGlos);
        dirsBox.add(glosRootField);

        JLabel writeableGlosLabel = new JLabel();
        Mnemonics.setLocalizedText(writeableGlosLabel, OStrings.getString("PP_WRITEABLE_GLOS"));
        Box bwGlos = Box.createHorizontalBox();
        bwGlos.setBorder(emptyBorder);
        bwGlos.add(writeableGlosLabel);
        bwGlos.add(Box.createHorizontalGlue());
        Mnemonics.setLocalizedText(wGlosBrowse, OStrings.getString("PP_BUTTON_BROWSE_WG"));
        wGlosBrowse.setName(WRITABLE_GLOSSARY_BROWSE_BUTTON_NAME);
        bwGlos.add(wGlosBrowse);
        dirsBox.add(bwGlos);
        dirsBox.add(writeableGlosField);

        JLabel locDictLabel = new JLabel();
        Mnemonics.setLocalizedText(locDictLabel, OStrings.getString("PP_DICT_ROOT"));
        Box bDict = Box.createHorizontalBox();
        bDict.setBorder(emptyBorder);
        bDict.add(locDictLabel);
        bDict.add(Box.createHorizontalGlue());
        Mnemonics.setLocalizedText(dictBrowse, OStrings.getString("PP_BUTTON_BROWSE_DICT"));
        dictBrowse.setName(DICTIONARY_BROWSE_BUTTON_NAME);
        bDict.add(dictBrowse);
        dirsBox.add(bDict);
        dirsBox.add(dictRootField);

        JLabel locRootLabel = new JLabel();
        Mnemonics.setLocalizedText(locRootLabel, OStrings.getString("PP_LOC_ROOT"));
        Box bLoc = Box.createHorizontalBox();
        bLoc.setBorder(emptyBorder);
        bLoc.add(locRootLabel);
        bLoc.add(Box.createHorizontalGlue());
        Mnemonics.setLocalizedText(locBrowse, OStrings.getString("PP_BUTTON_BROWSE_TAR"));
        locBrowse.setName(LOC_BROWSE_BUTTON_NAME);
        bLoc.add(locBrowse);
        dirsBox.add(bLoc);
        dirsBox.add(locRootField);

        JLabel exportTMRootLabel = new JLabel();
        Mnemonics.setLocalizedText(exportTMRootLabel, OStrings.getString("PP_EXPORT_TM_ROOT"));
        Box bExpTM = Box.createHorizontalBox();
        bExpTM.setBorder(emptyBorder);
        bExpTM.add(exportTMRootLabel);
        bExpTM.add(Box.createHorizontalGlue());
        Mnemonics.setLocalizedText(exportTMBrowse, OStrings.getString("PP_BUTTON_BROWSE_EXP_TM"));
        exportTMBrowse.setName(EXPORT_TM_BROWSE_BUTTON_NAME);
        bExpTM.add(exportTMBrowse);
        // Supply check boxes to choose which TM formats to export
        exportTMOmegaTCheckBox = new JCheckBox(OStrings.getString("PP_EXPORT_TM_OMEGAT"));
        exportTMLevel1CheckBox = new JCheckBox(OStrings.getString("PP_EXPORT_TM_LEVEL1"));
        exportTMLevel2CheckBox = new JCheckBox(OStrings.getString("PP_EXPORT_TM_LEVEL2"));
        exportTMOmegaTCheckBox.setName(EXPORT_TM_OMEGAT_CB_NAME);
        exportTMLevel1CheckBox.setName(EXPORT_TM_LEVEL1_CB_NAME);
        exportTMLevel2CheckBox.setName(EXPORT_TM_LEVEL2_CB_NAME);

        JPanel exportTMPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        JLabel cbExportLabel = new JLabel(OStrings.getString("PP_EXPORT_TM_LEVELS"));

        dirsBox.add(bExpTM);
        exportTMRootField.setName(EXPORT_TM_ROOT_FIELD_NAME);
        dirsBox.add(exportTMRootField);
        exportTMPanel.add(cbExportLabel);
        exportTMPanel.add(exportTMOmegaTCheckBox);
        exportTMPanel.add(exportTMLevel1CheckBox);
        exportTMPanel.add(exportTMLevel2CheckBox);
        dirsBox.add(exportTMPanel);

        return dirsBox;
    }

    private void setResolveDirsDefaults() {
        // disabling some controls
        sourceLocaleField.setEnabled(false);
        targetLocaleField.setEnabled(false);
        sourceTokenizerField.setEnabled(false);
        targetTokenizerField.setEnabled(false);
        sentenceSegmentingCheckBox.setEnabled(false);
        allowDefaultsCheckBox.setEnabled(false);
        removeTagsCheckBox.setEnabled(false);
        externalCommandTextArea.setEnabled(false);
        insertButton.setEnabled(false);
        variablesList.setEnabled(false);
        exportTMOmegaTCheckBox.setEnabled(false);
        exportTMLevel1CheckBox.setEnabled(false);
        exportTMLevel2CheckBox.setEnabled(false);

        // marking missing folder RED
        File f = new File(srcRootField.getText());
        if (!f.isDirectory()) {
            srcRootField.setForeground(Color.RED);
        }
        f = new File(locRootField.getText());
        if (!f.isDirectory()) {
            locRootField.setForeground(Color.RED);
        }
        f = new File(glosRootField.getText());
        if (!f.isDirectory()) {
            glosRootField.setForeground(Color.RED);
        }
        f = new File(writeableGlosField.getText());
        File wGlos = f.getParentFile(); // Remove the file name
        // The writeable glossary must be in in the /glossary folder
        if (!wGlos.isDirectory() || !wGlos.equals(new File(glosRootField.getText()))) {
            writeableGlosField.setForeground(Color.RED);
        }
        f = new File(tmRootField.getText());
        if (!f.isDirectory()) {
            tmRootField.setForeground(Color.RED);
        }
        f = new File(exportTMRootField.getText());
        if (!f.isDirectory()) {
            exportTMRootField.setForeground(Color.RED);
        }
        f = new File(dictRootField.getText());
        if (!f.isDirectory()) {
            dictRootField.setForeground(Color.RED);
        }
    }

    private void updateUIText() {
        switch (dialogType) {
        case NEW_PROJECT:
            setTitle(OStrings.getString("PP_CREATE_PROJ"));
            break;
        case RESOLVE_DIRS:
            setTitle(OStrings.getString("PP_OPEN_PROJ"));
            break;
        case EDIT_PROJECT:
            setTitle(OStrings.getString("PP_EDIT_PROJECT"));
            break;
        }
    }

    private static class ScrollableBox extends Box implements Scrollable {

        ScrollableBox(int axis) {
            super(axis);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return getFont().getSize();
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return getFont().getSize();
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

    }

    /**
     * Return new properties or null if dialog cancelled.
     */
    public ProjectProperties getResult() {
        return controller.getResult();

    }

    Mode getDialogType() {
        return dialogType;
    }

    // multiple translations
    JCheckBox allowDefaultsCheckBox = new JCheckBox();

    // Remove Tags
    JCheckBox removeTagsCheckBox = new JCheckBox();
    JButton exportTMBrowse = new JButton();
    JButton sentenceSegmentingButton = new JButton();

    // File Filters
    JButton fileFiltersButton = new JButton();
    JTextField exportTMRootField = new JTextField();
    JCheckBox exportTMOmegaTCheckBox;
    JCheckBox exportTMLevel1CheckBox;
    JCheckBox exportTMLevel2CheckBox;
    JButton srcExcludesBtn = new JButton();
    JTextField srcRootField = new JTextField();
    JTextField tmRootField = new JTextField();
    JTextField locRootField = new JTextField();

    // sentence-segmenting
    JCheckBox sentenceSegmentingCheckBox = new JCheckBox();
    JButton okButton = new JButton();
    JButton wGlosBrowse = new JButton();
    JButton srcBrowse = new JButton();
    JButton tmBrowse = new JButton();
    JButton insertButton = new JButton();
    JButton cancelButton = new JButton();
    JComboBox<Class<?>> targetTokenizerField;
    JComboBox<Language> targetLocaleField;
    JComboBox<Class<?>> sourceTokenizerField;
    JComboBox<Language> sourceLocaleField;

    // Repositories mapping
    JButton repositoriesButton = new JButton();

    // Repositories mapping
    JButton externalFinderButton = new JButton();

    JTextField glosRootField = new JTextField();
    JTextField writeableGlosField = new JTextField();
    JTextField dictRootField = new JTextField();
    JTextArea externalCommandTextArea = new JTextArea();
    JButton locBrowse = new JButton();
    JButton glosBrowse = new JButton();
    JButton dictBrowse = new JButton();

    // extern command
    JLabel variablesLabel = new javax.swing.JLabel();
    JComboBox<String> variablesList;

    // component name definitions for ui test.
    public static final String DIALOG_NAME = "project_properties_dialog";
    public static final String OK_BUTTON_NAME = "project_properties_ok_button";
    public static final String CANCEL_BUTTON_NAME = "project_properties_cancel_button";
    public static final String SENTENCE_SEGMENTING_CB_NAME = "project_properties_sentence_segmenting_cb";
    public static final String SENTENCE_SEGMENTING_BUTTON_NAME = "project_properties_sentence_segmenting_button";
    public static final String ALLOW_DEFAULTS_CB_NAME = "project_properties_allow_defaults_cb";
    public static final String REMOVE_TAGS_CB_NAME = "project_properties_remove_tags_cb";
    public static final String EXPORT_TM_BROWSE_BUTTON_NAME = "project_properties_export_tm_browse_button";
    public static final String FILE_FILTER_BUTTON_NAME = "project_properties_file_filter_button";
    public static final String EXPORT_TM_ROOT_FIELD_NAME = "project_properties_export_tm_root_field";
    public static final String EXPORT_TM_OMEGAT_CB_NAME = "project_properties_export_tm_omegat_cb";
    public static final String EXPORT_TM_LEVEL1_CB_NAME = "project_properties_export_tm_level1_cb";
    public static final String EXPORT_TM_LEVEL2_CB_NAME = "project_properties_export_tm_level2_cb";
    public static final String REPOSITORIES_BUTTON_NAME = "project_properties_repositories_button";
    public static final String EXTERNAL_FINDER_BUTTON_NAME = "project_properties_external_finder_button";
    public static final String EXTERNAL_COMMAND_TEXTAREA_NAME =
            "project_properties_external_command_textarea";
    public static final String VARIABLE_LIST_NAME = "project_properties_variable_list";
    public static final String SRC_EXCLUDES_BUTTON_NAME = "project_properties_src_excludes_button";
    public static final String TM_BROWSE_BUTTON_NAME = "project_properties_tm_browse";
    public static final String GLOSSARY_BROWSE_BUTTON_NAME = "project_properties_glossary_browse_button";
    public static final String WRITABLE_GLOSSARY_BROWSE_BUTTON_NAME =
            "project_properties_writable_glossary_browse_button";
    public static final String LOC_BROWSE_BUTTON_NAME = "project_properties_loc_browse_button";
    public static final String DICTIONARY_BROWSE_BUTTON_NAME = "project_properties_dictionary_browse_button";
    public static final String SOURCE_TOKENIZER_FIELD_NAME = "project_properties_source_tokenizer_field";
    public static final String TARGET_TOKENIZER_FIELD_NAME = "project_properties_target_tokenizer_field";
    public static final String SOURCE_LOCALE_CB_NAME = "project_properties_source_locale_cb";
    public static final String TARGET_LOCALE_CB_NAME = "project_properties_target_locale_cb";
}
