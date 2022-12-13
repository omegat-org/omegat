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

import java.util.Map;

/**
 * @author Hiroshi Miura
 */
public abstract class CacheFactory {

    protected CacheFactory() {
    }

    public static CacheFactory getInstance(String id) {
        if ("soft".equalsIgnoreCase(id)) {
            return SoftCacheFactory.getInstance();
        } else if ("simple".equalsIgnoreCase(id)) {
            return SimpleCacheFactory.getInstance();
        } else {
            return SimpleCacheFactory.getInstance();
        }
    }

    /**
     * Create cache.
     * 
     * @param initialCapacity
     *            initial capacity
     * @param maxCacheSize
     *            max capacity.
     */
    public abstract <K, V> Map<K, V> createCache(int initialCapacity, int maxCacheSize);

}
