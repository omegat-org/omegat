/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
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

import java.util.Map;
import java.util.TreeMap;

import org.omegat.filters3.Tag;

/**
 * This class handles inline tags, i.e. helps to replace all tags into
 * shortcuts. It handles bpt,ept,it tags numeration.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class InlineTagHandler {
    /** map of 'i' attributes to tag numbers */
    Map<String, Integer> pairTags = new TreeMap<String, Integer>();
    Map<String, Integer> pairedOtherTags = new TreeMap<String, Integer>();
    String currentI;
    String currentPos;
    int tagIndex;

    /**
     * Reset stored info for process new part of XML.
     */
    public void reset() {
        pairTags.clear();
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
     * Handle "ept" tag start.
     * 
     * @param attributeValues
     *            attributes to identify pairs
     */
    public void startEPT(String... attributeValues) {
        currentI = nvl(attributeValues);
    }

    /**
     * Handle "it" tag start.
     * 
     * @param posValue
     *            begin/end value
     */
    public void startIT(String posValue) {
        currentPos = posValue;
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
     * Handle "it" tag end.
     * 
     * @return shortcut index
     */
    public Integer endIT() {
        int result = tagIndex;
        tagIndex++;
        return result;
    }

    /**
     * Handle other tag end.
     * 
     * @return shortcut index
     */
    public Integer endOTHER() {
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
            pairedOtherTags.put(tagName, result);
            tagIndex++;
            return result;
        case END:
            Integer index = pairedOtherTags.get(tagName);
            if (index == null) {
                index = tagIndex;
                tagIndex++;
            }
            pairedOtherTags.remove(tagName);
            return index;
        case ALONE:
            result = tagIndex;
            tagIndex++;
            return result;
        default:
            throw new RuntimeException("Impossible tag type");
        }
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
