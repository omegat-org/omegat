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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.RuntimePreferenceStore;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.project.ProjectConfigMode;
import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.gui.LanguageComboBoxRenderer;
import org.omegat.util.gui.TokenizerComboBoxRenderer;
import org.openide.awt.Mnemonics;

/**
 * Combined step to select languages and configure tokenizers and core options.
 * Merges former LanguagesStep and TokenizersAndOptionsStep.
 */
public class LanguagesAndOptionsStep implements Step {
    private final ProjectConfigMode mode;
    private final JPanel panel = new JPanel();

    // Languages
    private final JComboBox<Language> srcLang;
    private final JComboBox<Language> trgLang;

    // Tokenizers
    private final JComboBox<Class<?>> srcTok;
    private final JComboBox<Class<?>> trgTok;

    // Options
    private final JCheckBox sentenceSeg;
    private final JCheckBox allowDefaults;
    private final JCheckBox removeTags;

    public LanguagesAndOptionsStep(ProjectConfigMode mode) {
        this.mode = mode;
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Languages UI
        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
        JLabel srcLangLabel = new JLabel();
        Mnemonics.setLocalizedText(srcLangLabel, OStrings.getString("PP_SRC_LANG"));
        row1.add(srcLangLabel);
        row1.add(Box.createHorizontalStrut(8));
        var langs = Language.getLanguages();
        srcLang = new JComboBox<>(new DefaultComboBoxModel<>(langs.toArray(new Language[0])));
        srcLang.setEditable(true);
        srcLang.setRenderer(new LanguageComboBoxRenderer());
        row1.add(srcLang);
        panel.add(row1);

        panel.add(Box.createVerticalStrut(6));

        JPanel row2 = new JPanel();
        row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
        JLabel trgLangLabel = new JLabel();
        Mnemonics.setLocalizedText(trgLangLabel, OStrings.getString("PP_LOC_LANG"));
        row2.add(trgLangLabel);
        row2.add(Box.createHorizontalStrut(8));
        trgLang = new JComboBox<>(new DefaultComboBoxModel<>(langs.toArray(new Language[0])));
        trgLang.setEditable(true);
        trgLang.setRenderer(new LanguageComboBoxRenderer());
        row2.add(trgLang);
        panel.add(row2);

        panel.add(Box.createVerticalStrut(12));

        // Tokenizers UI
        Class<?>[] tokenizers = PluginUtils.getTokenizerClasses().stream()
                .sorted(java.util.Comparator.comparing(Class::getName)).toArray(Class[]::new);

        JPanel t1 = new JPanel();
        t1.setLayout(new BoxLayout(t1, BoxLayout.X_AXIS));
        JLabel srcTokLabel = new JLabel();
        Mnemonics.setLocalizedText(srcTokLabel, OStrings.getString("PP_SRC_TOK"));
        t1.add(srcTokLabel);
        t1.add(Box.createHorizontalStrut(8));
        srcTok = new JComboBox<>(tokenizers);
        srcTok.setRenderer(new TokenizerComboBoxRenderer());
        t1.add(srcTok);
        panel.add(t1);

        panel.add(Box.createVerticalStrut(6));

        JPanel t2 = new JPanel();
        t2.setLayout(new BoxLayout(t2, BoxLayout.X_AXIS));
        JLabel trgTokLabel = new JLabel();
        Mnemonics.setLocalizedText(trgTokLabel, OStrings.getString("PP_LOC_TOK"));
        t2.add(trgTokLabel);
        t2.add(Box.createHorizontalStrut(8));
        trgTok = new JComboBox<>(tokenizers);
        trgTok.setRenderer(new TokenizerComboBoxRenderer());
        t2.add(trgTok);
        panel.add(t2);

        panel.add(Box.createVerticalStrut(12));

        // Options
        sentenceSeg = new JCheckBox();
        Mnemonics.setLocalizedText(sentenceSeg, OStrings.getString("PP_SENTENCE_SEGMENTING"));
        allowDefaults = new JCheckBox();
        Mnemonics.setLocalizedText(allowDefaults, OStrings.getString("PP_ALLOW_DEFAULTS"));
        removeTags = new JCheckBox();
        Mnemonics.setLocalizedText(removeTags, OStrings.getString("PP_REMOVE_TAGS"));
        panel.add(sentenceSeg);
        panel.add(allowDefaults);
        panel.add(removeTags);
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
        srcLang.setSelectedItem(p.getSourceLanguage());
        trgLang.setSelectedItem(p.getTargetLanguage());

        // Tokenizers, respecting CLI overrides
        String cliTokSrc = RuntimePreferenceStore.getInstance().getTokenizerSource();
        if (cliTokSrc != null) {
            try {
                srcTok.setSelectedItem(Class.forName(cliTokSrc));
                srcTok.setEnabled(false);
            } catch (Exception ignored) {
            }
        } else {
            srcTok.setSelectedItem(p.getSourceTokenizer());
        }
        String cliTokTrg = RuntimePreferenceStore.getInstance().getTokenizerTarget();
        if (cliTokTrg != null) {
            try {
                trgTok.setSelectedItem(Class.forName(cliTokTrg));
                trgTok.setEnabled(false);
            } catch (Exception ignored) {
            }
        } else {
            trgTok.setSelectedItem(p.getTargetTokenizer());
        }

        // Options
        sentenceSeg.setSelected(p.isSentenceSegmentingEnabled());
        allowDefaults.setSelected(p.isSupportDefaultTranslations());
        removeTags.setSelected(p.isRemoveTags());

        if (mode == ProjectConfigMode.RESOLVE_DIRS) {
            // In resolve mode, all these are informational and disabled
            srcLang.setEnabled(false);
            trgLang.setEnabled(false);
            srcTok.setEnabled(false);
            trgTok.setEnabled(false);
            sentenceSeg.setEnabled(false);
            allowDefaults.setEnabled(false);
            removeTags.setEnabled(false);
        }
    }

    @Override
    public @Nullable String validateInput() {
        Object s = srcLang.getSelectedItem();
        Object t = trgLang.getSelectedItem();
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
        p.setSourceLanguage(srcLang.getSelectedItem().toString());
        p.setTargetLanguage(trgLang.getSelectedItem().toString());
        // Tokenizers
        if (srcTok.isEnabled()) {
            p.setSourceTokenizer((Class<?>) srcTok.getSelectedItem());
        }
        if (trgTok.isEnabled()) {
            p.setTargetTokenizer((Class<?>) trgTok.getSelectedItem());
        }
        // Options
        p.setSentenceSegmentingEnabled(sentenceSeg.isSelected());
        p.setSupportDefaultTranslations(allowDefaults.isSelected());
        p.setRemoveTags(removeTags.isSelected());
    }
}
