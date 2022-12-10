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

import org.omegat.util.cache.impl.CaffeineLRUCacheFactory;
import org.omegat.util.cache.impl.CaffeineLRUSoftCacheFactory;
import org.omegat.util.cache.impl.CaffeineLRUWeakCacheFactory;
import org.omegat.util.cache.impl.SimpleLRUCacheFactory;

/**
 * @author Hiroshi Miura
 */
public abstract class LRUCacheFactory {

    protected LRUCacheFactory() {
    }

    static public LRUCacheFactory getFactory(String id) {
        if (id.equalsIgnoreCase("Caffeine")) {
            return CaffeineLRUCacheFactory.getInstance();
        } else if (id.equalsIgnoreCase("Weak")) {
            return CaffeineLRUWeakCacheFactory.getInstance();
        } else if (id.equalsIgnoreCase("Soft")) {
            return CaffeineLRUSoftCacheFactory.getInstance();
        } else {
            return SimpleLRUCacheFactory.getInstance();
        }
    }

    /**
     * Create default cache.
     * 
     * @param maxCacheSize
     *            max capacity.
     */
    public abstract <K, V> LRUCache<K, V> createLRUCache(int maxCacheSize);

    /**
     * Create default cache.
     * 
     * @param initialCapacity
     *            initial capacity
     * @param maxCacheSize
     *            max capacity.
     */
    public abstract <K, V> LRUCache<K, V> createLRUCache(int initialCapacity, int maxCacheSize);

}
