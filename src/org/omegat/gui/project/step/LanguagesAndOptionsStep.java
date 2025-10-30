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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import gen.core.project.RepositoryDefinition;
import org.jetbrains.annotations.Nullable;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.RuntimePreferenceStore;
import org.omegat.externalfinder.ExternalFinder;
import org.omegat.externalfinder.gui.ExternalFinderCustomizer;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.dialogs.RepositoriesMappingController;
import org.omegat.gui.project.ProjectConfigMode;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.LanguageComboBoxRenderer;
import org.omegat.util.gui.TokenizerComboBoxRenderer;
import org.openide.awt.Mnemonics;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

/**
 * Combined step to select languages and configure tokenizers and core options.
 * Merges former LanguagesStep and TokenizersAndOptionsStep.
 */
public class LanguagesAndOptionsStep implements Step {
    private final ProjectConfigMode mode;
    private final JPanel panel = new JPanel();

    // Remove Tags
    JCheckBox removeTagsCheckBox = new JCheckBox();

    // Languages
    private JComboBox<Language> sourceLocaleField = new JComboBox<>();
    private JComboBox<Language> targetLocaleField = new JComboBox<>();

    // Tokenizers
    private JComboBox<Class<?>> sourceTokenizerField;
    private JComboBox<Class<?>> targetTokenizerField;

    // Options
    private final JCheckBox sentenceSegmentingCheckBox = new JCheckBox();
    private final JCheckBox allowDefaultsCheckBox = new JCheckBox();


    // Repositories mapping
    JButton repositoriesButton = new JButton();

    // Repositories mapping
    JButton externalFinderButton = new JButton();

    public LanguagesAndOptionsStep(ProjectConfigMode mode) {
        this.mode = mode;
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(createLocalesBox());
        panel.add(createOptionsBox());
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
        localesBox.add(Box.createRigidArea(new Dimension(5, 0)));

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

        String cliTokSrc = RuntimePreferenceStore.getInstance().getTokenizerSource();
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

        String cliTokTrg = RuntimePreferenceStore.getInstance().getTokenizerTarget();
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
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weightx = 1.0;
        optionsBox.add(sentenceSegmentingCheckBox, gbc);

        // Repositories mapping
        Mnemonics.setLocalizedText(repositoriesButton, OStrings.getString("PP_REPOSITORIES"));
        repositoriesButton.setName(REPOSITORIES_BUTTON_NAME);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        optionsBox.add(repositoriesButton, gbc);

        // External finder
        Mnemonics.setLocalizedText(externalFinderButton, OStrings.getString("PP_EXTERNALFINDER"));
        externalFinderButton.setName(EXTERNAL_FINDER_BUTTON_NAME);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_END;
        optionsBox.add(externalFinderButton, gbc);

        // multiple translations
        Mnemonics.setLocalizedText(allowDefaultsCheckBox, OStrings.getString("PP_ALLOW_DEFAULTS"));
        allowDefaultsCheckBox.setName(ALLOW_DEFAULTS_CB_NAME);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        optionsBox.add(allowDefaultsCheckBox, gbc);

        // Remove Tags
        Mnemonics.setLocalizedText(removeTagsCheckBox, OStrings.getString("PP_REMOVE_TAGS"));
        removeTagsCheckBox.setName(REMOVE_TAGS_CB_NAME);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        optionsBox.add(removeTagsCheckBox, gbc);

        return optionsBox;
    }

    @Override
    public String getTitle() {
        return OStrings.getString("PP_LANGUAGES");
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void onLoad(ProjectProperties p) {
        // Languages
        sourceLocaleField.setSelectedItem(p.getSourceLanguage());
        targetLocaleField.setSelectedItem(p.getTargetLanguage());

        // Tokenizers, respecting CLI overrides
        String cliTokSrc = RuntimePreferenceStore.getInstance().getTokenizerSource();
        if (cliTokSrc != null) {
            try {
                sourceTokenizerField.setSelectedItem(Class.forName(cliTokSrc));
                sourceTokenizerField.setEnabled(false);
            } catch (Exception ignored) {
            }
        } else {
            sourceTokenizerField.setSelectedItem(p.getSourceTokenizer());
        }
        String cliTokTrg = RuntimePreferenceStore.getInstance().getTokenizerTarget();
        if (cliTokTrg != null) {
            try {
                targetTokenizerField.setSelectedItem(Class.forName(cliTokTrg));
                targetTokenizerField.setEnabled(false);
            } catch (Exception ignored) {
            }
        } else {
            targetTokenizerField.setSelectedItem(p.getTargetTokenizer());
        }

        // Options
        sentenceSegmentingCheckBox.setSelected(p.isSentenceSegmentingEnabled());
        allowDefaultsCheckBox.setSelected(p.isSupportDefaultTranslations());
        removeTagsCheckBox.setSelected(p.isRemoveTags());

        repositoriesButton.addActionListener(e -> {
            List<RepositoryDefinition> r = new RepositoriesMappingController().show(null, p.getRepositories());
            if (r != null) {
                p.setRepositories(r);
            }
        });
        externalFinderButton.addActionListener(e -> {
            var externalFinderConfig = ExternalFinder.getProjectConfig();
            ExternalFinderCustomizer dlg = new ExternalFinderCustomizer(true, externalFinderConfig);
            if (dlg.show(null)) {
                externalFinderConfig = dlg.getResult();
                ExternalFinder.setProjectConfig(externalFinderConfig);
            }
        });
        if (mode == ProjectConfigMode.RESOLVE_DIRS) {
            // In resolve mode, all these are informational and disabled
            sourceLocaleField.setEnabled(false);
            targetLocaleField.setEnabled(false);
            sourceTokenizerField.setEnabled(false);
            targetTokenizerField.setEnabled(false);
            sentenceSegmentingCheckBox.setEnabled(false);
            allowDefaultsCheckBox.setEnabled(false);
            removeTagsCheckBox.setEnabled(false);
        }
    }

    @Override
    public @Nullable String validateInput() {
        Object s = sourceLocaleField.getSelectedItem();
        Object t = targetLocaleField.getSelectedItem();
        if (s == null || t == null) {
            return OStrings.getString("NP_INVALID_SOURCE_LOCALE");
        }
        if (!Language.verifySingleLangCode(s.toString())) {
            return OStrings.getString("NP_INVALID_SOURCE_LOCALE");
        }
        if (!Language.verifySingleLangCode(t.toString())) {
            return OStrings.getString("NP_INVALID_TARGET_LOCALE");
        }
        return null;
    }

    @Override
    public void onSave(ProjectProperties p) {
        // Languages
        p.setSourceLanguage(sourceLocaleField.getSelectedItem().toString());
        p.setTargetLanguage(targetLocaleField.getSelectedItem().toString());
        // Tokenizers
        if (sourceTokenizerField.isEnabled()) {
            p.setSourceTokenizer((Class<?>) sourceTokenizerField.getSelectedItem());
        }
        if (targetTokenizerField.isEnabled()) {
            p.setTargetTokenizer((Class<?>) targetTokenizerField.getSelectedItem());
        }
        // Options
        p.setSentenceSegmentingEnabled(sentenceSegmentingCheckBox.isSelected());
        p.setSupportDefaultTranslations(allowDefaultsCheckBox.isSelected());
        p.setRemoveTags(removeTagsCheckBox.isSelected());

    }

    // component name definitions for ui test.
    public static final String EXTERNAL_FINDER_BUTTON_NAME = "project_properties_external_finder_button";
    public static final String SENTENCE_SEGMENTING_CB_NAME = "project_properties_sentence_segmenting_cb";
    public static final String SENTENCE_SEGMENTING_BUTTON_NAME = "project_properties_sentence_segmenting_button";
    public static final String ALLOW_DEFAULTS_CB_NAME = "project_properties_allow_defaults_cb";
    public static final String REMOVE_TAGS_CB_NAME = "project_properties_remove_tags_cb";
    public static final String FILE_FILTER_BUTTON_NAME = "project_properties_file_filter_button";
    public static final String SOURCE_TOKENIZER_FIELD_NAME = "project_properties_source_tokenizer_field";
    public static final String TARGET_TOKENIZER_FIELD_NAME = "project_properties_target_tokenizer_field";
    public static final String SOURCE_LOCALE_CB_NAME = "project_properties_source_locale_cb";
    public static final String TARGET_LOCALE_CB_NAME = "project_properties_target_locale_cb";
    public static final String REPOSITORIES_BUTTON_NAME = "project_properties_repositories_button";
}
