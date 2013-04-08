/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Alex Buloichik
               2012 Thomas Cordonnier
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

package org.omegat.core.matching;

import org.omegat.core.data.EntryKey;

import java.util.Map;

/**
 * Class to hold a single fuzzy match.
 * 
 * @author Keith Godfrey
 */
public class NearString {
    public enum MATCH_SOURCE {
        MEMORY, TM, FILES
    };

    public NearString(final EntryKey key, final String source, final String translation, MATCH_SOURCE comesFrom,
            final boolean fuzzyMark, final int nearScore, final int nearScoreNoStem, final int adjustedScore,
            final byte[] nearData, final String projName, final String creator, final long creationDate,
            final Map<String, String> props) {
        this.key = key;
        this.source = source;
        this.translation = translation;
        this.comesFrom = comesFrom;
        this.fuzzyMark = fuzzyMark;
        score = nearScore;
        scoreNoStem = nearScoreNoStem;
        this.adjustedScore = adjustedScore;
        attr = nearData;
        if (projName != null)
            proj = projName;
        this.props = props;
        this.creator = creator;
        this.creationDate = creationDate;
    }

    public EntryKey key;
    public String source;
    public String translation;
    public MATCH_SOURCE comesFrom;
    
    public boolean fuzzyMark;

    public int score;

    /** similarity score for match without non-word tokens */
    public int scoreNoStem;

    /** adjusted similarity score for match including all tokens */
    public int adjustedScore;

    /** matching attributes of near strEntry */
    public byte[] attr;
    public String proj = "";
    public Map<String,String> props;
    public String creator;
    public long creationDate;	
}
