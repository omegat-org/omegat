/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik, Aaron Madlon-Kay
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
    Map<String, String> pairTagsExternalMatch = new TreeMap<>();
    Map<String, ArrayDeque<Integer>> pairedOtherTags = new TreeMap<String, ArrayDeque<Integer>>();
    Map<String, Integer> shortcutLetters = new TreeMap<String, Integer>();
    String currentI;
    String currentX;
    String currentPos;
    int tagIndex;
    int otherTagShortcutLetter;

    /**
     * Reset stored info for process new part of XML.
     */
    public void reset() {
        pairTags.clear();
        pairTagsExternalMatch.clear();
        pairedOtherTags.clear();
        currentI = null;
        currentX = null;
        currentPos = null;
        tagIndex = 0;
    }

    /**
     * Handle "bpt" tag start for TMX. OmegaT internal tag number will be based
     * off the x attr (if provided).
     *
     * @param i
     *            TMX i attribute value
     * @param x
     *            TMX x attribute value (can be null)
     */
    public void startBPT(String i, String x) {
        if (i == null) {
            throw new RuntimeException("Wrong index in inline tag");
        }
        currentI = i;
        currentX = x;
        pairTags.put(currentI, tagIndex++);
        pairTagsExternalMatch.put(currentI, x);
    }

    /**
     * Handle "it" tag start for TMX. OmegaT internal tag number will be based
     * off the x attr (if provided).
     *
     * @param x
     *            TMX x attribute value (can be null)
     */
    public void startIT(String x) {
        currentX = x;
    }

    /**
     * Handle "ph" tag start for TMX. OmegaT internal tag number will be based
     * off the x attr (if provided).
     *
     * @param x
     *            TMX x attribute value (can be null)
     */
    public void startPH(String x) {
        currentX = x;
    }

    /**
     * Handle "bpt" tag start. Identifier will be first non-null attribute in
     * provided attributes. OmegaT internal tag number will be its index in the
     * list of tags in the segment (starting with 0).
     *
     * @param attributeValues
     *            attributes to identify pairs
     */
    public void startBPT(String... attributeValues) {
        currentI = nvl(attributeValues);
        currentX = null;
        pairTags.put(currentI, tagIndex++);
    }

    /**
     * Store shortcut letter for current 'i' value.
     *
     * @param letter
     *            letter to store
     */
    public void setTagShortcutLetter(int letter) {
        if (letter != 0) {
            shortcutLetters.put(currentI, letter);
        }
    }

    /**
     * Get stored shortcut letter for current 'i' value.
     *
     * @return
     */
    public int getTagShortcutLetter() {
        Integer c = shortcutLetters.get(currentI);
        return c != null ? c : 0;
    }

    /**
     * Store shortcut letter for current other tag.
     *
     * @param letter
     *            letter to store
     */
    public void setOtherTagShortcutLetter(int letter) {
        otherTagShortcutLetter = letter;
    }

    /**
     * Get stored shortcut letter for current other tag.
     *
     * @return
     */
    public int getOtherTagShortcutLetter() {
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
        currentX = null;
    }

    /**
     * Handle "bpt" tag end.
     *
     * @return shortcut index
     */
    public Integer endBPT() {
        String x = pairTagsExternalMatch.get(currentI);
        if (x != null) {
            return Integer.parseInt(x);
        } else {
            return pairTags.get(currentI);
        }
    }

    private Integer endExternalMatchTag() {
        if (currentX != null) {
            return Integer.parseInt(currentX);
        } else {
            return endOTHER();
        }
    }

    /**
     * Handle "IT" tag end.
     *
     * @return shortcut index
     */
    public Integer endIT() {
        return endExternalMatchTag();
    }

    /**
     * Handle "PH" tag end.
     *
     * @return shortcut index
     */
    public Integer endPH() {
        return endExternalMatchTag();
    }

    /**
     * Handle "ept" tag end.
     *
     * @return shortcut index
     */
    public Integer endEPT() {
        String x = pairTagsExternalMatch.get(currentI);
        if (x != null) {
            return Integer.parseInt(x);
        } else {
            return pairTags.get(currentI);
        }
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

    /**
     * Returns whether the current tag is externally matched (has a TMX "x"
     * attribute)
     */
    public boolean getIsExternallyMatched() {
        return (currentI != null && pairTagsExternalMatch.containsKey(currentI)) || currentX != null;
    }

    private String nvl(String... attributeValues) {
        String result = StringUtil.nvl(attributeValues);
        if (result == null) {
            throw new RuntimeException("Wrong index in inline tag");
        }
        return result;
    }
}
