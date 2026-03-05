/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2010 Arno Peters
               2013-2014 Alex Buloichik
               2015 Aaron Madlon-Kay
               2020 Vladimir Bychkov
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

import java.io.File;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.core.threads.LongProcessThread;
import org.omegat.gui.stat.StatisticsPanel;
import org.omegat.util.OConsts;

/**
 * Thread for calculate standard statistics.
 *
 * Calculation requires two different tags stripping: one for calculate unique
 * and remaining, and second for calculate number of words and chars.
 *
 * Number of words/chars calculation requires to just strip all tags, protected
 * parts, placeholders(see StatCount.java).
 *
 * Calculation of unique and remaining also requires to just strip all tags,
 * protected parts, placeholders for standard calculation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Arno Peters
 * @author Aaron Madlon-Kay
 */
public class CalcStandardStatistics extends LongProcessThread {

    private final StatisticsPanel callback;

    public CalcStandardStatistics(StatisticsPanel callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        IProject p = Core.getProject();
        StatsResult result = Statistics.buildProjectStats(p);
        callback.setProjectTableData(StatsResult.HT_HEADERS, result.getHeaderTable());
        callback.setFilesTableData(StatsResult.FT_HEADERS, result.getFilesTable());
        callback.setTextData(result.getTextData());
        callback.finishData();

        String internalDir = p.getProjectProperties().getProjectInternal();
        // removing old stats
        try {
            File oldstats = new File(internalDir + "word_counts");
            if (oldstats.exists()) {
                oldstats.delete();
            }
        } catch (Exception e) {
        }

        // now dump file based word counts to disk
        String fn = internalDir + OConsts.STATS_FILENAME;
        Statistics.writeStat(internalDir, result);
        callback.setDataFile(fn);
    }
}
