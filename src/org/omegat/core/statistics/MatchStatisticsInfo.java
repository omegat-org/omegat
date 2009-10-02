/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.core.statistics;

/**
 * Bean for store matches statistics info.
 * 
 * It required 7 rows to output:
 * "Exact match","100%","95% - 99%","85% - 94%","75% - 84%",50% - 74%","No
 * Match"
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class MatchStatisticsInfo {
    public final Row[] rows;

    public MatchStatisticsInfo() {
        rows = new Row[6];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = new Row();
        }
    }

    public static class Row {
        public int segments, words, charsWithoutSpaces, charsWithSpaces;
    }

    /**
     * Get row index by match percent.
     * 
     * @param percent
     *            match percent
     * @return row index
     */
    public int getRowByPercent(int percent) {
        if (percent == Integer.MAX_VALUE) {
            // exact match
            return 0;
        } else if (percent >= 95) {
            return 1;
        } else if (percent >= 85) {
            return 2;
        } else if (percent >= 75) {
            return 3;
        } else if (percent >= 50) {
            return 4;
        } else {
            return 5;
        }
    }
}
