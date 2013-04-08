/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik, Martin Fleurke
               2009 Martin Fleurke
               2013 Aaron Madlon-Kay, Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.tagvalidation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.filters2.po.PoFilter;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.tagvalidation.ErrorReport.TagError;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StaticUtils.TagInfo;

/**
 * Class for show tag validation results.
 * 
 * Class is synchronized around one check iteration, because it need to use some
 * local variables.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 * @author Aaron Madlon-Kay
 */
public class TagValidationTool implements ITagValidation, IProjectEventListener {
    private TagValidationFrame m_tagWin;
    private MainWindow mainWindow;

    // variables for one check iteration, by all entries or only by one entry
    private Pattern printfPattern = null;
    private Pattern javaMessageFormatPattern = null;
    private Pattern customTagPattern = null;
    private Pattern removePattern;
    private List<String> srcTags = new ArrayList<String>(32);
    private List<String> locTags = new ArrayList<String>(32);
    private HashSet<String> printfSourceSet = new HashSet<String>();
    private HashSet<String> printfTargetSet = new HashSet<String>();

    public TagValidationTool(final MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        CoreEvents.registerProjectChangeListener(this);
    }

    public TagValidationTool() {
        CoreEvents.registerProjectChangeListener(this);
    }

    @Override
    public synchronized void displayTagValidationErrors(List<ErrorReport> suspects, String message) {
        if (mainWindow != null) {
            showTagResultsInGui(suspects, message);
        } else {
            showTagResultsInConsole(suspects);
        }
    }

    private void showTagResultsInGui(List<ErrorReport> suspects, String message) {
        if (suspects != null && suspects.size() > 0) {
            // create a tag validation window if necessary
            if (m_tagWin == null) {
                m_tagWin = new TagValidationFrame(mainWindow);
                m_tagWin.setFont(Core.getMainWindow().getApplicationFont());
            } else {
                // close tag validation window if present
                m_tagWin.dispose();
            }

            // display list of suspect strings
            m_tagWin.setVisible(true);
            m_tagWin.setMessage(message);
            m_tagWin.displayErrorList(suspects);
        } else {
            // close tag validation window if present
            if (m_tagWin != null)
                m_tagWin.dispose();

            // show dialog saying all is OK
            JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(),
                    OStrings.getString("TF_NOTICE_OK_TAGS"), OStrings.getString("TF_NOTICE_TITLE_TAGS"),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showTagResultsInConsole(List<ErrorReport> suspects) {
        if (suspects != null && suspects.size() > 0) {
            for (ErrorReport report : suspects) {
                System.out.println(report.entryNum);
                System.out.println(report.source);
                System.out.println(report.translation);
                for (Map.Entry<TagError, List<String>> e : report.inverseReport().entrySet()) {
                    System.out.print("  ");
                    System.out.print(ErrorReport.localizedTagError(e.getKey()));
                    System.out.print(": ");
                    for (String tag : e.getValue()) {
                        System.out.print(tag);
                        System.out.print(" ");
                    }
                    System.out.println();
                }
            }
        }
    }

    public void onProjectChanged(final IProjectEventListener.PROJECT_CHANGE_TYPE eventType) {
        switch (eventType) {
        case CLOSE:
            if (m_tagWin != null)
                m_tagWin.dispose();
            break;
        }
    }

    private void initCheck() {
        printfPattern = null;
        javaMessageFormatPattern = null;
        customTagPattern = null;
        removePattern = null;

        // programming language validation: pattern to detect printf variables
        // (%s and %n$s)

        if ("true".equalsIgnoreCase(Preferences.getPreference(Preferences.CHECK_ALL_PRINTF_TAGS))) {
            printfPattern = PatternConsts.PRINTF_VARS;
        } else if ("true".equalsIgnoreCase(Preferences.getPreference(Preferences.CHECK_SIMPLE_PRINTF_TAGS))) {
            printfPattern = PatternConsts.SIMPLE_PRINTF_VARS;
        }
        if ("true".equalsIgnoreCase(Preferences.getPreference(Preferences.CHECK_JAVA_PATTERN_TAGS))) {
            javaMessageFormatPattern = PatternConsts.SIMPLE_JAVA_MESSAGEFORMAT_PATTERN_VARS;
        }
        String customRegExp = Preferences.getPreferenceDefaultAllowEmptyString(Preferences.CHECK_CUSTOM_PATTERN);
        if (!"".equalsIgnoreCase(customRegExp)) {
            customTagPattern = Pattern.compile(customRegExp);
        }
        removePattern = PatternConsts.getRemovePattern();
    }

    /**
     * Scans project and builds the list of entries which are suspected of
     * having changed (possibly invalid) tag structures.
     */
    @Override
    public synchronized List<ErrorReport> listInvalidTags() {
        initCheck();

        List<ErrorReport> suspects = new ArrayList<ErrorReport>(16);
        for (FileInfo fi : Core.getProject().getProjectFiles()) {
            for (SourceTextEntry ste : fi.entries) {
                ErrorReport err = checkEntry(fi, ste);
                if (err != null) {
                    suspects.add(err);
                }
            } // end loop over entries
        } // end loop over files
        return suspects.isEmpty() ? null : suspects;
    }

    @Override
    public synchronized boolean checkInvalidTags(SourceTextEntry ste) {
        initCheck();

        // find FileInfo
        for (FileInfo fi : Core.getProject().getProjectFiles()) {
            for (SourceTextEntry s : fi.entries) {
                if (s == ste) {
                    ErrorReport err = checkEntry(fi, ste);
                    return err == null;
                }
            }
        }

        throw new RuntimeException("Invalid SourceTextEntry storage for tag validation");
    }

    /**
     * Checks entry for valid tags.
     * 
     * @param ste
     * @return true if entry is valid, false if entry if not valid
     */
    private ErrorReport checkEntry(FileInfo fi, SourceTextEntry ste) {
        srcTags.clear();
        locTags.clear();
        printfSourceSet.clear();
        printfTargetSet.clear();

        String s = ste.getSrcText();
        TMXEntry te = Core.getProject().getTranslationInfo(ste);

        // if there's no translation, skip the string
        // bugfix for:
        // http://sourceforge.net/support/tracker.php?aid=1209839
        if (!te.isTranslated() || s.length() == 0) {
            return null;
        }
        ErrorReport report = new ErrorReport(ste, te.translation);

        // Check printf variables
        inspectPrintfVariables(printfPattern, report);

        // Extra checks for PO files:
        inspectPOWhitespace(fi.filterClass, report);

        inspectOmegaTTags(ste, customTagPattern, report);

        inspectJavaMessageFormat(javaMessageFormatPattern, report);

        inspectRemovePattern(removePattern, report);

        return report.isEmpty() ? null : report;
    }

    private static void inspectJavaMessageFormat(Pattern javaMessageFormatPattern, ErrorReport report) {

        if (javaMessageFormatPattern == null) {
            return;
        }

        List<String> srcTags = new ArrayList<String>();
        List<String> locTags = new ArrayList<String>();
        Matcher javaMessageFormatMatcher = javaMessageFormatPattern.matcher(report.source);
        while (javaMessageFormatMatcher.find()) {
            srcTags.add(javaMessageFormatMatcher.group(0));
        }
        javaMessageFormatMatcher = javaMessageFormatPattern.matcher(report.translation);
        while (javaMessageFormatMatcher.find()) {
            locTags.add(javaMessageFormatMatcher.group(0));
        }
        inspectUnorderedTags(srcTags, locTags, report);
    }

    private static void inspectPrintfVariables(Pattern printfPattern, ErrorReport report) {
        if (printfPattern == null) {
            return;
        }
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

    private static Map<String, String> extractPrintfVars(Pattern printfPattern, String translation) {
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

    private static void inspectPOWhitespace(Class filterClass, ErrorReport report) {
        if (!filterClass.equals(PoFilter.class)) {
            return;
        }
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

    /**
     * Check that translated tags are well-formed. 
     * In order to accommodate tags orphaned by segmenting,
     * unmatched tags are allowed, but only if they don't interfere with
     * non-orphaned tags.
     * @param srcTags A list of tags in the source text
     * @param locTags A list of tags in the translated text
     * @return Well-formed or not
     */
    private static void inspectOmegaTTags(SourceTextEntry ste, Pattern customTagPattern, ErrorReport report) {

        List<String> srcTags = new ArrayList<String>();
        List<String> locTags = new ArrayList<String>();
        // extract tags from src and loc string
        StaticUtils.buildTagList(report.source, ste.getProtectedParts(), srcTags);
        StaticUtils.buildTagList(report.translation, ste.getProtectedParts(), locTags);
        // custom pattern checks
        if (customTagPattern != null) {
            // extract tags from src and loc string
            Matcher customTagPatternMatcher = customTagPattern.matcher(report.source);
            while (customTagPatternMatcher.find()) {
                srcTags.add(customTagPatternMatcher.group(0));
            }
            customTagPatternMatcher = customTagPattern.matcher(report.translation);
            while (customTagPatternMatcher.find()) {
                locTags.add(customTagPatternMatcher.group(0));
            }
        }

        // Early-out if the tags are identical between source and translation
        if (srcTags.equals(locTags)) {
            return;
        }

        // If we're doing strict validation, pre-fill the report with warnings
        // about out-of-order tags.
        if (!Preferences.isPreference(Preferences.LOOSE_TAG_ORDERING)) {
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

    /**
     * Check that translated tags are well-formed. In order to accommodate tags
     * orphaned by segmenting, unmatched tags are allowed, but only if they
     * don't interfere with non-orphaned tags.
     * 
     * @param srcTags
     *            A list of tags in the source text
     * @param locTags
     *            A list of tags in the translated text
     * @return Well-formed or not
     */
    private boolean tagsAreWellFormed(List<String> srcTags, List<String> locTags) {

        // Check source tags for any missing from translation.
        for (String tag : srcTags) {
            if (!locTags.contains(tag)) {
                return false;
            }
        }

        Stack<TagInfo> tagStack = new Stack<TagInfo>();
        HashSet<String> cache = new HashSet<String>();

        TagInfo info;
        for (String tag : locTags) {
            // Make sure tag exists in source.
            if (!srcTags.contains(tag)) {
                return false;
            }
            // Check tag against cache to find duplicates.
            if (cache.contains(tag)) {
                return false;
            } else {
                cache.add(tag);
            }
            info = StaticUtils.getTagInfo(tag);
            // Build stack of tags to check well-formedness.
            switch (info.type) {
            case START:
                tagStack.push(info);
                break;
            case END:
                if (tagStack.isEmpty()) {
                    // This is an orphaned end tag. Allow this.
                    // (If it's not really orphaned but merely order-swapped,
                    // then the opening tag will be caught in the stack.)
                } else if (tagStack.peek().name.equals(info.name)) {
                    // Closing a tag normally.
                    tagStack.pop();
                } else {
                    // Closing the wrong opening tag. Disallow this.
                    return false;
                }
                break;
            case SINGLE:
                // Ignore
            }
        }

        // Check the stack to see if there are straggling open tags.
        while (!tagStack.isEmpty()) {
            // Allow stragglers only if they're orphans.
            if (srcTags.contains(StaticUtils.getPairedTag(tagStack.pop()))) {
                return false;
            }
        }

        return true;
    }

    private static void inspectRemovePattern(Pattern removePattern, ErrorReport report) {
        if (removePattern == null) {
            return;
        }
        Matcher removeMatcher = removePattern.matcher(report.translation);
        while (removeMatcher.find()) {
            report.transErrors.put(removeMatcher.group(), TagError.EXTRANEOUS);
        }
    }

    private static void inspectUnorderedTags(List<String> srcTags, List<String> locTags, ErrorReport report) {
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
     * Fix all errors indicated in a given ErrorReport.
     * 
     * @param report
     *            The report indicating the segment and errors to fix
     * @return The fixed translation string, or null if one of the errors is of
     *         type UNSPECIFIED.
     */
    public static String fixErrors(ErrorReport report) {
        // Don't try to fix unspecified errors.
        if (report.srcErrors.containsValue(TagError.UNSPECIFIED)
                || report.transErrors.containsValue(TagError.UNSPECIFIED)) {
            return null;
        }

        // Sort the map first to ensure that fixing works properly.
        Map<String, TagError> sortedErrors = new TreeMap<String, TagError>(new StaticUtils.TagComparator(
                report.source));
        sortedErrors.putAll(report.srcErrors);
        sortedErrors.putAll(report.transErrors);

        StringBuilder sb = new StringBuilder(report.translation);

        for (Map.Entry<String, TagError> e : sortedErrors.entrySet()) {
            fixTag(report.ste, e.getKey(), e.getValue(), sb, report.source);
        }

        return sb.toString();
    }

    private static void fixTag(SourceTextEntry ste, String tag, TagError error, StringBuilder translation, String source) {
        switch (error) {
        case DUPLICATE:
        case ORDER:
        case MALFORMED:
            fixMalformed(ste, translation, source, tag);
            break;
        case MISSING:
            fixMissing(ste, translation, source, tag);
            break;
        case EXTRANEOUS:
            fixExtraneous(translation, tag);
            break;
        case ORPHANED:
            // This is fixed by fixing MISSING.
            break;
        case WHITESPACE:
            fixWhitespace(translation, source);
            break;
        default:
            break;
        }
    }

    private static void fixWhitespace(StringBuilder translation, String source) {
        if (source.startsWith("\n") && translation.charAt(0) != '\n') {
            translation.insert(0, '\n');
        } else if (!source.startsWith("\n") && translation.charAt(0) == '\n') {
            translation.deleteCharAt(0);
        }
        if (source.endsWith("\n") && translation.charAt(0) != '\n') {
            translation.append('\n');
        } else if (!source.endsWith("\n") && translation.charAt(translation.length() - 1) == '\n') {
            translation.deleteCharAt(translation.length() - 1);
        }
    }

    private static void fixMalformed(SourceTextEntry ste, StringBuilder text, String source, String tag) {
        fixExtraneous(text, tag);
        fixMissing(ste, text, source, tag);
    }

    private static void fixMissing(SourceTextEntry ste, StringBuilder text, String source, String tag) {
        // Generate ordered list of source tags.
        List<String> tags = new ArrayList<String>();
        StaticUtils.buildTagList(source, ste.getProtectedParts(), tags);

        // Insert missing tag.
        int index = tags.indexOf(tag);
        String prev = index > 0 ? tags.get(index - 1) : null;
        String next = index + 1 < tags.size() ? tags.get(index + 1) : null;
        if (prev != null && text.indexOf(prev) > -1) {
            // Insert after a preceding tag.
            text.insert(text.indexOf(prev) + prev.length(), tag);
        } else if (next != null && text.indexOf(next) > -1) {
            // Insert before a proceeding tag.
            text.insert(text.indexOf(next), tag);
        } else {
            // Nothing before or after; append to end.
            text.append(tag);
        }
    }

    private static void fixExtraneous(StringBuilder text, String tag) {
        int i = 0;
        while ((i = text.indexOf(tag, i)) != -1) {
            text.delete(i, i + tag.length());
        }
    }
}
