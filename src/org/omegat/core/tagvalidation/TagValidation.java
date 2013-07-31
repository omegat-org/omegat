/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Aaron Madlon-Kay
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

package org.omegat.core.tagvalidation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.tagvalidation.ErrorReport.TagError;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StaticUtils.TagInfo;

/**
 * @author Aaron Madlon-Kay
 */
public class TagValidation {

    public static void inspectJavaMessageFormat(ErrorReport report) {

        Pattern pattern = PatternConsts.SIMPLE_JAVA_MESSAGEFORMAT_PATTERN_VARS;

        List<String> srcTags = new ArrayList<String>();
        List<String> locTags = new ArrayList<String>();
        Matcher javaMessageFormatMatcher = pattern.matcher(report.source);
        while (javaMessageFormatMatcher.find()) {
            srcTags.add(javaMessageFormatMatcher.group(0));
        }
        javaMessageFormatMatcher = pattern.matcher(report.translation);
        while (javaMessageFormatMatcher.find()) {
            locTags.add(javaMessageFormatMatcher.group(0));
        }
        inspectUnorderedTags(srcTags, locTags, report);
    }

    public static void inspectPrintfVariables(boolean simpleCheckOnly, ErrorReport report) {

        Pattern printfPattern = simpleCheckOnly? PatternConsts.SIMPLE_PRINTF_VARS : PatternConsts.PRINTF_VARS;

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
        Map<String, String> srcTags = extractPrintfVars(printfPattern, report.source);
        Map<String, String> locTags = extractPrintfVars(printfPattern, report.translation);

        if (!srcTags.keySet().equals(locTags.keySet())) {
            for (Map.Entry<String, String> e : srcTags.entrySet()) {
                report.srcErrors.put(e.getValue(), TagError.UNSPECIFIED);
            }
            for (Map.Entry<String, String> e : locTags.entrySet()) {
                report.transErrors.put(e.getValue(), TagError.UNSPECIFIED);
            }
        }
    }

    public static Map<String, String> extractPrintfVars(Pattern printfPattern, String translation) {
        Matcher printfMatcher = printfPattern.matcher(translation);
        Map<String, String> nameMapping = new HashMap<String, String>();
        int index = 1;
        while (printfMatcher.find()) {
            String printfVariable = printfMatcher.group(0);
            String argumentswapspecifier = printfMatcher.group(1);
            if (argumentswapspecifier != null && argumentswapspecifier.endsWith("$")) {
                String normalized = "" + argumentswapspecifier.substring(0, argumentswapspecifier.length() - 1)
                        + printfVariable.substring(printfVariable.length() - 1, printfVariable.length());
                nameMapping.put(normalized, printfVariable);

            } else {
                String normalized = "" + index
                        + printfVariable.substring(printfVariable.length() - 1, printfVariable.length());
                nameMapping.put(normalized, printfVariable);
                index++;
            }
        }
        return nameMapping;
    }

    public static void inspectPOWhitespace(ErrorReport report) {
        // check PO line start:
        if (report.source.startsWith("\n") != report.translation.startsWith("\n")) {
            report.transErrors.put("^\\n", TagError.WHITESPACE);
            report.srcErrors.put("^\\n", TagError.WHITESPACE);
        }
        // check PO line ending:
        if (report.source.endsWith("\n") != report.translation.endsWith("\n")) {
            report.transErrors.put("\\n$", TagError.WHITESPACE);
            report.srcErrors.put("\\n$", TagError.WHITESPACE);
        }
    }

    public static void inspectOmegaTTags(SourceTextEntry ste, ErrorReport report) {
        List<String> srcTags = new ArrayList<String>();
        List<String> locTags = new ArrayList<String>();
        // extract tags from src and loc string
        StaticUtils.buildTagList(report.source, ste.getProtectedParts(), srcTags);
        StaticUtils.buildTagList(report.translation, ste.getProtectedParts(), locTags);

        inspectOrderedTags(srcTags, locTags, Preferences.isPreference(Preferences.LOOSE_TAG_ORDERING), report);
    }

    public static void inspectRemovePattern(ErrorReport report) {
        Pattern removePattern = PatternConsts.getRemovePattern();
        if (removePattern == null) {
            return;
        }
        Matcher removeMatcher = removePattern.matcher(report.translation);
        while (removeMatcher.find()) {
            report.transErrors.put(removeMatcher.group(), TagError.EXTRANEOUS);
        }
    }
    
    public static void inspectCustomTags(ErrorReport report) {
        Pattern customTagPattern = PatternConsts.getCustomTagPattern();
        if (customTagPattern == null) {
            return;
        }
        List<String> srcTags = new ArrayList<String>();
        List<String> locTags = new ArrayList<String>();
        
        Matcher customTagPatternMatcher = customTagPattern.matcher(report.source);
        while (customTagPatternMatcher.find()) {
            srcTags.add(customTagPatternMatcher.group(0));
        }
        customTagPatternMatcher = customTagPattern.matcher(report.translation);
        while (customTagPatternMatcher.find()) {
            locTags.add(customTagPatternMatcher.group(0));
        }
        
        inspectUnorderedTags(srcTags, locTags, report);
    }
    
    protected static void inspectUnorderedTags(List<String> srcTags, List<String> locTags, ErrorReport report) {
        for (String tag : srcTags) {
            if (!locTags.contains(tag)) {
                report.srcErrors.put(tag, TagError.MISSING);
            }
        }
        for (String tag : locTags) {
            if (!srcTags.contains(tag)) {
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
    protected static void inspectOrderedTags(List<String> srcTags, List<String> locTags,
            boolean looseOrdering, ErrorReport report) {
     // Early-out if the tags are identical between source and translation
        if (srcTags.equals(locTags)) {
            return;
        }

        // If we're doing strict validation, pre-fill the report with warnings
        // about out-of-order tags.
        if (!looseOrdering) {
            List<String> commonTagsSrc = new ArrayList<String>(srcTags);
            commonTagsSrc.retainAll(locTags);
            List<String> commonTagsLoc = new ArrayList<String>(locTags);
            commonTagsLoc.retainAll(srcTags);

            for (int i = 0; i < commonTagsSrc.size(); i++) {
                String tag = commonTagsLoc.get(i);
                if (!tag.equals(commonTagsSrc.get(i))) {
                    report.transErrors.put(tag, TagError.ORDER);
                    commonTagsSrc.remove(tag);
                    commonTagsLoc.remove(i);
                    i--;
                }
            }
        }

        // Check source tags for any missing from translation.
        for (String tag : srcTags) {
            if (!locTags.contains(tag)) {
                report.srcErrors.put(tag, TagError.MISSING);
            }
        }

        // Check translation tags.
        Stack<TagInfo> tagStack = new Stack<TagInfo>();
        HashSet<String> cache = new HashSet<String>();
        for (String tag : locTags) {
            // Make sure tag exists in source.
            if (!srcTags.contains(tag)) {
                report.transErrors.put(tag, TagError.EXTRANEOUS);
                continue;
            }
            // Check tag against cache to find duplicates.
            if (cache.contains(tag)) {
                report.transErrors.put(tag, TagError.DUPLICATE);
                continue;
            } else {
                cache.add(tag);
            }

            // Build stack of tags to check well-formedness.
            TagInfo info = StaticUtils.getTagInfo(tag);
            switch (info.type) {
            case START:
                tagStack.push(info);
                break;
            case END:
                if (!tagStack.isEmpty() && tagStack.peek().name.equals(info.name)) {
                    // Closing a tag normally.
                    tagStack.pop();
                } else {
                    while (!tagStack.isEmpty()) {
                        // Closing the wrong opening tag.
                        // Rewind stack until we find its pair. Report everything along
                        // the way as malformed.
                        TagInfo last = tagStack.pop();
                        report.transErrors.put(StaticUtils.getOriginalTag(last),
                                TagError.MALFORMED);
                        if (last.name.equals(info.name)) break;
                    }
                    // If the stack was empty to begin with or we emptied it above,
                    // report the tag, but only if it's not a valid orphan.
                    if (tagStack.isEmpty()) {
                        String pair = StaticUtils.getPairedTag(info);
                        if (srcTags.contains(pair)) {
                            report.transErrors.put(tag,
                                    locTags.contains(pair) ? TagError.MALFORMED : TagError.ORPHANED);
                        }
                    }
                }
                break;
            case SINGLE:
                // Ignore
            }
        }

        // Check the stack to see if there are straggling open tags.
        while (!tagStack.isEmpty()) {
            // Allow stragglers only if they're orphans.
            TagInfo info = tagStack.pop();
            String pair = StaticUtils.getPairedTag(info);
            if (srcTags.contains(pair)) {
                report.transErrors.put(StaticUtils.getOriginalTag(info),
                        locTags.contains(pair) ? TagError.MALFORMED : TagError.ORPHANED);
            }
        }
    }
}
