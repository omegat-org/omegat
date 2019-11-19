/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015-2016 Hiroshi Miura, Aaron Madlon-Kay
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

package org.omegat.core.dictionaries;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.omegat.util.Language;
import org.omegat.util.StringUtil;
import org.trie4j.MapTrie;
import org.trie4j.doublearray.MapDoubleArray;
import org.trie4j.patricia.MapPatriciaTrie;

/**
 * A class that encapsulates the storage and retrieval of string-keyed data.
 * Usage:
 * <ol>
 * <li>Instantiate and insert data with {@link #add(String, Object)}
 * <li>Call {@link #done()} when done adding data (required!)
 * <li>Retrieve data with {@link #lookUp(String, boolean)}
 * </ol>
 *
 * @author Aaron Madlon-Kay
 *
 * @param <T>
 *            The type of data stored
 */
public class DictionaryData<T> {

    private final Language language;
    private MapDoubleArray<Object> data;
    private MapTrie<Object> temp;

    /**
     * @param language
     *            The dictionary's index language
     */
    public DictionaryData(Language language) {
        this.language = language;
        this.temp = new MapPatriciaTrie<>();
    }

    /**
     * Insert a key=value pair into the data store. Unicode normalization is
     * performed on the key. The value is stored both for the key and its
     * lowercase version, if the latter differs.
     *
     * @param key
     *            The key
     * @param value
     *            The value
     */
    public void add(String key, T value) {
        key = StringUtil.normalizeUnicode(key);
        doAdd(key, value);
        String lowerKey = key.toLowerCase(language.getLocale());
        if (!key.equals(lowerKey)) {
            doAdd(lowerKey, value);
        }
    }

    /**
     * Do the actual storing of the value. Most values are going to be singular,
     * but dictionaries may store multiple definitions for the same key, so in
     * that case we store the values in an array.
     *
     * @param key
     * @param value
     */
    private void doAdd(String key, T value) {
        Object stored = temp.get(key);
        if (stored == null) {
            temp.insert(key, value);
        } else {
            if (stored instanceof Object[]) {
                stored = extendArray((Object[]) stored, value);
            } else {
                stored = new Object[] { stored, value };
            }
            temp.put(key, stored);
        }
    }

    /**
     * Return the given array with the given value appended to it.
     *
     * @param array
     * @param value
     * @return
     */
    Object[] extendArray(Object[] array, Object value) {
        Object[] newArray = new Object[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[newArray.length - 1] = value;
        return newArray;
    }

    /**
     * Finalize the data store. This is <strong>required</strong> to be called
     * before any lookups can be performed.
     */
    public void done() {
        data = new MapDoubleArray<>(temp);
        temp = null;
    }

    /**
     * Look up the given word.
     *
     * @param word
     *            The word to look up
     * @return A list of stored objects matching the given word
     * @throws IllegalStateException
     *             If {@link #done()} has not yet been called
     */
    public List<Entry<String, T>> lookUp(String word) throws IllegalStateException {
        return doLookUpWithLowerCase(word, false);
    }

    /**
     * Look up the given word using predictive completion; e.g. "term" will
     * match "terminology" (and "terminal", etc.).
     *
     * @param word
     *            The word to look up
     * @return A list of stored objects matching the given word
     * @throws IllegalStateException
     *             If {@link #done()} has not yet been called
     */
    public List<Entry<String, T>> lookUpPredictive(String word) throws IllegalStateException {
        return doLookUpWithLowerCase(word, true);
    }

    private List<Entry<String, T>> doLookUpWithLowerCase(String word, boolean predictive) {
        List<Entry<String, T>> result = doLookUp(word, predictive);
        if (result.isEmpty()) {
            String lowerWord = word.toLowerCase(language.getLocale());
            result = doLookUp(lowerWord, predictive);
        }
        return result;
    }

    private List<Entry<String, T>> doLookUp(String word, boolean predictive) throws IllegalStateException {
        if (data == null) {
            throw new IllegalStateException(
                    "Object has not been finalized! You must call done() before doing any lookups.");
        }
        List<Entry<String, T>> result = new ArrayList<>();
        if (predictive) {
            data.predictiveSearch(word).forEach(w -> get(w, data.get(w), result));
        } else {
            get(word, data.get(word), result);
        }

        return result;
    }

    /**
     * Unpack the given stored object (singular, or array) into the given
     * collection.
     *
     * @param value
     * @param into
     */
    @SuppressWarnings("unchecked")
    private <U> void get(U key, Object value, Collection<Entry<U, T>> into) {
        if (value == null) {
            return;
        }
        if (value instanceof Object[]) {
            for (Object o : (Object[]) value) {
                into.add(new AbstractMap.SimpleImmutableEntry<>(key, (T) o));
            }
        } else {
            into.add(new AbstractMap.SimpleImmutableEntry<>(key, (T) value));
        }
    }

    /**
     * Get the number of stored keys. Returns <code>-1</code> if {@link #done()}
     * has not yet been called.
     *
     * @return The number of stored keys
     */
    public int size() {
        return data == null ? -1 : data.size();
    }
}
