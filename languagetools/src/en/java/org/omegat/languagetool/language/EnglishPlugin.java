/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura
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

package org.omegat.languagetool.language;

import java.io.IOException;

import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.AustralianEnglish;
import org.languagetool.language.CanadianEnglish;
import org.languagetool.language.NewZealandEnglish;
import org.languagetool.language.SouthAfricanEnglish;

import org.omegat.languagetools.LanguageToolLanguageManager;

public class EnglishPlugin {

    public static void loadPlugins() throws IOException {
        LanguageToolLanguageManager.registerLTLanguage(new AmericanEnglish());
        LanguageToolLanguageManager.registerLTLanguage(new AustralianEnglish());
        LanguageToolLanguageManager.registerLTLanguage(new CanadianEnglish());
        LanguageToolLanguageManager.registerLTLanguage(new NewZealandEnglish());
        LanguageToolLanguageManager.registerLTLanguage(new SouthAfricanEnglish());
    }

    public static void unloadPlugins() {
    }

}
