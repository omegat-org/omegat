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

package org.omegat.util;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class StreamUtil {

    private StreamUtil() {
    }

    /**
     * Get a comparator that compares based on PRIMARY differences with the
     * current default locale.
     *
     * @param keyExtractor
     *            Function for obtaining strings that the {@link Collator} can
     *            compare
     * @return A comparator useful for e.g. sorting lists of files in a stable
     *         fashion
     */
    public static <T> Comparator<T> localeComparator(Function<? super T, ? extends String> keyExtractor) {
        // Get the locale collator and set its strength to PRIMARY
        Collator localeCollator = Collator.getInstance();
        localeCollator.setStrength(Collator.PRIMARY);
        return Comparator.comparing(keyExtractor, localeCollator::compare);
    }

    /**
     * Get a comparator that sorts according to the provided list. Items not
     * appearing in the list are sorted alphabetically.
     */
    public static Comparator<String> comparatorByList(List<String> order) {
        return (o1, o2) -> {
            int pos1, pos2;
            if (order != null) {
                pos1 = order.indexOf(o1);
                pos2 = order.indexOf(o2);
            } else {
                pos1 = 0;
                pos2 = 0;
            }
            if (pos1 < 0) {
                pos1 = Integer.MAX_VALUE;
            }
            if (pos2 < 0) {
                pos2 = Integer.MAX_VALUE;
            }
            if (pos1 < pos2) {
                return -1;
            } else if (pos1 > pos2) {
                return 1;
            } else {
                return o1.compareToIgnoreCase(o2);
            }
        };
    }

    public static <T> Predicate<T> patternFilter(String regex, Function<T, String> stringExtractor) {
        Predicate<String> p = Pattern.compile(regex).asPredicate();
        return o -> p.test(stringExtractor.apply(o));
    }
}
