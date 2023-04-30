/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               Alex Buloichik
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

package org.omegat.util.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * Formatter for output data with session ID
 *
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class OmegaTLogFormatter extends Formatter {

    // Line mark is a five-character random number
    protected static final String LINE_MARK = String.format("%05d", ThreadLocalRandom.current().nextInt(100000));

    private String logMask;
    private boolean isMaskContainsMark;
    private boolean isMaskContainsThreadName;
    private boolean isMaskContainsLevel;
    private boolean isMaskContainsText;
    private boolean isMaskContainsKey;
    private boolean isMaskContainsLoggerName;
    private boolean isMaskContainsTime;

    private String defaultTimeFormat = "HH:mm:ss";

    /**
     * We have to use ThreadLocal for formatting time because DateFormat is not
     * thread safe.
     */
    private ThreadLocal<SimpleDateFormat> timeFormatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(defaultTimeFormat);
        }
    };

    /**
     * Initialize formatter.
     */
    public OmegaTLogFormatter() {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();

        logMask = manager.getProperty(cname + ".mask");
        if (logMask == null) {
            logMask = "$mark: $level: $text $key";
        }

        String timeFormat = manager.getProperty(cname + ".timeFormat");
        if (timeFormat != null) {
            defaultTimeFormat = timeFormat;
        }

        isMaskContainsKey = logMask.contains("$key");
        isMaskContainsLevel = logMask.contains("$level");
        isMaskContainsMark = logMask.contains("$mark");
        isMaskContainsTime = logMask.contains("$time");
        isMaskContainsText = logMask.contains("$text");
        isMaskContainsThreadName = logMask.contains("$threadName");
        isMaskContainsLoggerName = logMask.contains("$loggerName");
    }

    /**
     * Format output message.
     */
    @Override
    public String format(final LogRecord record) {
        final StringBuilder result = new StringBuilder();
        String message;
        String format;
        if (record.getResourceBundle() != null) {
            try {
                format = record.getResourceBundle().getString(record.getMessage());
            } catch (Exception ex) {
                // looks like ID not found in bundle
                format = record.getMessage();
            }
        } else {
            format = record.getMessage();
        }
        if (format == null) {
            format = "null";
        }
        if (record.getParameters() == null) {
            message = format;
        } else {
            message = StringUtil.format(format, record.getParameters());
        }
        String[] lines = message.split("\r|\n");
        for (String line : lines) {
            appendFormattedLine(result, record, line, false);
        }
        if (record.getThrown() != null) {
            StringWriter stackTrace = new StringWriter();

            record.getThrown().printStackTrace(new PrintWriter(stackTrace));
            for (String line : stackTrace.toString().split("\r|\n")) {
                appendFormattedLine(result, record, line, true);
            }
        }
        return result.toString();
    }

    /**
     * Format one line and append to output.
     */
    protected void appendFormattedLine(final StringBuilder out, final LogRecord record, final String line,
            final boolean isStack) {
        if (line.isEmpty()) {
            return;
        }

        String res = logMask;
        if (isMaskContainsMark) {
            res = res.replace("$mark", LINE_MARK);
        }
        if (isMaskContainsTime) {
            res = res.replace("$time", timeFormatter.get().format(new Date()));
        }
        if (isMaskContainsLoggerName) {
            res = res.replace("$loggerName", record.getLoggerName());
        }
        if (isMaskContainsThreadName) {
            res = res.replace("$threadName", Thread.currentThread().getName());
        }
        if (isMaskContainsLevel) {
            res = res.replace("$level", getLocalizedLevel(record.getLevel()));
        }
        if (isMaskContainsText) {
            res = res.replace("$text", line);
        }
        if (isMaskContainsKey) {
            if (record.getResourceBundle() != null && !isStack) {
                res = res.replace("$key", "(" + record.getMessage() + ")");
            } else {
                res = res.replace("$key", "");
            }
        }
        out.append(res).append(System.lineSeparator());
    }

    /**
     * Get localized level name.
     */
    protected String getLocalizedLevel(final Level logLevel) {
        String result;
        if (Level.INFO.getName().equals(logLevel.getName())) {
            result = OStrings.getString("LOG_LEVEL_INFO");
        } else if (Level.SEVERE.getName().equals(logLevel.getName())) {
            result = OStrings.getString("LOG_LEVEL_SEVERE");
        } else if (Level.WARNING.getName().equals(logLevel.getName())) {
            result = OStrings.getString("LOG_LEVEL_WARNING");
        } else {
            result = logLevel.getName();
        }
        return result.trim();
    }
}
