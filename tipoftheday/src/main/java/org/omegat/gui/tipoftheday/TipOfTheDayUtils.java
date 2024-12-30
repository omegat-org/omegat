/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.omegat.util.StaticUtils;

public final class TipOfTheDayUtils {

    private TipOfTheDayUtils() {
    }

    static URI getTipsFileURI(String filename) {
        return getTipsFileURI(filename, getLocale());
    }

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

    private static URI getTipsFileURI(String filename, String lang) {
        String installDir = StaticUtils.installDir();
        File file = Paths.get(installDir, "docs", "tips", lang, filename).toFile();
        if (file.isFile()) {
            return file.toURI();
        }
        // find in classpath
        URL url = OmegaTTipOfTheDayModel.class.getResource("/tips/" + lang + '/' + filename);
        if (url != null) {
            try {
                return url.toURI();
            } catch (URISyntaxException ignored) {
            }
        }
        return null;
    }

    static InputStream getIndexStream(String filename) throws IOException {
        String lang = getLocale();
        String installDir = StaticUtils.installDir();
        Path path = Paths.get(installDir, "docs", "tips", lang, filename);
        if (path.toFile().isFile()) {
            return Files.newInputStream(path);
        }
        return OmegaTTipOfTheDayModel.class.getResourceAsStream("/tips/" + lang + '/' + filename);
    }

    static boolean hasIndex() {
        String lang = getLocale();
        String installDir = StaticUtils.installDir();
        Path path = Paths.get(installDir, "docs", "tips", lang, TipOfTheDayController.INDEX_YAML);
        if (path.toFile().isFile()) {
            return true;
        }
        URL url =
                TipOfTheDayUtils.class.getResource("/tips/" + lang + '/' + TipOfTheDayController.INDEX_YAML);
        return url != null;
    }
}
