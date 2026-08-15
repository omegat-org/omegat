/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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
package org.omegat.gui.editor.sort;

import java.text.CollationKey;
import java.text.Collator;
import java.util.HashMap;
import java.util.Map;

/**
 * Locale-aware string comparator that compares pre-computed
 * {@link CollationKey}s instead of calling {@link Collator#compare} on every
 * comparison. The ordering is identical to the collator's by contract; the
 * keys are cached per distinct string, so a sort makes one key computation per
 * string instead of one collation per comparison. Instances live for one
 * sorter (like the sort itself), so the cache size is bounded by the file.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
class CachingCollatorComparator implements TextKeyComparator {

    private final Collator collator;
    private final Map<String, CollationKey> cache = new HashMap<>();

    CachingCollatorComparator(Collator collator) {
        this.collator = collator;
    }

    @Override
    public void prime(String s) {
        key(s);
    }

    @Override
    public int compare(String a, String b) {
        return key(a).compareTo(key(b));
    }

    private CollationKey key(String s) {
        return cache.computeIfAbsent(s, collator::getCollationKey);
    }
}
