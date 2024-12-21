/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Alex Buloichik
               2012 Thomas Cordonnier
               2013-2014 Aaron Madlon-Kay
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

package org.omegat.core.matching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ITMXEntry;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXProp;

/**
 * Class to hold a single fuzzy match.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Thomas Cordonnier
 * @author Aaron Madlon-Kay
 */
public class NearString {
    public enum MATCH_SOURCE {
        MEMORY, TM, FILES
    };

    public enum SORT_KEY {
        SCORE, SCORE_NO_STEM, ADJUSTED_SCORE
    }

    public NearString(EntryKey key, ITMXEntry entry, MATCH_SOURCE comesFrom, boolean fuzzyMark,
                      Scores scores, byte[] nearData, String projName) {
        this(key, entry.getSourceText(), entry.getTranslationText(), comesFrom, fuzzyMark, scores,
                nearData, projName, entry.getCreator(), entry.getCreationDate(), entry.getChanger(),
                entry.getChangeDate(), entry.getProperties());
    }

    /**
     * Constructor, backward compatible.
     * @param key entry key
     * @param source source text
     * @param translation translation text
     * @param comesFrom origin
     * @param fuzzyMark fuzzy or not
     * @param nearScore
     * @param nearScoreNoStem
     * @param adjustedScore
     * @param nearData similarity data.
     * @param projName project name.
     * @param creator creator name
     * @param creationDate creation date
     * @param changer changer name
     * @param changedDate changer date
     * @param props properties of entry.
     */
    @Deprecated
    public NearString(EntryKey key, String source, String translation, MATCH_SOURCE comesFrom,
            boolean fuzzyMark, int nearScore, int nearScoreNoStem, int adjustedScore,
            byte[] nearData, String projName, String creator, long creationDate,
            String changer, long changedDate, List<TMXProp> props) {
        this(key, source, translation, comesFrom, fuzzyMark, new Scores(nearScore, nearScoreNoStem,
                adjustedScore), nearData, projName, creator, creationDate, changer, changedDate, props);
    }

    private NearString(EntryKey key, String source, String translation, MATCH_SOURCE comesFrom,
                      boolean fuzzyMark, Scores scores, byte[] nearData, String projName, String creator,
                      long creationDate, String changer, long changedDate, List<TMXProp> props) {
        this.key = key;
        this.source = source;
        this.translation = translation;
        this.comesFrom = comesFrom;
        this.fuzzyMark = fuzzyMark;
        this.scores = new Scores[] { scores };
        this.attr = nearData;
        this.projs = new String[] { projName == null ? "" : projName };
        this.props = props;
        this.creator = creator;
        this.creationDate = creationDate;
        this.changer = changer;
        this.changedDate = changedDate;
    }

    /**
     * Merge NearString object.
     * @param ns NearString to merge.
     * @param key entry key.
     * @param entry TMXEntry entry
     * @param comesFrom origin
     * @param fuzzyMark fuzzy or not
     * @param scores similarity score
     * @param nearData similarity data
     * @param projName project name
     * @return NearString merged.
     */
    public static NearString merge(NearString ns, EntryKey key, ITMXEntry entry, MATCH_SOURCE comesFrom,
                                   boolean fuzzyMark, Scores scores, byte[] nearData, String projName) {

        List<String> projs = new ArrayList<>();
        List<Scores> mergedScores = new ArrayList<>();
        projs.addAll(Arrays.asList(ns.projs));
        mergedScores.addAll(Arrays.asList(ns.scores));

        NearString merged;
        if (scores.score > ns.scores[0].score) {
            merged = new NearString(key, entry, comesFrom, fuzzyMark, scores, nearData, null);
            projs.add(0, projName);
            mergedScores.add(0, merged.scores[0]);
        } else {
            merged = new NearString(ns.key, ns.source, ns.translation, ns.comesFrom, ns.fuzzyMark,
                    scores, ns.attr, null, ns.creator, ns.creationDate, ns.changer,
                    ns.changedDate, ns.props);
            projs.add(projName);
            mergedScores.add(merged.scores[0]);
        }
        merged.projs = projs.toArray(new String[projs.size()]);
        merged.scores = mergedScores.toArray(new Scores[mergedScores.size()]);
        return merged;
    }

    @Deprecated
    public static NearString merge(NearString ns, final EntryKey key, final String source, final String translation,
            MATCH_SOURCE comesFrom, final boolean fuzzyMark, final int nearScore, final int nearScoreNoStem,
            final int adjustedScore, final byte[] nearData, final String projName, final String creator,
            final long creationDate, final String changer, final long changedDate, final List<TMXProp> props) {

        List<String> projs = new ArrayList<>();
        List<Scores> mergedScores = new ArrayList<>();
        projs.addAll(Arrays.asList(ns.projs));
        mergedScores.addAll(Arrays.asList(ns.scores));

        NearString merged;
        if (nearScore > ns.scores[0].score) {
            merged = new NearString(key, source, translation, comesFrom, fuzzyMark, nearScore,
                    nearScoreNoStem, adjustedScore, nearData, null, creator, creationDate, changer, changedDate, props);
            projs.add(0, projName);
            mergedScores.add(0, merged.scores[0]);
        } else {
            merged = new NearString(ns.key, ns.source, ns.translation, ns.comesFrom, ns.fuzzyMark, nearScore,
                    nearScoreNoStem, adjustedScore, ns.attr, null, ns.creator, ns.creationDate, ns.changer,
                    ns.changedDate, ns.props);
            projs.add(projName);
            mergedScores.add(merged.scores[0]);
        }
        merged.projs = projs.toArray(new String[projs.size()]);
        merged.scores = mergedScores.toArray(new Scores[mergedScores.size()]);
        return merged;
    }

    @Override
    public String toString() {
        return String.join(" ", StringUtil.truncate(source, 20), scores[0].toString(), "x" + scores.length);
    }

    public EntryKey key;
    public String source;
    public String translation;
    public MATCH_SOURCE comesFrom;

    public boolean fuzzyMark;

    public Scores[] scores;
    public String[] projs;

    /** matching attributes of near strEntry */
    public byte[] attr;

    public final List<TMXProp> props;
    public final String creator;
    public final long creationDate;
    public final String changer;
    public final long changedDate;

    public static class Scores {
        public final int score;
        /** similarity score for match without non-word tokens */
        public final int scoreNoStem;
        /** adjusted similarity score for match including all tokens */
        public final int adjustedScore;
        /** penalty of the match */
        public final int penalty;

        public Scores(int score, int scoreNoStem, int adjustedScore) {
            this(score, scoreNoStem, adjustedScore, 0);
        }

        public Scores(int score, int scoreNoStem, int adjustedScore, int penalty) {
            this.score = score;
            this.scoreNoStem = scoreNoStem;
            this.adjustedScore = adjustedScore;
            this.penalty = penalty;
        }

        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append("(");
            b.append(score);
            b.append("/");
            b.append(scoreNoStem);
            b.append("/");
            b.append(adjustedScore);
            b.append("%)");
            return b.toString();
        }
    }

    public static class ScoresComparator implements Comparator<Scores> {

        private final SORT_KEY key;

        public ScoresComparator(SORT_KEY key) {
            this.key = key;
        }

        @Override
        public int compare(Scores o1, Scores o2) {
            int s1 = primaryScore(o1);
            int s2 = primaryScore(o2);
            if (s1 != s2) {
                return s1 > s2 ? 1 : -1;
            }
            s1 = secondaryScore(o1);
            s2 = secondaryScore(o2);
            if (s1 != s2) {
                return s1 > s2 ? 1 : -1;
            }
            s1 = ternaryScore(o1);
            s2 = ternaryScore(o2);
            if (s1 != s2) {
                return s1 > s2 ? 1 : -1;
            }
            return 0;
        }

        private int primaryScore(Scores s) {
            switch (key) {
            case SCORE:
                return s.score;
            case SCORE_NO_STEM:
                return s.scoreNoStem;
            case ADJUSTED_SCORE:
            default:
                return s.adjustedScore;
            }
        }

        private int secondaryScore(Scores s) {
            switch (key) {
            case SCORE:
                return s.scoreNoStem;
            case SCORE_NO_STEM:
                return s.score;
            case ADJUSTED_SCORE:
            default:
                return s.score;
            }
        }

        private int ternaryScore(Scores s) {
            switch (key) {
            case SCORE:
                return s.adjustedScore;
            case SCORE_NO_STEM:
                return s.adjustedScore;
            case ADJUSTED_SCORE:
            default:
                return s.scoreNoStem;
            }
        }
    }
}
