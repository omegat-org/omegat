/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013-2014 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.Core;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.statistics.StatisticsSettings;

/**
 * A collection of tag-related static utilities.
 * 
 * @author Aaron Madlon-Kay
 */
public class TagUtil {
    
    public static class TagOrder {
        public final int pos;
        public final String tag;
    
        public TagOrder(int pos, String tag) {
            this.pos = pos;
            this.tag = tag;
        }
    }

    /**
     * Indicates the type of a tag, e.g.:
     * <ul>
     * <li>&lt;foo> = START</li>
     * <li>&lt;/foo> = END</li>
     * <li>&lt;bar/> = SINGLE</li>
     * </ul>
     */
    public static enum TagType {
        START, END, SINGLE
    }

    /**
     * A tuple containing 
     * <ul><li>A tag's name</li>
     * <li>The tag's {@link TagType} type</li>
     * </ul>
     */
    public static class TagInfo {
        public final TagType type;
        public final String name;
        
        public TagInfo (String name, TagType type) {
            this.name = name;
            this.type = type;
        }
    }

    /**
     * Sort tags by order of their appearance in a reference string.
     */
    public static class TagComparator implements Comparator<String> {
    
        private final String source;
    
        public TagComparator(String source) {
            super();
            this.source = source;
        }
    
        @Override
        public int compare(String tag1, String tag2) {
            // Check for equality
            if (tag1.equals(tag2)) {
                return 0;
            }
            // Check to see if one tag encompases the other
            if (tag1.startsWith(tag2)) {
                return -1;
            } else if (tag2.startsWith(tag1)) {
                return 1;
            }
            // Check which tag comes first
            int index1 = source.indexOf(tag1);
            int index2 = source.indexOf(tag2);
            if (index1 == index2) {
                int len1 = tag1.length();
                int len2 = tag2.length();
                if (len1 > len2) {
                    return -1;
                } else if (len2 > len1) {
                    return 1;
                } else {
                    return tag1.compareTo(tag2);
                }
            }
            return index1 > index2 ? 1 : -1;
        }
    }

    final public static String TAG_SEPARATOR_SENTINEL = "\uE100";
        
    public static List<String> getAllTagsInSource() {
        
        List<String> result = new ArrayList<String>();
        
        // Add tags.
        SourceTextEntry ste = Core.getEditor().getCurrentEntry();
        for(ProtectedPart pp:ste.getProtectedParts()) {
            result.add(pp.getTextInSourceSegment());
        }
        return result;
    }

    public static List<String> getAllTagsMissingFromTarget() {
        List<String> result = new ArrayList<String>();
        
        String target = Core.getEditor().getCurrentTranslation();
        
        for (String tag : getAllTagsInSource()) {
            if (!target.contains(tag)) {
                result.add(tag);
            }
        }
        return result;
    }
    
    public static List<String> getGroupedMissingTagsFromTarget() {
        String sourceText = Core.getEditor().getCurrentEntry().getSrcText();
        List<String> result = new ArrayList<String>();
        
        int index = -1;
        List<String> group = new ArrayList<String>();
        List<String> tags = getAllTagsMissingFromTarget();
        for (int i = 0; i < tags.size(); i++) {
            String tag = tags.get(i);
            
            if (sourceText.startsWith(tag, index)) {
                // We are continuing an existing group.
                group.add(tag);
                index += tag.length();
            } else {
                // We are starting a new group.
                dumpGroup(group, result);
                group.clear();
                group.add(tag);
                index = sourceText.indexOf(tag, index) + tag.length();
            }
            
            // See if this tag and next tag make a pair and offer them as a set,
            // regardless of whether or not they're contiguous.
            // E.g. either an actual pair like <foo></foo> or a potential pair
            // like <foo/><foo/>.
            if (i + 1 < tags.size()) {
                String next = tags.get(i + 1);
                if (tagsArePaired(tag, next) || tagsAreStandalone(tag, next)) {
                    // Insert sentinel to allow cursor relocating.
                    result.add(tag + TAG_SEPARATOR_SENTINEL + next);
                }
            }
        }
        // Catch the last group.
        dumpGroup(group, result);
        
        return result;
    }
    
    private static void dumpGroup(List<String> groupTags, List<String> result) {
        if (groupTags.isEmpty()) {
            return;
        }
        if (groupTags.size() > 1) {
            StringBuilder sb = new StringBuilder();
            for (String t : groupTags) {
                sb.append(t);
            }
            result.add(sb.toString());
        }
        for (String t : groupTags) {
            result.add(t);
        }
    }
    
    public static boolean tagsArePaired(String tag1, String tag2) {
        TagInfo tag1info = getTagInfo(tag1);
        return tag1info.type == TagType.START && tag2.equals(getPairedTag(tag1info));
    }
    
    public static boolean tagsAreStandalone(String tag1, String tag2) {
        return getTagType(tag1) == TagType.SINGLE && getTagType(tag2) == TagType.SINGLE;
    }

    /**
     * Builds a list of format tags within the supplied string. Format tags are
     * 'protected parts' and OmegaT style tags: &lt;xx02&gt; or &lt;/yy01&gt;.
     */
    public static void buildTagList(String str, ProtectedPart[] protectedParts, List<String> tagList) {
        List<TagOrder> tags = new ArrayList<TagOrder>();
        if (protectedParts != null) {
            // Put string in temporary buffer and replace tags with spaces as we find them.
            // This ensures that we don't find identical tags multiple times unless they are
            // actually present multiple times.
            StringBuilder sb = new StringBuilder(str);
            while (true) {
                boolean foundTag = false;
                for (ProtectedPart pp : protectedParts) {
                    int pos = -1;
                    if ((pos = sb.indexOf(pp.getTextInSourceSegment(), pos + 1)) >= 0) {
                        tags.add(new TagOrder(pos, pp.getTextInSourceSegment()));
                        replaceWith(sb, pos, pos + pp.getTextInSourceSegment().length(), ' ');
                        foundTag = true;
                    }
                }
                if (!foundTag) {
                    break;
                }
            }
        }
    
        if (tags.isEmpty()) {
            return;
        }
        Collections.sort(tags, new Comparator<TagOrder>() {
            @Override
            public int compare(TagOrder o1, TagOrder o2) {
                return o1.pos - o2.pos;
            }
        });
        for (TagOrder t : tags) {
            tagList.add(t.tag);
        }
    }

    private static void replaceWith(StringBuilder sb, int start, int end, char replacement) {
        for (int i = start; i < end; i++) {
            sb.setCharAt(i, replacement);
        }
    }

    /**
     * Builds a list of all occurrences of all protected parts.
     */
    public static List<TagOrder> buildAllTagList(String str, ProtectedPart[] protectedParts) {
        List<TagOrder> tags = new ArrayList<TagOrder>();
        if (protectedParts != null) {
            for (ProtectedPart pp : protectedParts) {
                int pos = -1;
                do {
                    if ((pos = str.indexOf(pp.getTextInSourceSegment(), pos + 1)) >= 0) {
                        tags.add(new TagOrder(pos, pp.getTextInSourceSegment()));
                    }
                } while (pos >= 0);
            }
        }
    
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.sort(tags, new Comparator<TagOrder>() {
            @Override
            public int compare(TagOrder o1, TagOrder o2) {
                return o1.pos - o2.pos;
            }
        });
        return tags;
    }

    /**
     * Builds a list of format tags within the supplied string. Format tags are
     * OmegaT style tags: &lt;xx02&gt; or &lt;/yy01&gt;.
     * @return a string containing the tags
     */
    public static String buildTagListForRemove(String str) {
        String res = "";
        Pattern placeholderPattern = PatternConsts.OMEGAT_TAG;
        Matcher placeholderMatcher = placeholderPattern.matcher(str);
        while (placeholderMatcher.find()) {
            res += placeholderMatcher.group(0);
        }
        return res;
    }

    /**
     * Find the first tag in a segment
     * @param str A segment
     * @return the first tag in the segment, or null if there are no tags
     */
    public static String getFirstTag(String str) {
        Pattern placeholderPattern = PatternConsts.OMEGAT_TAG;
        Matcher placeholderMatcher = placeholderPattern.matcher(str);
        if (placeholderMatcher.find()) {
            return placeholderMatcher.group(0);
        }
        return null;
    }

    /**
     * Detect the type of a tag, e.g. one of {@link TagType}.
     * @param tag String containing full text of tag
     * @return The type of the tag
     */
    public static TagType getTagType(String tag) {
        if (tag.length() < 4 || (!tag.startsWith("<") && !tag.endsWith(">"))) {
            return TagType.SINGLE;
        }
        
        if (tag.startsWith("</")) {
            return TagType.END;
        } else if (tag.endsWith("/>")) {
            return TagType.SINGLE;
        }
    
        return TagType.START;
    }

    /**
     * Retrieve info about a tag.
     * @param tag String containing full text of tag
     * @return A {@link TagInfo} with tag's name and type
     */
    public static TagInfo getTagInfo(String tag) {
        Matcher m = PatternConsts.OMEGAT_TAG_DECOMPILE.matcher(tag);
        String name = m.find() ? m.group(2) + m.group(3) : tag;
        return new TagInfo(name, getTagType(tag));
    }

    /**
     * For a given tag, retrieve its pair e.g. &lt;/foo> for &lt;foo>.
     * @param info A {@link TagInfo} describing the tag
     * @return The tag's pair as a string, or null for self-contained tags
     */
    public static String getPairedTag(TagInfo info) {
        switch(info.type) {
        case START:
            return String.format("</%s>", info.name);
        case END:
            return String.format("<%s>", info.name);
        case SINGLE:
        default:
            return null;
        }
    }

    /**
     * Find some protected parts defined in Tag Validation Options dialog: printf variables, java
     * MessageFormat patterns, user defined cusom tags.
     * 
     * These protected parts shouldn't affect statistic but just be displayed in gray in editor and take part
     * in tag validation.
     */
    public static List<ProtectedPart> applyCustomProtectedParts(String source,
            Pattern protectedPartsPatterns, List<ProtectedPart> protectedParts) {
        List<ProtectedPart> result;
        if (protectedParts != null) {
            // Remove already define protected parts first for prevent intersection
            for (ProtectedPart pp : protectedParts) {
                source = source.replace(pp.getTextInSourceSegment(), StaticUtils.TAG_REPLACEMENT);
            }
            result = protectedParts;
        } else {
            result = new ArrayList<ProtectedPart>();
        }
    
        Matcher placeholderMatcher = protectedPartsPatterns.matcher(source);
        while (placeholderMatcher.find()) {
            ProtectedPart pp = new ProtectedPart();
            pp.setTextInSourceSegment(placeholderMatcher.group());
            pp.setDetailsFromSourceFile(placeholderMatcher.group());
            if (StatisticsSettings.isCountingCustomTags()) {
                pp.setReplacementWordsCountCalculation(placeholderMatcher.group());
            } else {
                pp.setReplacementWordsCountCalculation(StaticUtils.TAG_REPLACEMENT);
            }
            pp.setReplacementUniquenessCalculation(placeholderMatcher.group());
            pp.setReplacementMatchCalculation(placeholderMatcher.group());
            result.add(pp);
        }
        return result;
    }

    /**
     * Strips all XML tags (converts to plain text). Tags detected only by
     * pattern. Protected parts are not used.
     */
    public static String stripXmlTags(String xml) {
        return PatternConsts.OMEGAT_TAG.matcher(xml).replaceAll("");
    }

    /**
     * Reconstruct a tag from its {@link TagInfo}.
     * 
     * @param info
     *            Description of tag
     * @return Reconstructed original tag
     */
    public static String getOriginalTag(TagInfo info) {
        switch (info.type) {
        case START:
            return String.format("<%s>", info.name);
        case END:
            return String.format("</%s>", info.name);
        case SINGLE:
            return String.format("<%s/>", info.name);
        }
        return null;
    }
}
