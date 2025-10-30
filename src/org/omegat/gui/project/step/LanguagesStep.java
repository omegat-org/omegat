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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.data.ProjectProperties;
import org.omegat.gui.project.ProjectConfigMode;
import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.gui.LanguageComboBoxRenderer;

/**
 * Step to select languages.
 */
public class LanguagesStep implements Step {
    private final ProjectConfigMode mode;
    private final JPanel panel = new JPanel();
    private final JComboBox<Language> src;
    private final JComboBox<Language> trg;

    public LanguagesStep(ProjectConfigMode mode) {
        this.mode = mode;
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
        row1.add(new JLabel(OStrings.getString("PP_SRC_LANG")));
        row1.add(Box.createHorizontalStrut(8));
        var langs1 = Language.getLanguages();
        src = new JComboBox<>(new javax.swing.DefaultComboBoxModel<>(langs1.toArray(new Language[0])));
        src.setEditable(true);
        src.setRenderer(new LanguageComboBoxRenderer());
        row1.add(src);
        panel.add(row1);

        panel.add(Box.createVerticalStrut(6));

        JPanel row2 = new JPanel();
        row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
        row2.add(new JLabel(OStrings.getString("PP_LOC_LANG")));
        row2.add(Box.createHorizontalStrut(8));
        var langs2 = Language.getLanguages();
        trg = new JComboBox<>(new javax.swing.DefaultComboBoxModel<>(langs2.toArray(new Language[0])));
        trg.setEditable(true);
        trg.setRenderer(new LanguageComboBoxRenderer());
        row2.add(trg);
        panel.add(row2);
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
        src.setSelectedItem(p.getSourceLanguage());
        trg.setSelectedItem(p.getTargetLanguage());
        if (mode == ProjectConfigMode.RESOLVE_DIRS) {
            src.setEnabled(false);
            trg.setEnabled(false);
        }
    }

    @Override
    public @Nullable String validateInput() {
        Object s = src.getSelectedItem();
        Object t = trg.getSelectedItem();
        if (s == null || t == null)
            return OStrings.getString("NP_INVALID_SOURCE_LOCALE");
        if (!Language.verifySingleLangCode(s.toString()))
            return OStrings.getString("NP_INVALID_SOURCE_LOCALE");
        if (!Language.verifySingleLangCode(t.toString()))
            return OStrings.getString("NP_INVALID_TARGET_LOCALE");
        return null;
    }

    @Override
    public void onSave(ProjectProperties p) {
        p.setSourceLanguage(src.getSelectedItem().toString());
        p.setTargetLanguage(trg.getSelectedItem().toString());
    }
}
