/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
 Portions copyright 2007 Zoltan Bartko - bartkozoltan@bartkozoltan.com
               2009 Alex Buloichik
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

package org.omegat.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Date parser for the 'basic' ISO 8601 format that is advised for TMX dates.
 * There are many variations of the ISO8601 standard (the good thing about 
 * standards is that there are so many to choose from :( )
 * The {@link http://www.lisa.org/fileadmin/standards/tmx1.4/tmx.htm#refISO8601 
 * TMX specification} refers to 
 * {@link http://www.w3.org/TR/1998/NOTE-datetime-19980827}, the 'extended' 
 * format, but although referring to this standard, it recommends the 'basic' 
 * format: YYYYMMDDThhmmssZ. Since all translation tools conform to this format,
 * this parser only parses this variant of the 'basic' format.
 * 
 * @author Martin Fleurke
 */
public class TMXDateParser {

    /**
     * A DateFormat to format any date as YYYYMMDDThhmmssZ
     */
    private static DateFormat tmxDateFormat = initializeTMXDateFormat();

    /**
     * Creates a DateFormat with format YYYYMMDDThhmmssZ able to display a date 
     * in UTC time. This function is added so the timezone of the dateformat 
     * can be set
     * @return the DateFormat.
     */
    private static DateFormat initializeTMXDateFormat() {
        SimpleDateFormat tmxDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        tmxDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        return tmxDateFormat;
    }

    /**
     * Parse the given string as TMX date format 
     * (ISO 8601 compatible 'YYYYMMDDThhmmssZ')
     * and returns the Date
     * 
     * @param tmxDate A date representation in YYYYMMDDThhmmssZ format
     * @return a Date instance
     * @exception ParseException if the date is null or not valid
     */
    public static Date parse(String tmxDate) throws ParseException {
        if (tmxDate == null || tmxDate.length()!=16) { //parse function does not check this itself
            int offset = 0;
            if (tmxDate != null) {
                if (tmxDate.length()< 16) offset = tmxDate.length();
                else offset=16;
            }
            throw new ParseException("date '"+tmxDate+"' is null or not equal to YYYYMMDDThhmmssZ", offset);
        }
        return tmxDateFormat.parse(tmxDate);
    }

    /**
     * Returns the string representation of the date according to the preferred 
     * TMX date format 'YYYYMMDDThhmmssZ'
     * 
     * @param date a Date instance
     * @return a string representing the date in the ISO 8601 compatible format 'YYYYMMDDThhmmssZ'
     */
    public static String getTMXDate(Date date) {
        return tmxDateFormat.format(date);
    }
    
    /**
     * Returns the string representation of the date according to the preferred 
     * TMX date format 'YYYYMMDDThhmmssZ'
     * 
     * @param date unix timestamp (ms since 1970)
     * @return a string representing the date in the ISO 8601 compatible format 'YYYYMMDDThhmmssZ'
     */
    public static String getTMXDate(long date) {
        return tmxDateFormat.format(new Date(date));
    }
}