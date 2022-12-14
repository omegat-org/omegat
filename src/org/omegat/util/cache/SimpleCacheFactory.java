/*
 * *************************************************************************
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2022.
 *                Home page: http://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *************************************************************************
 *
 */

package org.omegat.util.cache;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Hiroshi Miura
 */
public class SimpleCacheFactory extends CacheFactory {

    private static CacheFactory instance = null;

    private SimpleCacheFactory() {
        super();
    }

    public static CacheFactory getInstance() {
        if (instance == null) {
            instance = new SimpleCacheFactory();
        }
        return instance;
    }

    /**
     * Create cache.
     * 
     * @param initialCapacity
     *            initial capacity
     * @param maxCacheSize
     *            max capacity.
     */
    public <K, V> Map<K, V> createCache(int initialCapacity, int maxCacheSize) {
        return  Collections.synchronizedMap(new SimpleCache<>(initialCapacity, maxCacheSize));
    }

    /**
     * @author Hiroshi Miura
     */
    public static class SimpleCache<K, V> extends LinkedHashMap<K, V> implements Serializable {

        private static final long serialVersionUID = 1L;

        static final float LOAD_FACTOR = 0.75f;

        private final int maximumCacheSize;

        public SimpleCache(int maximumCacheSize) {
            this(16, maximumCacheSize);
        }

        public SimpleCache(int initialCapacity, int maxCacheSize) {
            super(initialCapacity, LOAD_FACTOR, true);
            this.maximumCacheSize = maxCacheSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maximumCacheSize;
        }

    }
}
