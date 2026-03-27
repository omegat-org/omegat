/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura.
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
package org.omegat.filters2.text.yaml;

import org.omegat.filters2.AbstractOptions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class YamlOptions extends AbstractOptions {

    public static final String OPTION_IGNORE_KEYS = "ignoreKeys";

    public YamlOptions(Map<String, String> options) {
        super(options);
    }

    public Set<String> getIgnoreKeys() {
        String ignoreKeysStr = getString(OPTION_IGNORE_KEYS, null);
        if (ignoreKeysStr == null || ignoreKeysStr.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(Set.of(ignoreKeysStr.split("\t")));
    }

    public void setIgnoreKeys(Set<String> ignoreKeys) {
        if (ignoreKeys == null || ignoreKeys.isEmpty()) {
            setString(OPTION_IGNORE_KEYS, "");
        } else {
            setString(OPTION_IGNORE_KEYS, String.join("\t", ignoreKeys));
        }
    }
}
