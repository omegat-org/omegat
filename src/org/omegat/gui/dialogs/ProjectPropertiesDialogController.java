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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

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
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.OmegaTFileChooser;
import org.omegat.util.gui.StaticUIUtils;

import gen.core.filters.Filters;
import gen.core.project.RepositoryDefinition;

public class ProjectPropertiesDialogController {

    /**
     * The type of the dialog:
     * <ul>
     * <li>Creating project ==
     * {@link ProjectPropertiesDialogController.Mode#NEW_PROJECT}
     * <li>Resolving the project's directories (existing project with some dirs
     * missing) == {@link ProjectPropertiesDialogController.Mode#RESOLVE_DIRS}
     * <li>Editing project properties ==
     * {@link ProjectPropertiesDialogController.Mode#EDIT_PROJECT}
     * </ul>
     */
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

    private final Mode dialogType;

    private final ProjectProperties projectProperties;

    /** Project SRX. */
    private SRX srx;

    /** Project filters. */
    private Filters filters;

    /** Project ExternalFinder config */
    private ExternalFinderConfiguration externalFinderConfig;

    private final List<String> srcExcludes = new ArrayList<>();

    /**
     * Whether the user canceled the dialog.
     */
    private boolean dialogCancelled;

    private final Frame parent;
    private final ProjectPropertiesDialog dialog;

    public ProjectPropertiesDialogController(Frame parent, ProjectPropertiesDialog dialog, Mode type,
            ProjectProperties projectProperties) {
        this.parent = parent;
        this.dialog = dialog;
        this.dialogType = type;
        this.projectProperties = projectProperties;
        this.srx = projectProperties.getProjectSRX();
        this.filters = projectProperties.getProjectFilters();
        srcExcludes.addAll(projectProperties.getSourceRootExcludes());
        externalFinderConfig = ExternalFinder.getProjectConfig();
        initFromProperties();
        initializeActions();
    }

    /**
     * Return new properties or null if dialog cancelled.
     */
    public ProjectProperties getResult() {
        return dialogCancelled ? null : projectProperties;
    }

    private void initFromProperties() {
        dialog.srcRootField.setText(projectProperties.getSourceRoot());
        dialog.locRootField.setText(projectProperties.getTargetRoot());
        dialog.glosRootField.setText(projectProperties.getGlossaryRoot());
        dialog.writeableGlosField.setText(projectProperties.getWriteableGlossary());
        dialog.tmRootField.setText(projectProperties.getTMRoot());
        dialog.dictRootField.setText(projectProperties.getDictRoot());

        dialog.exportTMRootField.setText(projectProperties.getExportTMRoot());
        dialog.exportTMOmegaTCheckBox.setSelected(projectProperties.isExportTm("omegat"));
        dialog.exportTMLevel1CheckBox.setSelected(projectProperties.isExportTm("level1"));
        dialog.exportTMLevel2CheckBox.setSelected(projectProperties.isExportTm("level2"));

        dialog.sentenceSegmentingCheckBox.setSelected(projectProperties.isSentenceSegmentingEnabled());
        dialog.allowDefaultsCheckBox.setSelected(projectProperties.isSupportDefaultTranslations());
        dialog.removeTagsCheckBox.setSelected(projectProperties.isRemoveTags());

        dialog.sourceLocaleField.setSelectedItem(projectProperties.getSourceLanguage());
        dialog.targetLocaleField.setSelectedItem(projectProperties.getTargetLanguage());

        dialog.sourceTokenizerField.setSelectedItem(projectProperties.getSourceTokenizer());
        dialog.targetTokenizerField.setSelectedItem(projectProperties.getTargetTokenizer());

        dialog.externalCommandTextArea.setText(projectProperties.getExternalCommand());
    }

    private void initializeActions() {
        dialog.okButton.addActionListener(e -> doOK());
        StaticUIUtils.setEscapeAction(dialog, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        });

        // Configure locale actions and initialize
        ActionListener sourceLocaleListener = e -> {
            if (!dialog.sourceLocaleField.isEnabled()) {
                return;
            }
            Object newLang = dialog.sourceLocaleField.getSelectedItem();
            if (newLang instanceof String) {
                newLang = new Language((String) newLang);
            }
            Class<?> newTok = PluginUtils.getTokenizerClassForLanguage((Language) newLang);
            dialog.sourceTokenizerField.setSelectedItem(newTok);
        };
        dialog.sourceLocaleField.addActionListener(sourceLocaleListener);
        ActionListener targetLocaleListener = e -> {
            if (!dialog.targetLocaleField.isEnabled()) {
                return;
            }
            Object newLang = dialog.targetLocaleField.getSelectedItem();
            if (newLang instanceof String) {
                newLang = new Language((String) newLang);
            }
            Class<?> newTok = PluginUtils.getTokenizerClassForLanguage((Language) newLang);
            dialog.targetTokenizerField.setSelectedItem(newTok);
        };
        dialog.targetLocaleField.addActionListener(targetLocaleListener);
        if (dialogType == ProjectPropertiesDialogController.Mode.NEW_PROJECT) {
            // Infer appropriate tokenizers from source languages
            sourceLocaleListener.actionPerformed(null);
            targetLocaleListener.actionPerformed(null);
        }

        dialog.repositoriesButton.addActionListener(e -> {
            List<RepositoryDefinition> r = new RepositoriesMappingController().show(parent,
                    projectProperties.getRepositories());
            if (r != null) {
                projectProperties.setRepositories(r);
            }
        });
        dialog.externalFinderButton.addActionListener(e -> {
            ExternalFinderCustomizer dlg = new ExternalFinderCustomizer(true, externalFinderConfig);
            if (dlg.show(dialog)) {
                externalFinderConfig = dlg.getResult();
            }
        });
        dialog.cancelButton.addActionListener(e -> doCancel());
        dialog.srcBrowse.addActionListener(e -> doBrowseDirectoy(1, dialog.srcRootField));
        dialog.srcExcludesBtn.addActionListener(e -> {
            List<String> result = FilenamePatternsEditorController.show(srcExcludes);
            if (result != null) {
                srcExcludes.clear();
                srcExcludes.addAll(result);
            }
        });
        dialog.locBrowse.addActionListener(e -> doBrowseDirectoy(2, dialog.locRootField));
        dialog.glosBrowse.addActionListener(e -> {
            // Test now, because a result may change after doBrowseDirectory().
            boolean isDefaultGlossaryFile = projectProperties.isDefaultWriteableGlossaryFile();
            doBrowseDirectoy(3, dialog.glosRootField);
            // If a file started as default, automatically use new default.
            if (isDefaultGlossaryFile) {
                String newDefault = projectProperties.computeDefaultWriteableGlossaryFile();
                projectProperties.setWriteableGlossary(newDefault);
                dialog.writeableGlosField.setText(newDefault);
            }
        });
        dialog.wGlosBrowse.addActionListener(e -> doBrowseDirectoy(6, dialog.writeableGlosField));
        dialog.tmBrowse.addActionListener(e -> doBrowseDirectoy(4, dialog.tmRootField));
        dialog.exportTMBrowse.addActionListener(e -> doBrowseDirectoy(7, dialog.exportTMRootField));
        dialog.dictBrowse.addActionListener(e -> doBrowseDirectoy(5, dialog.dictRootField));
        dialog.sentenceSegmentingButton.addActionListener(e -> {
            SegmentationCustomizer segmentationCustomizer = new SegmentationCustomizer(true, SRX.getDefault(),
                    Preferences.getSRX(), srx);
            if (segmentationCustomizer.show(dialog)) {
                srx = segmentationCustomizer.getResult();
            }
        });
        dialog.fileFiltersButton.addActionListener(e -> {
            FiltersCustomizer dlg = new FiltersCustomizer(true, FilterMaster.createDefaultFiltersConfig(),
                    Preferences.getFilters(), filters);
            if (dlg.show(dialog)) {
                filters = dlg.getResult(); // saving config
            }
        });
    }

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
        boolean fileMode = false;
        boolean glossaryFile = false;

        if (browseTarget == 6) {
            fileMode = true;
            glossaryFile = true;
        }
        String title = getBrowserTitle(browseTarget);
        if (title == null) {
            return;
        }

        OmegaTFileChooser browser = new OmegaTFileChooser();
        browser.setDialogTitle(title);
        if (fileMode) {
            browser.setFileSelectionMode(OmegaTFileChooser.FILES_ONLY);
        } else {
            browser.setFileSelectionMode(OmegaTFileChooser.DIRECTORIES_ONLY);
        }

        // check if the current directory as specified by the field exists
        setCurDir(browser, field, browseTarget, fileMode);

        // show the browser
        int action = browser.showOpenDialog(dialog);

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

        // The writeable glossary file must end with .txt or utf8. Not .tab,
        // because it not necessarily is .utf8
        if (glossaryFile && !str.endsWith(OConsts.EXT_TSV_TXT) && !str.endsWith(OConsts.EXT_TSV_UTF8)) {
            str += OConsts.EXT_TSV_TXT; // Defaults to .txt
        }

        resetThePathAndWarn(browser, field, browseTarget, str);
    }

    private String getBrowserTitle(int browseTarget) {
        switch (browseTarget) {
        case 1:
            return OStrings.getString("PP_BROWSE_TITLE_SOURCE");
        case 2:
            return OStrings.getString("PP_BROWSE_TITLE_TARGET");
        case 3:
            return OStrings.getString("PP_BROWSE_TITLE_GLOS");
        case 4:
            return OStrings.getString("PP_BROWSE_TITLE_TM");
        case 5:
            return OStrings.getString("PP_BROWSE_TITLE_DICT");
        case 6:
            return OStrings.getString("PP_BROWSE_W_GLOS");
        case 7:
            return OStrings.getString("PP_BROWSE_TITLE_EXPORT_TM");
        default:
            return null;
        }
    }

    private void setCurDir(OmegaTFileChooser browser, JTextField field, int browseTarget, boolean fileMode) {
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
            curDir = getDirFromPreference(browseTarget);
        }

        if (fileMode) {
            File dirFile = new File(curDir);
            curDir = dirFile.getParent();
        }

        if (curDir.isEmpty()) {
            curDir = Preferences.getPreference(Preferences.CURRENT_FOLDER);
        }

        if (!curDir.isEmpty()) {
            File dir = new File(curDir);
            if (dir.exists() && dir.isDirectory()) {
                browser.setCurrentDirectory(dir);
            }
        }
    }

    private String getDirFromPreference(int browseTarget) {
        switch (browseTarget) {
        case 1:
            return Preferences.getPreference(Preferences.SOURCE_FOLDER);
        case 2:
            return Preferences.getPreference(Preferences.TARGET_FOLDER);
        case 3:
            return Preferences.getPreference(Preferences.GLOSSARY_FOLDER);
        case 4:
            return Preferences.getPreference(Preferences.TM_FOLDER);
        case 5:
            return Preferences.getPreference(Preferences.DICT_FOLDER);
        case 6:
            return Preferences.getPreference(Preferences.GLOSSARY_FILE);
        case 7:
            return Preferences.getPreference(Preferences.EXPORT_TM_FOLDER);
        default:
            return null;
        }
    }

    private void resetThePathAndWarn(OmegaTFileChooser browser, JTextField field, int browseTarget,
            String str) {
        // reset the appropriate path - store preferred directory
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
                    && projectProperties.getWriteableGlossaryDir()
                            .contains(projectProperties.getGlossaryRoot())) {
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

    private void doOK() {
        if (!Language.verifySingleLangCode(dialog.sourceLocaleField.getSelectedItem().toString())) {
            JOptionPane.showMessageDialog(dialog,
                    OStrings.getString("NP_INVALID_SOURCE_LOCALE")
                            + OStrings.getString("NP_LOCALE_SUGGESTION"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            dialog.sourceLocaleField.requestFocusInWindow();
            return;
        }
        projectProperties.setSourceLanguage(dialog.sourceLocaleField.getSelectedItem().toString());
        if (!Language.verifySingleLangCode(dialog.targetLocaleField.getSelectedItem().toString())) {
            JOptionPane.showMessageDialog(dialog,
                    OStrings.getString("NP_INVALID_TARGET_LOCALE")
                            + OStrings.getString("NP_LOCALE_SUGGESTION"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            dialog.targetLocaleField.requestFocusInWindow();
            return;
        }
        projectProperties.setTargetLanguage(dialog.targetLocaleField.getSelectedItem().toString());
        if (dialog.sourceTokenizerField.isEnabled()) {
            projectProperties.setSourceTokenizer((Class<?>) dialog.sourceTokenizerField.getSelectedItem());
        }
        if (dialog.targetTokenizerField.isEnabled()) {
            projectProperties.setTargetTokenizer((Class<?>) dialog.targetTokenizerField.getSelectedItem());
        }

        projectProperties.setSentenceSegmentingEnabled(dialog.sentenceSegmentingCheckBox.isSelected());
        projectProperties.setSupportDefaultTranslations(dialog.allowDefaultsCheckBox.isSelected());
        projectProperties.setRemoveTags(dialog.removeTagsCheckBox.isSelected());
        projectProperties.setExportTmLevels(dialog.exportTMOmegaTCheckBox.isSelected(),
                dialog.exportTMLevel1CheckBox.isSelected(), dialog.exportTMLevel2CheckBox.isSelected());
        projectProperties.setExternalCommand(dialog.externalCommandTextArea.getText());
        projectProperties.setSourceRoot(dialog.srcRootField.getText());
        if (!projectProperties.getSourceRoot().endsWith(File.separator)) {
            projectProperties.setSourceRoot(projectProperties.getSourceRoot() + File.separator);
        }

        if (dialogType != ProjectPropertiesDialogController.Mode.NEW_PROJECT
                && !new File(projectProperties.getSourceRoot()).exists()) {
            JOptionPane.showMessageDialog(dialog, OStrings.getString("NP_SOURCEDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            dialog.srcRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setTargetRoot(dialog.locRootField.getText());
        if (!projectProperties.getTargetRoot().endsWith(File.separator)) {
            projectProperties.setTargetRoot(projectProperties.getTargetRoot() + File.separator);
        }
        if (dialogType != ProjectPropertiesDialogController.Mode.NEW_PROJECT
                && !new File(projectProperties.getTargetRoot()).exists()) {
            JOptionPane.showMessageDialog(dialog, OStrings.getString("NP_TRANSDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            dialog.locRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setGlossaryRoot(dialog.glosRootField.getText());
        if (!projectProperties.getGlossaryRoot().endsWith(File.separator)) {
            projectProperties.setGlossaryRoot(projectProperties.getGlossaryRoot() + File.separator);
        }
        if (dialogType != ProjectPropertiesDialogController.Mode.NEW_PROJECT
                && !new File(projectProperties.getGlossaryRoot()).exists()) {
            JOptionPane.showMessageDialog(dialog, OStrings.getString("NP_GLOSSDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            dialog.glosRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setWriteableGlossary(dialog.writeableGlosField.getText());
        if (dialogType != ProjectPropertiesDialogController.Mode.NEW_PROJECT
                && !new File(projectProperties.getWriteableGlossaryDir()).exists()) {
            JOptionPane.showMessageDialog(dialog, OStrings.getString("NP_W_GLOSSDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            dialog.writeableGlosField.requestFocusInWindow();
            return;
        }

        String glossaryDir = projectProperties.getWriteableGlossaryDir();
        if (!glossaryDir.endsWith(File.separator)) {
            glossaryDir += File.separator;
        }
        if (!glossaryDir.contains(projectProperties.getGlossaryRoot())) {
            JOptionPane.showMessageDialog(dialog, OStrings.getString("NP_W_GLOSDIR_NOT_INSIDE_GLOS"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            dialog.writeableGlosField.requestFocusInWindow();
            return;
        }

        projectProperties.setTMRoot(dialog.tmRootField.getText());
        if (!projectProperties.getTMRoot().endsWith(File.separator)) {
            projectProperties.setTMRoot(projectProperties.getTMRoot() + File.separator);
        }
        if (dialogType != ProjectPropertiesDialogController.Mode.NEW_PROJECT
                && !new File(projectProperties.getTMRoot()).exists()) {
            JOptionPane.showMessageDialog(dialog, OStrings.getString("NP_TMDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            dialog.tmRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setExportTMRoot(dialog.exportTMRootField.getText());
        if (!projectProperties.getExportTMRoot().endsWith(File.separator)) {
            projectProperties.setExportTMRoot(projectProperties.getExportTMRoot() + File.separator);
        }
        if (dialogType != ProjectPropertiesDialogController.Mode.NEW_PROJECT
                && !new File(projectProperties.getExportTMRoot()).exists()) {
            JOptionPane.showMessageDialog(dialog, OStrings.getString("NP_EXPORT_TMDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            dialog.exportTMRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setDictRoot(dialog.dictRootField.getText());
        if (!projectProperties.getDictRoot().endsWith(File.separator)) {
            projectProperties.setDictRoot(projectProperties.getDictRoot() + File.separator);
        }
        if (dialogType != ProjectPropertiesDialogController.Mode.NEW_PROJECT
                && !new File(projectProperties.getDictRoot()).exists()) {
            JOptionPane.showMessageDialog(dialog, OStrings.getString("NP_DICTDIR_DOESNT_EXIST"),
                    OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            dialog.dictRootField.requestFocusInWindow();
            return;
        }

        projectProperties.setExportTmLevels(dialog.exportTMOmegaTCheckBox.isSelected(),
                dialog.exportTMLevel1CheckBox.isSelected(), dialog.exportTMLevel2CheckBox.isSelected());

        projectProperties.setProjectSRX(srx);
        projectProperties.setProjectFilters(filters);
        projectProperties.getSourceRootExcludes().clear();
        projectProperties.getSourceRootExcludes().addAll(srcExcludes);

        ExternalFinder.setProjectConfig(externalFinderConfig);

        dialogCancelled = false;
        dialog.setVisible(false);
    }

    private void doCancel() {
        // delete project dir in case of a new project
        // to fix bug 1476591 the project root is created before everything else
        // and if the new project is cancelled, the project root still exists,
        // so it must be deleted
        if (dialogType == ProjectPropertiesDialogController.Mode.NEW_PROJECT) {
            new File(projectProperties.getProjectRoot()).delete();
        }
        dialogCancelled = true;
        dialog.setVisible(false);
    }

    public static ProjectProperties showDialog(Frame parent, ProjectProperties projectProperties,
            String projFileName, ProjectPropertiesDialogController.Mode dialogTypeValue) {
        ProjectPropertiesDialog dialog = new ProjectPropertiesDialog(parent, projectProperties, projFileName,
                dialogTypeValue);
        dialog.setVisible(true);
        dialog.dispose();
        return dialog.getResult();
    }
}
