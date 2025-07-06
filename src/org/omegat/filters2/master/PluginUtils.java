/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Alex Buloichik
               2021-2022 Hiroshi Miura
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

package org.omegat.filters2.master;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.omegat.plugin.PluginManager;
import org.omegat.util.StringUtil;

/**
 * Static utilities for OmegaT filter plugins.
 *
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class PluginUtils {

    /**
     * Plugin type definitions.
     */
    public enum PluginType {
        /** File filters that provide IFilter API. */
        FILTER("filter"),
        /**
         * Tokenizers, currently bundled and it is for backward compatibility.
         */
        TOKENIZER("tokenizer"),
        /** Markers, that provide IMaker, mostly bundled. */
        MARKER("marker"),
        /**
         * Machine Translator service connectors, that provide
         * IMachineTranslation API.
         */
        MACHINETRANSLATOR("machinetranslator"),
        /** A plugin that change base of OmegaT system, not recommended. */
        BASE("base"),
        /**
         * Glosary, that provide IGlossary API.
         */
        GLOSSARY("glossary"),
        /**
         * Dictionary files/services connectors, that provide IDictionary and/or
         * IDictionaryFactory API.
         */
        DICTIONARY("dictionary"),
        /**
         * theme, that register Swing-Look-and-Feel with OmegaT properties into
         * UIManager.
         */
        THEME("theme"),
        /**
         * team repository version control system connector plugins.
         */
        REPOSITORY("repository"),
        /**
         * Misc plugins, such as a GUI extension like web browser support.
         */
        MISCELLANEOUS("miscellaneous"),
        /**
         * Spellchecker plugins.
         */
        SPELLCHECK("spellcheck"),
        /**
         * language plugin that bundles LanguageTool-language module and
         * spell-check dictionaries.
         */
        LANGUAGE("language"),
        /**
         * When plugin does not define any of the above.
         */
        UNKNOWN("Undefined");

        private final String typeValue;

        PluginType(String type) {
            typeValue = type;
        }

        public String getTypeValue() {
            return typeValue;
        }

        public static PluginType getTypeByValue(String str) {
            if (!StringUtil.isEmpty(str)) {
                String sType = str.toLowerCase(Locale.ENGLISH);
                for (PluginType v : values()) {
                    if (v.getTypeValue().equals(sType)) {
                        return v;
                    }
                }
            }
            return UNKNOWN;
        }
    }

    /** Private constructor to disallow creation */
    private PluginUtils() {
    }

    @Deprecated
    public static void loadPlugins(final Map<String, String> params) {
        PluginManager.loadPlugins(params.get("dev-manifests"));
    }

    @Deprecated
    public List<Class<?>> getFilterClasses() {
        return PluginManager.getFilterClasses();
    }

    @Deprecated
    public List<Class<?>> getTokenizerClasses() {
        return PluginManager.getTokenizerClasses();
    }

    @Deprecated
    public List<Class<?>> getMarkerClasses() {
        return PluginManager.getMarkerClasses();
    }
}
