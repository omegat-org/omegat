/*******************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Hiroshi Miura
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
 ******************************************************************************/

package org.omegat.core.spellchecker;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class DictionaryBroker {

    private static final Set<URI> dictionaries = new HashSet<>();

    private DictionaryBroker() {
    }

    public static void registerDictionary(URI uri) {
        dictionaries.add(uri);
    }

    public static URI getDictionaryURI(String filename) {
        return dictionaries.stream().filter(uri -> uri.getScheme().startsWith("jar"))
                .filter(uri -> uri.getSchemeSpecificPart().endsWith(filename)).findFirst().orElse(null);
    }
}
