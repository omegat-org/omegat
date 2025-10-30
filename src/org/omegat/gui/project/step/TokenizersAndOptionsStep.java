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
import org.omegat.util.OStrings;
import org.omegat.util.gui.TokenizerComboBoxRenderer;

/**
 * Step to configure tokenizers and core options.
 */
public class TokenizersAndOptionsStep implements Step {
    private final ProjectConfigMode mode;
    private final JPanel panel = new JPanel();
    private final JComboBox<Class<?>> srcTok;
    private final JComboBox<Class<?>> trgTok;
    private final JCheckBox sentenceSeg;
    private final JCheckBox allowDefaults;
    private final JCheckBox removeTags;

    public TokenizersAndOptionsStep(ProjectConfigMode mode) {
        this.mode = mode;
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        // Tokenizers
        JPanel t1 = new JPanel();
        t1.setLayout(new BoxLayout(t1, BoxLayout.X_AXIS));
        t1.add(new JLabel(OStrings.getString("PP_SRC_TOK")));
        t1.add(Box.createHorizontalStrut(8));
        Class<?>[] tokenizers = PluginUtils.getTokenizerClasses().stream()
                .sorted(java.util.Comparator.comparing(Class::getName)).toArray(Class[]::new);
        srcTok = new JComboBox<>(tokenizers);
        srcTok.setRenderer(new TokenizerComboBoxRenderer());
        t1.add(srcTok);
        panel.add(t1);

        panel.add(Box.createVerticalStrut(6));

        JPanel t2 = new JPanel();
        t2.setLayout(new BoxLayout(t2, BoxLayout.X_AXIS));
        t2.add(new JLabel(OStrings.getString("PP_LOC_TOK")));
        t2.add(Box.createHorizontalStrut(8));
        trgTok = new JComboBox<>(tokenizers);
        trgTok.setRenderer(new TokenizerComboBoxRenderer());
        t2.add(trgTok);
        panel.add(t2);

        panel.add(Box.createVerticalStrut(12));

        // Options
        sentenceSeg = new JCheckBox(OStrings.getString("PP_SENTENCE_SEGMENTING"));
        allowDefaults = new JCheckBox(OStrings.getString("PP_ALLOW_DEFAULTS"));
        removeTags = new JCheckBox(OStrings.getString("PP_REMOVE_TAGS"));
        panel.add(sentenceSeg);
        panel.add(allowDefaults);
        panel.add(removeTags);
    }

    @Override
    public String getTitle() {
        return OStrings.getString("PP_OPTIONS");
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void onLoad(ProjectProperties p) {
        // Respect CLI overrides: disable fields if runtime preference set
        String cliTokSrc = RuntimePreferenceStore.getInstance().getTokenizerSource();
        if (cliTokSrc != null) {
            try {
                srcTok.setSelectedItem(Class.forName(cliTokSrc));
                srcTok.setEnabled(false);
            } catch (Throwable t) {
            }
        } else {
            srcTok.setSelectedItem(p.getSourceTokenizer());
        }
        String cliTokTrg = RuntimePreferenceStore.getInstance().getTokenizerTarget();
        if (cliTokTrg != null) {
            try {
                trgTok.setSelectedItem(Class.forName(cliTokTrg));
                trgTok.setEnabled(false);
            } catch (Throwable t) {
            }
        } else {
            trgTok.setSelectedItem(p.getTargetTokenizer());
        }
        sentenceSeg.setSelected(p.isSentenceSegmentingEnabled());
        allowDefaults.setSelected(p.isSupportDefaultTranslations());
        removeTags.setSelected(p.isRemoveTags());
        if (mode == ProjectConfigMode.RESOLVE_DIRS) {
            srcTok.setEnabled(false);
            trgTok.setEnabled(false);
            sentenceSeg.setEnabled(false);
            allowDefaults.setEnabled(false);
            removeTags.setEnabled(false);
        }
    }

    @Override
    public @Nullable String validateInput() {
        return null;
    }

    @Override
    public void onSave(ProjectProperties p) {
        if (srcTok.isEnabled()) {
            p.setSourceTokenizer((Class<?>) srcTok.getSelectedItem());
        }
        if (trgTok.isEnabled()) {
            p.setTargetTokenizer((Class<?>) trgTok.getSelectedItem());
        }
        p.setSentenceSegmentingEnabled(sentenceSeg.isSelected());
        p.setSupportDefaultTranslations(allowDefaults.isSelected());
        p.setRemoveTags(removeTags.isSelected());
    }
}
