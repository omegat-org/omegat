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

import java.util.List;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.matching.ISimilarityCalculator;
import org.omegat.core.matching.LevenshteinDistance;
import org.omegat.core.threads.LongProcessThread;

/**
 * Thread for calculate match statistics.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class CalcMatchStatistics extends LongProcessThread {
    private Callback callback;

    public CalcMatchStatistics(Callback callback) {
        this.callback = callback;
    }

    public void run() {
        ISimilarityCalculator distanceCalculator = new LevenshteinDistance();
        MatchStatisticsInfo result = new MatchStatisticsInfo();
        List<SourceTextEntry> allEntries = Core.getProject().getAllEntries();

        // We should iterate all segments from all files in project.
        int percent = 0;
        for (int i = 0; i < allEntries.size(); i++) {
            SourceTextEntry ste = allEntries.get(i);
            int p = Statistics.getMaxSimilarityPercent(ste, distanceCalculator,
                    allEntries);
            int r = result.getRowByPercent(p);
            result.rows[r].segments++;
            result.rows[r].words += Statistics.numberOfWords(ste.getSrcText());
            if (isStopped) {
                return;
            }
            int newPercent = i * 100 / allEntries.size();
            if (percent != newPercent) {
                callback.showProgress(newPercent);
                percent = newPercent;
            }
        }
        callback.displayData(result);
    }

    public interface Callback {
        void displayData(MatchStatisticsInfo result);

        void showProgress(int percent);
    }
}
