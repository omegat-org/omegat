/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Alex Buloichik
               2012 Thomas Cordonnier
               2013-2014 Aaron Madlon-Kay
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

package org.omegat.core.matching;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.omegat.core.data.EntryKey;
import org.omegat.util.Preferences;
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

    public NearString(final EntryKey key, final String source, final String translation, MATCH_SOURCE comesFrom,
            final boolean fuzzyMark, final int nearScore, final int nearScoreNoStem, final int adjustedScore,
            final byte[] nearData, final String projName, final String creator, final long creationDate,
            final String changer, final long changedDate, final List<TMXProp> props) {
        this.key = key;
        this.source = source;
        this.translation = translation;
        this.comesFrom = comesFrom;
        this.fuzzyMark = fuzzyMark;
        this.scores = new Scores[] { new Scores(nearScore, nearScoreNoStem, adjustedScore) };
        this.attr = nearData;
        this.projs = new String[] { projName == null ? "" : projName };
        this.props = props;
        this.creator = creator;
        this.creationDate = creationDate;
        this.changer = changer;
        this.changedDate = changedDate;
    }
    
    public static NearString merge(NearString ns, final EntryKey key, final String source, final String translation,
            MATCH_SOURCE comesFrom, final boolean fuzzyMark, final int nearScore, final int nearScoreNoStem,
            final int adjustedScore, final byte[] nearData, final String projName, final String creator,
            final long creationDate, final String changer, final long changedDate, final List<TMXProp> props) {
        
        List<String> projs = new ArrayList<String>();
        List<Scores> scores = new ArrayList<Scores>();
        for (String p : ns.projs) {
            projs.add(p);
        }
        for (Scores s : ns.scores) {
            scores.add(s);
        }
        
        if (nearScore > ns.scores[0].score) {
            projs.add(0, projName);
            NearString merged = new NearString(key, source, translation, comesFrom, fuzzyMark, nearScore, nearScoreNoStem,
                    adjustedScore, nearData, null, creator, creationDate, changer, changedDate, props);
            scores.add(0, merged.scores[0]);
            merged.projs = projs.toArray(new String[projs.size()]);
            merged.scores = scores.toArray(new Scores[scores.size()]);
            return merged;
        } else {
            projs.add(projName);
            scores.add(new Scores(nearScore, nearScoreNoStem, adjustedScore));
            ns.projs = projs.toArray(new String[projs.size()]);
            ns.scores = scores.toArray(new Scores[scores.size()]);
            return ns;
        }
    }

    public EntryKey key;
    public String source;
    public String translation;
    public MATCH_SOURCE comesFrom;
    
    public boolean fuzzyMark;

    public Scores[] scores;

    /** matching attributes of near strEntry */
    public byte[] attr;
    public String[] projs;
    public List<TMXProp> props;
    public String creator;
    public long creationDate;
    public String changer;
    public long changedDate;

    public static class Scores {
        public final int score;
        /** similarity score for match without non-word tokens */
        public final int scoreNoStem;
        /** adjusted similarity score for match including all tokens */
        public final int adjustedScore;
        
        public Scores(int score, int scoreNoStem, int adjustedScore) {
            this.score = score;
            this.scoreNoStem = scoreNoStem;
            this.adjustedScore = adjustedScore;
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
        
        public ScoresComparator() {
            this.key = Preferences.getPreferenceEnumDefault(Preferences.EXT_TMX_SORT_KEY, SORT_KEY.SCORE);
        }
        
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
            switch(key) {
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
            switch(key) {
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
            switch(key) {
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
    
    public static class NearStringComparator implements Comparator<NearString> {
        
        private final SORT_KEY key;
        private final ScoresComparator c;
        
        public NearStringComparator() {
            this.key = Preferences.getPreferenceEnumDefault(Preferences.EXT_TMX_SORT_KEY, SORT_KEY.SCORE);
            this.c = new ScoresComparator(key);
        }
        
        public NearStringComparator(SORT_KEY key) {
            this.key = key;
            this.c = new ScoresComparator(key);
        }
        
        @Override
        public int compare(NearString o1, NearString o2) {
            return c.compare(o1.scores[0], o2.scores[0]);
        }
    }
}
