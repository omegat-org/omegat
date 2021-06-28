/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.glossary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.ITokenizer.StemmingMode;
import org.omegat.util.Language;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.TagUtil;
import org.omegat.util.TagUtil.Tag;
import org.omegat.util.Token;

/**
 * A class encapsulating glossary matching logic.
 *
 * @author Aaron Madlon-Kay
 */
public class GlossarySearcher {
    private final ITokenizer tok;
    private final Language lang;
    private final boolean mergeAltDefinitions;

    public GlossarySearcher(ITokenizer tok, Language lang, boolean mergeAltDefinitions) {
        this.tok = tok;
        this.lang = lang;
        this.mergeAltDefinitions = mergeAltDefinitions;
    }

    public List<GlossaryEntry> searchSourceMatches(SourceTextEntry ste, List<GlossaryEntry> entries) {

        List<GlossaryEntry> result = new ArrayList<>();

        // Compute source entry tokens
        Token[] strTokens = tokenize(ste.getSrcText(), TagUtil.buildTagList(ste.getSrcText(), ste.getProtectedParts()));

        for (GlossaryEntry glosEntry : entries) {
            checkCancelled();
            if (isTokenMatch(strTokens, ste.getSrcText(), glosEntry.getSrcText())
                    || isCjkMatch(ste.getSrcText(), glosEntry.getSrcText())) {
                result.add(glosEntry);
            }
        }

        // After the matched entries have been tokenized and listed.
        // We reorder entries: 1) by priority, 2) by length, 3) by alphabet
        // Then remove the duplicates and combine the synonyms.
        sortGlossaryEntries(result);
        return filterGlossary(result, mergeAltDefinitions);
    }

    public List<Token[]> searchSourceMatchTokens(SourceTextEntry ste, GlossaryEntry entry) {
        // Compute source entry tokens
        Token[] strTokens = tokenize(ste.getSrcText(), TagUtil.buildTagList(ste.getSrcText(), ste.getProtectedParts()));

        List<Token[]> toks = getMatchingTokens(strTokens, ste.getSrcText(), entry.getSrcText());
        if (toks.isEmpty()) {
            toks = getCjkMatchingTokens(ste.getSrcText(), entry.getSrcText());
        }
        return toks;
    }

    public List<String> searchTargetMatches(String trg, ProtectedPart[] protectedParts, GlossaryEntry entry) {

        List<String> result = new ArrayList<>();

        // Compute source entry tokens
        Token[] strTokens = tokenize(trg, TagUtil.buildTagList(trg, protectedParts));

        for (String term : entry.getLocTerms(true)) {
            checkCancelled();
            if (isTokenMatch(strTokens, trg, term) || isCjkMatch(trg, term)) {
                result.add(term);
            }
        }
        // No need to sort or filter
        return result;
    }

    /**
     * Override this to throw an exception (that you will catch) to abort matching.
     */
    protected void checkCancelled() {
    }

    private boolean isTokenMatch(Token[] fullTextTokens, String fullText, String term) {
        return !getMatchingTokens(fullTextTokens, fullText, term).isEmpty();
    }

    private List<Token[]> getMatchingTokens(Token[] fullTextTokens, String fullText, String term) {
        // Compute glossary entry tokens
        Token[] glosTokens = tokenize(term);
        if (glosTokens.length == 0) {
            return Collections.emptyList();
        }
        boolean notExact = Preferences.isPreferenceDefault(Preferences.GLOSSARY_NOT_EXACT_MATCH,
                Preferences.GLOSSARY_NOT_EXACT_MATCH_DEFAULT);
        List<Token[]> foundTokens = DefaultTokenizer.searchAll(fullTextTokens, glosTokens, notExact);
        foundTokens.removeIf(toks -> !keepMatch(toks, fullText, term));
        if (StringUtil.isCJK(term)) {
            // This is a workaround for a high reported hash collision rate for
            // short Japanese strings. This assumes that every matched term will
            // have at least one token that is a prefix of the term itself; this
            // doesn't necessarily hold in general for stemming/lemmatizing for
            // all languages, but it seems probably OK for CJK.
            //
            // See https://sourceforge.net/p/omegat/bugs/1034/
            foundTokens.removeIf(toks -> !rawMatch(toks, fullText, term));
        }
        return foundTokens;
    }

    private static boolean rawMatch(Token[] tokens, String srcTxt, String term) {
        for (Token token : tokens) {
            if (token.getTextFromString(srcTxt).equals(term.substring(token.getLength()))) {
                return true;
            }
        }
        return false;
    }

    private static boolean keepMatch(Token[] tokens, String srcTxt, String locTxt) {
        // Filter out matches where the glossary entry is all caps but the source-text match is not.
        if (Preferences.isPreferenceDefault(Preferences.GLOSSARY_REQUIRE_SIMILAR_CASE,
                Preferences.GLOSSARY_REQUIRE_SIMILAR_CASE_DEFAULT) && StringUtil.isUpperCase(locTxt)) {
            for (Token tok : tokens) {
                String matched = tok.getTextFromString(srcTxt);
                if (!StringUtil.isUpperCase(matched)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected static boolean isCjkMatch(String fullText, String term) {
        // This is a CJK word and our source language is not space-delimited, so include if
        // word appears anywhere in source string.
        IProject project = Core.getProject();
        return project.isProjectLoaded() && !project.getProjectProperties().getSourceLanguage().isSpaceDelimited()
                && StringUtil.isCJK(term) && fullText.contains(term);
    }

    private static List<Token[]> getCjkMatchingTokens(String fullText, String term) {
        // This is a CJK word and our source language is not space-delimited, so include if
        // word appears anywhere in source string.
        IProject project = Core.getProject();
        if (!project.isProjectLoaded() || project.getProjectProperties().getSourceLanguage().isSpaceDelimited()) {
            return Collections.emptyList();
        }
        if (!StringUtil.isCJK(term)) {
            return Collections.emptyList();
        }
        int i = fullText.indexOf(term);
        if (i == -1) {
            return Collections.emptyList();
        }
        List<Token[]> result = new ArrayList<>();
        result.add(new Token[] { new Token(term, i) });
        while ((i = fullText.indexOf(term, i + 1)) != -1) {
            result.add(new Token[] { new Token(term, i) });
        }
        return result;
    }

    private Token[] tokenize(String str) {
        // Make comparison case-insensitive
        String strLower = str.toLowerCase(lang.getLocale());
        if (Preferences.isPreferenceDefault(Preferences.GLOSSARY_STEMMING, Preferences.GLOSSARY_STEMMING_DEFAULT)) {
            return tok.tokenizeWords(strLower, StemmingMode.GLOSSARY);
        } else {
            return tok.tokenizeVerbatim(strLower);
        }
    }

    private Token[] tokenize(String str, List<Tag> tags) {
        Token[] tokens = tokenize(str);
        if (tags.isEmpty()) {
            return tokens;
        }
        List<Token> result = new ArrayList<>(tokens.length);
        for (Token tok : tokens) {
            if (!tokenInTag(tok, tags)) {
                result.add(tok);
            }
        }
        return result.toArray(new Token[result.size()]);
    }

    private static boolean tokenInTag(Token tok, List<Tag> tags) {
        for (Tag tag : tags) {
            if (tok.getOffset() >= tag.pos && tok.getOffset() + tok.getLength() <= tag.pos + tag.tag.length()) {
                return true;
            }
        }
        return false;
    }

    static void sortGlossaryEntries(List<GlossaryEntry> entries) {
        Collections.sort(entries, (o1, o2) -> {
            int p1 = o1.getPriority() ? 1 : 2;
            int p2 = o2.getPriority() ? 1 : 2;
            int c = p1 - p2;
            if (c == 0) {
                c = o2.getSrcText().length() - o1.getSrcText().length();
            }
            if (c == 0) {
                c = o1.getSrcText().compareToIgnoreCase(o2.getSrcText());
            }
            if (c == 0) {
                c = o1.getSrcText().compareTo(o2.getSrcText());
            }
            if (c == 0) {
                c = o1.getLocText().compareToIgnoreCase(o2.getLocText());
            }
            return c;
        });
    }

    private static List<GlossaryEntry> filterGlossary(List<GlossaryEntry> result, boolean mergeAltDefinitions) {
        // First check that entries exist in the list.
        if (result.isEmpty()) {
            return result;
        }

        // The default replace entry
        GlossaryEntry replaceEntry = new GlossaryEntry("", "", "", false, null);

        // Remove the duplicates from the list
        boolean removedDuplicate = false;
        for (int i = 0; i < result.size(); i++) {
            GlossaryEntry nowEntry = result.get(i);

            if (nowEntry.getSrcText().equals("")) {
                continue;
            }

            for (int j = i + 1; j < result.size(); j++) {
                GlossaryEntry thenEntry = result.get(j);

                if (thenEntry.getSrcText().equals("")) {
                    continue;
                }

                // If the Entries are exactely the same, insert a blank entry.
                if (nowEntry.getSrcText().equals(thenEntry.getSrcText())
                        && nowEntry.getLocText().equals(thenEntry.getLocText())
                        && nowEntry.getCommentText().equals(thenEntry.getCommentText())) {
                    result.set(j, replaceEntry);
                    removedDuplicate = true;
                }
            }
        }

        // Remove the blank entries from the list
        if (removedDuplicate) {
            Iterator<GlossaryEntry> myIter = result.iterator();
            List<GlossaryEntry> newList = new LinkedList<GlossaryEntry>();

            while (myIter.hasNext()) {
                GlossaryEntry checkEntry = myIter.next();
                if (checkEntry.getSrcText().equals("") || checkEntry.getLocText().equals("")) {
                    myIter.remove();
                } else {
                    newList.add(checkEntry);
                }
            }

            result = newList;
        }

        if (!mergeAltDefinitions) {
            return result;
        }

        List<GlossaryEntry> returnList = new LinkedList<GlossaryEntry>();

        // Group items with same scrTxt
        for (int i = 0; i < result.size(); i++) {
            List<GlossaryEntry> srcList = new LinkedList<GlossaryEntry>();
            GlossaryEntry nowEntry = result.get(i);

            if (nowEntry.getSrcText().equals("")) {
                continue;
            }
            srcList.add(nowEntry);

            for (int j = i + 1; j < result.size(); j++) {
                GlossaryEntry thenEntry = result.get(j);

                // Double check, needed?
                if (thenEntry.getSrcText().equals("")) {
                    continue;
                }
                if (nowEntry.getSrcText().equals(thenEntry.getSrcText())) {
                    srcList.add(thenEntry);
                    result.set(j, replaceEntry);
                }
            }

            // Sort items with same locTxt
            List<GlossaryEntry> sortList = new LinkedList<GlossaryEntry>();
            if (srcList.size() > 1) {
                for (int k = 0; k < srcList.size(); k++) {
                    GlossaryEntry srcNow = srcList.get(k);

                    if (srcNow.getSrcText().equals("")) {
                        continue;
                    }
                    sortList.add(srcNow);

                    for (int l = k + 1; l < srcList.size(); l++) {
                        GlossaryEntry srcThen = srcList.get(l);

                        if (srcThen.getSrcText().equals("")) {
                            continue;
                        }
                        if (srcNow.getLocText().equals(srcThen.getLocText())) {
                            sortList.add(srcThen);
                            srcList.set(l, replaceEntry);
                        }
                    }
                }
            } else {
                sortList = srcList;
            }
            // Now put the sortedList together
            String srcTxt = sortList.get(0).getSrcText();
            List<String> locTxts = new ArrayList<>();
            List<String> comTxts = new ArrayList<>();
            List<Boolean> prios = new ArrayList<>();
            List<String> origins = new ArrayList<>();

            for (GlossaryEntry e : sortList) {
                for (String s : e.getLocTerms(false)) {
                    locTxts.add(s);
                }
                for (String s : e.getComments()) {
                    comTxts.add(s);
                }
                for (boolean s : e.getPriorities()) {
                    prios.add(s);
                }
                for (String o : e.getOrigins(false)) {
                    origins.add(o);
                }
            }
            boolean[] priorities = new boolean[prios.size()];
            for (int j = 0; j < prios.size(); j++) {
                priorities[j] = prios.get(j);
            }

            GlossaryEntry combineEntry = new GlossaryEntry(srcTxt, locTxts.toArray(new String[locTxts.size()]),
                    comTxts.toArray(new String[comTxts.size()]), priorities,
                    origins.toArray(new String[origins.size()]));
            returnList.add(combineEntry);
        }
        return returnList;
    }
}
