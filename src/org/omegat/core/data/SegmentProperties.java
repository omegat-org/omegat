/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
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

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility methods for working with segment property arrays. These arrays are simple key-value maps, where
 * keys are on the even indexes and associated values immediately follow.
 *
 * @author Aaron Madlon-Kay
 *
 */
public final class SegmentProperties {

    private SegmentProperties() {
    }

    public static final String[] EMPTY_PROPS = new String[0];

    // Standard keys
    public static final String COMMENT = "comment";
    public static final String REFERENCE = "reference";

    public static boolean isEmpty(String[] props) {
        return props == null || props.length == 0;
    }

    public static String[] copy(String[] props) {
        return isEmpty(props) ? EMPTY_PROPS : Arrays.copyOf(props, props.length);
    }

    public static String joinValues(String[] props) {
        if (props.length == 0) {
            return "";
        }
        // Avoid creating a new string if we can avoid it.
        // This should be the majority case.
        if (props.length == 2) {
            return props[1];
        }
        return IntStream.range(0, props.length).filter(i -> i % 2 != 0).mapToObj(i -> props[i])
                .collect(Collectors.joining("\n"));
    }

    public static boolean isReferenceEntry(String[] props) {
        if (isEmpty(props)) {
            return false;
        }
        String value = getProperty(props, REFERENCE);
        return Boolean.parseBoolean(value);
    }

    public static String getProperty(String[] props, String key) {
        for (int i = 0; i < props.length; i += 2) {
            if (key.equals(props[i])) {
                return props[i + 1];
            }
        }
        return null;
    }
}
