/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023-2025 Hiroshi Miura.
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

package org.omegat.gui.tipoftheday;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.omegat.help.Help;

public final class TipOfTheDayUtils {

    static final String TIPS_DIR = "tips";

    private TipOfTheDayUtils() {
    }

    static final String INDEX_YAML = "tips.yaml";

    static String getLocale() {
        // Get the system locale (language and country)
        String language = Locale.getDefault().getLanguage().toLowerCase(Locale.ENGLISH);
        String country = Locale.getDefault().getCountry().toUpperCase(Locale.ENGLISH);
        String lang;
        if (language.equals("zh") || country.equals("BR")) {
            lang = language + "_" + country;
        } else {
            lang = language;
        }
        return lang;
    }

    static InputStream getIndexStream() throws IOException {
        return Help.getHelpFileURI(TIPS_DIR, getLocale(), INDEX_YAML).toURL().openStream();
    }

    static boolean hasIndex() {
        try (InputStream is = getIndexStream()) { // validate exists
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
