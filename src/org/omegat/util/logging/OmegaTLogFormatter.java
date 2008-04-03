/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               Alex Buloichik
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

package org.omegat.util.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;

/**
 * Formatter for output data with session ID
 * 
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class OmegaTLogFormatter extends Formatter {

    protected static String lineMark;

    protected static String lineSeparator = System
            .getProperty("line.separator");

    private String logMask;
    private boolean isMaskContainsMark;
    private boolean isMaskContainsThreadName;
    private boolean isMaskContainsLevel;
    private boolean isMaskContainsText;
    private boolean isMaskContainsKey;

    static {
        // get a positive random number
        Random generator = new Random();
        generator.setSeed(System.currentTimeMillis()); // use current time as
        // seed
        int random = Math.abs(generator.nextInt());

        // convert the number to string, 5 chars max, pad with zero's if
        // necessary
        String sessionID = String.valueOf(random);
        if (sessionID.length() > 5)
            sessionID = sessionID.substring(0, 5);
        else if (sessionID.length() < 5)
            for (int i = 5; i > sessionID.length(); i++)
                sessionID = "0" + sessionID;

        lineMark = sessionID;
    }

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

        isMaskContainsKey = logMask.contains("$key");
        isMaskContainsLevel = logMask.contains("$level");
        isMaskContainsMark = logMask.contains("$mark");
        isMaskContainsText = logMask.contains("$text");
        isMaskContainsThreadName = logMask.contains("$threadName");
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
            format = record.getResourceBundle().getString(record.getMessage());
        } else {
            format = record.getMessage();
        }
        message = StaticUtils.format(format, record.getParameters());
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
    protected void appendFormattedLine(final StringBuilder out,
            final LogRecord record, final String line, final boolean isStack) {
        if (line.length() == 0)
            return;

        String res = logMask;
        if (isMaskContainsMark) {
            res = res.replace("$mark", lineMark);
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
        out.append(res).append(lineSeparator);
    }

    /**
     * Get localized level name.
     */
    protected String getLocalizedLevel(final Level logLevel) {
        String result;
        if (Level.INFO.getName().equals(logLevel.getName())) {
            result = OStrings.getString("LOG_INFO_ID");
        } else if (Level.SEVERE.getName().equals(logLevel.getName())) {
            result = OStrings.getString("LOG_ERROR_ID");
        } else if (Level.WARNING.getName().equals(logLevel.getName())) {
            result = OStrings.getString("LOG_WARNING_ID");
        } else {
            result = logLevel.getLocalizedName();
        }
        result = result.replace("{0}", "").replace("()", "").trim();
        if (result.endsWith(":")) {
            result = result.substring(0, result.length() - 1);
        }
        return result.trim();
    }
}
