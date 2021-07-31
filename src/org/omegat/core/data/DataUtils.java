/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.core.data;

import java.util.Set;
import java.util.TreeSet;

public final class DataUtils {

    private static final Set<String> INTERNAL_IDS = new TreeSet<>();

    private DataUtils() {
    }

    public static boolean isDuplicate(SourceTextEntry ste, TMXEntry te) {
        return ste.getDuplicate() == SourceTextEntry.DUPLICATE.NEXT && te.defaultTranslation;
    }

    static void putInternalId(final String id) {
        INTERNAL_IDS.add(id);
    }

    static String generateInternalId(String source, String creator, long created) {
        StringBuilder sb = new StringBuilder();
        sb.append(source).append(creator).append(created);
        String id = Integer.toHexString(sb.hashCode());
        if (INTERNAL_IDS.contains(id)) {
            String original_id = id;
            for (int i = 0; INTERNAL_IDS.contains(id) || i < Integer.MAX_VALUE; i++) {
                id = original_id + "-" + Integer.toHexString(i);
            }
        }
        return id;
    }
}
