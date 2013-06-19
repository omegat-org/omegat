/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Alex Buloichik
               2012 Thomas Cordonnier
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

package org.omegat.core.matching;

import org.omegat.core.data.EntryKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public NearString(final EntryKey key, final String source, final String translation, MATCH_SOURCE comesFrom,
            final boolean fuzzyMark, final int nearScore, final int nearScoreNoStem, final int adjustedScore,
            final byte[] nearData, final String projName, final String creator, final long creationDate,
            final String changer, final long changedDate, final Map<String, String> props) {
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
            final long creationDate, final String changer, final long changedDate, final Map<String, String> props) {
        
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
            merged.projs = projs.toArray(new String[0]);
            merged.scores = scores.toArray(new Scores[0]);
            return merged;
        } else {
            projs.add(projName);
            scores.add(new Scores(nearScore, nearScoreNoStem, adjustedScore));
            ns.projs = projs.toArray(new String[0]);
            ns.scores = scores.toArray(new Scores[0]);
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
    public Map<String,String> props;
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
}
