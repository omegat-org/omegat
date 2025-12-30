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
import java.net.URL;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.omegat.gui.preferences.IPreferencesController;
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
        greetingPane.setName("GreetingPane");
        greetingPane.setComponentOrientation(getComponentOrientation());
        String language = Language.getLowerCaseLanguageFromLocale();
        String country = Language.getUpperCaseCountryFromLocale();
        String fullLocale = language + "_" + country;
        URL url = getPhilosophyResource(fullLocale);
        if (url == null) {
            url = getPhilosophyResource(language);
        }
        if (url == null) {
            url = getPhilosophyResource("en");
        }
        if (url != null) {
            try {
                greetingPane.setPage(url);
            } catch (IOException ignored) {
            }
        }
        JScrollPane greetScroll = new JScrollPane(greetingPane);
        greetScroll.setPreferredSize(new Dimension(280, 100));
        greetScroll.setBorder(BorderFactory.createTitledBorder(FirstTimeConfigurationWizardUtil.getString(
                "explain.title", "Explanation")));
    }

    private ComponentOrientation getComponentOrientation() {
        return BiDiUtils.isRtl(Language.getLowerCaseLanguageFromLocale()) ? ComponentOrientation.RIGHT_TO_LEFT
                : ComponentOrientation.LEFT_TO_RIGHT;
    }

    private @Nullable URL getPhilosophyResource(String lang) {
        return GreetingStepController.class.getResource(
                "/" + OConsts.HELP_DIR + "/" + OConsts.HELP_FIRST_STEPS_PREFIX + "/" + lang + "/" + PHILOSOPHY);
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
