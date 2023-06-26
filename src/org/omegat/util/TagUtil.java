/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013-2014 Aaron Madlon-Kay
               2017 Didier Briel
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.omegat.core.Core;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.statistics.StatisticsSettings;

/**
 * A collection of tag-related static utilities.
 *
 * @author Aaron Madlon-Kay
 */
public final class TagUtil {

    private TagUtil() {
    }

    private static final Comparator<Tag> TAG_COMPARATOR = (o1, o2) -> o1.pos - o2.pos;

    public static final class Tag {
        public final int pos;
        public final String tag;

        public Tag(int pos, String tag) {
            this.pos = pos;
            this.tag = tag;
        }

        public TagType getType() {
            Matcher m = PatternConsts.OMEGAT_TAG_DECOMPILE.matcher(tag);
            if (!m.find()) {
                return TagType.SINGLE;
            }

            boolean hasFrontSlash = "/".equals(m.group(1));
            boolean hasBackSlash = "/".equals(m.group(4));

            if (hasFrontSlash && !hasBackSlash) {
                return TagType.END;
            }

            if (!hasFrontSlash && !hasBackSlash) {
                return TagType.START;
            }

            return TagType.SINGLE;
        }

        public String getName() {
            Matcher m = PatternConsts.OMEGAT_TAG_DECOMPILE.matcher(tag);
            if (!m.find()) {
                return tag;
            }

            boolean hasFrontSlash = "/".equals(m.group(1));
            boolean hasBackSlash = "/".equals(m.group(4));

            if (hasFrontSlash && hasBackSlash) {
                return tag;
            }

            return m.group(2) + m.group(3);
        }

        public String getPairedTag() {
            switch (getType()) {
            case START:
                return "</" + getName() + ">";
            case END:
                return "<" + getName() + ">";
            case SINGLE:
            default:
                return null;
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + pos;
            result = prime * result + ((tag == null) ? 0 : tag.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Tag other = (Tag) obj;
            if (pos != other.pos) {
                return false;
            }
            if (tag == null) {
                if (other.tag != null) {
                    return false;
                }
            } else if (!tag.equals(other.tag)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return tag + "@" + pos;
        }
    }

    /**
     * Indicates the type of a tag, e.g.:
     * <ul>
     * <li>&lt;foo&gt; = START</li>
     * <li>&lt;/foo&gt; = END</li>
     * <li>&lt;bar/&gt; = SINGLE</li>
     * </ul>
     */
    public enum TagType {
        START, END, SINGLE
    }

    /**
     * A tuple containing
     * <ul>
     * <li>A tag's name</li>
     * <li>The tag's {@link TagType} type</li>
     * </ul>
     */
    public static class TagInfo {
        public final TagType type;
        public final String name;

        public TagInfo(String name, TagType type) {
            this.name = name;
            this.type = type;
        }
    }

    public static final String TAG_SEPARATOR_SENTINEL = "\uE100";
    public static final char TEXT_REPLACEMENT = '\uE100';

    public static List<Tag> getAllTagsInSource() {
        SourceTextEntry ste = Core.getEditor().getCurrentEntry();
        return buildTagList(ste.getSrcText(), ste.getProtectedParts());
    }

    public static List<Tag> getAllTagsMissingFromTarget() {
        List<Tag> result = new ArrayList<Tag>();

        StringBuilder target = new StringBuilder(Core.getEditor().getCurrentTranslation());

        for (Tag tag : getAllTagsInSource()) {
            int pos = -1;
            if ((pos = target.indexOf(tag.tag)) != -1) {
                replaceWith(target, pos, pos + tag.tag.length(), TEXT_REPLACEMENT);
            } else {
                result.add(tag);
            }
        }
        return result;
    }

    public static List<String> getGroupedMissingTagsFromTarget() {
        List<String> result = new ArrayList<String>();

        List<Tag> tags = getAllTagsMissingFromTarget();
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);

            // Compile the longest possible list of contiguous tags and offer as
            // a group.
            List<String> group = getGroupAt(tags, i).stream().map(t -> t.tag).collect(Collectors.toList());
            if (group.size() > 1) {
                result.add(String.join("", group));
            }

            // See if this tag and next tag make a pair and offer them as a set,
            // regardless of whether or not they're contiguous.
            // E.g. either an actual pair like <foo></foo> or a potential pair
            // like <foo/><foo/>.
            if (i + 1 < tags.size()) {
                Tag next = tags.get(i + 1);
                String pair = tag.getPairedTag();
                if (next.tag.equals(pair)
                        || (tag.getType() == TagType.SINGLE && next.getType() == TagType.SINGLE)) {
                    // Insert sentinel to allow cursor relocating.
                    result.add(tag.tag + TAG_SEPARATOR_SENTINEL + next.tag);
                }
            }

            result.addAll(group);
        }

        return result.stream().distinct().collect(Collectors.toList());
    }

    private static List<Tag> getGroupAt(List<Tag> tags, int index) {
        Tag tag = tags.get(index);
        if (index > 0) {
            Tag prev = tags.get(index - 1);
            if (prev.pos + prev.tag.length() == tag.pos) {
                // This tag is in the middle of a group; return just this tag.
                return Arrays.asList(tag);
            }
        }
        List<Tag> group = new ArrayList<>();
        group.add(tag);
        for (int j = index + 1; j < tags.size(); j++) {
            Tag prev = tags.get(j - 1);
            Tag next = tags.get(j);
            if (prev.pos + prev.tag.length() != next.pos) {
                break;
            }
            group.add(next);
        }
        return group;
    }

    /**
     * Builds a list of format tags within the supplied string. Format tags are
     * 'protected parts' and OmegaT style tags: &lt;xx02&gt; or &lt;/yy01&gt;.
     */
    public static List<Tag> buildTagList(String str, ProtectedPart[] protectedParts) {
        if (protectedParts == null || protectedParts.length == 0) {
            return Collections.emptyList();
        }

        List<Tag> tags = new ArrayList<Tag>();
        // Put string in temporary buffer and replace tags with spaces as we
        // find them.
        // This ensures that we don't find identical tags multiple times unless
        // they are
        // actually present multiple times.
        StringBuilder sb = new StringBuilder(str);
        while (true) {
            boolean loopAgain = false;
            for (ProtectedPart pp : protectedParts) {
                int pos = -1;
                if ((pos = sb.indexOf(pp.getTextInSourceSegment())) != -1) {
                    tags.add(new Tag(pos, pp.getTextInSourceSegment()));
                    replaceWith(sb, pos, pos + pp.getTextInSourceSegment().length(), TEXT_REPLACEMENT);
                    loopAgain = true;
                }
            }
            if (!loopAgain) {
                break;
            }
        }

        Collections.sort(tags, TAG_COMPARATOR);
        return tags;
    }

    /*
     * Builds a list of format tags that are not in the source segment, and
     * hence not in protected parts.
     *
     */
    public static void addExtraTags(List<Tag> resultList, List<Tag> srcTags, String str) {

        StringBuilder sb = new StringBuilder(str);

        Pattern placeholderPattern = PatternConsts.OMEGAT_TAG;
        Matcher placeholderMatcher = placeholderPattern.matcher(str);
        while (placeholderMatcher.find()) {
            if (!containsTag(srcTags, placeholderMatcher.group(0))) {
                int pos;
                if ((pos = sb.indexOf(placeholderMatcher.group(0))) != -1) {
                    resultList.add(new Tag(pos, placeholderMatcher.group(0)));
                    replaceWith(sb, pos, pos + placeholderMatcher.group(0).length(), TEXT_REPLACEMENT);
                }
            }
        }

        Collections.sort(resultList, TAG_COMPARATOR);
    }

    /**
     * Check whether a tag belongs to a list of tags
     * 
     * @param tags
     * @param tag
     * @return true or false
     */
    public static boolean containsTag(List<Tag> tags, String tag) {
        if (tag == null) {
            return false;
        }
        for (Tag t : tags) {
            if (t.tag.equals(tag)) {
                return true;
            }
        }
        return false;
    }

    private static void replaceWith(StringBuilder sb, int start, int end, char replacement) {
        for (int i = start; i < end; i++) {
            sb.setCharAt(i, replacement);
        }
    }

    /**
     * Builds a list of format tags within the supplied string. Format tags are
     * OmegaT style tags: &lt;xx02&gt; or &lt;/yy01&gt;.
     * 
     * @return a string containing the tags
     */
    public static String buildTagListForRemove(String str) {
        StringBuilder res = new StringBuilder();
        Pattern placeholderPattern = PatternConsts.OMEGAT_TAG;
        Matcher placeholderMatcher = placeholderPattern.matcher(str);
        while (placeholderMatcher.find()) {
            res.append(placeholderMatcher.group(0));
        }
        return res.toString();
    }

    /**
     * Find the first tag in a segment
     * 
     * @param str
     *            A segment
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
     * Find some protected parts according to the given regular expression. E.g.
     * printf variables, java MessageFormat patterns, user defined custom tags.
     *
     * These protected parts shouldn't affect statistic but just be displayed in
     * gray in editor and take part in tag validation.
     */
    public static List<ProtectedPart> applyCustomProtectedParts(String source, Pattern protectedPartsPatterns,
            List<ProtectedPart> protectedParts) {
        List<ProtectedPart> result;
        if (protectedParts != null) {
            // Remove already defined protected parts first to prevent
            // intersection
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
}
