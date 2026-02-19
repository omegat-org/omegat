/*
 * OmegaT - Computer Assisted Translation (CAT) tool
 *          with fuzzy matching, translation memory, keyword search,
 *          glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2011 Alex Buloichik
 *                2026 Hiroshi Miura
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.omegat.core.statistics.writer;

import org.omegat.core.statistics.dso.StatsResult;
import org.omegat.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public abstract class AbstractStatisticsWriter {

    abstract void write(StatsResult result, Writer out) throws IOException;
    /**
     * Writes the statistics result to the specified file.
     *
     * @param statFile
     *            the file the statistics should be written
     * @param result
     *            the statistics result object containing the data to be written
     */
    public void writeStat(File statFile, StatsResult result) {
        try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(statFile), StandardCharsets.UTF_8)) {
            write(result, out);
        } catch (Exception ex) {
            Log.log(ex);
        }
    }

    protected void setDate(StatsResult result) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        result.setDate(dateFormat.format(new Date()));
    }

}
