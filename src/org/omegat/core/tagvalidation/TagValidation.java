/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Aaron Madlon-Kay
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

package org.omegat.core.tagvalidation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.tagvalidation.ErrorReport.TagError;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
import org.omegat.util.TagUtil;
import org.omegat.util.TagUtil.Tag;

/**
 * @author Aaron Madlon-Kay
 */
public final class TagValidation {

    private TagValidation() {
    }

    public static void inspectJavaMessageFormat(ErrorReport report) {

        Pattern pattern = PatternConsts.SIMPLE_JAVA_MESSAGEFORMAT_PATTERN_VARS;

        List<Tag> srcTags = new ArrayList<>();
        List<Tag> locTags = new ArrayList<>();
        Matcher javaMessageFormatMatcher = pattern.matcher(report.source);
        while (javaMessageFormatMatcher.find()) {
            srcTags.add(new Tag(javaMessageFormatMatcher.start(), javaMessageFormatMatcher.group(0)));
        }
        javaMessageFormatMatcher = pattern.matcher(report.translation);
        while (javaMessageFormatMatcher.find()) {
            locTags.add(new Tag(javaMessageFormatMatcher.start(), javaMessageFormatMatcher.group(0)));
        }
        inspectUnorderedTags(srcTags, locTags, report);
    }

    public static void inspectPrintfVariables(boolean simpleCheckOnly, ErrorReport report) {

        Pattern printfPattern = simpleCheckOnly ? PatternConsts.SIMPLE_PRINTF_VARS
                : PatternConsts.PRINTF_VARS;

        // printf variables should be equal in number,
        // but order can change
        // (and with that also notation: e.g. from '%s' to '%1$s')
        // We check this by adding the string "index+type specifier"
        // of every found variable to a set.
        // (Actually a map, so we can keep track of the original
        // variable for display purposes.)
        // If the sets (map keys) of the source and target are not equal, then
        // there is a problem: either missing or extra variables,
        // or the type specifier has changed for the variable at the
        // given index.
        Map<String, Tag> srcTags = extractPrintfVars(printfPattern, report.source);
        Map<String, Tag> locTags = extractPrintfVars(printfPattern, report.translation);

        if (!srcTags.keySet().equals(locTags.keySet())) {
            for (Map.Entry<String, Tag> e : srcTags.entrySet()) {
                report.srcErrors.put(e.getValue(), TagError.UNSPECIFIED);
            }
            for (Map.Entry<String, Tag> e : locTags.entrySet()) {
                report.transErrors.put(e.getValue(), TagError.UNSPECIFIED);
            }
        }
    }

    public static Map<String, Tag> extractPrintfVars(Pattern printfPattern, String translation) {
        Matcher printfMatcher = printfPattern.matcher(translation);
        Map<String, Tag> nameMapping = new HashMap<>();
        int index = 1;
        while (printfMatcher.find()) {
            String printfVariable = printfMatcher.group(0);
            String argumentswapspecifier = printfMatcher.group(1);
            if (argumentswapspecifier != null && argumentswapspecifier.endsWith("$")) {
                String normalized = "" + argumentswapspecifier.substring(0, argumentswapspecifier.length() - 1)
                        + printfVariable.substring(printfVariable.length() - 1);
                nameMapping.put(normalized, new Tag(printfMatcher.start(), printfVariable));

            } else {
                String normalized = "" + index
                        + printfVariable.substring(printfVariable.length() - 1);
                nameMapping.put(normalized, new Tag(printfMatcher.start(), printfVariable));
                index++;
            }
        }
        return nameMapping;
    }

    public static void inspectPOWhitespace(ErrorReport report) {
        // check PO line start:
        boolean srcStartsWith = report.source.startsWith("\n");
        boolean trgStartsWith = report.translation.startsWith("\n");
        if (srcStartsWith && !trgStartsWith) {
            report.srcErrors.put(new Tag(0, "\n"), TagError.WHITESPACE);
        }
        if (!srcStartsWith && trgStartsWith) {
            report.transErrors.put(new Tag(0, "\n"), TagError.WHITESPACE);
        }
        // check PO line ending:
        boolean srcEndsWith = report.source.endsWith("\n");
        boolean trgEndsWith = report.translation.endsWith("\n");
        if (srcEndsWith && !trgEndsWith) {
            report.srcErrors.put(new Tag(report.source.length() - 1, "\n"), TagError.WHITESPACE);
        }
        if (!srcEndsWith && trgEndsWith) {
            report.transErrors.put(new Tag(report.translation.length() - 1, "\n"), TagError.WHITESPACE);
        }
    }

    public static void inspectOmegaTTags(SourceTextEntry ste, ErrorReport report) {
        // extract tags from src and loc string
        List<Tag> srcTags = TagUtil.buildTagList(report.source, ste.getProtectedParts());
        List<Tag> locTags = new ArrayList<>(TagUtil.buildTagList(report.translation, ste.getProtectedParts()));
        // Add extra tags in target that are not in protected parts
        TagUtil.addExtraTags(locTags, srcTags, report.translation);

        inspectOrderedTags(srcTags, locTags, Preferences.isPreference(Preferences.LOOSE_TAG_ORDERING), report);
    }

    public static void inspectRemovePattern(ErrorReport report) {
        Pattern removePattern = PatternConsts.getRemovePattern();
        if (removePattern == null) {
            return;
        }
        Matcher removeMatcher = removePattern.matcher(report.translation);
        while (removeMatcher.find()) {
            report.transErrors.put(new Tag(removeMatcher.start(), removeMatcher.group()), TagError.EXTRANEOUS);
        }
    }

    protected static void inspectUnorderedTags(List<Tag> srcTags, List<Tag> locTags, ErrorReport report) {
        for (Tag tag : srcTags) {
            if (!TagUtil.containsTag(locTags, tag.tag)) {
                report.srcErrors.put(tag, TagError.MISSING);
            }
        }
        for (Tag tag : locTags) {
            if (!TagUtil.containsTag(srcTags, tag.tag)) {
                report.transErrors.put(tag, TagError.EXTRANEOUS);
            }
        }
    }

    /**
     * Check that translated tags are well-formed.
     * In order to accommodate tags orphaned by segmenting,
     * unmatched tags are allowed, but only if they don't interfere with
     * non-orphaned tags.
     * @param srcTags A list of tags in the source text
     * @param locTags A list of tags in the translated text
     * @param report The report to append errors to
     */
    protected static void inspectOrderedTags(List<Tag> srcTags, List<Tag> locTags,
            boolean looseOrdering, ErrorReport report) {

        // If we're doing strict validation, pre-fill the report with warnings
        // about out-of-order tags.
        if (!looseOrdering) {
            List<Tag> commonTagsSrc = getCommonTags(srcTags, locTags);
            List<Tag> commonTagsLoc = getCommonTags(locTags, srcTags);

            for (int i = 0; i < commonTagsSrc.size(); i++) {
                Tag tag = commonTagsLoc.get(i);
                if (!tag.tag.equals(commonTagsSrc.get(i).tag)) {
                    report.transErrors.put(tag, TagError.ORDER);
                    commonTagsSrc.remove(i);
                    commonTagsLoc.remove(i);
                    i--;
                }
            }
        }

        // Check translation tags.

        List<Tag> expectedTags = new ArrayList<>(srcTags);
        Stack<Tag> tagStack = new Stack<>();
        for (Tag tag : locTags) {
            // Make sure tag exists in source.
            if (!TagUtil.containsTag(srcTags, tag.tag)) {
                report.transErrors.put(tag, TagError.EXTRANEOUS);
                continue;
            }
            // Reduce count. If we're below zero, there's extra in the translation.
            Tag expected = removeTag(expectedTags, tag.tag);
            if (expected == null) {
                report.transErrors.put(tag, TagError.DUPLICATE);
                continue;
            }

            // Build stack of tags to check well-formedness.
            switch (tag.getType()) {
            case START:
                String endTag =  tag.getPairedTag();
                if (TagUtil.containsTag(srcTags, endTag)) {
                    tagStack.push(tag);
                }
                // else:
                //source text doesn't have an end-tag for this tag, so probably the type is 'SINGLE' and not START.
                // E.g. html2 filter produces <i0> for <input> instead of <i0/> and <br0> instead of <br0/>
                //Ignore tag.
                break;
            case END:
                if (!tagStack.isEmpty() && tagStack.peek().getName().equals(tag.getName())) {
                    // Closing a tag normally.
                    tagStack.pop();
                } else {
                    while (!tagStack.isEmpty()) {
                        // Closing the wrong opening tag.
                        // Rewind stack until we find its pair. Report everything along
                        // the way as malformed.
                        Tag last = tagStack.pop();
                        report.transErrors.put(last, TagError.MALFORMED);
                        if (last.getName().equals(tag.getName())) {
                            break;
                        }
                    }
                    // If the stack was empty to begin with or we emptied it above,
                    // report the tag, but only if it's not a valid orphan.
                    if (tagStack.isEmpty()) {
                        String pair = tag.getPairedTag();
                        if (TagUtil.containsTag(srcTags, pair)) {
                            report.transErrors.put(tag,
                                    TagUtil.containsTag(locTags, pair) ? TagError.MALFORMED : TagError.ORPHANED);
                        }
                    }
                }
                break;
            case SINGLE:
                // Ignore
                break;
            default:
                throw new IllegalArgumentException();
            }
        }

        // Check expected tags for anything left.
        for (Tag tag : expectedTags) {
            report.srcErrors.put(tag, TagError.MISSING);
        }

        // Check the stack to see if there are straggling open tags.
        while (!tagStack.isEmpty()) {
            // Allow stragglers only if they're orphans.
            Tag tag = tagStack.pop();
            String pair = tag.getPairedTag();
            if (TagUtil.containsTag(srcTags, pair)) {
                report.transErrors.put(tag,
                        TagUtil.containsTag(locTags, pair) ? TagError.MALFORMED : TagError.ORPHANED);
            }
        }
    }

    private static List<Tag> getCommonTags(List<Tag> orig, List<Tag> compare) {
        List<Tag> result = new ArrayList<>();
        List<Tag> uninspected = new ArrayList<>(compare);
        for (Tag oTag : orig) {
            for (Tag cTag : uninspected) {
                if (oTag.tag.equals(cTag.tag)) {
                    result.add(oTag);
                    uninspected.remove(cTag);
                    break;
                }
            }
        }
        return result;
    }

    private static Tag removeTag(List<Tag> tags, String tag) {
        for (int i = 0; i < tags.size(); i++) {
            Tag t = tags.get(i);
            if (t.tag.equals(tag)) {
                tags.remove(i);
                return t;
            }
        }
        return null;
    }
}
