/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008-2012 Martin Fleurke
               2025 Hiroshi Miura
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
package org.omegat.util.nlp;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public final class PluralData {

    private static final PluralData INSTANCE = new PluralData();

    public static PluralData getInstance() {
        return INSTANCE;
    }

    private final HashMap<String, PluralInfo> info = new HashMap<>();

    private PluralData() {
        Properties props = new Properties();
        try (InputStream is = PluralData.class.getResourceAsStream("plurals.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException ignored) {
            // ignore and leave map empty
        }

        for (String lang : props.stringPropertyNames()) {
            String val = props.getProperty(lang);
            if (val == null) {
                continue;
            }
            val = val.trim();
            if (val.isEmpty()) {
                continue;
            }
            try {
                info.put(lang, new PluralInfo(val));
            } catch (IllegalArgumentException ignored) {
                // skip invalid entries silently
            }
        }
    }

    public @Nullable PluralInfo getPlural(String lang) {
        return info.get(lang);
    }
}
