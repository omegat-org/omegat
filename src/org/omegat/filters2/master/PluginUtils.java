/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Alex Buloichik
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

package org.omegat.filters2.master;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.omegat.core.plugins.PluginsManager;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.Tokenizer;
import org.omegat.util.Language;
import org.omegat.util.Log;

/**
 * Static utilities for OmegaT filter plugins.
 *
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class PluginUtils {

    /** Private constructor to disallow creation */
    private PluginUtils() {
    }

    /**
     * Loads all plugins from main classloader and from /plugins/ dir. We should
     * load all jars from /plugins/ dir first, because some plugin can use more
     * than one jar.
     */
    public static void loadPlugins(final Map<String, String> params) {
        PluginsManager.loadPlugins(params);
    }

    public static List<Class<?>> getFilterClasses() {
        return PluginsManager.getFilterClasses();
    }

    public static List<Class<?>> getTokenizerClasses() {
        return PluginsManager.getTokenizerClasses();
    }

    public static Class<?> getTokenizerClassForLanguage(Language lang) {
        if (lang == null) {
            return DefaultTokenizer.class;
        }

        // Prefer an exact match on the full ISO language code (XX-YY).
        Class<?> exactResult = searchForTokenizer(lang.getLanguage());
        if (isDefault(exactResult)) {
            return exactResult;
        }

        // Otherwise return a match for the language only (XX).
        Class<?> generalResult = searchForTokenizer(lang.getLanguageCode());
        if (isDefault(generalResult)) {
            return generalResult;
        } else if (exactResult != null) {
            return exactResult;
        } else if (generalResult != null) {
            return generalResult;
        }

        return DefaultTokenizer.class;
    }

    private static boolean isDefault(Class<?> c) {
        if (c == null) {
            return false;
        }
        Tokenizer ann = c.getAnnotation(Tokenizer.class);
        return ann != null && ann.isDefault();
    }

    private static Class<?> searchForTokenizer(String lang) {
        if (lang.isEmpty()) {
            return null;
        }

        lang = lang.toLowerCase(Locale.ENGLISH);

        // Choose first relevant tokenizer as fallback if no
        // "default" tokenizer is found.
        Class<?> fallback = null;

        for (Class<?> c : getTokenizerClasses()) {
            Tokenizer ann = c.getAnnotation(Tokenizer.class);
            if (ann == null) {
                continue;
            }
            String[] languages = ann.languages();
            try {
                if (languages.length == 1 && languages[0].equals(Tokenizer.DISCOVER_AT_RUNTIME)) {
                    languages = ((ITokenizer) c.getDeclaredConstructor().newInstance()).getSupportedLanguages();
                }
            } catch (Exception ex) {
                Log.log(ex);
            }
            for (String s : languages) {
                if (lang.equals(s)) {
                    if (ann.isDefault()) {
                        return c; // Return best possible match.
                    } else if (fallback == null) {
                        fallback = c;
                    }
                }
            }
        }

        return fallback;
    }

    public static List<Class<?>> getMarkerClasses() {
        return PluginsManager.getMarkerClasses();
    }

    public static List<Class<?>> getMachineTranslationClasses() {
        return PluginsManager.getMachineTranslationClasses();
    }

    public static List<Class<?>> getGlossaryClasses() {
        return PluginsManager.getGlossaryClasses();
    }

    public static void unloadPlugins() {
        PluginsManager.unloadPlugins();
    }
}
