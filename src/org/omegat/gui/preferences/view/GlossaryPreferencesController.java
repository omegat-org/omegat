/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.gui.preferences.view;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import org.omegat.gui.glossary.GlossaryRenderers;
import org.omegat.gui.glossary.IGlossaryRenderer;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.DelegatingComboBoxRenderer;

/**
 * @author Aaron Madlon-Kay
 */
public class GlossaryPreferencesController extends BasePreferencesController {

    private GlossaryPreferencesPanel panel;

    @Override
    public JComponent getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    @Override
    public String toString() {
        return OStrings.getString("PREFS_TITLE_GLOSSARY");
    }

    private void initGui() {
        panel = new GlossaryPreferencesPanel();
        panel.cbGlossaryLayout.setModel(new DefaultComboBoxModel<>(GlossaryRenderers.getAll().toArray(new IGlossaryRenderer[0])));
        panel.cbGlossaryLayout.setRenderer(new DelegatingComboBoxRenderer<IGlossaryRenderer, String>() {
            @Override
            protected String getDisplayText(IGlossaryRenderer value) {
                return value.getName();
            }
        });
    }

    @Override
    protected void initFromPrefs() {
        panel.displayContextCheckBox.setSelected(Preferences.isPreferenceDefault(
                Preferences.GLOSSARY_TBX_DISPLAY_CONTEXT, Preferences.GLOSSARY_TBX_DISPLAY_CONTEXT_DEFAULT));
        panel.useSeparateTermsCheckBox.setSelected(Preferences.isPreferenceDefault(
                Preferences.GLOSSARY_NOT_EXACT_MATCH, Preferences.GLOSSARY_NOT_EXACT_MATCH_DEFAULT));
        panel.useStemmingCheckBox.setSelected(Preferences.isPreferenceDefault(Preferences.GLOSSARY_STEMMING,
                Preferences.GLOSSARY_STEMMING_DEFAULT));
        panel.replaceHitsCheckBox.setSelected(Preferences.isPreference(Preferences.GLOSSARY_REPLACE_ON_INSERT));
        panel.requireSimilarCaseCheckBox.setSelected(Preferences.isPreferenceDefault(Preferences.GLOSSARY_REQUIRE_SIMILAR_CASE,
                Preferences.GLOSSARY_REQUIRE_SIMILAR_CASE_DEFAULT));
        panel.cbGlossaryLayout.setSelectedItem(GlossaryRenderers.getPreferredGlossaryRenderer());
        panel.mergeAlternateDefinitionsCheckBox.setSelected(Preferences.isPreferenceDefault(Preferences.GLOSSARY_MERGE_ALTERNATE_DEFINITIONS, 
                Preferences.GLOSSARY_MERGE_ALTERNATE_DEFINITIONS_DEFAULT));
    }

    @Override
    public void restoreDefaults() {
        panel.displayContextCheckBox.setSelected(Preferences.GLOSSARY_TBX_DISPLAY_CONTEXT_DEFAULT);
        panel.useSeparateTermsCheckBox.setSelected(Preferences.GLOSSARY_NOT_EXACT_MATCH_DEFAULT);
        panel.useStemmingCheckBox.setSelected(Preferences.GLOSSARY_STEMMING_DEFAULT);
        panel.replaceHitsCheckBox.setSelected(false);
        panel.requireSimilarCaseCheckBox.setSelected(Preferences.GLOSSARY_REQUIRE_SIMILAR_CASE_DEFAULT);
        panel.cbGlossaryLayout.setSelectedItem(GlossaryRenderers.DEFAULT_RENDERER);
        panel.mergeAlternateDefinitionsCheckBox.setSelected(Preferences.GLOSSARY_MERGE_ALTERNATE_DEFINITIONS_DEFAULT);
    }

    @Override
    public void persist() {
        Preferences.setPreference(Preferences.GLOSSARY_TBX_DISPLAY_CONTEXT, panel.displayContextCheckBox.isSelected());
        Preferences.setPreference(Preferences.GLOSSARY_NOT_EXACT_MATCH, panel.useSeparateTermsCheckBox.isSelected());
        Preferences.setPreference(Preferences.GLOSSARY_STEMMING, panel.useStemmingCheckBox.isSelected());
        Preferences.setPreference(Preferences.GLOSSARY_REPLACE_ON_INSERT, panel.replaceHitsCheckBox.isSelected());
        Preferences.setPreference(Preferences.GLOSSARY_REQUIRE_SIMILAR_CASE, panel.requireSimilarCaseCheckBox.isSelected());
        GlossaryRenderers.setPreferredGlossaryRenderer((IGlossaryRenderer) panel.cbGlossaryLayout.getSelectedItem());
        Preferences.setPreference(Preferences.GLOSSARY_MERGE_ALTERNATE_DEFINITIONS, panel.mergeAlternateDefinitionsCheckBox.isSelected());
    }
}
