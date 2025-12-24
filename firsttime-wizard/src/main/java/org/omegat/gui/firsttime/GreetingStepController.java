/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2025 Hiroshi Miura.
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.omegat.gui.firsttime;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URI;

import org.omegat.gui.preferences.IPreferencesController;
import org.omegat.help.Help;
import org.omegat.util.BiDiUtils;
import org.omegat.util.Language;
import org.omegat.util.OConsts;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * Simple preferences controller used for the last step of the First Time Config wizard.
 * It shows the localized "Philosophy" content and contains no settings.
 */
final class GreetingStepController implements IPreferencesController {

    private static final String PHILOSOPHY = "philosophy.html";

    private final JTextPane greetingPane = new JTextPane();

    GreetingStepController() {
        initGreetingPane();
    }

    private void initGreetingPane() {
        greetingPane.setEditable(false);
        greetingPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        try {
            String language = detectFirstStepsLanguage();
            greetingPane.setName("GreetingPane");
            greetingPane.setComponentOrientation(
                    BiDiUtils.isRtl(language) ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
            URI uri = Help.getHelpFileURI(OConsts.HELP_FIRST_STEPS_PREFIX, language, PHILOSOPHY);
            if (uri != null) {
                greetingPane.setPage(uri.toURL());
            }
        } catch (IOException ignored) {
        }
        JScrollPane greetScroll = new JScrollPane(greetingPane);
        greetScroll.setPreferredSize(new Dimension(280, 100));
        greetScroll.setBorder(BorderFactory.createTitledBorder(FirstTimeConfigurationWizardUtil.getString(
                "explain.title", "Explanation")));
    }

    private String detectFirstStepsLanguage() {
        String language = Language.getLowerCaseLanguageFromLocale();
        String country = Language.getUpperCaseCountryFromLocale();
        String fullLocale = language + "_" + country;
        if (Help.getHelpFileURI(OConsts.HELP_FIRST_STEPS_PREFIX, fullLocale, OConsts.HELP_FIRST_STEPS) != null) {
            return fullLocale;
        }
        if (Help.getHelpFileURI(OConsts.HELP_FIRST_STEPS_PREFIX, language, OConsts.HELP_FIRST_STEPS) != null) {
            return language;
        }
        return "en";
    }

    @Override
    public void addFurtherActionListener(FurtherActionListener listener) {
        // no-op
    }

    @Override
    public void removeFurtherActionListener(FurtherActionListener listener) {
        // no-op
    }

    @Override
    public boolean isRestartRequired() {
        return false;
    }

    @Override
    public boolean isReloadRequired() {
        return false;
    }

    @Override
    public String toString() {
        return FirstTimeConfigurationWizardUtil.getString("step.greeting", "Next step guide");
    }

    @Override
    public Component getGui() {
        return greetingPane;
    }

    @Override
    public void persist() {
        // no-op
    }

    @Override
    public void undoChanges() {
        // no-op
    }

    @Override
    public void restoreDefaults() {
        // no-op
    }

    @Override
    public boolean canRestoreDefaults() {
        return false;
    }
}
