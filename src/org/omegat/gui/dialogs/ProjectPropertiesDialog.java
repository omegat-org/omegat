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
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import org.omegat.core.segmentation.SRX;
import org.omegat.externalfinder.ExternalFinder;
import org.omegat.externalfinder.gui.ExternalFinderCustomizer;
import org.omegat.externalfinder.item.ExternalFinderConfiguration;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.filters2.FiltersCustomizer;
import org.omegat.gui.segmentation.SegmentationCustomizer;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.LanguageComboBoxRenderer;
import org.omegat.util.gui.OmegaTFileChooser;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.TokenizerComboBoxRenderer;

import gen.core.filters.Filters;
import gen.core.project.RepositoryDefinition;

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
    private ProjectProperties projectProperties;

    public enum Mode {
        /** This dialog is used to create a new project. */
        NEW_PROJECT,
        /**
         * This dialog is used to resolve missing directories of existing project
         * (upon opening the project).
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
     * <li>Creating project == {@link Mode#NEW_PROJECT}
     * <li>Resolving the project's directories (existing project with some dirs
     * missing) == {@link Mode#RESOLVE_DIRS}
     * <li>Editing project properties == {@link Mode#EDIT_PROJECT}
     * </ul>
     */
    private Mode dialogType;

    /** Project SRX. */
    private SRX srx;

    /** Project filters. */
    private Filters filters;

    /** Project ExternalFinder config */
    private ExternalFinderConfiguration externalFinderConfig;

    private List<String> srcExcludes = new ArrayList<>();

    /**
     * Creates a dialog to create a new project / edit folders of existing one.
     *
     * @param projectProperties
     *            properties of the project
     * @param projFileName
     *            project file name
     * @param dialogTypeValue
     *            type of the dialog ({@link Mode#NEW_PROJECT}, {@link Mode#RESOLVE_DIRS} or {@link Mode#EDIT_PROJECT}).
     */
    // CHECKSTYLE:OFF
    public ProjectPropertiesDialog(Frame parent, final ProjectProperties projectProperties, String projFileName,
            Mode dialogTypeValue) {
        super(parent, true);
        this.projectProperties = projectProperties;
        this.srx = projectProperties.getProjectSRX();
        this.dialogType = dialogTypeValue;
        this.filters = projectProperties.getProjectFilters();
        srcExcludes.addAll(projectProperties.getSourceRootExcludes());
        externalFinderConfig = ExternalFinder.getProjectConfig();

        Border emptyBorder = new EmptyBorder(2, 0, 2, 0);
        Box centerBox = new ScrollableBox(BoxLayout.Y_AXIS);
        // Have to set background and opacity on OS X or else entire dialog is white.
        centerBox.setBackground(getBackground());
        centerBox.setOpaque(true);
        centerBox.setBorder(new EmptyBorder(5, 5, 5, 5));

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
        final JComboBox<Language> sourceLocaleField = new JComboBox<>(new Vector<>(Language.getLanguages()));
        if (sourceLocaleField.getMaximumRowCount() < 20) {
            sourceLocaleField.setMaximumRowCount(20);
        }
        sourceLocaleField.setEditable(true);
        sourceLocaleField.setRenderer(new LanguageComboBoxRenderer());
        sourceLocaleField.setSelectedItem(projectProperties.getSourceLanguage());
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
        final JComboBox<Language> targetLocaleField = new JComboBox<>(new Vector<>(Language.getLanguages()));
        if (targetLocaleField.getMaximumRowCount() < 20) {
            targetLocaleField.setMaximumRowCount(20);
        }
        targetLocaleField.setEditable(true);
        targetLocaleField.setRenderer(new LanguageComboBoxRenderer());
        targetLocaleField.setSelectedItem(projectProperties.getTargetLanguage());
        bL.add(targetLocaleField);

        // Tokenizers box
        Box bT = Box.createVerticalBox();
        localesBox.add(bT);
        Class<?>[] tokenizers = PluginUtils.getTokenizerClasses().stream().sorted(Comparator.comparing(Class::getName))
                .toArray(Class[]::new);

        // Source tokenizer label
        JLabel sourceTokenizerLabel = new JLabel();
        Mnemonics.setLocalizedText(sourceTokenizerLabel, OStrings.getString("PP_SRC_TOK"));
        Box bST = Box.createHorizontalBox();
        bST.setBorder(emptyBorder);
        bST.add(sourceTokenizerLabel);
        bST.add(Box.createHorizontalGlue());
        bT.add(bST);

        // Source tokenizer field
        final JComboBox<Class<?>> sourceTokenizerField = new JComboBox<>(tokenizers);
        if (sourceTokenizerField.getMaximumRowCount() < 20) {
            sourceTokenizerField.setMaximumRowCount(20);
        }
        sourceTokenizerField.setEditable(false);
        sourceTokenizerField.setRenderer(new TokenizerComboBoxRenderer());
        sourceTokenizerField.setSelectedItem(projectProperties.getSourceTokenizer());
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

        ActionListener sourceLocaleListener = e -> {
            if (!sourceLocaleField.isEnabled()) {
                return;
            }
            Object newLang = sourceLocaleField.getSelectedItem();
            if (newLang instanceof String) {
                newLang = new Language((String) newLang);
            }
            Class<?> newTok = PluginUtils.getTokenizerClassForLanguage((Language) newLang);
            sourceTokenizerField.setSelectedItem(newTok);
        };
        sourceLocaleField.addActionListener(sourceLocaleListener);

        // Target tokenizer label
        JLabel targetTokenizerLabel = new JLabel();
        Mnemonics.setLocalizedText(targetTokenizerLabel, OStrings.getString("PP_LOC_TOK"));
        Box bTT = Box.createHorizontalBox();
        bTT.setBorder(emptyBorder);
        bTT.add(targetTokenizerLabel);
        bTT.add(Box.createHorizontalGlue());
        bT.add(bTT);

        // Target tokenizer field
        final JComboBox<Class<?>> targetTokenizerField = new JComboBox<>(tokenizers);
        if (targetTokenizerField.getMaximumRowCount() < 20) {
            targetTokenizerField.setMaximumRowCount(20);
        }
        targetTokenizerField.setEditable(false);
        targetTokenizerField.setRenderer(new TokenizerComboBoxRenderer());
        targetTokenizerField.setSelectedItem(projectProperties.getTargetTokenizer());
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

        ActionListener targetLocaleListener = e -> {
            if (!targetLocaleField.isEnabled()) {
                return;
            }
            Object newLang = targetLocaleField.getSelectedItem();
            if (newLang instanceof String) {
                newLang = new Language((String) newLang);
            }
            Class<?> newTok = PluginUtils.getTokenizerClassForLanguage((Language) newLang);
            targetTokenizerField.setSelectedItem(newTok);
        };
        targetLocaleField.addActionListener(targetLocaleListener);

        centerBox.add(localesBox);

        if (dialogTypeValue == Mode.NEW_PROJECT) {
            // Infer appropriate tokenizers from source languages
            sourceLocaleListener.actionPerformed(null);
            targetLocaleListener.actionPerformed(null);
        }

        // options
        centerBox.add(Box.createVerticalStrut(5));
        JPanel optionsBox = new JPanel(new GridBagLayout());
        optionsBox.setBorder(new EtchedBorder());
        optionsBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                OStrings.getString("PP_OPTIONS")));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);

        // sentence-segmenting
        final JCheckBox sentenceSegmentingCheckBox = new JCheckBox();
        Mnemonics
                .setLocalizedText(sentenceSegmentingCheckBox, OStrings.getString("PP_SENTENCE_SEGMENTING"));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        optionsBox.add(sentenceSegmentingCheckBox, gbc);

        JButton sentenceSegmentingButton = new JButton();
        Mnemonics.setLocalizedText(sentenceSegmentingButton, OStrings.getString("MW_OPTIONSMENU_LOCAL_SENTSEG"));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        optionsBox.add(sentenceSegmentingButton, gbc);

        // File Filters
        JButton fileFiltersButton = new JButton();
        Mnemonics.setLocalizedText(fileFiltersButton, OStrings.getString("TF_MENU_DISPLAY_LOCAL_FILTERS"));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        optionsBox.add(fileFiltersButton, gbc);

        // Repositories mapping
        JButton repositoriesButton = new JButton();
        Mnemonics.setLocalizedText(repositoriesButton, OStrings.getString("PP_REPOSITORIES"));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        optionsBox.add(repositoriesButton, gbc);

        // Repositories mapping
        JButton externalFinderButton = new JButton();
        Mnemonics.setLocalizedText(externalFinderButton, OStrings.getString("PP_EXTERNALFINDER"));
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        optionsBox.add(externalFinderButton, gbc);

        // multiple translations
        final JCheckBox allowDefaultsCheckBox = new JCheckBox();
        Mnemonics.setLocalizedText(allowDefaultsCheckBox, OStrings.getString("PP_ALLOW_DEFAULTS"));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        optionsBox.add(allowDefaultsCheckBox, gbc);

        // Remove Tags
        final JCheckBox removeTagsCheckBox = new JCheckBox();
        Mnemonics.setLocalizedText(removeTagsCheckBox, OStrings.getString("PP_REMOVE_TAGS"));
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
        final JTextArea externalCommandTextArea = new JTextArea();
        externalCommandTextArea.setRows(2);
        externalCommandTextArea.setLineWrap(true);
        externalCommandTextArea.setText(projectProperties.getExternalCommand());
        if (Preferences.isPreference(Preferences.ALLOW_PROJECT_EXTERN_CMD)) {
            Mnemonics.setLocalizedText(externalCommandLabel, OStrings.getString("PP_EXTERNAL_COMMAND"));
        } else {
            Mnemonics.setLocalizedText(externalCommandLabel, OStrings.getString("PP_EXTERN_CMD_DISABLED"));
            externalCommandTextArea.setEditable(false);
            externalCommandTextArea.setToolTipText(OStrings.getString("PP_EXTERN_CMD_DISABLED_TOOLTIP"));
            externalCommandLabel.setToolTipText(OStrings.getString("PP_EXTERN_CMD_DISABLED_TOOLTIP"));
            externalCommandTextArea.setBackground(getBackground());
        }
        final JScrollPane externalCommandScrollPane = new JScrollPane();
        externalCommandScrollPane.setViewportView(externalCommandTextArea);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        optionsBox.add(externalCommandScrollPane, gbc);
        final JLabel variablesLabel = new javax.swing.JLabel();
        final JComboBox<String> variablesList = new JComboBox<>(
                new Vector<>(CommandVarExpansion.getCommandVariables()));
        final JButton insertButton = new javax.swing.JButton();
        // Add variable insertion controls only if project external commands are enabled.
        if (Preferences.isPreference(Preferences.ALLOW_PROJECT_EXTERN_CMD)) {
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

        centerBox.add(optionsBox, BorderLayout.WEST);

        // directories
        centerBox.add(Box.createVerticalStrut(5));

        Box dirsBox = Box.createVerticalBox();
        dirsBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                OStrings.getString("PP_DIRECTORIES")));

        JLabel srcRootLabel = new JLabel();
        Mnemonics.setLocalizedText(srcRootLabel, OStrings.getString("PP_SRC_ROOT"));
        Box bSrc = Box.createHorizontalBox();
        bSrc.setBorder(emptyBorder);
        bSrc.add(srcRootLabel);
        bSrc.add(Box.createHorizontalGlue());
        JButton srcExcludesBtn = new JButton();
        Mnemonics.setLocalizedText(srcExcludesBtn, OStrings.getString("PP_BUTTON_BROWSE_SRC_EXCLUDES"));
        bSrc.add(srcExcludesBtn);
        JButton srcBrowse = new JButton();
        Mnemonics.setLocalizedText(srcBrowse, OStrings.getString("PP_BUTTON_BROWSE_SRC"));
        bSrc.add(srcBrowse);
        final JTextField srcRootField = new JTextField();
        dirsBox.add(bSrc);
        dirsBox.add(srcRootField);

        JLabel tmRootLabel = new JLabel();
        Mnemonics.setLocalizedText(tmRootLabel, OStrings.getString("PP_TM_ROOT"));
        Box bTM = Box.createHorizontalBox();
        bTM.setBorder(emptyBorder);
        bTM.add(tmRootLabel);
        bTM.add(Box.createHorizontalGlue());
        JButton tmBrowse = new JButton();
        Mnemonics.setLocalizedText(tmBrowse, OStrings.getString("PP_BUTTON_BROWSE_TM"));
        bTM.add(tmBrowse);
        final JTextField tmRootField = new JTextField();
        dirsBox.add(bTM);
        dirsBox.add(tmRootField);

        JLabel glosRootLabel = new JLabel();
        Mnemonics.setLocalizedText(glosRootLabel, OStrings.getString("PP_GLOS_ROOT"));
        Box bGlos = Box.createHorizontalBox();
        bGlos.setBorder(emptyBorder);
        bGlos.add(glosRootLabel);
        bGlos.add(Box.createHorizontalGlue());
        JButton glosBrowse = new JButton();
        Mnemonics.setLocalizedText(glosBrowse, OStrings.getString("PP_BUTTON_BROWSE_GL"));
        bGlos.add(glosBrowse);
        final JTextField glosRootField = new JTextField();
        dirsBox.add(bGlos);
        dirsBox.add(glosRootField);

        JLabel writeableGlosLabel = new JLabel();
        Mnemonics.setLocalizedText(writeableGlosLabel, OStrings.getString("PP_WRITEABLE_GLOS"));
        Box bwGlos = Box.createHorizontalBox();
        bwGlos.setBorder(emptyBorder);
        bwGlos.add(writeableGlosLabel);
        bwGlos.add(Box.createHorizontalGlue());
        JButton wGlosBrowse = new JButton();
        Mnemonics.setLocalizedText(wGlosBrowse, OStrings.getString("PP_BUTTON_BROWSE_WG"));
        bwGlos.add(wGlosBrowse);
        final JTextField writeableGlosField = new JTextField();
        dirsBox.add(bwGlos);
        dirsBox.add(writeableGlosField);

        JLabel locDictLabel = new JLabel();
        Mnemonics.setLocalizedText(locDictLabel, OStrings.getString("PP_DICT_ROOT"));
        Box bDict = Box.createHorizontalBox();
        bDict.setBorder(emptyBorder);
        bDict.add(locDictLabel);
        bDict.add(Box.createHorizontalGlue());
        JButton dictBrowse = new JButton();
        Mnemonics.setLocalizedText(dictBrowse, OStrings.getString("PP_BUTTON_BROWSE_DICT"));
        bDict.add(dictBrowse);
        final JTextField dictRootField = new JTextField();
        dirsBox.add(bDict);
        dirsBox.add(dictRootField);

        JLabel locRootLabel = new JLabel();
        Mnemonics.setLocalizedText(locRootLabel, OStrings.getString("PP_LOC_ROOT"));
        Box bLoc = Box.createHorizontalBox();
        bLoc.setBorder(emptyBorder);
        bLoc.add(locRootLabel);
        bLoc.add(Box.createHorizontalGlue());
        JButton locBrowse = new JButton();
        Mnemonics.setLocalizedText(locBrowse, OStrings.getString("PP_BUTTON_BROWSE_TAR"));
        bLoc.add(locBrowse);
        final JTextField locRootField = new JTextField();
        dirsBox.add(bLoc);
        dirsBox.add(locRootField);

        JLabel exportTMRootLabel = new JLabel();
        Mnemonics.setLocalizedText(exportTMRootLabel, OStrings.getString("PP_EXPORT_TM_ROOT"));
        Box bExpTM = Box.createHorizontalBox();
        bExpTM.setBorder(emptyBorder);
        bExpTM.add(exportTMRootLabel);
        bExpTM.add(Box.createHorizontalGlue());
        JButton exportTMBrowse = new JButton();
        Mnemonics.setLocalizedText(exportTMBrowse, OStrings.getString("PP_BUTTON_BROWSE_EXP_TM"));
        bExpTM.add(exportTMBrowse);
        final JTextField exportTMRootField = new JTextField();
        // Supply check boxes to choose which TM formats to export
        final JCheckBox exportTMOmegaTCheckBox = new JCheckBox(OStrings.getString("PP_EXPORT_TM_OMEGAT"));
        final JCheckBox exportTMLevel1CheckBox = new JCheckBox(OStrings.getString("PP_EXPORT_TM_LEVEL1"));
        final JCheckBox exportTMLevel2CheckBox = new JCheckBox(OStrings.getString("PP_EXPORT_TM_LEVEL2"));

        JPanel exportTMPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        JLabel cbExportLabel = new JLabel(OStrings.getString("PP_EXPORT_TM_LEVELS"));

        dirsBox.add(bExpTM);
        dirsBox.add(exportTMRootField);
        exportTMPanel.add(cbExportLabel);
        exportTMPanel.add(exportTMOmegaTCheckBox);
        exportTMPanel.add(exportTMLevel1CheckBox);
        exportTMPanel.add(exportTMLevel2CheckBox);
        dirsBox.add(exportTMPanel);



        centerBox.add(dirsBox);

        JScrollPane scrollPane = new JScrollPane(centerBox);
        // Prevent ugly white viewport background with GTK LAF
        scrollPane.setBackground(getBackground());
        scrollPane.getViewport().setOpaque(false);
        getContentPane().add(scrollPane, "Center");

        JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK"));
        getRootPane().setDefaultButton(okButton);
        JButton cancelButton = new JButton();
        Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL"));

        Box southBox = Box.createHorizontalBox();
        southBox.setBorder(new EmptyBorder(5, 5, 5, 5));
        southBox.add(Box.createHorizontalGlue());
        southBox.add(okButton);
        southBox.add(Box.createHorizontalStrut(5));
        southBox.add(cancelButton);
        getContentPane().add(southBox, "South");

        setResizable(false);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doOK(sourceLocaleField, targetLocaleField, sourceTokenizerField, targetTokenizerField,
                        sentenceSegmentingCheckBox, srcRootField, locRootField, glosRootField,
                        writeableGlosField, tmRootField, exportTMRootField, exportTMOmegaTCheckBox,
                        exportTMLevel1CheckBox, exportTMLevel2CheckBox, dictRootField, allowDefaultsCheckBox,
                        removeTagsCheckBox, externalCommandTextArea);
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        });

        srcBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doBrowseDirectoy(1, srcRootField);
            }
        });
        srcExcludesBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> result = FilenamePatternsEditorController.show(srcExcludes);
                if (result != null) {
                    srcExcludes.clear();
                    srcExcludes.addAll(result);
                }
            }
        });

        locBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doBrowseDirectoy(2, locRootField);
            }
        });

        glosBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Test now, because result may change after doBrowseDirectory().
                boolean isDefaultGlossaryFile = projectProperties.isDefaultWriteableGlossaryFile();
                doBrowseDirectoy(3, glosRootField);
                // If file started as default, automatically use new default.
                if (isDefaultGlossaryFile) {
                    String newDefault = projectProperties.computeDefaultWriteableGlossaryFile();
                    projectProperties.setWriteableGlossary(newDefault);
                    writeableGlosField.setText(newDefault);
                }
            }
        });

        wGlosBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doBrowseDirectoy(6, writeableGlosField);
            }
        });

        tmBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doBrowseDirectoy(4, tmRootField);
            }
        });

        exportTMBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doBrowseDirectoy(7, exportTMRootField);
            }
        });

        dictBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doBrowseDirectoy(5, dictRootField);
            }
        });

        sentenceSegmentingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SegmentationCustomizer segmentationCustomizer = new SegmentationCustomizer(true,
                        SRX.getDefault(), Preferences.getSRX(), srx);
                if (segmentationCustomizer.show(ProjectPropertiesDialog.this)) {
                    srx = segmentationCustomizer.getResult();
                }
            }
        });

        fileFiltersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FiltersCustomizer dlg = new FiltersCustomizer(true,
                        FilterMaster.createDefaultFiltersConfig(), Preferences.getFilters(), filters);
                if (dlg.show(ProjectPropertiesDialog.this)) {
                    // saving config
                    filters = dlg.getResult();
                }
            }
        });

        repositoriesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<RepositoryDefinition> r = new RepositoriesMappingController().show(parent,
                        projectProperties.getRepositories());
                if (r != null) {
                    projectProperties.setRepositories(r);
                }
            }
        });

        externalFinderButton.addActionListener(e -> {
            ExternalFinderCustomizer dlg = new ExternalFinderCustomizer(true, externalFinderConfig);
            if (dlg.show(ProjectPropertiesDialog.this)) {
                externalFinderConfig = dlg.getResult();
            }
        });

        StaticUIUtils.setEscapeAction(this, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        });

        srcRootField.setText(projectProperties.getSourceRoot());
        locRootField.setText(projectProperties.getTargetRoot());
        glosRootField.setText(projectProperties.getGlossaryRoot());
        writeableGlosField.setText(projectProperties.getWriteableGlossary());
        tmRootField.setText(projectProperties.getTMRoot());
        dictRootField.setText(projectProperties.getDictRoot());

        exportTMRootField.setText(projectProperties.getExportTMRoot());
        exportTMOmegaTCheckBox.setSelected(projectProperties.isExportTm("omegat"));
        exportTMLevel1CheckBox.setSelected(projectProperties.isExportTm("level1"));
        exportTMLevel2CheckBox.setSelected(projectProperties.isExportTm("level2"));

        sentenceSegmentingCheckBox.setSelected(projectProperties.isSentenceSegmentingEnabled());
        allowDefaultsCheckBox.setSelected(projectProperties.isSupportDefaultTranslations());
        removeTagsCheckBox.setSelected(projectProperties.isRemoveTags());

        switch (dialogType) {
        case RESOLVE_DIRS:
            // disabling some of the controls
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
            break;
        default:
            break;
        }

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
        setLocationRelativeTo(parent);
    }
    // CHECKSTYLE:ON

    /**
     * Browses for the directory.
     *
     * @param browseTarget
     *            customizes the messages depending on what is browsed for
     * @param field
     *            text field to write browsed folder to
     */
    private void doBrowseDirectoy(int browseTarget, JTextField field) {
        if (field == null) {
            return;
        }
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

        case 7:
            title = OStrings.getString("PP_BROWSE_TITLE_EXPORT_TM");
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
        String curDir = field.getText();
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

            case 7:
                curDir = Preferences.getPreference(Preferences.EXPORT_TM_FOLDER);
                break;

            }
        }

        if (fileMode) {
            File dirFile = new File(curDir);
            curDir = dirFile.getParent();
        }

        if (curDir.equals("")) {
            curDir = Preferences.getPreference(Preferences.CURRENT_FOLDER);
        }

        if (!curDir.equals("")) {
            File dir = new File(curDir);
            if (dir.exists() && dir.isDirectory()) {
                browser.setCurrentDirectory(dir);
            }
        }

        // show the browser
        int action = browser.showOpenDialog(this);

        // check if the selection has been approved
        if (action != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }

        // get the selected folder
        File dir = browser.getSelectedFile();
        if (dir == null) {
            return;
        }

        String str = dir.getAbsolutePath();
        if (!fileMode) {
            str += File.separator; // Add file separator for directories
        }

        // The writeable glossary file must end with .txt or utf8. Not .tab, because it not necessarily is .utf8
        if (glossaryFile && !str.endsWith(OConsts.EXT_TSV_TXT) && !str.endsWith(OConsts.EXT_TSV_UTF8)) {
            str += OConsts.EXT_TSV_TXT; // Defaults to .txt
        }

        // reset appropriate path - store preferred directory
        switch (browseTarget) {
        case 1:
            Preferences.setPreference(Preferences.SOURCE_FOLDER, browser.getSelectedFile().getParent());
            projectProperties.setSourceRoot(str);
            field.setText(projectProperties.getSourceRoot());
            if (new File(projectProperties.getSourceRoot()).exists()
                    && new File(projectProperties.getSourceRoot()).isDirectory()) {
                field.setForeground(java.awt.SystemColor.textText);
            }
            break;

        case 2:
            Preferences.setPreference(Preferences.TARGET_FOLDER, browser.getSelectedFile().getParent());
            projectProperties.setTargetRoot(str);
            field.setText(projectProperties.getTargetRoot());
            if (new File(projectProperties.getTargetRoot()).exists()
                    && new File(projectProperties.getTargetRoot()).isDirectory()) {
                field.setForeground(java.awt.SystemColor.textText);
            }
            break;

        case 3:
            Preferences.setPreference(Preferences.GLOSSARY_FOLDER, browser.getSelectedFile().getParent());
            projectProperties.setGlossaryRoot(str);
            field.setText(projectProperties.getGlossaryRoot());
            if (new File(projectProperties.getGlossaryRoot()).exists()
                    && new File(projectProperties.getGlossaryRoot()).isDirectory()) {
                field.setForeground(java.awt.SystemColor.textText);
            }
            break;

        case 4:
            Preferences.setPreference(Preferences.TM_FOLDER, browser.getSelectedFile().getParent());
            projectProperties.setTMRoot(str);
            field.setText(projectProperties.getTMRoot());
            if (new File(projectProperties.getTMRoot()).exists()
                    && new File(projectProperties.getTMRoot()).isDirectory()) {
                field.setForeground(java.awt.SystemColor.textText);
            }
            break;

        case 5:
            Preferences.setPreference(Preferences.DICT_FOLDER, browser.getSelectedFile().getParent());
            projectProperties.setDictRoot(str);
            field.setText(projectProperties.getDictRoot());
            if (new File(projectProperties.getDictRoot()).exists()
                    && new File(projectProperties.getDictRoot()).isDirectory()) {
                field.setForeground(java.awt.SystemColor.textText);
            }
            break;

        case 6:
            Preferences.setPreference(Preferences.GLOSSARY_FILE, browser.getSelectedFile().getPath());
            projectProperties.setWriteableGlossary(str);
            field.setText(projectProperties.getWriteableGlossary());
            // The writable glosssary file must be inside the glossary dir
            if (new File(projectProperties.getWriteableGlossaryDir()).exists()
                    && new File(projectProperties.getWriteableGlossaryDir()).isDirectory()
                    && projectProperties.getWriteableGlossaryDir().contains(projectProperties.getGlossaryRoot())) {
                field.setForeground(java.awt.SystemColor.textText);
            }
            break;

        case 7:
            Preferences.setPreference(Preferences.EXPORT_TM_FOLDER, browser.getSelectedFile().getParent());
            projectProperties.setExportTMRoot(str);
            field.setText(projectProperties.getExportTMRoot());
            if (new File(projectProperties.getExportTMRoot()).exists()
                    && new File(projectProperties.getExportTMRoot()).isDirectory()) {
                field.setForeground(java.awt.SystemColor.textText);
            }
            break;
        }
    }

    private void doOK(JComboBox<Language> sourceLocaleField, JComboBox<Language> targetLocaleField,
            JComboBox<Class<?>> sourceTokenizerField, JComboBox<Class<?>> targetTokenizerField,
            JCheckBox sentenceSegmentingCheckBox, JTextField srcRootField, JTextField locRootField,
            JTextField glosRootField, JTextField writeableGlosField, JTextField tmRootField,
            JTextField exportTMRootField, JCheckBox exportTMOmegaTCheckBox, JCheckBox exportTMLevel1CheckBox,
            JCheckBox exportTMLevel2CheckBox, JTextField dictRootField, JCheckBox allowDefaultsCheckBox,
            JCheckBox removeTagsCheckBox, JTextArea customCommandTextArea) {
        if (!Language.verifySingleLangCode(sourceLocaleField.getSelectedItem().toString())) {
            JOptionPane.showMessageDialog(
                    this,
                    OStrings.getString("NP_INVALID_SOURCE_LOCALE")
                            + OStrings.getString("NP_LOCALE_SUGGESTION"), OStrings.getString("TF_ERROR"),
                    JOptionPane.ERROR_MESSAGE);
            sourceLocaleField.requestFocusInWindow();
            return;
        }
        projectProperties.setSourceLanguage(sourceLocaleField.getSelectedItem().toString());

        if (!Language.verifySingleLangCode(targetLocaleField.getSelectedItem().toString())) {
            JOptionPane.showMessageDialog(
                    this,
                    OStrings.getString("NP_INVALID_TARGET_LOCALE")
                            + OStrings.getString("NP_LOCALE_SUGGESTION"), OStrings.getString("TF_ERROR"),
                    JOptionPane.ERROR_MESSAGE);
            targetLocaleField.requestFocusInWindow();
            return;
        }
        projectProperties.setTargetLanguage(targetLocaleField.getSelectedItem().toString());

        if (sourceTokenizerField.isEnabled()) {
            projectProperties.setSourceTokenizer((Class<?>) sourceTokenizerField.getSelectedItem());
        }

        if (targetTokenizerField.isEnabled()) {
            projectProperties.setTargetTokenizer((Class<?>) targetTokenizerField.getSelectedItem());
        }

        projectProperties.setSentenceSegmentingEnabled(sentenceSegmentingCheckBox.isSelected());

        projectProperties.setSupportDefaultTranslations(allowDefaultsCheckBox.isSelected());

        projectProperties.setRemoveTags(removeTagsCheckBox.isSelected());

        projectProperties.setExportTmLevels(exportTMOmegaTCheckBox.isSelected(),
                                            exportTMLevel1CheckBox.isSelected(),
                                            exportTMLevel2CheckBox.isSelected());

        projectProperties.setExternalCommand(customCommandTextArea.getText());

        projectProperties.setSourceRoot(srcRootField.getText());
        if (!projectProperties.getSourceRoot().endsWith(File.separator)) {
            projectProperties.setSourceRoot(projectProperties.getSourceRoot() + File.separator);
        }

        if (dialogType != Mode.NEW_PROJECT && !new File(projectProperties.getSourceRoot()).exists()) {
            JOptionPane.showMessageDialog(this, OStrings.getString("NP_SOURCEDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            srcRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setTargetRoot(locRootField.getText());
        if (!projectProperties.getTargetRoot().endsWith(File.separator)) {
            projectProperties.setTargetRoot(projectProperties.getTargetRoot() + File.separator);
        }
        if (dialogType != Mode.NEW_PROJECT && !new File(projectProperties.getTargetRoot()).exists()) {
            JOptionPane.showMessageDialog(this, OStrings.getString("NP_TRANSDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            locRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setGlossaryRoot(glosRootField.getText());
        if (!projectProperties.getGlossaryRoot().endsWith(File.separator)) {
            projectProperties.setGlossaryRoot(projectProperties.getGlossaryRoot() + File.separator);
        }
        if (dialogType != Mode.NEW_PROJECT && !new File(projectProperties.getGlossaryRoot()).exists()) {
            JOptionPane.showMessageDialog(this, OStrings.getString("NP_GLOSSDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            glosRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setWriteableGlossary(writeableGlosField.getText());
        if (dialogType != Mode.NEW_PROJECT && !new File(projectProperties.getWriteableGlossaryDir()).exists()) {
            JOptionPane.showMessageDialog(this, OStrings.getString("NP_W_GLOSSDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            writeableGlosField.requestFocusInWindow();
            return;
        }

        String glossaryDir = projectProperties.getWriteableGlossaryDir();
        if (!glossaryDir.endsWith(File.separator)) {
            glossaryDir += File.separator;
        }
        if (!glossaryDir.contains(projectProperties.getGlossaryRoot())) {
            JOptionPane.showMessageDialog(this, OStrings.getString("NP_W_GLOSDIR_NOT_INSIDE_GLOS"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            writeableGlosField.requestFocusInWindow();
            return;
        }

        projectProperties.setTMRoot(tmRootField.getText());
        if (!projectProperties.getTMRoot().endsWith(File.separator)) {
            projectProperties.setTMRoot(projectProperties.getTMRoot() + File.separator);
        }
        if (dialogType != Mode.NEW_PROJECT && !new File(projectProperties.getTMRoot()).exists()) {
            JOptionPane.showMessageDialog(this, OStrings.getString("NP_TMDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            tmRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setExportTMRoot(exportTMRootField.getText());
        if (!projectProperties.getExportTMRoot().endsWith(File.separator)) {
            projectProperties.setExportTMRoot(projectProperties.getExportTMRoot() + File.separator);
        }
        if (dialogType != Mode.NEW_PROJECT && !new File(projectProperties.getExportTMRoot()).exists()) {
            JOptionPane.showMessageDialog(this, OStrings.getString("NP_EXPORT_TMDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            exportTMRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setDictRoot(dictRootField.getText());
        if (!projectProperties.getDictRoot().endsWith(File.separator)) {
            projectProperties.setDictRoot(projectProperties.getDictRoot() + File.separator);
        }
        if (dialogType != Mode.NEW_PROJECT && !new File(projectProperties.getDictRoot()).exists()) {
            JOptionPane.showMessageDialog(this, OStrings.getString("NP_DICTDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            dictRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setExportTmLevels(exportTMOmegaTCheckBox.isSelected(), exportTMLevel1CheckBox.isSelected(),
                                       exportTMLevel2CheckBox.isSelected());

        projectProperties.setProjectSRX(srx);
        projectProperties.setProjectFilters(filters);
        projectProperties.getSourceRootExcludes().clear();
        projectProperties.getSourceRootExcludes().addAll(srcExcludes);

        ExternalFinder.setProjectConfig(externalFinderConfig);

        dialogCancelled = false;
        setVisible(false);
    }

    private void doCancel() {
        // delete project dir in case of a new project
        // to fix bug 1476591 the project root is created before everything else
        // and if the new project is cancelled, the project root still exists,
        // so it must be deleted
        if (dialogType == Mode.NEW_PROJECT) {
            new File(projectProperties.getProjectRoot()).delete();
        }
        dialogCancelled = true;
        setVisible(false);
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
     * Whether the user cancelled the dialog.
     */
    private boolean dialogCancelled;

    /**
     * Return new properties or null if dialog cancelled.
     */
    public ProjectProperties getResult() {
        return dialogCancelled ? null : projectProperties;
    }
}
