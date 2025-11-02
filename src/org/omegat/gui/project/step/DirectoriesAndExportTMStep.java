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
package org.omegat.gui.project.step;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.data.ProjectProperties;
import org.omegat.gui.project.ProjectConfigMode;
import org.omegat.util.OStrings;
import org.openide.awt.Mnemonics;

/**
 * Step to configure directory paths and Export TM.
 */
public class DirectoriesAndExportTMStep implements ProjectWizardStep {
    private final ProjectConfigMode mode;
    private final JPanel panel = new JPanel();
    // Directories section
    private final JTextField srcRootField = new JTextField();
    private final JButton srcExcludesBtn = new JButton();
    private final JButton srcBrowse = new JButton();
    private final JButton tmBrowse = new JButton();
    private final JButton glosBrowse = new JButton();
    private final JButton wGlosBrowse = new JButton();
    private final JTextField dictRootField = new JTextField();
    private final JButton dictBrowse = new JButton();
    private final JTextField locRootField = new JTextField();
    private final JButton locBrowse = new JButton();
    private final JTextField writeableGlosField = new JTextField();
    private final JTextField tmRootField = new JTextField();
    private final JTextField glosRootField = new JTextField();
    // Export TM section
    private final JTextField exportTmRoot = new JTextField();
    private final JCheckBox exportOmegaT = new JCheckBox();
    private final JCheckBox exportL1 = new JCheckBox();
    private final JCheckBox exportL2 = new JCheckBox();

    public DirectoriesAndExportTMStep(ProjectConfigMode mode) {
        this.mode = mode;
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(createDirsBox());
        panel.add(Box.createVerticalStrut(12));
        panel.add(createExportBox());
        panel.add(Box.createVerticalGlue());
    }

    private Box createDirsBox() {
        Border emptyBorder = new EmptyBorder(2, 0, 2, 0);
        Box dirsBox = Box.createVerticalBox();
        JLabel srcRootLabel = new JLabel();
        Mnemonics.setLocalizedText(srcRootLabel, OStrings.getString("PP_SRC_ROOT"));
        Box bSrcRootLabel = Box.createHorizontalBox();
        bSrcRootLabel.add(srcRootLabel);
        bSrcRootLabel.add(Box.createHorizontalGlue());
        Box bSrc = Box.createHorizontalBox();
        bSrc.setBorder(emptyBorder);
        bSrc.add(srcRootField);
        Mnemonics.setLocalizedText(srcExcludesBtn, OStrings.getString("PP_BUTTON_BROWSE_SRC_EXCLUDES"));
        srcExcludesBtn.setName(SRC_EXCLUDES_BUTTON_NAME);
        bSrc.add(Box.createRigidArea(new Dimension(5, 0)));
        bSrc.add(srcExcludesBtn);
        bSrc.add(Box.createRigidArea(new Dimension(5, 0)));
        bSrc.add(srcBrowse);
        Mnemonics.setLocalizedText(srcBrowse, OStrings.getString("PP_BUTTON_BROWSE_SRC"));
        dirsBox.add(Box.createRigidArea(new Dimension(0, 5)));
        dirsBox.add(bSrcRootLabel);
        dirsBox.add(bSrc);

        JLabel tmRootLabel = new JLabel();
        Mnemonics.setLocalizedText(tmRootLabel, OStrings.getString("PP_TM_ROOT"));
        Box bTmRootLabel = Box.createHorizontalBox();
        bTmRootLabel.add(tmRootLabel);
        bTmRootLabel.add(Box.createHorizontalGlue());
        Box bTM = Box.createHorizontalBox();
        bTM.setBorder(emptyBorder);
        bTM.add(tmRootField);
        Mnemonics.setLocalizedText(tmBrowse, OStrings.getString("PP_BUTTON_BROWSE_TM"));
        tmBrowse.setName(TM_BROWSE_BUTTON_NAME);
        bTM.add(Box.createRigidArea(new Dimension(5, 0)));
        bTM.add(tmBrowse);
        dirsBox.add(Box.createRigidArea(new Dimension(0, 5)));
        dirsBox.add(bTmRootLabel);
        dirsBox.add(bTM);

        JLabel glosRootLabel = new JLabel();
        Mnemonics.setLocalizedText(glosRootLabel, OStrings.getString("PP_GLOS_ROOT"));
        Box bGlosRootLabel = Box.createHorizontalBox();
        bGlosRootLabel.add(glosRootLabel);
        bGlosRootLabel.add(Box.createHorizontalGlue());
        Box bGlos = Box.createHorizontalBox();
        bGlos.setBorder(emptyBorder);
        bGlos.add(glosRootField);
        Mnemonics.setLocalizedText(glosBrowse, OStrings.getString("PP_BUTTON_BROWSE_GL"));
        glosBrowse.setName(GLOSSARY_BROWSE_BUTTON_NAME);
        bGlos.add(Box.createRigidArea(new Dimension(5, 0)));
        bGlos.add(glosBrowse);
        dirsBox.add(Box.createRigidArea(new Dimension(0, 5)));
        dirsBox.add(bGlosRootLabel);
        dirsBox.add(bGlos);

        JLabel writeableGlosLabel = new JLabel();
        Mnemonics.setLocalizedText(writeableGlosLabel, OStrings.getString("PP_WRITEABLE_GLOS"));
        Box bWriteableGlosLabel = Box.createHorizontalBox();
        bWriteableGlosLabel.add(writeableGlosLabel);
        bWriteableGlosLabel.add(Box.createHorizontalGlue());
        Box bwGlos = Box.createHorizontalBox();
        bwGlos.setBorder(emptyBorder);
        bwGlos.add(writeableGlosField);
        Mnemonics.setLocalizedText(wGlosBrowse, OStrings.getString("PP_BUTTON_BROWSE_WG"));
        wGlosBrowse.setName(WRITABLE_GLOSSARY_BROWSE_BUTTON_NAME);
        bwGlos.add(Box.createRigidArea(new Dimension(5, 0)));
        bwGlos.add(wGlosBrowse);
        dirsBox.add(Box.createRigidArea(new Dimension(0, 5)));
        dirsBox.add(bWriteableGlosLabel);
        dirsBox.add(bwGlos);

        JLabel locDictLabel = new JLabel();
        Mnemonics.setLocalizedText(locDictLabel, OStrings.getString("PP_DICT_ROOT"));
        Box bLocDictLabel = Box.createHorizontalBox();
        bLocDictLabel.add(locDictLabel);
        bLocDictLabel.add(Box.createHorizontalGlue());
        Box bDict = Box.createHorizontalBox();
        bDict.setBorder(emptyBorder);
        bDict.add(dictRootField);
        Mnemonics.setLocalizedText(dictBrowse, OStrings.getString("PP_BUTTON_BROWSE_DICT"));
        dictBrowse.setName(DICTIONARY_BROWSE_BUTTON_NAME);
        bDict.add(Box.createRigidArea(new Dimension(5, 0)));
        bDict.add(dictBrowse);
        dirsBox.add(Box.createRigidArea(new Dimension(0, 5)));
        dirsBox.add(bLocDictLabel);
        dirsBox.add(bDict);

        JLabel locRootLabel = new JLabel();
        Mnemonics.setLocalizedText(locRootLabel, OStrings.getString("PP_LOC_ROOT"));
        Box bLocRootLabel = Box.createHorizontalBox();
        bLocRootLabel.add(locRootLabel);
        bLocRootLabel.add(Box.createHorizontalGlue());
        Box bLoc = Box.createHorizontalBox();
        bLoc.setBorder(emptyBorder);
        bLoc.add(locRootField);
        Mnemonics.setLocalizedText(locBrowse, OStrings.getString("PP_BUTTON_BROWSE_TAR"));
        locBrowse.setName(LOC_BROWSE_BUTTON_NAME);
        bLoc.add(Box.createRigidArea(new Dimension(5, 0)));
        bLoc.add(locBrowse);
        dirsBox.add(Box.createRigidArea(new Dimension(0, 5)));
        dirsBox.add(bLocRootLabel);
        dirsBox.add(bLoc);
        dirsBox.add(Box.createRigidArea(new Dimension(0, 5)));
        dirsBox.add(Box.createVerticalGlue());
        return dirsBox;
    }

    private Box createExportBox() {
        Border emptyBorder = new EmptyBorder(2, 0, 2, 0);
        Box container = Box.createVerticalBox();

        // Export TM root
        Box bExportTmRoot = Box.createHorizontalBox();
        bExportTmRoot.setBorder(emptyBorder);
        JPanel exportRootRow = new JPanel();
        exportRootRow.add(new JLabel(OStrings.getString("PP_EXPORT_TM_ROOT")));
        exportRootRow.add(Box.createHorizontalStrut(8));
        exportRootRow.add(exportTmRoot);
        bExportTmRoot.add(exportRootRow);
        container.add(bExportTmRoot);
        container.add(Box.createVerticalStrut(8));

        // Export TM levels
        Box bExportTmLevels = Box.createHorizontalBox();
        bExportTmLevels.setBorder(emptyBorder);
        JPanel exLevels = new JPanel();
        JLabel exLevelsLabel = new JLabel();
        Mnemonics.setLocalizedText(exLevelsLabel, OStrings.getString("PP_EXPORT_TM_LEVELS"));
        exLevels.add(exLevelsLabel);
        exLevels.add(Box.createHorizontalStrut(8));
        Mnemonics.setLocalizedText(exportOmegaT, OStrings.getString("PP_EXPORT_TM_OMEGAT"));
        Mnemonics.setLocalizedText(exportL1, OStrings.getString("PP_EXPORT_TM_LEVEL1"));
        Mnemonics.setLocalizedText(exportL2, OStrings.getString("PP_EXPORT_TM_LEVEL2"));
        exLevels.add(exportOmegaT);
        exLevels.add(Box.createHorizontalStrut(8));
        exLevels.add(exportL1);
        exLevels.add(Box.createHorizontalStrut(8));
        exLevels.add(exportL2);
        bExportTmLevels.add(exLevels);
        bExportTmLevels.add(Box.createVerticalGlue());
        container.add(bExportTmLevels);
        return container;
    }

    @Override
    public String getTitle() {
        return OStrings.getString("PP_DIRECTORIES");
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void onLoad(ProjectProperties p) {
        // Directories
        srcRootField.setText(p.getSourceRoot());
        locRootField.setText(p.getTargetRoot());
        glosRootField.setText(p.getGlossaryRoot());
        writeableGlosField.setText(p.getWriteableGlossary());
        tmRootField.setText(p.getTMRoot());
        dictRootField.setText(p.getDictRoot());
        // Export TM
        exportTmRoot.setText(p.getExportTMRoot());
        List<String> lvls = p.getExportTmLevels();
        exportOmegaT.setSelected(lvls.contains("omegat"));
        exportL1.setSelected(lvls.contains("level1"));
        exportL2.setSelected(lvls.contains("level2"));
        if (mode == ProjectConfigMode.RESOLVE_DIRS) {
            // Highlight missing directories in red
            paintIfMissing(srcRootField);
            paintIfMissing(locRootField);
            paintIfMissing(glosRootField);
            // Writable glossary must be inside glossary dir
            File wg = new File(writeableGlosField.getText());
            File wParent = wg.getParentFile();
            if (wParent == null || !wParent.equals(new File(glosRootField.getText()))) {
                writeableGlosField.setForeground(Color.RED);
            }
            paintIfMissing(tmRootField);
            paintIfMissing(dictRootField);
            // Disable export controls
            exportOmegaT.setEnabled(false);
            exportL1.setEnabled(false);
            exportL2.setEnabled(false);
        }
    }

    private void paintIfMissing(JTextField field) {
        if (!new File(field.getText()).isDirectory()) {
            field.setForeground(Color.RED);
        }
    }

    @Override
    public @Nullable String validateInput() {
        if (mode != ProjectConfigMode.NEW_PROJECT) {
            if (!new File(srcRootField.getText()).isDirectory()) {
                return OStrings.getString("NP_SOURCEDIR_DOESNT_EXIST");
            }
            if (!new File(locRootField.getText()).isDirectory()) {
                return OStrings.getString("NP_TRANSDIR_DOESNT_EXIST");
            }
            if (!new File(glosRootField.getText()).isDirectory()) {
                return OStrings.getString("NP_GLOSSDIR_DOESNT_EXIST");
            }
            File wg = new File(writeableGlosField.getText());
            if (wg.getParentFile() == null || !wg.getParentFile().equals(new File(glosRootField.getText()))) {
                return OStrings.getString("NP_W_GLOSDIR_NOT_INSIDE_GLOS");
            }
            if (!new File(tmRootField.getText()).isDirectory()) {
                return OStrings.getString("NP_TMDIR_DOESNT_EXIST");
            }
            if (!new File(dictRootField.getText()).isDirectory()) {
                return OStrings.getString("NP_DICTDIR_DOESNT_EXIST");
            }
            if (!new File(exportTmRoot.getText()).isDirectory()) {
                return OStrings.getString("NP_EXPORT_TMDIR_DOESNT_EXIST");
            }
        }
        return null;
    }

    @Override
    public void onSave(ProjectProperties p) {
        p.setSourceRoot(ensureSep(srcRootField.getText()));
        p.setTargetRoot(ensureSep(locRootField.getText()));
        p.setGlossaryRoot(ensureSep(glosRootField.getText()));
        p.setWriteableGlossary(writeableGlosField.getText());
        p.setTMRoot(ensureSep(tmRootField.getText()));
        p.setDictRoot(ensureSep(dictRootField.getText()));
        p.setExportTMRoot(ensureSep(exportTmRoot.getText()));
        p.setExportTmLevels(exportOmegaT.isSelected(), exportL1.isSelected(), exportL2.isSelected());
    }

    private String ensureSep(String s) {
        if (s == null) {
            return "";
        }
        return s.endsWith(File.separator) ? s : s + File.separator;
    }

    public static final String SRC_EXCLUDES_BUTTON_NAME = "project_properties_src_excludes_button";
    public static final String TM_BROWSE_BUTTON_NAME = "project_properties_tm_browse";
    public static final String GLOSSARY_BROWSE_BUTTON_NAME = "project_properties_glossary_browse_button";
    public static final String WRITABLE_GLOSSARY_BROWSE_BUTTON_NAME =
            "project_properties_writable_glossary_browse_button";
    public static final String LOC_BROWSE_BUTTON_NAME = "project_properties_loc_browse_button";
    public static final String DICTIONARY_BROWSE_BUTTON_NAME = "project_properties_dictionary_browse_button";
}
