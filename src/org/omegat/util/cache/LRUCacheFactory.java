/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2022 Hiroshi Miura
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

package org.omegat.util.cache;

import org.omegat.util.cache.impl.CaffeineLRUCache;

/**
 * @author Hiroshi Miura
 */
public class LRUCacheFactory {

    private LRUCacheFactory() {
    }

    /**
     * @param maxCacheSize
     *            max capacity.
     */
    public static <K, V> LRUCache<K, V> createLRUCache(int maxCacheSize) {
        return new CaffeineLRUCache<>(maxCacheSize);
    }

    public static <K, V> LRUCache<K, V> createLRUCache(int initialCapacity, int maxCacheSize) {
        return new CaffeineLRUCache<>(initialCapacity, maxCacheSize);
    }

    public static <K, V> LRUCache<K, V> createLRUSoftCache(int maxCacheSize) {
        return new CaffeineLRUCache<>(16, maxCacheSize, false, true, false);
    }

    public static <K, V> LRUCache<K, V> createLRUSoftCache(int initialCapacity, int maxCacheSize) {
        return new CaffeineLRUCache<>(initialCapacity, maxCacheSize, false, true, false);
    }

    public static <K, V> LRUCache<K, V> createLRUWeakCache(int maxCacheSize) {
        return new CaffeineLRUCache<>(16, maxCacheSize, false, false, true);
    }

    public static <K, V> LRUCache<K, V> createLRUWeakCache(int initialCapacity, int maxCacheSize) {
        return new CaffeineLRUCache<>(initialCapacity, maxCacheSize, false, false, true);
    }
}
