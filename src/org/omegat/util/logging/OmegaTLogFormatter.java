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

import java.util.Random;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

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

    @Override
    public String format(final LogRecord record) {
        final StringBuilder result = new StringBuilder();
        for (String str : record.getMessage().split("\n")) {
            result.append(lineMark).append(": ").append(str).append(
                    lineSeparator);
        }
        return result.toString();
    }
}
