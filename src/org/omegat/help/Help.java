/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk,
                            Sandra Jean Chua, and Henry Pijffers
               2007 Didier Briel
               2009 Alex Buloichik
               2015 Aaron Madlon-Kay
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

package org.omegat.help;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.omegat.util.Language;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.DesktopWrapper;

/**
 * A utility class for accessing bundled local or online documentation.
 *
 * @author Keith Godfrey
 * @author Sandra Jean Chua - sachachua at users.sourceforge.net
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public final class Help {

    private Help() {
    }

    /**
     * URL for the online manual.
     */
    public static final String ONLINE_HELP_URL = OStrings.IS_BETA
            ? "https://omegat.sourceforge.io/manual-latest/"
            : "https://omegat.sourceforge.io/manual-standard/";

    public static final String ONLINE_JAVADOC_URL = OStrings.IS_BETA
            ? "https://omegat.sourceforge.io/javadoc-latest/"
            : "https://omegat.sourceforge.io/javadoc-standard/";

    public static void showJavadoc() throws IOException {
        URI uri = URI.create(ONLINE_JAVADOC_URL);
        DesktopWrapper.browse(uri);
    }

    /**
     * Shows help in the system browser.
     *
     * @throws IOException
     */
    public static void showHelp() throws IOException {
        String lang = detectHelpLanguage();
        URI uri = getHelpFileURI(lang, OConsts.HELP_HOME);
        if (uri == null) {
            uri = URI.create(ONLINE_HELP_URL);
        }
        DesktopWrapper.browse(uri);
    }

    public static URI getHelpFileURI(String filename) {
        return getHelpFileURI(null, filename);
    }

    public static URI getHelpFileURI(String lang, String filename) {
        // find in install dir
        String path = lang == null ? filename : lang + File.separator + filename;
        File file = Paths.get(StaticUtils.installDir(), OConsts.HELP_DIR, path).toFile();
        if (file.isFile()) {
            return file.toURI();
        }
        // find in classpath
        path = lang == null ? filename : lang + '/' + filename;
        URL url = Help.class.getResource('/' + OConsts.HELP_DIR + '/' + path);
        if (url != null) {
            try {
                return url.toURI();
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        }
        return null;
    }

    // immortalize the BeOS 404 messages (some modified a bit for context)
    public static String errorHaiku() {
        int id = ThreadLocalRandom.current().nextInt(11) + 1;
        return OStrings.getString("HF_HAIKU_" + id);
    }

    /**
     * Detects the documentation language to use.
     *
     * If the latest manual is not available in the system locale language, it
     * returns null, i.e. show a language selection screen.
     */
    private static String detectHelpLanguage() {
        // Get the system locale (language and country)
        String language = Language.getLowerCaseLanguageFromLocale();
        String country = Language.getUpperCaseCountryFromLocale();

        // Check if there's a translation for the full locale (lang + country)
        String locale = language + "_" + country;
        String version = getDocVersion(locale);
        if (OStrings.VERSION.equals(version)) {
            return locale;
        }

        // Check if there's a translation for the language only
        version = getDocVersion(language);
        if (OStrings.VERSION.equals(version)) {
            return language;
        }

        // No suitable translation found
        return null;
    }

    /**
     * Returns the version of (a translation of) the user manual. If there is no
     * translation for the specified locale, null is returned.
     */
    private static String getDocVersion(String locale) {
        // Check if there's a manual for the specified locale
        // (Assume yes if the index file is there)

        if (getHelpFileURI(locale, OConsts.HELP_HOME) == null) {
            return null;
        }

        // Load the property file containing the doc version
        Properties prop = new Properties();
        URI u = getHelpFileURI(locale, "version_" + locale + ".properties");
        if (u == null) {
            u = getHelpFileURI(locale, "version.properties");
        }
        try (InputStream in = u.toURL().openStream()) {
            prop.load(in);
        } catch (IOException ex) {
            return null;
        }

        // Get the doc version and return it
        // (null if the version entry is not present)
        return prop.getProperty("version");
    }
}
