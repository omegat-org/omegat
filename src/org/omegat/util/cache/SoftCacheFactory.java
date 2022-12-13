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

import java.util.Map;
import java.util.concurrent.ForkJoinPool;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

/**
 * @author Hiroshi Miura
 */
public class SoftCacheFactory extends CacheFactory {

    private static CacheFactory instance = null;

    private SoftCacheFactory() {
        super();
    }

    public static CacheFactory getInstance() {
        if (instance == null) {
            instance = new SoftCacheFactory();
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
        Caffeine<K, V> caffeine = Caffeine.newBuilder().initialCapacity(initialCapacity)
                .maximumSize(maxCacheSize).removalListener(new Listener<>());
        caffeine.softValues();
        caffeine.executor(task -> ForkJoinPool.commonPool().execute(task));
        return caffeine.build().asMap();
    }

    public static class Listener<K, V> implements RemovalListener<K, V> {
        @Override
        public void onRemoval(K key, V value, RemovalCause cause) {
        }
    }
}
