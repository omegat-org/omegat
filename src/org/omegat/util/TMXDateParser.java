/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Zoltan Bartko - bartkozoltan@bartkozoltan.com
               2009 Alex Buloichik
               2010 Martin Fleurke
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

package org.omegat.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Date parser for the 'basic' ISO 8601 format that is advised for TMX dates.
 * There are many variations of the ISO8601 standard (the good thing about
 * standards is that there are so many to choose from :( ) The
 * <a href="http://ttt.org/oscarstandards/tmx/#refISO8601">
 * TMX specification</a> refers to
 * <a href="http://www.w3.org/TR/1998/NOTE-datetime-19980827">the 'extended'
 * format</a>, but although referring to this standard, it recommends the
 * 'basic' format: YYYYMMDDThhmmssZ. Since all translation tools conform to this
 * format, this parser only parses this variant of the 'basic' format.
 * <p>
 * DateFormat is not thread-safe, so this class must be instantiated.
 *
 * @author Martin Fleurke
 */
public class TMXDateParser {

    /**
     * A DateFormat to format any date as YYYYMMDDThhmmssZ
     */
    private final DateFormat tmxDateFormat;

    /**
     * Wraps a DateFormat with format YYYYMMDDThhmmssZ able to display a date
     * in UTC time.
     */
    public TMXDateParser() {
        tmxDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        tmxDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
    }

    /**
     * Parse the given string as TMX date format (ISO 8601 compatible
     * 'YYYYMMDDThhmmssZ') and returns the Date
     *
     * @param tmxDate
     *            A date representation in YYYYMMDDThhmmssZ format
     * @return a Date instance
     * @exception ParseException
     *                if the date is null or not valid
     */
    public Date parse(String tmxDate) throws ParseException {
        if (tmxDate == null || tmxDate.length() != 16) {
            // Parse function does not check this itself.
            int offset = tmxDate == null ? 0 : Math.min(tmxDate.length(), 16);
            throw new ParseException("date '" + tmxDate + "' is null or not equal to YYYYMMDDThhmmssZ",
                    offset);
        }
        return tmxDateFormat.parse(tmxDate);
    }

    /**
     * Returns the string representation of the date according to the preferred
     * TMX date format 'YYYYMMDDThhmmssZ'
     *
     * @param date
     *            a Date instance
     * @return a string representing the date in the ISO 8601 compatible format
     *         'YYYYMMDDThhmmssZ'
     */
    public String getTMXDate(Date date) {
        return tmxDateFormat.format(date);
    }

    /**
     * Returns the string representation of the date according to the preferred
     * TMX date format 'YYYYMMDDThhmmssZ'
     *
     * @param date
     *            unix timestamp (ms since 1970)
     * @return a string representing the date in the ISO 8601 compatible format
     *         'YYYYMMDDThhmmssZ'
     */
    public String getTMXDate(long date) {
        return tmxDateFormat.format(new Date(date));
    }
}
