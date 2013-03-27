/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik, Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.util;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.TreeMap;

import org.omegat.filters3.Tag;

/**
 * This class handles inline tags, i.e. helps to replace all tags into
 * shortcuts. It handles bpt,ept,it tags numeration.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public class InlineTagHandler {
    /** map of 'i' attributes to tag numbers */
    Map<String, Integer> pairTags = new TreeMap<String, Integer>();
    Map<String, ArrayDeque<Integer>> pairedOtherTags = new TreeMap<String, ArrayDeque<Integer>>();
    Map<String, Character> shortcutLetters = new TreeMap<String, Character>();
    String currentI;
    String currentPos;
    int tagIndex;
    char otherTagShortcutLetter;

    /**
     * Reset stored info for process new part of XML.
     */
    public void reset() {
        pairTags.clear();
        pairedOtherTags.clear();
        currentI = null;
        currentPos = null;
        tagIndex = 0;
    }

    /**
     * Handle "bpt" tag start.
     * 
     * @param attributeValues
     *            attributes to identify pairs
     */
    public void startBPT(String... attributeValues) {
        currentI = nvl(attributeValues);
        pairTags.put(currentI, tagIndex++);
    }

    /**
     * Store shortcut letter for current 'i' value.
     * 
     * @param letter
     *            letter to store
     */
    public void setTagShortcutLetter(char letter) {
        if (letter != 0) {
            shortcutLetters.put(currentI, letter);
        }
    }

    /**
     * Get stored shortcut letter for current 'i' value.
     * 
     * @return
     */
    public char getTagShortcutLetter() {
        Character c = shortcutLetters.get(currentI);
        return c != null ? c.charValue() : 0;
    }

    /**
     * Store shortcut letter for current other tag.
     * 
     * @param letter
     *            letter to store
     */
    public void setOtherTagShortcutLetter(char letter) {
        otherTagShortcutLetter = letter;
    }

    /**
     * Get stored shortcut letter for current other tag.
     * 
     * @return
     */
    public char getOtherTagShortcutLetter() {
        return otherTagShortcutLetter;
    }

    /**
     * Handle "ept" tag start.
     * 
     * @param attributeValues
     *            attributes to identify pairs
     */
    public void startEPT(String... attributeValues) {
        currentI = nvl(attributeValues);
    }

    /**
     * Handle other tag start.
     */
    public void startOTHER() {
        currentI = null;
    }

    /**
     * Handle "bpt" tag end.
     * 
     * @return shortcut index
     */
    public Integer endBPT() {
        return pairTags.get(currentI);
    }

    /**
     * Handle "ept" tag end.
     * 
     * @return shortcut index
     */
    public Integer endEPT() {
        return pairTags.get(currentI);
    }

    /**
     * Handle other tag end.
     * 
     * @return shortcut index
     */
    public int endOTHER() {
        int result = tagIndex;
        tagIndex++;
        return result;
    }

    /**
     * Handle paired tag end.
     * 
     * @return shortcut index
     */
    public int paired(String tagName, Tag.Type tagType) {
        int result;
        switch (tagType) {
        case BEGIN:
            result = tagIndex;
            cacheTagIndex(tagName, result);
            tagIndex++;
            return result;
        case END:
            Integer index = getCachedTagIndex(tagName);
            if (index == null) {
                index = tagIndex;
                tagIndex++;
            }
            return index;
        case ALONE:
            result = tagIndex;
            tagIndex++;
            return result;
        default:
            throw new RuntimeException("Impossible tag type");
        }
    }

    private void cacheTagIndex(String tagName, int result) {
        ArrayDeque<Integer> cache = pairedOtherTags.get(tagName);
        if (cache == null) {
            cache = new ArrayDeque<Integer>();
            pairedOtherTags.put(tagName, cache);
        }
        cache.addFirst(result);
    }

    private Integer getCachedTagIndex(String tagName) {
        ArrayDeque<Integer> cache = pairedOtherTags.get(tagName);
        if (cache == null) {
            return null;
        }
        return cache.pollFirst();
    }

    /**
     * Remember current begin/end mark of "it" tag.
     */
    public void setCurrentPos(String currentPos) {
        this.currentPos = currentPos;
    }

    /**
     * Returns current begin/end mark of "it" tag.
     */
    public String getCurrentPos() {
        return currentPos;
    }

    private String nvl(String... attributeValues) {
        String result = StringUtil.nvl(attributeValues);
        if (result == null) {
            throw new RuntimeException("Wrong index in inline tag");
        }
        return result;
    }
}
