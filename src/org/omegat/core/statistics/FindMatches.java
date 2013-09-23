/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Alex Buloichik
               2012 Thomas Cordonnier, Martin Fleurke
               2013 Aaron Madlon-Kay
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

package org.omegat.core.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ExternalTMX;
import org.omegat.core.data.IProject;
import org.omegat.core.data.IProject.DefaultTranslationsIterator;
import org.omegat.core.data.IProject.MultipleTranslationsIterator;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.events.IStopped;
import org.omegat.core.matching.FuzzyMatcher;
import org.omegat.core.matching.ISimilarityCalculator;
import org.omegat.core.matching.LevenshteinDistance;
import org.omegat.core.matching.NearString;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.Token;

/**
 * Class to find matches by specified criteria.
 * 
 * Since we can use stemmers to prepare tokens, we should use 3-pass comparison of similarity. Similarity will
 * be calculated in 3 steps:
 * 
 * 1. Split original segment into word-only tokens using stemmer (with stop words list), then compare tokens.
 * 
 * 2. Split original segment into word-only tokens without stemmer, then compare tokens.
 * 
 * 3. Split original segment into not-only-words tokens (including numbers and tags) without stemmer, then
 * compare tokens.
 * 
 * This class is not thread safe ! Must be used in the one thread only.
 * 
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 * @author Aaron Madlon-Kay
 */
public class FindMatches {

    private static final int PENALTY_FOR_FUZZY = 20;
    private static final int PENALTY_FOR_REMOVED = 5;

    private final ISimilarityCalculator distance = new LevenshteinDistance();

    /**
     * the removePattern that was configured by the user.
     */
    private final Pattern removePattern = PatternConsts.getRemovePattern();

    private final ITokenizer tok;
    private final int maxCount;

    /** Result list. */
    private List<NearString> result = new ArrayList<NearString>(OConsts.MAX_NEAR_STRINGS + 1);

    private String originalText;
    private String srcText;

    /**
     * Text that was removed by the removePattern from the source text.
     */
    private String removedText;

    /** Tokens for original string, with and without stems. */
    private Token[] strTokensStem, strTokensNoStem;

    /** Tokens for original string, includes numbers and tags. */
    private Token[] strTokensAll;

    public FindMatches(ITokenizer sourceTokenizer, int maxCount) {
        tok = sourceTokenizer;
        this.maxCount = maxCount;
    }

    public List<NearString> search(final IProject project, final String searchText,
            final boolean requiresTranslation, final boolean fillSimilarityData, final IStopped stop)
            throws StoppedException {
        result.clear();

        originalText = searchText;
        srcText = searchText;

        this.removedText = "";
        // remove part that is to be removed according to user settings.
        // Rationale: it might be a big string influencing the 'editing distance', while it is not really part
        // of the translatable text
        if (removePattern != null) {
            Matcher removeMatcher = removePattern.matcher(srcText);
            while (removeMatcher.find()) {
                removedText += srcText.substring(removeMatcher.start(), removeMatcher.end());
            }
            srcText = removeMatcher.replaceAll("");
        }
        // get tokens for original string
        strTokensStem = tokenizeStem(srcText);
        strTokensNoStem = tokenizeNoStem(srcText);
        strTokensAll = tokenizeAll(srcText);
        /* HP: includes non - word tokens */

        final String orphanedFileName = OStrings.getString("CT_ORPHAN_STRINGS");

        // travel by project entries, including orphaned
        if (project.getProjectProperties().isSupportDefaultTranslations()) {
            project.iterateByDefaultTranslations(new DefaultTranslationsIterator() {
                public void iterate(String source, TMXEntry trans) {
                    checkStopped(stop);
                    if (source.equals(originalText)) {
                        // skip original==original entry comparison
                        return;
                    }
                    if (requiresTranslation && trans.translation == null) {
                        return;
                    }
                    String fileName = project.isOrphaned(source) ? orphanedFileName : null;
                    processEntry(null, source, trans.translation, NearString.MATCH_SOURCE.MEMORY, false, 0,
                            fileName, trans.creator, trans.creationDate, trans.changer, trans.changeDate,
                            trans.properties);
                }
            });
        }
        project.iterateByMultipleTranslations(new MultipleTranslationsIterator() {
            public void iterate(EntryKey source, TMXEntry trans) {
                checkStopped(stop);
                if (source.sourceText.equals(originalText)) {
                    // skip original==original entry comparison
                    return;
                }
                if (requiresTranslation && trans.translation == null) {
                    return;
                }
                String fileName = project.isOrphaned(source) ? orphanedFileName : null;
                processEntry(source, source.sourceText, trans.translation, NearString.MATCH_SOURCE.MEMORY,
                        false, 0, fileName, trans.creator, trans.creationDate, trans.changer,
                        trans.changeDate, trans.properties);
            }
        });

        // travel by translation memories
        Pattern SEARCH_FOR_PENALTY = Pattern.compile("penalty-(\\d+)");
        for (Map.Entry<String, ExternalTMX> en : project.getTransMemories().entrySet()) {
            int penalty = 0;
            Matcher matcher = SEARCH_FOR_PENALTY.matcher(en.getKey());
            if (matcher.find()) {
                penalty = Integer.parseInt(matcher.group(1));
            }
            for (TMXEntry tmen : en.getValue().getEntries()) {
                checkStopped(stop);
                if (requiresTranslation && tmen.translation == null) {
                    continue;
                }
                processEntry(null, tmen.source, tmen.translation, NearString.MATCH_SOURCE.TM, false, penalty,
                        en.getKey(), tmen.creator, tmen.creationDate, tmen.changer, tmen.changeDate,
                        tmen.properties);
            }
        }

        // travel by all entries for check source file translations
        for (SourceTextEntry ste : project.getAllEntries()) {
            checkStopped(stop);
            if (ste.getSourceTranslation() != null) {
                processEntry(ste.getKey(), ste.getSrcText(), ste.getSourceTranslation(),
                        NearString.MATCH_SOURCE.MEMORY, ste.isSourceTranslationFuzzy(), 0, ste.getKey().file,
                        "", 0, "", 0, null);
            }
        }

        if (fillSimilarityData) {
            // fill similarity data only for result
            for (NearString near : result) {
                // fix for bug 1586397
                byte[] similarityData = FuzzyMatcher.buildSimilarityData(strTokensAll,
                        tokenizeAll(near.source));
                near.attr = similarityData;
            }
        }

        return result;
    }

    /**
     * Compare one entry with original entry.
     * 
     * @param candEntry
     *            entry to compare
     */
    protected void processEntry(final EntryKey key, final String source, final String translation,
            NearString.MATCH_SOURCE comesFrom, final boolean fuzzy, final int penalty, final String tmxName,
            final String creator, final long creationDate, final String changer, final long changedDate,
            final Map<String, String> props) {
        // remove part that is to be removed prior to tokenize
        String realSource = source;
        String entryRemovedText = "";
        int realPenaltyForRemoved = 0;
        if (this.removePattern != null) {
            Matcher removeMatcher = removePattern.matcher(realSource);
            while (removeMatcher.find()) {
                entryRemovedText += source.substring(removeMatcher.start(), removeMatcher.end());
            }
            realSource = removeMatcher.replaceAll("");
            // calculate penalty if something has been removed, otherwise different strings get 100% match.
            if (!entryRemovedText.equals(this.removedText)) {
                // penalty for different 'removed'-part
                realPenaltyForRemoved = PENALTY_FOR_REMOVED;
            }
        }

        Token[] candTokens = tokenizeStem(realSource);

        // First percent value - with stemming if possible
        int similarityStem = FuzzyMatcher.calcSimilarity(distance, strTokensStem, candTokens);

        similarityStem -= penalty;
        if (fuzzy) {
            // penalty for fuzzy
            similarityStem -= PENALTY_FOR_FUZZY;
        }
        similarityStem -= realPenaltyForRemoved;

        // check if we have chance by first percentage only
        if (!haveChanceToAdd(similarityStem, Integer.MAX_VALUE, Integer.MAX_VALUE)) {
            return;
        }

        Token[] candTokensNoStem = tokenizeNoStem(realSource);
        // Second percent value - without stemming
        int similarityNoStem = FuzzyMatcher.calcSimilarity(distance, strTokensNoStem, candTokensNoStem);
        similarityNoStem -= penalty;
        if (fuzzy) {
            // penalty for fuzzy
            similarityNoStem -= PENALTY_FOR_FUZZY;
        }
        similarityNoStem -= realPenaltyForRemoved;

        // check if we have chance by first and second percentages
        if (!haveChanceToAdd(similarityStem, similarityNoStem, Integer.MAX_VALUE)) {
            return;
        }

        Token[] candTokensAll = tokenizeAll(realSource);
        // Third percent value - with numbers, tags, etc.
        int simAdjusted = FuzzyMatcher.calcSimilarity(distance, strTokensAll, candTokensAll);
        simAdjusted -= penalty;
        if (fuzzy) {
            // penalty for fuzzy
            simAdjusted -= PENALTY_FOR_FUZZY;
        }
        simAdjusted -= realPenaltyForRemoved;

        // check if we have chance by first, second and third percentages
        if (!haveChanceToAdd(similarityStem, similarityNoStem, simAdjusted)) {
            return;
        }

        addNearString(key, source, translation, comesFrom, fuzzy, similarityStem, similarityNoStem,
                simAdjusted, null, tmxName, creator, creationDate, changer, changedDate, props);
    }

    /**
     * Check if entry have a chance to be added to result list. If no, there is no sense to calculate other
     * parameters.
     * 
     * @param simStem
     *            similarity with stemming
     * @param simNoStem
     *            similarity without stemming
     * @param simExactly
     *            exactly similarity
     * @return true if we have chance
     */
    protected boolean haveChanceToAdd(final int simStem, final int simNoStem, final int simExactly) {
        if (simStem < OConsts.FUZZY_MATCH_THRESHOLD && simNoStem < OConsts.FUZZY_MATCH_THRESHOLD) {
            return false;
        }
        if (result.size() < maxCount) {
            return true;
        }
        NearString st = result.get(result.size() - 1);
        Boolean chanse = checkScore(st.scores[0].score, simStem);
        if (chanse == null) {
            chanse = checkScore(st.scores[0].scoreNoStem, simNoStem);
        }
        if (chanse == null) {
            chanse = checkScore(st.scores[0].adjustedScore, simExactly);
        }
        if (chanse == null) {
            chanse = true;
        }
        return chanse;
    }

    private Boolean checkScore(final int storedScore, final int checkedStore) {
        if (storedScore < checkedStore) {
            return true;
        } else if (storedScore > checkedStore) {
            return false;
        } else {
            return null;
        }
    }

    /**
     * Add near string into result list. Near strings sorted by "similarity,simAdjusted"
     */
    protected void addNearString(final EntryKey key, final String source, final String translation,
            NearString.MATCH_SOURCE comesFrom, final boolean fuzzy, final int similarity,
            final int similarityNoStem, final int simAdjusted, final byte[] similarityData,
            final String tmxName, final String creator, final long creationDate, final String changer,
            final long changedDate, final Map<String, String> tuProperties) {
        // find position for new data
        int pos = 0;
        for (int i = 0; i < result.size(); i++) {
            NearString st = result.get(i);
            if (source.equals(st.source)
                    && (translation == null && st.translation == null || translation != null
                            && translation.equals(st.translation))) {
                // Consolidate identical matches from different sources into a single NearString with
                // multiple project entries.
                result.set(i, NearString.merge(st, key, source, translation, comesFrom, fuzzy, similarity,
                        similarityNoStem, simAdjusted, similarityData, tmxName, creator, creationDate,
                        changer, changedDate, tuProperties));
                return;
            }
            if (st.scores[0].score < similarity) {
                break;
            }
            if (st.scores[0].score == similarity) {
                if (st.scores[0].scoreNoStem < similarityNoStem) {
                    break;
                }
                if (st.scores[0].scoreNoStem == similarityNoStem) {
                    if (st.scores[0].adjustedScore < simAdjusted) {
                        break;
                    }
                    // Patch contributed by Antonio Vilei
                    String entrySource = srcText;
                    // text with the same case has precedence
                    if (similarity == 100 && !st.source.equals(entrySource) && source.equals(entrySource)) {
                        break;
                    }
                }
            }
            pos = i + 1;
        }

        result.add(pos, new NearString(key, source, translation, comesFrom, fuzzy, similarity,
                similarityNoStem, simAdjusted, similarityData, tmxName, creator, creationDate, changer,
                changedDate, tuProperties));
        if (result.size() > maxCount) {
            result.remove(result.size() - 1);
        }
    }

    /*
     * Methods for tokenize strings with caching.
     */
    Map<String, Token[]> tokenizeStemCache = new HashMap<String, Token[]>();
    Map<String, Token[]> tokenizeNoStemCache = new HashMap<String, Token[]>();
    Map<String, Token[]> tokenizeAllCache = new HashMap<String, Token[]>();

    public Token[] tokenizeStem(String str) {
        Token[] result = tokenizeStemCache.get(str);
        if (result == null) {
            result = tok.tokenizeWords(str, ITokenizer.StemmingMode.MATCHING);
            tokenizeStemCache.put(str, result);
        }
        return result;
    }

    public Token[] tokenizeNoStem(String str) {
        Token[] result = tokenizeNoStemCache.get(str);
        if (result == null) {
            result = tok.tokenizeWords(str, ITokenizer.StemmingMode.NONE);
            tokenizeNoStemCache.put(str, result);
        }
        return result;
    }

    public Token[] tokenizeAll(String str) {
        Token[] result = tokenizeAllCache.get(str);
        if (result == null) {
            result = tok.tokenizeAllExactly(str);
            tokenizeAllCache.put(str, result);
        }
        return result;
    }

    protected void checkStopped(IStopped stop) throws StoppedException {
        if (stop.isStopped()) {
            throw new StoppedException();
        }
    }

    /**
     * Process will throw this exception if it stopped.All callers must catch it and just skip.
     */
    @SuppressWarnings("serial")
    public static class StoppedException extends RuntimeException {
    }
}
