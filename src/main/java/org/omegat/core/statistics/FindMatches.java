/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Alex Buloichik
               2012 Thomas Cordonnier, Martin Fleurke
               2013 Aaron Madlon-Kay, Alex Buloichik
               2024 Hiroshi Miura, Thomas Cordonnier
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

package org.omegat.core.statistics;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.Core;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ExternalTMFactory;
import org.omegat.core.data.ExternalTMX;
import org.omegat.core.data.IProject;
import org.omegat.core.data.ITMXEntry;
import org.omegat.core.data.PrepareTMXEntry;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.StringData;
import org.omegat.core.events.IStopped;
import org.omegat.core.matching.FuzzyMatcher;
import org.omegat.core.matching.ISimilarityCalculator;
import org.omegat.core.matching.LevenshteinDistance;
import org.omegat.core.matching.MatchEquivalence;
import org.omegat.core.matching.NearString;
import org.omegat.core.segmentation.Rule;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Language;
import org.omegat.util.NumeralValueParser;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
import org.omegat.util.Token;

/**
 * Class to find matches by specified criteria.
 * <p>
 * Since we can use stemmers to prepare tokens, we should use 3-pass comparison
 * of similarity. Similarity will be calculated in 3 steps:
 * <ol>
 * <li>Split the original segment into word-only tokens using stemmer (with stop
 * words list), then compare tokens.</li>
 * <li>Split the original segment into word-only tokens without a stemmer,
 * then compare tokens.</li>
 * <li>Split the original segment into not-only-words tokens (including numbers
 * and tags) without a stemmer, then compare tokens.</li>
 * </ol>
 * <p>
 * This class is not thread safe! Must be used in the one thread only.
 *
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 * @author Aaron Madlon-Kay
 */
public class FindMatches {

    /**
     * According to gettext source code, PO fuzzy entries are created above 60%
     * <a href=
     * "https://sourceforge.net/p/omegat/feature-requests/1258/">RFE#1258</a>
     */
    static final int PENALTY_FOR_FUZZY = 40;
    private static final int PENALTY_FOR_REMOVED = 5;
    /**
     * Number-only segments have no word tokens, so their first two similarity
     * passes would always score 0. With the match-numbers project option they
     * are compared with numbers as a placeholder class instead, minus this
     * penalty (feature request #465).
     */
    private static final int PENALTY_FOR_NUMBER_ONLY = 10;
    private static final int SUBSEGMENT_MATCH_THRESHOLD = 85;

    private static final Pattern SEARCH_FOR_PENALTY = Pattern.compile("penalty-(\\d+)");

    private static final String ORPHANED_FILE_NAME = OStrings.getString("CT_ORPHAN_STRINGS");

    private final ISimilarityCalculator distance = new LevenshteinDistance();

    /**
     * the removePattern that was configured by the user.
     */
    private final Pattern removePattern = PatternConsts.getRemovePattern();

    private final IProject project;
    private final ITokenizer tok;
    private final Locale srcLocale;
    private final int maxCount;

    /** Result list. */
    private List<NearString> result;

    private final boolean searchExactlyTheSame;
    private String srcText;

    /**
     * Text that was removed by the removePattern from the source text.
     */
    private String removedText;

    /** Tokens for original string, with and without stems. */
    private Token[] strTokensStem;
    private Token[] strTokensNoStem;

    /** Tokens for original string, includes numbers and tags. */
    private Token[] strTokensAll;

    private final int fuzzyMatchThreshold;

    /** Consider numbers by value during matching (project option, #465). */
    private final boolean matchNumbers;

    /**
     * Replacement map of the character equivalence classes active in this
     * project (#1681); character variants fold together before comparison.
     */
    private final Map<Integer, String> equivalenceFoldMap;

    /**
     * Length-preserving subset of the fold map, applied to whole strings
     * before tokenization so variant characters cannot shift word boundaries
     * (straight and curly apostrophes segment differently).
     */
    private final Map<Integer, String> equivalenceSameLengthFoldMap;

    private final Segmenter segmenter;

    /**
     * Constructs a FindMatches instance for finding fuzzy matched translation memories.
     *
     * @param project
     *        OmegaT project.
     * @param maxCount
     *        Limits the maximum count of the results.
     * @param allowSeparateSegmentMatch
     *        Specifies whether to allow separate segment matching.
     * @param searchExactlyTheSame
     *        Allows searching for similarities with the exact same text as the source segment.
     *        This mode is used specifically for separate sentence match in paragraph-based projects,
     *        where the source is part of the current segment.
     * @deprecated
     */
    @Deprecated(since = "6.1.0")
    public FindMatches(IProject project, int maxCount, boolean allowSeparateSegmentMatch,
            boolean searchExactlyTheSame) {
        this(project, Core.getSegmenter(), maxCount, searchExactlyTheSame, Preferences.getPreferenceDefault(
                Preferences.EXT_TMX_FUZZY_MATCH_THRESHOLD, OConsts.FUZZY_MATCH_THRESHOLD));
    }

    /**
     * FindMatches find fuzzy matched translation memories.
     *
     * @param project
     *            OmegaT project.
     * @param segmenter
     *            used when running a segmentation search.
     * @param maxCount
     *            limit the maximum count of the results.
     * @param searchExactlyTheSame
     *            allows searching similarities with the same text as a source
     *            segment. This mode is used only for separate sentence match
     *            in a paragraph project, i.e., where a source is just part of
     *            the current source.
     * @param threshold
     *            threshold to use.
     */
    public FindMatches(IProject project, Segmenter segmenter, int maxCount,
            boolean searchExactlyTheSame, int threshold) {
        this.project = project;
        this.segmenter = segmenter;
        this.tok = project.getSourceTokenizer();
        this.srcLocale = project.getProjectProperties().getSourceLanguage().getLocale();
        this.maxCount = maxCount;
        this.searchExactlyTheSame = searchExactlyTheSame;
        this.fuzzyMatchThreshold = threshold;
        this.matchNumbers = project.getProjectProperties().isMatchNumbersEnabled();
        Set<MatchEquivalence> activeEquivalences = project.getProjectProperties()
                .getActiveMatchEquivalences();
        this.equivalenceFoldMap = MatchEquivalence.buildFoldMap(activeEquivalences);
        this.equivalenceSameLengthFoldMap = MatchEquivalence.buildSameLengthFoldMap(activeEquivalences);
    }

    /**
     * Searches for translation matches in the translation memory.
     *
     * @param searchText
     *        The target segment or term to search for.
     * @param requiresTranslation
     *        Indicates whether only translations are required during the search.
     * @param fillSimilarityData
     *        Specifies whether similarity data should be included in the resulting
     *        NearString objects.
     * @param stop
     *        The IStopped callback object to handle cancellation of the search
     *        process.
     * @return
     *        A list of NearString objects representing the matched translation
     *        entries.
     * @throws StoppedException
     *        Thrown if the search process is stopped by the IStopped callback.
     * @deprecated
     */
    @Deprecated(since = "6.1.0")
    public List<NearString> search(final String searchText, final boolean requiresTranslation,
            final boolean fillSimilarityData, final IStopped stop) throws StoppedException {
        return search(searchText, fillSimilarityData, stop);
    }

    /**
     * Search Translation memories.
     *
     * @param searchText
     *        The target segment or term to search for.
     * @param fillSimilarityData
     *        Specifies whether similarity data should be filled into the result
     *        of NearString objects.
     * @param stop
     *        IStopped callback object to indicate cancel operation.
     * @return
     *        List of NearString objects, which hold matched translation entry.
     * @throws StoppedException
     *        raised when stopped during a search process.
     */
    public List<NearString> search(String searchText, boolean fillSimilarityData, IStopped stop)
            throws StoppedException {
        return search(searchText, fillSimilarityData, stop,
                Preferences.isPreferenceDefault(Preferences.PARAGRAPH_MATCH_FROM_SEGMENT_TMX, true)
                        && !project.getProjectProperties().isSentenceSegmentingEnabled());
    }

    /**
     * Search Translation memories.
     * <p>
     * Internal method to handle search conditions.
     * It is accessible as package-private for testing.
     *
     * @param searchText
     *        target segment or term to search.
     * @param fillSimilarityData
     *        fill similarity data into the result of NearString objects.
     * @param stop
     *        IStopped callback object to indicate cancel operation.
     * @param runSeparateSegmentMatch
     *        Also search with segmented terms search.
     * @return
     *        List of NearString objects.
     * @throws StoppedException
     *        When stopped the process during search.
     */
    List<NearString> search(String searchText, boolean fillSimilarityData, IStopped stop,
                            boolean runSeparateSegmentMatch) throws StoppedException {
        result = new ArrayList<>(OConsts.MAX_NEAR_STRINGS + 1);
        srcText = searchText;
        removedText = "";
        // remove part that is to be removed according to user settings.
        // Rationale: it might be a big string influencing the 'editing
        // distance', while it is not really part
        // of the translatable text
        if (removePattern != null) {
            StringBuilder removedBuffer = new StringBuilder();
            Matcher removeMatcher = removePattern.matcher(srcText);
            while (removeMatcher.find()) {
                removedBuffer.append(removeMatcher.group());
            }
            srcText = removeMatcher.replaceAll("");
            removedText = removedBuffer.toString();
        }
        // get tokens for original string which includes non-word tokens
        strTokensStem = tokenizeStem(srcText);
        strTokensNoStem = tokenizeNoStem(srcText);
        strTokensAll = tokenizeAll(srcText);

        // travel by project entries, including orphaned
        if (project.getProjectProperties().isSupportDefaultTranslations()) {
            project.iterateByDefaultTranslations((source, trans) -> {
                checkStopped(stop);
                if (!searchExactlyTheSame && source.equals(searchText)) {
                    // skip original==original entry comparison
                    return;
                }
                if (trans.translation == null) {
                    return;
                }
                String fileName = project.isOrphaned(source) ? ORPHANED_FILE_NAME : null;
                PrepareTMXEntry entry = new PrepareTMXEntry(trans);
                entry.source = source;
                processEntry(null, entry, fileName, NearString.MATCH_SOURCE.MEMORY, false, 0);
            });
        }
        project.iterateByMultipleTranslations((source, trans) -> {
            checkStopped(stop);
            if (!searchExactlyTheSame && source.sourceText.equals(searchText)) {
                // skip original==original entry comparison
                return;
            }
            if (trans.translation == null) {
                return;
            }
            String fileName = project.isOrphaned(source) ? ORPHANED_FILE_NAME : null;
            PrepareTMXEntry entry = new PrepareTMXEntry(trans);
            entry.source = source.sourceText;
            processEntry(source, entry, fileName, NearString.MATCH_SOURCE.MEMORY, false, 0);
        });
        /*
         * Penalty applied for fuzzy matches in another language (if no match in
         * the target language was found).
         */
        int foreignPenalty = Preferences.getPreferenceDefault(Preferences.PENALTY_FOR_FOREIGN_MATCHES,
                Preferences.PENALTY_FOR_FOREIGN_MATCHES_DEFAULT);
        for (Map.Entry<String, ExternalTMX> en : project.getTransMemories().entrySet()) {
            int penalty = 0;
            Matcher matcher = SEARCH_FOR_PENALTY.matcher(en.getKey());
            if (matcher.find()) {
                penalty = Integer.parseInt(matcher.group(1));
            }
            for (ITMXEntry tmen : en.getValue().getEntries()) {
                checkStopped(stop);
                if (tmen.getSourceText() == null) {
                    // Not all TMX entries have a source; skip it in
                    // the case, because of no meaningful.
                    continue;
                }
                if (tmen.getTranslationText() == null) {
                    continue;
                }
                int tmenPenalty = penalty;
                if (tmen.hasPropValue(ExternalTMFactory.TMXLoader.PROP_FOREIGN_MATCH, "true")) {
                    tmenPenalty += foreignPenalty;
                }
                processEntry(null, tmen, en.getKey(), NearString.MATCH_SOURCE.TM, false, tmenPenalty);
            }
        }

        // travel by all entries for check source file translations
        for (SourceTextEntry ste : project.getAllEntries()) {
            checkStopped(stop);
            if (ste.getSourceTranslation() != null) {
                PrepareTMXEntry entry = new PrepareTMXEntry();
                entry.source = ste.getSrcText();
                entry.translation = ste.getSourceTranslation();
                processEntry(ste.getKey(), entry, ste.getKey().file, NearString.MATCH_SOURCE.FILES,
                        ste.isSourceTranslationFuzzy(), 0);
            }
        }
        if (runSeparateSegmentMatch) {
            FindMatches separateSegmentMatcher = new FindMatches(project, segmenter, 1, true,
                    fuzzyMatchThreshold);
            // split paragraph even when segmentation disabled, then find
            // matches for every segment
            List<StringBuilder> spaces = new ArrayList<>();
            List<Rule> brules = new ArrayList<>();
            Language sourceLang = project.getProjectProperties().getSourceLanguage();
            Language targetLang = project.getProjectProperties().getTargetLanguage();
            List<String> segments = segmenter.segment(sourceLang, srcText, spaces, brules);
            if (segments.size() > 1) {
                Set<String> tmxNames = new HashSet<>();
                List<String> fsrc = new ArrayList<>(segments.size());
                List<String> ftrans = new ArrayList<>(segments.size());
                int maxPenalty = 0;
                // multiple segments
                for (String onesrc : segments) {
                    // find match for a separate segment.
                    // WARN: the 5th argument should be
                    // `false` to avoid an infinite-loop.
                    List<NearString> segmentMatch = separateSegmentMatcher.search(onesrc, false, stop, false);
                    if (!segmentMatch.isEmpty()
                            && segmentMatch.get(0).scores[0].score >= SUBSEGMENT_MATCH_THRESHOLD) {
                        fsrc.add(segmentMatch.get(0).source);
                        ftrans.add(segmentMatch.get(0).translation);
                        segmentMatch.stream().filter(match -> !match.projs[0].isEmpty())
                                .map(match -> match.projs[0]).forEach(tmxNames::add);
                        if (segmentMatch.get(0).fuzzyMark) {
                            if (maxPenalty < PENALTY_FOR_FUZZY) {
                                maxPenalty = PENALTY_FOR_FUZZY;
                            }
                        }
                        maxPenalty = Math.max(maxPenalty, segmentMatch.get(0).scores[0].penalty);
                    } else {
                        fsrc.add("");
                        ftrans.add("");
                    }
                }
                // glue found sources and translations
                PrepareTMXEntry entry = new PrepareTMXEntry();
                entry.source = segmenter.glue(sourceLang, sourceLang, fsrc, spaces, brules);
                entry.translation = segmenter.glue(sourceLang, targetLang, ftrans, spaces, brules);
                processEntry(null, entry, String.join(",", tmxNames), NearString.MATCH_SOURCE.SUBSEGMENTS,
                        false, maxPenalty);
            }
        }
        // fill similarity data only for a result
        if (fillSimilarityData) {
            for (NearString near : result) {
                near.attr = buildDisplaySimilarityData(near.source);
            }
        }
        return result;
    }

    /**
     * Compare one entry with the original entry.
     *
     * @param key
     *            entry to compare
     * @param entry
     *            PrepareTMXEntry entry to process.
     * @param comesFrom
     *            match source
     * @param fuzzy
     *            is it fuzzy or not
     * @param penalty
     *            penalty score
     * @param tmxName
     *            tmx name
     */
    public void processEntry(EntryKey key, ITMXEntry entry, String tmxName,
                              NearString.MATCH_SOURCE comesFrom, boolean fuzzy, int penalty) {
        // remove part that is to be removed prior to tokenize
        String realSource = entry.getSourceText();
        int realPenaltyForRemoved = 0;
        if (removePattern != null) {
            StringBuilder entryRemovedText = new StringBuilder();
            Matcher removeMatcher = removePattern.matcher(realSource);
            while (removeMatcher.find()) {
                entryRemovedText.append(removeMatcher.group());
            }
            realSource = removeMatcher.replaceAll("");
            // calculate penalty if something has been removed, otherwise
            // different strings get 100% match.
            if (!entryRemovedText.toString().equals(removedText)) {
                // penalty for different 'removed'-part
                realPenaltyForRemoved = PENALTY_FOR_REMOVED;
            }
        }

        Token[] candTokens = tokenizeStem(realSource);

        // First percent value - with stemming if possible
        int similarityStem = FuzzyMatcher.calcSimilarity(distance, strTokensStem, candTokens);

        // Both sides without word tokens means two number-only segments; the
        // word passes then carry no signal, so compare with numbers as a
        // placeholder class instead of letting the score stay 0 (#465).
        int numberOnlySimilarity = -1;
        if (matchNumbers && strTokensStem.length == 0 && candTokens.length == 0
                && strTokensAll.length > 0) {
            numberOnlySimilarity = Math.max(0, FuzzyMatcher.calcSimilarity(distance,
                    tokenizeAllPlaceholder(srcText), tokenizeAllPlaceholder(realSource))
                    - PENALTY_FOR_NUMBER_ONLY);
            similarityStem = numberOnlySimilarity;
        }

        similarityStem -= penalty;
        if (fuzzy) {
            // penalty for fuzzy
            similarityStem -= PENALTY_FOR_FUZZY;
        }
        similarityStem -= realPenaltyForRemoved;

        // check if we have a chance by first percentage only
        if (noChanceToAdd(similarityStem, Integer.MAX_VALUE, Integer.MAX_VALUE)) {
            return;
        }

        Token[] candTokensNoStem = tokenizeNoStem(realSource);
        // Second percent value - without stemming
        int similarityNoStem = FuzzyMatcher.calcSimilarity(distance, strTokensNoStem, candTokensNoStem);
        if (numberOnlySimilarity >= 0 && strTokensNoStem.length == 0 && candTokensNoStem.length == 0) {
            similarityNoStem = numberOnlySimilarity;
        }
        similarityNoStem -= penalty;
        if (fuzzy) {
            // penalty for fuzzy
            similarityNoStem -= PENALTY_FOR_FUZZY;
        }
        similarityNoStem -= realPenaltyForRemoved;

        // check if we have a chance by first and second percentages
        if (noChanceToAdd(similarityStem, similarityNoStem, Integer.MAX_VALUE)) {
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
        if (noChanceToAdd(similarityStem, similarityNoStem, simAdjusted)) {
            return;
        }

        // BUGS#1236 - stat display does not use threshold config check
        if (fuzzyMatchThreshold > 0 && similarityStem < fuzzyMatchThreshold
                && similarityNoStem < fuzzyMatchThreshold && simAdjusted < fuzzyMatchThreshold) {
            return;
        }

        addNearString(key, entry, comesFrom, fuzzy,
                new NearString.Scores(similarityStem, similarityNoStem, simAdjusted, penalty), tmxName);
    }

    /**
     * Check if entries have a chance to be added to a result list. If true,
     * there is no sense to calculate other parameters.
     *
     * @param simStem
     *            similarity with stemming
     * @param simNoStem
     *            similarity without stemming
     * @param simExactly
     *            exactly similarity
     * @return true if we have no chance.
     */
    private boolean noChanceToAdd(int simStem, int simNoStem, int simExactly) {
        if (result.size() < maxCount) {
            return false;
        }
        NearString st = result.get(result.size() - 1);
        int chance = Integer.compare(st.scores[0].score, simStem);
        if (chance == 0) {
            chance = Integer.compare(st.scores[0].scoreNoStem, simNoStem);
        }
        if (chance == 0) {
            chance = Integer.compare(st.scores[0].adjustedScore, simExactly);
        }
        return chance == 1;
    }

    /**
     * Add near string into the result list. Near strings sorted by "similarity,
     * simAdjusted"
     */
    private void addNearString(EntryKey key, ITMXEntry entry, NearString.MATCH_SOURCE comesFrom, boolean fuzzy,
                               NearString.Scores scores, String tmxName) {
        final String source = entry.getSourceText();
        final String translation = entry.getTranslationText();
        // find position for new data
        int pos = 0;
        for (int i = 0; i < result.size(); i++) {
            NearString st = result.get(i);
            if (source.equals(st.source) && Objects.equals(translation, st.translation)) {
                // Consolidate identical matches from different sources into a
                // single NearString with multiple project entries.
                result.set(i, NearString.merge(st, key, entry, comesFrom, fuzzy, scores, null, tmxName));
                return;
            }
            if (st.scores[0].score < scores.score) {
                break;
            }
            if (st.scores[0].score == scores.score) {
                if (st.scores[0].scoreNoStem < scores.scoreNoStem) {
                    break;
                }
                if (st.scores[0].scoreNoStem == scores.scoreNoStem) {
                    if (st.scores[0].adjustedScore < scores.adjustedScore) {
                        break;
                    }
                    // Patch contributed by Antonio Vilei
                    // text with the same case has precedence
                    if (scores.score == 100 && !st.source.equals(srcText) && source.equals(srcText)) {
                        break;
                    }
                }
            }
            pos = i + 1;
        }
        result.add(pos, new NearString(key, entry, comesFrom, fuzzy, scores, null, tmxName));
        if (result.size() > maxCount) {
            result.remove(result.size() - 1);
        }
    }

    /*
     * Methods for tokenize strings with caching.
     */
    Map<String, Token[]> tokenizeStemCache = new HashMap<>();
    Map<String, Token[]> tokenizeNoStemCache = new HashMap<>();
    Map<String, Token[]> tokenizeAllCache = new HashMap<>();
    Map<String, Token[]> tokenizeAllPlaceholderCache = new HashMap<>();

    Token[] tokenizeStem(String str) {
        // Word-token passes compare token text only, so folding the whole
        // string before tokenization is safe and also aligns token boundaries
        // (an apostrophe variant can change word segmentation).
        str = foldEquivalence(str);
        Token[] tokens = tokenizeStemCache.get(str);
        if (tokens == null) {
            if (Preferences.isPreference(Preferences.MATCHES_STEMMING_FULL)) {
                tokens = tok.tokenizeWords(str, ITokenizer.StemmingMode.MATCHING_FULL);
            } else {
                tokens = tok.tokenizeWords(str, ITokenizer.StemmingMode.MATCHING);
            }
            tokenizeStemCache.put(str, tokens);
        }
        return tokens;
    }

    Token[] tokenizeNoStem(String str) {
        // No-stemming token comparisons are intentionally case-insensitive
        // for matching purposes.
        str = foldEquivalence(str.toLowerCase(srcLocale));
        Token[] tokens = tokenizeNoStemCache.get(str);
        if (tokens == null) {
            tokens = tok.tokenizeWords(str, ITokenizer.StemmingMode.NONE);
            tokenizeNoStemCache.put(str, tokens);
        }
        return tokens;
    }

    Token[] tokenizeAll(String str) {
        if (matchNumbers) {
            // Tokenize the original-case string: the Roman numeral gate needs
            // the case; non-number tokens are lowercased token by token below.
            Token[] tokens = tokenizeAllCache.get(str);
            if (tokens == null) {
                tokens = mapNumberTokens(str, false);
                tokenizeAllCache.put(str, tokens);
            }
            return tokens;
        }
        // Verbatim token comparisons are intentionally case-insensitive.
        // for matching purposes.
        str = str.toLowerCase(srcLocale);
        Token[] tokens = tokenizeAllCache.get(str);
        if (tokens == null) {
            String folded = MatchEquivalence.foldSameLength(str, equivalenceSameLengthFoldMap);
            tokens = foldTokens(tok.tokenizeVerbatim(folded), folded);
            tokenizeAllCache.put(str, tokens);
        }
        return tokens;
    }

    /**
     * Similarity data for match-pane highlighting. The pane pairs the flags
     * with a verbatim tokenization of the raw match source, while similarity
     * is computed on the folded comparison tokens, whose boundaries can
     * differ. The length-preserving fold keeps both tokenizations in the same
     * string coordinates, so the folded flags project onto the raw tokens by
     * offset overlap.
     */
    private byte[] buildDisplaySimilarityData(String matchSource) {
        Token[] foldedTokens = tokenizeAll(matchSource);
        byte[] foldedFlags = FuzzyMatcher.buildSimilarityData(strTokensAll, foldedTokens);
        Token[] rawTokens = tok.tokenizeVerbatim(matchSource);
        byte[] rawFlags = new byte[rawTokens.length];
        for (int i = 0; i < rawTokens.length; i++) {
            int rawStart = rawTokens[i].getOffset();
            int rawEnd = rawStart + rawTokens[i].getLength();
            byte flags = 0;
            for (int j = 0; j < foldedTokens.length; j++) {
                int foldedStart = foldedTokens[j].getOffset();
                if (foldedStart < rawEnd && rawStart < foldedStart + foldedTokens[j].getLength()) {
                    if ((foldedFlags[j] & StringData.UNIQ) != 0) {
                        flags = StringData.UNIQ;
                        break;
                    }
                    if ((foldedFlags[j] & StringData.PAIR) != 0) {
                        flags = StringData.PAIR;
                    }
                }
            }
            rawFlags[i] = flags;
        }
        return rawFlags;
    }

    private String foldEquivalence(String str) {
        return MatchEquivalence.fold(str, equivalenceFoldMap);
    }

    /**
     * Verbatim tokens with equivalence folding applied to the token text;
     * tokens folded to nothing (invisible formatting characters) are dropped.
     * Offsets and lengths keep referring to the original string, so similarity
     * data built from these tokens highlights the right characters.
     */
    private Token[] foldTokens(Token[] tokens, String str) {
        List<Token> kept = new ArrayList<>(tokens.length);
        for (Token token : tokens) {
            String text = token.getTextFromString(str);
            String folded = foldEquivalence(text);
            if (folded.isEmpty()) {
                continue;
            }
            kept.add(folded.equals(text) ? token
                    : new Token(folded, token.getOffset(), token.getLength()));
        }
        return kept.toArray(new Token[0]);
    }

    Token[] tokenizeAllPlaceholder(String str) {
        Token[] tokens = tokenizeAllPlaceholderCache.get(str);
        if (tokens == null) {
            tokens = mapNumberTokens(str, true);
            tokenizeAllPlaceholderCache.put(str, tokens);
        }
        return tokens;
    }

    /**
     * Verbatim tokens with every number token replaced: by its numeric value
     * (so differently written but equal numbers compare as equal, "XII" and
     * "12"), or by one shared placeholder (so any two numbers compare as
     * equal). Other tokens are lowercased as in the plain verbatim pass.
     */
    private Token[] mapNumberTokens(String str, boolean placeholder) {
        str = MatchEquivalence.foldSameLength(str, equivalenceSameLengthFoldMap);
        Token[] tokens = tok.tokenizeVerbatim(str);
        List<Token> mapped = new ArrayList<>(tokens.length);
        for (Token token : tokens) {
            String text = token.getTextFromString(str);
            // Roman numerals count here: a word misread as one only shifts the
            // similarity a little, and reading them is the point of the option.
            BigInteger value = NumeralValueParser.parseTokenWhole(text, true).orElse(null);
            String mappedText;
            if (value != null) {
                mappedText = placeholder ? "\0#" : "\0#" + value;
            } else {
                mappedText = foldEquivalence(text.toLowerCase(srcLocale));
                if (mappedText.isEmpty()) {
                    continue;
                }
            }
            mapped.add(new Token(mappedText, token.getOffset(), token.getLength()));
        }
        return mapped.toArray(new Token[0]);
    }

    private void checkStopped(IStopped stop) throws StoppedException {
        if (stop.isStopped()) {
            throw new StoppedException();
        }
    }

    /**
     * The Process will throw this exception if it stopped. All callers must
     * catch it and just skip.
     */
    @SuppressWarnings("serial")
    public static class StoppedException extends RuntimeException {
    }
}
