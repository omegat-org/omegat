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

package org.omegat.util.cache.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import org.omegat.util.cache.LRUCache;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

/**
 * @author Hiroshi Miura
 */
public class CaffeineLRUCache<K, V> implements LRUCache<K, V>, RemovalListener<K, V> {

    private final Cache<K, V> cache;
    private final Map<K, V> map;
    private final Consumer<V> evict;
    private final int maximumCacheSize;

    /**
     * LRU cache implementation that use Caffeine cache library.
     * <p>
     * please use LRUCacheFactory instead of instantiate directly.
     * </p>
     * 
     * @param maxCacheSize
     *            the maximum cache size.
     */
    public CaffeineLRUCache(int maxCacheSize) {
        this(16, maxCacheSize);
    }

    public CaffeineLRUCache(int initialCapacity, int maxCacheSize) {
        this(initialCapacity, maxCacheSize, false, false, false);
    }

    public CaffeineLRUCache(int initalCapacity, int maxCacheSize, boolean stopOnEviction, boolean soft,
            boolean weak) {
        this(initalCapacity, maxCacheSize,
                stopOnEviction ? CaffeineLRUCache::doStop : CaffeineLRUCache::doNothing, soft, weak);
    }

    public CaffeineLRUCache(int initialCapacity, int maxCacheSize, Consumer<V> evict, boolean soft,
            boolean weak) {
        Caffeine<K, V> caffeine = Caffeine.newBuilder().initialCapacity(initialCapacity)
                .maximumSize(maxCacheSize).removalListener(this);
        if (soft) {
            caffeine.softValues();
        }
        if (weak) {
            caffeine.weakKeys();
            caffeine.weakValues();
        }
        caffeine.executor(task -> ForkJoinPool.commonPool().execute(task));
        this.cache = caffeine.build();
        this.map = cache.asMap();
        this.evict = Objects.requireNonNull(evict);
        this.maximumCacheSize = maxCacheSize;
    }

    @Override
    public void cleanUp() {
        cache.cleanUp();
    }

    @Override
    public int getMaxCacheSize() {
        return maximumCacheSize;
    }

    @Override
    public int size() {
        return (int) cache.estimatedSize();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return map.containsValue(o);
    }

    @Override
    public V get(Object o) {
        return map.get(o);
    }

    @Override
    public V put(K k, V v) {
        return map.put(k, v);
    }

    @Override
    public V remove(Object o) {
        return map.remove(o);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        this.cache.putAll(map);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public void onRemoval(K key, V value, RemovalCause cause) {
        if (cause.wasEvicted()) {
            evict.accept(value);
        }
    }

    static <V> void doNothing(V value) {
    }

    static <V> void doStop(V value) {
    }
}
