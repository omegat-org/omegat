/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik, Martin Fleurke
               2009 Martin Fleurke
               2013 Aaron Madlon-Kay
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

package org.omegat.gui.tagvalidation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
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
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StaticUtils.TagInfo;

/**
 * Class for show tag validation results.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 * @author Aaron Madlon-Kay
 */
public class TagValidationTool implements ITagValidation, IProjectEventListener {
    private TagValidationFrame m_tagWin;
    private MainWindow mainWindow;

    public TagValidationTool(final MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        CoreEvents.registerProjectChangeListener(this);
    }

    public TagValidationTool() {
        CoreEvents.registerProjectChangeListener(this);
    }

    @Override
    public void displayTagValidationErrors(List<SourceTextEntry> suspects) {
        if (mainWindow != null) {
            showTagResultsInGui(suspects);
        } else {
            showTagResultsInConsole(suspects);
        }
    }

    private void showTagResultsInGui(List<SourceTextEntry> suspects) {
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
            m_tagWin.displayStringList(suspects);
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
    
    private void showTagResultsInConsole(List<SourceTextEntry> suspects) {
        if (suspects != null && suspects.size() > 0) {
            for (SourceTextEntry ste : suspects) {
                String src = ste.getSrcText();
                TMXEntry trans = Core.getProject().getTranslationInfo(ste);
                if (src.length() > 0 && trans.isTranslated()) {
                    System.out.println(ste.entryNum());
                    System.out.println(src);
                    System.out.println(trans.translation);
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

    /**
     * Scans project and builds the list of entries which are suspected of
     * having changed (possibly invalid) tag structures.
     */
    @Override
    public List<SourceTextEntry> listInvalidTags() {
        int j;
        String s;
        TMXEntry te;
        List<String> srcTags = new ArrayList<String>(32);
        List<String> locTags = new ArrayList<String>(32);
        List<SourceTextEntry> suspects = new ArrayList<SourceTextEntry>(16);

        // programming language validation: pattern to detect printf variables
        // (%s and %n$s)
        Pattern printfPattern = null;
        if ("true".equalsIgnoreCase(Preferences.getPreference(Preferences.CHECK_ALL_PRINTF_TAGS))) {
            printfPattern = PatternConsts.PRINTF_VARS;
        } else if ("true".equalsIgnoreCase(Preferences.getPreference(Preferences.CHECK_SIMPLE_PRINTF_TAGS))) {
            printfPattern = PatternConsts.SIMPLE_PRINTF_VARS;
        }
        Pattern javaMessageFormatPattern = null;
        if ("true".equalsIgnoreCase(Preferences.getPreference(Preferences.CHECK_JAVA_PATTERN_TAGS))) {
            javaMessageFormatPattern = PatternConsts.SIMPLE_JAVA_MESSAGEFORMAT_PATTERN_VARS;
        }
        Pattern customTagPattern = null;
        String customRegExp = Preferences.getPreferenceDefaultAllowEmptyString(Preferences.CHECK_CUSTOM_PATTERN);
        if (!"".equalsIgnoreCase(customRegExp)) {
            customTagPattern = Pattern.compile(customRegExp);
        }
        Pattern RemovePattern = PatternConsts.getRemovePattern();

        for (FileInfo fi : Core.getProject().getProjectFiles()) {
            for (SourceTextEntry ste : fi.entries) {
                s = ste.getSrcText();
                te = Core.getProject().getTranslationInfo(ste);

                // if there's no translation, skip the string
                // bugfix for
                // http://sourceforge.net/support/tracker.php?aid=1209839
                if (!te.isTranslated()) {
                    continue;
                }

                if (printfPattern != null) {
                    // printf variables should be equal in number, 
                    // but order can change
                    // (and with that also notation: e.g. from '%s' to '%1$s') 
                    // We check this by adding the string "index+type specifier"
                    // of every found variable to a set.
                    // If the sets of the source and target are not equal, then
                    // there is a problem: either missing or extra variables, 
                    // or the type specifier has changed for the variable at the 
                    // given index.
                    HashSet<String> printfSourceSet = new HashSet<String>();
                    Matcher printfMatcher = printfPattern.matcher(s);
                    int index = 1;
                    while (printfMatcher.find()) {
                        String printfVariable = printfMatcher.group(0);
                        String argumentswapspecifier = printfMatcher.group(1);
                        if (argumentswapspecifier != null && argumentswapspecifier.endsWith("$")) {
                            printfSourceSet.add(""
                                    + argumentswapspecifier.substring(0, argumentswapspecifier.length() - 1)
                                    + printfVariable.substring(printfVariable.length() - 1,
                                            printfVariable.length()));
                        } else {
                            printfSourceSet.add(""
                                    + index
                                    + printfVariable.substring(printfVariable.length() - 1,
                                            printfVariable.length()));
                            index++;
                        }
                    }
                    HashSet<String> printfTargetSet = new HashSet<String>();
                    printfMatcher = printfPattern.matcher(te.translation);
                    index = 1;
                    while (printfMatcher.find()) {
                        String printfVariable = printfMatcher.group(0);
                        String argumentswapspecifier = printfMatcher.group(1);
                        if (argumentswapspecifier != null && argumentswapspecifier.endsWith("$")) {
                            printfTargetSet.add(""
                                    + argumentswapspecifier.substring(0, argumentswapspecifier.length() - 1)
                                    + printfVariable.substring(printfVariable.length() - 1,
                                            printfVariable.length()));
                        } else {
                            printfTargetSet.add(""
                                    + index
                                    + printfVariable.substring(printfVariable.length() - 1,
                                            printfVariable.length()));
                            index++;
                        }
                    }
                    if (!printfSourceSet.equals(printfTargetSet)) {
                        suspects.add(ste);
                        continue;
                    }
                }
                // Extra checks for PO files:
                if (fi.filterClass.equals(PoFilter.class)) {
                    // check PO line start:
                    if (s.startsWith("\n") != te.translation.startsWith("\n") ) {
                        suspects.add(ste);
                        continue;
                    }
                    // check PO line ending:
                    if (s.endsWith("\n") != te.translation.endsWith("\n")) {
                        suspects.add(ste);
                        continue;
                    }
                }
                // OmegaT tags and custom tags check: order and number should be equal
                srcTags.clear();
                locTags.clear();
                // extract tags from src and loc string
                StaticUtils.buildTagList(s, srcTags);
                StaticUtils.buildTagList(te.translation, locTags);
                // custom pattern checks: order and number should be equal
                if (customTagPattern != null) {
                    // extract tags from src and loc string
                    Matcher customTagPatternMatcher = customTagPattern.matcher(s);
                    while (customTagPatternMatcher.find()) {
                        srcTags.add(customTagPatternMatcher.group(0));
                    }
                    customTagPatternMatcher = customTagPattern.matcher(te.translation);
                    while (customTagPatternMatcher.find()) {
                        locTags.add(customTagPatternMatcher.group(0));
                    }
                }
                
                // Compare tag list sizes
                if (srcTags.size() != locTags.size()) {
                    suspects.add(ste);
                    continue;
                }
                
                // Compare tags one by one.
                boolean tagsAreIdentical = true;
                for (j = 0; j < srcTags.size(); j++) {
                    s = srcTags.get(j);
                    String t = locTags.get(j);
                    if (!s.equals(t)) {
                        tagsAreIdentical = false;
                        break;
                    }
                }
                
                if (!tagsAreIdentical) {
                    // If we are doing strict validation only, or if the tags are malformed,
                    // finish this TU now.
                    if (!Preferences.isPreference(Preferences.LOOSE_TAG_ORDERING)
                            || !tagsAreWellFormed(srcTags, locTags)) {
                        suspects.add(ste);
                        continue;
                    }
                }

                // Java MessageFormat pattern checks: order can change, number should be equal.
                if (javaMessageFormatPattern != null) {
                    srcTags.clear();
                    locTags.clear();
                    Matcher javaMessageFormatMatcher = javaMessageFormatPattern.matcher(s);
                    while (javaMessageFormatMatcher.find()) {
                        srcTags.add(javaMessageFormatMatcher.group(0));
                    }
                    javaMessageFormatMatcher = javaMessageFormatPattern.matcher(te.translation);
                    while (javaMessageFormatMatcher.find()) {
                        locTags.add(javaMessageFormatMatcher.group(0));
                    }
                    Collections.sort(srcTags);
                    Collections.sort(locTags);
                    if (!srcTags.equals(locTags)) {
                        suspects.add(ste);
                        continue;
                    }
                }

                //check translation for stuff that should have been removed.
                if (RemovePattern != null) {
                    Matcher removeMatcher = RemovePattern.matcher(te.translation);
                    if (removeMatcher.find()) {
                        suspects.add(ste);
                        continue;
                    }
                }

            } //end loop over entries
        } //end loop over files
        return suspects.isEmpty() ? null : suspects;
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
}
