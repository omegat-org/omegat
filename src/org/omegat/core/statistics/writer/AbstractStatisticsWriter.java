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
