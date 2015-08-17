/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Aaron Madlon-Kay, Zoltan Bartko
               2014 Aaron Madlon-Kay
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

package org.omegat.gui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.lucene.util.Version;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.core.Core;
import org.omegat.core.data.ProtectedPart;
import org.omegat.gui.editor.autocompleter.AutoCompleterItem;
import org.omegat.gui.editor.autocompleter.AutoCompleterListView;
import org.omegat.util.OStrings;
import org.omegat.util.TagUtil;
import org.omegat.util.TagUtil.Tag;
import org.omegat.util.Token;

/**
 * An AutoCompleterView for inserting missing tags.
 * 
 * @author Aaron Madlon-Kay
 */
public class TagAutoCompleterView extends AutoCompleterListView {

    private static final ITokenizer TAG_TOKENIZER = new TagTokenizer();

    public TagAutoCompleterView() {
        super(OStrings.getString("AC_TAG_VIEW"));
    }

    @Override
    public List<AutoCompleterItem> computeListData(String prevText, boolean contextualOnly) {
        String wordChunk = getLastToken(prevText);
        
        List<String> missingGroups = TagUtil.getGroupedMissingTagsFromTarget();
        
        // If wordChunk is a tag, pretend we have a blank wordChunk.
        for (Tag tag : TagUtil.getAllTagsInSource()) {
            if (tag.tag.equals(wordChunk)) {
                wordChunk = "";
                break;
            }
        }

        List<String> matchGroups = new ArrayList<String>();
        if (!"".equals(wordChunk)) {
            // Check for partial matches among missing tag groups.
            for (String g : missingGroups) {
                if (g.startsWith(wordChunk)) matchGroups.add(g);
            }
        }

        // If there are no partial matches, show all missing tags as suggestions.
        if (matchGroups.isEmpty() && !contextualOnly) {
            return convertList(missingGroups, 0);
        }
        
        return convertList(matchGroups, wordChunk.length());
    }

    private static List<AutoCompleterItem> convertList(List<String> list, int replacementLength) {
        List<AutoCompleterItem> result = new ArrayList<AutoCompleterItem>();
        for (String s : list) {
            int sep = s.indexOf(TagUtil.TAG_SEPARATOR_SENTINEL);
            String cleaned = s;
            String display = s;
            int adjustment = 0;
            boolean keepSelection = false;
            if (sep > -1) {
                cleaned = s.replace(TagUtil.TAG_SEPARATOR_SENTINEL, "");
                display = s.replace(TagUtil.TAG_SEPARATOR_SENTINEL, "|");
                adjustment = - (s.length() - 1 - sep);
                keepSelection = true;
            }
            result.add(new AutoCompleterItem(cleaned, new String[] { display }, adjustment, keepSelection, replacementLength));
        }
        return result;
    }

    @Override
    public ITokenizer getTokenizer() {
        return TAG_TOKENIZER;
    }

    @Override
    public String itemToString(AutoCompleterItem item) {
        return item.extras[0];
    }
    
    private static class TagTokenizer implements ITokenizer {

        @SuppressWarnings("unchecked")
        @Override
        public Map<Version, String> getSupportedBehaviors() {
            return Collections.EMPTY_MAP;
        }

        @Override
        public Version getBehavior() {
            return null;
        }

        @Override
        public void setBehavior(Version behavior) {}

        @Override
        public Version getDefaultBehavior() {
            return null;
        }

        @Override
        public Token[] tokenizeWords(String str, StemmingMode stemmingMode) {
            return tokenize(str);
        }

        @Override
        public Token[] tokenizeWordsForSpelling(String str) {
            return tokenize(str);
        }

        @Override
        public Token[] tokenizeAllExactly(String str) {
            return tokenize(str);
        }

        private Token[] tokenize(String str) {
            String regex = buildRegex();
            if (regex == null) {
                return new Token[] { new Token(str, 0) };
            }
            String[] pieces = str.split(regex);
            Token[] tokens = new Token[pieces.length];
            for (int i = 0, offset = 0; i < pieces.length; i++) {
                tokens[i] = new Token(pieces[i], offset);
                offset += pieces[i].length();
            }
            return tokens;
        }
        
        /**
         * Create a regex that will split a string in front of any protected parts.
         * It will look like "(?=c1|c2|c3|...|cn)" where c1,...,cn are the unique first
         * characters of protected parts in the current segment.
         * 
         * @return regex string
         */
        private String buildRegex() {
            ProtectedPart[] protectedParts = Core.getEditor().getCurrentEntry().getProtectedParts();
            if (protectedParts.length == 0) {
                return null;
            }
            List<String> initials = new ArrayList<String>();
            for (ProtectedPart pp : protectedParts) {
                String part = pp.getTextInSourceSegment();
                String initial = part.substring(0, part.offsetByCodePoints(0, 1));
                if (!initials.contains(initial)) {
                    initials.add(initial);
                }
            }
            StringBuilder regex = new StringBuilder("(?=");
            for (int i = 0, max = initials.size(); i < max; i++) {
                regex.append(Pattern.quote(initials.get(i)));
                if (i + 1 < max) {
                    regex.append('|');
                }
            }
            regex.append(')');
            return regex.toString();
        }
        
        @Override
        public String[] getSupportedLanguages() {
            return new String[0];
        }
    }
}
