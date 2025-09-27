/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
               2024 Hiroshi Miura
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

package org.omegat.gui.glossary;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jetbrains.annotations.VisibleForTesting;
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
 * @author Hiroshi Miura
 */
public class GlossarySearcher {
    private final ITokenizer tok;
    private final Language srcLang;
    private final Language targetLang;
    private final boolean mergeAltDefinitions;

    public GlossarySearcher(ITokenizer tok, Language srcLang, boolean mergeAltDefinitions) {
        this(tok, srcLang, Core.getProject().getProjectProperties().getTargetLanguage(), mergeAltDefinitions);
    }

    public GlossarySearcher(ITokenizer tok, Language srcLang, Language targetLang,
            boolean mergeAltDefinitions) {
        this.tok = tok;
        this.srcLang = srcLang;
        this.targetLang = targetLang;
        this.mergeAltDefinitions = mergeAltDefinitions;
    }

    public List<GlossaryEntry> searchSourceMatches(SourceTextEntry ste, List<GlossaryEntry> entries) {

        List<GlossaryEntry> result = new ArrayList<>();

        // Compute source entry tokens
        Token[] strTokens = tokenize(ste.getSrcText(),
                TagUtil.buildTagList(ste.getSrcText(), ste.getProtectedParts()));

        for (GlossaryEntry glosEntry : entries) {
            checkCancelled();
            if (isTokenMatch(strTokens, ste.getSrcText(), glosEntry.getSrcText())
                    || isCjkMatch(ste.getSrcText(), glosEntry.getSrcText())) {
                result.add(glosEntry);
            }
        }

        // After the matched entries have been tokenized and listed,
        // we reorder entries as
        // 1) by priority
        // 2) by length of source text if one contains another (optional)
        // 3) by alphabet of source term
        // 4) by length of localized term (optional)
        // 5) by alphabet of localized term
        // Then remove the duplicates and combine the synonyms.
        final Collator srcLangCollator = Collator.getInstance(srcLang.getLocale());
        final Collator targetLangCollator = Collator.getInstance(targetLang.getLocale());
        return filterGlossary(sortGlossaryEntries(srcLangCollator, targetLangCollator, result),
                mergeAltDefinitions);
    }

    public List<Token[]> searchSourceMatchTokens(SourceTextEntry ste, GlossaryEntry entry) {
        // Compute source entry tokens
        Token[] strTokens = tokenize(ste.getSrcText(),
                TagUtil.buildTagList(ste.getSrcText(), ste.getProtectedParts()));

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
     * Override this to throw an exception (that you will catch) to abort
     * matching.
     */
    protected void checkCancelled() {
    }

    private boolean isTokenMatch(Token[] fullTextTokens, String fullText, String term) {
        return !getMatchingTokens(fullTextTokens, fullText, term).isEmpty();
    }

    @VisibleForTesting
    boolean isGlossaryNotExactMatch() {
        return Preferences.isPreferenceDefault(Preferences.GLOSSARY_NOT_EXACT_MATCH,
                Preferences.GLOSSARY_NOT_EXACT_MATCH_DEFAULT);
    }

    private List<Token[]> getMatchingTokens(Token[] fullTextTokens, String fullText, String term) {
        // Compute glossary entry tokens
        Token[] glosTokens = tokenize(term);
        if (glosTokens.length == 0) {
            return Collections.emptyList();
        }
        boolean notExact = isGlossaryNotExactMatch();
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
            if (term.contains(token.getTextFromString(srcTxt))) {
                return true;
            }
        }
        return false;
    }

    @VisibleForTesting
    boolean isRequireSimilarCase() {
        return Preferences.isPreferenceDefault(Preferences.GLOSSARY_REQUIRE_SIMILAR_CASE,
                Preferences.GLOSSARY_REQUIRE_SIMILAR_CASE_DEFAULT);
    }

    private boolean keepMatch(Token[] tokens, String srcTxt, String locTxt) {
        // Filter out matches where the glossary entry is all caps but the
        // source-text match is not.
        if (isRequireSimilarCase() && StringUtil.isUpperCase(locTxt)) {
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
        // This is a CJK word and our source language is not space-delimited, so
        // include if word appears anywhere in source string.
        IProject project = Core.getProject();
        return project.isProjectLoaded()
                && !project.getProjectProperties().getSourceLanguage().isSpaceDelimited()
                && StringUtil.isCJK(term) && fullText.contains(term);
    }

    private static List<Token[]> getCjkMatchingTokens(String fullText, String term) {
        // This is a CJK word and our source language is not space-delimited, so
        // include if word appears anywhere in source string.
        IProject project = Core.getProject();
        if (!project.isProjectLoaded()
                || project.getProjectProperties().getSourceLanguage().isSpaceDelimited()) {
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

    @VisibleForTesting
    boolean isGlossaryStemming() {
        return Preferences.isPreferenceDefault(Preferences.GLOSSARY_STEMMING, Preferences.GLOSSARY_STEMMING_DEFAULT);
    }

    @VisibleForTesting
    Token[] tokenize(String str) {
        // Make comparison case-insensitive
        String strLower = str.toLowerCase(srcLang.getLocale());
        if (isGlossaryStemming()) {
            if (Preferences.isPreference(Preferences.GLOSSARY_STEMMING_FULL)) {
                return tok.tokenizeWords(strLower, StemmingMode.GLOSSARY_FULL);
            } else {
                return tok.tokenizeWords(strLower, StemmingMode.GLOSSARY);
            }
        } else {
            // skip whitespace tokens
            return Arrays.stream(tok.tokenizeVerbatim(strLower)).filter(tok -> !StringUtil.isWhiteSpace(
                    strLower.charAt(tok.getOffset()))).toArray(Token[]::new);
        }
    }

    private Token[] tokenize(String str, List<Tag> tags) {
        Token[] tokens = tokenize(str);
        if (tags.isEmpty()) {
            return tokens;
        }
        List<Token> result = new ArrayList<>(tokens.length);
        for (Token token : tokens) {
            if (!tokenInTag(token, tags)) {
                result.add(token);
            }
        }
        return result.toArray(new Token[0]);
    }

    private static boolean tokenInTag(Token tok, List<Tag> tags) {
        for (Tag tag : tags) {
            if (tok.getOffset() >= tag.pos
                    && tok.getOffset() + tok.getLength() <= tag.pos + tag.tag.length()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sorts a list of glossary entries based on various criteria, including
     * priority, source text length, source text alphabetical order, target text
     * length, and target text alphabetical order.
     *
     * @param srcLangCollator
     *            Collator used for sorting source texts language-dependently.
     * @param targetLangCollator
     *            Collator used for sorting target texts language-dependently.
     * @param entries
     *            The list of glossary entries to be sorted.
     * @return A sorted list of glossary entries.
     * @throws IllegalArgumentException
     *             If the entries list is null.
     */
    List<GlossaryEntry> sortGlossaryEntries(Collator srcLangCollator, Collator targetLangCollator,
            List<GlossaryEntry> entries) throws IllegalArgumentException {
        if (entries == null) {
            throw new IllegalArgumentException("entries must not be null");
        }
        return entries
                .stream().filter(Objects::nonNull).sorted((o1, o2) -> compareGlossaryEntries(o1, o2,
                        srcLangCollator, targetLangCollator)).collect(Collectors.toList());
    }

    @VisibleForTesting
    boolean isGlossarySortBySrcLength() {
        return Preferences.isPreferenceDefault(Preferences.GLOSSARY_SORT_BY_SRC_LENGTH, true);
    }

    @VisibleForTesting
    boolean isGlossarySortByLength() {
        return Preferences.isPreferenceDefault(Preferences.GLOSSARY_SORT_BY_LENGTH, false);
    }

    private int compareGlossaryEntries(GlossaryEntry o1, GlossaryEntry o2, Collator srcLangCollator,
            Collator targetLangCollator) {
        int p1 = o1.getPriority() ? 1 : 2;
        int p2 = o2.getPriority() ? 1 : 2;
        boolean sortBySrcLength = isGlossarySortBySrcLength();
        boolean sortByLength = isGlossarySortByLength();
        int c = p1 - p2;
        if (c == 0 && sortBySrcLength && (o2.getSrcText().startsWith(o1.getSrcText())
                || o1.getSrcText().startsWith(o2.getSrcText()))) {
            c = o2.getSrcText().length() - o1.getSrcText().length();
        }
        if (c == 0) {
            c = compareLanguageDependent(srcLangCollator, o1.getSrcText(), o2.getSrcText());
        }
        if (c == 0 && sortByLength) {
            c = o2.getLocText().length() - o1.getLocText().length();
        }
        if (c == 0) {
            c = compareLanguageDependent(targetLangCollator, o1.getLocText(), o2.getLocText());
        }
        return c;
    }

    private int compareLanguageDependent(Collator langCollator, String s1, String s2) {
        // Use primary criteria - for most languages written with latin
        // alphabet, PRIMARY means case-insensitive
        // (see
        // https://docs.oracle.com/javase/8/docs/api/java/text/Collator.html#PRIMARY)
        langCollator.setStrength(Collator.PRIMARY);
        int c = langCollator.compare(s1, s2);
        if (c != 0) {
            return c;
        }
        // Use secondary criteria - for most languages written with latin
        // alphabet, SECONDARY means ignore accents
        // (see
        // https://docs.oracle.com/javase/8/docs/api/java/text/Collator.html#PRIMARY)
        langCollator.setStrength(Collator.SECONDARY);
        c = langCollator.compare(s1, s2);
        if (c != 0) {
            return c;
        }
        // Use tertiary criteria - language-dependent
        // (see
        // https://docs.oracle.com/javase/8/docs/api/java/text/Collator.html#TERTIARY)
        langCollator.setStrength(Collator.TERTIARY);
        return langCollator.compare(s1, s2);
    }

    private static List<GlossaryEntry> filterGlossary(List<GlossaryEntry> result,
            boolean mergeAltDefinitions) {
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

            if (nowEntry.getSrcText().isEmpty()) {
                continue;
            }

            for (int j = i + 1; j < result.size(); j++) {
                GlossaryEntry thenEntry = result.get(j);

                if (thenEntry.getSrcText().isEmpty()) {
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
            List<GlossaryEntry> newList = new LinkedList<>();

            while (myIter.hasNext()) {
                GlossaryEntry checkEntry = myIter.next();
                if (checkEntry.getSrcText().isEmpty() || checkEntry.getLocText().isEmpty()) {
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

        List<GlossaryEntry> returnList = new LinkedList<>();

        // Group items with same scrTxt
        for (int i = 0; i < result.size(); i++) {
            List<GlossaryEntry> srcList = new LinkedList<>();
            GlossaryEntry nowEntry = result.get(i);

            if (nowEntry.getSrcText().isEmpty()) {
                continue;
            }
            srcList.add(nowEntry);

            for (int j = i + 1; j < result.size(); j++) {
                GlossaryEntry thenEntry = result.get(j);

                // Double check, needed?
                if (thenEntry.getSrcText().isEmpty()) {
                    continue;
                }
                if (nowEntry.getSrcText().equals(thenEntry.getSrcText())) {
                    srcList.add(thenEntry);
                    result.set(j, replaceEntry);
                }
            }

            // Sort items with same locTxt
            List<GlossaryEntry> sortList = new LinkedList<>();
            if (srcList.size() > 1) {
                for (int k = 0; k < srcList.size(); k++) {
                    GlossaryEntry srcNow = srcList.get(k);

                    if (srcNow.getSrcText().isEmpty()) {
                        continue;
                    }
                    sortList.add(srcNow);

                    for (int l = k + 1; l < srcList.size(); l++) {
                        GlossaryEntry srcThen = srcList.get(l);

                        if (srcThen.getSrcText().isEmpty()) {
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

            GlossaryEntry combineEntry = new GlossaryEntry(srcTxt,
                    locTxts.toArray(new String[locTxts.size()]), comTxts.toArray(new String[comTxts.size()]),
                    priorities, origins.toArray(new String[origins.size()]));
            returnList.add(combineEntry);
        }
        return returnList;
    }
}
